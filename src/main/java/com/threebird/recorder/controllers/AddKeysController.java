package com.threebird.recorder.controllers;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.BehaviorEvent;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.models.schemas.SchemaVersion;
import com.threebird.recorder.models.schemas.SchemasManager;
import com.threebird.recorder.models.sessions.RecordingManager;
import com.threebird.recorder.utils.Alerts;
import com.threebird.recorder.utils.BehaviorLoggerUtil;

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
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Combines with add_keys.fxml. The researcher uses this view to add new keys after pressing unknown keys during a
 * recording
 */
public class AddKeysController
{
  private Map< TextField, KeyBehaviorMapping > behaviorFields = Maps.newHashMap();
  private Runnable onSave;

  @FXML private VBox mappingsBox;
  @FXML private VBox errMsgBox;
  private RecordingManager manager;

  public static void showAddKeysView( RecordingManager manager, Runnable onSave )
  {
    String filepath = "views/add-keys.fxml";
    AddKeysController controller = BehaviorLoggerUtil.showScene( filepath, "Add Keys" );
    controller.init( manager, onSave );
  }

  private void init( RecordingManager manager, Runnable onSave )
  {
    this.manager = manager;
    this.onSave = onSave;
    populateMappingsBox();
  }

  /**
   * For each KeyBehaviorMapping, print out a checkbox, some labels, and a TextField. Wire these widgets to the model.
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
      behaviorField.setPromptText( kbm.description );
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
    BehaviorLoggerUtil.dialogStage.get().close();
  }

  /**
   * Runs through the 'behaviorFields' and adds new KeyBehaviorMappings to 'schema', saves 'schema', updates the
   * recorded behaviors, and redraws the recording window with new information
   */
  @FXML private void onSavePress()
  {
    SchemaVersion schema = SchemasManager.getSelected();

    if (!validate()) {
      return;
    }

    // Set UUIDs on new behaviors, modify the Schema and save it
    behaviorFields.forEach( ( field, kbm ) -> {
      String behavior = field.getText().trim();
      if (kbm.uuid == null) {
        kbm.uuid = UUID.randomUUID().toString();
      }
      schema.behaviors.add( new KeyBehaviorMapping( kbm.uuid, kbm.key, behavior, kbm.isContinuous, kbm.archived ) );
    } );

    try {
      SchemasManager.save( schema );
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
        Lists.transform( updatedDiscretes, db -> new DiscreteBehavior( db.uuid,
                                                                       db.key,
                                                                       schema.behaviorsMap().get( db.key ).description,
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
        Lists.transform( updatedContinuous, cb -> new ContinuousBehavior( cb.uuid,
                                                                          cb.key,
                                                                          schema.behaviorsMap().get( cb.key ).description,
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

    Collections.sort( manager.discrete, BehaviorEvent.comparator );
    Collections.sort( manager.continuous, BehaviorEvent.comparator );

    onSave.run();

    manager.unknowns.clear();
    BehaviorLoggerUtil.dialogStage.get().close();
  }

  private boolean validate()
  {
    errMsgBox.getChildren().clear();

    String cssRed = "-fx-background-color:#FFDDDD;-fx-border-color: #f00;";
    Text emptyMsg = new Text( "Selected items should have a value." );
    emptyMsg.setFill( Color.RED );
    Text dupMsg = new Text( "Each name must be unique." );
    dupMsg.setFill( Color.RED );

    AtomicBoolean nonEmptyValid = new AtomicBoolean( true );
    AtomicBoolean noDuplicatesValid = new AtomicBoolean( true );

    behaviorFields.forEach( ( field, kbm ) -> {
      String name = field.getText().trim();
      if (Strings.isNullOrEmpty( name )) {
        nonEmptyValid.set( false );
        field.setStyle( cssRed );
      } else {
        field.setStyle( "" );
      }
    } );

    Map< String, List< TextField > > byName =
        behaviorFields.keySet().stream().collect( Collectors.groupingBy( field -> field.getText().trim() ) );

    byName.forEach( ( name, fields ) -> {
      if (fields.size() > 1) {
        noDuplicatesValid.set( false );
      }
      String style = fields.size() > 1 ? cssRed : "";
      fields.forEach( field -> field.setStyle( style ) );
    } );

    if (!nonEmptyValid.get()) {
      errMsgBox.getChildren().add( emptyMsg );
    }

    if (!noDuplicatesValid.get()) {
      errMsgBox.getChildren().add( dupMsg );
    }

    return nonEmptyValid.get() && noDuplicatesValid.get();
  }
}
