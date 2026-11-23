package com.dbguardian.test;

import javax.sql.DataSource;

import com.dbguardian.test.entity.User;
import com.dbguardian.test.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dbguardian.spring.RoutingContextHolder;
import io.dbguardian.spring.runtime.ClusterRuntimeStateManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JpaReadWriteSplittingTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserService users;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private ClusterRuntimeStateManager state;
    @Autowired
    @Qualifier("dbguardianMasterDataSource")
    private DataSource master;
    @Autowired
    @Qualifier("dbguardianSlaveDataSource")
    private DataSource slave;

    @BeforeEach
    void prepareDistinctDatabases() {
        setupTables(master, "master-only");
        setupTables(slave, "slave-only");
        state.updateSlaveStatus("slave", true, true, 0L, "test_replica_ready");
        state.restoreSlaveReads("master", "slave", "test_replica_ready");
    }

    @Test
    void standardQueriesReadReplicaButExplicitPrimaryQueryReadsPrimary() {
        assertEquals("slave-only", users.findById(1L).orElseThrow().getUsername());
        assertTrue(users.findByUsername("slave-only").isPresent());
        assertFalse(users.findByUsername("master-only").isPresent());
        assertEquals("slave-only", users.listUsers().get(0).getUsername());
        assertEquals("master-only", users.findByIdFromMaster(1L).orElseThrow().getUsername());
        assertNull(RoutingContextHolder.get());
    }

    @Test
    void writesAndReadWithinWriteTransactionUsePrimary() {
        assertInstanceOf(JpaTransactionManager.class, transactionManager);
        User created = users.create(user("created"));
        assertTrue(created.getId() > 1);
        assertTrue(users.findByIdFromMaster(created.getId()).isPresent());
        assertFalse(users.findById(created.getId()).isPresent());

        // Only the primary has this row, so update also verifies the read inside the write transaction.
        users.update(created.getId(), user("updated")).orElseThrow();
        assertEquals("updated", users.findByIdFromMaster(created.getId()).orElseThrow().getUsername());
        users.delete(created.getId());
        assertFalse(users.findByIdFromMaster(created.getId()).isPresent());
        assertEquals(1, new JdbcTemplate(slave).queryForObject("SELECT COUNT(*) FROM t_user", Integer.class));
        assertNull(RoutingContextHolder.get());
    }

    @Test
    void fallsBackToPrimaryAndResumesReplicaReads() {
        state.markSlaveReadsFallbackToMaster("master", "test_replica_unavailable");
        assertEquals("master-only", users.findById(1L).orElseThrow().getUsername());
        state.restoreSlaveReads("master", "slave", "test_replica_recovered");
        assertEquals("slave-only", users.findById(1L).orElseThrow().getUsername());
    }

    @Test
    void routesToPromotedPoolAndBackToOriginalPrimary() {
        // Replay role changes without issuing database promotion commands.
        state.replayStatusMessage("FAILOVER:master:slave");
        User promotedWrite = users.create(user("promoted-write"));
        assertEquals("promoted-write", users.findByIdFromMaster(promotedWrite.getId()).orElseThrow().getUsername());
        assertEquals(1, new JdbcTemplate(master).queryForObject("SELECT COUNT(*) FROM t_user", Integer.class));
        state.replayStatusMessage("FAILBACK:master:slave");
        assertEquals("master-only", users.findByIdFromMaster(1L).orElseThrow().getUsername());
        assertEquals("slave-only", users.findById(1L).orElseThrow().getUsername());
    }

    @Test
    void orderEndpointsReadReplicaAndWritePrimary() throws Exception {
        mvc.perform(get("/order/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNo").value("slave-only"));
        mvc.perform(get("/order/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value("slave-only"));
        mvc.perform(get("/order/by-user/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNo").value("slave-only"));
        mvc.perform(get("/order/by-order-no/{orderNo}", "slave-only"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value("slave-only"));
        mvc.perform(get("/order/master/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value("master-only"));

        String created = mvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderNo":"ORD-NEW","userId":1,"amount":12.50}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();
        assertTrue(id > 1L);
        mvc.perform(get("/order/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(put("/order/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderNo":"ORD-UPDATED","userId":1,"amount":20.50,"status":"PAID"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
        mvc.perform(get("/order/master/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value("ORD-UPDATED"))
                .andExpect(jsonPath("$.amount").value(20.5));
        mvc.perform(delete("/order/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/order/master/{id}", id)).andExpect(status().isNotFound());
        assertEquals(1, new JdbcTemplate(slave).queryForObject("SELECT COUNT(*) FROM t_order", Integer.class));
        assertNull(RoutingContextHolder.get());
    }

    private static User user(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.local");
        return user;
    }

    private static void setupTables(DataSource dataSource, String marker) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("DROP TABLE IF EXISTS t_user");
        jdbc.execute("""
                CREATE TABLE t_user (id BIGINT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE,
                    email VARCHAR(100) NOT NULL, phone VARCHAR(20), status INT NOT NULL DEFAULT 1,
                    create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP)
                """);
        jdbc.update("INSERT INTO t_user (username, email) VALUES (?, ?)", marker, marker + "@test.local");
        jdbc.execute("DROP TABLE IF EXISTS t_order");
        jdbc.execute("""
                CREATE TABLE t_order (id BIGINT AUTO_INCREMENT PRIMARY KEY, order_no VARCHAR(50) NOT NULL UNIQUE,
                    user_id BIGINT NOT NULL, amount DECIMAL(10,2) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                    create_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    update_time DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP)
                """);
        jdbc.update("INSERT INTO t_order (order_no, user_id, amount) VALUES (?, 1, 10.00)", marker);
    }
}
