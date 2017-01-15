package com.threebird.recorder.persistence;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.threebird.recorder.models.preferences.PreferencesManager;
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
    // Validate UUIDs
    UUID.fromString( schema.uuid );
    UUID.fromString( schema.versionUuid );

    // Validate version number
    List< SchemaVersion > versionset = getVersionSet( schema.uuid );
    int expected;
    if (versionset.isEmpty()) {
      expected = 1;
    } else {
      expected = versionset.get( versionset.size() - 1 ).versionNumber + 1;
    }
    if (schema.versionNumber != expected) {
      String msg = String.format( "Failed to save schema %s: versionNumber=%d but expected %d",
                                  schema.uuid,
                                  schema.versionNumber,
                                  expected );
      throw new IllegalArgumentException( msg );
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
    for (KeyBehaviorMapping mapping : schema.behaviors) {
      KeyBehaviors.save( schema, mapping );
    }
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

        Iterable< KeyBehaviorMapping > mappings = KeyBehaviors.getAllForSchema( s.versionUuid );
        s.behaviors = Lists.newArrayList( mappings );

        result.add( s );
      }
    };

    SqliteDao.query( sql, Lists.newArrayList(), callback );

    return result;
  }

  public static List< SchemaVersion > getVersionSet( String schemaId ) throws Exception
  {
    String sql = "SELECT * FROM schema_versions_v1_1 WHERE uuid = ? ORDER BY version_number ASC";
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

        Iterable< KeyBehaviorMapping > mappings = KeyBehaviors.getAllForSchema( s.versionUuid );
        s.behaviors = Lists.newArrayList( mappings );

        result.add( s );
      }
    };

    SqliteDao.query( sql, Lists.newArrayList( schemaId ), callback );

    return result;
  }

  public static void saveVersionset( List< SchemaVersion > versionset ) throws Exception
  {
    if (versionset.size() == 0) {
      return;
    }

    String schemaId = versionset.get( 0 ).uuid;

    // Validate that these are all the same schema
    for (SchemaVersion sv : versionset) {
      if (!sv.uuid.equals( schemaId )) {
        String msg =
            String.format( "Received Schema version where UUID=%s, but first version had UUID=%s", sv.uuid, schemaId );
        throw new IllegalArgumentException( msg );
      }
    }

    // Validate schemaId (throws IllegalArgumentException if invalid)
    UUID.fromString( schemaId );

    // Get the current version-set IDs
    List< SchemaVersion > currentVersionset = getVersionSet( schemaId );

    List< Object > currentIds =
        currentVersionset.stream().map( sv -> sv.versionUuid ).collect( Collectors.toList() );

    // Delete old behavior_versions
    String placeholders = currentIds.stream().map( s -> "?" ).collect( Collectors.joining( "," ) );
    String deleteSql = "DELETE FROM behavior_versions_v1_1 WHERE schema_version_uuid IN (" + placeholders + ")";
    SqliteDao.update( deleteSql, currentIds, SqlCallback.NOOP );

    // Delete old behaviors
    deleteSql = "DELETE FROM behaviors_v1_1 WHERE schema_uuid=?";
    SqliteDao.update( deleteSql, Lists.newArrayList( schemaId ), SqlCallback.NOOP );

    // Delete old SchemaVersions
    deleteSql = "DELETE FROM schema_versions_v1_1 WHERE uuid=?";
    SqliteDao.update( deleteSql, Lists.newArrayList( schemaId ), SqlCallback.NOOP );

    // Incrementally add new version-sets
    Collections.sort( versionset, ( sv1, sv2 ) -> sv1.versionNumber - sv2.versionNumber );
    for (SchemaVersion sv : versionset) {
      save( sv );
    }

    // Check if there's a session-directory set for this schema
    Optional< File > forSchemaId = SessionDirectories.getForSchemaId( schemaId );
    if (!forSchemaId.isPresent()) {
      SessionDirectories.create( schemaId, new File( PreferencesManager.getSessionDirectory() ) );
    }
  }
}
