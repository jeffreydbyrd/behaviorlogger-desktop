package com.threebird.recorder.views.preferences;

import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

public class FilenameComponent extends Pane
{
  private HBox root;
  private Label index;
  private CheckBox checkbox;
  private Label label;

  public FilenameComponent( int i, String name )
  {
    this.index = new Label( i + "." );
    this.checkbox = new CheckBox();
    checkbox.setCursor( Cursor.DEFAULT );
    this.label = new Label( name );
    HBox.setHgrow( label, Priority.ALWAYS );
    label.setFont( Font.font( 14 ) );

    this.root = new HBox( index, checkbox, label );
    this.root.setSpacing( 5 );
    this.getChildren().add( root );

  }

  public void setIndex( int i )
  {
    this.index.setText( i + "." );
  }
}
