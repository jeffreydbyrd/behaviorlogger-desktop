package com.threebird.recorder.utils.ioa;

public enum IoaMethod
{
  Exact_Agreement("Exact Agreement"),
  Partial_Agreement("Partial Agreement"),
  Time_Window("Time Window");

  public final String display;

  private IoaMethod( String display )
  {
    this.display = display;
  }

  public static IoaMethod get( String display )
  {
    for (IoaMethod m : IoaMethod.values()) {
      if (m.display.equals( display )) {
        return m;
      }
    }

    throw new IllegalStateException( "No IoaMethod with that display exists." );
  }
}