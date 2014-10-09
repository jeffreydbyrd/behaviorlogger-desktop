package com.threebird.recorder.controllers;

import java.io.IOException;
import java.util.HashMap;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import com.threebird.recorder.EventRecorder;
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

  @FXML private VBox keyBox;
  @FXML private VBox behaviorBox;
  @FXML private Button plusMappingButton;
  @FXML private Button saveButton;

  @FXML private Button startButton;

  /**
   * When JavaFX initializes a controller, it will look for an 'initialize()'
   * method that's annotated with @FXML. If it finds one, it will call it as
   * soon as the class is instantiated
   */
  @FXML private void initialize()
  {
    initSchemas();
    initSchemaListView();
  }

  private void initSchemas()
  {
    rightSide.setVisible( false );
    schemas = FXCollections.observableArrayList();
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
      Schema schema = schemaList.getSelectionModel().getSelectedItem();
      schema.name = nameField.getText().trim();
      nameText.setText( schema.name );
      nameField.setVisible( false );
      redraw( schema );
    }
  }

  private static String acceptableKeys =
      "abcdefghijklmnopqrstuvwxyz1234567890`-=[]\\;',./";

  /**
   * Adds 2 adjacent text fields to 'keyBox' and 'behaviorBox'. Attaches a
   * KeyTyped EventHandler to the first field that prevents the user from typing
   * more than 1 key
   */
  private void addMappingBox( String key, String behavior )
  {
    TextField keyField = new TextField( key );
    keyField.setOnKeyTyped( evt -> {
      if (keyField.getText().trim().length() > 0
          || !acceptableKeys.contains( evt.getCharacter() ))
        evt.consume();
    } );
    TextField behaviorField = new TextField( behavior );
    behaviorField.setMinWidth( 265 );

    keyBox.getChildren().add( keyField );
    behaviorBox.getChildren().add( behaviorField );
  }

  private static int defaultNumBoxes = 6;

  /**
   * Runs through the key-behavior pairs in schema and populates the mappingsBox
   */
  private void populateMappingsBox( Schema schema )
  {
    keyBox.getChildren().clear();
    behaviorBox.getChildren().clear();

    schema.mappings.forEach( ( ch, str ) -> addMappingBox( ch.toString(), str ) );

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
   * construct new key-behavior mappings. Update the underlying schema with
   * newly added and removed keys
   */
  @FXML private void onSaveClicked( ActionEvent evt )
  {
    Schema schema = schemaList.getSelectionModel().getSelectedItem();
    HashMap< Character, String > mappings = new HashMap< Character, String >();
    
    ObservableList< Node > keyFields = keyBox.getChildrenUnmodifiable();
    ObservableList< Node > behaviorFields =
        behaviorBox.getChildrenUnmodifiable();

    for (int i = 0; i < keyFields.size(); i++) {
      TextField kField = (TextField) keyFields.get( i );
      TextField bField = (TextField) behaviorFields.get( i );
      if (!kField.getText().trim().isEmpty()) {
        mappings.put( kField.getText().charAt( 0 ), bField.getText() );
      }
    }

    schema.mappings = mappings;
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

  /**
   * When user clicks "start", load up "recording.fxml" and switch scenes. The
   * "recording" view is controlled by RecordingController.java
   */
  @FXML private void onStartClicked( ActionEvent evt ) throws IOException
  {
    RecordingController.SCHEMA =
        schemaList.getSelectionModel().getSelectedItem();

    Parent root =
        FXMLLoader.load( EventRecorder.class.getResource( "./views/recording.fxml" ) );

    Scene scene = new Scene( root );

    EventRecorder.STAGE.setTitle( "Recording" );
    EventRecorder.STAGE.setScene( scene );
  }
}
