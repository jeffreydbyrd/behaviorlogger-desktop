package com.threebird.recorder.views.edit_schema;

import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import com.threebird.recorder.controllers.EditSchemaController;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.utils.EventRecorderUtil;

/**
 * This is an HBox with 3 Nodes (CheckBox and two TextFields). We use it to
 * represent a {@link KeyBehaviorMapping} in the {@link EditSchemaController}
 */
public class MappingBox extends HBox
{
  public final CheckBox checkbox;
  public final TextField keyField;
  public final TextField behaviorField;

  private static final Insets insets = new Insets( .5, .5, .5, .5 );
  private static char[] acceptableKeys =
      "abcdefghijklmnopqrstuvwxyz1234567890`-=[]\\;',./".toCharArray();

  public MappingBox( boolean isContinuous, String key, String behavior )
  {
    super();

    checkbox = new CheckBox();
    checkbox.setSelected( isContinuous );
    HBox.setHgrow( checkbox, Priority.NEVER );
    HBox.setMargin( checkbox, new Insets( 5, 5, 0, 10 ) );

    keyField = new TextField( key );
    keyField.setMaxWidth( 40 );
    HBox.setHgrow( keyField, Priority.NEVER );
    HBox.setMargin( keyField, insets );
    keyField.setOnKeyTyped( EventRecorderUtil.createFieldLimiter( keyField, acceptableKeys, 1 ) );

    behaviorField = new TextField( behavior );
    HBox.setHgrow( behaviorField, Priority.ALWAYS );
    HBox.setMargin( behaviorField, insets );

    getChildren().add( checkbox );
    getChildren().add( keyField );
    getChildren().add( behaviorField );
  }

  public KeyBehaviorMapping translate()
  {
    boolean isContinuous = checkbox.isSelected();
    String key = keyField.getText().trim();
    String behavior = behaviorField.getText().trim();
    return key.isEmpty() ? null : new KeyBehaviorMapping( key, behavior, isContinuous );
  }
}
