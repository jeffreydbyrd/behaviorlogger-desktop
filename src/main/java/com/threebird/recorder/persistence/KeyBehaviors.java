package com.threebird.recorder.persistence;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.utils.persistence.SqlCallback;
import com.threebird.recorder.utils.persistence.SqlQueryData;
import com.threebird.recorder.utils.persistence.SqliteDao;

/**
 * A set of static functions that interact with they key_behaviors table
 */
public class KeyBehaviors
{

  /**
   * For a given Schema, return all
   * 
   * @throws Exception
   */
  public static Set< KeyBehaviorMapping > getAllForSchema( String schemaId ) throws Exception
  {
    String sql = "SELECT * FROM key_behaviors WHERE schema_uuid = ?";

    Set< KeyBehaviorMapping > mappings = Sets.newHashSet();

    SqlCallback callback = rs -> {
      while (rs.next()) {
        String key = rs.getString( "key" );
        MappableChar ch = MappableChar.getForChar( key.charAt( 0 ) ).get();
        String behavior = rs.getString( "name" );
        boolean isContinuous = rs.getBoolean( "is_continuous" );
        mappings.add( new KeyBehaviorMapping( ch, behavior, isContinuous ) );
      }
    };

    SqliteDao.query( SqlQueryData.create( sql, Lists.newArrayList( schemaId ), callback ) );

    return mappings;
  }

  /**
   * Adds each KeyBehaviorMapping to key_behaviors. This function will explode
   * if you try to enter a duplicate
   * 
   * @throws Exception
   */
  public static void addAll( String schemaId, Iterable< KeyBehaviorMapping > mappings ) throws Exception
  {
    for (KeyBehaviorMapping mapping : mappings) {
      create( schemaId, mapping );
    }
  }

  /**
   * Inserts a new KeyBehaviorMapping into the key_behaviors table
   * 
   * @throws Exception
   */
  public static void create( String schemaId, KeyBehaviorMapping mapping ) throws Exception
  {
    String sql =
        "INSERT INTO key_behaviors (schema_uuid,key,name,is_continuous) VALUES (?,?,?,?)";
    List< Object > params =
        Lists.newArrayList( schemaId, mapping.key.c + "", mapping.behavior, mapping.isContinuous );

    SqliteDao.update( SqlQueryData.create( sql, params, SqlCallback.NOOP ) );
  }

  /**
   * Deletes the KeyBehaviorMapping with a specific schema_id and key
   * 
   * @throws Exception
   */
  public static void delete( String schemaId, MappableChar key ) throws Exception
  {
    String sql =
        "DELETE FROM key_behaviors WHERE schema_uuid = ? AND key = ?";

    List< Object > params =
        Lists.newArrayList( schemaId, key.c + "" );

    SqliteDao.update( SqlQueryData.create( sql, params, SqlCallback.NOOP ) );
  }
}
