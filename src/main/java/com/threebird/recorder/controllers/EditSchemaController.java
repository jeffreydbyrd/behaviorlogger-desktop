package com.threebird.recorder.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;

import com.google.common.base.Optional;
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
import com.threebird.recorder.views.edit_schema.MappingBox;

/**
 * Corresponds to edit_schema.fxml. The researcher uses this to create or edit a selected schema
 */
public class EditSchemaController
{
  @FXML private TextField clientField;
  @FXML private TextField projectField;

  @FXML private VBox mappingsBox;
  @FXML private Button addRowButton;

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

  @FXML private Button deleteSchemaButton;

  protected static int defaultNumBoxes = 10;
  private static char[] digits = "0123456789".toCharArray();

  private Schema model;

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
    populateMappingsBox( model );
    projectField.setText( Strings.nullToEmpty( model.project ) );

    String dir =
        model.sessionDirectory == null
            ? PreferencesManager.getSessionDirectory()
            : model.sessionDirectory.getPath();

    directoryField.setText( dir );

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
   * Adds 2 adjacent text fields and a checkbox to 'mappingsBox'. Attaches a KeyTyped EventHandler to the first field
   * that prevents the user from typing more than 1 key
   */
  private void addMappingBox( Optional< KeyBehaviorMapping > kbm )
  {
    mappingsBox.getChildren().add( new MappingBox( kbm ) );
  }

  /**
   * Runs through the key-behavior pairs in schema and populates the mappingsBox
   */
  private void populateMappingsBox( Schema schema )
  {
    mappingsBox.getChildren().clear();

    schema.mappings.forEach( ( key, mapping ) -> {
      addMappingBox( Optional.fromNullable( mapping ) );
    } );
  }

  private static String cssRed = "-fx-background-color:#FFDDDD;-fx-border-color: #f00;";

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

    // Validate behavior mappings
    if (!validateBehaviors()) {
      isValid = false;
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
   * Validates the fields in the the behavior mapping box:
   * 
   * no duplicate keys, no duplicate names
   */
  private boolean validateBehaviors()
  {
    AtomicBoolean keysValid = new AtomicBoolean( true );
    AtomicBoolean namesValid = new AtomicBoolean( true );

    Text duplicateKeyMsg = new Text( "- Each key must be unique." );
    Text duplicateNameMsg = new Text( "- Each name must be unique." );
    duplicateKeyMsg.setFill( Color.RED );
    duplicateNameMsg.setFill( Color.RED );

    // Collect all the fields into a list that we can analyze better
    List< Pair< KeyBehaviorMapping, MappingBox > > behaviors =
        mappingsBox.getChildren().stream()
                   .map( node -> (MappingBox) node )
                   .map( box -> new Pair< KeyBehaviorMapping, MappingBox >( box.translate(), box ) )
                   .filter( pair -> pair.getKey() != null )
                   .collect( Collectors.toList() );

    // Check for duplicate keys
    Map< MappableChar, List< Pair< KeyBehaviorMapping, MappingBox > > > byKeys =
        behaviors.stream().collect( Collectors.groupingBy( pair -> pair.getKey().key ) );
    byKeys.forEach( ( key, list ) -> {
      if (list.size() > 1) {
        keysValid.set( false );
      }
      String style = list.size() > 1 ? cssRed : "";
      list.stream()
          .map( pair -> pair.getValue().keyField )
          .forEach( field -> field.setStyle( style ) );
    } );

    // Check for duplicate names
    Map< String, List< Pair< KeyBehaviorMapping, MappingBox > > > byName =
        behaviors.stream().collect( Collectors.groupingBy( pair -> pair.getKey().behavior ) );
    byName.forEach( ( key, list ) -> {
      if (list.size() > 1) {
        namesValid.set( false );
      }
      String style = list.size() > 1 ? cssRed : "";
      list.stream()
          .map( pair -> pair.getValue().behaviorField )
          .forEach( field -> field.setStyle( style ) );
    } );

    if (!keysValid.get()) {
      errorMsgBox.getChildren().add( duplicateKeyMsg );
    }

    if (!namesValid.get()) {
      errorMsgBox.getChildren().add( duplicateNameMsg );
    }

    return keysValid.get() && namesValid.get();
  }

  /**
   * When user clicks "Add Row" under the key-behavior mappingsBox
   */
  @FXML private void onAddRowClicked( ActionEvent evt )
  {
    addMappingBox( Optional.absent() );
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
