package com.threebird.observer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EventRecorder extends Application
{
  public static Stage STAGE;

  public static void main( String[] args )
  {
    launch( args );
  }

  @Override public void start( Stage primaryStage ) throws Exception
  {
    STAGE = primaryStage;
    Parent root =
        FXMLLoader.load( EventRecorder.class.getResource( "./views/schemas.fxml" ) );

    Scene scene = new Scene( root );

    STAGE.setTitle( "Scheme Select" );
    STAGE.setScene( scene );
    STAGE.show();
  }
}
