package com.threebird.recorder.models;

import java.io.File;

import javafx.beans.property.SimpleDoubleProperty;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.utils.resources.ResourceUtils;

/**
 * This simply exists to keep the state of windows' positions across sessions
 */
public class PositionManager
{
  private static class GsonBean
  {
    double mainXPos;
    double mainYPos;
    double mainHeight;
    double mainWidth;

    double notesXPos;
    double notesYPos;
    double notesHeight;
    double notesWidth;
  }

  private static SimpleDoubleProperty mainXPosProperty;
  private static SimpleDoubleProperty mainYPoxProperty;
  private static SimpleDoubleProperty mainHeightProperty;
  private static SimpleDoubleProperty mainWidthProperty;
  private static SimpleDoubleProperty notesXPosProperty;
  private static SimpleDoubleProperty notesYPosProperty;
  private static SimpleDoubleProperty notesHeightProperty;
  private static SimpleDoubleProperty notesWidthProperty;

  private static File file = ResourceUtils.getPositionDetails();
  private static Supplier< GsonBean > defaultModel = Suppliers.memoize( ( ) -> {
    GsonBean bean = new GsonBean();
    bean.mainWidth = 900;
    bean.mainHeight = 700;
    try {
      return GsonUtils.get( file, bean );
    } catch (Exception e) {
      // No err message...the user can still continue if this fails
      return bean;
    }
  } );

  private static void persist()
  {
    GsonBean model = new GsonBean();
    model.mainXPos = mainXProperty().get();
    model.mainYPos = mainYProperty().get();
    model.notesXPos = notesXProperty().get();
    model.notesYPos = notesYProperty().get();

    model.mainHeight = mainHeightProperty().get();
    model.mainWidth = mainWidthProperty().get();
    model.notesHeight = notesHeightProperty().get();
    model.notesWidth = notesWidthProperty().get();

    try {
      GsonUtils.save( file, model );
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static SimpleDoubleProperty notesXProperty()
  {
    if (notesXPosProperty == null) {
      notesXPosProperty = new SimpleDoubleProperty( defaultModel.get().notesXPos );
      notesXPosProperty.addListener( ( obs, old, newV ) -> persist() );
    }

    return notesXPosProperty;
  }

  public static SimpleDoubleProperty notesYProperty()
  {
    if (notesYPosProperty == null) {
      notesYPosProperty = new SimpleDoubleProperty( defaultModel.get().notesYPos );
      notesYPosProperty.addListener( ( obs, old, newV ) -> persist() );
    }

    return notesYPosProperty;
  }

  public static SimpleDoubleProperty mainXProperty()
  {
    if (mainXPosProperty == null) {
      mainXPosProperty = new SimpleDoubleProperty( defaultModel.get().mainXPos );
      mainXPosProperty.addListener( ( obs, old, newV ) -> persist() );
    }

    return mainXPosProperty;
  }

  public static SimpleDoubleProperty mainYProperty()
  {
    if (mainYPoxProperty == null) {
      mainYPoxProperty = new SimpleDoubleProperty( defaultModel.get().mainYPos );
      mainYPoxProperty.addListener( ( obs, old, newV ) -> persist() );
    }

    return mainYPoxProperty;
  }

  public static SimpleDoubleProperty mainHeightProperty()
  {
    if (mainHeightProperty == null) {
      mainHeightProperty = new SimpleDoubleProperty( defaultModel.get().mainHeight );
      mainHeightProperty.addListener( ( obs, old, newV ) -> persist() );
    }

    return mainHeightProperty;
  }

  public static SimpleDoubleProperty mainWidthProperty()
  {
    if (mainWidthProperty == null) {
      mainWidthProperty = new SimpleDoubleProperty( defaultModel.get().mainWidth );
      mainWidthProperty.addListener( ( obs, old, newV ) -> persist() );
    }

    return mainWidthProperty;
  }

  public static SimpleDoubleProperty notesHeightProperty()
  {
    if (notesHeightProperty == null) {
      notesHeightProperty = new SimpleDoubleProperty( defaultModel.get().notesHeight );
      notesHeightProperty.addListener( ( obs, old, newV ) -> persist() );
    }

    return notesHeightProperty;
  }

  public static SimpleDoubleProperty notesWidthProperty()
  {
    if (notesWidthProperty == null) {
      notesWidthProperty = new SimpleDoubleProperty( defaultModel.get().notesWidth );
      notesWidthProperty.addListener( ( obs, old, newV ) -> persist() );
    }

    return notesWidthProperty;
  }
}
