package com.threebird.scratch;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class HelloWorld extends Application
{
  public static void main( String[] args )
  {
    Application.launch( args );
  }

  @Override public void start( Stage primaryStage ) throws Exception
  {
    primaryStage.setTitle( "Hello World!" );
    Button btn = new Button();
    btn.setText( "Say 'Hello World'" );

    btn.setOnAction( ( ActionEvent evt ) -> {
      System.out.println( "Hello World!" );
    } );

    StackPane root = new StackPane();
    root.getChildren().add( btn );
    primaryStage.setScene( new Scene( root, 300, 250 ) );
    primaryStage.show();
  }
}
