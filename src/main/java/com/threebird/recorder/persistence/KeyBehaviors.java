package com.threebird.recorder.persistence;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.persistence.util.SqlCallback;
import com.threebird.recorder.persistence.util.SqlQueryData;
import com.threebird.recorder.persistence.util.SqliteDao;

/**
 * A set of static functions that interact with they key_behaviors table
 */
public class KeyBehaviors
{

  /**
   * For a given Schema, return all
   */
  public static Set< KeyBehaviorMapping > getAllForSchema( Integer schemaId )
  {
    String sql = "SELECT * FROM key_behaviors WHERE schema_id = " + schemaId;

    Set< KeyBehaviorMapping > mappings = Sets.newHashSet();

    SqlCallback callback = mappingSet -> {
      while (mappingSet.next()) {
        String key = mappingSet.getString( "key" );
        String behavior = mappingSet.getString( "behavior" );
        boolean isContinuous = mappingSet.getBoolean( "is_continuous" );
        mappings.add( new KeyBehaviorMapping( key, behavior, isContinuous ) );
      }
    };

    SqliteDao.query( SqlQueryData.create( sql, Lists.newArrayList(), callback ) );

    return mappings;
  }

  /**
   * Adds each KeyBehaviorMapping to key_behaviors. This function will explode
   * if you try to enter a duplicate
   */
  public static void addAll( Integer schemaId, Iterable< KeyBehaviorMapping > mappings )
  {
    for (KeyBehaviorMapping mapping : mappings) {
      create( schemaId, mapping );
    }
  }

  /**
   * Inserts a new KeyBehaviorMapping into the key_behaviors table
   */
  public static void create( Integer schemaId, KeyBehaviorMapping mapping )
  {
    String sql =
        "INSERT INTO key_behaviors (schema_id,key,behavior,is_continuous) VALUES (?,?,?,?)";
    List< Object > params =
        Lists.newArrayList( schemaId, mapping.key.toString(), mapping.behavior, mapping.isContinuous );

    SqliteDao.update( SqlQueryData.create( sql, params, stmt -> {} ) );
  }

  /**
   * Deletes the KeyBehaviorMapping with a specific schema_id and key
   */
  public static void delete( Integer schemaId, Character key )
  {
    String sql =
        "DELETE FROM key_behaviors WHERE schema_id = ? AND key = ?";

    List< Object > params =
        Lists.newArrayList( schemaId, key.toString() );

    SqliteDao.update( SqlQueryData.create( sql, params, rs -> {} ) );
  }
}
