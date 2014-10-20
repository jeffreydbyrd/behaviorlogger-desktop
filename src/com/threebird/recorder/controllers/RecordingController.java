package com.threebird.recorder.controllers;

import java.io.IOException;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.Schema;

/**
 * Controls the Recording view. It should explode if SCHEMA is not yet
 * initialized
 */
public class RecordingController
{
  private Schema schema;
  private int duration;
  private int counter = 0;
  private boolean playing = false;
  private Timeline timer;

  @FXML private Text nameText;
  @FXML private ScrollPane eventsScrollPane;
  @FXML private VBox keylogBox;
  @FXML private Button playButton;
  @FXML private Button goBackButton;
  @FXML private Button newSessionButton;
  @FXML private Label timeLabel;

  @FXML private ScrollPane referenceScrollPane;
  @FXML private VBox referenceBox;

  /**
   * @param sch
   *          - The Schema being used for recording
   * @param duration
   *          - the duration, in seconds, of the session
   */
  public void init( Schema sch, int duration )
  {
    this.schema = sch;
    this.duration = duration;
    nameText.setText( schema.name );

    // Populate the key-behavior reference box
    schema.mappings.forEach( ( key, m ) -> {
      Text keyText = new Text( key.toString() );
      keyText.setWrappingWidth( 10 );
      HBox.setHgrow( keyText, Priority.NEVER );

      Text separator = new Text( " : " );
      HBox.setHgrow( separator, Priority.NEVER );

      Text behaviorText = new Text( m.behavior );
      behaviorText.setWrappingWidth( 140 );
      HBox.setHgrow( behaviorText, Priority.ALWAYS );

      HBox hbox = new HBox( keyText, separator, behaviorText );
      referenceBox.getChildren().add( hbox );
    } );

    // Setup the timer
    timer = new Timeline();
    timer.setCycleCount( Animation.INDEFINITE );
    KeyFrame kf = new KeyFrame( Duration.seconds( 1 ), this::onTick );
    timer.getKeyFrames().add( kf );

    referenceScrollPane.requestFocus();

    // When keylogBox changes size, scroll to the bottom to show change
    keylogBox.heightProperty()
             .addListener( ( obv, oldV, newV ) -> eventsScrollPane.setVvalue( 1.0 ) );
  }

  /**
   * Every second, update the counter. When the counter reaches the duration,
   * try to signal the user
   */
  private void onTick( ActionEvent evt )
  {
    counter++;
    timeLabel.setText( counter + "" );

    if (counter == duration) {
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
  }

  /**
   * Attached to the root pane, onKeyTyped should fire no matter what is
   * selected
   */
  @FXML private void onKeyTyped( KeyEvent evt )
  {
    Character c = evt.getCharacter().charAt( 0 );

    if (new Character( ' ' ).equals( c ) && !playButton.isFocused()) {
      togglePlayButton();
    }

    if (!playing) {
      return;
    }

    // look at this elegant motherfucker
    schema.getMapping( c ).ifPresent( ( mapping ) -> {
      String behavior = mapping.behavior;
      String text = String.format( "%d - (%c) %s", counter, c, behavior );
      keylogBox.getChildren().add( new Text( text ) );
    } );
  }

  @FXML private void onPlayPress( ActionEvent evt )
  {
    togglePlayButton();
  }

  @FXML private void onGoBackPress( ActionEvent evt ) throws IOException
  {
    EventRecorder.toSchemaView();
  }

  @FXML private void onNewSessionPress( ActionEvent evt ) throws IOException
  {
    EventRecorder.toRecordingView( schema, duration );
  }

  @FXML private void stopClickPropogation( MouseEvent evt )
  {
    evt.consume();
  }
}
