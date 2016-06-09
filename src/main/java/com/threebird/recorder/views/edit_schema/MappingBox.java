package com.threebird.recorder.views.edit_schema;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.UUID;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.threebird.recorder.controllers.EditSchemaController;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.utils.EventRecorderUtil;

/**
 * This is an HBox with 3 Nodes (CheckBox and two TextFields). We use it to represent a {@link KeyBehaviorMapping} in
 * the {@link EditSchemaController}
 */
public class MappingBox extends HBox
{
  private final Optional< KeyBehaviorMapping > keyBehaviorMapping;

  public final CheckBox checkbox;
  public final TextField keyField;
  public final TextField behaviorField;

  private static final Insets insets = new Insets( .5, .5, .5, .5 );
  private static char[] acceptableKeys =
      "abcdefghijklmnopqrstuvwxyz1234567890`-=[]\\;',./".toCharArray();

  public MappingBox( Optional< KeyBehaviorMapping > keyBehMapping )
  {
    super();

    this.keyBehaviorMapping = keyBehMapping;

    KeyBehaviorMapping kbm;
    if (keyBehMapping.isPresent()) {
      kbm = keyBehMapping.get();
    } else {
      kbm = new KeyBehaviorMapping( "", "", "", false );
    }

    checkbox = new CheckBox();
    checkbox.setSelected( kbm.isContinuous );
    HBox.setHgrow( checkbox, Priority.NEVER );
    HBox.setMargin( checkbox, new Insets( 5, 5, 0, 10 ) );

    keyField = new TextField( kbm.key.c + "" );
    keyField.setMaxWidth( 40 );
    HBox.setHgrow( keyField, Priority.NEVER );
    HBox.setMargin( keyField, insets );
    keyField.setOnKeyTyped( EventRecorderUtil.createFieldLimiter( keyField, acceptableKeys, 1 ) );

    behaviorField = new TextField( kbm.behavior );
    HBox.setHgrow( behaviorField, Priority.ALWAYS );
    HBox.setMargin( behaviorField, insets );

    getChildren().add( checkbox );
    getChildren().add( keyField );
    getChildren().add( behaviorField );
  }

  public KeyBehaviorMapping translate()
  {
    if (this.keyBehaviorMapping.isPresent() && !Strings.isNullOrEmpty( this.keyBehaviorMapping.get().uuid )) {
      return this.keyBehaviorMapping.get();
    }

    String uuid = UUID.randomUUID().toString();
    boolean isContinuous = checkbox.isSelected();
    String key = Strings.nullToEmpty( keyField.getText() ).trim();
    String behavior = behaviorField.getText().trim();

    if (Strings.isNullOrEmpty( key ) || Strings.isNullOrEmpty( behavior )) {
      return null;
    }

    return new KeyBehaviorMapping( uuid, key, behavior, isContinuous );
  }
}
