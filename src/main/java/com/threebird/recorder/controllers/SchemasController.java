package com.threebird.recorder.controllers;

import java.util.HashMap;
import java.util.Iterator;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
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
public class SchemasController extends AbstractEditSchemaController
{
  @FXML private Button createSchemaButton;
  @FXML private ListView< Schema > schemaList;

  @FXML private AnchorPane rightSide;
  @FXML private Text emptyMessage;
  @FXML private Text nameText;
  @FXML private TextField nameField;

  @FXML private Button saveButton;
  @FXML private Button startButton;

  private ObservableList< Schema > schemas;
  
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

  /**
   * This is fired when the user is focused on the 'nameField' and she presses
   * ENTER or she focuses on something else. It simply updates the selected
   * Schema and redraws the list-view
   */
  private void onSchemaNameChange()
  {
    Schema schema = schemaList.getSelectionModel().getSelectedItem();
    schema.name = nameField.getText().trim();
    nameText.setText( schema.name );
    nameField.setVisible( false );
    redrawListView( schema );
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
   * There's no easy way (that I know of) to redraw the ListView. It will redraw
   * if you set an item in the underlying ObservableList.
   */
  private void redrawListView( Schema schema )
  {
    int i = schemaList.getSelectionModel().getSelectedIndex();
    schemas.set( i, schema );
    schemaList.getSelectionModel().select( i );
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
   * When user clicks "Create Schema" button: create Schema, add to 'schemas'
   * and select it
   */
  @FXML private void onCreateSchemaClicked( ActionEvent evt )
  {
    EventRecorder.toCreateSchemaView();
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

  /**
   * When user clicks "save", run through 'keyBox' and 'behaviorBox' and
   * construct new key-behavior mappings. Update the underlying schema with new
   * mappings
   */
  @FXML private void onSaveClicked( ActionEvent evt )
  {
    Schema schema = schemaList.getSelectionModel().getSelectedItem();
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

    schema.mappings = temp;
    schema.duration = getDuration();
  }

  /**
   * When user clicks "start", load up "recording.fxml" and switch scenes. Set
   * the schema and time limit for the recording. The "recording" view is
   * controlled by RecordingController.java
   */
  @FXML private void onStartClicked( ActionEvent evt )
  {
    Schema schema = schemaList.getSelectionModel().getSelectedItem();
    EventRecorder.toRecordingView( schema );
  }
}
