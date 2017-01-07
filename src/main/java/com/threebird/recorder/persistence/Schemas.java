package com.threebird.recorder.persistence;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.models.schemas.SchemaVersion;
import com.threebird.recorder.utils.persistence.SqlCallback;
import com.threebird.recorder.utils.persistence.SqliteDao;

/**
 * A set of static functions that interact with the Schemas table
 */
public class Schemas
{
  private static final String TBL_NAME = "schema_versions_v1_1";

  /**
   * Saves the given schema in the 'schema_versions' table. Also adds all related behaviors to the key_behaviors table
   * 
   * @throws Exception
   */
  public static void save( SchemaVersion schema ) throws Exception
  {
    schema.versionUuid = UUID.randomUUID().toString();
    
    if (schema.versionNumber == null) {
      schema.versionNumber = 1;
    } else {
      schema.versionNumber = schema.versionNumber + 1;
    }
    
    if (schema.uuid == null) {
      schema.uuid = UUID.randomUUID().toString();
    }

    String sql =
        "INSERT INTO " + TBL_NAME
            + " (uuid, version_uuid, version_number, client, project, duration, pause_on_end, color_on_end, sound_on_end, archived) "
            + " VALUES (?,?,?,?,?,?,?,?,?,?)";
    List< Object > params = Lists.newArrayList( schema.uuid,
                                                schema.versionUuid,
                                                schema.versionNumber,
                                                schema.client,
                                                schema.project,
                                                schema.duration,
                                                schema.pause,
                                                schema.color,
                                                schema.sound,
                                                schema.archived );

    SqliteDao.update( sql, params, SqlCallback.NOOP );
    for (KeyBehaviorMapping mapping : schema.behaviors.values()) {
      KeyBehaviors.save( schema, mapping );
    }
  }

  /**
   * Returns an Optional containing the schema associated with this uuid, or empty if there is none. This method
   * will return an archived schema also.
   * 
   * @throws Exception
   */
  public static Optional< SchemaVersion > getLatestForUuid( String uuid ) throws Exception
  {
    String sql = "SELECT * "
        + " FROM " + TBL_NAME
        + " WHERE"
        + "  version_number = (SELECT MAX(version_number) FROM " + TBL_NAME + " WHERE uuid = ?)"
        + "  AND uuid = ?";
    List< Object > params = Lists.newArrayList( uuid, uuid );

    SchemaVersion s = new SchemaVersion();

    SqlCallback handle = ( ResultSet rs ) -> {
      int counter = 0;
      while (rs.next()) {
        Preconditions.checkState( counter < 2 ); // this should never go higher than 1 iteration
        s.uuid = rs.getString( "uuid" );
        s.versionUuid = rs.getString( "version_uuid" );
        s.versionNumber = rs.getInt( "version_number" );
        s.client = rs.getString( "client" );
        s.project = rs.getString( "project" );
        s.duration = rs.getInt( "duration" );
        s.color = rs.getBoolean( "color_on_end" );
        s.pause = rs.getBoolean( "pause_on_end" );
        s.sound = rs.getBoolean( "sound_on_end" );
        s.archived = rs.getBoolean( "archived" );

        Iterable< KeyBehaviorMapping > mappings = KeyBehaviors.getAllForSchema( s.uuid );
        s.behaviors = Maps.newHashMap( Maps.uniqueIndex( mappings, m -> m.key ) );

        counter++;
      }
    };

    SqliteDao.query( sql, params, handle );

    if (s.uuid == null) {
      return Optional.empty();
    }

    return Optional.of( s );
  }

  public static boolean isArchived( String uuid ) throws Exception
  {
    String sql = "SELECT 1 "
        + "FROM " + TBL_NAME
        + "WHERE"
        + "  version = (SELECT MAX(version) FROM " + TBL_NAME + " WHERE uuid = ?)"
        + "  AND archived = 1";
    List< Object > params = Lists.newArrayList( uuid );
    AtomicBoolean isArchived = new AtomicBoolean( false );

    SqlCallback handle = ( ResultSet rs ) -> {
      isArchived.set( true );
    };

    SqliteDao.query( sql, params, handle );

    return isArchived.get();
  }

  /**
   * Retrieves all the latest Schemas
   * 
   * @throws Exception
   */
  public static List< SchemaVersion > allLatest() throws Exception
  {
    String sql = "SELECT * "
        + "FROM " + TBL_NAME + " AS outer "
        + "WHERE"
        + "  version_number = (SELECT MAX(version_number) FROM " + TBL_NAME + " WHERE uuid = outer.uuid)";
    List< SchemaVersion > result = Lists.newArrayList();

    SqlCallback callback = rs -> {
      while (rs.next()) {
        SchemaVersion s = new SchemaVersion();
        s.uuid = rs.getString( "uuid" );
        s.versionUuid = rs.getString( "version_uuid" );
        s.versionNumber = rs.getInt( "version_number" );
        s.client = rs.getString( "client" );
        s.project = rs.getString( "project" );
        s.duration = rs.getInt( "duration" );
        s.color = rs.getBoolean( "color_on_end" );
        s.pause = rs.getBoolean( "pause_on_end" );
        s.sound = rs.getBoolean( "sound_on_end" );
        s.archived = rs.getBoolean( "archived" );

        Iterable< KeyBehaviorMapping > mappings = KeyBehaviors.getAllForSchema( s.uuid );
        s.behaviors = Maps.newHashMap( Maps.uniqueIndex( mappings, m -> m.key ) );

        result.add( s );
      }
    };

    SqliteDao.query( sql, Lists.newArrayList(), callback );

    return result;
  }
}
