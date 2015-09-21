package com.threebird.recorder.utils.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import com.threebird.recorder.utils.resources.ResourceUtils;

/**
 * A small library that handles SQLite Connections and PreparedStatements for
 * you. Use the {@link SqliteDao#query(SqlQueryData)} or
 * {@link SqliteDao#update(SqlQueryData)} methods to interact with the DB.
 */
public class SqliteDao
{
  @FunctionalInterface
  public interface SqlTask
  {
    public void run() throws Exception;
  }

  private static String DATABASE =
      String.format( "jdbc:sqlite:%s", ResourceUtils.getDb().getAbsolutePath() );

  private static Connection conn;

  /**
   * @return false if a Connection is already open, or true if a new Connection
   *         was successfully created
   * 
   * @throws Exception
   */
  private static boolean open() throws Exception
  {
    if (conn == null) {
      DriverManager.registerDriver( new org.sqlite.JDBC() );
      conn = DriverManager.getConnection( DATABASE );
    } else if (!conn.isClosed()) {
      return false;
    }

    conn = DriverManager.getConnection( DATABASE );
    return true;
  }

  /**
   * Makes a PreparedStatement, executes it, and handles the returned ResultSet
   * 
   * @throws Exception
   */
  private static void execute( SqlQueryData sqd, boolean isDML ) throws Exception
  {
    boolean topLevel = open();
    PreparedStatement stmt = conn.prepareStatement( sqd.getSql() );

    int i = 1;
    for (Object o : sqd.getSqlParams()) {
      stmt.setObject( i++, o );
    }

    if (isDML) {
      stmt.executeUpdate();
      sqd.handle( stmt.getGeneratedKeys() );
    } else {
      sqd.handle( stmt.executeQuery() );
    }
    stmt.close();

    if (topLevel) {
      conn.close();
    }
  }

  /**
   * Executes a SQL Query (ie. a select statment) according to the data provided
   * by a {@link SqlQueryData}. The ResultSet given to the 'handle' function
   * consists of each column specified in the query. Any calls to
   * {@link SqliteDao#query(SqlQueryData)} or
   * {@link SqliteDao#update(SqlQueryData)} within
   * {@link SqlQueryData#handle(java.sql.ResultSet)} will occur within the same
   * transaction.
   * 
   * @throws Exception
   */
  synchronized public static void query( final SqlQueryData sqc ) throws Exception
  {
    execute( sqc, false );
  }

  /**
   * Executes a DML statment (insert, update, delete) with the data provided by
   * a {@link SqlQueryData}. The ResultSet given to the 'handle' function
   * consists of the statement's generated keys. Any calls to
   * {@link SqliteDao#query(SqlQueryData)} or
   * {@link SqliteDao#update(SqlQueryData)} within
   * {@link SqlQueryData#handle(java.sql.ResultSet)} will occur within the same
   * transaction.
   * 
   * @throws Exception
   */
  synchronized public static void update( final SqlQueryData sqc ) throws Exception
  {
    execute( sqc, true );
  }

  /**
   * Same as calling SqliteDao.update( SqlQueryData.create( sql ) );
   * 
   * @throws Exception
   */
  synchronized public static void update( final String sql ) throws Exception
  {
    update( SqlQueryData.create( sql ) );
  }
}
