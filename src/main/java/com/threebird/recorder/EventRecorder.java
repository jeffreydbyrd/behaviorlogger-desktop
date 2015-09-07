package com.threebird.recorder;

import javafx.application.Application;
import javafx.stage.Stage;

import com.threebird.recorder.controllers.StartMenuController;
import com.threebird.recorder.models.PositionManager;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.persistence.Recordings;

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
    STAGE = primaryStage;

    STAGE.setX( PositionManager.mainXProperty().doubleValue() );
    STAGE.setY( PositionManager.mainYProperty().doubleValue() );
    STAGE.xProperty().addListener( ( obs, old, xpos ) -> PositionManager.mainXProperty().setValue( xpos ) );
    STAGE.yProperty().addListener( ( obs, old, ypos ) -> PositionManager.mainYProperty().setValue( ypos ) );

    if (PositionManager.mainHeightProperty().doubleValue() != 0
        || PositionManager.mainWidthProperty().doubleValue() != 0) {
      STAGE.setHeight( PositionManager.mainHeightProperty().get() );
      STAGE.setWidth( PositionManager.mainWidthProperty().get() );
    }
    STAGE.heightProperty().addListener( ( obs, old, h ) -> PositionManager.mainHeightProperty().setValue( h ) );
    STAGE.widthProperty().addListener( ( obs, old, w ) -> PositionManager.mainWidthProperty().setValue( w ) );

    StartMenuController.toStartMenuView();
  }
}
