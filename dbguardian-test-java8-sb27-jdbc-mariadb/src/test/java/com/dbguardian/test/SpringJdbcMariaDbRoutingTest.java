package com.dbguardian.test;

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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
class SpringJdbcMariaDbRoutingTest {

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
        state.replayStatusMessage("FAILBACK:master:slave");
        state.updateSlaveStatus("slave", true, true, 0L, "test_replica_ready");
    }

    @Test
    void selectsUseReplicaUnlessBoundToWriterTransaction() {
        assertTrue(transactionManager instanceof DataSourceTransactionManager);
        assertEquals("slave-only", users.findById(1L).get().getUsername());
        assertEquals("slave-only", users.listUsers().get(0).getUsername());
        assertTrue(users.findByUsername("slave-only").isPresent());
        assertFalse(users.findByUsername("master-only").isPresent());
        assertEquals("master-only", users.findByIdFromMaster(1L).get().getUsername());
        assertNull(RoutingContextHolder.get());
    }

    @Test
    void writesReturnGeneratedKeysAndReadBackOnWriter() {
        User input = user("created");
        input.setPhone("1234567890");
        User created = users.create(input);
        long id = created.getId();
        assertTrue(id > 1L);
        assertNotNull(created.getCreateTime());
        assertEquals("1234567890", created.getPhone());
        assertFalse(users.findById(id).isPresent());
        User updated = users.update(id, user("updated")).get();
        assertEquals("updated", updated.getUsername());
        assertNull(updated.getPhone());
        assertEquals("updated", users.findByIdFromMaster(id).get().getUsername());
        assertTrue(users.delete(id));
        assertFalse(users.findByIdFromMaster(id).isPresent());
        assertEquals(1, count(slave, "SELECT COUNT(*) FROM t_user"));
        assertNull(RoutingContextHolder.get());
    }

    @Test
    void fallsBackToWriterAndResumesReplicaReads() {
        state.markSlaveReadsFallbackToMaster("master", "test_replica_unavailable");
        assertEquals("master-only", users.findById(1L).get().getUsername());
        state.restoreSlaveReads("master", "slave", "test_replica_recovered");
        assertEquals("slave-only", users.findById(1L).get().getUsername());
    }

    @Test
    void followsPromotedWriterAndThenReturnsToOriginalWriter() {
        state.replayStatusMessage("FAILOVER:master:slave");
        User promotedWrite = users.create(user("promoted-write"));
        assertEquals("promoted-write", users.findByIdFromMaster(promotedWrite.getId()).get().getUsername());
        assertEquals(1, count(master, "SELECT COUNT(*) FROM t_user"));
        assertEquals(2, count(slave, "SELECT COUNT(*) FROM t_user"));
        state.replayStatusMessage("FAILBACK:master:slave");
        assertEquals("master-only", users.findByIdFromMaster(1L).get().getUsername());
        assertEquals("slave-only", users.findById(1L).get().getUsername());
    }

    @Test
    void orderEndpointsReadReplicaAndWriteToWriter() throws Exception {
        mvc.perform(get("/order/list")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNo").value("slave-only"));
        mvc.perform(get("/order/by-user/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNo").value("slave-only"));
        mvc.perform(get("/order/by-order-no/slave-only")).andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
        mvc.perform(get("/order/master/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value("master-only"));

        String created = mvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"ORD-NEW\",\"userId\":1,\"amount\":12.50}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();
        mvc.perform(get("/order/{id}", id)).andExpect(status().isNotFound());
        mvc.perform(put("/order/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderNo\":\"ORD-NEW\",\"userId\":1,\"amount\":20.50,\"status\":\"PAID\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAID"));
        mvc.perform(get("/order/master/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(20.5));
        mvc.perform(delete("/order/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/order/master/{id}", id)).andExpect(status().isNotFound());
        assertEquals(1, count(slave, "SELECT COUNT(*) FROM t_order"));
        assertNull(RoutingContextHolder.get());
    }

    private static User user(String username) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@test.local");
        return user;
    }

    private static int count(DataSource dataSource, String sql) {
        return new JdbcTemplate(dataSource).queryForObject(sql, Integer.class);
    }

    private static void setupTables(DataSource dataSource, String label) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("DROP TABLE IF EXISTS t_order");
        jdbc.execute("DROP TABLE IF EXISTS t_user");
        jdbc.execute("CREATE TABLE t_user (id BIGINT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) NOT NULL UNIQUE, "
                + "email VARCHAR(100) NOT NULL, phone VARCHAR(20), status INT NOT NULL DEFAULT 1, "
                + "create_time TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "update_time TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        jdbc.execute("CREATE TABLE t_order (id BIGINT AUTO_INCREMENT PRIMARY KEY, order_no VARCHAR(50) NOT NULL UNIQUE, "
                + "user_id BIGINT NOT NULL, amount DECIMAL(10,2) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'PENDING', "
                + "create_time TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "update_time TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP)");
        jdbc.update("INSERT INTO t_user (username, email) VALUES (?, ?)", label, label + "@test.local");
        jdbc.update("INSERT INTO t_order (order_no, user_id, amount) VALUES (?, 1, 10.00)", label);
    }
}
