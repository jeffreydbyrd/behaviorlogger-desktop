package com.threebird.recorder;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Strings;
import com.threebird.recorder.controllers.NewVersionController;
import com.threebird.recorder.controllers.StartMenuController;
import com.threebird.recorder.models.PositionManager;
import com.threebird.recorder.models.preferences.PreferencesManager;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.persistence.InitSQLiteTables;
import com.threebird.recorder.persistence.Recordings;
import com.threebird.recorder.persistence.Recordings.Writer;

/**
 * This is considered the main entry point by extending {@link Application}. The one method we must override is
 * start(Stage stage), which we invoke by calling the launch(...) method.
 */
public class EventRecorder extends Application
{
  /**
   * A static variable for the Stage so that it's easily accessible later when we want to switch scenes
   */
  public static Stage STAGE;

  public static String version = "0.3";

  public static void main( String[] args )
  {
    launch( args );

    // Cleanup background threads
    GsonUtils.es.shutdown();
    for (Writer writer : Recordings.Writer.values()) {
      writer.shutdown();
    }
  }

  @Override public void start( Stage primaryStage ) throws Exception
  {
    STAGE = primaryStage;

    InitSQLiteTables.init();

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

    if (PreferencesManager.getCheckVersion()) {
      checkVersion();
    }
  }

  private void checkVersion()
  {
    new Thread( ( ) -> {
      try {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
          HttpGet httpget = new HttpGet( "http://3birdsoftware.com/bl-version.txt" );

          ResponseHandler< String > responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
              HttpEntity entity = response.getEntity();
              return entity != null ? EntityUtils.toString( entity ) : null;
            }

            return null;
          };

          String v = httpclient.execute( httpget, responseHandler ).trim();

          if (Strings.isNullOrEmpty( v )
              || version.equals( v )
              || PreferencesManager.lastVersionCheckProperty().get().equals( v )) {
            return;
          }

          Platform.runLater( ( ) -> {
            // We only want to interrupt the user if they are on the start-menu
            if (STAGE.getTitle().equals( StartMenuController.TITLE )) {
              NewVersionController.show( v );
            }
          } );
        } finally {
          httpclient.close();
        }
      } catch (Exception e) {
        // Eat the Exception because this function isn't essential
        e.printStackTrace();
      }
    } ).start();
  }
}
