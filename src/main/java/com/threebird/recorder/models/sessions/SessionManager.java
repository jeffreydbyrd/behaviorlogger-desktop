package com.threebird.recorder.models.sessions;

import java.io.File;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.utils.resources.ResourceUtils;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class SessionManager
{
  private static class GsonBean
  {
    String observer;
    String therapist;
    String condition;
    String location;
    int sessionNumber;
  }

  private static SimpleStringProperty observerProperty;
  private static SimpleStringProperty therapistProperty;
  private static SimpleStringProperty conditionProperty;
  private static SimpleStringProperty locationProperty;
  private static SimpleIntegerProperty sessionNumberProperty;

  private static File file = ResourceUtils.getSessionDetails();
  private static Supplier< GsonBean > defaultModel = Suppliers.memoize( () -> {
    GsonBean bean = new GsonBean();
    try {
      return GsonUtils.get( file, bean );
    } catch (Exception e) {
      e.printStackTrace();
      return bean;
      // XXX: No err message...the user can still continue if this fails
    }
  } );

  private static void persist()
  {
    GsonBean model = new GsonBean();
    model.observer = getObserver();
    model.therapist = getTherapist();
    model.condition = getCondition();
    model.location = getLocation();
    model.sessionNumber = getSessionNumber();

    try {
      GsonUtils.save( file, model );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static SimpleStringProperty observerProperty()
  {
    if (observerProperty == null) {
      observerProperty = new SimpleStringProperty( Strings.nullToEmpty( defaultModel.get().observer ) );
      observerProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return observerProperty;
  }

  public static SimpleStringProperty therapistProperty()
  {
    if (therapistProperty == null) {
      therapistProperty = new SimpleStringProperty( Strings.nullToEmpty( defaultModel.get().therapist ) );
      therapistProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return therapistProperty;
  }

  public static SimpleStringProperty conditionProperty()
  {
    if (conditionProperty == null) {
      conditionProperty = new SimpleStringProperty( Strings.nullToEmpty( defaultModel.get().condition ) );
      conditionProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return conditionProperty;
  }

  public static SimpleStringProperty locationProperty()
  {
    if (locationProperty == null) {
      locationProperty = new SimpleStringProperty( Strings.nullToEmpty( defaultModel.get().location ) );
      locationProperty.addListener( ( o, old, newV ) -> persist() );
    }
    return locationProperty;
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

  public static String getLocation()
  {
    return locationProperty().get();
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

  public static void setLocation( String s )
  {
    locationProperty().set( s );
  }

  public static void setSessionNumber( Integer n )
  {
    sessionNumberProperty().set( n );
  }
}
