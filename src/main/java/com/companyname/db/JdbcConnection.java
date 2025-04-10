package com.companyname.db;

import com.companyname.config.Properties;
import com.companyname.config.props.PropertyListReader;
import com.companyname.config.props.PropertyReader;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcConnection {

    private static final String DEFAULT_JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private String jdbcDriver;
    private String jdbcUri;
    private String jdbcUser;
    private String jdbcPassword;

    public static JdbcConnection loadFromProperties(String envType) {
        JdbcConnection connection = new JdbcConnection();
        connection.setJdbcDriver(PropertyReader.getProperty(StringUtils.joinWith(".", envType, Properties.DB_DRIVER), DEFAULT_JDBC_DRIVER))
                .setJdbcUri(PropertyReader.getProperty(StringUtils.joinWith(".", envType, Properties.DB_URL)))
                .setJdbcUser(PropertyReader.getProperty(StringUtils.joinWith(".", envType, Properties.DB_USER)))
                .setJdbcPassword(PropertyReader.getProperty(StringUtils.joinWith(".", envType, Properties.DB_PASSWORD)));
        return connection;
    }

    public static JdbcConnection loadFromProperties(String envType, String propListName) {
        JdbcConnection connection = new JdbcConnection();
        connection.setJdbcDriver(PropertyListReader.get(propListName).getProperty(StringUtils.joinWith(".", envType, Properties.DB_DRIVER), DEFAULT_JDBC_DRIVER))
                .setJdbcUri(PropertyListReader.get(propListName).getProperty(StringUtils.joinWith(".", envType, Properties.DB_URL)))
                .setJdbcUser(PropertyListReader.get(propListName).getProperty(StringUtils.joinWith(".", envType, Properties.DB_USER)))
                .setJdbcPassword(PropertyListReader.get(propListName).getProperty(StringUtils.joinWith(".", envType, Properties.DB_PASSWORD)));
        return connection;
    }

    public String getJdbcDriver() {
        return jdbcDriver;
    }

    public JdbcConnection setJdbcDriver(String jdbcDriver) {
        if (StringUtils.isBlank(jdbcDriver)) {
            jdbcDriver = DEFAULT_JDBC_DRIVER;
        }
        this.jdbcDriver = jdbcDriver;
        System.setProperty("jdbc.drivers", jdbcDriver);
        return this;
    }

    public String getJdbcUri() {
        return jdbcUri;
    }

    public JdbcConnection setJdbcUri(String jdbcUri) {
        this.jdbcUri = jdbcUri;
        return this;
    }

    public String getJdbcUser() {
        return jdbcUser;
    }

    public JdbcConnection setJdbcUser(String jdbcUser) {
        this.jdbcUser = jdbcUser;
        return this;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public JdbcConnection setJdbcPassword(String jdbcPassword) {
        this.jdbcPassword = jdbcPassword;
        return this;
    }

    public Connection getJdbcConnection() {
        if (StringUtils.isBlank(jdbcUri) || StringUtils.isBlank(jdbcUser) || StringUtils.isBlank(jdbcPassword)) {
            throw new RuntimeException(String.format("Not all connection parameters are set, please check: jdbcUri=%1s, jdbcUser=%2s, jdbcPassword=%3s", jdbcUri, jdbcUser, jdbcPassword));
        }
        try {
            return DriverManager.getConnection(jdbcUri, jdbcUser, jdbcPassword);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
