package com.behaviorlogger.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.behaviorlogger.models.MappableChar;
import com.behaviorlogger.models.behaviors.ContinuousBehavior;
import com.behaviorlogger.models.behaviors.DiscreteBehavior;
import com.behaviorlogger.models.schemas.KeyBehaviorMapping;
import com.behaviorlogger.models.schemas.SchemaVersion;
import com.behaviorlogger.models.schemas.SchemasManager;
import com.behaviorlogger.models.sessions.ContinuousCounter;
import com.behaviorlogger.models.sessions.RecordingManager;
import com.behaviorlogger.models.sessions.SessionManager;
import com.behaviorlogger.utils.BehaviorLoggerUtil;
import com.behaviorlogger.views.recording.BehaviorCountBox;
import com.behaviorlogger.views.recording.ContinuousCountBox;
import com.behaviorlogger.views.recording.DiscreteCountBox;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controls recording.fxml
 */
public class RecordingController {
    private RecordingManager manager;

    @FXML
    private Label clientLabel;
    @FXML
    private Label projectLabel;
    @FXML
    private HBox sessionDetailsBox;

    @FXML
    private GridPane behaviorGrid;
    @FXML
    private VBox discreteBox;
    @FXML
    private VBox continuousBox;
    private Map<MappableChar, BehaviorCountBox> countBoxes = Maps.newHashMap();

    @FXML
    private Text pausedText;
    @FXML
    private Text recordingText;
    @FXML
    private Label timeBox;

    @FXML
    private Label spacebarLbl;
    @FXML
    private StackPane saveLabelPane;
    @FXML
    private Label failedLabel;
    @FXML
    private Label savedLabel;

    @FXML
    private Button playButton;
    @FXML
    private Button notesButton;
    @FXML
    private Button goBackButton;
    @FXML
    private Button newSessionButton;
    @FXML
    private Button addNewKeysButton;

    private Stage notesStage;

    /**
     * Sets the stage to the Recording view
     */
    public static void toRecordingView() {
	String filepath = "views/recording/recording.fxml";
	RecordingController controller = BehaviorLoggerUtil.loadScene(filepath, "Recording");
	controller.init();
    }

    private void init() {
	SchemaVersion schema = SchemasManager.getSelected();
	manager = new RecordingManager();

	behaviorGrid.setDisable(true);
	timeBox.setDisable(true);

	clientLabel.setText(schema.client);
	projectLabel.setText(schema.project);

	if (!Strings.isNullOrEmpty(SessionManager.getObserver())) {
	    String obsrvr = "Observer: " + SessionManager.getObserver();
	    sessionDetailsBox.getChildren().add(new Label(obsrvr));
	}

	if (!Strings.isNullOrEmpty(SessionManager.getTherapist())) {
	    String therapist = "Therapist: " + SessionManager.getTherapist();
	    sessionDetailsBox.getChildren().add(new Label(therapist));
	}

	if (!Strings.isNullOrEmpty(SessionManager.getCondition())) {
	    String condition = "Condition: " + SessionManager.getCondition();
	    sessionDetailsBox.getChildren().add(new Label(condition));
	}

	if (!Strings.isNullOrEmpty(SessionManager.getLocation())) {
	    String condition = "Location: " + SessionManager.getLocation();
	    sessionDetailsBox.getChildren().add(new Label(condition));
	}

	if (SessionManager.getSessionNumber() != null) {
	    String session = "Session: " + SessionManager.getSessionNumber();
	    sessionDetailsBox.getChildren().add(new Label(session));
	}

	savedLabel.setText("Saved data to " + RecordingManager.getFullFileName() + "(.raw/.xls)");
	savedLabel.setVisible(false);
	// saveLabelPane.setVisible( false );

	initializeTimer();
	initializeBehaviorCountBoxes();

	manager.playingProperty.addListener((obs, oldV, playing) -> onPlayToggled(playing));
	manager.saveSuccessfulProperty.addListener((obs, oldV, success) -> onSaveSuccessfulToggled(success));
    }

