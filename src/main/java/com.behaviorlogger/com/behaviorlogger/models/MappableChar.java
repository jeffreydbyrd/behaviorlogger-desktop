package com.behaviorlogger.models;

import java.util.HashMap;
import java.util.Optional;

import javafx.scene.input.KeyCode;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

/**
 * All the possible chars that a user is allowed to map to a behavior in a Schema
 */
public enum MappableChar
{
  A('a', KeyCode.A),
  B('b', KeyCode.B),
  C('c', KeyCode.C),
  D('d', KeyCode.D),
  E('e', KeyCode.E),
  F('f', KeyCode.F),
  G('g', KeyCode.G),
  H('h', KeyCode.H),
  I('i', KeyCode.I),
  J('j', KeyCode.J),
  K('k', KeyCode.K),
  L('l', KeyCode.L),
  M('m', KeyCode.M),
  N('n', KeyCode.N),
  O('o', KeyCode.O),
  P('p', KeyCode.P),
  Q('q', KeyCode.Q),
  R('r', KeyCode.R),
  S('s', KeyCode.S),
  T('t', KeyCode.T),
  U('u', KeyCode.U),
  V('v', KeyCode.V),
  W('w', KeyCode.W),
  X('x', KeyCode.X),
  Y('y', KeyCode.Y),
  Z('z', KeyCode.Z),
  DIGIT0('0', KeyCode.DIGIT0),
  DIGIT1('1', KeyCode.DIGIT1),
  DIGIT2('2', KeyCode.DIGIT2),
  DIGIT3('3', KeyCode.DIGIT3),
  DIGIT4('4', KeyCode.DIGIT4),
  DIGIT5('5', KeyCode.DIGIT5),
  DIGIT6('6', KeyCode.DIGIT6),
  DIGIT7('7', KeyCode.DIGIT7),
  DIGIT8('8', KeyCode.DIGIT8),
  DIGIT9('9', KeyCode.DIGIT9),
  BACK_QUOTE('`', KeyCode.BACK_QUOTE),
  MINUS('-', KeyCode.MINUS),
  EQUALS('=', KeyCode.EQUALS),
  OPEN_BRACKET('[', KeyCode.OPEN_BRACKET),
  CLOSE_BRACKET(']', KeyCode.CLOSE_BRACKET),
  BACK_SLASH('\\', KeyCode.BACK_SLASH),
  SEMICOLON(';', KeyCode.SEMICOLON),
  QUOTE('\'', KeyCode.QUOTE),
  COMMA(',', KeyCode.COMMA),
  PERIOD('.', KeyCode.PERIOD),
  SLASH('/', KeyCode.SLASH);

  public static final JsonDeserializer< MappableChar > gsonDeserializer =
      ( json, typeOfT, context ) -> {
        String str = json.getAsJsonPrimitive().getAsString();
        Optional< MappableChar > mc = MappableChar.getForString( str );
        return mc.get();
      };

  public static final JsonSerializer< MappableChar > gsonSerializer =
      ( src, typeOfSrc, context ) -> {
        JsonElement el = new JsonPrimitive( src.c );
        return el;
      };

  public final char c;
  public final KeyCode code;

  private MappableChar( char c, KeyCode code )
  {
    this.c = c;
    this.code = code;
  }

  private static HashMap< KeyCode, MappableChar > keycodes;
  private static HashMap< Character, MappableChar > chars;
  static {
    MappableChar[] values = MappableChar.values();
    keycodes = new HashMap< KeyCode, MappableChar >( values.length );
    chars = new HashMap< Character, MappableChar >( values.length );
    for (MappableChar mc : values) {
      keycodes.put( mc.code, mc );
      chars.put( mc.c, mc );
    }
  }

  public static Optional< MappableChar > getForKeyCode( KeyCode kc )
  {
    return Optional.ofNullable( keycodes.get( kc ) );
  }

  public static Optional< MappableChar > getForChar( char c )
  {
    return Optional.ofNullable( chars.get( c ) );
  }

  public static Optional< MappableChar > getForString( String s )
  {
    if (s.isEmpty()) {
      return Optional.empty();
    }

    return getForChar( s.charAt( 0 ) );
  }

  private static char[] acceptableKeys;
  static {
    MappableChar[] values = MappableChar.values();
    acceptableKeys = new char[values.length];
    for (int i = 0; i < values.length; i++) {
      MappableChar mappableChar = values[i];
      acceptableKeys[i] = mappableChar.c;
    }
  }

  public static char[] acceptableKeys()
  {
    return acceptableKeys;
  }

  @Override public String toString()
  {
    return c + "";
  }
}
