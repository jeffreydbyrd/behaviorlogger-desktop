package com.threebird.recorder.persistence;

import java.io.File;
import java.sql.ResultSet;
import java.util.List;
import java.util.Set;

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
   * If schema.id is null, creates a new schema in the database. Otherwise, it
   * updates the row that matches schema.id
   */
  public static void save( Schema schema )
  {
    if (schema.id == null) {
      create( schema );
    } else {
      update( schema );
    }
  }

  /**
   * Updates all the values of the given schema in the 'schemas' and
   * 'key_behaviors' table
   */
  private static void update( Schema schema )
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
            + "WHERE id = ?";

    List< Object > params = Lists.newArrayList( schema.client,
                                                schema.project,
                                                schema.duration,
                                                schema.sessionDirectory.getPath(),
                                                schema.pause,
                                                schema.color,
                                                schema.sound,
                                                schema.id );

    SqlCallback handle = ( ResultSet rs ) -> {
      Set< KeyBehaviorMapping > oldSet = KeyBehaviors.getAllForSchema( schema.id );
      Set< KeyBehaviorMapping > newSet = Sets.newHashSet( schema.mappings.values() );

      SetView< KeyBehaviorMapping > delete = Sets.difference( oldSet, newSet );
      SetView< KeyBehaviorMapping > create = Sets.difference( newSet, oldSet );

      for (KeyBehaviorMapping mapping : delete) {
        KeyBehaviors.delete( schema.id, mapping.key );
      }

      for (KeyBehaviorMapping mapping : create) {
        KeyBehaviors.create( schema.id, mapping );
      }
    };

    SqliteDao.update( SqlQueryData.create( sql, params, handle ) );
  }

  /**
   * Creates the given schema in the 'schemas' table. Also adds all related
   * behaviors to the key_behaviors table
   */
  private static void create( Schema schema )
  {
    String sql =
        "INSERT INTO schemas "
            + "(client, project, duration, session_directory, pause_on_end, color_on_end, sound_on_end) "
            + "VALUES (?,?,?,?,?,?,?)";
    List< Object > params = Lists.newArrayList( schema.client,
                                                schema.project,
                                                schema.duration,
                                                schema.sessionDirectory.getPath(),
                                                schema.pause,
                                                schema.color,
                                                schema.sound );

    SqlCallback callback = rs -> {
      schema.id = rs.getInt( 1 );
      KeyBehaviors.addAll( schema.id, schema.mappings.values() );
    };

    SqliteDao.update( SqlQueryData.create( sql, params, callback ) );
  }

  /**
   * Deletes the given Schema and all of its KeyBehaviorMappings
   */
  public static void delete( Schema schema )
  {
    for (KeyBehaviorMapping kbm : schema.mappings.values()) {
      KeyBehaviors.delete( schema.id, kbm.key );
    }

    String sql = "DELETE FROM schemas WHERE id = ?";
    List< Object > params = Lists.newArrayList( schema.id );
    SqliteDao.update( SqlQueryData.create( sql, params, SqlCallback.NOOP ) );
  }

  /**
   * Retrieves all Schemas
   */
  public static List< Schema > all()
  {
    String sql = "SELECT * FROM schemas";
    List< Schema > result = Lists.newArrayList();

    SqlCallback callback = rs -> {
      while (rs.next()) {
        Schema s = new Schema();
        s.id = rs.getInt( "id" );
        s.client = rs.getString( "client" );
        s.project = rs.getString( "project" );
        s.sessionDirectory = new File( rs.getString( "session_directory" ) );
        s.duration = rs.getInt( "duration" );
        s.color = rs.getBoolean( "color_on_end" );
        s.pause = rs.getBoolean( "pause_on_end" );
        s.sound = rs.getBoolean( "sound_on_end" );

        Iterable< KeyBehaviorMapping > mappings = KeyBehaviors.getAllForSchema( s.id );
        s.mappings = Maps.newHashMap( Maps.uniqueIndex( mappings, m -> m.key ) );

        result.add( s );
      }
    };

    SqliteDao.query( SqlQueryData.create( sql, Lists.newArrayList(), callback ) );

    return result;
  }
}
