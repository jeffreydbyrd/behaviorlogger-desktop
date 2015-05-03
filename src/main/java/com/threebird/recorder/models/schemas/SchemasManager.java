package com.threebird.recorder.models.schemas;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import com.threebird.recorder.persistence.Schemas;
import com.threebird.recorder.utils.Alerts;

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
      schemas.addListener( SchemasManager::onSchemasChange );
    }
    return schemas;
  }

  /**
   * Using JavaFX's fucked up "Change" iterator-like object, we basically delete
   * any removed schemas and save any new ones
   */
  private static void onSchemasChange( ListChangeListener.Change< ? extends Schema > c )
  {
    while (c.next()) {
      for (Schema schema : c.getAddedSubList()) {
        try {
          Schemas.save( schema );
        } catch (Exception e) {
          Alerts.error( "Failed to add Schema", "There was a problem while trying to save a schema.", e );
          e.printStackTrace();
        }
      }

      for (Schema schema : c.getRemoved()) {
        try {
          Schemas.delete( schema );
        } catch (Exception e) {
          Alerts.error( "Failed to delete Schema", "There was a problem while trying to delete schema.", e );
          e.printStackTrace();
        }

        if (schema.equals( getSelected() )) {
          setSelected( null );
        }
      }
    }
  }

  /**
   * Returns the selected schema in the Start-Menu. If no schema is selected,
   * defaults to the first element in schemas(), or null if schemas() is empty.
   */
  public static SimpleObjectProperty< Schema > selectedProperty()
  {
    if (selectedProperty == null || selectedProperty.get() == null || !schemas().contains( selectedProperty.get() )) {
      Schema selected = schemas().isEmpty() ? null : schemas().get( 0 );
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
