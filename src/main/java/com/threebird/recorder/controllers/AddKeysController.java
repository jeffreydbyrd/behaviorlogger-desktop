package com.threebird.recorder.controllers;

import java.util.Set;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;

import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.Schema;
import com.threebird.recorder.views.MappingBox;

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

  /**
   * Adds 2 adjacent text fields and a checkbox to 'mappingsBox'. Attaches a
   * KeyTyped EventHandler to the first field that prevents the user from typing
   * more than 1 key
   */
  private void addMappingBox( boolean isContinuous,
                              String key,
                              String behavior )
  {
    MappingBox box = new MappingBox( isContinuous, key, behavior );
    box.keyField.setDisable( true );
    mappingsBox.getChildren().add( box );
  }

  private void populateMappingsBox()
  {
    mappingsBox.getChildren().clear();

    for (Character c : unknowns) {
      addMappingBox( false, c.toString(), "UNKNOWN" );
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
