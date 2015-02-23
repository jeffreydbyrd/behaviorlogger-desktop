package com.threebird.recorder.controllers;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import com.threebird.recorder.utils.EventRecorderUtil;
import com.threebird.recorder.utils.ioa.IoaUtils;

public class IoaCalculatorController
{
  @FXML private TextField file1Field;
  @FXML private TextField file2Field;
  @FXML private Button browse1Btn;
  @FXML private Button browse2Btn;
  @FXML private Button generateBtn;
  @FXML private Label file1NotFoundLbl;
  @FXML private Label file2NotFoundLbl;

  public static void showIoaCalculator()
  {
    String fxmlPath = "views/ioa-calculator.fxml";
    EventRecorderUtil.showScene( fxmlPath, "IOA Calculator" );
  }

  private File getFile( TextField fileField )
  {
    return new File( fileField.getText().trim() );
  }

  private File getFile1()
  {
    return getFile( file1Field );
  }

  private File getFile2()
  {
    return getFile( file2Field );
  }

  private void browseBtnPressed( TextField fileField )
  {
    File f = getFile( fileField );
    if (!f.exists()) {
      f = new File( System.getProperty( "user.home" ) );
    } else {
      if (!f.isDirectory()) {
        f = f.getParentFile(); // if not a directory, get the parent directory
      }
    }

    FileChooser fileChooser = new FileChooser();
    fileChooser.setInitialDirectory( f );
    ExtensionFilter extFilter = new FileChooser.ExtensionFilter( "CSV files (*.csv)", "*.csv" );
    fileChooser.getExtensionFilters().add( extFilter );
    File newFile = fileChooser.showOpenDialog( EventRecorderUtil.dialogStage.get() );

    if (newFile != null) {
      fileField.setText( newFile.getPath() );
    }
  }

  private boolean validate()
  {
    String cssRed = "-fx-background-color:#FFDDDD;-fx-border-color: #f00;";
    boolean valid = true;

    File f1 = getFile1();
    File f2 = getFile2();

    if (!f1.exists()) {
      file1Field.setStyle( cssRed );
      file1NotFoundLbl.setVisible( true );
      valid = false;
    } else {
      file1Field.setStyle( "" );
      file1NotFoundLbl.setVisible( false );
    }

    if (!f2.exists()) {
      file2Field.setStyle( cssRed );
      file2NotFoundLbl.setVisible( true );
      valid = false;
    } else {
      file2Field.setStyle( "" );
      file2NotFoundLbl.setVisible( false );
    }

    return valid;
  }

  @FXML private void browse1BtnPressed()
  {
    browseBtnPressed( file1Field );
  }

  @FXML private void browse2BtnPressed()
  {
    browseBtnPressed( file2Field );
  }

  @FXML private void generateBtnPressed()
  {
    if (!validate()) {
      return;
    }

    File f1 = getFile1();
    File f2 = getFile2();

    try {
      File result = IoaUtils.compare( f1, f2 );
    } catch (IOException e) {
      throw new RuntimeException( e );
    }
  }

  @FXML private void onCloseBtnPressed()
  {
    EventRecorderUtil.dialogStage.get().close();
  }
}
