package com.threebird.recorder.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.utils.resources.ResourceUtils;

/**
 * A collection of helpful functions used throughout the app
 */
public class EventRecorderUtil
{

  public static Supplier< Stage > dialogStage = Suppliers.memoize( ( ) -> {
    Stage s = new Stage();
    s.initModality( Modality.APPLICATION_MODAL );
    s.initStyle( StageStyle.UTILITY );
    s.initOwner( EventRecorder.STAGE );
    return s;
  } );

  /**
   * @param window
   *          - the containing window that will own the FileChooser
   * @param textField
   *          - the TextField that we will populate after the user chooses a
   *          file
   */
  public static void chooseFile( Stage window, TextField textField )
  {
    File f = new File( textField.getText().trim() );
    if (!f.exists()) {
      f = new File( System.getProperty( "user.home" ) );
    }

    DirectoryChooser dirChooser = new DirectoryChooser();
    dirChooser.setInitialDirectory( f );
    File newFile = dirChooser.showDialog( window );

    if (newFile != null) {
      textField.setText( newFile.getPath() );
    }
  }

  /**
   * Displays a dialog window containing the resource specified by fxmlPath
   * 
   * @param stage
   *          - the stage that will house the new scene
   * @param fxmlPath
   *          - the file path to the fxml template (relative to
   *          EventRecorder.java)
   * @param title
   *          - the title of the scene
   * @return the controller T associated with the fxml resource specified
   */
  public static < T > T showScene( String fxmlPath, String title )
  {
    FXMLLoader fxmlLoader =
        new FXMLLoader( EventRecorder.class.getResource( fxmlPath ) );

    Parent root;
    try {
      root = (Parent) fxmlLoader.load();
    } catch (IOException e) {
      Alerts.error( "Error Loading Resource", "There was a problem loading a resource: " + fxmlPath, e );
      throw new RuntimeException( e );
    }
    Scene scene = new Scene( root );

    dialogStage.get().setTitle( title );
    dialogStage.get().setScene( scene );
    dialogStage.get().show();

    return fxmlLoader.< T > getController();
  }

  /**
   * Loads an FXML file into EventRecorder.STAGE's current scene
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
  public static < T > T loadScene( String fxmlPath,
                                   String title )
  {
    FXMLLoader fxmlLoader =
        new FXMLLoader( EventRecorder.class.getResource( fxmlPath ) );

    Parent root;
    try {
      root = (Parent) fxmlLoader.load();
    } catch (IOException e) {
      Alerts.error( "Error Loading Resource", "There was a problem loading a resource: " + fxmlPath, e );
      throw new RuntimeException( e );
    }

    Scene scene = EventRecorder.STAGE.getScene();
    if (scene == null) {
      scene = new Scene( root );
      EventRecorder.STAGE.setScene( scene );
    } else {
      scene.setRoot( root );
    }

    EventRecorder.STAGE.setTitle( title );
    EventRecorder.STAGE.show();

    return fxmlLoader.< T > getController();
  }

  /**
   * Creates an EventHandler that will prevent a field's text from containing
   * any characters not in "acceptableKeys" and from exceeding the character
   * limit
   * 
   * @param field
   *          - the input-field that this EventHandler reads
   * @param acceptableKeys
   *          - a list of characters that the user is allowed to input
   * @param limit
   *          - the max length of the field
   * @return an EventHandler that consumes a KeyEvent if the typed Char is
   *         outside 'acceptableKeys' or if the length of 'field' is longer than
   *         'limit'
   */
  public static EventHandler< ? super KeyEvent >
    createFieldLimiter( TextField field, char[] acceptableKeys, int limit )
  {
    return evt -> {
      if (field.getText().trim().length() == limit
          || !String.valueOf( acceptableKeys ).contains( evt.getCharacter() ))
        evt.consume();
    };
  }

  public static int strToInt( String s )
  {
    return Integer.valueOf( Strings.isNullOrEmpty( s ) ? "0" : s );
  }

  public static String intToStr( int i )
  {
    return i > 0 ? i + "" : "";
  }

  /**
   * @param timestamp
   *          - String in the form of 'hh:mm:ss'
   * @return the equivalent number of seconds
   */
  public static int getDuration( String timestamp )
  {
    String[] split = timestamp.split( " *: *" );
    int hrs = EventRecorderUtil.strToInt( split[0] );
    int min = EventRecorderUtil.strToInt( split[1] );
    int sec = EventRecorderUtil.strToInt( split[2] );

    return (hrs * 60 * 60) + (min * 60) + sec;
  }

  /**
   * Converts the contents of hoursField, minutesField, and secondsField into
   * the equivalent number of seconds. If "isInfinite" is true, then returns 0
   */
  public static int getDuration( boolean isInfinite,
                                 TextField hoursField,
                                 TextField minutesField,
                                 TextField secondsField )
  {
    if (isInfinite) {
      return 0;
    }

    Integer hours = strToInt( hoursField.getText() );
    Integer mins = strToInt( minutesField.getText() );
    Integer secs = strToInt( secondsField.getText() );
    return (hours * 60 * 60) + (mins * 60) + secs;
  }

  public static String millisToTimestampNoSpaces( int totalMillis )
  {
    String withSpaces = secondsToTimestamp( totalMillis / 1000 );
    return withSpaces.replaceAll( " *", "" );
  }

  public static String millisToTimestamp( int totalMillis )
  {
    return secondsToTimestamp( totalMillis / 1000 );
  }

  public static String secondsToTimestamp( Integer totalSeconds )
  {
    int remaining = totalSeconds % (60 * 60);
    int hours = totalSeconds / (60 * 60);
    int minutes = remaining / 60;
    int seconds = remaining % 60;

    return String.format( "%02d : %02d : %02d", hours, minutes, seconds );
  }

  public static double millisToMinutes( int millis )
  {
    return ((double) millis) / (1000 * 60.0);
  }

  public static void openManual( String section )
  {
    String filepath = ResourceUtils.getManual().getAbsolutePath();
    try {
      URI uri = new URI( "file", null, filepath, section );
      Desktop.getDesktop().browse( uri );
    } catch (Exception e) {
      Alerts.error( "Error Loading Resource", "There was a problem loading a resource: " + filepath, e );
    }
  }
}
