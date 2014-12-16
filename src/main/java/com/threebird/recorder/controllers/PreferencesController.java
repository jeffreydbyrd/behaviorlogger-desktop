package com.threebird.recorder.controllers;

import java.util.ArrayList;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import com.google.common.collect.Lists;

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
      Label lbl = new Label( component );
      componentsBox.getChildren().add( lbl );

      lbl.setOnDragDetected( evt -> lbl.startFullDrag() );
      lbl.setOnMouseDragEntered( evt -> lbl.setStyle( "-fx-background-color: #ffffa0;" ) );
      lbl.setOnMouseDragExited( evt -> lbl.setStyle( "" ) );
      lbl.setOnMouseDragReleased( evt -> {
        lbl.setStyle( "" );
        int indexOfDraggingNode = componentsBox.getChildren().indexOf( evt.getGestureSource() );
        int indexOfDropTarget = componentsBox.getChildren().indexOf( lbl );
        rotateNodes( componentsBox, indexOfDraggingNode, indexOfDropTarget );
        evt.consume();
      } );
    }

    componentsBox.setOnDragDropped( evt -> {
      int indexOfDraggingNode = componentsBox.getChildren().indexOf( evt.getGestureSource() );
      rotateNodes( componentsBox, indexOfDraggingNode, componentsBox.getChildren().size() - 1 );
    } );
  }

  private void rotateNodes( final VBox vbox,
                            final int indexOfDraggingNode,
                            final int indexOfDropTarget )
  {
    if (indexOfDraggingNode >= 0 && indexOfDropTarget >= 0) {
      final Node node = vbox.getChildren().remove( indexOfDraggingNode );
      vbox.getChildren().add( indexOfDropTarget, node );
    }
  }
}
