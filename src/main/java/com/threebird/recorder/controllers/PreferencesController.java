package com.threebird.recorder.controllers;

import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import com.google.common.collect.Lists;

/**
 * Corresponds to preferences.fxml
 */
public class PreferencesController
{
  @FXML private Node root;
  @FXML private VBox componentsBox;

  private static final String DRAGGING = "-fx-text-fill:gray;-fx-background-color:gray;";
  private static final String NORMAL = "";

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
      Label node = new Label( component );
      node.setMinWidth( 200 );
      node.setFont( Font.font( 16 ) );
      componentsBox.getChildren().add( node );

      node.setOnDragDetected( evt -> {
        addPreview( componentsBox, node );
        node.setStyle( DRAGGING );
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
      componentsBox.getChildren().forEach( node -> node.setStyle( NORMAL ) );
    } );
  }

  private void addPreview( final VBox vbox, final Label label )
  {
    ImageView imageView = new ImageView( label.snapshot( null, null ) );
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
