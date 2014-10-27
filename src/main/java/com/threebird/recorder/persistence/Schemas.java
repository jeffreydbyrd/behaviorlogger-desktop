package com.threebird.recorder.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.google.common.collect.Lists;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.Schema;

public class Schemas
{
  public static void save( Schema schema ) throws SQLException
  {
    Connection conn = Persistence.getConnection();

    // Make new entry in `schemas`
    String schemasSql = "INSERT INTO schemas (name, duration) VALUES (?,?)";
    PreparedStatement schemasStmt = conn.prepareStatement( schemasSql );
    schemasStmt.setString( 1, schema.name );
    schemasStmt.setInt( 2, schema.duration );
    schemasStmt.executeUpdate();
    schema.id = schemasStmt.getGeneratedKeys().getInt( 1 );
    schemasStmt.close();

    // Insert each behavior to `key_behaviors`
    for (KeyBehaviorMapping mapping : schema.mappings.values()) {
      String sql =
          "INSERT INTO key_behaviors (key,behavior,is_continuous,schema_id) VALUES (?,?,?,?)";
      PreparedStatement stmt = conn.prepareStatement( sql );
      stmt.setString( 1, mapping.key.toString() );
      stmt.setString( 2, mapping.behavior );
      stmt.setBoolean( 3, mapping.isContinuous );
      stmt.setInt( 4, schema.id );
      stmt.executeUpdate();
      stmt.close();
    }

    conn.close();
  }

  public static List< Schema > all() throws SQLException
  {
    Connection conn = Persistence.getConnection();
    PreparedStatement selectAll =
        conn.prepareStatement( "SELECT * FROM schemas" );
    ResultSet schemaSet = selectAll.executeQuery();

    List< Schema > result = Lists.newArrayList();

    while (schemaSet.next()) {
      Schema s = new Schema();
      s.id = schemaSet.getInt( "id" );
      s.name = schemaSet.getString( "name" );
      s.duration = schemaSet.getInt( "duration" );

      String sql = "SELECT * FROM key_behaviors WHERE schema_id = " + s.id;
      PreparedStatement selectMappings = conn.prepareStatement( sql );
      ResultSet mappingSet = selectMappings.executeQuery();

      while (mappingSet.next()) {
        String key = mappingSet.getString( "key" );
        String behavior = mappingSet.getString( "behavior" );
        boolean isContinuous = mappingSet.getBoolean( "is_continuous" );
        s.addMapping( new KeyBehaviorMapping( key, behavior, isContinuous ) );
      }

      result.add( s );
    }

    return result;
  }
}
