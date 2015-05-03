package com.threebird.recorder.controllers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.models.schemas.Schema;
import com.threebird.recorder.models.schemas.SchemasManager;
import com.threebird.recorder.models.sessions.RecordingManager;
import com.threebird.recorder.models.sessions.SessionManager;
import com.threebird.recorder.utils.EventRecorderUtil;
import com.threebird.recorder.views.recording.BehaviorCountBox;
import com.threebird.recorder.views.recording.ContinuousCountBox;
import com.threebird.recorder.views.recording.DiscreteCountBox;

/**
 * Controls recording.fxml
 */
public class RecordingController
{
  private RecordingManager manager;

  @FXML private Label clientLabel;
  @FXML private Label projectLabel;
  @FXML private HBox sessionDetailsBox;

  @FXML private GridPane behaviorGrid;
  @FXML private VBox discreteBox;
  @FXML private VBox continuousBox;
  private Map< MappableChar, BehaviorCountBox > countBoxes = Maps.newHashMap();

  @FXML private Text pausedText;
  @FXML private Text recordingText;
  @FXML private Label timeBox;

  @FXML private Label spacebarLbl;
  @FXML private Label failedLabel;
  @FXML private Label savedLabel;

  @FXML private Button playButton;
  @FXML private Button goBackButton;
  @FXML private Button newSessionButton;
  @FXML private Button addNewKeysButton;

  /**
   * Sets the stage to the Recording view
   */
  public static void toRecordingView()
  {
    String filepath = "views/recording/recording.fxml";
    RecordingController controller = EventRecorderUtil.loadScene( filepath, "Recording" );
    controller.init();
  }

  private void init()
  {
    Schema schema = SchemasManager.getSelected();
    manager = new RecordingManager();

    behaviorGrid.setDisable( true );
    timeBox.setDisable( true );

    clientLabel.setText( schema.client );
    projectLabel.setText( schema.project );

    if (!Strings.isNullOrEmpty( SessionManager.getObserver() )) {
      String obsrvr = "Observer: " + SessionManager.getObserver();
      sessionDetailsBox.getChildren().add( new Label( obsrvr ) );
    }

    if (!Strings.isNullOrEmpty( SessionManager.getTherapist() )) {
      String therapist = "Therapist: " + SessionManager.getTherapist();
      sessionDetailsBox.getChildren().add( new Label( therapist ) );
    }

    if (!Strings.isNullOrEmpty( SessionManager.getCondition() )) {
      String condition = "Condition: " + SessionManager.getCondition();
      sessionDetailsBox.getChildren().add( new Label( condition ) );
    }

    if (!Strings.isNullOrEmpty( SessionManager.getLocation() )) {
      String condition = "Location: " + SessionManager.getLocation();
      sessionDetailsBox.getChildren().add( new Label( condition ) );
    }

    if (SessionManager.getSessionNumber() != null) {
      String session = "Session: " + SessionManager.getSessionNumber();
      sessionDetailsBox.getChildren().add( new Label( session ) );
    }

    savedLabel.setText( "Saved data to " + RecordingManager.getFullFileName() + "(.csv/.xls)" );
    savedLabel.setVisible( false );

    initializeTimer();
    initializeBehaviorCountBoxes();

    manager.playingProperty.addListener( ( obs, oldV, playing ) -> onPlayToggled( playing ) );
  }

  public void update()
  {
    Schema schema = SchemasManager.getSelected();

    // Figure out which unknowns were ignored and which were updated
    Set< MappableChar > mappedChars = schema.mappings.keySet();
    Set< MappableChar > unknownChars = manager.unknowns.keySet();
    SetView< MappableChar > ignoredChars = Sets.difference( unknownChars, mappedChars );
    SetView< MappableChar > newChars = Sets.intersection( mappedChars, unknownChars );

    // Remove the [unknown]s we don't care about from the GUI
    for (MappableChar ignored : ignoredChars) {
      BehaviorCountBox countBox = countBoxes.get( ignored );
      List< Node > target =
          discreteBox.getChildren().contains( countBox ) ? discreteBox.getChildren() : continuousBox.getChildren();

      int i = target.indexOf( countBox );
      target.remove( i + 1 );
      target.remove( i );

      countBoxes.remove( ignored );
    }

    // Update the CountBoxes labels
    for (MappableChar newChar : newChars) {
      String behavior = schema.mappings.get( newChar ).behavior;
      countBoxes.get( newChar ).behaviorLbl.setText( behavior );
    }

    addNewKeysButton.setVisible( false );
  }

