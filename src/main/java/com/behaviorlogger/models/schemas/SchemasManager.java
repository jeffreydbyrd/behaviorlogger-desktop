package com.behaviorlogger.models.schemas;

import java.util.UUID;

import com.behaviorlogger.persistence.Schemas;
import com.behaviorlogger.utils.Alerts;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SchemasManager
{
  private static ObservableList< SchemaVersion > schemas;
  private static SimpleObjectProperty< SchemaVersion > selectedProperty;

  public static ObservableList< SchemaVersion > schemas()
  {
    if (schemas == null) {
      try {
        schemas = FXCollections.observableArrayList( Schemas.allLatest() );
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

  private static SchemaVersion getFirstActiveSchema()
  {
    for (SchemaVersion s : schemas()) {
      if (!s.archived) {
        return s;
      }
    }

    return null;
  }
  
  public static void save(SchemaVersion sv) throws Exception {
    if (sv.uuid == null) {
      sv.uuid = UUID.randomUUID().toString();
      sv.versionNumber = 0;
    }
    
    sv.versionUuid = UUID.randomUUID().toString();
    sv.versionNumber++;
    
    Schemas.save( sv );
  }

  /**
   * Returns the selected schema in the Start-Menu. If no schema is selected, defaults to the first active element in
   * schemas(), or null if schemas() is empty.
   */
  public static SimpleObjectProperty< SchemaVersion > selectedProperty()
  {
    SchemaVersion selected = null;

    if (selectedProperty == null) {
      selected = getFirstActiveSchema();
      selectedProperty = new SimpleObjectProperty< SchemaVersion >( selected );
    } else if (selectedProperty.get() == null || !schemas().contains( selectedProperty.get() )) {
      selected = getFirstActiveSchema();
      selectedProperty.set( selected );
    }

    return selectedProperty;
  }

  /**
   * Shortcut for selectedProperty().get()
   */
  public static SchemaVersion getSelected()
  {
    return selectedProperty().get();
  }

  /**
   * Shortcut for selectedProperty().set(schema)
   */
  public static void setSelected( SchemaVersion schema )
  {
    selectedProperty().set( schema );
  }
}
