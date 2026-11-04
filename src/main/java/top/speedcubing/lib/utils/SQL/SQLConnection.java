package top.speedcubing.lib.utils.SQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;

public class SQLConnection implements AutoCloseable {

    private final Connection connection;

    public SQLConnection(String url, String user, String password) {
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    public SQLConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    //start the CRUD string to prepare
    public SQLPrepare select(String field) {
        return new SQLPrepare(this, new SQLBuilder().select(field));
    }

    public SQLPrepare insert(String table, String field) {
        return new SQLPrepare(this, new SQLBuilder().insert(table, field));
    }

    public SQLPrepare update(String table) {
        return new SQLPrepare(this, new SQLBuilder().update(table));
    }

    public SQLPrepare delete(String table) {
        return new SQLPrepare(this, new SQLBuilder().delete(table));
    }

    //direct execute CRUD
    public ResultSet select(String table, String field, String where) {
        return executeQuery("SELECT " + field + " FROM " + table + " WHERE " + where);
    }

    public int insert(String table, String field, String value) {
        return executeUpdate("INSERT INTO `" + table + "` (" + field + ") VALUES (" + value + ")");
    }

    public int update(String table, String field, String where) {
        return executeUpdate("UPDATE `" + table + "` SET " + field + " WHERE " + where);
    }

    public int delete(String table, String where) {
        return executeUpdate("DELETE FROM `" + table + "` WHERE " + where);
    }

    //create prepare-statement from SQL
    public SQLPrepare prepare(String sql) {
        return new SQLPrepare(this, new SQLBuilder().append(sql));
    }

    //exists
    public Boolean exist(String table, String where) {
        SQLPrepare prepare = select("*").from(table).where(where);
        try (ResultSet r = prepare.executeQuery()) {
            return r.next();
        } catch (SQLException e) {
            throw new SQLRuntimeException(prepare.toSQL(), e);
        }
    }

    //execute SQL

    public SQLResult executeResult(String sql) {
        return new SQLResult(executeQuery(sql));
    }

    public void execute(String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new SQLRuntimeException(sql, e);
        }
    }

    public ResultSet executeQuery(String sql) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            return statement.executeQuery();
        } catch (SQLException e) {
            throw new SQLRuntimeException(sql, e);
        }
    }

    public int executeUpdate(String sql) {
        try (PreparedStatement statement = connection.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLRuntimeException(sql, e);
        }
    }

    public void executeBatch(String... sqls) {
        executeBatch(Arrays.asList(sqls));
    }

    public void executeBatch(Collection<String> sqls) {
        try (Statement statement = connection.createStatement()) {
            for (String sql : sqls) {
                statement.addBatch(sql);
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new SQLRuntimeException(sqls, e);
        }
    }

    //transaction
    public void startTransaction() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    public void stopTransaction() {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    public void commit() {
        try {
            connection.commit();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        }
    }

    public void doTransaction(Runnable r) {
        startTransaction();
        try {
            r.run();
            commit();
        } catch (RuntimeException e) {
            rollback();
            throw e;
        } finally {
            stopTransaction();
        }
    }
}
