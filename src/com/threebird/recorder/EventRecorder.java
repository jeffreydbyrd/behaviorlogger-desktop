package com.threebird.recorder;

import java.io.IOException;

import com.threebird.recorder.controllers.RecordingController;
import com.threebird.recorder.models.Schema;

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
  public static Scene schemaScene;

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

    // Initialize the Schema view and keep it around in memory so we can go back
    // to it later
    schemaScene = new Scene( root );

    toSchemaView();
  }

  /**
   * Sets the stage to the Schema view
   * 
   * @param schema
   *          - the currently selected schema, or null if no schema is selected
   * @throws IOException
   */
  public static void toSchemaView() throws IOException
  {
    STAGE.setTitle( "Scheme Select" );
    STAGE.setScene( schemaScene );
    STAGE.show();
  }

  /**
   * Sets the stage to the Recording view
   * 
   * @param schema
   *          - the currently selected Schema. This parameter must not be null
   * @param duration
   *          - the duration, in seconds, of the session
   * @throws IOException
   */
  public static void toRecordingView( Schema schema, int duration )
      throws IOException
  {
    FXMLLoader fxmlLoader =
        new FXMLLoader( EventRecorder.class.getResource( "./views/recording.fxml" ) );

    Parent root = (Parent) fxmlLoader.load();
    Scene scene = new Scene( root );

    fxmlLoader.< RecordingController > getController().init( schema, duration );

    STAGE.setTitle( "Recording" );
    STAGE.setScene( scene );
    STAGE.show();
  }
}