    public void update() {
	SchemaVersion schema = SchemasManager.getSelected();

	// Figure out which unknowns were ignored and which were updated
	Set<MappableChar> mappedChars = schema.behaviorsMap().keySet();
	Set<MappableChar> unknownChars = manager.unknowns.keySet();
	SetView<MappableChar> ignoredChars = Sets.difference(unknownChars, mappedChars);
	SetView<MappableChar> newChars = Sets.intersection(mappedChars, unknownChars);

	// Remove the [unknown]s we don't care about from the GUI
	for (MappableChar ignored : ignoredChars) {
	    BehaviorCountBox countBox = countBoxes.get(ignored);
	    boolean isDiscrete = discreteBox.getChildren().contains(countBox);
	    List<Node> target = isDiscrete ? discreteBox.getChildren() : continuousBox.getChildren();

	    int i = target.indexOf(countBox);
	    target.remove(i + 1);
	    target.remove(i);

	    countBoxes.remove(ignored);

	    if (isDiscrete) {
		manager.discreteCounts.remove(ignored);
	    } else {
		manager.continuousCounts.remove(ignored);
	    }
	}

	// Update the CountBoxes labels
	for (MappableChar newChar : newChars) {
	    KeyBehaviorMapping kbm = schema.behaviorsMap().get(newChar);
	    countBoxes.get(kbm.key).behaviorLbl.setText(kbm.description);
	}

	addNewKeysButton.setVisible(false);
    }

    /**
     * Populates 'discreteBox' and the 'continuousBox' with the selected Schema's
     * mappings
     */
    private void initializeBehaviorCountBoxes() {
	SchemaVersion schema = SchemasManager.getSelected();

	for (KeyBehaviorMapping kbm : schema.behaviors) {
	    if (!kbm.archived) {
		initializeBehaviorCountBox(kbm);
	    }
	}
    }

    private void initializeBehaviorCountBox(KeyBehaviorMapping kbm) {
	BehaviorCountBox bcb;
	VBox target;
	SimpleIntegerProperty count = new SimpleIntegerProperty(0);

	if (kbm.isContinuous) {
	    bcb = new ContinuousCountBox(kbm);
	    target = continuousBox;
	    initializeContinuousCountBox(kbm, count);
	} else {
	    bcb = new DiscreteCountBox(kbm);
	    target = discreteBox;
	    manager.discreteCounts.put(kbm.key, count);
	}

	count.addListener((obs, old, newv) -> bcb.setCount(newv.intValue()));
	target.getChildren().add(bcb);
	target.getChildren().add(new Separator());

	countBoxes.put(kbm.key, bcb);
    }

    private void initializeContinuousCountBox(KeyBehaviorMapping kbm, SimpleIntegerProperty count) {
	Timeline timer = new Timeline();

	timer.setCycleCount(Animation.INDEFINITE);
	KeyFrame kf = new KeyFrame(Duration.millis(1), evt -> {
	    count.set(count.get() + 1);
	});
	timer.getKeyFrames().add(kf);

	manager.playingProperty.addListener((obs, oldV, playing) -> {
	    if (manager.midContinuous.containsKey(kbm.key)) {
		if (playing) {
		    timer.play();
		} else {
		    timer.pause();
		}
	    }
	});

	manager.continuousCounts.put(kbm.key, new ContinuousCounter(timer, count));
    }

    /**
     * Sets up the 'timer' field to call "onTick" every second.
     */
    private void initializeTimer() {
	manager.counter.addListener((ctr, old, count) -> onTick(count.intValue()));
    }

    /**
     * Every millisecond, update the counter. When the counter reaches the duration,
     * try to signal the user
     */
    private void onTick(int millis) {
	SchemaVersion schema = SchemasManager.getSelected();
	timeBox.setText(BehaviorLoggerUtil.millisToTimestamp(millis));

	if (millis == schema.duration) {
	    if (schema.color) {
		timeBox.setStyle("-fx-background-color: #FFC0C0;-fx-border-color:red;-fx-border-radius:2;");
	    }

	    if (schema.pause) {
		manager.playingProperty.set(false);
	    }

	    if (schema.sound) {
		java.awt.Toolkit.getDefaultToolkit().beep();
	    }
	}
    }

