package com.threebird.recorder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This is considered the main entry point by extending {@link Application}. The
 * one method we must override is start(Stage stage), which we invoke by calling
 * the launch(...) method.
 */
public class EventRecorder extends Application
{

  /**
   * We define a global, static variable for the Stage so that it's easily
   * accessible across the whole app
   */
  public static Stage STAGE;

  public static void main( String[] args )
  {
    launch( args );
  }

  @Override public void start( Stage primaryStage ) throws Exception
  {
    STAGE = primaryStage;

    // load up the FXML file we generated with Scene Builder, "schemas.fxml".
    // This view is controlled by SchemasController.java
    Parent root =
        FXMLLoader.load( EventRecorder.class.getResource( "./views/schemas.fxml" ) );

    // Set it as the root
    Scene scene = new Scene( root );

    STAGE.setTitle( "Scheme Select" );
    STAGE.setScene( scene );
    STAGE.show();

    // Later we can swap out the scene with others.
  }
}
