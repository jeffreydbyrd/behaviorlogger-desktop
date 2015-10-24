package com.threebird.recorder.controllers;

import javafx.fxml.FXML;

import com.threebird.recorder.utils.EventRecorderUtil;

public class NewVersionController
{
  public static void show( String newVersion )
  {
    String fxmlPath = "views/new-version.fxml";
    EventRecorderUtil.showScene( fxmlPath, "New Version" );
  }

  @FXML private void onClosePressed()
  {
    EventRecorderUtil.dialogStage.get().close();
  }
}
