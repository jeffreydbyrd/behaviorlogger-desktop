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

  public static void create( Schema s ) throws Exception
  {
    Schemas.create( s );
    schemas().add( s );
  }

  public static void update( Schema s ) throws Exception
  {
    if (!Schemas.hasChanged( s )) {
      return;
    }

    Optional< Schema > oldOpt = Schemas.getForUuid( s.uuid );
    Preconditions.checkState( oldOpt.isPresent() );
    Schema old = oldOpt.get();
    s.version = old.version + 1;

    Schemas.update( s );
  }

  private static Schema getFirstActiveSchema()
  {
    for (Schema s : schemas()) {
      if (!s.archived) {
        return s;
      }
    }

    return null;
  }

  /**
   * Returns the selected schema in the Start-Menu. If no schema is selected, defaults to the first element in
   * schemas(), or null if schemas() is empty.
   */
  public static SimpleObjectProperty< Schema > selectedProperty()
  {
    Schema selected = null;

    if (selectedProperty == null) {
      selected = getFirstActiveSchema();
      selectedProperty = new SimpleObjectProperty< Schema >( selected );
    } else if (selectedProperty.get() == null || !schemas().contains( selectedProperty.get() )) {
      selected = getFirstActiveSchema();
      selectedProperty.set( selected );
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
