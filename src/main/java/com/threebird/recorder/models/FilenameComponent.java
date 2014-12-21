package com.threebird.recorder.models;

import com.threebird.recorder.views.preferences.FilenameComponentView;

public enum FilenameComponent
{
  Client("Client"),
  Project("Project"),
  Observer("Observer"),
  Therapist("Therapist"),
  Condition("Condition"),
  Session_Number("Session Number");

  public final String display;
  public final FilenameComponentView view;

  FilenameComponent( String display )
  {
    this.display = display;
    this.view = new FilenameComponentView( ordinal(), display );
  }
}
