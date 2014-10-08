package com.threebird.observer.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import com.threebird.observer.models.Schema;

public class RecordingController
{
  public static Schema SCHEMA;

  @FXML private AnchorPane root;
  @FXML private VBox keylogPane;

  @FXML private void initialize()
  {
    assert SCHEMA != null;
  }

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
}
