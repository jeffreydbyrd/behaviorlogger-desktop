package com.behaviorlogger.persistence;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.behaviorlogger.models.MappableChar;
import com.behaviorlogger.models.schemas.KeyBehaviorMapping;
import com.behaviorlogger.models.schemas.SchemaVersion;
import com.behaviorlogger.utils.persistence.SqlCallback;
import com.behaviorlogger.utils.persistence.SqliteDao;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * A set of static functions that interact with they key_behaviors table
 */
public class KeyBehaviors
{
  private static final String BEHAVIORS_TBL = "behaviors_v1_1";
  private static final String BEHAVIOR_VERSIONS_TBL = "behavior_versions_v1_1";

  /**
   * For a given Schema, return all
   * 
   * @throws Exception
   */
  public static Set< KeyBehaviorMapping > getAllForSchema( String schemaVersionId ) throws Exception
  {
    String sql =
        "SELECT bv.behavior_uuid, bv.k, bv.description, b.is_continuous, bv.archived "
            + "FROM " + BEHAVIORS_TBL + " AS b"
            + "  JOIN " + BEHAVIOR_VERSIONS_TBL + " AS bv"
            + "  ON b.uuid = bv.behavior_uuid "
            + "WHERE bv.schema_version_uuid = ?";

    Set< KeyBehaviorMapping > mappings = Sets.newHashSet();

    SqlCallback callback = rs -> {
      while (rs.next()) {
        String behaviorUuid = rs.getString( "behavior_uuid" );
        String k = rs.getString( "k" );
        String description = rs.getString( "description" );
        boolean isContinuous = rs.getBoolean( "is_continuous" );
        boolean archived = rs.getBoolean( "archived" );

        MappableChar ch = MappableChar.getForChar( k.charAt( 0 ) ).get();
        mappings.add( new KeyBehaviorMapping( behaviorUuid, ch, description, isContinuous, archived ) );
      }
    };

    SqliteDao.query( sql, Lists.newArrayList( schemaVersionId ), callback );

    return mappings;
  }

  public static void save( SchemaVersion schema, KeyBehaviorMapping mapping ) throws Exception
  {
    // Validate UUID
    if (mapping.uuid == null) {
      mapping.uuid = UUID.randomUUID().toString();
    } else {
      UUID.fromString( mapping.uuid );
    }

    // Create in behaviors if it doesn't exist
    String insertBehavior =
        "INSERT OR IGNORE INTO " + BEHAVIORS_TBL + " (uuid, schema_uuid, is_continuous) VALUES (?,?,?)";
    List< Object > params1 =
        Lists.newArrayList( mapping.uuid, schema.uuid, mapping.isContinuous );
    SqliteDao.update( insertBehavior, params1, SqlCallback.NOOP );

    // Insert new entry in behavior_versions
    String sql =
        "INSERT INTO " + BEHAVIOR_VERSIONS_TBL
            + " (behavior_uuid, schema_version_uuid, k, description, archived) VALUES (?,?,?,?,?)";

    List< Object > params =
        Lists.newArrayList( mapping.uuid,
                            schema.versionUuid,
                            mapping.key.c + "",
                            mapping.description,
                            mapping.archived );

    SqliteDao.update( sql, params, SqlCallback.NOOP );
  }
}
