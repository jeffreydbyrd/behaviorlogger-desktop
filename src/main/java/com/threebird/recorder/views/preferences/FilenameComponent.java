package com.threebird.recorder.views.preferences;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

public class FilenameComponent extends Pane
{
  private HBox root;
  private CheckBox checkbox;
  private Label label;

  private static final String DRAGGING = "-fx-text-fill:gray;-fx-background-color:gray;";
  private static final String NORMAL = "";

  public FilenameComponent( String name )
  {
    this.checkbox = new CheckBox();
    this.label = new Label( name );
    this.root = new HBox( checkbox, label );
    this.getChildren().add( root );

    label.setFont( Font.font( 14 ) );
  }

  public void setDraggingStyle()
  {
    checkbox.setVisible( false );
    label.setVisible( false );
    this.setStyle( DRAGGING );
  }

  public void setNormalStyle()
  {
    checkbox.setVisible( true );
    label.setVisible( true );
    this.setStyle( NORMAL );
  }
}
