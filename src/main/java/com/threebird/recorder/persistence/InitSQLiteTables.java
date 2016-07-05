package com.threebird.recorder.persistence;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.Lists;
import com.threebird.recorder.utils.persistence.SqliteDao;
import com.threebird.recorder.utils.resources.ResourceUtils;

/**
 * A class that provides {@link InitSQLiteTables#init()}, which will migrate the user's local SQLite database to the
 * most up-to-date schema. Each "evolution" is called in {@link InitSQLiteTables#run()}. If you add any more, they must
 * written in an idempotent manner. This means that you must not modify the evolutions after they have been deployed.
 */
public class InitSQLiteTables
{
  public static void init() throws Exception
  {
    File dbFile = ResourceUtils.getDb();
    dbFile.createNewFile();

    evo0_create_tables();
    evo1_0_add_uuid();
    evo1_1_add_versioning();
  }

  /**
   * Defines the first generation of tables
   */
  private static void evo0_create_tables() throws Exception
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

    SqliteDao.update( createSchemas );
    SqliteDao.update( createBehaviors );
  }

  /**
   * Updates the schemas table to have uuid instead of an integer id.
   * 
   * @throws Exception
   */
  private static void evo1_0_add_uuid() throws Exception
  {
    // figure out if this evolution has been executed yet
    String pragmaSchemas = "SELECT name FROM sqlite_master WHERE type='table' AND name='schemas_v1_0'";
    AtomicBoolean tableExists = new AtomicBoolean( false );

    SqliteDao.query( pragmaSchemas, Lists.newArrayList(), rs -> {
      while (rs.next()) {
        tableExists.set( true );
        return;
      }
    } );

    // if the PRAGMA statment returned anything, then don't bother with the rest
    if (tableExists.get()) {
      return;
    }

    // Create the new tables
    String createNewSchemas =
        "CREATE TABLE schemas_v1_0 ("
            + "uuid TEXT PRIMARY KEY,"
            + "client TEXT NOT NULL,"
            + "project TEXT NOT NULL,"
            + "session_directory TEXT NOT NULL,"
            + "duration INTEGER NOT NULL,"
            + "pause_on_end INTEGER NOT NULL,"
            + "color_on_end INTEGER NOT NULL,"
            + "sound_on_end INTEGER NOT NULL );";

    String createNewBehaviors =
        "CREATE TABLE key_behaviors_v1_0 ("
            + "schema_uuid TEXT NOT NULL,"
            + "key CHAR(1) NOT NULL,"
            + "name TEXT NOT NULL,"
            + "is_continuous INTEGER NOT NULL,"
            + "FOREIGN KEY (schema_uuid) REFERENCES schemas(uuid),"
            + "UNIQUE(schema_uuid, key) );";

    SqliteDao.update( createNewSchemas );
    SqliteDao.update( createNewBehaviors );

    String insertSchemaFmt = "INSERT INTO schemas_v1_0 VALUES ('%s','%s','%s','%s',%d,%d,%d,%d);";
    String getBehaviorsFmt = "SELECT * FROM key_behaviors WHERE schema_id = %d;";
    String insertBehaviorFmt = "INSERT INTO key_behaviors_v1_0 VALUES ('%s','%s','%s',%d);";

    SqliteDao.query( "SELECT * FROM schemas", Lists.newArrayList(), rs -> {
      while (rs.next()) {
        String uuid = UUID.randomUUID().toString();
        String insertSchema = String.format( insertSchemaFmt,
                                             uuid,
                                             rs.getString( "client" ),
                                             rs.getString( "project" ),
                                             rs.getString( "session_directory" ),
                                             rs.getInt( "duration" ),
                                             rs.getInt( "pause_on_end" ),
                                             rs.getInt( "color_on_end" ),
                                             rs.getInt( "sound_on_end" ) );
        SqliteDao.update( insertSchema );

        int originalId = rs.getInt( 1 );
        String getBehaviors = String.format( getBehaviorsFmt, originalId );

        SqliteDao.query( getBehaviors, Lists.newArrayList(), rs2 -> {
          while (rs2.next()) {
            String name = rs2.getString( "behavior" );
            String insertBehavior =
                String.format( insertBehaviorFmt,
                               uuid,
                               rs2.getString( "key" ),
                               name,
                               rs2.getInt( "is_continuous" ) );
            SqliteDao.update( insertBehavior );
          }
        } );
      }
    } );

    // Keep the old tables in case the user goes back to old version
  }

  private static void evo1_1_add_versioning() throws Exception
  {
    // figure out if this evolution has been executed yet
    String pragmaSchemas = "SELECT name FROM sqlite_master WHERE type='table' AND name='schemas_v1_1'";
    AtomicBoolean tableExists = new AtomicBoolean( false );

    SqliteDao.query( pragmaSchemas, Lists.newArrayList(), rs -> {
      while (rs.next()) {
        tableExists.set( true );
        return;
      }
    } );

    // if the PRAGMA statment returned anything, then don't bother with the rest
    if (tableExists.get()) {
      return;
    }

    // Create the new tables
    String createNewSchemas =
        "CREATE TABLE schemas_v1_1 ("
            + "uuid TEXT PRIMARY KEY,"
            + "version INTEGER NOT NULL," // new field
            + "client TEXT NOT NULL,"
            + "project TEXT NOT NULL,"
            + "session_directory TEXT NOT NULL,"
            + "duration INTEGER NOT NULL,"
            + "pause_on_end INTEGER NOT NULL,"
            + "color_on_end INTEGER NOT NULL,"
            + "sound_on_end INTEGER NOT NULL);";

    String createNewBehaviors =
        "CREATE TABLE behaviors_v1_1 ("
            + "uuid TEXT NOT NULL," // new field
            + "schema_uuid TEXT NOT NULL,"
            + "key CHAR(1) NOT NULL,"
            + "description TEXT NOT NULL," // changed names
            + "is_continuous INTEGER NOT NULL,"
            + "FOREIGN KEY (schema_uuid) REFERENCES schemas(uuid),"
            + "UNIQUE(schema_uuid, key) );";

    SqliteDao.update( createNewSchemas );
    SqliteDao.update( createNewBehaviors );

    String insertSchemaFmt = "INSERT INTO schemas_v1_1 VALUES ('%s',%d,'%s','%s','%s',%d,%d,%d,%d);";
    String getBehaviorsFmt = "SELECT * FROM key_behaviors_v1_0 WHERE schema_uuid = '%s';";
    String insertBehaviorFmt = "INSERT INTO behaviors_v1_1 VALUES ('%s','%s','%s','%s',%d);";

    SqliteDao.query( "SELECT * FROM schemas_v1_0", Lists.newArrayList(), rs -> {
      while (rs.next()) {
        String schemaUuid = rs.getString( "uuid" );
        String insertSchema = String.format( insertSchemaFmt,
                                             schemaUuid,
                                             1,
                                             rs.getString( "client" ),
                                             rs.getString( "project" ),
                                             rs.getString( "session_directory" ),
                                             rs.getInt( "duration" ),
                                             rs.getInt( "pause_on_end" ),
                                             rs.getInt( "color_on_end" ),
                                             rs.getInt( "sound_on_end" ) );
        SqliteDao.update( insertSchema );

        String getBehaviors = String.format( getBehaviorsFmt, schemaUuid );

        SqliteDao.query( getBehaviors, Lists.newArrayList(), rs2 -> {
          while (rs2.next()) {
            String behaviorUuid = UUID.randomUUID().toString();
            String insertBehavior =
                String.format( insertBehaviorFmt,
                               behaviorUuid,
                               schemaUuid,
                               rs2.getString( "key" ),
                               rs2.getString( "name" ),
                               rs2.getInt( "is_continuous" ) );
            SqliteDao.update( insertBehavior );
          }
        } );
      }
    } );

    // Keep the old tables in case the user goes back to old version

  }
}
