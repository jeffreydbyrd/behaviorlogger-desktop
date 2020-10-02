package com.behaviorlogger.utils.ioa;

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

  @Override public String toString()
  {
    return display;
  }
}