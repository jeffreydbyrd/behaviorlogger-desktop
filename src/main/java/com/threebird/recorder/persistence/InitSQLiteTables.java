package com.threebird.recorder.persistence;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.threebird.recorder.utils.persistence.SqlQueryData;
import com.threebird.recorder.utils.persistence.SqliteDao;
import com.threebird.recorder.utils.resources.ResourceUtils;

/**
 * A class that provides {@link InitSQLiteTables#init()}, which will migrate the
 * user's local SQLite database to the most up-to-date schema. Each "evolution"
 * is called in {@link InitSQLiteTables#run()}. If you add any more, they must
 * written in an idempotent manner. This means that you must not modify the
 * evolutions after they have been deployed.
 */
public class InitSQLiteTables
{
  public static void init() throws Exception
  {
    File dbFile = ResourceUtils.getDb();
    dbFile.createNewFile();

    evo0_create_tables();
    evo1();
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

    SqliteDao.update( SqlQueryData.create( createSchemas, rs -> {} ) );
    SqliteDao.update( SqlQueryData.create( createBehaviors, rs -> {} ) );
  }

  /**
   * Restructures the schemas and key_behaviors tables so that schemas have
   * UUIDs and the behavior names are unique. Beware, SQLite makes it very hard
   * to restructure tables without losing data. The things I did, I did them for
   * a greater good.
   * 
   * @throws Exception
   */
  private static void evo1() throws Exception
  {
    // used to figure out if this evolution has been executed yet
    String pragmaSchemas = "PRAGMA table_info(schemas)";
    AtomicBoolean hasUuidCol = new AtomicBoolean( false );

    SqliteDao.query( SqlQueryData.create( pragmaSchemas, rs -> {
      while (rs.next()) {
        String colName = rs.getString( 2 );
        if (colName.equals( "uuid" )) {
          hasUuidCol.set( true );
          return;
        }
      }
    } ) );

    // if we have the Uuid column, then the user has already executed this
    if (hasUuidCol.get()) {
      return;
    }

    // Create the new tables
    String createNewSchemas =
        "CREATE TABLE new_schemas ("
            + "uuid TEXT PRIMARY KEY,"
            + "client TEXT NOT NULL,"
            + "project TEXT NOT NULL,"
            + "session_directory TEXT NOT NULL,"
            + "duration INTEGER NOT NULL,"
            + "pause_on_end INTEGER NOT NULL,"
            + "color_on_end INTEGER NOT NULL,"
            + "sound_on_end INTEGER NOT NULL );";

    String createNewBehaviors =
        "CREATE TABLE new_key_behaviors ("
            + "schema_uuid TEXT NOT NULL,"
            + "key CHAR(1) NOT NULL,"
            + "name TEXT NOT NULL,"
            + "is_continuous INTEGER NOT NULL,"
            + "FOREIGN KEY (schema_uuid) REFERENCES schemas(uuid),"
            + "UNIQUE(schema_uuid, key),"
            + "UNIQUE(schema_uuid, name) );";

    SqliteDao.update( SqlQueryData.create( createNewSchemas ) );
    SqliteDao.update( SqlQueryData.create( createNewBehaviors ) );

    String insertSchemaFmt = "INSERT INTO new_schemas VALUES ('%s','%s','%s','%s',%d,%d,%d,%d);";
    String getBehaviorsFmt = "SELECT * FROM key_behaviors WHERE schema_id = %d;";
    String insertBehaviorFmt = "INSERT INTO new_key_behaviors VALUES ('%s','%s','%s',%d);";

    SqliteDao.query( SqlQueryData.create( "SELECT * FROM schemas", rs -> {
      while (rs.next()) {
        String uuid = UUID.randomUUID().toString();
        String insertSchema = String.format( insertSchemaFmt, uuid, rs.getString( "client" ),
                                             rs.getString( "project" ), rs.getString( "session_directory" ),
                                             rs.getInt( "duration" ), rs.getInt( "pause_on_end" ),
                                             rs.getInt( "color_on_end" ), rs.getInt( "sound_on_end" ) );
        SqliteDao.update( SqlQueryData.create( insertSchema ) );

        int originalId = rs.getInt( 1 );
        String getBehaviors = String.format( getBehaviorsFmt, originalId );

        SqliteDao.query( SqlQueryData.create( getBehaviors, rs2 -> {
          Set< String > seenNames = new HashSet< String >();
          while (rs2.next()) {
            String name = rs2.getString( "behavior" );
            boolean isUniq = seenNames.add( name );

            if (isUniq) {
              String insertBehavior =
                  String.format( insertBehaviorFmt, uuid, rs2.getString( "key" ),
                                 name, rs2.getInt( "is_continuous" ) );
              SqliteDao.update( SqlQueryData.create( insertBehavior ) );
            }
          }
        } ) );
      }
    } ) );

    // remove the old tables
    SqliteDao.update( "DROP TABLE schemas" );
    SqliteDao.update( "DROP TABLE key_behaviors" );

    // rename the new ones
    SqliteDao.update( "ALTER TABLE new_schemas RENAME TO schemas" );
    SqliteDao.update( "ALTER TABLE new_key_behaviors RENAME TO key_behaviors" );
  }
}
