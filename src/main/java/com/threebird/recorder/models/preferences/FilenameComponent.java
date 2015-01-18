package com.threebird.recorder.models.preferences;

import com.threebird.recorder.views.preferences.FilenameComponentView;

public class FilenameComponent
{
  public int order;
  public final String display;
  public boolean enabled;

  FilenameComponent( int order, String display, boolean enabled )
  {
    this.order = order;
    this.display = display;
    this.enabled = enabled;
  }

  FilenameComponent( int order, String display )
  {
    this( order, display, false );
  }

  public FilenameComponentView view()
  {
    return new FilenameComponentView( this.order, this.display, this.enabled, this );
  }
}