    /**
     * Starts and stops recording, changes the playButton text appropriately.
     */
    private void onPlayToggled(boolean playing) {
	if (playing) {
	    manager.timer.play();
	    saveLabelPane.setVisible(false);
	} else {
	    manager.timer.pause();
	    saveLabelPane.setVisible(true);

	    // trigger all mid-continuous keys
	    List<KeyBehaviorMapping> midContinousKeys = new ArrayList<KeyBehaviorMapping>();
	    for (Entry<MappableChar, ContinuousBehavior> e : manager.midContinuous.entrySet()) {
		ContinuousBehavior cb = e.getValue();
		KeyBehaviorMapping kbm = new KeyBehaviorMapping(cb.uuid, e.getKey(), cb.name, true, false);
		midContinousKeys.add(kbm);
	    }
	    for (KeyBehaviorMapping kbm : midContinousKeys) {
		logBehavior(kbm);
	    }
	}

	behaviorGrid.setDisable(!behaviorGrid.isDisabled());
	timeBox.setDisable(!timeBox.isDisabled());
	spacebarLbl.setText(playing ? "Spacebar = Stop" : "Spacebar = Continue");
	playButton.setText(playing ? "Stop" : "Continue");
	goBackButton.setVisible(!playing);
	newSessionButton.setVisible(!playing);
	recordingText.setVisible(!recordingText.isVisible());
	pausedText.setVisible(!pausedText.isVisible());

	if (!manager.unknowns.isEmpty()) {
	    addNewKeysButton.setVisible(!playing);
	}
    }

    private void onSaveSuccessfulToggled(Boolean saveSuccessful) {
	if (saveSuccessful != null) {
	    failedLabel.setVisible(!saveSuccessful);
	    savedLabel.setVisible(saveSuccessful);
	}
    }

    /**
     * If the event is executing a shortcut, then execute the shortcut and return
     * true, else return false.
     */
    private boolean handleShortcut(KeyEvent evt) {
	KeyCode c = evt.getCode();

	if (KeyCode.SPACE.equals(c)) {
	    manager.togglePlayingProperty();
	    return true;
	}

	if (KeyCode.Z.equals(c) && evt.isShortcutDown()) {
	    undo();
	    return true;
	}

	if (KeyCode.N.equals(c) && evt.isShortcutDown()) {
	    onNotesPress();
	    return true;
	}

	return false;
    }

    /**
     * Returns the latest, actively running, ContinuousBehavior or null if there are
     * none running
     */
    private ContinuousBehavior getLatestRunningContinuous() {
	ContinuousBehavior result = null;

	for (Entry<MappableChar, ContinuousBehavior> entry : manager.midContinuous.entrySet()) {
	    ContinuousBehavior cb = entry.getValue();
	    if (result == null || result.startTime < cb.startTime) {
		result = cb;
	    }
	}

	return result;
    }

    /**
     * Removes the latest logged behavior from either the Discrete or Continuous
     * list (whichever is latest)
     */
    private void undo() {
	int lastIndexDiscrete = manager.discrete.size() - 1;
	int lastIndexContinuous = manager.continuous.size() - 1;

	DiscreteBehavior db = lastIndexDiscrete < 0 ? null : manager.discrete.get(lastIndexDiscrete);
	ContinuousBehavior cb = lastIndexContinuous < 0 ? null : manager.continuous.get(lastIndexContinuous);
	ContinuousBehavior midCb = getLatestRunningContinuous();

	if (discreteIsLatest(db, cb, midCb)) {
	    removeLatestDiscrete();
	    return;
	}

	if (continuousIsLatest(db, cb, midCb)) {
	    removeLatestContinuous();
	    return;
	}

	if (midContinuousIsLatest(db, cb, midCb)) {
	    removeMidContinuous(midCb);
	    return;
	}
    }

