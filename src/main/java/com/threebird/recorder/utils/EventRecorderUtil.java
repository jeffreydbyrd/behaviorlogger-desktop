package com.threebird.recorder.utils;

import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class EventRecorderUtil
{
  /**
   * Creates an EventHandler that will prevent a field's text from containing
   * any characters not in "acceptableKeys" and from exceeding the character
   * limit
   * 
   * @param field
   *          - the input-field that this EventHandler reads
   * @param acceptableKeys
   *          - a list of characters that the user is allowed to input
   * @param limit
   *          - the max length of the field
   * @return an EventHandler that consumes a KeyEvent if the typed Char is
   *         outside 'acceptableKeys' or if the length of 'field' is longer than
   *         'limit'
   */
  public static EventHandler< ? super KeyEvent >
    createFieldLimiter( TextField field, char[] acceptableKeys, int limit )
  {
    return evt -> {
      if (field.getText().trim().length() == limit
          || !String.valueOf( acceptableKeys ).contains( evt.getCharacter() ))
        evt.consume();
    };
  }
}
