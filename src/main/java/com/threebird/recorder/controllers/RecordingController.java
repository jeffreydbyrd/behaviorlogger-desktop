package com.threebird.recorder.controllers;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
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
import com.threebird.recorder.models.sessions.ContinuousCounter;
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
  private Map< KeyBehaviorMapping, BehaviorCountBox > countBoxes = Maps.newHashMap();

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
      initializeBehaviorCountBox( kbm );
    }
  }

  private void initializeBehaviorCountBox( KeyBehaviorMapping kbm )
  {
    BehaviorCountBox bcb;
    VBox target;
    SimpleIntegerProperty count = new SimpleIntegerProperty( 0 );

    if (kbm.isContinuous) {
      bcb = new ContinuousCountBox( kbm );
      target = continuousBox;
      initializeContinuousCountBox( kbm, count );
    } else {
      bcb = new DiscreteCountBox( kbm );
      target = discreteBox;
      manager.discreteCounts.put( kbm, count );
    }

    count.addListener( ( obs, old, newv ) -> bcb.setCount( newv.intValue() ) );
    target.getChildren().add( bcb );
    target.getChildren().add( new Separator() );

    countBoxes.put( kbm, bcb );
  }

  private void initializeContinuousCountBox( KeyBehaviorMapping kbm, SimpleIntegerProperty count )
  {
    Timeline timer = new Timeline();

    timer.setCycleCount( Animation.INDEFINITE );
    KeyFrame kf = new KeyFrame( Duration.seconds( 1 ), evt -> {
      count.set( count.get() + 1 );
    } );
    timer.getKeyFrames().add( kf );

    manager.playingProperty.addListener( ( obs, oldV, playing ) -> {
      if (manager.midContinuous.containsKey( kbm )) {
        if (playing) {
          timer.play();
        } else {
          timer.pause();
        }
      }
    } );

    manager.continuousCounts.put( kbm, new ContinuousCounter( timer, count ) );
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
   * If the event is executing a shortcut, then execute the shortcut and return
   * true, else return false.
   */
  private boolean handleShortcut( KeyEvent evt )
  {
    KeyCode c = evt.getCode();

    if (KeyCode.SPACE.equals( c )) {
      manager.togglePlayingProperty();
      return true;
    }

    if (KeyCode.Z.equals( c ) && evt.isShortcutDown()) {
      undo();
      return true;
    }

    return false;
  }

  /**
   * Returns the latest, actively running, ContinuousBehavior wrapped in an
   * Optional, or empty if there are none running
   */
  private Optional< ContinuousBehavior > getLatestRunningContinuous()
  {
    Optional< ContinuousBehavior > result = Optional.empty();

    for (Entry< KeyBehaviorMapping, ContinuousBehavior > entry : manager.midContinuous.entrySet()) {
      ContinuousBehavior cb = entry.getValue();
      if (!result.isPresent() || result.get().startTime < cb.startTime) {
        result = Optional.of( cb );
      }
    }

    return result;
  }

  /**
   * Removes the latest logged behavior from either the Discrete or Continuous
   * list (whichever is latest)
   */
  private void undo()
  {
    int lastIndexDiscrete = manager.discrete.size() - 1;
    int lastIndexContinuous = manager.continuous.size() - 1;

    // TODO: address the case where we haven't recording any behaviors yet.
    // Currently we get Array Index Out-Of-Bounds
    DiscreteBehavior db = manager.discrete.get( lastIndexDiscrete );
    ContinuousBehavior cb = manager.continuous.get( lastIndexContinuous );
    Optional< ContinuousBehavior > optCb = getLatestRunningContinuous();

    // If discrete-behavior started after the continuous-behavior ended and
    // after the mid-continuous started (if there is one):
    if (db.startTime > (cb.startTime + cb.getDuration())
        && (!optCb.isPresent() || cb.startTime > optCb.get().startTime)) {
      manager.discrete.remove( lastIndexDiscrete );
      SimpleIntegerProperty count = manager.discreteCounts.get( db );
      count.set( count.get() - 1 );
      return;
    }

    // If continuous-behavior ended after mid-continuous started
    if (!optCb.isPresent() || (cb.startTime + cb.getDuration() > optCb.get().startTime)) {
      manager.continuous.remove( lastIndexContinuous );
      KeyBehaviorMapping kbm = new KeyBehaviorMapping( cb.key, cb.description, true );
      manager.midContinuous.put( kbm, cb );
      ContinuousCounter counter = manager.continuousCounts.get( kbm );
      counter.count.set( counter.count.get() + (manager.count() - counter.count.get()) );
      counter.timer.play();
      return;
    }

    // If we made it this far and mid-continuous is present, then it must be the
    // latest action
    if (optCb.isPresent()) {
      ContinuousBehavior midCb = optCb.get();
      KeyBehaviorMapping kbm = new KeyBehaviorMapping( midCb.key, midCb.description, true );
      ContinuousCounter counter = manager.continuousCounts.get( kbm );
      counter.timer.stop();
      counter.count.set( counter.count.get() - (manager.count() - midCb.startTime) );
    }
  }

  private void logBehavior( KeyBehaviorMapping mapping )
  {
    countBoxes.get( mapping ).toggle();

    if (mapping.isContinuous) {
      logContinuous( mapping );
    } else {
      manager.log( new DiscreteBehavior( mapping.key, mapping.behavior, manager.count() ) );
      SimpleIntegerProperty count = manager.discreteCounts.get( mapping );
      count.set( count.get() + 1 );
    }
  }

  /**
   * Converts the KeyBehaviorMapping to a ContinuousBehavior and registers it
   * with the manager. If the behavior hasn't been initialized, it gets cached
   * in manager.midContinuous. If it *has* been initialized, it gets moved into
   * manager.continuous.
   */
  private void logContinuous( KeyBehaviorMapping mapping )
  {
    if (manager.midContinuous.containsKey( mapping )) {
      ContinuousBehavior cb = manager.midContinuous.get( mapping );
      int duration = manager.count() - cb.startTime;
      manager.log( new ContinuousBehavior( cb.key, cb.description, cb.startTime, duration ) );
      manager.midContinuous.remove( mapping );
      manager.continuousCounts.get( mapping ).timer.pause();
    } else {
      ContinuousBehavior cb =
          new ContinuousBehavior( mapping.key, mapping.behavior, manager.count(), null );
      manager.midContinuous.put( mapping, cb );
      manager.continuousCounts.get( mapping ).timer.play();
    }
  }

  /**
   * The user just pressed a key that isn't mapped. Add it to the 'unknowns' map
   */
  private void initUnknown( MappableChar mc, boolean isContinuous )
  {
    KeyBehaviorMapping kbm = new KeyBehaviorMapping( mc, "[unknown]", isContinuous );
    initializeBehaviorCountBox( kbm );
    manager.unknowns.put( mc, kbm );
    logBehavior( kbm );
  }

  /**
   * Attached to the root pane, onKeyPressed should fire when the user types a
   * key, no matter what is selected
   */
  @FXML private void onKeyPressed( KeyEvent evt )
  {
    KeyCode code = evt.getCode();

    boolean isShortcut = handleShortcut( evt );
    if (isShortcut) {
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
        initUnknown( mc, evt.isShiftDown() );
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
