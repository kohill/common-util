package com.companyname.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;

public class DbUtilities {
    private static final Logger LOG = LogManager.getLogger(DbUtilities.class);
    private static final ValueExtractor<String> STRING_EXTRACTOR = (rs, ci) -> {
        try {
            return rs.getString(ci);
        } catch (SQLException e) {
            LOG.debug("Failed to call getString on column " + ci + "; original error: " + e.getCause() +
                    "\n Object#toString will be used instead.");
            Object o = rs.getObject(ci);
            return o == null ? null : o.toString();
        }
    };
    private final Connection connection;

    private DbUtilities(Connection connection) {
        this.connection = connection;
    }

    /**
     * Creates instance with connection parameters from properties.
     * <p>Example: 'envPrefix=pas' - requires properties below to be set:</p>
     * <ol>
     * <li>pas.db.url={db_url}</li>
     * <li>pas.db.user={username}</li>
     * <li>pas.db.password={password}</li>
     * <li>and optional: pas.db.driver={db_driver} Default = oracle.jdbc.driver.OracleDriver</li>
     * </ol>
     *
     * @param envPrefix - use predefined properties with env name prefix.
     * @return {@link DbUtilities} instance
     */
    public static DbUtilities get(String envPrefix) {
        return new DbUtilities(JdbcConnection.loadFromProperties(envPrefix).getJdbcConnection());
    }

    /**
     * Creates instance with connection parameters from properties with default 'envPrefix=pas'
     * <p>Requires properties below to be set:</p>
     * <ol>
     * <li>pas.db.url={db_url}</li>
     * <li>pas.db.user={username}</li>
     * <li>pas.db.password={password}</li>
     * <li>and optional: pas.db.driver={db_driver} Default = oracle.jdbc.driver.OracleDriver</li>
     * </ol>
     *
     * @return {@link DbUtilities} instance
     */
    public static DbUtilities get() {
        return get("pas");
    }

    /**
     * Creates instance of DbUtilities with manually created {@link JdbcConnection} instance;
     *
     * @param connection - manually created {@link JdbcConnection} instance
     * @return {@link DbUtilities} instance
     */
    public static DbUtilities get(JdbcConnection connection) {
        return new DbUtilities(connection.getJdbcConnection());
    }

    /**
     * Execute INSERT, UPDATE or DELETE SQL query
     *
     * @param sql  SQL query
     * @param args query arguments
     * @return number of affected rows
     */
    public int executeUpdate(String sql, Object... args) {
        return execute(PreparedStatement::executeUpdate, sql, args);
    }

    /**
     * Get the value of the first column in the first row as optional String
     *
     * @param sql  SQL query
     * @param args query arguments
     * @return optional String (empty if no rows were returned)
     */
    public Optional<String> getValue(String sql, Object... args) {
        return executeQuery(rs -> rs.next() ? Optional.ofNullable(STRING_EXTRACTOR.get(rs, 1)) : Optional.empty(), sql, args);
    }

    /**
     * Get the value of the first column in all returned rows treating them as values of desired type
     *
     * @param sql  SQL query
     * @param args query arguments
     * @return list of column values
     */
    public List<String> getColumn(String sql, Object... args) {
        return executeQuery(buildListProcessor(rs -> r -> STRING_EXTRACTOR.get(r, 1)), sql, args);
    }

    /**
     * Get the value of the first row treating column values as Strings
     *
     * @param sql  SQL query
     * @param args query arguments
     * @return row value (a map of column names and column values)
     */
    public Map<String, String> getRow(String sql, Object... args) {
        return executeQuery(rs -> rs.next() ? buildRowProcessor(rs.getMetaData()).process(rs) : new LinkedHashMap<>(), sql, args);
    }

    /**
     * Get all returned rows treating column values as Strings
     *
     * @param sql  SQL query
     * @param args query arguments
     * @return list of row values (each value a map of column names and values)
     */
    public List<Map<String, String>> getRows(String sql, Object... args) {
        return executeQuery(buildListProcessor(rs -> buildRowProcessor(rs.getMetaData())), sql, args);
    }

    /**
     * Execute provided action using parameterized SQL query
     *
     * @param <T>    action return type
     * @param action action to perform
     * @param sql    SQL query
     * @param params query arguments
     * @return action result
     */
    private <T> T execute(Action<T> action, String sql, Object... params) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (params != null || params.length == 0) {
                ListIterator<Object> iterator = Arrays.asList(params).listIterator();
                while (iterator.hasNext()) {
                    stmt.setObject(iterator.nextIndex() + 1, iterator.next());
                }
            }
            LOG.info("Executing SQL query: " + logFormatQuery(sql, params));
            T retValue = action.apply(stmt);
            LOG.info("SQL query returned: " + retValue);
            if (!stmt.isClosed()) {
                stmt.close();
            }
            return retValue;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute SQL query", e);
        } finally {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException("FAiled to close connection", e);
            }
        }
    }

    /**
     * Execute SQL query (use for SELECT's)
     *
     * @param <V>       expected result type
     * @param processor result set processor
     * @param sql       SQL query
     * @param params    query arguments
     * @return query result
     */
    private <V> V executeQuery(ResultProcessor<V> processor, String sql, Object... params) {
        return execute(ps -> processor.process(ps.executeQuery()), sql, params);
    }

    /**
     * Build processor for a whole row value in the result set
     *
     * @param metadata result set metadata
     * @return row value (a map of strings)
     */
    private ResultProcessor<Map<String, String>> buildRowProcessor(ResultSetMetaData metadata) {
        return rs -> {
            Map<String, String> rowValues = new LinkedHashMap<>();
            for (int i = 1; i <= metadata.getColumnCount(); i++) {
                rowValues.put(metadata.getColumnName(i), STRING_EXTRACTOR.get(rs, i));
            }
            return rowValues;
        };
    }

    /**
     * Build processor for items across all rows in the result set
     *
     * @param itemProcessorBuilder builder for an item processor (it can be either row processor or single cell processor)
     * @return list of values extracted from the rows
     */
    private <V> ResultProcessor<List<V>> buildListProcessor(ResultProcessor<ResultProcessor<V>> itemProcessorBuilder) {
        return rs -> {
            List<V> accumulator = new ArrayList<>();
            ResultProcessor<V> itemProcessor = itemProcessorBuilder.process(rs);
            while (rs.next()) {
                accumulator.add(itemProcessor.process(rs));
            }
            return accumulator;
        };
    }

    /**
     * Used to format log output
     */
    private String logFormatQuery(String sql, Object[] params) {
        return params.length == 0 ? sql : String.format("%1$s (with args %2$s)", sql, Arrays.asList(params));
    }

    /**
     * Action to perform on prepared statement
     *
     * @param <T> return type of the action
     */
    @FunctionalInterface
    private interface Action<T> {
        T apply(PreparedStatement ps) throws SQLException;
    }

    /**
     * Extractor of a single cell value
     *
     * @param <V> return type of the value
     */
    @FunctionalInterface
    private interface ValueExtractor<V> {
        V get(ResultSet rs, int columnIndex) throws SQLException;
    }

    /**
     * ResultSet processor
     *
     * @param <V> return type of the processing
     */
    @FunctionalInterface
    private interface ResultProcessor<V> {
        V process(ResultSet rs) throws SQLException;
    }

}