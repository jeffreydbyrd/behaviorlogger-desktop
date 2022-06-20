package com.behaviorlogger;

import com.behaviorlogger.controllers.StartMenuController;
import com.behaviorlogger.models.PositionManager;
import com.behaviorlogger.persistence.CreateResources;
import com.behaviorlogger.persistence.GsonUtils;
import com.behaviorlogger.persistence.InitSQLiteTables;
import com.behaviorlogger.persistence.recordings.Recordings;
import com.behaviorlogger.persistence.recordings.Recordings.Writer;
import com.behaviorlogger.utils.Alerts;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This is considered the main entry point by extending {@link Application}. The
 * one method we must override is start(Stage stage), which we invoke by calling
 * the launch(...) method.
 */
public class BehaviorLoggerApp extends Application {
    /**
     * A static variable for the Stage so that it's easily accessible later when we
     * want to switch scenes
     */
    public static Stage STAGE;

    public static String version = "1.1.3";

    public static void main(String[] args) {
	try {
	    launch(args);
	} catch (Exception e) {
	    Alerts.error("Fatal Error", "You can share this report with jeffreydbyrd@gmail.com.", e);
	}

	// Cleanup background threads
	GsonUtils.es.shutdown();
	for (Writer writer : Recordings.Writer.values()) {
	    writer.shutdown();
	}
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
	STAGE = primaryStage;

	CreateResources.apply();
	InitSQLiteTables.init();

	STAGE.setX(PositionManager.mainXProperty().doubleValue());
	STAGE.setY(PositionManager.mainYProperty().doubleValue());
	STAGE.xProperty().addListener((obs, old, xpos) -> PositionManager.mainXProperty().setValue(xpos));
	STAGE.yProperty().addListener((obs, old, ypos) -> PositionManager.mainYProperty().setValue(ypos));

	if (PositionManager.mainHeightProperty().doubleValue() != 0
		|| PositionManager.mainWidthProperty().doubleValue() != 0) {
	    STAGE.setHeight(PositionManager.mainHeightProperty().get());
	    STAGE.setWidth(PositionManager.mainWidthProperty().get());
	}
	STAGE.heightProperty().addListener((obs, old, h) -> PositionManager.mainHeightProperty().setValue(h));
	STAGE.widthProperty().addListener((obs, old, w) -> PositionManager.mainWidthProperty().setValue(w));

	StartMenuController.toStartMenuView();
    }
}
