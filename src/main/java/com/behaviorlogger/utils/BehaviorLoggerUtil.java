package com.behaviorlogger.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.function.Consumer;

import com.behaviorlogger.BehaviorLoggerApp;
import com.behaviorlogger.utils.resources.ResourceUtils;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * A collection of helpful functions used throughout the app
 */
public class BehaviorLoggerUtil {

    public static Supplier<Stage> dialogStage = Suppliers.memoize(() -> {
	Stage s = new Stage();
	s.initModality(Modality.APPLICATION_MODAL);
	s.initStyle(StageStyle.UTILITY);
	s.initOwner(BehaviorLoggerApp.STAGE);
	return s;
    });

    /**
     * @param window    - the containing window that will own the FileChooser
     * @param textField - the TextField that we will populate after the user chooses
     *                  a file
     */
    public static void chooseFile(Stage window, TextField textField) {
	File f = new File(textField.getText().trim());
	if (!f.exists()) {
	    f = new File(System.getProperty("user.home"));
	}

	DirectoryChooser dirChooser = new DirectoryChooser();
	dirChooser.setInitialDirectory(f);
	File newFile = dirChooser.showDialog(window);

	if (newFile != null) {
	    textField.setText(newFile.getPath());
	}
    }

    /**
     * Displays a dialog window containing the resource specified by fxmlPath
     * 
     * @param stage    - the stage that will house the new scene
     * @param fxmlPath - the file path to the fxml template (relative to
     *                 EventRecorder.java)
     * @param title    - the title of the scene
     * @return the controller T associated with the fxml resource specified
     */
    public static <T> T showScene(String fxmlPath, String title) {
	FXMLLoader fxmlLoader = new FXMLLoader(BehaviorLoggerApp.class.getResource(fxmlPath));

	Parent root;
	try {
	    root = (Parent) fxmlLoader.load();
	} catch (IOException e) {
	    Alerts.error("Error Loading Resource", "There was a problem loading a resource: " + fxmlPath, e);
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}
	Scene scene = new Scene(root);

	dialogStage.get().setTitle(title);
	dialogStage.get().setScene(scene);
	dialogStage.get().show();

	return fxmlLoader.<T>getController();
    }

    /**
     * Loads an FXML file into EventRecorder.STAGE's current scene
     * 
     * @param T        - the type of the Controller you want returned to you
     * @param fxmlPath - the path (relative to EventRecorder.java's location) to the
     *                 FXML resource from which we will derive this scene
     * @param title    - the title of this new scene
     * @return the Controller linked to from the FXML file
     */
    public static <T> T loadScene(String fxmlPath, String title) {
	FXMLLoader fxmlLoader = new FXMLLoader(BehaviorLoggerApp.class.getResource(fxmlPath));

	Parent root;
	try {
	    root = (Parent) fxmlLoader.load();
	} catch (IOException e) {
	    Alerts.error("Error Loading Resource", "There was a problem loading a resource: " + fxmlPath, e);
	    e.printStackTrace();
	    throw new RuntimeException(e);
	}

	Scene scene = BehaviorLoggerApp.STAGE.getScene();
	if (scene == null) {
	    scene = new Scene(root);
	    BehaviorLoggerApp.STAGE.setScene(scene);
	} else {
	    scene.setRoot(root);
	}

	BehaviorLoggerApp.STAGE.setTitle(title);
	BehaviorLoggerApp.STAGE.show();

	return fxmlLoader.<T>getController();
    }

    /**
     * Creates an EventHandler that will prevent a field's text from exceeding the
     * character limit
     * 
     * @param limit - the max length of the field
     * @return an EventHandler that consumes a KeyEvent if the length of 'field' is
     *         longer than 'limit'
     */
    public static EventHandler<? super KeyEvent> createFieldLimiter(int limit) {
	return evt -> {
	    Object source = evt.getSource();
	    if (source instanceof TextField) {
		String text = Strings.nullToEmpty(((TextField) source).getText());
		if (text.length() >= limit) {
		    evt.consume();
		}
	    }
	};
    }

    /**
     * Creates an EventHandler that will prevent a field's text from containing any
     * characters not in "acceptableKeys" and from exceeding the character limit
     * 
     * @param acceptableKeys a list of characters that the user is allowed to input
     * @param limit          the max length of the field; if limit <= 0 then it's
     *                       treated as unlimited
     * @return an EventHandler that consumes a KeyEvent if the typed Char is outside
     *         'acceptableKeys' or if the length of 'field' is longer than 'limit'
     */
    public static EventHandler<? super KeyEvent> createFieldLimiter(char[] acceptableKeys, int limit) {
	return evt -> {
	    Object source = evt.getSource();
	    if (source instanceof TextField) {
		if ((limit > 0 && ((TextField) source).getText().length() >= limit)
			|| !String.valueOf(acceptableKeys).contains(evt.getCharacter())) {
		    evt.consume();
		}
	    }
	};
    }
    
