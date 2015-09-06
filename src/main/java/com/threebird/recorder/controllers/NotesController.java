package com.threebird.recorder.controllers;

import java.io.IOException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.sessions.NotesManager;
import com.threebird.recorder.models.sessions.RecordingManager;
import com.threebird.recorder.utils.Alerts;

public class NotesController
{
  @FXML private TextArea textArea;

  public static void bindNotesToStage( Stage stage, RecordingManager manager )
  {
    String fxmlPath = "views/recording/notes.fxml";

    FXMLLoader fxmlLoader =
        new FXMLLoader( EventRecorder.class.getResource( fxmlPath ) );

    Parent root;
    try {
      root = (Parent) fxmlLoader.load();
    } catch (IOException e) {
      Alerts.error( "Error Loading Resource", "There was a problem loading a resource: " + fxmlPath, e );
      throw new RuntimeException( e );
    }
    Scene scene = new Scene( root );

    stage.setTitle( "Notes" );
    stage.setScene( scene );

    stage.setX( NotesManager.xPosProperty().get() );
    stage.setY( NotesManager.yPosProperty().get() );

    stage.xProperty().addListener( ( obs, o, xPos ) -> NotesManager.xPosProperty().setValue( xPos ) );
    stage.yProperty().addListener( ( obs, o, yPos ) -> NotesManager.yPosProperty().setValue( yPos ) );

    ChangeListener< Parent > onViewChange = new ChangeListener< Parent >() {
      @Override public void changed( ObservableValue< ? extends Parent > observable,
                                     Parent oldValue,
                                     Parent newValue )
      {
        stage.hide();
        EventRecorder.STAGE.getScene().rootProperty().removeListener( this );
      }
    };
    EventRecorder.STAGE.getScene().rootProperty().addListener( onViewChange );

    fxmlLoader.< NotesController > getController().init( manager );
  }

  private void init( RecordingManager manager )
  {
    textArea.textProperty().addListener( ( obs, old, newV ) -> manager.notes.set( newV ) );
    textArea.requestFocus();
  }
}
