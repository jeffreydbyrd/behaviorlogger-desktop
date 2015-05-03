package com.threebird.recorder.utils.persistence;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.google.common.collect.Lists;
import com.threebird.recorder.utils.resources.ResourceUtils;

/**
 * A small library that handles SQLite Connections and PreparedStatements for
 * you. Use the {@link SqliteDao#query(SqlQueryData)} or
 * {@link SqliteDao#update(SqlQueryData)} methods to interact with the DB.
 */
public class SqliteDao
{
  private static String DATABASE =
      String.format( "jdbc:sqlite:%s", ResourceUtils.getDb().getAbsolutePath() );

  private static Connection conn;

  private interface ExecuteStatement
  {
    ResultSet execute( PreparedStatement stmt ) throws SQLException;
  }

  private static void initTables() throws Exception
  {
    String createSchemas =
        "CREATE TABLE IF NOT EXISTS schemas (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "client TEXT NOT NULL," +
            "project TEXT NOT NULL," +
            "session_directory TEXT NOT NULL," +
            "duration INTEGER NOT NULL," +
            "pause_on_end INTEGER NOT NULL," +
            "color_on_end INTEGER NOT NULL," +
            "sound_on_end INTEGER NOT NULL );";

    String createBehaviors =
        "CREATE TABLE IF NOT EXISTS key_behaviors (" +
            "schema_id INTEGER NOT NULL," +
            "key CHAR(1) NOT NULL," +
            "behavior TEXT NOT NULL," +
            "is_continuous INTEGER NOT NULL," +

            "FOREIGN KEY (schema_id) REFERENCES schemas(id)," +
            "PRIMARY KEY(schema_id, key) );";

    update( SqlQueryData.create( createSchemas, Lists.newArrayList(), rs -> {} ) );
    update( SqlQueryData.create( createBehaviors, Lists.newArrayList(), rs -> {} ) );
  }

  /**
   * @return false if a Connection is already open, or true if a new Connection
   *         was successfully created
   * 
   * @throws Exception
   */
  private static boolean open() throws Exception
  {
    if (conn == null) {
      File dbFile = ResourceUtils.getDb();
      boolean created = dbFile.createNewFile();

      DriverManager.registerDriver( new org.sqlite.JDBC() );
      conn = DriverManager.getConnection( DATABASE );

      if (created) {
        initTables();
      }
    }

    if (!conn.isClosed()) {
      return false;
    }

    conn = DriverManager.getConnection( DATABASE );
    return true;
  }

  /**
   * Makes a PreparedStatement, executes it with 'executeStatement', and handles
   * the returned ResultSet
   * 
   * @throws Exception
   */
  private static void execute( SqlQueryData sqd,
                               ExecuteStatement executeStatement ) throws Exception
  {
    boolean topLevel = open();
    PreparedStatement stmt = conn.prepareStatement( sqd.getSql() );

    int i = 1;
    for (Object o : sqd.getSqlParams()) {
      stmt.setObject( i++, o );
    }

    sqd.handle( executeStatement.execute( stmt ) );
    stmt.close();

    if (topLevel) {
      conn.close();
    }
  }

  /**
   * Executes a SQL Query (ie. a select statment) according to the data provided
   * by a {@link SqlQueryData}. The ResultSet given to the 'handle' function
   * consists of each column specified in the query
   * 
   * @throws Exception
   */
  public static void query( final SqlQueryData sqc ) throws Exception
  {
    execute( sqc, stmt -> stmt.executeQuery() );
  }

  /**
   * Executes a DML statment (insert, update, delete) with the data provided by
   * a {@link SqlQueryData}. The ResultSet given to the 'handle' function
   * consists of the statement's generated keys.
   * 
   * @throws Exception
   */
  public static void update( final SqlQueryData sqc ) throws Exception
  {
    execute( sqc, stmt -> {
      stmt.executeUpdate();
      return stmt.getGeneratedKeys();
    } );
  }

}
