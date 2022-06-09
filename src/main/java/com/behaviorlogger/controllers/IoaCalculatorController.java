package com.behaviorlogger.controllers;

import java.io.File;

import com.behaviorlogger.models.ioa.IoaManager;
import com.behaviorlogger.utils.Alerts;
import com.behaviorlogger.utils.BehaviorLoggerUtil;
import com.behaviorlogger.utils.ioa.IoaMethod;
import com.behaviorlogger.utils.ioa.ProcessIoa;
import com.google.common.base.Strings;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class IoaCalculatorController
{
  @FXML private TextField file1Field;
  @FXML private TextField file2Field;
  @FXML private Button browse1Btn;
  @FXML private Button browse2Btn;
  @FXML private ChoiceBox< IoaMethod > methodChoiceBox;
  @FXML private TextField thresholdField;
  @FXML private RadioButton newFileRadio;
  @FXML private RadioButton appendRadio;
  @FXML private VBox appendBox;
  @FXML private TextField appendField;
  @FXML private Button appendBrowseBtn;
  @FXML private Button generateBtn;
  @FXML private Label file1NotFoundLbl;
  @FXML private Label file2NotFoundLbl;
  @FXML private Label appendFileNotFoundLbl;
  @FXML private ScrollPane summaryBox;
  @FXML private Label saveStatusLbl;

  public static void showIoaCalculator()
  {
    String fxmlPath = "views/ioa/ioa-calculator.fxml";
    BehaviorLoggerUtil.showScene( fxmlPath, "IOA Calculator" );
  }

  @FXML private void initialize()
  {
    file1Field.setText( IoaManager.file1Property().get() );
    file2Field.setText( IoaManager.file2Property().get() );
    thresholdField.setText( IoaManager.thresholdProperty().get() + "" );

    methodChoiceBox.setItems( FXCollections.observableArrayList( IoaMethod.values() ) );
    methodChoiceBox.getSelectionModel().select( IoaManager.getSelectedMethod() );

    file1Field.textProperty().addListener( ( o, old, newV ) -> IoaManager.file1Property().set( newV ) );
    file2Field.textProperty().addListener( ( o, old, newV ) -> IoaManager.file2Property().set( newV ) );
    methodChoiceBox.getSelectionModel()
                   .selectedItemProperty()
                   .addListener( ( observable, oldValue, newValue ) -> {
                     IoaManager.methodProperty().set( newValue.name() );
                   } );

    BehaviorLoggerUtil.addIntegerListener(thresholdField, n -> {
	int val = n < 1 ? 1 : n;
	IoaManager.thresholdProperty().set(val);
    });
    thresholdField.textProperty().addListener( ( o, old, newV ) -> {
      int n = Strings.isNullOrEmpty( newV ) ? 1 : Integer.valueOf( newV );
      IoaManager.thresholdProperty().set( n );
    } );
    thresholdField.focusedProperty().addListener( ( o, old, focused ) -> {
      if (!focused) {
        String text = thresholdField.getText();
        if (Strings.isNullOrEmpty( text )) {
          thresholdField.setText( "1" );
        }
      }
    } );

    initSaveOptions();
  }

  private void initSaveOptions()
  {
    ToggleGroup group = new ToggleGroup();
    newFileRadio.setToggleGroup( group );
    appendRadio.setToggleGroup( group );
    appendRadio.selectedProperty().addListener( ( observable,
                                                  oldValue,
                                                  selected ) -> {
      IoaManager.appendSelectedProperty().setValue( selected );
      appendBox.setDisable( !selected );
    } );

    boolean appendFileSelected = IoaManager.appendSelectedProperty().getValue();
    appendBox.setDisable( !appendFileSelected );
    newFileRadio.setSelected( !appendFileSelected );
    appendRadio.setSelected( appendFileSelected );

    IoaManager.getAppendFile().ifPresent( appendField::setText );
    appendField.textProperty().addListener( ( o, old, newV ) -> IoaManager.appendFileProperty().set( newV ) );
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

  private File getAppendFile()
  {
    return getFile( appendField );
  }

  private void browseBtnPressed( TextField fileField,
                                 String filterDescription,
                                 String... filterExtension )
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
    ExtensionFilter extFilter = new FileChooser.ExtensionFilter( filterDescription, filterExtension );
    fileChooser.getExtensionFilters().add( extFilter );
    File newFile = fileChooser.showOpenDialog( BehaviorLoggerUtil.dialogStage.get() );

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
    File f3 = getAppendFile();

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

    if (appendRadio.isSelected() && !f3.exists()) {
      appendField.setStyle( cssRed );
      appendFileNotFoundLbl.setVisible( true );
      valid = false;
    } else {
      appendField.setStyle( "" );
      appendFileNotFoundLbl.setVisible( false );
    }

    return valid;
  }

  @FXML private void browse1BtnPressed()
  {
    browseBtnPressed( file1Field, "Raw data files (*.raw)", "*.raw" );
  }

  @FXML private void browse2BtnPressed()
  {
    browseBtnPressed( file2Field, "Raw data files (*.raw)", "*.raw" );
  }

  @FXML private void appendBtnPressed()
  {
    browseBtnPressed( appendField, "Excel files (*.xls, *.xlsx)", "*.xls", "*.xlsx" );
  }

  @FXML private void generateBtnPressed()
  {
    if (!validate()) {
      return;
    }

    File result;
    boolean appendToFile = IoaManager.appendSelectedProperty().get();
    if (appendToFile) {
      result = getAppendFile();
    } else {
      FileChooser fileChooser = new FileChooser();
      FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter( "XLS files (*.xls)", "*.xls" );
      fileChooser.getExtensionFilters().add( extFilter );
      result = fileChooser.showSaveDialog( BehaviorLoggerUtil.dialogStage.get() );
    }

    if (result == null) {
      return;
    }

    try {
      Pane summary = ProcessIoa.process( getFile1(),
                                         getFile2(),
                                         IoaManager.getSelectedMethod(),
                                         IoaManager.thresholdProperty().get(),
                                         appendToFile,
                                         result );
      summaryBox.setContent( summary );

      if (appendToFile) {
        saveStatusLbl.setText( "IOA results appended to: " + result.getAbsolutePath() );
      } else {
        saveStatusLbl.setText( "IOA results saved to new file: " + result.getAbsolutePath() );
      }
    } catch (Exception e) {
      Alerts.error( null, "IOA Calculator encountered a problem.", e );
      saveStatusLbl.setText( "Your IOA results may not have been saved." );
      e.printStackTrace();
    }
  }

  @FXML private void onCloseBtnPressed()
  {
    BehaviorLoggerUtil.dialogStage.get().close();
  }

  @FXML private void onHelpBtnPressed()
  {
    BehaviorLoggerUtil.openManual( "ioa-calculator" );
  }
}