    /**
     * Adds a Listener to the TextField that will prevent its text from containing any
     * characters not in "acceptable" and from exceeding the character limit.
     *
     * @param textField   the textField to add the listener to
     * @param acceptable  a list of characters that the user is allowed to input
     * @param limit       the max length of the field; if limit <= 0 then it's
     *                    treated as unlimited
     * @param callback    a callback that gets called with *valid* values entered into textField
     */
    public static void addLimitingListener(TextField textField, String acceptable, int limit, Consumer<String> callback) {
	textField.textProperty().addListener((o, old, newV) -> {
	    boolean hasAcceptableValues = newV.chars().allMatch(c -> acceptable.indexOf(c) >= 0);
	    if (!hasAcceptableValues) {
		textField.setText(old);
		return;
	    }
	    if (limit < 0) {
		callback.accept(newV);
		return;
	    }
	    if (newV.length() > limit) {
		textField.setText(old);
		return;
	    }
	    callback.accept(newV);
	});
    }

    public static void addLengthListener(TextField textField, int len, Consumer<String> callback) {
	textField.textProperty().addListener((o, old, newV) -> {
	    if (len < 0) {
		callback.accept(newV);
		return;
	    }
	    if (newV.length() > len) {
		textField.setText(old);
		return;
	    }
	    callback.accept(newV);
	});
    }

    public static void addIntegerListener(TextField textField, Consumer<Integer> callback) {
	textField.textProperty().addListener((o, old, newV) -> {
	    if (newV.isEmpty()) {
		callback.accept(0);
		return;
	    }
	    if (!newV.matches("\\d*")) {
		textField.setText(old);
		return;
	    }
	    callback.accept(Integer.parseInt(newV));
	});
    }

    public static int strToInt(String s) {
	return Integer.valueOf(Strings.isNullOrEmpty(s) ? "0" : s);
    }

    public static String intToStr(int i) {
	return i > 0 ? i + "" : "";
    }

    /**
     * @param timestamp - String in the form of 'hh:mm:ss'
     * @return the equivalent number of seconds
     */
    public static int getDuration(String timestamp) {
	String[] split = timestamp.split(" *: *");
	int hrs = BehaviorLoggerUtil.strToInt(split[0]);
	int min = BehaviorLoggerUtil.strToInt(split[1]);
	int sec = BehaviorLoggerUtil.strToInt(split[2]);

	return (hrs * 60 * 60) + (min * 60) + sec;
    }

    /**
     * Converts the contents of hoursField, minutesField, and secondsField into the
     * equivalent number of milliseconds. If "isInfinite" is true, then returns 0
     */
    public static int getDurationInMillis(boolean isInfinite, TextField hoursField, TextField minutesField,
	    TextField secondsField) {
	if (isInfinite) {
	    return 0;
	}

	Integer hours = strToInt(hoursField.getText());
	Integer mins = strToInt(minutesField.getText());
	Integer secs = strToInt(secondsField.getText());
	return ((hours * 60 * 60) + (mins * 60) + secs) * 1000;
    }

    public static String millisToTimestampNoSpaces(long totalMillis) {
	String withSpaces = millisToTimestamp(totalMillis);
	return withSpaces.replaceAll(" *", "");
    }

    public static String millisToTimestamp(long totalMillis) {
	long totalSeconds = (totalMillis / 1000);
	long remaining = totalSeconds % (60 * 60);
	long hours = totalSeconds / (60 * 60);
	long minutes = remaining / 60;
	long seconds = remaining % 60;

	return String.format("%02d : %02d : %02d", hours, minutes, seconds);
    }

    public static double millisToMinutes(long millis) {
	return ((double) millis) / (1000 * 60.0);
    }

    public static void openManual(String section) {
	if (!Desktop.isDesktopSupported()) {
	    return;
	}
	String filepath = ResourceUtils.getManual().getAbsolutePath();
	if (System.getProperty("os.name").contains("Win")) {
	    filepath = "/" + filepath.replaceAll("\\\\", "/");
	}

	try {
	    URI uri = new URI("file", null, filepath, section);
	    Desktop.getDesktop().browse(uri);
	} catch (Exception e) {
	    e.printStackTrace();
	    Alerts.error("Error Loading Resource", "There was a problem loading a resource: " + filepath, e);
	}
    }

    /**
     * Opens a file selection dialog and populates TextField fileField with the
     * chosen file path.
     * 
     * @param fileField
     * @param filterDescription
     * @param filterExtension
     */
    public static void browseBtnPressed(TextField fileField, String filterDescription, String... filterExtension) {
	File f = getFile(fileField);
	if (!f.exists()) {
	    f = new File(System.getProperty("user.home"));
	} else {
	    if (!f.isDirectory()) {
		f = f.getParentFile(); // if not a directory, get the parent directory
	    }
	}

	FileChooser fileChooser = new FileChooser();
	fileChooser.setInitialDirectory(f);
	ExtensionFilter extFilter = new FileChooser.ExtensionFilter(filterDescription, filterExtension);
	fileChooser.getExtensionFilters().add(extFilter);
	File newFile = fileChooser.showOpenDialog(BehaviorLoggerUtil.dialogStage.get());

	if (newFile != null) {
	    fileField.setText(newFile.getPath());
	}
    }

    private static File getFile(TextField fileField) {
	String text = Strings.nullToEmpty(fileField.getText()).trim();
	return new File(text);
    }

}
