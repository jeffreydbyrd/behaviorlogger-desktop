package com.threebird.recorder.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.preferences.FilenameComponent;
import com.threebird.recorder.models.preferences.PreferencesManager;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.models.schemas.Schema;
import com.threebird.recorder.models.schemas.SchemasManager;
import com.threebird.recorder.persistence.Schemas;
import com.threebird.recorder.utils.Alerts;
import com.threebird.recorder.utils.EventRecorderUtil;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Corresponds to edit_schema.fxml. The researcher uses this to create or edit a selected schema
 */
public class EditSchemaController
{
  /**
   * Used for keeping track of and editing the mappings on the page without modifying the underlying schema (until the
   * user clicks "save")
   */
  private static class MutableMapping
  {
    String uuid;
    MappableChar key;
    String behavior;
    boolean isContinuous;

    public MutableMapping( KeyBehaviorMapping kbm )
    {
      this.uuid = kbm.uuid;
      this.key = kbm.key;
      this.behavior = kbm.behavior;
      this.isContinuous = kbm.isContinuous;
    }
  }

  @FXML private TextField clientField;
  @FXML private TextField projectField;

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

  @FXML private TableView< MutableMapping > behaviorTable;
  @FXML private TableColumn< MutableMapping, Boolean > contCol;
  @FXML private TableColumn< MutableMapping, String > keyCol;
  @FXML private TableColumn< MutableMapping, String > descriptionCol;
  @FXML private TableColumn< MutableMapping, String > actionCol;

  @FXML private TextField keyField;
  @FXML private TextField descriptionField;
  @FXML private CheckBox contCheckbox;
  @FXML private Button addButton;
  @FXML private Text mappingErrorText;

  @FXML private VBox errorMsgBox;

  @FXML private Button deleteSchemaButton;

  protected static int defaultNumBoxes = 10;
  private static char[] digits = "0123456789".toCharArray();
  private static String cssRed = "-fx-background-color:#FFDDDD;-fx-border-color: #f00;";

  private static char[] acceptableKeys;
  static {
    MappableChar[] values = MappableChar.values();
    acceptableKeys = new char[values.length];
    for (int i = 0; i < values.length; i++) {
      MappableChar mappableChar = values[i];
      acceptableKeys[i] = mappableChar.c;
    }
  }

  private Schema model;
  private ObservableList< MutableMapping > mappings;

  /**
   * Sets the stage to the EditScema view.
   * 
   * @param schema
   *          - the currently selected schema if editing, or null if creating a new schema
   */
  public static void toEditSchemaView( Schema selected )
  {
    String filepath = "views/edit_schema/edit-schema.fxml";
    EditSchemaController controller = EventRecorderUtil.loadScene( filepath, "Create Schema" );
    controller.init( selected );
  }

  /**
   * @param sch
   *          - The Schema being edited. If null, a new schema is created
   */
  private void init( Schema selected )
  {
    deleteSchemaButton.setVisible( selected != null );
    model = selected == null ? new Schema() : selected;

    clientField.setText( Strings.nullToEmpty( model.client ) );
    projectField.setText( Strings.nullToEmpty( model.project ) );

    String dir =
        model.sessionDirectory == null
            ? PreferencesManager.getSessionDirectory()
            : model.sessionDirectory.getPath();

    directoryField.setText( dir );

    setupDurationRadioButtons();
    setupDurationTextFields();

    initBehaviorSection( model );

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

    int duration = model.duration == null ? PreferencesManager.getDuration() : model.duration;

    infiniteRadioBtn.setSelected( duration == 0 );
    timedRadioBtn.setSelected( duration != 0 );
    durationBox.setDisable( duration == 0 );
  }

  private void setupDurationTextFields()
  {
    int duration = model.duration == null ? PreferencesManager.getDuration() : model.duration;

    int hrs = duration / (60 * 60);
    int minDivisor = duration % (60 * 60);
    int mins = minDivisor / 60;
    int secs = minDivisor % 60;

    hoursField.setText( EventRecorderUtil.intToStr( hrs ) );
    minutesField.setText( EventRecorderUtil.intToStr( mins ) );
    secondsField.setText( EventRecorderUtil.intToStr( secs ) );

    hoursField.setOnKeyTyped( EventRecorderUtil.createFieldLimiter( hoursField, digits, 2 ) );
    minutesField.setOnKeyTyped( EventRecorderUtil.createFieldLimiter( minutesField, digits, 2 ) );
    secondsField.setOnKeyTyped( EventRecorderUtil.createFieldLimiter( secondsField, digits, 2 ) );

    boolean color = model.color == null ? PreferencesManager.getColorOnEnd() : model.color;
    boolean pause = model.pause == null ? PreferencesManager.getPauseOnEnd() : model.pause;
    boolean sound = model.sound == null ? PreferencesManager.getSoundOnEnd() : model.sound;

    colorCheckBox.setSelected( color );
    pauseCheckBox.setSelected( pause );
    beepCheckBox.setSelected( sound );
  }

