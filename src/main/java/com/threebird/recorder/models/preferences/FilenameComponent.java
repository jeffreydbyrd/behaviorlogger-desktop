package com.threebird.recorder.models.preferences;

import com.threebird.recorder.views.preferences.FilenameComponentView;

public class FilenameComponent
{
  public int order;
  public final String display;
  public final String example;
  public boolean enabled;

  FilenameComponent( int order, String display, boolean enabled, String example )
  {
    this.order = order;
    this.display = display;
    this.example = example;
    this.enabled = enabled;
  }

  FilenameComponent( int order, String display, String example )
  {
    this( order, display, false, example );
  }

  public FilenameComponentView view()
  {
    return new FilenameComponentView( this.order, this.display, this.enabled, this );
  }
}
