package com.threebird.recorder.controllers;

import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.Schema;
import com.threebird.recorder.persistence.Schemas;
import com.threebird.recorder.views.TimeBox;

/**
 * Controls the first view the user sees. All these member variables with the @FXML
 * annotation are physical objects I placed in Scene Builder and applied an
 * 'id'. The 'id' must match the variable name. Methods with an @FXML annotation
 * are triggered by events (again, specified in scene builder)
 */
public class SchemasController
{
  @FXML private TableView< Schema > schemaTable;
  @FXML private TableColumn< Schema, String > clientCol;
  @FXML private TableColumn< Schema, String > projectCol;
  @FXML private Button createSchemaButton;

  @FXML private AnchorPane rightSide;
  @FXML private Text emptyMessage;

  @FXML private VBox mappingsBox;

  @FXML private Pane timeBoxSlot;
  private TimeBox timeBox;

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
    rightSide.setVisible( false );
    initSchemaListView();

    clientCol.setCellValueFactory( p -> new SimpleStringProperty( p.getValue().client ) );
    projectCol.setCellValueFactory( p -> new SimpleStringProperty( p.getValue().project ) );

    timeBox = new TimeBox( 0 );
    timeBoxSlot.getChildren().add( timeBox );
  }

  /**
   * Initializes 'schemaList' and binds it to 'schemas'
   */
  private void initSchemaListView()
  {
    List< Schema > all = Schemas.all();
    schemas = FXCollections.observableArrayList( all );

    schemaTable.setItems( schemas );

    schemaTable.getSelectionModel()
               .selectedItemProperty()
               .addListener( this::onSchemaSelect );
  }

  /**
   * Populates the 'mappingBox' with the currently selected Schema's
   * key-behavior mappings
   */
  private void populateMappingsTable( Schema schema )
  {
    mappingsBox.getChildren().clear();

    schema.mappings.values().forEach( mapping -> {
      Label contLbl = new Label();
      contLbl.setText( mapping.isContinuous ? "(cont.)" : "" );
      contLbl.setMinWidth( 45 );

      Label keyLbl = new Label( mapping.key.toString() );
      keyLbl.setMinWidth( 15 );

      Label separator = new Label( ":" );

      Label behaviorLbl = new Label( mapping.behavior );
      behaviorLbl.setWrapText( true );

      HBox hbox = new HBox( contLbl, keyLbl, separator, behaviorLbl );
      hbox.setSpacing( 5 );
      mappingsBox.getChildren().add( hbox );
    } );
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
      rightSide.setVisible( true );
      emptyMessage.setVisible( false );
      populateMappingsTable( newV );
      timeBox.setTime( newV.duration );
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
    EventRecorder.toEditSchemaView( null );
  }

  @FXML private void onEditSchemaClicked( ActionEvent evt )
  {
    Schema schema = schemaTable.getSelectionModel().getSelectedItem();
    EventRecorder.toEditSchemaView( schema );
  }

  /**
   * When user clicks "start", load up "recording.fxml" and switch scenes. Set
   * the schema and time limit for the recording. The "recording" view is
   * controlled by RecordingController.java
   */
  @FXML private void onStartClicked( ActionEvent evt )
  {
    Schema schema = schemaTable.getSelectionModel().getSelectedItem();
    EventRecorder.toRecordingView( schema );
  }
}
