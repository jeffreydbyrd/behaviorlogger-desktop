package com.threebird.recorder.controllers;

import java.awt.Desktop;
import java.net.URI;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

import com.threebird.recorder.BehaviorLoggerApp;
import com.threebird.recorder.models.preferences.PreferencesManager;
import com.threebird.recorder.utils.BehaviorLoggerUtil;

public class NewVersionController
{
  public static final String URL = "https://behaviorlogger.com/static/apps/download/app.html";
  
  @FXML Label currVersionLbl;
  @FXML Label newVersionLbl;
  @FXML CheckBox stopChecking;

  private String newVersion;

  public static void show( String newVersion )
  {
    String fxmlPath = "views/new-version.fxml";
    NewVersionController controller = BehaviorLoggerUtil.showScene( fxmlPath, "New Version" );
    controller.init( newVersion );
  }

  private void init( String newVersion )
  {
    this.newVersion = newVersion;
    this.currVersionLbl.setText( "Current Version: " + BehaviorLoggerApp.version );
    this.newVersionLbl.setText( "New Version: " + this.newVersion );

    stopChecking.selectedProperty().addListener( ( obs, old, selected ) -> {
      PreferencesManager.checkVersionProperty().set( !selected );
    } );
  }

  @FXML private void onHyperlinkClick() throws Exception
  {
    URI uri = new URI( URL );
    Desktop.getDesktop().browse( uri );
  }

  @FXML private void onClosePressed()
  {
    PreferencesManager.lastVersionCheckProperty().set( newVersion );
    BehaviorLoggerUtil.dialogStage.get().close();
  }
}
