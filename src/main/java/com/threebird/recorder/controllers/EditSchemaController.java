package com.threebird.recorder.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.threebird.recorder.BehaviorLoggerApp;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.preferences.FilenameComponent;
import com.threebird.recorder.models.preferences.PreferencesManager;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.models.schemas.Schema;
import com.threebird.recorder.models.schemas.SchemasManager;
import com.threebird.recorder.persistence.SessionDirectories;
import com.threebird.recorder.utils.Alerts;
import com.threebird.recorder.utils.BehaviorLoggerUtil;
import com.threebird.recorder.views.edit_schema.BehaviorBox;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
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
  private static final EventHandler< ? super KeyEvent > LIMIT_FIELD_100 = BehaviorLoggerUtil.createFieldLimiter( 100 );
  private static EventHandler< ? super KeyEvent > LIMIT_FIELD_1 =
      BehaviorLoggerUtil.createFieldLimiter( MappableChar.acceptableKeys(), 1 );
  private static char[] DIGITS = "0123456789".toCharArray();
  private static String CSS_RED = "-fx-background-color:#FFDDDD;-fx-border-color: #f00;";

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

  @FXML private TextField keyField;
  @FXML private TextField descriptionField;
  @FXML private CheckBox contCheckbox;
  @FXML private Button addButton;
  @FXML private Text mappingErrorText;

  @FXML private VBox discreteBoxes;
  @FXML private VBox continuousBoxes;
  @FXML private VBox errorMsgBox;

  @FXML private Button deleteSchemaButton;

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
    EditSchemaController controller = BehaviorLoggerUtil.loadScene( filepath, "Create Schema" );
    controller.init( selected );
  }

  /**
   * @param sch
   *          - The Schema being edited. If null, a new schema is created
   */
  private void init( Schema selected )
  {
    deleteSchemaButton.setVisible( selected != null );
    if (selected == null) {
      model = new Schema();
      model.archived = false;
    } else {
      model = selected;
    }

    clientField.setText( Strings.nullToEmpty( model.client ) );
    projectField.setText( Strings.nullToEmpty( model.project ) );

    EventHandler< ? super KeyEvent > limitLength100 = LIMIT_FIELD_100;
    clientField.setOnKeyTyped( limitLength100 );
    projectField.setOnKeyTyped( limitLength100 );

    String dir = SessionDirectories.getForSchemaIdOrDefault( model.uuid ).getPath();
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
    duration = duration / 1000;

    int hrs = duration / (60 * 60);
    int minDivisor = duration % (60 * 60);
    int mins = minDivisor / 60;
    int secs = minDivisor % 60;

    hoursField.setText( BehaviorLoggerUtil.intToStr( hrs ) );
    minutesField.setText( BehaviorLoggerUtil.intToStr( mins ) );
    secondsField.setText( BehaviorLoggerUtil.intToStr( secs ) );

    EventHandler< ? super KeyEvent > limit2Digits = BehaviorLoggerUtil.createFieldLimiter( DIGITS, 2 );
    hoursField.setOnKeyTyped( limit2Digits );
    minutesField.setOnKeyTyped( limit2Digits );
    secondsField.setOnKeyTyped( limit2Digits );

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
    // Add BehaviorBoxes for existing behavior-mappings
    for (KeyBehaviorMapping kbm : schema.behaviors.values()) {
      BehaviorBox bb = new BehaviorBox( kbm.uuid, kbm.isContinuous, kbm.key, kbm.description, this::getBehaviorBoxes );
      if (kbm.isContinuous) {
        this.continuousBoxes.getChildren().add( bb );
      } else {
        this.discreteBoxes.getChildren().add( bb );
      }
    }

    // Setup the Add-Behavior widget
    // Prevent user from duplicating keys and limit to 1 character
    keyField.setOnKeyTyped( this::newBehaviorKeyOnType );

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

    descriptionField.setOnKeyTyped( LIMIT_FIELD_100 );
  }

  private void newBehaviorKeyOnType( KeyEvent evt )
  {
    String text = keyField.getText() + evt.getCharacter();

    // limit to 1 char
    LIMIT_FIELD_1.handle( evt );
    if (evt.isConsumed() || text.length() != 1) {
      return;
    }

    Optional< MappableChar > optChar = MappableChar.getForString( evt.getCharacter() );
    if (!optChar.isPresent()) {
      evt.consume();
      return;
    }

    MappableChar c = optChar.get();
    boolean isTakenD = discreteBoxes.getChildren()
                                    .stream()
                                    .map( node -> (BehaviorBox) node )
                                    .anyMatch( bbox -> !bbox.isDeleted() && bbox.getKey().equals( c ) );
    boolean isTakenC = continuousBoxes.getChildren()
                                      .stream()
                                      .map( node -> (BehaviorBox) node )
                                      .anyMatch( bbox -> !bbox.isDeleted() && bbox.getKey().equals( c ) );
    boolean isTaken = isTakenD || isTakenC;

    if (isTaken) {
      evt.consume();
      mappingErrorText.setText( "That key is already taken." );
    } else {
      mappingErrorText.setText( "" );
    }
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
        field.setStyle( CSS_RED );
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
      directoryField.setStyle( CSS_RED );
      errorMsgBox.getChildren().add( dirMsg );
    } else {
      directoryField.setStyle( "" );
    }

    // Make sure either the Client or Project field are filled
    if (Strings.isNullOrEmpty( clientField.getText() ) && Strings.isNullOrEmpty( projectField.getText() )) {
      isValid = false;
      errorMsgBox.getChildren().add( clientProjectMsg );
      clientField.setStyle( CSS_RED );
      projectField.setStyle( CSS_RED );
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
      keyField.setStyle( CSS_RED );
      mappingErrorText.setText( "Key is required." );
      return;
    } else {
      keyField.setStyle( "" );
    }

    // Check if description is empty
    if (descriptionField.getText().isEmpty()) {
      descriptionField.setStyle( CSS_RED );
      mappingErrorText.setText( "Behavior description is required." );
      return;
    } else {
      descriptionField.setStyle( "" );
    }

    String uuid = UUID.randomUUID().toString();
    MappableChar key = MappableChar.getForString( keyField.getText() ).get(); // This shouldn't be empty
    String behavior = descriptionField.getText();
    boolean isCont = contCheckbox.isSelected();

    BehaviorBox bb = new BehaviorBox( uuid, isCont, key, behavior, this::getBehaviorBoxes );
    if (contCheckbox.isSelected()) {
      this.continuousBoxes.getChildren().add( bb );
    } else {
      this.discreteBoxes.getChildren().add( bb );
    }

    mappingErrorText.setText( "" );
    keyField.setText( "" );
    descriptionField.setText( "" );
    contCheckbox.setSelected( false );

    keyField.requestFocus();
  }

  private List< BehaviorBox > getBehaviorBoxes()
  {
    List< BehaviorBox > discretes =
        discreteBoxes.getChildrenUnmodifiable().stream().map( n -> (BehaviorBox) n ).collect( Collectors.toList() );
    List< BehaviorBox > continuous =
        continuousBoxes.getChildrenUnmodifiable().stream().map( n -> (BehaviorBox) n ).collect( Collectors.toList() );
    discretes.addAll( continuous );

    return discretes;
  }

  @FXML void onBrowseButtonPressed( ActionEvent evt )
  {
    BehaviorLoggerUtil.chooseFile( BehaviorLoggerApp.STAGE, directoryField );
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
    ArrayList< Node > behaviorBoxes = Lists.newArrayList( discreteBoxes.getChildren() );
    behaviorBoxes.addAll( continuousBoxes.getChildren() );
    for (Node node : behaviorBoxes) {
      BehaviorBox bb = (BehaviorBox) node;
      if (bb.isDeleted()) {
        continue;
      }
      MappableChar key = bb.getKey();
      String description = bb.getDescription();
      boolean isContinuous = bb.isContinuous();
      String uuid = bb.uuid;
      temp.put( key, new KeyBehaviorMapping( uuid, key, description, isContinuous ) );
    }

    model.client = clientField.getText().trim();
    model.project = projectField.getText().trim();
    model.behaviors = temp;
    model.duration =
        BehaviorLoggerUtil.getDurationInMillis( infiniteRadioBtn.isSelected(), hoursField, minutesField, secondsField );
    model.color = colorCheckBox.isSelected();
    model.pause = pauseCheckBox.isSelected();
    model.sound = beepCheckBox.isSelected();

    try {
      if (model.uuid == null) {
        SchemasManager.create( model );
        SessionDirectories.create( model.uuid, getDirectory() );
        SchemasManager.setSelected( model );
      } else {
        SchemasManager.update( model );
        SessionDirectories.update( model.uuid, getDirectory() );
      }
    } catch (Exception e) {
      e.printStackTrace();
      Alerts.error( "Error saving Schema", "There was a problem while saving your schema.", e );
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
      model.archived = true;
      try {
        SchemasManager.update( model );
      } catch (Exception e) {
        Alerts.error( "Failed to delete schema", "There was a problem while trying to delete the schema", e );
        e.printStackTrace();
      }
      SchemasManager.setSelected( null );
      StartMenuController.toStartMenuView();
    } );
  }

  @FXML void onHelpBtnPressed()
  {
    BehaviorLoggerUtil.openManual( "edit-schema" );
  }
}
