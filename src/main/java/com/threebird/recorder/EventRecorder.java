package com.threebird.recorder;

import java.io.IOException;
import java.util.Collection;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.threebird.recorder.controllers.AddKeysController;
import com.threebird.recorder.controllers.EditSchemaController;
import com.threebird.recorder.controllers.RecordingController;
import com.threebird.recorder.models.KeyBehaviorMapping;
import com.threebird.recorder.models.Schema;

/**
 * This is considered the main entry point by extending {@link Application}. The
 * one method we must override is start(Stage stage), which we invoke by calling
 * the launch(...) method.
 */
public class EventRecorder extends Application
{
  /**
   * A static variable for the Stage so that it's easily accessible later when
   * we want to switch scenes
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

  /**
   * Loads an FXML file into a new Scene and sets it in STAGE.
   * 
   * @param T
   *          - the type of the Controller you want returned to you
   * @param fxmlPath
   *          - the path (relative to EventRecorder.java's location) to the FXML
   *          resource from which we will derive this scene
   * @param title
   *          - the title of this new scene
   * @return the Controller linked to from the FXML file
   */
  private static < T > T loadScene( String fxmlPath, String title )
  {
    FXMLLoader fxmlLoader =
        new FXMLLoader( EventRecorder.class.getResource( fxmlPath ) );

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

  public static void requireConfirmation( String msg,
                                          String leftBtn,
                                          String rightBtn,
                                          EventHandler< ActionEvent > onLeft,
                                          EventHandler< ActionEvent > onRight )
  {
    Stage dialogStage = new Stage();
    dialogStage.initModality( Modality.WINDOW_MODAL );
    dialogStage.initStyle( StageStyle.UTILITY );
    dialogStage.initOwner( EventRecorder.STAGE );
    Label question = new Label( msg );
    question.setAlignment( Pos.BASELINE_CENTER );

    Button left = new Button( leftBtn );
    left.setOnAction( evt -> {
      dialogStage.close();
      onLeft.handle( evt );
    } );

    Button right = new Button( rightBtn );
    right.setOnAction( evt -> {
      dialogStage.close();
      onRight.handle( evt );
    } );

    HBox hBox = new HBox();
    hBox.setAlignment( Pos.BASELINE_RIGHT );
    hBox.setSpacing( 20.0 );
    hBox.getChildren().addAll( left, right );

    VBox vBox = new VBox();
    vBox.setSpacing( 20.0 );
    vBox.getChildren().addAll( question, hBox );
    vBox.setPadding( new Insets( 10 ) );

    dialogStage.setScene( new Scene( vBox ) );
    dialogStage.show();
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
   * 
   * @param schema
   *          - the currently selected schema if editing, or null if creating a
   *          new schema
   */
  public static void toEditSchemaView( Schema schema )
  {
    String filepath = "./views/edit_schema/edit_schema.fxml";
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
    String filepath = "./views/recording/recording.fxml";
    RecordingController controller = loadScene( filepath, "Recording" );
    controller.init( schema );
  }

  public static void toAddKeysView( Scene recordingScene,
                                    RecordingController recordingController,
                                    Schema schema,
                                    Collection< KeyBehaviorMapping > unknowns )
  {
    String filepath = "./views/add_keys.fxml";
    AddKeysController controller = loadScene( filepath, "Add Keys" );
    controller.init( recordingScene, recordingController, schema, unknowns );
  }
}
