package com.threebird.recorder.views.edit_schema;

import com.threebird.recorder.models.MappableChar;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class BehaviorBox extends VBox
{
  public final String uuid;
  private SimpleBooleanProperty isContinuousProp;
  private SimpleStringProperty keyProp;
  private SimpleStringProperty descriptionProp;

  private HBox hbox;

  private Label keyText;
  private Label descText;
  private Label keyTakenLbl;
  private Label descEmptyLbl;

  private TextField keyField;
  private TextField descField;

  private HBox actionBox;
  private Button deleteBtn;
  private Button editBtn;
  private Button reviveBtn;

  public BehaviorBox( String uuid, boolean isContinuous, MappableChar key, String description )
  {
    super();

    this.uuid = uuid;
    this.isContinuousProp = new SimpleBooleanProperty( isContinuous );
    this.keyProp = new SimpleStringProperty( key.c + "" );
    this.descriptionProp = new SimpleStringProperty( description );

    keyText = new Label( key.c + "" );
    descText = new Label( description );

    keyTakenLbl = new Label( "That key is taken." );
    keyTakenLbl.setTextFill( Color.RED );
    descEmptyLbl = new Label( "Behavior description is required." );
    descEmptyLbl.setTextFill( Color.RED );

    keyField = new TextField( key.c + "" );
    descField = new TextField( description );
    HBox.setHgrow( descField, Priority.ALWAYS );

    keyField.textProperty().addListener( ( observable, oldValue, newValue ) -> keyProp.setValue( newValue ) );
    descField.textProperty().addListener( ( observable, oldValue, newValue ) -> descriptionProp.setValue( newValue ) );

    keyProp.addListener( ( o, ov, nv ) -> keyText.setText( nv ) );
    descriptionProp.addListener( ( o, ov, nv ) -> descText.setText( nv ) );

    Separator sep2 = new Separator( Orientation.VERTICAL );

    deleteBtn = new Button( "delete" );
    editBtn = new Button( "edit" );
    reviveBtn = new Button( "revive" );
    actionBox = new HBox( deleteBtn, editBtn );
    actionBox.setNodeOrientation( NodeOrientation.RIGHT_TO_LEFT );
    HBox.setHgrow( actionBox, Priority.ALWAYS );

    hbox = new HBox();
    hbox.setPrefHeight( 30 );
    hbox.setMinHeight( USE_PREF_SIZE );
    hbox.setOnMouseEntered( evt -> hbox.setStyle( "-fx-background-color:#e0e0e0;" ) );
    hbox.setOnMouseExited( evt -> hbox.setStyle( "" ) );
    hbox.getChildren().addAll( keyText, sep2, descText, actionBox );

    this.getChildren().addAll( hbox );

    // Setup delete button
    deleteBtn.setOnAction( e -> {

    } );
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