  /**
   * Populates 'discreteBox' and the 'continuousBox' with the selected Schema's
   * mappings
   */
  private void initializeBehaviorCountBoxes()
  {
    Schema schema = SchemasManager.getSelected();

    for (KeyBehaviorMapping kbm : schema.mappings.values()) {
      BehaviorCountBox bcb =
          kbm.isContinuous
              ? new ContinuousCountBox( kbm, manager )
              : new DiscreteCountBox( kbm );
      VBox target = kbm.isContinuous ? continuousBox : discreteBox;

      target.getChildren().add( bcb );
      target.getChildren().add( new Separator() );

      countBoxes.put( kbm.key, bcb );
    }
  }

  /**
   * Sets up the 'timer' field to call "onTick" every second.
   */
  private void initializeTimer()
  {
    manager.counter.addListener( ( ctr, old, count ) -> onTick( count.intValue() ) );
  }

  /**
   * Every millisecond, update the counter. When the counter reaches the
   * duration, try to signal the user
   */
  private void onTick( int millis )
  {
    Schema schema = SchemasManager.getSelected();
    timeBox.setText( EventRecorderUtil.millisToTimestamp( millis ) );

    if (millis == schema.duration * 1000) {
      if (schema.color) {
        timeBox.setStyle( "-fx-background-color: #FFC0C0;-fx-border-color:red;-fx-border-radius:2;" );
      }

      if (schema.pause) {
        manager.playingProperty.set( false );
      }

      if (schema.sound) {
        java.awt.Toolkit.getDefaultToolkit().beep();
      }
    }
  }

  /**
   * Starts and stops recording, changes the playButton text appropriately.
   */
  private void onPlayToggled( boolean playing )
  {
    if (playing) {
      manager.timer.play();
      failedLabel.setVisible( false );
      savedLabel.setVisible( false );
    } else {
      manager.timer.pause();
      failedLabel.setVisible( !manager.saveSuccessfulProperty.get() );
      savedLabel.setVisible( manager.saveSuccessfulProperty.get() );
    }

    behaviorGrid.setDisable( !behaviorGrid.isDisabled() );
    timeBox.setDisable( !timeBox.isDisabled() );
    spacebarLbl.setText( playing ? "Spacebar = Stop" : "Spacebar = Continue" );
    playButton.setText( playing ? "Stop" : "Continue" );
    goBackButton.setVisible( !playing );
    newSessionButton.setVisible( !playing );
    recordingText.setVisible( !recordingText.isVisible() );
    pausedText.setVisible( !pausedText.isVisible() );

    if (!manager.unknowns.isEmpty()) {
      addNewKeysButton.setVisible( !playing );
    }
  }

  /**
   * @return true if 'c' is supposed to trigger one of he available shortcuts,
   *         or false otherwise
   */
  private boolean isShortcut( KeyCode c )
  {
    return KeyCode.SPACE.equals( c );
  }

  /**
   * Fires the appropriate action corresponding to the shortcut 'c' represents
   */
  private void handleShortcut( KeyCode c )
  {
    if (KeyCode.SPACE.equals( c )) {
      manager.togglePlayingProperty();
    }
  }

  /**
   * Logs a KeyMappingBehavior in GUI for the user to see
   */
  private void logBehavior( KeyBehaviorMapping mapping )
  {
    boolean toggledOn = countBoxes.get( mapping.key ).toggle();

    if (mapping.isContinuous) {
      if (!toggledOn) {
        ContinuousCountBox ccb = (ContinuousCountBox) countBoxes.get( mapping.key );
        int duration = manager.count() - ccb.getLastStart();
        manager.log( new ContinuousBehavior( mapping.key, mapping.behavior, ccb.getLastStart(), duration ) );
      }
    } else {
      manager.log( new DiscreteBehavior( mapping.key, mapping.behavior, manager.count() ) );
    }
  }

