package com.behaviorlogger.controllers;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import com.behaviorlogger.models.preferences.FilenameComponent;
import com.behaviorlogger.models.preferences.PreferencesManager;
import com.behaviorlogger.utils.BehaviorLoggerUtil;
import com.behaviorlogger.views.preferences.FilenameComponentView;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * Corresponds to preferences.fxml
 */
public class PreferencesController
{
  @FXML private TextField directoryField;
  @FXML private Button browseButton;

  @FXML private Label exampleLbl;
  @FXML private VBox componentsBox;

  @FXML private RadioButton infiniteRadioBtn;
  @FXML private RadioButton timedRadioBtn;

  @FXML private CheckBox colorCheckBox;
  @FXML private CheckBox pauseCheckBox;
  @FXML private CheckBox beepCheckBox;

  @FXML private VBox durationBox;

  @FXML private TextField hoursField;
  @FXML private TextField minutesField;
  @FXML private TextField secondsField;

  @FXML private CheckBox checkVersion;

  @FXML private VBox errMsgBox;

  @FXML private Button saveBtn;
  @FXML private Button cancelBtn;

  /**
   * Shows the preferences page in a separate window
   */
  public static void showPreferences()
  {
    String fxmlPath = "views/preferences/preferences.fxml";
    PreferencesController controller = BehaviorLoggerUtil.showScene( fxmlPath, "Preferences" );
    controller.init();
  }

  private void init()
  {
    SimpleStringProperty dirProp = PreferencesManager.sessionDirectoryProperty();
    directoryField.setText( dirProp.get() );

    initComponentsBox();

    setupDurationRadioButtons();
    setupDurationTextFields();

    checkVersion.setSelected( PreferencesManager.getCheckVersion() );
  }

  private void initComponentsBox()
  {
    List< FilenameComponent > components = PreferencesManager.filenameComponents();

    for (FilenameComponent comp : components) {
      FilenameComponentView node = comp.view();
      componentsBox.getChildren().add( node );

      node.setOnDragDetected( evt -> {
        BehaviorLoggerUtil.dialogStage.get().getScene().setCursor( Cursor.CLOSED_HAND );
        addPreview( componentsBox, node );
        node.setVisible( false );
        node.startFullDrag();
      } );

      node.setOnMouseDragEntered( evt -> {
        int indexOfDraggingNode = componentsBox.getChildren().indexOf( evt.getGestureSource() );
        int indexOfDropTarget = componentsBox.getChildren().indexOf( node );
        rotateNodes( componentsBox, indexOfDraggingNode, indexOfDropTarget );
        evt.consume();
      } );

      node.setOnMouseEntered( evt -> node.setStyle( "-fx-background-color:#e0e0e0;" ) );
      node.setOnMouseExited( evt -> node.setStyle( "" ) );
      node.setCursor( Cursor.OPEN_HAND );

      node.checkbox.selectedProperty().addListener( ( o, oldv, newv ) -> redrawFilenameExample() );
    }

    componentsBox.setOnMouseDragReleased( evt -> {
      BehaviorLoggerUtil.dialogStage.get().getScene().setCursor( Cursor.DEFAULT );
      removePreview( componentsBox );
      redrawFilenameExample();
      for (int i = 0; i < components.size(); i++) {
        FilenameComponentView comp = (FilenameComponentView) componentsBox.getChildren().get( i );
        comp.setVisible( true );
        comp.setIndex( i + 1 );
      }
    } );

    redrawFilenameExample();
  }

  private void redrawFilenameExample()
  {
    List< String > examples =
        componentsBox.getChildren().stream()
                     .map( node -> ((FilenameComponentView) node).ref )
                     .filter( c -> c.enabled )
                     .map( c -> c.example )
                     .collect( Collectors.toList() );

    String example = String.format( "%s.xls", String.join( "-", examples ) );
    exampleLbl.setText( example );
  }

  private void setupDurationRadioButtons()
  {
    ToggleGroup group = new ToggleGroup();
    infiniteRadioBtn.setToggleGroup( group );
    timedRadioBtn.setToggleGroup( group );
    timedRadioBtn.selectedProperty().addListener( ( observable,
                                                    oldValue,
                                                    selected ) -> {
      durationBox.setDisable( !selected );
    } );

    int duration = PreferencesManager.getDuration();

    infiniteRadioBtn.setSelected( duration == 0 );
    timedRadioBtn.setSelected( duration != 0 );
    durationBox.setDisable( duration == 0 );
  }

