package com.threebird.recorder.controllers;

import java.io.IOException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.PositionManager;
import com.threebird.recorder.models.sessions.RecordingManager;
import com.threebird.recorder.utils.Alerts;

public class NotesController
{
  @FXML private TextArea textArea;
  @FXML private Label savedLabel;
  @FXML private Label failedLabel;

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

    stage.setX( PositionManager.notesXProperty().get() );
    stage.setY( PositionManager.notesYProperty().get() );
    stage.xProperty().addListener( ( obs, o, xPos ) -> PositionManager.notesXProperty().setValue( xPos ) );
    stage.yProperty().addListener( ( obs, o, yPos ) -> PositionManager.notesYProperty().setValue( yPos ) );

    if (PositionManager.notesHeightProperty().get() != 0 || PositionManager.notesWidthProperty().get() != 0) {
      stage.setHeight( PositionManager.notesHeightProperty().get() );
      stage.setWidth( PositionManager.notesWidthProperty().get() );
    }
    stage.heightProperty().addListener( ( obs, o, xPos ) -> PositionManager.notesHeightProperty().setValue( xPos ) );
    stage.widthProperty().addListener( ( obs, o, xPos ) -> PositionManager.notesWidthProperty().setValue( xPos ) );

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
    textArea.requestFocus();
    textArea.textProperty().addListener( ( obs, old, newV ) -> manager.notes.set( newV ) );

    Timeline timer = new Timeline();
    timer.setCycleCount( 1 );
    KeyFrame kf = new KeyFrame( Duration.millis( 250 ), evt -> {
      boolean saved = manager.saveSuccessfulProperty.get();
      savedLabel.setVisible( saved );
      failedLabel.setVisible( !saved );
    } );
    timer.getKeyFrames().add( kf );

    textArea.textProperty().addListener( ( obs, old, newV ) -> {
      savedLabel.setVisible( false );
      failedLabel.setVisible( false );
      timer.stop();
      timer.setCycleCount( 1 );
      timer.play();
    } );

    savedLabel.setText( "Saved notes to " + RecordingManager.getFullFileName() + ".xls" );
    savedLabel.setVisible( false );
  }
}
