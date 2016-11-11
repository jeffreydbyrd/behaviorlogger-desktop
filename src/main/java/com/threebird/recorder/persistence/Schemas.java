package com.threebird.recorder.persistence;

import java.sql.ResultSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Preconditions;
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

  public static boolean hasChanged( Schema schema ) throws Exception
  {
    Optional< Schema > current = getForUuid( schema.uuid );

    Preconditions.checkState( current.isPresent() );

    return Schema.isDifferent( schema, current.get() );
  }

  /**
   * Updates all the values of the given schema in the 'schemas' and 'key_behaviors' table. This method assumes that the
   * schema 'version' is already set to the correct value.
   * 
   * @throws Exception
   */
  public static void update( Schema schema ) throws Exception
  {
    String sql =
        "INSERT INTO " + TBL_NAME
            + " (uuid, version, client, project, duration, pause_on_end, color_on_end, sound_on_end, archived) "
            + " VALUES (?,?,?,?,?,?,?,?,?)";

    List< Object > params = Lists.newArrayList( schema.uuid,
                                                schema.version,
                                                schema.client,
                                                schema.project,
                                                schema.duration,
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
    if (schema.version == null) {
      schema.version = 1;
    }
    if (schema.uuid == null) {
      schema.uuid = UUID.randomUUID().toString();
    }

    String sql =
        "INSERT INTO " + TBL_NAME
            + " (uuid, version, client, project, duration, pause_on_end, color_on_end, sound_on_end, archived) "
            + " VALUES (?,?,?,?,?,?,?,?,?)";
    List< Object > params = Lists.newArrayList( schema.uuid,
                                                schema.version,
                                                schema.client,
                                                schema.project,
                                                schema.duration,
                                                schema.pause,
                                                schema.color,
                                                schema.sound,
                                                schema.archived );

    SqliteDao.update( sql, params, SqlCallback.NOOP );
    KeyBehaviors.addAll( schema, schema.mappings.values() );
  }

  /**
   * Returns the an Optional containing the schema associated with this uuid, or empty if there is none. This method
   * will return an archived schema also.
   * 
   * @throws Exception
   */
  public static Optional< Schema > getForUuid( String uuid ) throws Exception
  {
    String sql = "SELECT * "
        + " FROM " + TBL_NAME
        + " WHERE"
        + "  version = (SELECT MAX(version) FROM " + TBL_NAME + " WHERE uuid = ?)"
        + "  AND uuid = ?";
    List< Object > params = Lists.newArrayList( uuid, uuid );

    Schema s = new Schema();

    SqlCallback handle = ( ResultSet rs ) -> {
      int counter = 0;
      while (rs.next()) {
        Preconditions.checkState( counter < 2 ); // this should never go higher than 1 iteration
        s.uuid = rs.getString( "uuid" );
        s.version = rs.getInt( "version" );
        s.client = rs.getString( "client" );
        s.project = rs.getString( "project" );
        s.duration = rs.getInt( "duration" );
        s.color = rs.getBoolean( "color_on_end" );
        s.pause = rs.getBoolean( "pause_on_end" );
        s.sound = rs.getBoolean( "sound_on_end" );
        s.archived = rs.getBoolean( "archived" );

        Iterable< KeyBehaviorMapping > mappings = KeyBehaviors.getAllForSchema( s.uuid );
        s.mappings = Maps.newHashMap( Maps.uniqueIndex( mappings, m -> m.key ) );

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
   * Retrieves all active Schemas
   * 
   * @throws Exception
   */
  public static List< Schema > all() throws Exception
  {
    String sql = "SELECT * "
        + "FROM " + TBL_NAME + " AS outer "
        + "WHERE"
        + "  version = (SELECT MAX(version) FROM " + TBL_NAME + " WHERE uuid = outer.uuid)";
    List< Schema > result = Lists.newArrayList();

    SqlCallback callback = rs -> {
      while (rs.next()) {
        Schema s = new Schema();
        s.uuid = rs.getString( "uuid" );
        s.version = rs.getInt( "version" );
        s.client = rs.getString( "client" );
        s.project = rs.getString( "project" );
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
