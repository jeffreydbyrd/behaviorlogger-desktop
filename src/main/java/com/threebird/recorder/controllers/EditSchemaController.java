package com.threebird.recorder.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.Schema;
import com.threebird.recorder.persistence.Schemas;
import com.threebird.recorder.views.edit_schema.MappingBox;

/**
 * Corresponds to edit_schema.fxml. The researcher uses this to create or edit a
 * selected schema
 */
public class EditSchemaController
{
  @FXML private TextField clientField;
  @FXML private TextField projectField;

  @FXML private VBox mappingsBox;
  @FXML private Button addRowButton;
  @FXML private Button deleteSchemaButton;

  @FXML private TextField directoryField;
  @FXML private Button browseButton;

  @FXML private RadioButton infiniteRadioBtn;
  @FXML private RadioButton timedRadioBtn;

  @FXML private VBox durationBox;

  @FXML private TextField hoursField;
  @FXML private TextField minutesField;
  @FXML private TextField secondsField;

  @FXML private CheckBox colorCheckBox;
  @FXML private CheckBox pauseCheckBox;
  @FXML private CheckBox beepCheckBox;

  @FXML private VBox errorMsgBox;

  protected static int defaultNumBoxes = 10;
  private static char[] digits = "0123456789".toCharArray();

  private Schema model;

  /**
   * @param sch
   *          - The Schema being edited. If null, a new schema is created
   */
  public void init( Schema sch )
  {
    deleteSchemaButton.setVisible( sch != null );
    model = sch == null ? new Schema() : sch;

    clientField.setText( Strings.nullToEmpty( model.client ) );
    populateMappingsBox( model );
    projectField.setText( Strings.nullToEmpty( model.project ) );
    directoryField.setText( model.sessionDirectory.getPath() );
    setupDurationRadioButtons();
    setupDurationTextFields();

    clientField.requestFocus();
  }

  private void setupDurationRadioButtons()
  {
    ToggleGroup group = new ToggleGroup();
    infiniteRadioBtn.setToggleGroup( group );
    timedRadioBtn.setToggleGroup( group );
    timedRadioBtn.selectedProperty().addListener( ( observable,
                                                    oldValue,
                                                    selected ) -> {
      durationBox.setDisable( !selected );
    } );

    infiniteRadioBtn.setSelected( model.duration == 0 );
    timedRadioBtn.setSelected( model.duration != 0 );
    durationBox.setDisable( model.duration == 0 );
  }

