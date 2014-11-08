package com.threebird.recorder.persistence.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A small library that handles SQLite Connections and PreparedStatements for
 * you. Use the {@link SqliteDao#query(SqlQueryData)} or
 * {@link SqliteDao#update(SqlQueryData)} methods to interact with the DB.
 */
public class SqliteDao
{
  private static String DATABASE = "jdbc:sqlite:recorder.db";

  private static Connection conn;

  private interface ExecuteStatement
  {
    ResultSet execute( PreparedStatement stmt ) throws SQLException;
  }

  // Load the Sqlite JDBC driver
  static {
    try {
      DriverManager.registerDriver( new org.sqlite.JDBC() );
      conn = getConnection();
    } catch (SQLException e) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Returns a Connection to the local "recorder" database
   * 
   * @throws SQLException
   */
  private static Connection getConnection() throws SQLException
  {
    return DriverManager.getConnection( DATABASE );
  }

  /**
   * @return false if a Connection is already open, or true if a new Connection
   *         was successfully created
   * @throws SQLException
   */
  private static boolean open() throws SQLException
  {
    if (!conn.isClosed()) {
      return false;
    }

    conn = getConnection();
    return true;
  }

  /**
   * Makes a PreparedStatement, executes it with 'executeStatement', and handles
   * the returned ResultSet
   */
  private static void execute( SqlQueryData sqc,
                               ExecuteStatement executeStatement )
  {
    try {
      boolean topLevel = open();
      PreparedStatement stmt = conn.prepareStatement( sqc.getSql() );

      int i = 1;
      for (Object o : sqc.getSqlParams()) {
        stmt.setObject( i++, o );
      }

      sqc.handle( executeStatement.execute( stmt ) );
      stmt.close();

      if (topLevel) {
        conn.close();
      }
    } catch (SQLException e) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Executes a SQL Query (ie. a select statment) according to the data provided
   * by a {@link SqlQueryData}. The ResultSet given to the 'handle' function
   * consists of each column specified in the query
   */
  public static void query( final SqlQueryData sqc )
  {
    execute( sqc, stmt -> stmt.executeQuery() );
  }

  /**
   * Executes a DML statment (insert, update, delete) with the data provided by
   * a {@link SqlQueryData}. The ResultSet given to the 'handle' function
   * consists of the statement's generated keys.
   */
  public static void update( final SqlQueryData sqc )
  {
    execute( sqc, stmt -> {
      stmt.executeUpdate();
      return stmt.getGeneratedKeys();
    } );
  }

}