    private boolean discreteIsLatest(DiscreteBehavior db, ContinuousBehavior cb, ContinuousBehavior midCb) {
	if (db == null) {
	    return false;
	}

	if (cb != null && db.startTime < cb.startTime + cb.getDuration()) {
	    return false;
	}

	if (midCb != null && db.startTime < midCb.startTime) {
	    return false;
	}

	return true;
    }

    private boolean continuousIsLatest(DiscreteBehavior db, ContinuousBehavior cb, ContinuousBehavior midCb) {
	if (cb == null) {
	    return false;
	}

	long endTime = cb.startTime + cb.getDuration();

	if (db != null && db.startTime > endTime) {
	    return false;
	}

	if (midCb != null && endTime < midCb.startTime) {
	    return false;
	}

	return true;
    }

    private boolean midContinuousIsLatest(DiscreteBehavior db, ContinuousBehavior cb, ContinuousBehavior midCb) {
	if (midCb == null) {
	    return false;
	}

	if (db != null && db.startTime > midCb.startTime) {
	    return false;
	}

	if (cb != null && cb.startTime + cb.getDuration() > midCb.startTime) {
	    return false;
	}

	return true;
    }

    private void removeLatestDiscrete() {
	int lastIndexDiscrete = manager.discrete.size() - 1;
	if (lastIndexDiscrete < 0) {
	    return;
	}

	DiscreteBehavior db = manager.discrete.get(lastIndexDiscrete);
	manager.discrete.remove(lastIndexDiscrete);
	SimpleIntegerProperty count = manager.discreteCounts.get(db.key);
	count.set(count.get() - 1);
    }

    private void removeLatestContinuous() {
	int lastIndexContinuous = manager.continuous.size() - 1;
	if (lastIndexContinuous < 0) {
	    return;
	}

	ContinuousBehavior cb = manager.continuous.get(lastIndexContinuous);
	manager.continuous.remove(lastIndexContinuous);

	ContinuousCounter counter = manager.continuousCounts.get(cb.key);
	counter.count.set(counter.count.get() - cb.getDuration());
    }

    private void removeMidContinuous(ContinuousBehavior midCb) {
	manager.midContinuous.remove(midCb.key);

	ContinuousCounter counter = manager.continuousCounts.get(midCb.key);
	counter.timer.stop();
	long duration = manager.count() - midCb.startTime;
	counter.count.set(counter.count.get() - (int) duration);

	countBoxes.get(midCb.key).toggle();
    }

    private void logBehavior(KeyBehaviorMapping mapping) {
	countBoxes.get(mapping.key).toggle();

	if (mapping.isContinuous) {
	    logContinuous(mapping);
	} else {
	    manager.log(new DiscreteBehavior(mapping.uuid, mapping.key, mapping.description, manager.count()));
	    SimpleIntegerProperty count = manager.discreteCounts.get(mapping.key);
	    count.set(count.get() + 1);
	}
    }

    /**
     * Converts the KeyBehaviorMapping to a ContinuousBehavior and registers it with
     * the manager. If the behavior hasn't been initialized, it gets cached in
     * manager.midContinuous. If it *has* been initialized, it gets moved into
     * manager.continuous.
     */
    private void logContinuous(KeyBehaviorMapping mapping) {
	if (manager.midContinuous.containsKey(mapping.key)) {
	    ContinuousBehavior cb = manager.midContinuous.get(mapping.key);
	    long duration = manager.count() - cb.startTime;
	    manager.log(new ContinuousBehavior(cb.uuid, cb.key, cb.name, cb.startTime, (int) duration));
	    manager.midContinuous.remove(mapping.key);
	    manager.continuousCounts.get(mapping.key).timer.pause();
	} else {
	    ContinuousBehavior cb = new ContinuousBehavior(mapping.uuid, mapping.key, mapping.description,
		    manager.count(), 0);
	    manager.midContinuous.put(mapping.key, cb);
	    manager.continuousCounts.get(mapping.key).timer.play();
	}
    }

