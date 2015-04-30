package com.threebird.recorder.controllers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import com.google.common.collect.Maps;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.models.schemas.Schema;
import com.threebird.recorder.models.schemas.SchemasManager;
import com.threebird.recorder.models.sessions.RecordingManager;
import com.threebird.recorder.persistence.Schemas;
import com.threebird.recorder.utils.EventRecorderUtil;

/**
 * Combines with add_keys.fxml. The researcher uses this view to add new keys
 * after pressing unknown keys during a recording
 */
public class AddKeysController
{
  private RecordingController recordingController;
  private Collection< KeyBehaviorMapping > unknowns;
  private Map< TextField, KeyBehaviorMapping > behaviorFields = Maps.newHashMap();

  @FXML private VBox mappingsBox;
  private RecordingManager manager;

  public static void showAddKeysView( RecordingController recordingController,
                                      RecordingManager manager )
  {
    String filepath = "views/add-keys.fxml";
    AddKeysController controller = EventRecorderUtil.showScene( filepath, "Add Keys" );
    controller.init( recordingController, manager );
  }

  private void init( RecordingController controller, RecordingManager manager )
  {
    this.recordingController = controller;
    this.manager = manager;
    this.unknowns = manager.unknowns.values();

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

      Label keyText = new Label( kbm.key.c + "" );
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
   * Close the window
   */
  @FXML private void onCancelPress( ActionEvent evt )
  {
    EventRecorderUtil.dialogStage.get().close();
  }

  /**
   * Runs through the 'behaviorFields' and adds new KeyBehaviorMappings to
   * 'schema', saves 'schema', redraws the recording window with new information
   */
  @FXML private void onSavePress( ActionEvent evt )
  {
    Schema schema = SchemasManager.getSelected();

    behaviorFields.forEach( ( field, kbm ) -> {
      String behavior = field.getText().trim();
      schema.mappings.put( kbm.key, new KeyBehaviorMapping( kbm.key, behavior, kbm.isContinuous ) );
    } );

    Schemas.save( schema );

    Set< MappableChar > newChars = behaviorFields.values()
                                                 .stream()
                                                 .map( kbm -> kbm.key )
                                                 .collect( Collectors.toSet() );

    List< DiscreteBehavior > updatedDiscretes =
        manager.discrete.stream()
                        .filter( disc -> newChars.contains( disc.key ) )
                        .collect( Collectors.toList() );

    // So the user has mapped this behavior. We must remove it and add an
    // updated version
    for (DiscreteBehavior discrete : updatedDiscretes) {
      String description = schema.mappings.get( discrete.key ).behavior;
      DiscreteBehavior addMe =
          new DiscreteBehavior( discrete.key, description, discrete.startTime );
      manager.discrete.remove( discrete );
      manager.discrete.add( addMe );
    }

    List< ContinuousBehavior > updatedContinuous =
        manager.continuous.stream()
                          .filter( disc -> newChars.contains( disc.key ) )
                          .collect( Collectors.toList() );

    for (ContinuousBehavior continuous : updatedContinuous) {
      String description = schema.mappings.get( continuous.key ).behavior;
      ContinuousBehavior addMe =
          new ContinuousBehavior( continuous.key, description, continuous.startTime, continuous.getDuration() );
      manager.continuous.remove( continuous );
      manager.continuous.add( addMe );
    }

    recordingController.update();

    manager.unknowns.clear();
    EventRecorderUtil.dialogStage.get().close();
  }
}
