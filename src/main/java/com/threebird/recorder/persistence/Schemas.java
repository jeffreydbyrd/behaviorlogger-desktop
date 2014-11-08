package com.threebird.recorder.persistence;

import java.sql.ResultSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.Schema;
import com.threebird.recorder.persistence.util.SqlCallback;
import com.threebird.recorder.persistence.util.SqlQueryData;
import com.threebird.recorder.persistence.util.SqliteDao;

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
    String sql = "UPDATE schemas SET name = ?, duration = ? WHERE id = ?";

    List< Object > params =
        Lists.newArrayList( schema.name, schema.duration, schema.id );

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
    String sql = "INSERT INTO schemas (name, duration) VALUES (?,?)";
    List< Object > params = Lists.newArrayList( schema.name, schema.duration );

    SqlCallback callback = rs -> {
      schema.id = rs.getInt( 1 );
      KeyBehaviors.addAll( schema.id, schema.mappings.values() );
    };

    SqliteDao.update( SqlQueryData.create( sql, params, callback ) );
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
        s.name = rs.getString( "name" );
        s.duration = rs.getInt( "duration" );

        Iterable< KeyBehaviorMapping > mappings = KeyBehaviors.getAllForSchema( s.id );
        s.mappings = Maps.newHashMap( Maps.uniqueIndex( mappings, m -> m.key ) );

        result.add( s );
      }
    };

    SqliteDao.query( SqlQueryData.create( sql, Lists.newArrayList(), callback ) );

    return result;
  }
}
