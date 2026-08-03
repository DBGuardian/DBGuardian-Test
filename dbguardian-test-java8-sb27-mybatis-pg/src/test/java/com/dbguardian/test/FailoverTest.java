package com.dbguardian.test;

import io.dbguardian.boot2.config.DbGuardianBoot2AutoConfiguration;
import io.dbguardian.core.datasource.DataSourceWrapper;
import io.dbguardian.core.registry.DataSourceRegistry;
import io.dbguardian.core.recovery.DatabaseRecoveryHandler;
import io.dbguardian.core.recovery.DatabaseRecoverySettings;
import io.dbguardian.spi.DatabaseDialect;
import io.dbguardian.spring.coordination.DatasourceCoordinationService;
import io.dbguardian.spring.failover.DataSourceHealthChecker;
import io.dbguardian.spring.failover.FailoverController;
import io.dbguardian.spring.runtime.ClusterRuntimeStateManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PostgreSQL 故障转移测试。
 */
@SpringBootTest(classes = {Application.class, DbGuardianBoot2AutoConfiguration.class})
@ActiveProfiles("test")
public class FailoverTest {

    @Autowired(required = false)
    private DatasourceCoordinationService coordinationService;

    @Test
    public void testCurrentDataSourceStatus() {
        assertNotNull(coordinationService, "协调服务应被 starter 注入");
        DatasourceCoordinationService.CoordinationStatus status = coordinationService.getCoordinationStatus();
        assertNotNull(status, "协调状态不应为空");
        assertTrue(DatasourceCoordinationService.STATUS_NORMAL.equals(status.getMasterStatus())
                || DatasourceCoordinationService.STATUS_SLAVE_PROMOTED.equals(status.getMasterStatus()),
                "主库状态应由 starter 管理");
    }

    @Test
    public void testPostgreSqlSlaveReadResumesOnlyAfterStreamingReplication() throws Exception {
        DataSourceRegistry registry = new DataSourceRegistry();
        ClusterRuntimeStateManager runtime = new ClusterRuntimeStateManager();
        runtime.initialize("postgresql-routing", "master", "slave");

        PostgreSqlRecoveryController failover = new PostgreSqlRecoveryController(registry);
        failover.setRuntimeStateManager(runtime);
        DatabaseRecoverySettings settings = new DatabaseRecoverySettings();
        settings.setEnabled(true);
        settings.setDataDirectory("/var/lib/postgresql/data");
        failover.setRecoverySettings(settings);

        DataSourceHealthChecker checker = new DataSourceHealthChecker(registry, failover);
        checker.setRuntimeStateManager(runtime);
        checker.setFailoverEnabled(false);
        activate(checker);

        PostgreSqlHealthDataSource master = PostgreSqlHealthDataSource.primary();
        PostgreSqlHealthDataSource slave = PostgreSqlHealthDataSource.standby(false);
        registry.registerMaster("master", wrapper("master", true, master));
        registry.registerSlave("slave", wrapper("slave", false, slave));

        invoke(checker, "checkSlaves");
        assertEquals(0, registry.getAvailableSlaveCount(), "不可用从库必须移出读池");
        assertEquals("master", runtime.current().getActiveReadNodeId(), "从库失效时读请求应路由主库");
        assertTrue(runtime.current().isSlaveReadsBlocked(), "从库失效期间应阻断从读");

        slave.setConnectionHealthy(true);
        invoke(checker, "checkSlaves");
        assertTrue(failover.replicationRebuilt, "从库连接恢复后应先重建 PostgreSQL 复制");
        assertEquals("slave", failover.recoveredReplicaId, "复制重建目标应为恢复的从库");
        assertEquals(0, registry.getAvailableSlaveCount(), "未确认流复制时不得恢复从读");

        slave.setStreaming(true);
        invoke(checker, "checkSlaves");
        assertEquals(1, registry.getAvailableSlaveCount(), "流复制确认后从库应重新加入读池");
        assertEquals("slave", runtime.current().getActiveReadNodeId(), "读请求应恢复路由从库");
        assertFalse(runtime.current().isSlaveReadsBlocked(), "流复制确认后应解除从读阻断");
    }

    private static DataSourceWrapper wrapper(String id, boolean master, DataSource dataSource) {
        return new DataSourceWrapper(id, "jdbc:postgresql://127.0.0.1:5432/appdb", "postgres",
                "postgres", "org.postgresql.Driver", 100, 100, master, dataSource);
    }

