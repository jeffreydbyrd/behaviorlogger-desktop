package com.threebird.recorder.controllers;

import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import com.google.common.collect.Lists;
import com.threebird.recorder.views.preferences.FilenameComponent;

/**
 * Corresponds to preferences.fxml
 */
public class PreferencesController
{
  @FXML private VBox componentsBox;

  @FXML private void initialize()
  {
    initComponentsBox();
  }

  private void initComponentsBox()
  {
    final ArrayList< String > components =
        Lists.newArrayList( "Client", "Project",
                            "Observer", "Therapist",
                            "Condition", "Session Number" );

    for (String component : components) {
      FilenameComponent node = new FilenameComponent( component );
      componentsBox.getChildren().add( node );

      node.setOnDragDetected( evt -> {
        addPreview( componentsBox, node );
        node.setDraggingStyle();
        node.startFullDrag();
      } );

      node.setOnMouseDragEntered( evt -> {
        int indexOfDraggingNode = componentsBox.getChildren().indexOf( evt.getGestureSource() );
        int indexOfDropTarget = componentsBox.getChildren().indexOf( node );
        rotateNodes( componentsBox, indexOfDraggingNode, indexOfDropTarget );
        evt.consume();
      } );
    }

    componentsBox.setOnMouseDragReleased( evt -> {
      removePreview( componentsBox );
      for (Node node : componentsBox.getChildren()) {
        ((FilenameComponent) node).setNormalStyle();
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
}
