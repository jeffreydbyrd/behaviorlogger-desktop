package com.threebird.recorder;

import javafx.application.Application;
import javafx.stage.Stage;

import com.threebird.recorder.controllers.StartMenuController;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.persistence.Recordings;
import com.threebird.recorder.utils.EventRecorderUtil;
import com.threebird.recorder.utils.resources.ResourceUtils;

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
      ResourceUtils.getResources().mkdirs();

      STAGE = primaryStage;
      StartMenuController.toStartMenuView();
    } catch (Exception e) {
      String msg = "The program ran into a problem: " + e.getMessage();
      EventRecorderUtil.dialogBox( msg, "ok", evt -> {} );
      e.printStackTrace();
    }
  }
}
