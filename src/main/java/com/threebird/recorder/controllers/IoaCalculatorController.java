package com.threebird.recorder.controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.threebird.recorder.models.ioa.IoaManager;
import com.threebird.recorder.utils.EventRecorderUtil;
import com.threebird.recorder.utils.ioa.IoaMethod;
import com.threebird.recorder.utils.ioa.IoaUtils;

public class IoaCalculatorController
{
  @FXML private TextField file1Field;
  @FXML private TextField file2Field;
  @FXML private Button browse1Btn;
  @FXML private Button browse2Btn;
  @FXML private ChoiceBox< IoaMethod > methodChoiceBox;
  @FXML private TextField thresholdField;
  @FXML private Button generateBtn;
  @FXML private Label file1NotFoundLbl;
  @FXML private Label file2NotFoundLbl;

  public static void showIoaCalculator()
  {
    String fxmlPath = "views/ioa-calculator.fxml";
    EventRecorderUtil.showScene( fxmlPath, "IOA Calculator" );
  }

  @FXML private void initialize()
  {

    file1Field.setText( IoaManager.file1Property().get() );
    file2Field.setText( IoaManager.file2Property().get() );
    thresholdField.setText( IoaManager.thresholdProperty().get() + "" );
    char[] digits = "0123456789".toCharArray();
    thresholdField.setOnKeyTyped( EventRecorderUtil.createFieldLimiter( thresholdField, digits, 5 ) );

    methodChoiceBox.setItems( FXCollections.observableArrayList( IoaMethod.values() ) );
    methodChoiceBox.getSelectionModel().select( IoaManager.getSelectedMethod() );

    file1Field.textProperty().addListener( ( o, old, newV ) -> IoaManager.file1Property().set( newV ) );
    file2Field.textProperty().addListener( ( o, old, newV ) -> IoaManager.file2Property().set( newV ) );
    methodChoiceBox.getSelectionModel()
                   .selectedItemProperty()
                   .addListener( ( observable, oldValue, newValue ) -> {
                     IoaManager.methodProperty().set( newValue.name() );
                   } );
    thresholdField.textProperty().addListener( ( o, old, newV ) -> {
      int n = Strings.isNullOrEmpty( newV ) ? 1 : Integer.valueOf( newV );
      IoaManager.thresholdProperty().set( n );
    } );
    thresholdField.focusedProperty().addListener( ( o, old, focused ) -> {
      if (!focused) {
        String text = thresholdField.getText();
        if (Strings.isNullOrEmpty( text )) {
          thresholdField.setText( "0" );
        }
      }
    } );
  }

  private File getFile( TextField fileField )
  {
    String text = Strings.nullToEmpty( fileField.getText() ).trim();
    return new File( text );
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

    FileChooser fileChooser = new FileChooser();
    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter( "XLS files (*.xls)", "*.xls" );
    fileChooser.getExtensionFilters().add( extFilter );
    File result = fileChooser.showSaveDialog( EventRecorderUtil.dialogStage.get() );

    try {
      IoaUtils.process( getFile1(),
                        getFile2(),
                        IoaManager.getSelectedMethod(),
                        IoaManager.thresholdProperty().get(),
                        result );
    } catch (IOException e) {
      throw new RuntimeException( e );
    }
  }

  @FXML private void onCloseBtnPressed()
  {
    EventRecorderUtil.dialogStage.get().close();
  }
}
