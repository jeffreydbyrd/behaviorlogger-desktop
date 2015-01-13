package com.threebird.recorder.models.sessions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.threebird.recorder.persistence.SessionDetails;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class SessionManager
{
  private static SimpleStringProperty observerProperty;
  private static SimpleStringProperty therapistProperty;
  private static SimpleStringProperty conditionProperty;
  private static SimpleIntegerProperty sessionNumberProperty;

  public static SimpleStringProperty observerProperty()
  {
    if (observerProperty == null) {
      observerProperty = new SimpleStringProperty();
      observerProperty.addListener( ( o, old, newV ) -> {
        SessionDetails.saveObserver( newV );
      } );
    }
    return observerProperty;
  }

  public static SimpleStringProperty therapistProperty()
  {
    if (therapistProperty == null) {
      therapistProperty = new SimpleStringProperty();
      therapistProperty.addListener( ( o, old, newV ) -> {
        SessionDetails.saveTherapist( newV );
      } );
    }
    return therapistProperty;
  }

  public static SimpleStringProperty conditionProperty()
  {
    if (conditionProperty == null) {
      conditionProperty = new SimpleStringProperty();
      conditionProperty.addListener( ( o, old, newV ) -> {
        SessionDetails.saveCondition( newV );
      } );
    }
    return conditionProperty;
  }

  public static SimpleIntegerProperty sessionNumberProperty()
  {
    if (sessionNumberProperty == null) {
      sessionNumberProperty = new SimpleIntegerProperty();
      sessionNumberProperty.addListener( ( o, old, newV ) -> {
        SessionDetails.saveSessionNumber( newV.intValue() );
      } );
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
