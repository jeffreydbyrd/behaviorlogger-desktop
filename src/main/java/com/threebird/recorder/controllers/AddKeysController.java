package com.threebird.recorder.controllers;

import java.util.Collection;
import java.util.Map;

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

import com.google.common.collect.Maps;
import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.Schema;
import com.threebird.recorder.persistence.Schemas;
import com.threebird.recorder.utils.EventRecorderUtil;

/**
 * Combines with add_keys.fxml. The researcher uses this view to add new keys
 * after pressing unknown keys during a recording
 */
public class AddKeysController
{
  private Scene recordingScene;
  private RecordingController recordingController;
  private Schema schema;
  private Collection< KeyBehaviorMapping > unknowns;
  private Map< TextField, KeyBehaviorMapping > behaviorFields = Maps.newHashMap();

  @FXML private VBox mappingsBox;

  public static void toAddKeysView( Scene recordingScene,
                                    RecordingController recordingController,
                                    Schema schema,
                                    Collection< KeyBehaviorMapping > unknowns )
  {
    String filepath = "./views/add_keys.fxml";
    AddKeysController controller = EventRecorderUtil.loadScene( filepath, "Add Keys" );
    controller.init( recordingScene, recordingController, schema, unknowns );
  }

  /**
   * @param recordingScene
   *          - a reference back to the recording scene so we can go back to it
   *          after exiting the add-keys view
   * @param schema
   *          - the schema we are adding new keys to
   * @param unknowns
   *          - the collection of unknown behavior mappings
   */
  private void init( Scene recordingScene,
                     RecordingController controller,
                     Schema schema,
                     Collection< KeyBehaviorMapping > unknowns )
  {
    this.recordingScene = recordingScene;
    this.recordingController = controller;
    this.schema = schema;
    this.unknowns = unknowns;

    populateMappingsBox();
  }

  /**
   * For each KeyBehaviorMapping, print out a checkbox, some labels, and a
   * TextField. Wire these widgets to the model.
   */
  private void populateMappingsBox()
  {
    mappingsBox.getChildren().clear();

    for (KeyBehaviorMapping kbm : unknowns) {
      CheckBox selectBox = new CheckBox();
      HBox.setHgrow( selectBox, Priority.NEVER );
      HBox.setMargin( selectBox, new Insets( 5, 0, 0, 10 ) );

      Label contLabel = new Label( kbm.isContinuous ? "cont." : "" );
      contLabel.setMinWidth( 35 );
      contLabel.setMaxWidth( 35 );
      HBox.setHgrow( contLabel, Priority.NEVER );
      HBox.setMargin( contLabel, new Insets( 5, 0, 0, 5 ) );

      Label keyText = new Label( kbm.key.toString() );
      keyText.setMaxWidth( 40 );
      keyText.setMinWidth( 40 );
      keyText.setAlignment( Pos.CENTER );
      keyText.setPadding( new Insets( 6, 0, 0, 10 ) );
      HBox.setHgrow( keyText, Priority.NEVER );

      TextField behaviorField = new TextField();
      behaviorField.setPromptText( kbm.behavior );
      HBox.setHgrow( behaviorField, Priority.ALWAYS );
      HBox.setMargin( behaviorField, new Insets( 2, 2, 2, 10 ) );

      selectBox.selectedProperty().addListener( ( observable,
                                                  oldValue,
                                                  newValue ) -> {
        behaviorField.setDisable( !newValue );
        if (newValue) {
          behaviorFields.put( behaviorField, kbm );
        } else {
          behaviorFields.remove( behaviorField );
        }
      } );

      selectBox.selectedProperty().setValue( true );

      HBox box = new HBox( selectBox, contLabel, keyText, behaviorField );
      mappingsBox.getChildren().add( box );
    }
  }

  /**
   * Bring us back to the recording view
   */
  @FXML private void onCancelPress( ActionEvent evt )
  {
    EventRecorder.STAGE.setScene( recordingScene );
  }

  /**
   * Runs through the 'behaviorFields' and adds new KeyBehaviorMappings to
   * 'schema'. Then saves 'schema'
   */
  @FXML private void onSavePress( ActionEvent evt )
  {
    behaviorFields.forEach( ( field, kbm ) -> {
      String behavior = field.getText().trim();
      schema.mappings.put( kbm.key, new KeyBehaviorMapping( kbm.key, behavior, kbm.isContinuous ) );
    } );

    Schemas.save( schema );
    recordingController.update();
    EventRecorder.STAGE.setScene( recordingScene );
  }
}
