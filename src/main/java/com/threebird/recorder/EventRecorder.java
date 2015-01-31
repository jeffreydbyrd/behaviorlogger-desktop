package com.threebird.recorder;

import javafx.application.Application;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import com.threebird.recorder.controllers.PreferencesController;
import com.threebird.recorder.controllers.StartMenuController;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.persistence.Recordings;
import com.threebird.recorder.utils.EventRecorderUtil;

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

    // Cleanup background threads
    GsonUtils.es.shutdown();
    Recordings.Writer.CSV.shutdown();
    Recordings.Writer.XLS.shutdown();
  }

  @Override public void start( Stage primaryStage ) throws Exception
  {
    try {
      STAGE = primaryStage;
      StartMenuController.toStartMenuView();
      createMainMenuBar();
    } catch (Exception e) {
      String msg = "";
      msg += e.getMessage();
      for (StackTraceElement el : e.getStackTrace()) {
        msg += "\n" + el.getClassName() + "." + el.getMethodName() + ": " + el.getLineNumber();
      }
      EventRecorderUtil.dialogBox( msg, "ok", "ok", evt -> {}, evt -> {} );
    }
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
      PreferencesController.showPreferences();
    } );

    ((Pane) STAGE.getScene().getRoot()).getChildren().add( menuBar );
  }
}
