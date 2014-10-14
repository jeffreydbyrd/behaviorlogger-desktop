package com.threebird.recorder.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import com.threebird.recorder.models.Schema;

/**
 * Controls the Recording view. It should explode if SCHEMA is not yet
 * initialized
 */
public class RecordingController
{
  private Schema schema;

  @FXML private Text nameText;
  @FXML private VBox keylogPane;
  @FXML private Button playButton;
  @FXML private Text timeText;

  private int counter = 0;
  private boolean playing = false;
  private Timeline timer;

  public void init( Schema sch )
  {
    this.schema = sch;
    nameText.setText( schema.name );

    timer = new Timeline();
    timer.setCycleCount( Animation.INDEFINITE );
    KeyFrame kf = new KeyFrame( Duration.seconds( 1 ), this::onTick );
    timer.getKeyFrames().add( kf );
    timer.play();
  }

  private void onTick( ActionEvent evt )
  {
    counter++;
    timeText.setText( counter + "" );
  }

  /**
   * Attached to the root pane, onKeyTyped should fire no matter what is
   * selected
   */
  @FXML private void onKeyTyped( KeyEvent evt )
  {
    Character c = evt.getCharacter().charAt( 0 );

    if (new Character( ' ' ).equals( c )) {
      togglePlayButton();
      return;
    }

    if (!schema.mappings.containsKey( c )) {
      return;
    }

    String text = String.format( "%d - (%c) %s",
                                 counter, c, schema.mappings.get( c )
                        );

    keylogPane.getChildren().add( new Text( text ) );
  }

  @FXML private void onPlayPress( ActionEvent evt )
  {
    togglePlayButton();
  }

  private void togglePlayButton()
  {
    playing = !playing;
    if (playing) {
      playButton.setText( "Stop" );
      timer.play();
    } else {
      playButton.setText( "Play" );
      timer.pause();
    }
  }

}
