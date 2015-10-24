package com.threebird.recorder.controllers;

import java.awt.Desktop;
import java.net.URI;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;

import com.threebird.recorder.EventRecorder;
import com.threebird.recorder.models.preferences.PreferencesManager;
import com.threebird.recorder.utils.EventRecorderUtil;

public class NewVersionController
{
  public static final String URL = "http://3birdsoftware.com/html/behaviorlogger.html";
  
  @FXML Label currVersionLbl;
  @FXML Label newVersionLbl;
  @FXML CheckBox stopChecking;

  private String newVersion;

  public static void show( String newVersion )
  {
    String fxmlPath = "views/new-version.fxml";
    NewVersionController controller = EventRecorderUtil.showScene( fxmlPath, "New Version" );
    controller.init( newVersion );
  }

  private void init( String newVersion )
  {
    this.newVersion = newVersion;
    this.currVersionLbl.setText( "Current Version: " + EventRecorder.version );
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
    EventRecorderUtil.dialogStage.get().close();
  }
}
