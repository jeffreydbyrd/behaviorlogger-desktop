package com.threebird.recorder.models.preferences;

import com.google.common.base.Supplier;
import com.threebird.recorder.models.schemas.SchemasManager;
import com.threebird.recorder.models.sessions.SessionManager;
import com.threebird.recorder.views.preferences.FilenameComponentView;

public enum FilenameComponent
{
  Client("Client", "client", ( ) -> SchemasManager.getSelected().client),
  Project("Project", "project", ( ) -> SchemasManager.getSelected().project),
  Observer("Observer", "observer", ( ) -> SessionManager.getObserver()),
  Therapist("Therapist", "therapist", ( ) -> SessionManager.getTherapist()),
  Condition("Condition", "condition", ( ) -> SessionManager.getCondition()),
  Session_Number("Session Number", "123", ( ) -> SessionManager.getSessionNumber().toString());

  public int order;
  public final String name;
  public final String example;
  public boolean enabled = false;
  private final Supplier< String > getComponent;

  private FilenameComponent( String display,
                             String example,
                             Supplier< String > getComponent )
  {
    this.getComponent = getComponent;
    this.order = ordinal() + 1;
    this.name = display;
    this.example = example;
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
