package com.threebird.recorder.controllers;

import java.util.Optional;
import java.util.Set;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import com.google.common.collect.Sets;
import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.Recording;
import com.threebird.recorder.models.Schema;

/**
 * Controls the Recording view
 */
public class RecordingController
{
  private Schema schema;
  private int counter = 0;
  private boolean playing = false;
  private Timeline timer;
  private Set< Character > unknowns = Sets.newHashSet();
  private Recording recording;

  @FXML private Text nameText;

  @FXML private VBox discreteBox;
  @FXML private VBox continuousBox;

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
    initializeBehaviorBoxes();
    initializeTimer();
  }

  private void initializeBehaviorBoxes()
  {
    for (KeyBehaviorMapping kbm : schema.mappings.values()) {
      if (kbm.isContinuous) {
        
      } else {

      }
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

  private void logContinuous( KeyBehaviorMapping kbm )
  {}

  private void logDiscrete( KeyBehaviorMapping kbm )
  {}

  /**
   * @return true if 'c' is supposed to trigger one of he available shortcuts,
   *         or false otherwise
   */
  private boolean isShortcut( Character c )
  {
    return new Character( ' ' ).equals( c );
  }

  /**
   * Fires the appropriate action corresponding to the shortcut 'c' represents
   */
  private void handleShortcut( Character c )
  {
    if (new Character( ' ' ).equals( c )) {
      togglePlayButton();
    }
  }

  /**
   * Logs a KeyMappingBehavior in GUI for the user to see
   */
  private void logBehavior( KeyBehaviorMapping mapping )
  {
    if (mapping.isContinuous) {
      logContinuous( mapping );
    } else {
      logDiscrete( mapping );
    }
  }

  private void logUnknown( Character c )
  {
    unknowns.add( c );
  }

  /**
   * Attached to the root pane, onKeyTyped should fire when the user types a
   * key, no matter what is selected
   */
  @FXML private void onKeyTyped( KeyEvent evt )
  {
    Character c = evt.getCharacter().charAt( 0 );

    if (isShortcut( c )) {
      handleShortcut( c );
      return;
    }

    if (!playing) {
      return;
    }

    Optional< KeyBehaviorMapping > optMapping = schema.getMapping( c );
    if (optMapping.isPresent()) {
      logBehavior( optMapping.get() );
    } else {
      logUnknown( c );
    }

  }

  /** Upon pressing the "Play" or "Stop" button */
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
    EventRecorder.toAddKeysView( EventRecorder.STAGE.getScene(), schema, unknowns );
  }
}
