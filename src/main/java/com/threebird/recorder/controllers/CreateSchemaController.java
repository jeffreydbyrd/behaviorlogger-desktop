package com.threebird.recorder.controllers;

import java.util.HashMap;
import java.util.Iterator;

import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.Schema;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CreateSchemaController
{
  @FXML private VBox mappingsBox;
  @FXML private Button addRowButton;

  @FXML private TextField hoursField;
  @FXML private TextField minutesField;
  @FXML private TextField secondsField;

  private Schema newSchema = new Schema( "New Schema" );

  @FXML private void initialize()
  {
    addRowButton.requestFocus();
    
    for (int i = 0; i < 10; i++) {
      addMappingBox( "", "" );
    }
  }

  private int strToInt( String s )
  {
    return Integer.valueOf( s.isEmpty() ? "0" : s );
  }

  /**
   * Converts the contents of hoursField, minutesField, and secondsField into
   * the equivalent number of seconds and
   */
  private int getDuration()
  {
    Integer hours = strToInt( hoursField.getText() );
    Integer mins = strToInt( minutesField.getText() );
    Integer secs = strToInt( secondsField.getText() );
    return (hours * 60 * 60) + (mins * 60) + secs;
  }

  private EventHandler< ? super KeyEvent >
    createFieldLimiter( TextField field, String acceptableKeys, int limit )
  {
    return evt -> {
      if (field.getText().trim().length() == limit
          || !acceptableKeys.contains( evt.getCharacter() ))
        evt.consume();
    };
  }

  private static final Insets insets = new Insets( .5, .5, .5, .5 );
  private static String acceptableKeys =
      "abcdefghijklmnopqrstuvwxyz1234567890`-=[]\\;',./";

  /**
   * Adds 2 adjacent text fields and a checkbox to 'mappingsBox'. Attaches a
   * KeyTyped EventHandler to the first field that prevents the user from typing
   * more than 1 key
   */
  private void addMappingBox( String key, String behavior )
  {
    TextField keyField = new TextField( key );
    keyField.setMaxWidth( 40 );
    HBox.setHgrow( keyField, Priority.NEVER );
    HBox.setMargin( keyField, insets );
    keyField.setOnKeyTyped( createFieldLimiter( keyField, acceptableKeys, 1 ) );

    TextField behaviorField = new TextField( behavior );
    HBox.setHgrow( behaviorField, Priority.ALWAYS );
    HBox.setMargin( behaviorField, insets );

    CheckBox checkbox = new CheckBox();
    HBox.setHgrow( checkbox, Priority.NEVER );
    HBox.setMargin( checkbox, new Insets( 3, 30, .5, 10 ) );

    mappingsBox.getChildren()
               .add( new HBox( keyField, behaviorField, checkbox ) );
  }

  @FXML void onAddRowClicked( ActionEvent evt )
  {
    addMappingBox( "", "" );
  }

  @FXML void onCancelClicked( ActionEvent evt )
  {
    EventRecorder.toSchemaView();
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
  }
}
