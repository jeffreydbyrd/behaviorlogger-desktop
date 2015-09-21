package com.threebird.recorder.persistence;

import java.io.File;
import java.sql.ResultSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.models.schemas.Schema;
import com.threebird.recorder.utils.persistence.SqlCallback;
import com.threebird.recorder.utils.persistence.SqlQueryData;
import com.threebird.recorder.utils.persistence.SqliteDao;

/**
 * A set of static functions that interact with the Schemas table
 */
public class Schemas
{
  /**
   * Updates all the values of the given schema in the 'schemas' and
   * 'key_behaviors' table
   * 
   * @throws Exception
   */
  public static void update( Schema schema ) throws Exception
  {
    String sql =
        "UPDATE schemas SET "
            + "client = ?,"
            + "project = ?,"
            + "duration = ?,"
            + "session_directory = ?,"
            + "pause_on_end = ?,"
            + "color_on_end = ?,"
            + "sound_on_end = ? "
            + "WHERE uuid = ?";

    List< Object > params = Lists.newArrayList( schema.client,
                                                schema.project,
                                                schema.duration,
                                                schema.sessionDirectory.getPath(),
                                                schema.pause,
                                                schema.color,
                                                schema.sound,
                                                schema.uuid );

    SqlCallback handle = ( ResultSet rs ) -> {
      Set< KeyBehaviorMapping > oldSet = KeyBehaviors.getAllForSchema( schema.uuid );
      Set< KeyBehaviorMapping > newSet = Sets.newHashSet( schema.mappings.values() );

      SetView< KeyBehaviorMapping > delete = Sets.difference( oldSet, newSet );
      SetView< KeyBehaviorMapping > create = Sets.difference( newSet, oldSet );

      for (KeyBehaviorMapping mapping : delete) {
        KeyBehaviors.delete( schema.uuid, mapping.key );
      }

      for (KeyBehaviorMapping mapping : create) {
        KeyBehaviors.create( schema.uuid, mapping );
      }
    };

    SqliteDao.update( SqlQueryData.create( sql, params, handle ) );
  }

  /**
   * Creates the given schema in the 'schemas' table. Also adds all related
   * behaviors to the key_behaviors table
   * 
   * @throws Exception
   */
  public static void create( Schema schema ) throws Exception
  {
    if (schema.uuid == null) {
      schema.uuid = UUID.randomUUID().toString();
    }

    String sql =
        "INSERT INTO schemas "
            + "(uuid, client, project, duration, session_directory, pause_on_end, color_on_end, sound_on_end) "
            + "VALUES (?,?,?,?,?,?,?,?)";
    List< Object > params = Lists.newArrayList( schema.uuid,
                                                schema.client,
                                                schema.project,
                                                schema.duration,
                                                schema.sessionDirectory.getPath(),
                                                schema.pause,
                                                schema.color,
                                                schema.sound );

    SqliteDao.update( SqlQueryData.create( sql, params, SqlCallback.NOOP ) );
    KeyBehaviors.addAll( schema.uuid, schema.mappings.values() );
  }

  /**
   * Deletes the given Schema and all of its KeyBehaviorMappings
   * 
   * @throws Exception
   */
  public static void delete( Schema schema ) throws Exception
  {
    for (KeyBehaviorMapping kbm : schema.mappings.values()) {
      KeyBehaviors.delete( schema.uuid, kbm.key );
    }

    String sql = "DELETE FROM schemas WHERE uuid = ?";
    List< Object > params = Lists.newArrayList( schema.uuid );
    SqliteDao.update( SqlQueryData.create( sql, params, SqlCallback.NOOP ) );
  }

  /**
   * Retrieves all Schemas
   * 
   * @throws Exception
   */
  public static List< Schema > all() throws Exception
  {
    String sql = "SELECT * FROM schemas";
    List< Schema > result = Lists.newArrayList();

    SqlCallback callback = rs -> {
      while (rs.next()) {
        Schema s = new Schema();
        s.uuid = rs.getString( "uuid" );
        s.client = rs.getString( "client" );
        s.project = rs.getString( "project" );
        s.sessionDirectory = new File( rs.getString( "session_directory" ) );
        s.duration = rs.getInt( "duration" );
        s.color = rs.getBoolean( "color_on_end" );
        s.pause = rs.getBoolean( "pause_on_end" );
        s.sound = rs.getBoolean( "sound_on_end" );

        Iterable< KeyBehaviorMapping > mappings = KeyBehaviors.getAllForSchema( s.uuid );
        s.mappings = Maps.newHashMap( Maps.uniqueIndex( mappings, m -> m.key ) );

        result.add( s );
      }
    };

    SqliteDao.query( SqlQueryData.create( sql, callback ) );

    return result;
  }
}
