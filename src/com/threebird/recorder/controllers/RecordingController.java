package com.threebird.recorder.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import com.threebird.recorder.models.Schema;

/**
 * Controls the Recording view. It should explode if SCHEMA is not yet
 * initialized
 */
public class RecordingController
{
  public static Schema SCHEMA;

  @FXML private Text nameText;
  @FXML private VBox keylogPane;
  @FXML private Button playButton;

  @FXML private void initialize()
  {
    assert SCHEMA != null;

    nameText.setText( SCHEMA.name );
  }

  /**
   * Attached to the root pane, onKeyTyped should fire no matter what is
   * selected
   */
  @FXML private void onKeyTyped( KeyEvent evt )
  {
    Character c = evt.getCharacter().charAt( 0 );
    if (!SCHEMA.mappings.containsKey( c )) {
      return;
    }
    String time = new SimpleDateFormat( "kk:mm:ss" ).format( new Date() );
    String text = String.format( "%s - (%c) %s",
                                 time,
                                 c,
                                 SCHEMA.mappings.get( c )
                        );

    keylogPane.getChildren().add( new Text( text ) );
  }

  @FXML private void onPlayPress( ActionEvent evt )
  {
    System.out.println( "play!" );
  }
}
