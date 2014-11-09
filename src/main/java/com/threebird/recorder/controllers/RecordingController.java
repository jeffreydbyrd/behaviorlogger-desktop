package com.threebird.recorder.controllers;

import java.util.HashMap;
import java.util.Map;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import com.google.common.collect.Maps;
import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.models.Recording;
import com.threebird.recorder.models.Schema;
import com.threebird.recorder.models.behaviors.ContinuousBehavior;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
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
  private boolean playing = false;
  private Timeline timer;
  private HashMap< Character, KeyBehaviorMapping > unknowns = Maps.newHashMap();
  private Recording recording = new Recording();

  @FXML private Text nameText;

  @FXML private VBox discreteBox;
  @FXML private VBox continuousBox;
  private Map< Character, BehaviorCountBox > countBoxes = Maps.newHashMap();

  @FXML private Text pausedText;
  @FXML private Text recordingText;
  @FXML private Label timeLabel;

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
    nameText.setText( schema.name );
    initializeTimer();
    initializeBehaviorCountBoxes();
  }

  /**
   * Populates 'discreteBox' and the 'continuousBox' with the selected Schema's
   * mappings
   */
  private void initializeBehaviorCountBoxes()
  {
    for (KeyBehaviorMapping kbm : schema.mappings.values()) {
      BehaviorCountBox bcb =
          kbm.isContinuous ? new ContinuousCountBox( kbm, timer ) : new DiscreteCountBox( kbm );
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
    timeLabel.setText( counter + "" );

    if (counter == schema.duration) {
      timeLabel.setStyle( "-fx-background-color: #FFC0C0;-fx-border-color:black;-fx-border-radius:2;" );
    }
  }

  /**
   * Starts and stops recording, changes the playButton text appropriately.
   */
  private void togglePlayButton()
  {
    playing = !playing;
    if (playing) {
      timer.play();
    } else {
      timer.pause();
    }

    playButton.setText( playing ? "Stop" : "Play" );
    goBackButton.setVisible( !playing );
    newSessionButton.setVisible( !playing );

    if (!unknowns.isEmpty()) {
      addNewKeysButton.setVisible( !playing );
    }

    recordingText.setVisible( !recordingText.isVisible() );
    pausedText.setVisible( !pausedText.isVisible() );
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
      togglePlayButton();
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
  private void initUnknown( Character c, boolean isContinuous )
  {
    KeyBehaviorMapping kbm = new KeyBehaviorMapping( c, "[unknown]", isContinuous );
    unknowns.put( c, kbm );
    BehaviorCountBox bcb =
        kbm.isContinuous ? new ContinuousCountBox( kbm, timer ) : new DiscreteCountBox( kbm );
    VBox target = kbm.isContinuous ? continuousBox : discreteBox;

    target.getChildren().add( bcb );
    target.getChildren().add( new Separator() );

    countBoxes.put( kbm.key, bcb );
    countBoxes.get( c ).toggle();
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

    if (!playing) {
      return;
    }

    MappableChar.getForKeyCode( code ).ifPresent( mc -> {
      char c = mc.c;
      if (schema.mappings.containsKey( c )) {
        logBehavior( schema.mappings.get( c ) );
      } else if (unknowns.containsKey( c )) {
        logBehavior( unknowns.get( c ) );
      } else {
        initUnknown( c, evt.isControlDown() );
      }
    } );
  }

  @FXML private void onPlayPress( ActionEvent evt )
  {
    togglePlayButton();
  }

  @FXML private void onGoBackPress( ActionEvent evt )
  {
    EventRecorder.toSchemasView();
  }

  @FXML private void onNewSessionPress( ActionEvent evt )
  {
    EventRecorder.toRecordingView( schema );
  }

  @FXML private void onAddNewKeysPress( ActionEvent evt )
  {
    EventRecorder.toAddKeysView( EventRecorder.STAGE.getScene(), schema, unknowns.keySet() );
  }
}
