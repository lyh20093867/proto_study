package com.moji.pool;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
abstract public class AbstractDataSource {
    protected HikariDataSource bigDataMojiDataSource = new HikariDataSource();

    protected HikariDataSource getDataSource() {
        initSource();
        return bigDataMojiDataSource;
    }

    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    /**
     * 对bigDataMojiDataSource进行初始化
     */
    protected abstract void initSource();

    public ResultSet queryBySql(Connection conn, String sql) throws SQLException {
        return queryBySql(conn.createStatement(), sql);
    }

    public ResultSet queryBySql(Statement state, String sql) throws SQLException {
        return state.executeQuery(sql);
    }

    public void close(Connection conn) throws SQLException {
        if (conn != null && !conn.isClosed()) conn.close();
    }

    public void closePool(HikariDataSource dataSource) {
        if (dataSource != null && !dataSource.isClosed()) dataSource.close();
    }
}
