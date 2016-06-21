package com.threebird.recorder.views.edit_schema;

import com.threebird.recorder.models.MappableChar;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class BehaviorBox extends HBox
{
  public final String uuid;
  private SimpleBooleanProperty isContinuousProp;
  private SimpleStringProperty keyProp;
  private SimpleStringProperty descriptionProp;

  public CheckBox checkbox;
  public Text keyText;
  public Text descText;

  public TextField keyField;
  public TextField descField;

  public BehaviorBox( String uuid, boolean isContinuous, MappableChar key, String description )
  {
    super();
    this.uuid = uuid;
    this.isContinuousProp = new SimpleBooleanProperty();
    this.keyProp = new SimpleStringProperty();
    this.descriptionProp = new SimpleStringProperty();

    checkbox = new CheckBox();
    checkbox.setSelected( isContinuous );
    checkbox.setOnAction( e -> isContinuousProp.set( checkbox.isSelected() ) );

    keyText = new Text( key.c + "" );
    descText = new Text( description );

    keyField.textProperty().addListener( ( observable, oldValue, newValue ) -> keyProp.setValue( newValue ) );
    descField.textProperty().addListener( ( observable, oldValue, newValue ) -> descriptionProp.setValue( newValue ) );

    keyProp.addListener( ( o, ov, nv ) -> keyText.setText( nv ) );
    descriptionProp.addListener( ( o, ov, nv ) -> descText.setText( nv ) );

    Separator sep1 = new Separator( Orientation.VERTICAL );
    Separator sep2 = new Separator( Orientation.VERTICAL );

    this.getChildren().addAll( checkbox, sep1, keyText, sep2, descText );
  }

  public String getUuid()
  {
    return this.uuid;
  }

  public boolean isContinuous()
  {
    return this.isContinuousProp.get();
  }

  public MappableChar getKey()
  {
    String text = this.keyProp.get();
    return MappableChar.getForString( text ).get();
  }

  public String getDescription()
  {
    return this.descriptionProp.get();
  }
}