  private File getDirectory()
  {
    return new File( directoryField.getText().trim() );
  }

  /**
   * Configures {@link #behaviorTable} to represent the schema's list of KeyBehaviorsMappings
   */
  private void initBehaviorSection( Schema schema )
  {
    // Setup the Behavior Table
    mappings = FXCollections.observableArrayList( schema.mappings.values().stream().map( MutableMapping::new )
                                                                 .collect( Collectors.toList() ) );
    contCol.setCellValueFactory( p -> {
      SimpleBooleanProperty property = new SimpleBooleanProperty( p.getValue().isContinuous );
      property.addListener( ( observable, oldValue, newValue ) -> p.getValue().isContinuous = newValue );
      return property;
    } );
    contCol.setCellFactory( CheckBoxTableCell.forTableColumn( contCol ) );

    keyCol.setCellValueFactory( p -> new SimpleStringProperty( p.getValue().key.c + "" ) );
    keyCol.setCellFactory( TextFieldTableCell.forTableColumn() );
    keyCol.setOnEditCommit( new EventHandler< TableColumn.CellEditEvent< MutableMapping, String > >() {
      @Override public void handle( CellEditEvent< MutableMapping, String > evt )
      {
        MutableMapping mm = evt.getRowValue();
        String newValue = evt.getNewValue();

        if (newValue.length() != 1) {
          evt.consume();
          mappingErrorText.setText( "Must be one character." );
        } else if (!validateBehaviorKey( newValue )) {
          
        } else {
          MappableChar.getForString( newValue ).map( c -> mm.key = c );
        }

        // A stupid hack to re-render the column because the cell doesn't get updated properly
        evt.getTableColumn().setVisible( false );
        evt.getTableColumn().setVisible( true );
      }
    } );

    descriptionCol.setCellValueFactory( p -> new SimpleStringProperty( p.getValue().behavior ) );

    behaviorTable.setItems( mappings );

    // Setup the Add-Behavior widget
    // Prevent user from duplicating keys and limit to 1 character
    EventHandler< ? super KeyEvent > limitText = EventRecorderUtil.createFieldLimiter( keyField, acceptableKeys, 1 );
    keyField.setOnKeyTyped( evt -> {
      limitText.handle( evt );
      if (evt.isConsumed()) {
        return;
      }

      String ch = evt.getCharacter();
      if (!validateBehaviorKey( ch )) {
        evt.consume();
      }
    } );

    // If user hits ENTER, then click "Add" button
    EventHandler< ? super KeyEvent > onEnter = evt -> {
      if (evt.getCode().equals( KeyCode.ENTER )) {
        onAddClicked();
      }
    };
    keyField.setOnKeyPressed( onEnter );
    descriptionField.setOnKeyPressed( onEnter );
    contCheckbox.setOnKeyPressed( onEnter );
    addButton.setOnKeyPressed( onEnter );
  }

  private boolean validateBehaviorKey( String ch )
  {
    boolean isTaken = mappings.stream().anyMatch( kbm -> ch.equals( kbm.key.c + "" ) );
    
    if (isTaken) {
      mappingErrorText.setText( "That key is already taken." );
    } else {
      mappingErrorText.setText( "" );
    }

    return !isTaken;
  }