  /**
   * The user just pressed a key that isn't mapped. Add it to the 'unknowns'
   * map, the 'countBoxes' map, and display it on the screen
   */
  private void initUnknown( MappableChar mc, boolean isContinuous )
  {
    KeyBehaviorMapping kbm = new KeyBehaviorMapping( mc, "[unknown]", isContinuous );
    manager.unknowns.put( mc, kbm );
    BehaviorCountBox bcb =
        kbm.isContinuous
            ? new ContinuousCountBox( kbm, manager )
            : new DiscreteCountBox( kbm );
    VBox target = kbm.isContinuous ? continuousBox : discreteBox;

    target.getChildren().add( bcb );
    target.getChildren().add( new Separator() );

    countBoxes.put( kbm.key, bcb );

    if (!isContinuous) {
      logBehavior( kbm );
    } else {
      countBoxes.get( mc ).toggle();
    }
  }

  /**
   * Attached to the root pane, onKeyPressed should fire when the user types a
   * key, no matter what is selected
   */
  @FXML private void onKeyPressed( KeyEvent evt )
  {
    KeyCode code = evt.getCode();

    if (isShortcut( code )) {
      handleShortcut( code );
      return;
    }

    if (!manager.playingProperty.get()) {
      return;
    }

    MappableChar.getForKeyCode( code ).ifPresent( mc -> {
      Schema schema = SchemasManager.getSelected();
      if (schema.mappings.containsKey( mc )) {
        logBehavior( schema.mappings.get( mc ) );
      } else if (manager.unknowns.containsKey( mc )) {
        logBehavior( manager.unknowns.get( mc ) );
      } else {
        initUnknown( mc, evt.isControlDown() );
      }
    } );
  }

  @FXML private void onPlayPress( ActionEvent evt )
  {
    manager.togglePlayingProperty();
  }

  /**
   * If unknown behaviors exist, prompt the user to edit them. If user declines,
   * run 'toScene'. If user accepts, go to add-keys view
   * 
   * @param toScene
   *          - a void function that should change scenes
   */
  private void checkUnknownsAndChangeScene( Runnable toScene )
  {
    if (!manager.unknowns.isEmpty()) {
      String msg = "You have recorded unknown behaviors. Would you like to edit them?";
      Alert alert = new Alert( Alert.AlertType.CONFIRMATION );
      alert.setTitle( "Confirm deletion." );
      alert.setHeaderText( null );
      alert.setContentText( msg );
      ButtonType editBtn = new ButtonType( "Edit Unknowns" );
      ButtonType discardBtn = new ButtonType( "Discard Unknowns", ButtonData.CANCEL_CLOSE );
      alert.getButtonTypes().setAll( discardBtn, editBtn );
      alert.showAndWait()
           .ifPresent( r -> {
             if (r == editBtn) {
               AddKeysController.showAddKeysView( this, manager );
             } else {
               toScene.run();
             }
           } );
    } else {
      toScene.run();
    }
  }

  @FXML private void onGoBackPress( ActionEvent evt )
  {
    Integer sessionNum = SessionManager.getSessionNumber();

    checkUnknownsAndChangeScene( ( ) -> {
      if (sessionNum != null && manager.timer.getCurrentTime().greaterThan( Duration.ZERO )) {
        SessionManager.setSessionNumber( sessionNum + 1 );
      }
      StartMenuController.toStartMenuView();
    } );
  }

  @FXML private void onNewSessionPress( ActionEvent evt )
  {
    Integer sessionNum = SessionManager.getSessionNumber();

    checkUnknownsAndChangeScene( ( ) -> {
      if (sessionNum != null) {
        SessionManager.setSessionNumber( sessionNum + 1 );
      }
      RecordingController.toRecordingView();
    } );
  }

  @FXML private void onAddNewKeysPress( ActionEvent evt )
  {
    AddKeysController.showAddKeysView( this, manager );
  }

  @FXML private void onHelpBtnPressed()
  {
    EventRecorderUtil.openManual( "recording" );
  }
}
