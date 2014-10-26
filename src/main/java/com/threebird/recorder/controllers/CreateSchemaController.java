package com.threebird.recorder.controllers;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.Schema;
import com.threebird.recorder.persistence.Schemas;

public class CreateSchemaController extends AbstractEditSchemaController
{
  private Schema newSchema = new Schema( "New Schema" );

  @FXML private void initialize()
  {
    addRowButton.requestFocus();

    for (int i = 0; i < 10; i++) {
      addMappingBox( "", "" );
    }
  }

  /**
   * Simply bring the user back to the Schemas view with
   */
  @FXML void onCancelClicked( ActionEvent evt )
  {
    EventRecorder.toSchemasView();
  }

  @FXML void onCreateSchemaClicked( ActionEvent evt )
  {
    HashMap< Character, KeyBehaviorMapping > temp =
        new HashMap< Character, KeyBehaviorMapping >();
    ObservableList< Node > nodes =
        mappingsBox.getChildrenUnmodifiable();

    for (Node hbox : nodes) {
      Iterator< Node > it = ((HBox) hbox).getChildren().iterator();
      TextField keyField = (TextField) it.next();
      TextField behaviorField = (TextField) it.next();
      CheckBox checkbox = (CheckBox) it.next();

      String key = keyField.getText().trim();
      String behavior = behaviorField.getText().trim();
      boolean isContinuous = checkbox.isSelected();

      if (!key.isEmpty() && !behavior.isEmpty()) {
        Character ch = key.charAt( 0 );
        temp.put( ch, new KeyBehaviorMapping( key, behavior, isContinuous ) );
      }
    }

    newSchema.mappings = temp;
    newSchema.duration = getDuration();

    try {
      Schemas.save( newSchema );
    } catch (SQLException e) {
      throw new RuntimeException( e );
    }
  }
}
