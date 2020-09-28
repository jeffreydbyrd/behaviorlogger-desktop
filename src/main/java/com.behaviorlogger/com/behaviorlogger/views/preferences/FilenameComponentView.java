package com.behaviorlogger.views.preferences;

import com.behaviorlogger.models.preferences.FilenameComponent;

import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;

public class FilenameComponentView extends Pane
{
  private HBox root;
  private Label index;
  public final CheckBox checkbox;
  private Label label;

  private int i;
  public final FilenameComponent ref;

  public FilenameComponentView( int i, String name, boolean enabled, FilenameComponent ref )
  {
    this.i = i;
    this.ref = ref;
    this.index = new Label( i + "." );

    this.checkbox = new CheckBox();
    checkbox.setCursor( Cursor.DEFAULT );
    checkbox.setSelected( enabled );
    checkbox.selectedProperty().addListener( ( observable, oldV, newV ) -> {
      ref.enabled = newV;
    } );

    this.label = new Label( name );
    HBox.setHgrow( label, Priority.ALWAYS );
    label.setFont( Font.font( 14 ) );

    this.root = new HBox( index, checkbox, label );
    this.root.setSpacing( 5 );
    this.getChildren().add( root );

  }

  public void setIndex( int i )
  {
    this.i = i;
    this.ref.order = i;
    this.index.setText( i + "." );
  }

  public String getName()
  {
    return label.getText();
  }

  public boolean isSelected()
  {
    return checkbox.isSelected();
  }

  public int getIndex()
  {
    return i;
  }
}