    private static void activate(DataSourceHealthChecker checker) throws Exception {
        Field running = DataSourceHealthChecker.class.getDeclaredField("running");
        running.setAccessible(true);
        ((AtomicBoolean) running.get(checker)).set(true);
    }

    private static void invoke(Object target, String methodName) throws Exception {
        Method method = DataSourceHealthChecker.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(target);
    }

    private static final class PostgreSqlRecoveryController extends FailoverController {
        private boolean replicationRebuilt;
        private String recoveredReplicaId;

        private PostgreSqlRecoveryController(DataSourceRegistry registry) {
            super(registry, null);
        }

        @Override
        protected boolean rebuildPostgreSqlSlaveReplication(DataSourceWrapper recoveredSlave,
                                                            DataSourceWrapper currentMaster,
                                                            DatabaseDialect dialect,
                                                            DatabaseRecoveryHandler handler) {
            replicationRebuilt = true;
            recoveredReplicaId = recoveredSlave.getId();
            return true;
        }
    }

    private static final class PostgreSqlHealthDataSource implements DataSource {
        private boolean connectionHealthy;
        private boolean streaming;

        static PostgreSqlHealthDataSource primary() {
            PostgreSqlHealthDataSource source = new PostgreSqlHealthDataSource();
            source.connectionHealthy = true;
            return source;
        }

        static PostgreSqlHealthDataSource standby(boolean healthy) {
            PostgreSqlHealthDataSource source = new PostgreSqlHealthDataSource();
            source.connectionHealthy = healthy;
            return source;
        }

        void setConnectionHealthy(boolean healthy) {
            connectionHealthy = healthy;
        }

        void setStreaming(boolean value) {
            streaming = value;
        }

        @Override
        public Connection getConnection() {
            InvocationHandler connection = (proxy, method, args) -> {
                if ("isValid".equals(method.getName())) return connectionHealthy;
                if ("createStatement".equals(method.getName())) return statement();
                return defaultValue(method.getReturnType());
            };
            return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Connection.class}, connection);
        }

        private Statement statement() {
            InvocationHandler statement = (proxy, method, args) -> {
                if ("executeQuery".equals(method.getName())) return replicaStatus();
                return defaultValue(method.getReturnType());
            };
            return (Statement) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Statement.class}, statement);
        }

        private ResultSet replicaStatus() {
            InvocationHandler resultSet = new InvocationHandler() {
                private boolean consumed;

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    if ("next".equals(method.getName())) {
                        if (consumed) return false;
                        consumed = true;
                        return true;
                    }
                    if ("getString".equals(method.getName())) {
                        String column = String.valueOf(args[0]);
                        if ("replica_io_running".equals(column) || "replica_sql_running".equals(column)) {
                            return streaming ? "streaming" : "stopped";
                        }
                        if ("replay_lag_seconds".equals(column)) return "0";
                        if ("executed_position".equals(column)) return "0/100";
                    }
                    return defaultValue(method.getReturnType());
                }
            };
            return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {ResultSet.class}, resultSet);
        }

        @Override public Connection getConnection(String username, String password) { return getConnection(); }
        @Override public <T> T unwrap(Class<T> iface) throws SQLException { throw new SQLException("unsupported"); }
        @Override public boolean isWrapperFor(Class<?> iface) { return false; }
        @Override public java.io.PrintWriter getLogWriter() { return null; }
        @Override public void setLogWriter(java.io.PrintWriter out) { }
        @Override public void setLoginTimeout(int seconds) { }
        @Override public int getLoginTimeout() { return 0; }
        @Override public java.util.logging.Logger getParentLogger() { return java.util.logging.Logger.getGlobal(); }
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (Boolean.TYPE.equals(type)) return false;
        if (Character.TYPE.equals(type)) return '\0';
        if (Byte.TYPE.equals(type)) return (byte) 0;
        if (Short.TYPE.equals(type)) return (short) 0;
        if (Integer.TYPE.equals(type)) return 0;
        if (Long.TYPE.equals(type)) return 0L;
        if (Float.TYPE.equals(type)) return 0F;
        if (Double.TYPE.equals(type)) return 0D;
        return null;
    }
}