package com.behaviorlogger.views.edit_schema;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.behaviorlogger.models.MappableChar;
import com.behaviorlogger.utils.BehaviorLoggerUtil;
import com.google.common.base.Functions;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class BehaviorBox extends VBox {
    public final String uuid;
    private SimpleBooleanProperty isContinuousProp;
    private SimpleStringProperty keyProp;
    private SimpleStringProperty descriptionProp;
    private SimpleBooleanProperty archivedProp;

    private HBox hbox = new HBox();
    private Label keyTakenLbl = new Label("That key is taken.");

    private Label keyText;
    private Label descText;
    private Separator separator = new Separator(Orientation.VERTICAL);

    private TextField keyField;
    private TextField descField;

    private Button archiveBtn = new Button("archive");
    private Button editBtn = new Button("edit");
    private Button reviveBtn = new Button("revive");
    private HBox actionBox = new HBox(archiveBtn, editBtn);

    public BehaviorBox(String uuid, MappableChar key, String description, boolean isContinuous, boolean archived,
	    Supplier<List<BehaviorBox>> getOthers) {
	super();

	this.uuid = uuid;
	this.isContinuousProp = new SimpleBooleanProperty(isContinuous);
	this.keyProp = new SimpleStringProperty(key.c + "");
	this.descriptionProp = new SimpleStringProperty(description);
	this.archivedProp = new SimpleBooleanProperty(archived);
	keyText = new Label(key.c + "");
	descText = new Label(description);
	keyField = new TextField(key.c + "");
	descField = new TextField(description);

	keyProp.addListener((o, ov, nv) -> keyText.setText(nv));
	descriptionProp.addListener((o, ov, nv) -> descText.setText(nv));

	keyText.setMinWidth(44);
	keyText.setAlignment(Pos.CENTER);
	descText.setMinWidth(80);
	keyField.setMaxWidth(44);
	descField.setMinWidth(80);
	editBtn.setMinWidth(40);
	archiveBtn.setMinWidth(50);
	reviveBtn.setMinWidth(40);
	actionBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
	HBox.setHgrow(descText, Priority.ALWAYS);
	HBox.setHgrow(descField, Priority.ALWAYS);
	HBox.setHgrow(actionBox, Priority.ALWAYS);
	keyTakenLbl.setTextFill(Color.RED);
	hbox.setPrefHeight(30);
	hbox.setMinHeight(USE_PREF_SIZE);
	hbox.setAlignment(Pos.CENTER);
	actionBox.setAlignment(Pos.CENTER);

	Font font = Font.font(12);
	editBtn.setFont(font);
	archiveBtn.setFont(font);
	reviveBtn.setFont(font);

	hbox.getChildren().addAll(keyText, separator, descText, actionBox);
	this.getChildren().addAll(hbox);

	this.setOnMouseEntered(evt -> this.setStyle("-fx-background-color:#e0e0e0;"));
	this.setOnMouseExited(evt -> this.setStyle(""));

	// Setup archive button
	archiveBtn.setOnAction(e -> {
	    deactivateBox();
	});

	// setup revive button
	reviveBtn.setOnAction(e -> {
	    if (isTaken(getKey(), getOthers.get())) {
		e.consume();
		displayKeyTakenLabel();
		return;
	    }

	    archivedProp.setValue(false);
	    actionBox.getChildren().clear();
	    actionBox.getChildren().addAll(archiveBtn, editBtn);
	    editBtn.setVisible(true);
	    keyText.setDisable(false);
	    descText.setDisable(false);
	    archiveBtn.requestFocus();
	});

	// If already archived, then click the archive btn
	if (archived) {
	    deactivateBox();
	}

	// Setup Edit Stuff
	SimpleBooleanProperty editingProp = new SimpleBooleanProperty(false);

	editBtn.setOnAction(e -> {
	    hbox.getChildren().clear();
	    hbox.getChildren().addAll(keyField, descField);
	    editingProp.set(true);
	    keyField.requestFocus();
	});

	EventHandler<? super KeyEvent> onEnter = evt -> {
	    if (evt.getCode().equals(KeyCode.ENTER) || evt.getCode().equals(KeyCode.ESCAPE)) {
		save();
		editBtn.requestFocus();
		return;
	    }
	};

	keyField.setOnKeyPressed(onEnter);
	descField.setOnKeyPressed(onEnter);
	BehaviorLoggerUtil.addLengthListener(descField, 100, s -> {
	});

	keyField.focusedProperty().addListener((old, ov, isFocused) -> {
	    if (!isFocused && !descField.isFocused()) {
		save();
	    }
	});

	descField.focusedProperty().addListener((old, ov, isFocused) -> {
	    if (!isFocused && !keyField.isFocused()) {
		save();
	    }
	});

	BehaviorLoggerUtil.addLimitingListener(keyField, text -> {
	    if (text.isEmpty()) {
		return true;
	    }
	    if (text.length() > 1) {
		return false;
	    }
	    Optional<MappableChar> optChar = MappableChar.getForChar(text.charAt(0));
	    if (!optChar.isPresent()) {
		return false;
	    }
	    MappableChar c = optChar.get();
	    if (isTaken(c, getOthers.get())) {
		displayKeyTakenLabel();
		return false;
	    }
	    return true;
	}, text -> {
	    this.getChildren().clear();
	    this.getChildren().addAll(hbox);
	});
    }

    Timeline timer;

    private void displayKeyTakenLabel() {
	this.getChildren().clear();
	this.getChildren().addAll(hbox, keyTakenLbl);

	if (timer != null) {
	    timer.playFromStart();
	} else {
	    timer = new Timeline();

	    BehaviorBox self = this;
	    timer.setCycleCount(1);
	    KeyFrame kf = new KeyFrame(Duration.seconds(4), evt -> {
		timer.stop();
		self.getChildren().clear();
		self.getChildren().addAll(hbox);
	    });

	    timer.getKeyFrames().add(kf);
	    timer.play();
	}
    }

    private void deactivateBox() {
	archivedProp.setValue(true);
	actionBox.getChildren().clear();
	actionBox.getChildren().addAll(reviveBtn, editBtn);
	editBtn.setVisible(false);
	keyText.setDisable(true);
	descText.setDisable(true);
	reviveBtn.requestFocus();
    }

    private boolean isTaken(MappableChar c, List<BehaviorBox> boxes) {
	for (BehaviorBox behaviorBox : boxes) {
	    if (behaviorBox == this) {
		continue;
	    }

	    if (!behaviorBox.isDeleted() && behaviorBox.getKey().equals(c)) {
		return true;
	    }
	}

	return false;
    }

    private void save() {
	this.getChildren().clear();
	this.getChildren().addAll(hbox);
	hbox.getChildren().clear();
	hbox.getChildren().addAll(keyText, separator, descText, actionBox);

	if (keyField.getText().isEmpty()) {
	    this.keyField.setText(this.keyProp.get());
	    return;
	}

	if (descField.getText().isEmpty()) {
	    this.descField.setText(this.descriptionProp.get());
	    return;
	}

	this.keyProp.setValue(keyField.getText());
	this.descriptionProp.setValue(descField.getText());
    }

    public String getUuid() {
	return this.uuid;
    }

    public boolean isContinuous() {
	return this.isContinuousProp.get();
    }

    public MappableChar getKey() {
	String text = this.keyProp.get();
	return MappableChar.getForString(text).get();
    }

    public String getDescription() {
	return this.descriptionProp.get();
    }

    public boolean isDeleted() {
	return this.archivedProp.get();
    }
}
