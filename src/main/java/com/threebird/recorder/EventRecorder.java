package com.threebird.recorder;

import java.io.IOException;
import java.util.Collection;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.threebird.recorder.controllers.AddKeysController;
import com.threebird.recorder.controllers.EditSchemaController;
import com.threebird.recorder.controllers.PreferencesController;
import com.threebird.recorder.controllers.RecordingController;
import com.threebird.recorder.controllers.SchemasController;
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
    toSchemasView( null );
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
  private static < T > T loadScene( String fxmlPath,
                                    String title )
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

  /**
   * Creates a "System MenuBar" and adds it to the Scene. Apparently, if it's a
   * "System MenuBar", the Stage will pick it up and use it globally for all
   * other Scenes that we insert into this Stage. How fucked up is that?
   */
  private static void createMainMenuBar()
  {
    MenuBar menuBar = new MenuBar();
    menuBar.setUseSystemMenuBar( true );

    Menu mainMenu = new Menu( "Event Recorder" );
    menuBar.getMenus().addAll( mainMenu );

    MenuItem prefs = new MenuItem( "Preferences" );
    mainMenu.getItems().add( prefs );

    prefs.setOnAction( ( evt ) -> {
      showPreferences();
    } );

    ((Pane) STAGE.getScene().getRoot()).getChildren().add( menuBar );
  }

  /**
   * load up the FXML file we generated with Scene Builder, "schemas.fxml". This
   * view is controlled by SchemasController.java
   */
  public static void toSchemasView( Schema selected )
  {
    String filepath = "./views/schemas.fxml";
    SchemasController controller = loadScene( filepath, "Schemas" );
    controller.init( selected );

    createMainMenuBar();
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

  private static Stage prefsStage = new Stage();

  /**
   * Shows the preferences page in a modal window
   */
  public static void showPreferences()
  {
    if (prefsStage == null) {
      prefsStage.initModality( Modality.APPLICATION_MODAL );
    }

    FXMLLoader fxmlLoader =
        new FXMLLoader( EventRecorder.class.getResource( "./views/preferences.fxml" ) );

    Parent root;
    try {
      root = (Parent) fxmlLoader.load();
    } catch (IOException e) {
      throw new RuntimeException( e );
    }
    Scene scene = new Scene( root );

    prefsStage.setTitle( "Preferences" );
    prefsStage.setScene( scene );
    prefsStage.show();

    fxmlLoader.< PreferencesController > getController().init( prefsStage );
  }
}
