package com.threebird.recorder.controllers;

import java.io.File;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import com.threebird.recorder.models.FilenameComponent;
import com.threebird.recorder.models.preferences.PreferencesManager;
import com.threebird.recorder.utils.EventRecorderUtil;
import com.threebird.recorder.views.preferences.FilenameComponentView;

/**
 * Corresponds to preferences.fxml
 */
public class PreferencesController
{
  @FXML private TextField directoryField;
  @FXML private Button browseButton;
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

  @FXML private Button saveBtn;
  @FXML private Button cancelBtn;
  private Stage stage;

  public void init( Stage stage )
  {
    this.stage = stage;

    SimpleStringProperty dirProp = PreferencesManager.sessionDirectoryProperty();
    directoryField.setText( dirProp.get() );

    initComponentsBox();

    setupDurationRadioButtons();
    setupDurationTextFields();
  }

  private void initComponentsBox()
  {
    FilenameComponent[] components = FilenameComponent.values();

    for (int i = 0; i < components.length; i++) {
      FilenameComponentView node = components[i].view;
      componentsBox.getChildren().add( node );

      node.setOnDragDetected( evt -> {
        stage.getScene().setCursor( Cursor.CLOSED_HAND );
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
    }

    componentsBox.setOnMouseDragReleased( evt -> {
      stage.getScene().setCursor( Cursor.DEFAULT );
      removePreview( componentsBox );
      for (int i = 0; i < components.length; i++) {
        FilenameComponentView comp = (FilenameComponentView) componentsBox.getChildren().get( i );
        comp.setVisible( true );
        comp.setIndex( i + 1 );
      }
    } );
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
    int duration = PreferencesManager.getDuration();
    int hrs = duration / (60 * 60);
    int minDivisor = duration % (60 * 60);
    int mins = minDivisor / 60;
    int secs = minDivisor % 60;

    hoursField.setText( EventRecorderUtil.intToStr( hrs ) );
    minutesField.setText( EventRecorderUtil.intToStr( mins ) );
    secondsField.setText( EventRecorderUtil.intToStr( secs ) );

    char[] digits = "0123456789".toCharArray();
    hoursField.setOnKeyTyped( EventRecorderUtil.createFieldLimiter( hoursField, digits, 2 ) );
    minutesField.setOnKeyTyped( EventRecorderUtil.createFieldLimiter( minutesField, digits, 2 ) );
    secondsField.setOnKeyTyped( EventRecorderUtil.createFieldLimiter( secondsField, digits, 2 ) );

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

  private void validate()
  {
    String cssRed = "-fx-background-color:#FFDDDD;-fx-border-color: #f00;";

    // Validate Directory field
    if (!getDirectory().exists()) {
      directoryField.setStyle( cssRed );
    } else {
      directoryField.setStyle( "" );
    }
  }

  @FXML void onBrowseButtonPressed( ActionEvent evt )
  {
    File f = getDirectory();
    if (!f.exists()) {
      f = new File( System.getProperty( "user.home" ) );
    }

    DirectoryChooser dirChooser = new DirectoryChooser();
    dirChooser.setInitialDirectory( f );
    File newFile = dirChooser.showDialog( null );

    if (newFile != null) {
      directoryField.setText( newFile.getPath() );
    }
  }

  @FXML private void onSavePressed( ActionEvent evt )
  {
    validate();
    PreferencesManager.saveSessionDirectory( directoryField.getText().trim() );
    int duration =
        EventRecorderUtil.getDuration( infiniteRadioBtn.isSelected(), hoursField, minutesField, secondsField );
    PreferencesManager.saveDuration( duration );
    PreferencesManager.saveColorOnEnd( colorCheckBox.isSelected() );
    PreferencesManager.savePauseOnEnd( pauseCheckBox.isSelected() );
    PreferencesManager.saveSoundOnEnd( beepCheckBox.isSelected() );
    stage.close();
  }

  @FXML private void onCancelPressed( ActionEvent evt )
  {
    stage.close();
  }

}
