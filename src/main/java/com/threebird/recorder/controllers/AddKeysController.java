package com.threebird.recorder.controllers;

import java.util.Collections;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.Behavior;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.models.schemas.Schema;
import com.threebird.recorder.models.schemas.SchemasManager;
import com.threebird.recorder.models.sessions.RecordingManager;
import com.threebird.recorder.persistence.Schemas;
import com.threebird.recorder.utils.Alerts;
import com.threebird.recorder.utils.EventRecorderUtil;

/**
 * Combines with add_keys.fxml. The researcher uses this view to add new keys
 * after pressing unknown keys during a recording
 */
public class AddKeysController
{
  private Map< TextField, KeyBehaviorMapping > behaviorFields = Maps.newHashMap();
  private Runnable onSave;

  @FXML private VBox mappingsBox;
  private RecordingManager manager;

  public static void showAddKeysView( RecordingManager manager, Runnable onSave )
  {
    String filepath = "views/add-keys.fxml";
    AddKeysController controller = EventRecorderUtil.showScene( filepath, "Add Keys" );
    controller.init( manager, onSave );
  }

  private void init( RecordingManager manager, Runnable onSave )
  {
    this.manager = manager;
    this.onSave = onSave;
    populateMappingsBox();
  }

  /**
   * For each KeyBehaviorMapping, print out a checkbox, some labels, and a
   * TextField. Wire these widgets to the model.
   */
  private void populateMappingsBox()
  {
    mappingsBox.getChildren().clear();

    for (KeyBehaviorMapping kbm : manager.unknowns.values()) {
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
   * 'schema', saves 'schema', updates the recorded behaviors, and redraws the
   * recording window with new information
   */
  @FXML private void onSavePress()
  {
    Schema schema = SchemasManager.getSelected();

    // Modify the Schema and save it
    behaviorFields.forEach( ( field, kbm ) -> {
      String behavior = field.getText().trim();
      schema.mappings.put( kbm.key, new KeyBehaviorMapping( kbm.key, behavior, kbm.isContinuous ) );
    } );

    try {
      Schemas.update( schema );
    } catch (Exception e) {
      Alerts.error( "Failed to Save Schema",
                    "The application encountered a problem while trying to save the schema.",
                    e );
      e.printStackTrace();
      return;
    }

    Set< MappableChar > unknownChars = manager.unknowns.keySet();
    Set< MappableChar > newChars =
        behaviorFields.values().stream()
                      .map( kbm -> kbm.key )
                      .collect( Collectors.toSet() );

    Set< MappableChar > ignoredChars =
        unknownChars.stream()
                    .filter( c -> !newChars.contains( c ) )
                    .collect( Collectors.toSet() );

    List< DiscreteBehavior > updatedDiscretes =
        manager.discrete.stream()
                        .filter( db -> newChars.contains( db.key ) )
                        .collect( Collectors.toList() );

    List< DiscreteBehavior > newDiscretes =
        Lists.transform( updatedDiscretes, db ->
                         new DiscreteBehavior( db.key,
                                               schema.mappings.get( db.key ).behavior,
                                               db.startTime ) );

    List< DiscreteBehavior > removedDiscretes =
        manager.discrete.stream()
                        .filter( db -> ignoredChars.contains( db.key ) )
                        .collect( Collectors.toList() );
    removedDiscretes.addAll( updatedDiscretes );

    List< ContinuousBehavior > updatedContinuous =
        manager.continuous.stream()
                          .filter( cb -> newChars.contains( cb.key ) )
                          .collect( Collectors.toList() );

    List< ContinuousBehavior > newContinuous =
        Lists.transform( updatedContinuous, cb ->
                         new ContinuousBehavior( cb.key,
                                                 schema.mappings.get( cb.key ).behavior,
                                                 cb.startTime,
                                                 cb.getDuration() ) );

    List< ContinuousBehavior > removedContinuous =
        manager.continuous.stream()
                          .filter( db -> ignoredChars.contains( db.key ) )
                          .collect( Collectors.toList() );
    removedContinuous.addAll( updatedContinuous );

    manager.discrete.removeAll( removedDiscretes );
    manager.discrete.addAll( newDiscretes );
    manager.continuous.removeAll( removedContinuous );
    manager.continuous.addAll( newContinuous );

    Collections.sort( manager.discrete, Behavior.comparator );
    Collections.sort( manager.continuous, Behavior.comparator );

    onSave.run();

    manager.unknowns.clear();
    EventRecorderUtil.dialogStage.get().close();
  }
}
