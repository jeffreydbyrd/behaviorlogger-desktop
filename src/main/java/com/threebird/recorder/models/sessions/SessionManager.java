package com.threebird.recorder.models.sessions;

import java.io.File;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.utils.resources.ResourceUtils;

public class SessionManager
{
  private static class GsonBean
  {
    String observer;
    String therapist;
    String condition;
    int sessionNumber;
  }

  private static SimpleStringProperty observerProperty;
  private static SimpleStringProperty therapistProperty;
  private static SimpleStringProperty conditionProperty;
  private static SimpleIntegerProperty sessionNumberProperty;

  private static File file = ResourceUtils.getSessionDetails();
  private static Supplier< GsonBean > defaultModel = Suppliers.memoize( ( ) -> GsonUtils.get( file, new GsonBean() ) );

  private static void persist()
  {
    GsonBean model = new GsonBean();
    model.observer = getObserver();
    model.therapist = getTherapist();
    model.condition = getCondition();
    model.sessionNumber = getSessionNumber();
    GsonUtils.save( file, model );
  }

  public static SimpleStringProperty observerProperty()
  {
    if (observerProperty == null) {
      observerProperty = new SimpleStringProperty( defaultModel.get().observer );
      observerProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return observerProperty;
  }

  public static SimpleStringProperty therapistProperty()
  {
    if (therapistProperty == null) {
      therapistProperty = new SimpleStringProperty( defaultModel.get().therapist );
      therapistProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return therapistProperty;
  }

  public static SimpleStringProperty conditionProperty()
  {
    if (conditionProperty == null) {
      conditionProperty = new SimpleStringProperty( defaultModel.get().condition );
      conditionProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return conditionProperty;
  }

  public static SimpleIntegerProperty sessionNumberProperty()
  {
    if (sessionNumberProperty == null) {
      sessionNumberProperty = new SimpleIntegerProperty( defaultModel.get().sessionNumber );
      sessionNumberProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return sessionNumberProperty;
  }

  public static String getObserver()
  {
    return observerProperty().get();
  }

  public static String getTherapist()
  {
    return therapistProperty().get();
  }

  public static String getCondition()
  {
    return conditionProperty().get();
  }

  public static Integer getSessionNumber()
  {
    return sessionNumberProperty().get();
  }

  public static void setObserver( String s )
  {
    observerProperty().set( s );
  }

  public static void setTherapist( String s )
  {
    therapistProperty().set( s );
  }

  public static void setCondition( String s )
  {
    conditionProperty().set( s );
  }

  public static void setSessionNumber( Integer n )
  {
    sessionNumberProperty().set( n );
  }
}