  private void setupDurationTextFields()
  {
    int hrs = model.duration / (60 * 60);
    int minDivisor = model.duration % (60 * 60);
    int mins = minDivisor / 60;
    int secs = minDivisor % 60;

    hoursField.setText( intToStr( hrs ) );
    minutesField.setText( intToStr( mins ) );
    secondsField.setText( intToStr( secs ) );

    hoursField.setOnKeyTyped( createFieldLimiter( hoursField, digits, 2 ) );
    minutesField.setOnKeyTyped( createFieldLimiter( minutesField, digits, 2 ) );
    secondsField.setOnKeyTyped( createFieldLimiter( secondsField, digits, 2 ) );

    colorCheckBox.setSelected( model.color );
    pauseCheckBox.setSelected( model.pause );
    beepCheckBox.setSelected( model.sound );
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
   * the equivalent number of seconds. If "infinite" is true, then returns 0
   */
  private int getDuration()
  {
    if (infiniteRadioBtn.isSelected()) {
      return 0;
    }

    Integer hours = strToInt( hoursField.getText() );
    Integer mins = strToInt( minutesField.getText() );
    Integer secs = strToInt( secondsField.getText() );
    return (hours * 60 * 60) + (mins * 60) + secs;
  }

  private File getDirectory()
  {
    return new File( directoryField.getText().trim() );
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
    createFieldLimiter( TextField field, char[] acceptableKeys, int limit )
  {
    return evt -> {
      if (field.getText().trim().length() == limit
          || !String.valueOf( acceptableKeys ).contains( evt.getCharacter() ))
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
    Text dirMsg = new Text( "That folder does not exist." );
    dirMsg.setFill( Color.RED );
    Text keyMsg = new Text( "You must have at least one key mapped." );
    keyMsg.setFill( Color.RED );
    Text duplicateMsg = new Text( "Each key must be unique." );
    duplicateMsg.setFill( Color.RED );

    // Validate name field
    if (clientField.getText().trim().isEmpty()) {
      isValid = false;
      clientField.setStyle( cssRed );
      errorMsgBox.getChildren().add( nameMsg );
    } else {
      clientField.setStyle( "" );
    }

    // Validate Directory field
    if (!getDirectory().exists()) {
      isValid = false;
      directoryField.setStyle( cssRed );
      errorMsgBox.getChildren().add( dirMsg );
    } else {
      directoryField.setStyle( "" );
    }

    // Collect all the key-fields into an easier collection
    Map< String, List< TextField >> keyToField =
        mappingsBox.getChildren().stream()
                   .map( node -> ((MappingBox) node).keyField )
                   .filter( textField -> !textField.getText().isEmpty() )
                   .collect( Collectors.groupingBy( keyField -> keyField.getText() ) );

    // make sure at least one field was non-empty
    if (keyToField.isEmpty()) {
      errorMsgBox.getChildren().add( keyMsg );
      return false;
    }

    // check for duplicates
    boolean foundDups = false;
    for (Entry< String, List< TextField >> entry : keyToField.entrySet()) {
      List< TextField > keyFields = entry.getValue();

      if (keyFields.size() > 1) {
        foundDups = true;
        keyFields.forEach( field -> field.setStyle( cssRed ) );
      } else {
        keyFields.forEach( field -> field.setStyle( "" ) );
      }
    }

    if (foundDups) {
      isValid = false;
      errorMsgBox.getChildren().add( duplicateMsg );
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

  @FXML void onBrowseButtonPressed( ActionEvent evt )
  {
    File f = getDirectory();
    if (!f.exists()) {
      f = new File( System.getProperty( "user.home" ) );
    }

    DirectoryChooser dirChooser = new DirectoryChooser();
    dirChooser.setInitialDirectory( f );
    File newFile = dirChooser.showDialog( EventRecorder.STAGE );

    if (newFile != null) {
      directoryField.setText( newFile.getPath() );
    }
  }

  /**
   * Simply bring the user back to the Schemas view without saving
   */
  @FXML void onCancelClicked( ActionEvent evt )
  {
    EventRecorder.toSchemasView( model );
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

    HashMap< MappableChar, KeyBehaviorMapping > temp = Maps.newHashMap();

    List< KeyBehaviorMapping > keyBehaviors =
        mappingsBox.getChildren().stream()
                   .map( node -> ((MappingBox) node).translate() )
                   .filter( bhvr -> bhvr != null )
                   .collect( Collectors.toList() );

    for (KeyBehaviorMapping behavior : keyBehaviors) {
      temp.put( behavior.key, behavior );
    }

    model.client = clientField.getText().trim();
    model.project = projectField.getText().trim();
    model.mappings = temp;
    model.sessionDirectory = getDirectory();
    model.duration = getDuration();
    model.color = colorCheckBox.isSelected();
    model.pause = pauseCheckBox.isSelected();
    model.sound = beepCheckBox.isSelected();

    Schemas.save( model );

    EventRecorder.toSchemasView( model );
  }

  /**
   * When the user clicks delete, display a confirmation box and only delete
   * Schema if they click "yes"
   */
  @FXML void onDeleteSchemaClicked( ActionEvent actionEvent )
  {
    String msg = "Are you sure you want to delete this schema?\nYou can't undo this action.";

    EventHandler< ActionEvent > onDeleteClicked = evt -> {
      Schemas.delete( this.model );
      EventRecorder.toSchemasView( null );
    };

    EventRecorder.dialogBox( msg,
                             "Cancel",
                             "Delete",
                             e -> {},
                             onDeleteClicked );
  }
}
