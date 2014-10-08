package com.threebird.recorder.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.Schema;

/**
 * Controls the first view the user sees
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

  @FXML private Button startButton;

  @FXML private void initialize()
  {
    initSchemas();
    initSchemaListView();
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
   * Initializes and popualtes 'schemas'.
   */
  private void initSchemas()
  {
    rightSide.setVisible( false );
    schemas = FXCollections.observableArrayList();
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
   * On SCHEMA-select: Update or hide right-hand side.
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
   * When user clicks on the SCHEMA name, allow for edit
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
   * SCHEMA and force 'schemaList' to redraw
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

  private void addMappingBox( String key, String behavior )
  {
    TextField keyBox = new TextField( key );
    keyBox.setOnKeyTyped( evt -> {
      if (keyBox.getText().trim().length() > 0
          || !acceptableKeys.contains( evt.getCharacter() ))
        evt.consume();
    } );
    TextField behaviorBox = new TextField( behavior );
    behaviorBox.setMinWidth( 265 );

    HBox slot = new HBox( 2 );
    slot.getChildren().add( keyBox );
    slot.getChildren().add( behaviorBox );

    this.mappingsBox.getChildren().add( slot );
  }

  private static int defaultNumBoxes = 6;

  private void populateMappingsBox( Schema schema )
  {
    mappingsBox.getChildren().clear();

    schema.mappings.forEach( ( ch, str ) -> addMappingBox( ch.toString(), str ) );

    for (int i = schema.mappings.size(); i < defaultNumBoxes; i++) {
      addMappingBox( "", "" );
    }
  }

  @FXML private void onPlusMappingClicked( ActionEvent evt )
  {
    addMappingBox( "", "" );
  }

  @FXML private void onSaveClicked( ActionEvent evt )
  {
    Schema schema = schemaList.getSelectionModel().getSelectedItem();
    HashMap< Character, String > temp = new HashMap< Character, String >();

    mappingsBox.getChildrenUnmodifiable().forEach( node -> {
      HBox box = (HBox) node;
      Iterator< Node > iter = box.getChildren().iterator();
      String key = ((TextField) iter.next()).getText();
      String behavior = ((TextField) iter.next()).getText();
      if (!key.trim().isEmpty()) {
        temp.put( key.charAt( 0 ), behavior );
      }
    } );

    HashSet< Character > removed = new HashSet< Character >();
    schema.mappings.keySet().forEach( c -> {
      if (!temp.containsKey( c )) {
        removed.add( c );
      }
    } );

    HashSet< Character > added = new HashSet< Character >();
    temp.keySet().forEach( c -> {
      if (!schema.mappings.containsKey( c )) {
        added.add( c );
      }
    } );

    removed.forEach( c -> schema.mappings.remove( c ) );
    added.forEach( c -> schema.mappings.put( c, temp.get( c ) ) );
  }

  private void redraw( Schema schema )
  {
    int i = schemaList.getSelectionModel().getSelectedIndex();
    schemas.set( i, schema );
    schemaList.getSelectionModel().select( i );
  }

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
