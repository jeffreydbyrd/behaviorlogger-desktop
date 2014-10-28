package com.threebird.recorder.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import com.google.common.collect.Sets;
import com.threebird.recorder.models.KeyBehaviorMapping;

public class KeyBehaviors
{

  public static Set< KeyBehaviorMapping > getAllForSchema( Integer schemaId, Connection c ) throws SQLException
  {
    String sql = "SELECT * FROM key_behaviors WHERE schema_id = " + schemaId;
    PreparedStatement selectMappings = c.prepareStatement( sql );
    ResultSet mappingSet = selectMappings.executeQuery();

    Set< KeyBehaviorMapping > mappings = Sets.newHashSet();

    while (mappingSet.next()) {
      String key = mappingSet.getString( "key" );
      String behavior = mappingSet.getString( "behavior" );
      boolean isContinuous = mappingSet.getBoolean( "is_continuous" );
      mappings.add( new KeyBehaviorMapping( key, behavior, isContinuous ) );
    }

    return mappings;
  }

  /**
   * Adds each KeyBehaviorMapping to key_behaviors. This function will explode
   * if you try to enter a duplicate
   */
  public static void addAll( Integer schemaId, Iterable< KeyBehaviorMapping > mappings ) throws SQLException
  {
    Connection c = Persistence.getConnection();
    for (KeyBehaviorMapping mapping : mappings) {
      create( schemaId, mapping, c );
    }
    c.close();
  }

  /**
   * Inserts a new KeyBehaviorMapping into the key_behaviors table
   */
  public static void create( Integer schemaId, KeyBehaviorMapping mapping, Connection conn ) throws SQLException
  {
    String sql =
        "INSERT INTO key_behaviors (schema_id,key,behavior,is_continuous) VALUES (?,?,?,?)";
    PreparedStatement stmt = conn.prepareStatement( sql );
    int c = 1;
    stmt.setInt( c++, schemaId );
    stmt.setString( c++, mapping.key.toString() );
    stmt.setString( c++, mapping.behavior );
    stmt.setBoolean( c++, mapping.isContinuous );
    stmt.executeUpdate();
    stmt.close();
  }

  /**
   * Deletes the KeyBehaviorMapping with a specific schema_id and key
   */
  public static void delete( Integer schemaId, Character key, Connection conn ) throws SQLException
  {
    String sql =
        "DELETE FROM key_behaviors WHERE schema_id = ? AND key = ?";
    PreparedStatement stmt = conn.prepareStatement( sql );
    stmt.setInt( 1, schemaId );
    stmt.setString( 2, key.toString() );
    stmt.executeUpdate();
    stmt.close();
  }
}
