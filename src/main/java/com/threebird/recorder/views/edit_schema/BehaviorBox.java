package com.threebird.recorder.views.edit_schema;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import com.threebird.recorder.models.MappableChar;
import com.threebird.recorder.utils.BehaviorLoggerUtil;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class BehaviorBox extends VBox
{
  public final String uuid;
  private SimpleBooleanProperty isContinuousProp;
  private SimpleStringProperty keyProp;
  private SimpleStringProperty descriptionProp;
  private SimpleBooleanProperty deletedProp;

  private HBox hbox = new HBox();
  private Label keyTakenLbl = new Label( "That key is taken." );

  private Label keyText;
  private Label descText;
  private Separator separator = new Separator( Orientation.VERTICAL );

  private TextField keyField;
  private TextField descField;

  private Button deleteBtn = new Button( "delete" );
  private Button editBtn = new Button( "edit" );
  private Button undoBtn = new Button( "undo" );
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

    keyProp.addListener( ( o, ov, nv ) -> keyText.setText( nv ) );
    descriptionProp.addListener( ( o, ov, nv ) -> descText.setText( nv ) );

    keyText.setMinWidth( 44 );
    keyText.setAlignment( Pos.CENTER );
    descText.setMinWidth( 80 );
    keyField.setMaxWidth( 44 );
    descField.setMinWidth( 80 );
    editBtn.setMinWidth( 40 );
    deleteBtn.setMinWidth( 50 );
    undoBtn.setMinWidth( 40 );
    actionBox.setNodeOrientation( NodeOrientation.RIGHT_TO_LEFT );
    HBox.setHgrow( descText, Priority.ALWAYS );
    HBox.setHgrow( descField, Priority.ALWAYS );
    HBox.setHgrow( actionBox, Priority.ALWAYS );
    keyTakenLbl.setTextFill( Color.RED );
    hbox.setPrefHeight( 30 );
    hbox.setMinHeight( USE_PREF_SIZE );
    hbox.setAlignment( Pos.CENTER );
    actionBox.setAlignment( Pos.CENTER );

    Font font = Font.font( 12 );
    editBtn.setFont( font );
    deleteBtn.setFont( font );
    undoBtn.setFont( font );

    hbox.getChildren().addAll( keyText, separator, descText, actionBox );
    this.getChildren().addAll( hbox );

    this.setOnMouseEntered( evt -> this.setStyle( "-fx-background-color:#e0e0e0;" ) );
    this.setOnMouseExited( evt -> this.setStyle( "" ) );

    // Setup delete button
    deleteBtn.setOnAction( e -> {
      deletedProp.setValue( true );
      actionBox.getChildren().clear();
      actionBox.getChildren().addAll( undoBtn, editBtn );
      editBtn.setVisible( false );
      keyText.setDisable( true );
      descText.setDisable( true );
      undoBtn.requestFocus();
    } );

    undoBtn.setOnAction( e -> {
      if (isTaken( getKey(), getOthers.get() )) {
        e.consume();
        displayKeyTakenLabel();
        return;
      }

      deletedProp.setValue( false );
      actionBox.getChildren().clear();
      actionBox.getChildren().addAll( deleteBtn, editBtn );
      editBtn.setVisible( true );
      keyText.setDisable( false );
      descText.setDisable( false );
      deleteBtn.requestFocus();
    } );

    // Setup Edit Stuff
    SimpleBooleanProperty editingProp = new SimpleBooleanProperty( false );

    editBtn.setOnAction( e -> {
      hbox.getChildren().clear();
      hbox.getChildren().addAll( keyField, descField );
      editingProp.set( true );
      keyField.requestFocus();
    } );

    EventHandler< ? super KeyEvent > onEnter = evt -> {
      if (evt.getCode().equals( KeyCode.ENTER ) || evt.getCode().equals( KeyCode.ESCAPE )) {
        save();
        editBtn.requestFocus();
        return;
      }
    };

    keyField.setOnKeyPressed( onEnter );
    descField.setOnKeyPressed( onEnter );
    descField.setOnKeyTyped( BehaviorLoggerUtil.createFieldLimiter( 100 ) );

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
        BehaviorLoggerUtil.createFieldLimiter( MappableChar.acceptableKeys(), 1 );

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

      if (isTaken( c, getOthers.get() )) {
        evt.consume();
        displayKeyTakenLabel();
        return;
      }

      this.getChildren().clear();
      this.getChildren().addAll( hbox );
    } );
  }

  Timeline timer;

  private void displayKeyTakenLabel()
  {
    this.getChildren().clear();
    this.getChildren().addAll( hbox, keyTakenLbl );

    if (timer != null) {
      timer.playFromStart();
    } else {
      timer = new Timeline();

      BehaviorBox self = this;
      timer.setCycleCount( 1 );
      KeyFrame kf = new KeyFrame( Duration.seconds( 4 ), evt -> {
        timer.stop();
        self.getChildren().clear();
        self.getChildren().addAll( hbox );
      } );

      timer.getKeyFrames().add( kf );
      timer.play();
    }
  }

  private boolean isTaken( MappableChar c, List< BehaviorBox > boxes )
  {
    for (BehaviorBox behaviorBox : boxes) {
      if (behaviorBox == this) {
        continue;
      }

      if (!behaviorBox.isDeleted() && behaviorBox.getKey().equals( c )) {
        return true;
      }
    }

    return false;
  }

  private void save()
  {
    this.getChildren().clear();
    this.getChildren().addAll( hbox );
    hbox.getChildren().clear();
    hbox.getChildren().addAll( keyText, separator, descText, actionBox );

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
