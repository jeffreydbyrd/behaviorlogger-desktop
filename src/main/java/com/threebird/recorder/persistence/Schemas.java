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
import com.threebird.recorder.utils.persistence.SqliteDao;

/**
 * A set of static functions that interact with the Schemas table
 */
public class Schemas
{
  private static final String TBL_NAME = "schemas_v1_1";

  /**
   * Updates all the values of the given schema in the 'schemas' and 'key_behaviors' table
   * 
   * @throws Exception
   */
  public static void update( Schema schema ) throws Exception
  {
    String sql =
        "INSERT INTO " + TBL_NAME
            + " (uuid, version, client, project, duration, session_directory, pause_on_end, color_on_end, sound_on_end, archived) "
            + " VALUES (?,?,?,?,?,?,?,?,?,?)";

    schema.version = schema.version + 1;
    List< Object > params = Lists.newArrayList( schema.uuid,
                                                schema.version,
                                                schema.client,
                                                schema.project,
                                                schema.duration,
                                                schema.sessionDirectory.getPath(),
                                                schema.pause,
                                                schema.color,
                                                schema.sound,
                                                schema.archived );

    Set< KeyBehaviorMapping > oldSet = KeyBehaviors.getAllForSchema( schema.uuid );
    Set< KeyBehaviorMapping > newSet = Sets.newHashSet( schema.mappings.values() );
    SetView< KeyBehaviorMapping > create = Sets.difference( newSet, oldSet );
    List< KeyBehaviorMapping > update = Lists.newArrayList();
    for (KeyBehaviorMapping kbm : newSet) {
      if (oldSet.contains( kbm )) {
        update.add( kbm );
      }
    }

    SqlCallback handle = ( ResultSet rs ) -> {
      for (KeyBehaviorMapping mapping : create) {
        KeyBehaviors.create( schema, mapping );
      }

      for (KeyBehaviorMapping mapping : update) {
        KeyBehaviors.update( schema.uuid, mapping );
        KeyBehaviors.bridge( schema.uuid, schema.version, mapping.uuid );
      }
    };

    SqliteDao.update( sql, params, handle );
  }

  /**
   * Creates the given schema in the 'schemas' table. Also adds all related behaviors to the key_behaviors table
   * 
   * @throws Exception
   */
  public static void create( Schema schema ) throws Exception
  {
    schema.version = 1;
    if (schema.uuid == null) {
      schema.uuid = UUID.randomUUID().toString();
    }

    String sql =
        "INSERT INTO " + TBL_NAME
            + " (uuid, version, client, project, duration, session_directory, pause_on_end, color_on_end, sound_on_end, archived) "
            + " VALUES (?,?,?,?,?,?,?,?,?,?)";
    List< Object > params = Lists.newArrayList( schema.uuid,
                                                schema.version,
                                                schema.client,
                                                schema.project,
                                                schema.duration,
                                                schema.sessionDirectory.getPath(),
                                                schema.pause,
                                                schema.color,
                                                schema.sound,
                                                schema.archived );

    SqliteDao.update( sql, params, SqlCallback.NOOP );
    KeyBehaviors.addAll( schema, schema.mappings.values() );
  }

  public static void archive( Schema schema ) throws Exception
  {
    schema.archived = true;
    update( schema );
  }

  /**
   * Retrieves all Schemas
   * 
   * @throws Exception
   */
  public static List< Schema > all() throws Exception
  {
    String sql = "SELECT * "
        + "FROM " + TBL_NAME + " AS outer "
        + "WHERE"
        + "  version = (SELECT MAX(version) FROM " + TBL_NAME + " WHERE uuid = outer.uuid)"
        + "  AND archived = 0";
    List< Schema > result = Lists.newArrayList();

    SqlCallback callback = rs -> {
      while (rs.next()) {
        Schema s = new Schema();
        s.uuid = rs.getString( "uuid" );
        s.version = rs.getInt( "version" );
        s.client = rs.getString( "client" );
        s.project = rs.getString( "project" );
        s.sessionDirectory = new File( rs.getString( "session_directory" ) );
        s.duration = rs.getInt( "duration" );
        s.color = rs.getBoolean( "color_on_end" );
        s.pause = rs.getBoolean( "pause_on_end" );
        s.sound = rs.getBoolean( "sound_on_end" );
        s.archived = rs.getBoolean( "archived" );

        Iterable< KeyBehaviorMapping > mappings = KeyBehaviors.getAllForSchema( s.uuid );
        s.mappings = Maps.newHashMap( Maps.uniqueIndex( mappings, m -> m.key ) );

        result.add( s );
      }
    };

    SqliteDao.query( sql, Lists.newArrayList(), callback );

    return result;
  }
}
