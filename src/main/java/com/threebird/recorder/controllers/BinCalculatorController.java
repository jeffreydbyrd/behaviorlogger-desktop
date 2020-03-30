package com.threebird.recorder.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.base.Strings;
import com.threebird.recorder.models.BinManager;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.persistence.WriteBinIntervals;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_1.SessionBean1_1;
import com.threebird.recorder.utils.Alerts;
import com.threebird.recorder.utils.BehaviorLoggerUtil;
import com.threebird.recorder.utils.ioa.KeyToInterval;
import com.threebird.recorder.utils.ioa.version1_1.IoaUtils1_1;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class BinCalculatorController
{

  @FXML private TextField fileField;
  @FXML private Button browseBtn;
  @FXML private Label fileNotFoundLbl;
  @FXML private RadioButton newFileRadio;
  @FXML private RadioButton appendRadio;
  @FXML private TextField binsizeField;
  @FXML private VBox appendBox;
  @FXML TextField appendField;
  @FXML private Button appendBrowseBtn;
  @FXML private Label appendFileNotFoundLbl;
  @FXML Label saveStatusLbl;
  @FXML Button generateBtn;
  @FXML Button helpButton;

  public static void showBinCalculator()
  {
    String fxmlPath = "views/bin-calculator.fxml";
    BehaviorLoggerUtil.showScene( fxmlPath, "Data Stream Bin Calculator" );
  }

  @FXML private void initialize()
  {
    fileField.setText( BinManager.fileProperty().get() );
    binsizeField.setText( BinManager.binsizeProperty().get() + "" );
    char[] digits = "0123456789".toCharArray();
    binsizeField.setOnKeyTyped( BehaviorLoggerUtil.createFieldLimiter( digits, 5 ) );
    fileField.textProperty().addListener( ( o, old, newV ) -> BinManager.fileProperty().set( newV ) );

    binsizeField.textProperty().addListener( ( o, old, newV ) -> {
      int n = Strings.isNullOrEmpty( newV ) ? 1 : Integer.valueOf( newV );
      BinManager.binsizeProperty().set( n );
    } );

    ToggleGroup group = new ToggleGroup();
    newFileRadio.setToggleGroup( group );
    appendRadio.setToggleGroup( group );
    appendRadio.selectedProperty().addListener( ( observable, oldValue, selected ) -> {
      BinManager.appendSelectedProperty().setValue( selected );
      appendBox.setDisable( !selected );
    } );

    boolean appendFileSelected = BinManager.appendSelectedProperty().getValue();
    appendBox.setDisable( !appendFileSelected );
    newFileRadio.setSelected( !appendFileSelected );
    appendRadio.setSelected( appendFileSelected );
    BinManager.getAppendFile().ifPresent( appendField::setText );
    appendField.textProperty().addListener( ( o, old, newV ) -> BinManager.appendFileProperty().set( newV ) );
  }

  @FXML private void browseBtnPressed()
  {
    browseBtnPressed( this.fileField, "Raw data files (*.raw)", "*.raw" );
  }

  @FXML private void appendBtnPressed()
  {
    browseBtnPressed( appendField, "Excel files (*.xls, *.xlsx)", "*.xls", "*.xlsx" );
  }

  @FXML private void onCloseBtnPressed()
  {
    BehaviorLoggerUtil.dialogStage.get().close();
  }

  @FXML private void onHelpBtnPressed()
  {
    // BehaviorLoggerUtil.openManual( "ioa-calculator" );
  }

  @FXML private void generateBtnPressed()
  {
    if (!validate()) {
      return;
    }

    File result;
    boolean appendToFile = BinManager.appendSelectedProperty().get();
    if (appendToFile) {
      result = getFile( appendField );
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
      write( result );

      if (appendToFile) {
        saveStatusLbl.setText( "Bin results appended to: " + result.getAbsolutePath() );
      } else {
        saveStatusLbl.setText( "Bin results saved to new file: " + result.getAbsolutePath() );
      }
    } catch (Exception e) {
      Alerts.error( null, "Bin Calculator encountered a problem.", e );
      saveStatusLbl.setText( "Bin results may not have been saved." );
      e.printStackTrace();
    }
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

  private File getFile( TextField fileField )
  {
    String text = Strings.nullToEmpty( fileField.getText() ).trim();
    return new File( text );
  }

  private boolean validate()
  {
    String cssRed = "-fx-background-color:#FFDDDD;-fx-border-color: #f00;";
    boolean valid = true;

    File f1 = getFile( fileField );
    File f2 = getFile( appendField );

    if (!f1.exists()) {
      fileField.setStyle( cssRed );
      fileNotFoundLbl.setVisible( true );
      valid = false;
    } else {
      fileField.setStyle( "" );
      fileNotFoundLbl.setVisible( false );
    }

    if (appendRadio.isSelected() && !f2.exists()) {
      appendField.setStyle( cssRed );
      appendFileNotFoundLbl.setVisible( true );
      valid = false;
    } else {
      appendField.setStyle( "" );
      appendFileNotFoundLbl.setVisible( false );
    }

    return valid;
  }

  private void write( File result ) throws Exception
  {
    File rawFile = this.getFile( this.fileField );
    boolean appendToFile = BinManager.appendSelectedProperty().get();
    int binsize = BinManager.binsizeProperty().get() < 1 ? 1 : BinManager.binsizeProperty().get();
    SessionBean1_1 dataStream = GsonUtils.get( rawFile, new SessionBean1_1() );
    HashMap< String, ArrayList< Integer > > map1 = IoaUtils1_1.createIoaMap( dataStream );
    KeyToInterval partitioned = IoaUtils1_1.partition( map1, dataStream.duration, binsize );
    WriteBinIntervals.write( partitioned, appendToFile, result );
  }
}
