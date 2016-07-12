package com.threebird.recorder.persistence;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.models.schemas.Schema;
import com.threebird.recorder.utils.persistence.SqlCallback;
import com.threebird.recorder.utils.persistence.SqliteDao;

/**
 * A set of static functions that interact with they key_behaviors table
 */
public class KeyBehaviors
{
  private static final String TBL_NAME = "behaviors_v1_1";
  private static final String BRIDGE_NAME = "schema_behaviors_v1_1";

  /**
   * For a given Schema, return all
   * 
   * @throws Exception
   */
  public static Set< KeyBehaviorMapping > getAllForSchema( String schemaId ) throws Exception
  {
    String sql =
        "SELECT b.uuid, b.key, b.description, b.is_continuous "
            + "FROM " + TBL_NAME + " AS b"
            + "  JOIN " + BRIDGE_NAME + " AS sb"
            + "  ON"
            + "    b.uuid = sb.behavior_uuid"
            + "    AND schema_uuid = ?"
            + "    AND schema_version = (SELECT MAX(version) FROM schemas_v1_1 WHERE uuid = ?)";

    Set< KeyBehaviorMapping > mappings = Sets.newHashSet();

    SqlCallback callback = rs -> {
      while (rs.next()) {
        String uuid = rs.getString( "uuid" );
        String key = rs.getString( "key" );
        MappableChar ch = MappableChar.getForChar( key.charAt( 0 ) ).get();
        String behavior = rs.getString( "description" );
        boolean isContinuous = rs.getBoolean( "is_continuous" );
        mappings.add( new KeyBehaviorMapping( uuid, ch, behavior, isContinuous ) );
      }
    };

    SqliteDao.query( sql, Lists.newArrayList( schemaId, schemaId ), callback );

    return mappings;
  }

  /**
   * Adds each KeyBehaviorMapping to key_behaviors. This function will explode if you try to enter a duplicate
   * 
   * @throws Exception
   */
  public static void addAll( Schema schema, Iterable< KeyBehaviorMapping > mappings ) throws Exception
  {
    for (KeyBehaviorMapping mapping : mappings) {
      create( schema, mapping );
    }
  }

  /**
   * Inserts a new KeyBehaviorMapping into the behaviors table and schema_behaviors brdige table
   * 
   * @throws Exception
   */
  public static void create( Schema schema, KeyBehaviorMapping mapping ) throws Exception
  {
    String insertBehavior =
        "INSERT INTO " + TBL_NAME + " (uuid, key, description, is_continuous) VALUES (?,?,?,?)";
    List< Object > params1 =
        Lists.newArrayList( mapping.uuid, mapping.key.c + "", mapping.behavior, mapping.isContinuous );
    SqliteDao.update( insertBehavior, params1, SqlCallback.NOOP );

    bridge( schema.uuid, schema.version, mapping.uuid );
  }

  public static void update( String uuid, KeyBehaviorMapping mapping ) throws Exception
  {
    String sql =
        "UPDATE " + TBL_NAME + " SET key=?, description=? WHERE uuid=?";

    List< Object > params =
        Lists.newArrayList( mapping.key.c + "", mapping.behavior, mapping.uuid );

    SqliteDao.update( sql, params, SqlCallback.NOOP );
  }

  public static void bridge( String schemaUuid, int version, String behaviorUuid ) throws Exception
  {
    String insertSchemaBehavior =
        "INSERT INTO " + BRIDGE_NAME + "(schema_uuid, schema_version, behavior_uuid) VALUES (?,?,?)";
    List< Object > params2 =
        Lists.newArrayList( schemaUuid, version, behaviorUuid );
    SqliteDao.update( insertSchemaBehavior, params2, SqlCallback.NOOP );
  }
}