  private void setupDurationTextFields()
  {
    int totalSeconds = PreferencesManager.getDuration() / 1000;
    int hrs = totalSeconds / (60 * 60);
    int minDivisor = totalSeconds % (60 * 60);
    int mins = minDivisor / 60;
    int secs = minDivisor % 60;

    hoursField.setText( BehaviorLoggerUtil.intToStr( hrs ) );
    minutesField.setText( BehaviorLoggerUtil.intToStr( mins ) );
    secondsField.setText( BehaviorLoggerUtil.intToStr( secs ) );

    char[] digits = "0123456789".toCharArray();
    EventHandler< ? super KeyEvent > limit2Digits = BehaviorLoggerUtil.createFieldLimiter( digits, 2 );
    hoursField.setOnKeyTyped( limit2Digits );
    minutesField.setOnKeyTyped( limit2Digits );
    secondsField.setOnKeyTyped( limit2Digits );

    boolean colorOnEnd = PreferencesManager.getColorOnEnd();
    boolean pauseOnEnd = PreferencesManager.getPauseOnEnd();
    boolean soundOnEnd = PreferencesManager.getSoundOnEnd();

    colorCheckBox.setSelected( colorOnEnd );
    pauseCheckBox.setSelected( pauseOnEnd );
    beepCheckBox.setSelected( soundOnEnd );
  }

  private void addPreview( final VBox vbox, final Node node )
  {
    ImageView imageView = new ImageView( node.snapshot( null, null ) );
    imageView.setManaged( false );
    imageView.setMouseTransparent( true );
    vbox.getChildren().add( imageView );
    vbox.setUserData( imageView );
    vbox.setOnMouseDragged( event -> {
      imageView.setY( event.getY() - 9 );
    } );
  }

  private void removePreview( final VBox root )
  {
    root.setOnMouseDragged( null );
    root.getChildren().remove( root.getUserData() );
    root.setUserData( null );
  }

  private static void rotateNodes( final VBox vbox,
                                   final int indexOfDraggingNode,
                                   final int indexOfDropTarget )
  {
    if (indexOfDraggingNode >= 0 && indexOfDropTarget >= 0) {
      final Node node = vbox.getChildren().remove( indexOfDraggingNode );
      vbox.getChildren().add( indexOfDropTarget, node );
    }
  }

  private File getDirectory()
  {
    return new File( directoryField.getText().trim() );
  }

  private boolean validate()
  {
    String cssRed = "-fx-background-color:#FFDDDD;-fx-border-color: #f00;";

    boolean valid = true;
    errMsgBox.getChildren().clear();

    // Validate Directory field
    File directory = getDirectory();
    if (!directory.exists()) {
      valid = false;
      Label lbl = new Label( "Directory does not exist." );
      lbl.setTextFill( Color.RED );
      errMsgBox.getChildren().add( lbl );
    } else if (!directory.canWrite() || !directory.canRead()) {
      valid = false;
      Label lbl = new Label( "You don't have read/write permissions to that folder." );
      lbl.setTextFill( Color.RED );
      errMsgBox.getChildren().add( lbl );
    } else {
      directoryField.setStyle( "" );
    }

    boolean enabledOne =
        componentsBox.getChildren().stream()
                     .anyMatch( node -> ((FilenameComponentView) node).checkbox.isSelected() );

    if (!enabledOne) {
      componentsBox.setStyle( cssRed );
      valid = false;
      Label lbl = new Label( "Your filename must have at least one enabled component." );
      lbl.setTextFill( Color.RED );
      errMsgBox.getChildren().add( lbl );
    } else {
      componentsBox.setStyle( "" );
    }

    return valid;
  }

  @FXML void onBrowseButtonPressed( ActionEvent evt )
  {
    BehaviorLoggerUtil.chooseFile( BehaviorLoggerUtil.dialogStage.get(), directoryField );
  }

  @FXML private void onSavePressed( ActionEvent evt )
  {
    if (!validate()) {
      return;
    }

    PreferencesManager.saveSessionDirectory( directoryField.getText().trim() );
    int duration =
        BehaviorLoggerUtil.getDurationInMillis( infiniteRadioBtn.isSelected(), hoursField, minutesField, secondsField );
    PreferencesManager.saveDuration( duration );
    PreferencesManager.saveColorOnEnd( colorCheckBox.isSelected() );
    PreferencesManager.savePauseOnEnd( pauseCheckBox.isSelected() );
    PreferencesManager.saveSoundOnEnd( beepCheckBox.isSelected() );

    List< FilenameComponent > components =
        componentsBox.getChildren()
                     .stream()
                     .map( ( Node n ) -> ((FilenameComponentView) n).ref )
                     .collect( Collectors.toList() );

    PreferencesManager.saveFilenameComponents( components );

    PreferencesManager.setCheckVersion( checkVersion.isSelected() );

    BehaviorLoggerUtil.dialogStage.get().close();
  }

  @FXML private void onCancelPressed( ActionEvent evt )
  {
    BehaviorLoggerUtil.dialogStage.get().close();
  }

  @FXML void onHelpBtnPressed()
  {
    BehaviorLoggerUtil.openManual( "preferences" );
  }
}
