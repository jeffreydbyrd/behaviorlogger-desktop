package com.threebird.recorder.models;

import java.io.File;
import java.util.Optional;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.utils.resources.ResourceUtils;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ConditionalProbabilityManager {

    private static class GsonBean {
	String file;
	boolean appendSelected;
	String appendFile;
	int window;
	boolean backgroundRandomSamplingSelected;
	String backgroundNumEvents;
    }

    private static SimpleStringProperty fileProperty;
    private static SimpleBooleanProperty appendSelectedProperty;
    private static SimpleStringProperty appendFileProperty;
    private static SimpleIntegerProperty windowProperty;
    private static SimpleBooleanProperty backgroundRandomSamplingSelectedProperty;
    private static SimpleStringProperty backgroundNumEventsProperty;

    private static File file = ResourceUtils.getConditionalProbabilityDetails();

    private static Supplier<GsonBean> defaultModel = Suppliers.memoize(() -> {
	GsonBean bean = new GsonBean();
	try {
	    return GsonUtils.get(file, bean);
	} catch (Exception e) {
	    return bean;
	}
    });

    private static void persist() {
	GsonBean model = new GsonBean();
	model.file = fileProperty().get();
	model.appendSelected = appendSelectedProperty().get();
	model.appendFile = appendFileProperty().get();
	model.window = windowProperty().get();
	model.backgroundRandomSamplingSelected = backgroundRandomSamplingSelectedProperty().get();
	model.backgroundNumEvents = backgroundNumEventsProperty().get();

	try {
	    GsonUtils.save(file, model);
	} catch (Exception e) {
	    // XXX: No err message...the user can still continue if this fails
	    e.printStackTrace();
	}
    }

    public static SimpleStringProperty fileProperty() {
	if (fileProperty == null) {
	    fileProperty = new SimpleStringProperty(defaultModel.get().file);
	    fileProperty.addListener((o, old, newV) -> persist());
	}
	return fileProperty;
    }

    public static SimpleBooleanProperty appendSelectedProperty() {
	if (appendSelectedProperty == null) {
	    appendSelectedProperty = new SimpleBooleanProperty(defaultModel.get().appendSelected);
	    appendSelectedProperty.addListener((o, old, newV) -> persist());
	}
	return appendSelectedProperty;
    }

    public static SimpleStringProperty appendFileProperty() {
	if (appendFileProperty == null) {
	    appendFileProperty = new SimpleStringProperty(defaultModel.get().appendFile);
	    appendFileProperty.addListener((o, old, newV) -> persist());
	}
	return appendFileProperty;
    }

    public static Optional<String> getAppendFile() {
	return Optional.ofNullable(appendFileProperty().getValue());
    }

    public static SimpleIntegerProperty windowProperty() {
	if (windowProperty == null) {
	    int window = defaultModel.get().window;
	    if (window <= 0) {
		window = 10;
	    }
	    windowProperty = new SimpleIntegerProperty(window);
	    windowProperty.addListener((o, old, newV) -> persist());
	}
	return windowProperty;
    }

    public static SimpleBooleanProperty backgroundRandomSamplingSelectedProperty() {
	if (backgroundRandomSamplingSelectedProperty == null) {
	    backgroundRandomSamplingSelectedProperty = new SimpleBooleanProperty(
		    defaultModel.get().backgroundRandomSamplingSelected);
	    backgroundRandomSamplingSelectedProperty.addListener((o, old, newV) -> persist());
	}
	return backgroundRandomSamplingSelectedProperty;
    }

    public static SimpleStringProperty backgroundNumEventsProperty() {
	if (backgroundNumEventsProperty == null) {
	    backgroundNumEventsProperty = new SimpleStringProperty(defaultModel.get().backgroundNumEvents);
	    backgroundNumEventsProperty.addListener((o, old, newV) -> persist());
	}
	return backgroundNumEventsProperty;
    }
}
