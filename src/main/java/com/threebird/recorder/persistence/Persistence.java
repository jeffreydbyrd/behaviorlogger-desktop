package com.threebird.recorder.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Some common SQLite connection stuff
 */
public class Persistence
{
  private static String DATABASE = "jdbc:sqlite:recorder.db";

  // Load the Sqlite JDBC driver
  static {
    try {
      DriverManager.registerDriver( new org.sqlite.JDBC() );
    } catch (SQLException e) {
      throw new RuntimeException( e );
    }
  }

  /**
   * Returns a Connection to the local "recorder" database
   * 
   * @throws SQLException
   * @throws ClassNotFoundException
   */
  public static Connection getConnection() throws SQLException
  {
    return DriverManager.getConnection( DATABASE );
  }
}
