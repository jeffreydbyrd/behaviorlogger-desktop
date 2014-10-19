package com.threebird.recorder.controllers;

import java.io.IOException;
import java.util.Iterator;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.Schema;

/**
 * Controls the first view the user sees. All these member variables with the @FXML
 * annotation are physical objects I placed in Scene Builder and applied an
 * 'id'. The 'id' must match the variable name. Methods with an @FXML annotation
 * are triggered by events (again, specified in scene builder)
 */
public class SchemasController
{
  @FXML private Button plusSchemaButton;
  @FXML private Button minusSchemaButton;
  @FXML private ListView< Schema > schemaList;
  private ObservableList< Schema > schemas;

  @FXML private AnchorPane rightSide;
  @FXML private Text emptyMessage;
  @FXML private Text nameText;
  @FXML private TextField nameField;

  @FXML private VBox mappingsBox;
  @FXML private Button plusMappingButton;
  @FXML private Button saveButton;

  @FXML private TextField hoursField;
  @FXML private TextField minutesField;
  @FXML private TextField secondsField;

  @FXML private Button startButton;

  private static int defaultNumBoxes = 10;
  private static String digits = "0123456789";
  private static String acceptableKeys =
      "abcdefghijklmnopqrstuvwxyz1234567890`-=[]\\;',./";

  /**
   * When JavaFX initializes a controller, it will look for an 'initialize()'
   * method that's annotated with @FXML. If it finds one, it will call it as
   * soon as the class is instantiated
   */
  @FXML private void initialize()
  {
    initSchemas();
    initSchemaListView();

    // When I lose focus on the name-field, go on and submit changes
    nameField.focusedProperty().addListener( ( obsrvbl, old, isFocused ) -> {
      if (!isFocused) {
        onSchemaNameChange();
      }
    } );

    hoursField.setOnKeyTyped( createFieldLimiter( hoursField, digits, 2 ) );
    minutesField.setOnKeyTyped( createFieldLimiter( minutesField, digits, 2 ) );
    secondsField.setOnKeyTyped( createFieldLimiter( secondsField, digits, 2 ) );
  }

  private void initSchemas()
  {
    rightSide.setVisible( false );
    schemas = FXCollections.observableArrayList();
  }

  private void onSchemaNameChange()
  {
    Schema schema = schemaList.getSelectionModel().getSelectedItem();
    schema.name = nameField.getText().trim();
    nameText.setText( schema.name );
    nameField.setVisible( false );
    redraw( schema );
  }

  /**
   * Initializes 'schemaList' and binds it to 'schemas'
   */
  private void initSchemaListView()
  {
    schemaList.setItems( schemas );
    schemaList.setCellFactory( ( ListView< Schema > ls ) -> new ListCell< Schema >() {
      @Override protected void updateItem( Schema s, boolean bln )
      {
        super.updateItem( s, bln );
        setText( s != null ? s.name : "" );
      }
    } );

    schemaList.getSelectionModel()
              .selectedItemProperty()
              .addListener( this::onSchemaSelect );
  }

  /**
   * When user clicks "+" button: create SCHEMA, add to 'schemas' and select it
   */
  @FXML private void onPlusSchemaClicked( ActionEvent evt )
  {
    schemas.add( new Schema( "New Schema" ) );
    int i = schemas.size() - 1;
    schemaList.getSelectionModel().select( i );
    schemaList.getFocusModel().focus( i );
  }

  /**
   * When user clicks "-" button: remove selected SCHEMA from 'schemas'
   */
  @FXML private void onMinusSchemaClicked( ActionEvent evt )
  {
    if (schemas.isEmpty()) {
      return;
    }

    int i = schemaList.getSelectionModel().getSelectedIndex();
    schemas.remove( i );
  }

  /**
   * On schema-select: if the new value is null, hide right-hand-side.
   * Otherwise, populate right-hand-side with new schema's data
   */
  private void onSchemaSelect( ObservableValue< ? extends Schema > ov,
                               Schema oldV,
                               Schema newV )
  {
    if (newV != null) {
      nameText.setText( newV.name );
      rightSide.setVisible( true );
      emptyMessage.setVisible( false );
      populateMappingsBox( newV );
    } else {
      rightSide.setVisible( false );
      emptyMessage.setVisible( true );
    }
  }

  /**
   * When user clicks on the schema name, allow for edit
   */
  @FXML private void onNameClicked( MouseEvent evt )
  {
    Schema schema = schemaList.getSelectionModel().getSelectedItem();
    nameField.setVisible( true );
    nameField.setText( schema.name );
    nameField.requestFocus();
  }

  /**
   * When user hits enter while editing name field: save the new name to the
   * schema and force 'schemaList' to redraw
   */
  @FXML private void onNameFieldKeyPressed( KeyEvent evt )
  {
    if (evt.getCode().equals( KeyCode.ENTER )) {
      onSchemaNameChange();
    }
  }

  private static final Insets insets = new Insets( .5, .5, .5, .5 );

  /**
   * Adds 2 adjacent text fields to 'keyBox' and 'behaviorBox'. Attaches a
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

    mappingsBox.getChildren().add( new HBox( keyField, behaviorField ) );
  }

  /**
   * Runs through the key-behavior pairs in schema and populates the mappingsBox
   */
  private void populateMappingsBox( Schema schema )
  {
    mappingsBox.getChildren().clear();

    schema.mappings.forEach( ( key, mapping ) -> {
      addMappingBox( mapping.key.toString(), mapping.behavior );
    } );

    // add some extra boxes at the end to match 'defaultNumBoxes'
    for (int i = schema.mappings.size(); i < defaultNumBoxes; i++) {
      addMappingBox( "", "" );
    }
  }

  /**
   * When user clicks "+" under the key-behavior mappingsBox
   */
  @FXML private void onPlusMappingClicked( ActionEvent evt )
  {
    addMappingBox( "", "" );
  }

  /**
   * When user clicks "save", run through 'keyBox' and 'behaviorBox' and
   * construct new key-behavior mappings. Update the underlying schema with new
   * mappings
   */
  @FXML private void onSaveClicked( ActionEvent evt )
  {
    Schema schema = schemaList.getSelectionModel().getSelectedItem();
    schema.mappings.clear();
    ObservableList< Node > nodes =
        mappingsBox.getChildrenUnmodifiable();

    for (Node hbox : nodes) {
      Iterator< Node > it = ((HBox) hbox).getChildren().iterator();
      TextField keyField = (TextField) it.next();
      TextField behaviorField = (TextField) it.next();
      String key = keyField.getText().trim();
      String behavior = behaviorField.getText().trim();

      if (!key.isEmpty() && !behavior.isEmpty()) {
        schema.mappings.put( key.charAt( 0 ),
                             new KeyBehaviorMapping( key, behavior ) );
      }
    }
  }

  /**
   * There's no easy way (that I know of) to redraw the ListView. It will redraw
   * if you set an item in the underlying ObservableList.
   */
  private void redraw( Schema schema )
  {
    int i = schemaList.getSelectionModel().getSelectedIndex();
    schemas.set( i, schema );
    schemaList.getSelectionModel().select( i );
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

  /**
   * When user clicks "start", load up "recording.fxml" and switch scenes. Set
   * the schema and time limit for the recording. The "recording" view is
   * controlled by RecordingController.java
   */
  @FXML private void onStartClicked( ActionEvent evt ) throws IOException
  {
    Schema schema = schemaList.getSelectionModel().getSelectedItem();
    int duration = getDuration();
    EventRecorder.toRecordingView( schema, duration );
  }
}
