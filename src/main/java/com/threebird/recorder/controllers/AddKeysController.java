package com.threebird.recorder.controllers;

import java.util.Set;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.Schema;

public class AddKeysController
{
  private Scene recordingScene;
  private Schema schema;
  private Set< Character > unknowns;

  @FXML private VBox mappingsBox;

  /**
   * @param recordingScene
   *          - a reference back to the recording scene so we can go back to it
   *          after exiting the add-keys view
   * @param schema
   *          - the schema we are adding new keys to
   * @param unknowns
   *          - the set of uknown keys
   */
  public void init( Scene recordingScene, Schema schema, Set< Character > unknowns )
  {
    this.recordingScene = recordingScene;
    this.schema = schema;
    this.unknowns = unknowns;

    populateMappingsBox();
  }

  private static final Insets insets = new Insets( 2, 2, 2, 2 );

  private void addMappingBox( String key, String behavior )
  {
    CheckBox checkbox = new CheckBox();
    checkbox.setSelected( false );
    HBox.setHgrow( checkbox, Priority.NEVER );
    HBox.setMargin( checkbox, new Insets( 5, 5, 0, 10 ) );

    Label keyText = new Label( key );
    keyText.setMaxWidth( 40 );
    keyText.setAlignment( Pos.CENTER );
    keyText.setPadding( new Insets( 6, 4, 0, 0 ) );
    HBox.setHgrow( keyText, Priority.NEVER );

    TextField behaviorField = new TextField( behavior );
    HBox.setHgrow( behaviorField, Priority.ALWAYS );
    HBox.setMargin( behaviorField, insets );

    HBox box = new HBox( checkbox, keyText, behaviorField );
    mappingsBox.getChildren().add( box );
  }

  private void populateMappingsBox()
  {
    mappingsBox.getChildren().clear();

    for (Character c : unknowns) {
      addMappingBox( c.toString(), "UNKNOWN" );
    }
  }

  /**
   * Bring us back to the recording view
   */
  @FXML private void onCancelPress( ActionEvent evt )
  {
    EventRecorder.STAGE.setScene( recordingScene );
  }
}
