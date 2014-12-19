package com.threebird.recorder.controllers;

import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import com.google.common.collect.Lists;

/**
 * Corresponds to preferences.fxml
 */
public class PreferencesController
{
  
  @FXML private Node root;
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
      Label lbl = new Label( component );
      lbl.setMinWidth( 300 );
      componentsBox.getChildren().add( lbl );

      lbl.setOnDragDetected( evt -> {
        addPreview( componentsBox, lbl );
        lbl.setStyle( "-fx-text-fill:gray;-fx-background-color:gray;" );
        lbl.startFullDrag();
      } );

      lbl.setOnMouseDragEntered( evt -> {
        int indexOfDraggingNode = componentsBox.getChildren().indexOf( evt.getGestureSource() );
        int indexOfDropTarget = componentsBox.getChildren().indexOf( lbl );
        rotateNodes( componentsBox, indexOfDraggingNode, indexOfDropTarget );
        evt.consume();
      } );

      lbl.setOnMouseDragReleased( evt -> {
        removePreview( componentsBox );
        lbl.setStyle( "" );
      } );
    }
  }

  private void addPreview( final VBox root, final Label label )
  {
    ImageView imageView = new ImageView( label.snapshot( null, null ) );
    imageView.setManaged( false );
    imageView.setMouseTransparent( true );
    root.getChildren().add( imageView );
    root.setUserData( imageView );
    root.setOnMouseDragged( event -> {
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