  /**
   * Runs through the scene and makes sure the nameField and projectField are filled in (if required), and that there
   * are no duplicate keys. Any validation errors will get highlighted red, and a message gets put in the 'errorMsgBox'
   * 
   * @return true if all inputs are valid, or false if a single input fails
   */
  private boolean validate()
  {
    boolean isValid = true;
    errorMsgBox.getChildren().clear();

    // Make some red error messages:
    Text dirMsg = new Text( "- That folder does not exist." );
    Text clientProjectMsg = new Text( "- You must fill either Client or Project (or both)." );
    Text duplicateKeyMsg = new Text( "- Each key must be unique." );
    Text duplicateNameMsg = new Text( "- Each name must be unique." );
    Text emptyKeyMsg = new Text( "- You must have at least one key mapped." );
    Lists.newArrayList( dirMsg, clientProjectMsg, duplicateKeyMsg, duplicateNameMsg, emptyKeyMsg )
         .forEach( txt -> txt.setFill( Color.RED ) );

    // Verify that all the required fields for the file-name are filled
    ImmutableMap< String, FilenameComponent > displayToComp =
        Maps.uniqueIndex( PreferencesManager.filenameComponents(), c -> c.name );

    Map< FilenameComponent, TextField > compToField = Maps.newHashMap();
    compToField.put( displayToComp.get( "Client" ), clientField );
    compToField.put( displayToComp.get( "Project" ), projectField );

    for (Entry< FilenameComponent, TextField > entry : compToField.entrySet()) {
      FilenameComponent comp = entry.getKey();
      TextField field = entry.getValue();
      if (comp.enabled && Strings.isNullOrEmpty( field.getText() )) {
        field.setStyle( cssRed );
        Label lbl = new Label( "- " + comp.name + " is required for your data file's name." );
        lbl.setTextFill( Color.RED );
        errorMsgBox.getChildren().add( lbl );
        isValid = false;
      } else {
        field.setStyle( "" );
      }
    }

    // Validate Directory field
    if (!getDirectory().exists()) {
      isValid = false;
      directoryField.setStyle( cssRed );
      errorMsgBox.getChildren().add( dirMsg );
    } else {
      directoryField.setStyle( "" );
    }

    // Make sure either the Client or Project field are filled
    if (Strings.isNullOrEmpty( clientField.getText() ) && Strings.isNullOrEmpty( projectField.getText() )) {
      isValid = false;
      errorMsgBox.getChildren().add( clientProjectMsg );
      clientField.setStyle( cssRed );
      projectField.setStyle( cssRed );
    } else {
      clientField.setStyle( "" );
      projectField.setStyle( "" );
    }

    return isValid;
  }

  /**
   * When user clicks the "Add" button
   */
  @FXML private void onAddClicked()
  {
    // Check if key is empty
    if (keyField.getText().isEmpty()) {
      keyField.setStyle( cssRed );
      mappingErrorText.setText( "Key is required." );
      return;
    } else {
      keyField.setStyle( "" );
    }

    // Check if description is empty
    if (descriptionField.getText().isEmpty()) {
      descriptionField.setStyle( cssRed );
      mappingErrorText.setText( "Behavior description is required." );
      return;
    } else {
      descriptionField.setStyle( "" );
    }

    String uuid = UUID.randomUUID().toString();
    String key = keyField.getText();
    String behavior = descriptionField.getText();
    boolean isCont = contCheckbox.isSelected();
    KeyBehaviorMapping newMapping = new KeyBehaviorMapping( uuid, key, behavior, isCont );
    mappings.add( new MutableMapping( newMapping ) );

    mappingErrorText.setText( "" );
    keyField.setText( "" );
    descriptionField.setText( "" );
    contCheckbox.setSelected( false );

    keyField.requestFocus();
  }

  @FXML void onBrowseButtonPressed( ActionEvent evt )
  {
    EventRecorderUtil.chooseFile( EventRecorder.STAGE, directoryField );
  }

  /**
   * Simply bring the user back to the Schemas view without saving
   */
  @FXML void onCancelClicked( ActionEvent evt )
  {
    StartMenuController.toStartMenuView();
  }

  /**
   * Run through the nameField and mappingsBox and update the 'model'. Then persist the new model
   */
  @FXML void onSaveSchemaClicked( ActionEvent evt )
  {
    if (!validate()) {
      return;
    }

    HashMap< MappableChar, KeyBehaviorMapping > temp = Maps.newHashMap();
    for (MutableMapping m : mappings) {
      temp.put( m.key, new KeyBehaviorMapping( m.uuid, m.key, m.behavior, m.isContinuous ) );
    }

    model.client = clientField.getText().trim();
    model.project = projectField.getText().trim();
    model.mappings = temp;
    model.sessionDirectory = getDirectory();
    model.duration =
        EventRecorderUtil.getDuration( infiniteRadioBtn.isSelected(), hoursField, minutesField, secondsField );
    model.color = colorCheckBox.isSelected();
    model.pause = pauseCheckBox.isSelected();
    model.sound = beepCheckBox.isSelected();

    if (model.uuid == null) {
      SchemasManager.schemas().add( model );
    } else {
      try {
        Schemas.update( model );
      } catch (Exception e) {
        e.printStackTrace();
        Alerts.error( "Error saving Schema", "There was a problem while saving your schema.", e );
      }
    }

    StartMenuController.toStartMenuView();
  }

  /**
   * When the user clicks delete, display a confirmation box and only delete Schema if they click "yes"
   */
  @FXML void onDeleteSchemaClicked( ActionEvent actionEvent )
  {
    String msg = "Are you sure you want to delete this schema?\nYou can't undo this action.";

    Alerts.confirm( "Confirm deletion", null, msg, () -> {
      SchemasManager.schemas().remove( model );
      StartMenuController.toStartMenuView();
    } );
  }

  @FXML void onHelpBtnPressed()
  {
    EventRecorderUtil.openManual( "edit-schema" );
  }
}
