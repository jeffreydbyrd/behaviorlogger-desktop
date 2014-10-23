package com.threebird.recorder.controllers;

import java.io.IOException;
import java.util.HashMap;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
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
import com.threebird.recorder.models.KeyBehaviorMapping;
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

  @FXML private ScrollPane behaviorScrollPane;
  @FXML private VBox behaviorBox;

  @FXML private VBox continuousBox;

  @FXML private Text pausedText;
  @FXML private Text recordingText;
  @FXML private Label timeLabel;

  @FXML private Button playButton;
  @FXML private Button goBackButton;
  @FXML private Button newSessionButton;

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

    populateKeyBehaviorReferenceBox();

    initializeTimer();

    referenceScrollPane.requestFocus();

    // When behaviorBox changes size, scroll to the bottom to show change
    behaviorBox.heightProperty()
             .addListener( ( obv, oldV, newV ) -> behaviorScrollPane.setVvalue( 1.0 ) );
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
   * Fills up the right-most ScrollPane with all the behaviors the researcher
   * has mapped out. This serves as a reference during sessions
   */
  private void populateKeyBehaviorReferenceBox()
  {
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

    recordingText.setVisible( !recordingText.isVisible() );
    pausedText.setVisible( !pausedText.isVisible() );
  }

  /**
   * Builds a String for a duration behavior that will be logged in the left
   * ScrollPane
   */
  private String continuousString( KeyBehaviorMapping kbm, Integer startTime )
  {
    String format = "%d - %d : (%c) %s";
    String str =
        String.format( format, startTime, counter, kbm.key, kbm.behavior );
    return str;
  }

  /**
   * Builds a String for an eventful behavior that will be logged in the left
   * ScrollPane
   */
  private String discreteString( KeyBehaviorMapping kbm )
  {
    String str =
        String.format( "%d : (%c) %s", counter, kbm.key, kbm.behavior );
    return str;
  }

  /**
   * I'm simply creating this for the logContinuous function so we can map a
   * Character to both a Textbox and a start-time and save them for when the
   * user stops a continuous behavior.
   */
  private class IntegerTextPair
  {
    final Integer startTime;
    final Text text;

    public IntegerTextPair( Integer startTime, Text text )
    {
      this.startTime = startTime;
      this.text = text;
    }
  }

  private HashMap< Character, IntegerTextPair > intermediate = new HashMap<>();

  /**
   * The user just pressed a key that represents a DurationBehavior. If this
   * behavior has already started, stop it and log it to the left ScollPane.
   * Otherwise start it and log it to the right ScrollPane
   */
  private void logContinuous( KeyBehaviorMapping kbm )
  {
    Character key = kbm.key;
    ObservableList< Node > texts = continuousBox.getChildren();

    if (intermediate.containsKey( key )) {
      IntegerTextPair itp = intermediate.get( key );
      String str = continuousString( kbm, itp.startTime );
      behaviorBox.getChildren().add( new Text( str ) );
      texts.remove( itp.text );
      intermediate.remove( key );
    } else {
      String str = discreteString( kbm );
      Text text = new Text( str );
      IntegerTextPair itp = new IntegerTextPair( counter, text );
      intermediate.put( key, itp );
      texts.add( text );
    }
  }

  /**
   * The user just pressed a key that represents a discrete behavior. Simply
   * log it to the left ScrollPane
   */
  private void logDiscrete( KeyBehaviorMapping kbm )
  {
    String str = discreteString( kbm );
    behaviorBox.getChildren().add( new Text( str ) );
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

    schema.getMapping( c ).ifPresent( ( mapping ) -> {
      if (mapping.isContinuous) {
        logContinuous( mapping );
      } else {
        logDiscrete( mapping );
      }
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
