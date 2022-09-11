package com.behaviorlogger.models;

import java.io.File;
import java.util.Optional;

import com.behaviorlogger.persistence.GsonUtils;
import com.behaviorlogger.utils.resources.ResourceUtils;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

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
	int backgroundNumEventsV2;
	boolean debugBackgroundRandomSamplingSelected;
	String calculationType;
	String operationType;

	@Deprecated
	@SuppressWarnings("unused")
	String backgroundNumEvents;
    }

    private static SimpleStringProperty fileProperty;
    private static SimpleBooleanProperty appendSelectedProperty;
    private static SimpleStringProperty appendFileProperty;
    private static SimpleIntegerProperty windowProperty;
    private static SimpleBooleanProperty backgroundRandomSamplingSelectedProperty;
    private static SimpleIntegerProperty backgroundNumEventsProperty;
    private static SimpleBooleanProperty debugBackgroundRandomSamplingSelectedProperty;
    private static SimpleStringProperty calculationTypeProperty;
    private static SimpleStringProperty operationTypeProperty;

    private static File file = ResourceUtils.getConditionalProbabilityDetails();

    public static String CALCULATION_BINARY = "CALCULATION_BINARY";
    public static String CALCULATION_PROPORTIONAL = "CALCULATION_PROPORTIONAL";
    public static String OPERATION_ESTABLISHING = "OPERATION_ESTABLISHING";
    public static String OPERATION_NON_ESTABLISHING = "OPERATION_NON_ESTABLISHING";

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
	model.backgroundNumEventsV2 = backgroundNumEventsProperty().get();
	model.debugBackgroundRandomSamplingSelected = debugBackgroundRandomSamplingSelectedProperty().get();
	model.calculationType = calculationTypeProperty.get();
	model.operationType = operationTypeProperty.get();

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

    public static SimpleIntegerProperty backgroundNumEventsProperty() {
	if (backgroundNumEventsProperty == null) {
	    backgroundNumEventsProperty = new SimpleIntegerProperty(defaultModel.get().backgroundNumEventsV2);
	    backgroundNumEventsProperty.addListener((o, old, newV) -> persist());
	}
	return backgroundNumEventsProperty;
    }

    public static SimpleBooleanProperty debugBackgroundRandomSamplingSelectedProperty() {
	if (debugBackgroundRandomSamplingSelectedProperty == null) {
	    debugBackgroundRandomSamplingSelectedProperty = new SimpleBooleanProperty(
		    defaultModel.get().debugBackgroundRandomSamplingSelected);
	    debugBackgroundRandomSamplingSelectedProperty.addListener((o, old, newV) -> persist());
	}
	return debugBackgroundRandomSamplingSelectedProperty;
    }

    public static SimpleStringProperty calculationTypeProperty() {
	if (calculationTypeProperty == null) {
	    calculationTypeProperty = new SimpleStringProperty(defaultModel.get().calculationType);
	    calculationTypeProperty.addListener((o, old, newV) -> persist());
	}
	return calculationTypeProperty;
    }

    public static SimpleStringProperty operationTypeProperty() {
	if (operationTypeProperty == null) {
	    operationTypeProperty = new SimpleStringProperty(defaultModel.get().operationType);
	    operationTypeProperty.addListener((o, old, newV) -> persist());
	}
	return operationTypeProperty;
    }
}
