package com.threebird.recorder.controllers;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.Schema;
import com.threebird.recorder.persistence.Schemas;

/**
 * Combines with add_keys.fxml. The researcher uses this view to add new keys
 * after pressing unknown keys during a recording
 */
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
   * Adds a new row (a checkbox, a label, and a text field) to the mappingsBox
   */
  private void addMappingBox( String key, String behavior )
  {
    CheckBox checkbox = new CheckBox();
    checkbox.setSelected( false );
    HBox.setHgrow( checkbox, Priority.NEVER );
    HBox.setMargin( checkbox, new Insets( 5, 0, 0, 10 ) );

    Label keyText = new Label( key );
    keyText.setMaxWidth( 40 );
    keyText.setMinWidth( 40 );
    keyText.setAlignment( Pos.CENTER );
    keyText.setPadding( new Insets( 6, 0, 0, 10 ) );
    HBox.setHgrow( keyText, Priority.NEVER );

    TextField behaviorField = new TextField( behavior );
    HBox.setHgrow( behaviorField, Priority.ALWAYS );
    HBox.setMargin( behaviorField, new Insets( 2, 2, 2, 10 ) );

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
   * Converts a row from the mappingsBox into a KeyBehaviorMapping
   */
  private KeyBehaviorMapping toKeyBehaviorMapping( Node n )
  {
    HBox box = (HBox) n;
    Iterator< Node > iter = box.getChildren().iterator();

    CheckBox checkbox = (CheckBox) iter.next();
    Label label = (Label) iter.next();
    TextField textfield = (TextField) iter.next();

    boolean isContinuous = checkbox.isSelected();
    String key = label.getText();
    String behavior = textfield.getText();

    return new KeyBehaviorMapping( key, behavior, isContinuous );
  }

  /**
   * Bring us back to the recording view
   */
  @FXML private void onCancelPress( ActionEvent evt )
  {
    EventRecorder.STAGE.setScene( recordingScene );
  }

  /**
   * Runs through the mappingsBox and adds new KeyBehaviorMappings to 'schema'.
   * Then saves 'schema'
   */
  @FXML private void onSavePress( ActionEvent evt )
  {
    List< KeyBehaviorMapping > keyBehaviors =
        mappingsBox.getChildren().stream()
                   .map( this::toKeyBehaviorMapping )
                   .collect( Collectors.toList() );

    for (KeyBehaviorMapping behavior : keyBehaviors) {
      schema.mappings.put( behavior.key, behavior );
    }

    Schemas.save( schema );

    EventRecorder.STAGE.setScene( recordingScene );
  }
}
