package com.threebird.recorder.controllers;

import java.util.HashMap;
import java.util.Iterator;

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

import com.google.common.base.Strings;
import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.Schema;
import com.threebird.recorder.persistence.Schemas;

public class EditSchemaController
{
  @FXML private TextField nameField;

  @FXML private VBox mappingsBox;
  @FXML private Button addRowButton;

  @FXML private TextField hoursField;
  @FXML private TextField minutesField;
  @FXML private TextField secondsField;

  protected static int defaultNumBoxes = 10;
  protected static String digits = "0123456789";
  protected static String acceptableKeys =
      "abcdefghijklmnopqrstuvwxyz1234567890`-=[]\\;',./";
  private static final Insets insets = new Insets( .5, .5, .5, .5 );

  private Schema model;

  /**
   * @param sch
   *          - The Schema being edited. If null, a new schema is created
   */
  public void init( Schema sch )
  {
    model = sch == null ? new Schema() : sch;
    nameField.setText( Strings.nullToEmpty( model.name ) );

    populateMappingsBox( model );

    int hrs = model.duration / 60 / 60;
    int minDivisor = model.duration % 60 % 60;
    int mins = minDivisor / 60;
    int secs = minDivisor % 60;

    hoursField.setText( intToStr( hrs ) );
    minutesField.setText( intToStr( mins ) );
    secondsField.setText( intToStr( secs ) );

    hoursField.setOnKeyTyped( createFieldLimiter( hoursField, acceptableKeys, 2 ) );
    minutesField.setOnKeyTyped( createFieldLimiter( minutesField, acceptableKeys, 2 ) );
    secondsField.setOnKeyTyped( createFieldLimiter( secondsField, acceptableKeys, 2 ) );
  }

  private static String intToStr( int i )
  {
    return i > 0 ? i + "" : "";
  }

  private static int strToInt( String s )
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

  /**
   * @param field
   *          - the input-field that this EventHandler reads
   * @param acceptableKeys
   *          - a list of characters that the user is allowed to input
   * @param limit
   *          - the max length of the field
   * @return an EventHandler that consumes a KeyEvent if the typed Char is
   *         outside 'acceptableKeys' or if the length of 'field' is longer than
   *         'limit'
   */
  private EventHandler< ? super KeyEvent >
    createFieldLimiter( TextField field, String acceptableKeys, int limit )
  {
    return evt -> {
      if (field.getText().trim().length() == limit
          || !acceptableKeys.contains( evt.getCharacter() ))
        evt.consume();
    };
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
    CheckBox checkbox = new CheckBox();
    checkbox.setSelected( isContinuous );
    HBox.setHgrow( checkbox, Priority.NEVER );
    HBox.setMargin( checkbox, new Insets( 5, 5, 0, 10 ) );

    TextField keyField = new TextField( key );
    keyField.setMaxWidth( 40 );
    HBox.setHgrow( keyField, Priority.NEVER );
    HBox.setMargin( keyField, insets );
    keyField.setOnKeyTyped( createFieldLimiter( keyField, acceptableKeys, 1 ) );

    TextField behaviorField = new TextField( behavior );
    HBox.setHgrow( behaviorField, Priority.ALWAYS );
    HBox.setMargin( behaviorField, insets );

    mappingsBox.getChildren().add( new HBox( checkbox, keyField, behaviorField ) );
  }

  /**
   * Runs through the key-behavior pairs in schema and populates the mappingsBox
   */
  private void populateMappingsBox( Schema schema )
  {
    mappingsBox.getChildren().clear();

    schema.mappings.forEach( ( key, mapping ) -> {
      addMappingBox( mapping.isContinuous, mapping.key.toString(), mapping.behavior );
    } );

    // add some extra boxes at the end to match 'defaultNumBoxes'
    for (int i = schema.mappings.size(); i < defaultNumBoxes; i++) {
      addMappingBox( false, "", "" );
    }
  }

  /**
   * When user clicks "Add Row" under the key-behavior mappingsBox
   */
  @FXML private void onAddRowClicked( ActionEvent evt )
  {
    addMappingBox( false, "", "" );
  }

  /**
   * Simply bring the user back to the Schemas view with
   */
  @FXML void onCancelClicked( ActionEvent evt )
  {
    EventRecorder.toSchemasView();
  }

  @FXML void onSaveSchemaClicked( ActionEvent evt )
  {
    HashMap< Character, KeyBehaviorMapping > temp =
        new HashMap< Character, KeyBehaviorMapping >();
    ObservableList< Node > nodes =
        mappingsBox.getChildrenUnmodifiable();

    for (Node hbox : nodes) {
      Iterator< Node > it = ((HBox) hbox).getChildren().iterator();
      CheckBox checkbox = (CheckBox) it.next();
      TextField keyField = (TextField) it.next();
      TextField behaviorField = (TextField) it.next();

      String key = keyField.getText().trim();
      String behavior = behaviorField.getText().trim();
      boolean isContinuous = checkbox.isSelected();

      if (!key.isEmpty() && !behavior.isEmpty()) {
        Character ch = key.charAt( 0 );
        temp.put( ch, new KeyBehaviorMapping( key, behavior, isContinuous ) );
      }
    }

    model.name = Strings.nullToEmpty( nameField.getText().trim() );
    model.mappings = temp;
    model.duration = getDuration();

    Schemas.save( model );

    EventRecorder.toSchemasView();
  }
}
