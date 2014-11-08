package com.threebird.recorder.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.Schema;
import com.threebird.recorder.persistence.Schemas;
import com.threebird.recorder.views.edit_schema.MappingBox;

public class EditSchemaController
{
  @FXML private TextField nameField;

  @FXML private VBox mappingsBox;
  @FXML private Button addRowButton;

  @FXML private TextField hoursField;
  @FXML private TextField minutesField;
  @FXML private TextField secondsField;

  @FXML private VBox errorMsgBox;

  protected static int defaultNumBoxes = 10;
  protected static String digits = "0123456789";
  protected static String acceptableKeys =
      "abcdefghijklmnopqrstuvwxyz1234567890`-=[]\\;',./";

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
  public static EventHandler< ? super KeyEvent >
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
    mappingsBox.getChildren().add( new MappingBox( isContinuous, key, behavior ) );
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
   * Runs through the scene and makes sure the nameField is filled in, and that
   * there are no duplicate keys. Any validation errors will get highlighted
   * red, and a message gets put in the 'errorMsgBox'
   * 
   * @return true if all inputs are valid, or false if a single input fails
   */
  private boolean validate()
  {
    boolean isValid = true;
    errorMsgBox.getChildren().clear();

    String cssRed = "-fx-background-color:#FFDDDD;-fx-border-color: #f00;";

    // Make some red error messages:
    Text nameMsg = new Text( "You must enter a name." );
    nameMsg.setFill( Color.RED );
    Text keyMsg = new Text( "Each key must be unique." );
    keyMsg.setFill( Color.RED );

    // Validate name field
    if (nameField.getText().trim().isEmpty()) {
      isValid = false;
      nameField.setStyle( cssRed );
      errorMsgBox.getChildren().add( nameMsg );
    } else {
      nameField.setStyle( "" );
    }

    // Check for duplicate keys:
    Map< String, List< TextField >> keyToField =
        mappingsBox.getChildren().stream()
                   .map( node -> ((MappingBox) node).keyField )
                   .collect( Collectors.groupingBy( keyField -> keyField.getText() ) );

    boolean foundDups = false;
    for (Entry< String, List< TextField >> entry : keyToField.entrySet()) {
      String k = entry.getKey();
      List< TextField > keyFields = entry.getValue();

      if (!k.isEmpty() && keyFields.size() > 1) {
        isValid = false;
        foundDups = true;
        keyFields.forEach( field -> field.setStyle( cssRed ) );
      } else {
        keyFields.forEach( field -> field.setStyle( "" ) );
      }
    }

    if (foundDups) {
      errorMsgBox.getChildren().add( keyMsg );
    }

    return isValid;
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

  /**
   * Run through the nameField and mappingsBox and update the 'model'. Then
   * persist the new model
   */
  @FXML void onSaveSchemaClicked( ActionEvent evt )
  {
    if (!validate()) {
      return;
    }

    HashMap< Character, KeyBehaviorMapping > temp = Maps.newHashMap();

    String name = nameField.getText().trim();
    List< KeyBehaviorMapping > keyBehaviors =
        mappingsBox.getChildren().stream()
                   .map( node -> ((MappingBox) node).translate() )
                   .filter( bhvr -> bhvr != null )
                   .collect( Collectors.toList() );

    for (KeyBehaviorMapping behavior : keyBehaviors) {
      temp.put( behavior.key, behavior );
    }

    model.name = name;
    model.mappings = temp;
    model.duration = getDuration();

    Schemas.save( model );

    EventRecorder.toSchemasView();
  }
}
