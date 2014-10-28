package com.threebird.recorder.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.Schema;

public class Schemas
{
  /**
   * If schema.id is null, creates a new schema in the database. Otherwise, it
   * updates the row that matches schema.id
   */
  public static void save( Schema schema )
  {
    try {
      if (schema.id == null) {
        create( schema );
      } else {
        update( schema );
      }
    } catch (SQLException e) {
      throw new RuntimeException( e );
    }
  }

  private static void update( Schema schema ) throws SQLException
  {
    Connection conn = Persistence.getConnection();

    // Update the 'schemas' row
    String updateSql = "UPDATE schemas SET name = ?, duration = ? WHERE id = ?";
    PreparedStatement updateSmt = conn.prepareStatement( updateSql );
    updateSmt.setString( 1, schema.name );
    updateSmt.setInt( 2, schema.duration );
    updateSmt.setInt( 3, schema.id );
    updateSmt.executeUpdate();
    updateSmt.close();

    // Figure out which mappings need to be deleted/created
    Set< KeyBehaviorMapping > oldSet = KeyBehaviors.getAllForSchema( schema.id, conn );
    Set< KeyBehaviorMapping > newSet = Sets.newHashSet( schema.mappings.values() );

    SetView< KeyBehaviorMapping > delete = Sets.difference( oldSet, newSet );
    SetView< KeyBehaviorMapping > create = Sets.difference( newSet, oldSet );

    for (KeyBehaviorMapping mapping : delete) {
      KeyBehaviors.delete( schema.id, mapping.key, conn );
    }

    for (KeyBehaviorMapping mapping : create) {
      KeyBehaviors.create( schema.id, mapping, conn );
    }

    conn.close();
  }

  private static void create( Schema schema ) throws SQLException
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

    KeyBehaviors.addAll( schema.id, schema.mappings.values() );

    conn.close();
  }

  public static List< Schema > all()
  {
    try {
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

        Iterable< KeyBehaviorMapping > mappings = KeyBehaviors.getAllForSchema( s.id, conn );
        s.mappings = Maps.newHashMap( Maps.uniqueIndex( mappings, m -> m.key ) );

        result.add( s );
      }

      conn.close();

      return result;
    } catch (SQLException e) {
      throw new RuntimeException( e );
    }
  }
}
