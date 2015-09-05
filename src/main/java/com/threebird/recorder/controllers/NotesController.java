package com.threebird.recorder.controllers;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.utils.Alerts;

public class NotesController
{
  public static void bindNotesToStage(Stage stage)
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

    fxmlLoader.< NotesController > getController().init();
  }

  private void init()
  {
    System.out.println( "hello notes!" );
  }
}
