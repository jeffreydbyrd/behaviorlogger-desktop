package com.threebird.recorder.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class Alerts
{
  private static void alert( Alert.AlertType type, String title, String header, String message, Runnable onOk )
  {
    Alert alert = new Alert( type );
    alert.setTitle( title );
    alert.setHeaderText( header );
    alert.setContentText( message );
    alert.showAndWait()
         .filter( result -> result == ButtonType.OK )
         .ifPresent( r -> onOk.run() );
  }

  /**
   * Displays an Error Alert dialog box that executes the 'onOk' Runnable after
   * clicking "Ok"
   */
  public static void error( String title, String header, Exception cause, Runnable onOk )
  {
    alert( Alert.AlertType.ERROR, title, header, cause.getMessage(), onOk );
  }

  /**
   * Displays an Error Alert dialog box that takes no action upon clicking "Ok"
   */
  public static void error( String title, String header, Exception cause )
  {
    error( title, header, cause, ( ) -> {} );
  }

  /**
   * Displays a Confirmation dialog box that executes the 'onOk' Runnable after
   * clicking "Ok"
   */
  public static void confirm( String title, String header, String message, Runnable onOk )
  {
    alert( Alert.AlertType.CONFIRMATION, title, header, message, onOk );
  }
}
