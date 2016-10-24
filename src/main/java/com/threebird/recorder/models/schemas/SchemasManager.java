package com.threebird.recorder.models.schemas;

import java.util.Optional;

import com.google.common.base.Preconditions;
import com.threebird.recorder.persistence.Schemas;
import com.threebird.recorder.utils.Alerts;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SchemasManager
{
  private static ObservableList< Schema > schemas;
  private static SimpleObjectProperty< Schema > selectedProperty;

  public static ObservableList< Schema > schemas()
  {
    if (schemas == null) {
      try {
        schemas = FXCollections.observableArrayList( Schemas.all() );
      } catch (Exception e) {
        Alerts.error( "Error retrieving Schemas",
                      "There was an error retrieving your schemas from the local database.",
                      e );
        e.printStackTrace();
        schemas = FXCollections.emptyObservableList();
      }
    }
    return schemas;
  }

  public static void create( Schema s )
  {
    try {
      Schemas.create( s );
    } catch (Exception e) {
      e.printStackTrace();
      Alerts.error( "Failed to create schema", "Error while trying to create schema", e );
      return;
    }

    schemas().add( s );
  }

  public static void update( Schema s )
  {
    try {
      Optional< Schema > oldOpt = Schemas.getForUuid( s.uuid );
      Preconditions.checkState( oldOpt.isPresent() );

      Schema old = oldOpt.get();
      s.version = old.version + 1;

      Schemas.update( s );
    } catch (Exception e) {
      e.printStackTrace();
      Alerts.error( "Failed to archive schema.", "Error while trying to archive schema " + s.uuid, e );
      return;
    }
  }

  /**
   * Returns the selected schema in the Start-Menu. If no schema is selected, defaults to the first element in
   * schemas(), or null if schemas() is empty.
   */
  public static SimpleObjectProperty< Schema > selectedProperty()
  {
    if (selectedProperty == null || selectedProperty.get() == null || !schemas().contains( selectedProperty.get() )) {
      Schema selected = null;
      // = schemas().isEmpty() ? null : schemas().get( 0 );
      for (Schema s : schemas()) {
        if (!s.archived) {
          selected = s;
          break;
        }
      }
      selectedProperty = new SimpleObjectProperty< Schema >( selected );
    }
    return selectedProperty;
  }

  /**
   * Shortcut for selectedProperty().get()
   */
  public static Schema getSelected()
  {
    return selectedProperty().get();
  }

  /**
   * Shortcut for selectedProperty().set(schema)
   */
  public static void setSelected( Schema schema )
  {
    selectedProperty().set( schema );
  }
}
