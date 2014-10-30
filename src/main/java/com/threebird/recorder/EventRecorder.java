package com.threebird.recorder;

import java.io.IOException;

import com.threebird.recorder.controllers.EditSchemaController;
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

  public static void main( String[] args )
  {
    launch( args );
  }

  @Override public void start( Stage primaryStage ) throws Exception
  {
    STAGE = primaryStage;

    toSchemasView();
  }

  private static < T > T loadScene( String filepath, String title )
  {
    FXMLLoader fxmlLoader =
        new FXMLLoader( EventRecorder.class.getResource( filepath ) );

    Parent root;
    try {
      root = (Parent) fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException( e );
    }
    Scene scene = new Scene( root );

    STAGE.setTitle( title );
    STAGE.setScene( scene );
    STAGE.show();

    return fxmlLoader.< T > getController();
  }

  /**
   * load up the FXML file we generated with Scene Builder, "schemas.fxml". This
   * view is controlled by SchemasController.java
   */
  public static void toSchemasView()
  {
    String filepath = "./views/schemas.fxml";
    loadScene( filepath, "Schemas" );
  }

  /**
   * Sets the stage to the EditScema view.
   */
  public static void toEditSchemaView( Schema schema )
  {
    String filepath = "./views/edit_schema.fxml";
    EditSchemaController controller = loadScene( filepath, "Create Schema" );
    controller.init( schema );
  }

  /**
   * Sets the stage to the Recording view
   * 
   * @param schema
   *          - the currently selected Schema. This parameter must not be null
   */
  public static void toRecordingView( Schema schema )
  {
    String filepath = "./views/recording.fxml";
    RecordingController controller = loadScene( filepath, "Recording" );
    controller.init( schema );
  }
}
