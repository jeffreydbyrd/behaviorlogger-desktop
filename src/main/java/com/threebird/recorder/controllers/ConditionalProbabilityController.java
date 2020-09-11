package com.threebird.recorder.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.threebird.recorder.models.ConditionalProbabilityManager;
import com.threebird.recorder.models.behaviors.BehaviorEvent;
import com.threebird.recorder.models.behaviors.DiscreteBehavior;
import com.threebird.recorder.models.schemas.KeyBehaviorMapping;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.persistence.recordings.RecordingRawJson1_1.SessionBean1_1;
import com.threebird.recorder.utils.BehaviorLoggerUtil;
import com.threebird.recorder.utils.ConditionalProbability;
import com.threebird.recorder.utils.ConditionalProbability.AllResults;
import com.threebird.recorder.utils.ConditionalProbability.TooManyBackgroundEventsException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class ConditionalProbabilityController {
    @FXML
    private TextField fileField;
    @FXML
    private Button browseBtn;
    @FXML
    private Label fileNotFoundLbl;
    @FXML
    private RadioButton newFileRadio;
    @FXML
    private RadioButton appendRadio;
    @FXML
    private VBox appendBox;
    @FXML
    private TextField appendField;
    @FXML
    private Button appendBrowseBtn;
    @FXML
    private Label appendFileNotFoundLbl;
    @FXML
    VBox behaviorSelection;
    @FXML
    Label unselectedTargetLbl;
    @FXML
    Label saveStatusLbl;
    @FXML
    TextField windowField;
    @FXML
    Label windowRequiredLbl;
    @FXML
    RadioButton backgroundRandomSamplingRadio;
    @FXML
    RadioButton backgroundCompleteSamplingRadio;
    @FXML
    TextField backgroundRandomSamplingNumEventsField;

    private KeyBehaviorMapping selectedBehavior;
    private SessionBean1_1 dataStream;

    public static void showCalculator() {
	String fxmlPath = "views/prob-calculator.fxml";
	BehaviorLoggerUtil.showScene(fxmlPath, "Conditional Probability Calculator");
    }

    @FXML
    private void initialize() {
	initFileField();
	initWindowField();
	initBackgroundFields();
	initSaveOptions();
    }

    @FXML
    private void browseBtnPressed() {
	BehaviorLoggerUtil.browseBtnPressed(this.fileField, "Raw data files (*.raw)", "*.raw");
    }

    @FXML
    public void onCloseBtnPressed() {
	BehaviorLoggerUtil.dialogStage.get().close();
    }

    @FXML
    public void generateBtnPressed() throws EncryptedDocumentException, InvalidFormatException, IOException {
	if (!validate()) {
	    return;
	}
	generateResults();
    }

    @FXML
    public void onHelpBtnPressed() {
	// BehaviorLoggerUtil.openManual( "conditional-probability" );
    }

    @FXML
    public void appendBtnPressed() {
	BehaviorLoggerUtil.browseBtnPressed(this.appendField, "Excel files (*.xls, *.xlsx)", "*.xls", "*.xlsx");
    }

    private void initFileField() {
	this.fileField.textProperty().addListener((o, old, newV) -> cacheSession());
	this.fileField.setText(ConditionalProbabilityManager.fileProperty().get());
	this.fileField.textProperty()
		.addListener((o, old, newV) -> ConditionalProbabilityManager.fileProperty().set(newV));
    }

    private void initSaveOptions() {
	ToggleGroup group = new ToggleGroup();
	newFileRadio.setToggleGroup(group);
	appendRadio.setToggleGroup(group);
	appendRadio.selectedProperty().addListener((observable, oldValue, selected) -> {
	    ConditionalProbabilityManager.appendSelectedProperty().setValue(selected);
	    appendBox.setDisable(!selected);
	});

	boolean appendFileSelected = ConditionalProbabilityManager.appendSelectedProperty().getValue();
	appendBox.setDisable(!appendFileSelected);
	newFileRadio.setSelected(!appendFileSelected);
	appendRadio.setSelected(appendFileSelected);

	ConditionalProbabilityManager.getAppendFile().ifPresent(appendField::setText);
	appendField.textProperty()
		.addListener((o, old, newV) -> ConditionalProbabilityManager.appendFileProperty().set(newV));
    }

    private void initWindowField() {
	char[] digits = "0123456789".toCharArray();
	this.windowField.setOnKeyTyped(BehaviorLoggerUtil.createFieldLimiter(digits, 3));
	this.windowField.setText(Integer.toString(ConditionalProbabilityManager.windowProperty().get()));
	this.windowField.textProperty().addListener((o, old, newV) -> {
	    int n = Strings.isNullOrEmpty(newV) ? 1 : Integer.valueOf(newV);
	    ConditionalProbabilityManager.windowProperty().set(n);
	});
    }

    private void initBackgroundFields() {
	ToggleGroup group = new ToggleGroup();
	this.backgroundRandomSamplingRadio.setToggleGroup(group);
	this.backgroundCompleteSamplingRadio.setToggleGroup(group);

	boolean backgroundRandomSamplingSelected = ConditionalProbabilityManager
		.backgroundRandomSamplingSelectedProperty().getValue();
	this.backgroundCompleteSamplingRadio.setSelected(!backgroundRandomSamplingSelected);
	this.backgroundRandomSamplingRadio.setSelected(backgroundRandomSamplingSelected);
	backgroundRandomSamplingRadio.selectedProperty().addListener((observable, oldValue, selected) -> {
	    ConditionalProbabilityManager.backgroundRandomSamplingSelectedProperty().setValue(selected);
	});

	char[] digits = "0123456789".toCharArray();
	this.backgroundRandomSamplingNumEventsField.setOnKeyTyped(BehaviorLoggerUtil.createFieldLimiter(digits, -1));
	this.backgroundRandomSamplingNumEventsField
		.setText(ConditionalProbabilityManager.backgroundNumEventsProperty().get());
	this.backgroundRandomSamplingNumEventsField.textProperty().addListener((o, old, newV) -> {
	    ConditionalProbabilityManager.backgroundNumEventsProperty().set(newV);
	});
    }

    private void cacheSession() {
	this.behaviorSelection.getChildren().clear();
	this.dataStream = null;
	this.selectedBehavior = null;

	File rawFile = getFile(this.fileField);
	if (!rawFile.exists()) {
	    this.fileNotFoundLbl.setVisible(true);
	    return;
	}
	this.fileNotFoundLbl.setVisible(false);
	try {
	    this.dataStream = GsonUtils.get(rawFile, new SessionBean1_1());
	} catch (IOException e) {
	    e.printStackTrace();
	    this.fileNotFoundLbl.setVisible(true);
	    return;
	}
	ToggleGroup group = new ToggleGroup();
	for (KeyBehaviorMapping kbm : this.dataStream.schema.behaviors) {
	    if (kbm.isContinuous) {
		continue;
	    }
	    RadioButton radio = new RadioButton(kbm.key.c + " - " + kbm.description);
	    radio.setToggleGroup(group);
	    this.behaviorSelection.getChildren().add(radio);
	    radio.selectedProperty().addListener((observable, oldValue, selected) -> {
		if (selected) {
		    this.selectedBehavior = kbm;
		}
	    });
	}
    }

    private File getFile(TextField fileField) {
	String text = Strings.nullToEmpty(fileField.getText()).trim();
	return new File(text);
    }

    private boolean validate() {
	String cssRed = "-fx-background-color:#FFDDDD;-fx-border-color: #f00;";
	boolean valid = true;

	File rawFile = getFile(this.fileField);
	if (!rawFile.exists()) {
	    this.fileField.setStyle(cssRed);
	    this.fileNotFoundLbl.setVisible(true);
	    valid = false;
	} else {
	    this.fileField.setStyle("");
	    this.fileNotFoundLbl.setVisible(false);
	}

	File appendFile = getFile(this.appendField);
	if (appendRadio.isSelected() && !appendFile.exists()) {
	    appendField.setStyle(cssRed);
	    appendFileNotFoundLbl.setVisible(true);
	    valid = false;
	} else {
	    appendField.setStyle("");
	    appendFileNotFoundLbl.setVisible(false);
	}

	if (this.windowField.getText().isEmpty()) {
	    this.windowField.setStyle(cssRed);
	    this.windowRequiredLbl.setVisible(true);
	    valid = false;
	} else {
	    this.windowField.setStyle("");
	    this.windowRequiredLbl.setVisible(false);
	}

	if (this.selectedBehavior == null) {
	    this.unselectedTargetLbl.setVisible(true);
	    valid = false;
	} else {
	    this.unselectedTargetLbl.setVisible(false);
	}

	return valid;
    }

    private void generateResults() throws IOException, EncryptedDocumentException, InvalidFormatException {
	KeyBehaviorMapping targetBehavior = this.selectedBehavior;
	List<BehaviorEvent> targetEvents = this.dataStream.discreteEvents.stream() //
		.filter((e) -> e.behaviorUuid.equals(targetBehavior.uuid)) //
		.map((e) -> {
		    return new DiscreteBehavior(targetBehavior.uuid, targetBehavior.key, targetBehavior.description,
			    e.time);
		}).collect(Collectors.toList());

	Map<KeyBehaviorMapping, AllResults> actualResultsMap = Maps.newHashMap();
	Map<KeyBehaviorMapping, AllResults> backgroundResultsMap = Maps.newHashMap();

	for (KeyBehaviorMapping kbm : this.dataStream.schema.behaviors) {
	    if (kbm.uuid.equals(targetBehavior.uuid)) {
		continue;
	    }
	    List<BehaviorEvent> consequenceEvents = ConditionalProbability.getConsequenceEvents(kbm,
		    this.dataStream.discreteEvents, this.dataStream.continuousEvents);
	    int windowMillis = ConditionalProbabilityManager.windowProperty().get() * 1000;
	    AllResults actualResults = ConditionalProbability.all(targetEvents, consequenceEvents, windowMillis);
	    actualResultsMap.put(kbm, actualResults);

	    List<BehaviorEvent> backgroundEvents;
	    if (ConditionalProbabilityManager.backgroundRandomSamplingSelectedProperty().get()) {
		int numEvents = actualResults.binaryEO.sampled;
		try {
		    backgroundEvents = ConditionalProbability.randomBackgroundEvents(targetBehavior, consequenceEvents,
			    (int) this.dataStream.duration, numEvents);
		} catch (TooManyBackgroundEventsException e1) {
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		    backgroundEvents = Lists.newArrayList();
		}
	    } else {
		backgroundEvents = ConditionalProbability.completeBackgroundEvents(targetBehavior, consequenceEvents,
			(int) this.dataStream.duration);
	    }

	    AllResults backgroundResults = ConditionalProbability.all(backgroundEvents, consequenceEvents,
		    windowMillis);
	    backgroundResultsMap.put(kbm, backgroundResults);
	}

	writeToFile(actualResultsMap, backgroundResultsMap);
    }

    private void writeToFile(Map<KeyBehaviorMapping, AllResults> resultsMap,
	    Map<KeyBehaviorMapping, AllResults> backgroundResultsMap)
	    throws IOException, EncryptedDocumentException, InvalidFormatException {
	File outputFile;
	boolean appendToFile = ConditionalProbabilityManager.appendSelectedProperty().get();
	if (appendToFile) {
	    outputFile = getFile(this.appendField);
	} else {
	    FileChooser fileChooser = new FileChooser();
	    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XLS files (*.xls)", "*.xls");
	    fileChooser.getExtensionFilters().add(extFilter);
	    outputFile = fileChooser.showSaveDialog(BehaviorLoggerUtil.dialogStage.get());
	}
	if (outputFile == null) {
	    return;
	}
	if (!outputFile.exists()) {
	    outputFile.createNewFile();
	}

	Workbook wb;
	if (appendToFile) {
	    File tmp = File.createTempFile(outputFile.getName(), "");
	    Files.copy(outputFile, tmp);
	    wb = WorkbookFactory.create(tmp);
	} else {
	    wb = new HSSFWorkbook();
	}

	Sheet s = wb.createSheet();
	Row row;
	int r = 0;

	// __Summary__
	row = s.createRow(r++);
	row.createCell(0).setCellValue("File");
	row.createCell(1).setCellValue(outputFile.getName());
	row = s.createRow(r++);
	row.createCell(0).setCellValue("Target");
	String targetDescription = String.format("%s (%s)", this.selectedBehavior.description,
		this.selectedBehavior.key.c);
	row.createCell(1).setCellValue(targetDescription);
	row = s.createRow(r++);
	row.createCell(0).setCellValue("Window Size (seconds)");
	row.createCell(1).setCellValue(ConditionalProbabilityManager.windowProperty().get());

	// __Headers__
	r++; // skip row
	row = s.createRow(r++); // main headers
	row.createCell(1).setCellValue("Binary");
	row.createCell(7).setCellValue("Proportion");
	row = s.createRow(r++); // eo/non-eo headers
	row.createCell(1).setCellValue("EO");
	row.createCell(4).setCellValue("Non-EO");
	row.createCell(7).setCellValue("EO");
	row.createCell(10).setCellValue("Non-EO");
	row = s.createRow(r++); // behavior/sample/condition/background
	row.createCell(0).setCellValue("Behavior");
	row.createCell(1).setCellValue("Sample");
	row.createCell(2).setCellValue("Condition");
	row.createCell(3).setCellValue("Background");
	row.createCell(4).setCellValue("Sample");
	row.createCell(5).setCellValue("Condition");
	row.createCell(6).setCellValue("Background");
	row.createCell(7).setCellValue("Sample");
	row.createCell(8).setCellValue("Condition");
	row.createCell(9).setCellValue("Background");
	row.createCell(10).setCellValue("Sample");
	row.createCell(11).setCellValue("Condition");
	row.createCell(12).setCellValue("Background");

	// __Details__
	Set<Entry<KeyBehaviorMapping, AllResults>> entrySet = resultsMap.entrySet();
	ArrayList<Entry<KeyBehaviorMapping, AllResults>> entries = Lists.newArrayList(entrySet);
	entries.sort((e1, e2) -> -1 * ConditionalProbability.AllResults.compare.compare(e1.getValue(), e2.getValue()));
	for (Entry<KeyBehaviorMapping, AllResults> entry : entries) {
	    KeyBehaviorMapping key = entry.getKey();
	    AllResults actualResults = entry.getValue();
	    AllResults backgroundResults = backgroundResultsMap.get(key);

	    row = s.createRow(r++);
	    int c = 0;
	    row.createCell(c++).setCellValue(String.format("%s (%s)", key.description, key.key.c));
	    row.createCell(c++).setCellValue(actualResults.binaryEO.sampled);
	    row.createCell(c++).setCellValue(actualResults.binaryEO.probability);
	    row.createCell(c++).setCellValue(backgroundResults.binaryEO.probability);
	    row.createCell(c++).setCellValue(actualResults.binaryNonEO.sampled);
	    row.createCell(c++).setCellValue(actualResults.binaryNonEO.probability);
	    row.createCell(c++).setCellValue(backgroundResults.binaryNonEO.probability);
	    row.createCell(c++).setCellValue(actualResults.proportionEO.sampled);
	    row.createCell(c++).setCellValue(actualResults.proportionEO.probability);
	    row.createCell(c++).setCellValue(backgroundResults.proportionEO.probability);
	    row.createCell(c++).setCellValue(actualResults.proportionNonEO.sampled);
	    row.createCell(c++).setCellValue(actualResults.proportionNonEO.probability);
	    row.createCell(c++).setCellValue(backgroundResults.proportionNonEO.probability);
	}

	FileOutputStream out = new FileOutputStream(outputFile);
	wb.write(out);
	out.flush();
	wb.close();
	out.close();

	if (appendToFile) {
	    this.saveStatusLbl.setText("Conditional Probabilities appended to: " + outputFile.getAbsolutePath());
	} else {
	    this.saveStatusLbl.setText("Conditional Probabilities saved to new file: " + outputFile.getAbsolutePath());
	}
    }
}
