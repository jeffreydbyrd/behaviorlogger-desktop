package com.threebird.recorder.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.Recording;
import com.threebird.recorder.models.Schema;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.views.TimeBox;
import com.threebird.recorder.views.recording.BehaviorCountBox;
import com.threebird.recorder.views.recording.ContinuousCountBox;
import com.threebird.recorder.views.recording.DiscreteCountBox;

/**
 * Controls recording.fxml
 */
public class RecordingController
{
  private Schema schema;
  private int counter = 0;
  private SimpleBooleanProperty playingProperty = new SimpleBooleanProperty( false );
  private Timeline timer;
  private HashMap< MappableChar, KeyBehaviorMapping > unknowns = Maps.newHashMap();
  private Recording recording = new Recording();

  @FXML private Label clientLabel;
  @FXML private Label projectLabel;
  @FXML private HBox sessionDetailsBox;

  @FXML private VBox discreteBox;
  @FXML private VBox continuousBox;
  private Map< MappableChar, BehaviorCountBox > countBoxes = Maps.newHashMap();

  @FXML private Text pausedText;
  @FXML private Text recordingText;
  @FXML private Pane timeBoxSlot;
  private TimeBox timeBox;

  @FXML private Button playButton;
  @FXML private Button goBackButton;
  @FXML private Button newSessionButton;
  @FXML private Button addNewKeysButton;

  /**
   * @param sch
   *          - The Schema being used for recording
   */
  public void init( Schema sch )
  {
    this.schema = sch;

    clientLabel.setText( this.schema.client );
    projectLabel.setText( this.schema.project );

    if (SchemasController.SESSION_DETAILS.observer != null) {
      String obsrvr = "Observer: " + SchemasController.SESSION_DETAILS.observer;
      sessionDetailsBox.getChildren().add( new Label( obsrvr ) );
    }

    if (SchemasController.SESSION_DETAILS.therapist != null) {
      String therapist = "Therapist: " + SchemasController.SESSION_DETAILS.therapist;
      sessionDetailsBox.getChildren().add( new Label( therapist ) );
    }

    if (SchemasController.SESSION_DETAILS.condition != null) {
      String condition = "Condition: " + SchemasController.SESSION_DETAILS.condition;
      sessionDetailsBox.getChildren().add( new Label( condition ) );
    }

    if (SchemasController.SESSION_DETAILS.sessionNum != null) {
      String session = "Session: " + SchemasController.SESSION_DETAILS.sessionNum;
      sessionDetailsBox.getChildren().add( new Label( session ) );
    }

    initializeTimer();
    initializeBehaviorCountBoxes();

    playingProperty.addListener( ( obs, oldV, playing ) -> togglePlayButton( playing ) );
  }

  public void update()
  {
    Set< MappableChar > mappedChars = schema.mappings.keySet();
    Set< MappableChar > unknownChars = unknowns.keySet();
    SetView< MappableChar > ignoredChars = Sets.difference( unknownChars, mappedChars );
    SetView< MappableChar > newChars = Sets.intersection( mappedChars, unknownChars );

    // Well this shit is ugly, but it gets the job done
    for (MappableChar ignored : ignoredChars) {
      BehaviorCountBox countBox = countBoxes.get( ignored );
      List< Node > target =
          discreteBox.getChildren().contains( countBox ) ? discreteBox.getChildren() : continuousBox.getChildren();

      int i = target.indexOf( countBox );
      target.remove( i + 1 );
      target.remove( i );

      countBoxes.remove( ignored );
    }

    for (MappableChar newChar : newChars) {
      String behavior = schema.mappings.get( newChar ).behavior;
      countBoxes.get( newChar ).behaviorLbl.setText( behavior );
    }

    unknowns.clear();
    addNewKeysButton.setVisible( false );
  }

