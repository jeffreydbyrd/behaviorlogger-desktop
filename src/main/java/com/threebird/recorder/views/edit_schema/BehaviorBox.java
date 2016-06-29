package com.threebird.recorder.views.edit_schema;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.utils.EventRecorderUtil;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class BehaviorBox extends HBox
{
  public final String uuid;
  private SimpleBooleanProperty isContinuousProp;
  private SimpleStringProperty keyProp;
  private SimpleStringProperty descriptionProp;
  private SimpleBooleanProperty deletedProp;

  private Label keyText;
  private Label descText;
  private Separator separator = new Separator( Orientation.VERTICAL );

  private TextField keyField;
  private TextField descField;

  private Button deleteBtn = new Button( "delete" );
  private Button editBtn = new Button( "edit" );
  private Button undoBtn = new Button( "revive" );
  private HBox actionBox = new HBox( deleteBtn, editBtn );

  public BehaviorBox( String uuid,
                      boolean isContinuous,
                      MappableChar key,
                      String description,
                      Supplier< List< BehaviorBox > > getOthers )
  {
    super();

    this.uuid = uuid;
    this.isContinuousProp = new SimpleBooleanProperty( isContinuous );
    this.keyProp = new SimpleStringProperty( key.c + "" );
    this.descriptionProp = new SimpleStringProperty( description );
    this.deletedProp = new SimpleBooleanProperty( false );
    keyText = new Label( key.c + "" );
    descText = new Label( description );
    keyField = new TextField( key.c + "" );
    descField = new TextField( description );

    keyText.setMinWidth( 44 );
    descText.setMinWidth( 100 );
    keyField.setMaxWidth( 44 );
    descField.setMinWidth( 100 );
    HBox.setHgrow( descField, Priority.ALWAYS );

    keyProp.addListener( ( o, ov, nv ) -> keyText.setText( nv ) );
    descriptionProp.addListener( ( o, ov, nv ) -> descText.setText( nv ) );

    actionBox.setNodeOrientation( NodeOrientation.RIGHT_TO_LEFT );
    HBox.setHgrow( actionBox, Priority.ALWAYS );

    this.setPrefHeight( 30 );
    this.setMinHeight( USE_PREF_SIZE );
    this.setOnMouseEntered( evt -> this.setStyle( "-fx-background-color:#e0e0e0;" ) );
    this.setOnMouseExited( evt -> this.setStyle( "" ) );
    this.getChildren().addAll( keyText, separator, descText, actionBox );

    // Setup delete button
    deleteBtn.setOnAction( e -> {
      deletedProp.setValue( true );
      actionBox.getChildren().clear();
      actionBox.getChildren().addAll( undoBtn, editBtn );
      editBtn.setDisable( true );
      keyText.setDisable( true );
      descText.setDisable( true );
      undoBtn.requestFocus();
    } );

    undoBtn.setOnAction( e -> {
      deletedProp.setValue( false );
      actionBox.getChildren().clear();
      actionBox.getChildren().addAll( deleteBtn, editBtn );
      editBtn.setDisable( false );
      keyText.setDisable( false );
      descText.setDisable( false );
      deleteBtn.requestFocus();
    } );

    // Setup Edit Stuff
    SimpleBooleanProperty editingProp = new SimpleBooleanProperty( false );

    editBtn.setOnAction( e -> {
      this.getChildren().clear();
      this.getChildren().addAll( keyField, descField );
      editingProp.set( true );
      keyField.requestFocus();
    } );

    EventHandler< ? super KeyEvent > onEnter = evt -> {
      if (evt.getCode().equals( KeyCode.ENTER ) || evt.getCode().equals( KeyCode.ESCAPE )) {
        save();
        editBtn.requestFocus();
      }
    };

    keyField.setOnKeyPressed( onEnter );
    descField.setOnKeyPressed( onEnter );

    keyField.focusedProperty().addListener( ( old, ov, isFocused ) -> {
      if (!isFocused && !descField.isFocused()) {
        save();
      }
    } );

    descField.focusedProperty().addListener( ( old, ov, isFocused ) -> {
      if (!isFocused && !keyField.isFocused()) {
        save();
      }
    } );

    EventHandler< ? super KeyEvent > limitText =
        EventRecorderUtil.createFieldLimiter( keyField, MappableChar.acceptableKeys(), 1 );

    keyField.setOnKeyTyped( evt -> {
      String text = keyField.getText() + evt.getCharacter();

      // limit to 1 char
      limitText.handle( evt );
      if (evt.isConsumed() || text.length() != 1) {
        return;
      }

      Optional< MappableChar > optChar = MappableChar.getForString( evt.getCharacter() );
      if (!optChar.isPresent()) {
        evt.consume();
        return;
      }

      MappableChar c = optChar.get();

      for (BehaviorBox behaviorBox : getOthers.get()) {
        if (behaviorBox == this) {
          continue;
        }

        if (behaviorBox.getKey().equals( c )) {
          evt.consume();
          return;
        }
      }
    } );
  }

  private void save()
  {
    this.getChildren().clear();
    this.getChildren().addAll( keyText, separator, descText, actionBox );

    if (keyField.getText().isEmpty()) {
      this.keyField.setText( this.keyProp.get() );
      return;
    }

    if (descField.getText().isEmpty()) {
      this.descField.setText( this.descriptionProp.get() );
      return;
    }

    this.keyProp.setValue( keyField.getText() );
    this.descriptionProp.setValue( descField.getText() );
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

  public boolean isDeleted()
  {
    return this.deletedProp.get();
  }
}
