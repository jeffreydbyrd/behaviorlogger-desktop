package com.threebird.recorder.controllers;

import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import com.threebird.recorder.models.FilenameComponent;
import com.threebird.recorder.views.preferences.FilenameComponentView;

/**
 * Corresponds to preferences.fxml
 */
public class PreferencesController
{
  @FXML private TextField directoryField;
  @FXML private Button browseButton;
  @FXML private VBox componentsBox;
  private Stage stage;

  public void init( Stage stage )
  {
    this.stage = stage;
    initComponentsBox();
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

}