  /**
   * Populates 'discreteBox' and the 'continuousBox' with the selected Schema's
   * mappings
   */
  private void initializeBehaviorCountBoxes()
  {
    for (KeyBehaviorMapping kbm : schema.mappings.values()) {
      BehaviorCountBox bcb =
          kbm.isContinuous
              ? new ContinuousCountBox( kbm, playingProperty )
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
    timeBox = new TimeBox( 0 );
    timeBoxSlot.getChildren().clear();
    timeBoxSlot.getChildren().add( timeBox );
    timer = new Timeline();
    timer.setCycleCount( Animation.INDEFINITE );
    KeyFrame kf = new KeyFrame( Duration.seconds( 1 ), this::onTick );
    timer.getKeyFrames().add( kf );
  }

  /**
   * Every second, update the counter. When the counter reaches the duration,
   * try to signal the user
   */
  private void onTick( ActionEvent evt )
  {
    counter++;
    timeBox.setTime( counter );

    if (counter == schema.duration) {
      if (schema.color) {
        timeBoxSlot.setStyle( "-fx-background-color: #FFC0C0;-fx-border-color:red;-fx-border-radius:2;" );
      }

      if (schema.pause) {
        playingProperty.set( false );
      }

      if (schema.sound) {
        java.awt.Toolkit.getDefaultToolkit().beep();
      }
    }
  }

  /**
   * Starts and stops recording, changes the playButton text appropriately.
   */
  private void togglePlayButton( boolean playing )
  {
    if (playing) {
      timer.play();
    } else {
      timer.pause();
    }

    playButton.setText( playing ? "Stop" : "Play" );
    goBackButton.setVisible( !playing );
    newSessionButton.setVisible( !playing );
    recordingText.setVisible( !recordingText.isVisible() );
    pausedText.setVisible( !pausedText.isVisible() );

    if (!unknowns.isEmpty()) {
      addNewKeysButton.setVisible( !playing );
    }
  }

  private void togglePlayingProperty()
  {
    playingProperty.set( !playingProperty.get() );
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
      togglePlayingProperty();
    }
  }

  /**
   * Logs a KeyMappingBehavior in GUI for the user to see
   */
  private void logBehavior( KeyBehaviorMapping mapping )
  {
    boolean toggled = countBoxes.get( mapping.key ).toggle();

    if (mapping.isContinuous) {
      if (!toggled) {
        ContinuousCountBox ccb = (ContinuousCountBox) countBoxes.get( mapping.key );
        int duration = counter - ccb.getLastStart();
        if (duration > 0) {
          recording.log( new ContinuousBehavior( mapping.key, mapping.behavior, ccb.getLastStart(), duration ) );
        }
      }
    } else {
      recording.log( new DiscreteBehavior( mapping.key, mapping.behavior, counter ) );
    }
  }

  /**
   * The user just pressed a key that isn't mapped. Add it to the 'unknowns'
   * map, the 'countBoxes' map, and display it on the screen
   */
  private void initUnknown( MappableChar mc, boolean isContinuous )
  {
    KeyBehaviorMapping kbm = new KeyBehaviorMapping( mc, "[unknown]", isContinuous );
    unknowns.put( mc, kbm );
    BehaviorCountBox bcb =
        kbm.isContinuous
            ? new ContinuousCountBox( kbm, playingProperty )
            : new DiscreteCountBox( kbm );
    VBox target = kbm.isContinuous ? continuousBox : discreteBox;

    target.getChildren().add( bcb );
    target.getChildren().add( new Separator() );

    countBoxes.put( kbm.key, bcb );
    countBoxes.get( mc ).toggle();
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

    if (!playingProperty.get()) {
      return;
    }

    MappableChar.getForKeyCode( code ).ifPresent( mc -> {
      if (schema.mappings.containsKey( mc )) {
        logBehavior( schema.mappings.get( mc ) );
      } else if (unknowns.containsKey( mc )) {
        logBehavior( unknowns.get( mc ) );
      } else {
        initUnknown( mc, evt.isControlDown() );
      }
    } );
  }

  @FXML private void onPlayPress( ActionEvent evt )
  {
    togglePlayingProperty();
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
    if (!unknowns.isEmpty()) {
      String msg = "You have recorded unknown behaviors. Would you like to edit them?";
      String leftOption = "Discard Unknowns";
      String rightOption = "Edit Unknowns";

      EventHandler< ActionEvent > onDiscardClick = e -> toScene.run();

      EventHandler< ActionEvent > onEditClick = e ->
          EventRecorder.toAddKeysView( EventRecorder.STAGE.getScene(), this, schema, unknowns.values() );

      EventRecorder.dialogBox( msg, leftOption, rightOption, onDiscardClick, onEditClick );
    } else {
      toScene.run();
    }
  }

  @FXML private void onGoBackPress( ActionEvent evt )
  {
    if (SchemasController.SESSION_DETAILS.sessionNum != null && timer.getCurrentTime().greaterThan( Duration.ZERO )) {
      SchemasController.SESSION_DETAILS.sessionNum += 1;
    }
    checkUnknownsAndChangeScene( ( ) -> EventRecorder.toSchemasView( schema ) );
  }

  @FXML private void onNewSessionPress( ActionEvent evt )
  {
    if (SchemasController.SESSION_DETAILS.sessionNum != null) {
      SchemasController.SESSION_DETAILS.sessionNum += 1;
    }
    checkUnknownsAndChangeScene( ( ) -> EventRecorder.toRecordingView( schema ) );
  }

  @FXML private void onAddNewKeysPress( ActionEvent evt )
  {
    EventRecorder.toAddKeysView( EventRecorder.STAGE.getScene(), this, schema, unknowns.values() );
  }
}
