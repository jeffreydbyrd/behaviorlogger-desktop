package com.threebird.recorder.controllers;

import java.io.File;

import com.google.common.base.Strings;
import com.threebird.recorder.utils.BehaviorLoggerUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class ConditionalProbabilityController
{
  @FXML private TextField fileField;
  @FXML private Button browseBtn;
  @FXML private Label fileNotFoundLbl;

  public static void showCalculator()
  {
    String fxmlPath = "views/prob-calculator.fxml";
    BehaviorLoggerUtil.showScene( fxmlPath, "Conditional Probability Calculator" );
  }

  @FXML private void browseBtnPressed()
  {
     BehaviorLoggerUtil.browseBtnPressed( this.fileField, "Raw data files (*.raw)", "*.raw" );
  }
}
