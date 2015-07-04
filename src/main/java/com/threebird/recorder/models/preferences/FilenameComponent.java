package com.threebird.recorder.models.preferences;

import com.google.common.base.Supplier;
import com.threebird.recorder.models.schemas.SchemasManager;
import com.threebird.recorder.models.sessions.SessionManager;
import com.threebird.recorder.views.preferences.FilenameComponentView;

public enum FilenameComponent
{
  Client("Client", "client", true, ( ) -> SchemasManager.getSelected().client),
  Project("Project", "project", true, ( ) -> SchemasManager.getSelected().project),
  Observer("Observer", "observer", true, ( ) -> SessionManager.getObserver()),
  Therapist("Therapist", "therapist", false, ( ) -> SessionManager.getTherapist()),
  Condition("Condition", "condition", false, ( ) -> SessionManager.getCondition()),
  Location("Location", "location", false, ( ) -> SessionManager.getLocation()),
  Session_Number("Session Number", "123", true, ( ) -> SessionManager.getSessionNumber().toString());

  public int order;
  public final String name;
  public final String example;
  public boolean enabled = false;
  private final Supplier< String > getComponent;

  private FilenameComponent( String display,
                             String example,
                             boolean defaultEnabled,
                             Supplier< String > getComponent )
  {
    this.getComponent = getComponent;
    this.order = ordinal() + 1;
    this.name = display;
    this.example = example;
    this.enabled = defaultEnabled;
  }

  public FilenameComponentView view()
  {
    return new FilenameComponentView( this.order, this.name, this.enabled, this );
  }

  public String getComponent()
  {
    return getComponent.get();
  }
}
