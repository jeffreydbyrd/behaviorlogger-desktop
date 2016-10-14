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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.threebird.recorder.BehaviorLoggerApp;
import com.threebird.recorder.models.PositionManager;
import com.threebird.recorder.models.sessions.RecordingManager;
import com.threebird.recorder.utils.Alerts;
import com.threebird.recorder.utils.BehaviorLoggerUtil;

public class NotesController
{
  @FXML private TextArea textArea;
  @FXML private Label savedLabel;
  @FXML private Label failedLabel;
  private Stage stage;
  private RecordingManager manager;

  public static void bindNotesToStage( Stage stage, RecordingManager manager )
  {
    String fxmlPath = "views/recording/notes.fxml";

    FXMLLoader fxmlLoader =
        new FXMLLoader( BehaviorLoggerApp.class.getResource( fxmlPath ) );

    Parent root;
    try {
      root = (Parent) fxmlLoader.load();
    } catch (IOException e) {
      Alerts.error( "Error Loading Resource", "There was a problem loading a resource: " + fxmlPath, e );
      e.printStackTrace();
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
        BehaviorLoggerApp.STAGE.getScene().rootProperty().removeListener( this );
      }
    };
    BehaviorLoggerApp.STAGE.getScene().rootProperty().addListener( onViewChange );

    fxmlLoader.< NotesController > getController().init( stage, manager );
  }

  private void init( Stage stage, RecordingManager manager )
  {
    this.stage = stage;
    this.manager = manager;

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

    savedLabel.setText( "Notes saved!" );
    savedLabel.setVisible( false );
  }

  @FXML private void onKeyPressed( KeyEvent evt )
  {
    KeyCode code = evt.getCode();

    // The ESCAPE keyboard shortcut will simply close the notes stage
    if (code == KeyCode.ESCAPE) {
      this.stage.close();
      return;
    }

    // Ctrl/cmd+T inserts the current session timestamp
    if (evt.isShortcutDown() && code == KeyCode.T) {
      int index = textArea.getCaretPosition();
      int count = manager.count();
      String timestamp = BehaviorLoggerUtil.millisToTimestamp( count );
      String msg = String.format( "[%s] ", timestamp );
      textArea.insertText( index, msg );
      return;
    }
  }
}