    /**
     * The user just pressed a key that isn't mapped. Add it to the 'unknowns' map
     */
    private void initUnknown(MappableChar mc, boolean isContinuous) {
	KeyBehaviorMapping kbm = new KeyBehaviorMapping(null, mc, "[unknown]", isContinuous, false);
	initializeBehaviorCountBox(kbm);
	manager.unknowns.put(mc, kbm);
	logBehavior(kbm);
    }

    /**
     * Attached to the root pane, onKeyPressed should fire when the user types a
     * key, no matter what is selected
     */
    @FXML
    private void onKeyPressed(KeyEvent evt) {
	boolean isShortcut = handleShortcut(evt);
	if (isShortcut) {
	    return;
	}

	if (!manager.playingProperty.get()) {
	    return;
	}

	KeyCode code = evt.getCode();
	MappableChar.getForKeyCode(code).ifPresent(mc -> {
	    SchemaVersion schema = SchemasManager.getSelected();
	    if (schema.behaviorsMap().containsKey(mc)) {
		KeyBehaviorMapping kbm = schema.behaviorsMap().get(mc);
		if (!kbm.archived) {
		    logBehavior(schema.behaviorsMap().get(mc));
		}
	    } else if (manager.unknowns.containsKey(mc)) {
		logBehavior(manager.unknowns.get(mc));
	    } else {
		initUnknown(mc, evt.isShiftDown());
	    }
	});
    }

    @FXML
    private void onPlayPress(ActionEvent evt) {
	manager.togglePlayingProperty();
    }

    @FXML
    private void onNotesPress() {
	if (notesStage == null) {
	    notesStage = new Stage();
	    NotesController.bindNotesToStage(notesStage, manager);
	}

	if (!notesStage.isShowing()) {
	    notesStage.show();
	}
    }

    /**
     * If unknown behaviors exist, prompt the user to edit them. If user declines,
     * run 'toScene'. If user accepts, go to add-keys view
     * 
     * @param toScene - a void function that should change scenes
     */
    private void checkUnknownsAndChangeScene(Runnable toScene) {
	if (!manager.unknowns.isEmpty()) {
	    String msg = "You have recorded unknown behaviors. Would you like to edit them?";
	    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
	    alert.setTitle("Confirm deletion.");
	    alert.setHeaderText(null);
	    alert.setContentText(msg);
	    ButtonType editBtn = new ButtonType("Edit Unknowns");
	    ButtonType discardBtn = new ButtonType("Discard Unknowns", ButtonData.CANCEL_CLOSE);
	    alert.getButtonTypes().setAll(discardBtn, editBtn);
	    alert.showAndWait().ifPresent(r -> {
		if (r == editBtn) {
		    AddKeysController.showAddKeysView(manager, this::update);
		} else {
		    toScene.run();
		}
	    });
	} else {
	    toScene.run();
	}
    }

    @FXML
    private void onGoBackPress(ActionEvent evt) {
	Integer sessionNum = SessionManager.getSessionNumber();

	checkUnknownsAndChangeScene(() -> {
	    if (sessionNum != null && manager.timer.getCurrentTime().greaterThan(Duration.ZERO)) {
		SessionManager.setSessionNumber(sessionNum + 1);
	    }
	    StartMenuController.toStartMenuView();
	});
    }

    @FXML
    private void onNewSessionPress(ActionEvent evt) {
	Integer sessionNum = SessionManager.getSessionNumber();

	checkUnknownsAndChangeScene(() -> {
	    if (sessionNum != null) {
		SessionManager.setSessionNumber(sessionNum + 1);
	    }
	    RecordingController.toRecordingView();
	});
    }

    @FXML
    private void onAddNewKeysPress(ActionEvent evt) {
	AddKeysController.showAddKeysView(manager, this::update);
    }

    @FXML
    private void onHelpBtnPressed() {
	BehaviorLoggerUtil.openManual("recording");
    }
}
