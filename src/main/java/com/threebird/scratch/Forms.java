package com.threebird.scratch;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Forms extends Application
{

  private static boolean showGrid = false;

  public static void main( String[] args )
  {
    Application.launch( args );
  }

  @Override public void start( Stage primaryStage ) throws Exception
  {
    primaryStage.setTitle( "JavaFX Welcome" );

    // setup our grid:
    GridPane grid = new GridPane();
    grid.setGridLinesVisible( showGrid );

    // show Welcome message
    Text scenetitle = new Text( "Welcome" );
    scenetitle.setId( "welcome-text" );
    grid.add( scenetitle, 0, 0, 2, 1 );

    // Set username label
    HBox username = new HBox();
    username.getChildren().add( new Label( "Username:" ) );
    grid.add( username, 0, 1 );

    // Set password label
    HBox pw = new HBox();
    pw.getChildren().add( new Label( "Password:" ) );
    grid.add( pw, 0, 2 );

    // set username text field
    TextField userTextField = new TextField();
    grid.add( userTextField, 1, 1 );

    // set password text field
    PasswordField pwBox = new PasswordField();
    grid.add( pwBox, 1, 2 );

    // set "sign in" button
    Button btn = new Button( "Sign in" );
    HBox hbBtn = new HBox();
    hbBtn.getChildren().add( btn );
    grid.add( hbBtn, 1, 4 );

    Text actiontarget = new Text();
    actiontarget.setId( "actiontarget" );
    grid.add( actiontarget, 1, 6 );

    btn.setOnAction( evt -> actiontarget.setText( "Sign in button pressed" ) );

    Scene scene = new Scene( grid, 300, 275 );
    primaryStage.setScene( scene );

    scene.getStylesheets()
         .add( Forms.class.getResource( "login.css" )
                          .toExternalForm() );

    primaryStage.show();
  }
}
