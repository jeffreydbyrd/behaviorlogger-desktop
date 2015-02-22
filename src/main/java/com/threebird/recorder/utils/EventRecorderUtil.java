package com.threebird.recorder.utils;

import java.io.File;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.threebird.recorder.EventRecorder;

/**
 * A collection of helpful functions used throughout the app
 */
public class EventRecorderUtil
{

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
   * Loads an FXML file into a new Scene, which gets set in EventRecorder.STAGE.
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

  private static Stage dialogStage;

  /**
   * Display a simple dialog-box that displays a message and allows the user to
   * click two buttons
   * 
   * @param msg
   *          - the main message to display
   * @param leftBtn
   *          - the text displayed on the left-button
   * @param rightBtn
   *          - the text displayed on the right-button
   * @param onLeftClicked
   *          - what happens when user clicks left-button
   * @param onRightClicked
   *          - what happens when user clicks right-button
   */
  public static void dialogBox( String msg,
                                String leftBtn,
                                String rightBtn,
                                EventHandler< ActionEvent > onLeftClicked,
                                EventHandler< ActionEvent > onRightClicked )
  {
    if (dialogStage == null) {
      dialogStage = new Stage();
      dialogStage.initModality( Modality.APPLICATION_MODAL );
      dialogStage.initStyle( StageStyle.UTILITY );
      dialogStage.initOwner( EventRecorder.STAGE );
    }

    Label question = new Label( msg );
    question.setAlignment( Pos.BASELINE_CENTER );

    HBox hBox = new HBox();
    hBox.setAlignment( Pos.BASELINE_RIGHT );
    hBox.setSpacing( 20.0 );

    if (onLeftClicked != null && leftBtn != null) {
      Button left = new Button( leftBtn );
      left.setOnAction( evt -> {
        dialogStage.close();
        onLeftClicked.handle( evt );
      } );
      hBox.getChildren().add( left );
    }

    Button right = new Button( rightBtn );
    right.setOnAction( evt -> {
      dialogStage.close();
      onRightClicked.handle( evt );
    } );
    hBox.getChildren().add( right );

    VBox vBox = new VBox();
    vBox.setSpacing( 20.0 );
    vBox.getChildren().addAll( question, hBox );
    vBox.setPadding( new Insets( 10 ) );

    dialogStage.setScene( new Scene( vBox ) );
    dialogStage.show();
  }

  public static void dialogBox( String msg,
                                String button,
                                EventHandler< ActionEvent > onBtnClicked )
  {
    dialogBox( msg, null, button, null, onBtnClicked );
  }

  public static int strToInt( String s )
  {
    return Integer.valueOf( s.isEmpty() ? "0" : s );
  }

  public static String intToStr( int i )
  {
    return i > 0 ? i + "" : "";
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
}
