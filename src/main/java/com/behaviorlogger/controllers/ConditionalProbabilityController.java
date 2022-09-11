package com.behaviorlogger.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;

import com.behaviorlogger.models.ConditionalProbabilityManager;
import com.behaviorlogger.models.behaviors.ContinuousBehavior;
import com.behaviorlogger.models.behaviors.DiscreteBehavior;
import com.behaviorlogger.models.schemas.KeyBehaviorMapping;
import com.behaviorlogger.persistence.GsonUtils;
import com.behaviorlogger.persistence.recordings.RecordingRawJson1_1.SessionBean1_1;
import com.behaviorlogger.utils.Alerts;
import com.behaviorlogger.utils.BehaviorLoggerUtil;
import com.behaviorlogger.utils.ConditionalProbability;
import com.behaviorlogger.utils.ConditionalProbability.Results;
import com.behaviorlogger.utils.ConditionalProbability.TooManyBackgroundEventsException;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
    RadioButton calculationBinaryRadio;
    @FXML
    RadioButton calculationProportionalRadio;
    @FXML
    RadioButton operationEstablishingRadio;
    @FXML
    RadioButton operationNonEstablishingRadio;
    @FXML
    RadioButton backgroundRandomSamplingRadio;
    @FXML
    CheckBox debugBackgroundRandomSamplingCheckBox;
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
	initCalculationFields();
	initOperationFields();
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
    public void generateBtnPressed() throws EncryptedDocumentException, IOException {
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
	this.windowField.setText(Integer.toString(ConditionalProbabilityManager.windowProperty().get()));
	BehaviorLoggerUtil.addIntegerListener(this.windowField, 999, i -> {
	    if (i <= 0) {
		i = 1;
	    }
	    ConditionalProbabilityManager.windowProperty().set(i);
	});
    }

    private void initCalculationFields() {
	ToggleGroup group = new ToggleGroup();
	this.calculationBinaryRadio.setToggleGroup(group);
	this.calculationProportionalRadio.setToggleGroup(group);

	String calculationType = ConditionalProbabilityManager.calculationTypeProperty().getValueSafe();
	this.calculationBinaryRadio.setSelected(
		calculationType.isEmpty() || calculationType.equals(ConditionalProbabilityManager.CALCULATION_BINARY));
	this.calculationProportionalRadio
		.setSelected(calculationType.equals(ConditionalProbabilityManager.CALCULATION_PROPORTIONAL));
	this.calculationBinaryRadio.selectedProperty().addListener((o, ov, selected) -> {
	    if (selected) {
		ConditionalProbabilityManager.calculationTypeProperty()
			.setValue(ConditionalProbabilityManager.CALCULATION_BINARY);
	    }
	});
	this.calculationProportionalRadio.selectedProperty().addListener((o, ov, selected) -> {
	    if (selected) {
		ConditionalProbabilityManager.calculationTypeProperty()
			.setValue(ConditionalProbabilityManager.CALCULATION_PROPORTIONAL);
	    }
	});
    }

    private void initOperationFields() {
	ToggleGroup group = new ToggleGroup();
	this.operationEstablishingRadio.setToggleGroup(group);
	this.operationNonEstablishingRadio.setToggleGroup(group);

	String operationType = ConditionalProbabilityManager.operationTypeProperty().getValueSafe();
	this.operationEstablishingRadio.setSelected(
		operationType.isEmpty() || operationType.equals(ConditionalProbabilityManager.OPERATION_ESTABLISHING));
	this.operationNonEstablishingRadio
		.setSelected(operationType.equals(ConditionalProbabilityManager.OPERATION_NON_ESTABLISHING));
	this.operationEstablishingRadio.selectedProperty().addListener((o, ov, selected) -> {
	    if (selected) {
		ConditionalProbabilityManager.operationTypeProperty()
			.setValue(ConditionalProbabilityManager.OPERATION_ESTABLISHING);
	    }
	});
	this.operationNonEstablishingRadio.selectedProperty().addListener((o, ov, selected) -> {
	    if (selected) {
		ConditionalProbabilityManager.operationTypeProperty()
			.setValue(ConditionalProbabilityManager.OPERATION_NON_ESTABLISHING);
	    }
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

	boolean debugBackgroundRandomSamplingSelected = ConditionalProbabilityManager
		.debugBackgroundRandomSamplingSelectedProperty().getValue();
	this.debugBackgroundRandomSamplingCheckBox.setSelected(debugBackgroundRandomSamplingSelected);
	this.debugBackgroundRandomSamplingCheckBox.selectedProperty().addListener((o, old, selected) -> {
	    ConditionalProbabilityManager.debugBackgroundRandomSamplingSelectedProperty().setValue(selected);
	});

	if (ConditionalProbabilityManager.backgroundNumEventsProperty().get() > 0) {
	    this.backgroundRandomSamplingNumEventsField
		    .setText(Integer.toString(ConditionalProbabilityManager.backgroundNumEventsProperty().get()));
	}
	BehaviorLoggerUtil.addIntegerListener(this.backgroundRandomSamplingNumEventsField, i -> {
	    ConditionalProbabilityManager.backgroundNumEventsProperty().set(i);
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

    private void generateResults() throws IOException, EncryptedDocumentException {
	KeyBehaviorMapping targetBehavior = this.selectedBehavior;
	List<DiscreteBehavior> targetEvents = getTargetEvents(targetBehavior);
	int windowMillis = ConditionalProbabilityManager.windowProperty().get() * 1000;
	Map<KeyBehaviorMapping, Results> actualResultsMap = Maps.newHashMap();
	Map<KeyBehaviorMapping, Results> backgroundResultsMap = Maps.newHashMap();
	StringBuilder debugBackgroundRandomSamplingOutput = new StringBuilder();
	debugBackgroundRandomSamplingOutput.append("Randomly selected background events (seconds):\n\n");

	for (KeyBehaviorMapping consequenceBehavior : this.dataStream.schema.behaviors) {
	    if (consequenceBehavior.uuid.equals(targetBehavior.uuid)) {
		continue;
	    }
	    List<ContinuousBehavior> consequenceEvents = ConditionalProbability.getConsequenceEvents(
		    consequenceBehavior, this.dataStream.discreteEvents, this.dataStream.continuousEvents);
	    
	    Results actualResults = calculate(targetEvents, consequenceEvents, windowMillis);
	    actualResultsMap.put(consequenceBehavior, actualResults);

	    // generate background probabilities
	    List<DiscreteBehavior> backgroundEvents;
	    try {
		if (ConditionalProbabilityManager.operationTypeProperty().getValueSafe()
			.equals(ConditionalProbabilityManager.OPERATION_ESTABLISHING)) {
		    backgroundEvents = getBackgroundEvents(actualResults, targetBehavior, consequenceEvents);
		} else {
		    backgroundEvents = getBackgroundEvents(actualResults, targetBehavior, Lists.newArrayList());
		}
	    } catch (TooManyBackgroundEventsException e) {
		Alerts.error("Error Generating Background Events",
			"The data stream is too small to generate the required background events. Consider using the Complete option or reducing the number of desired background events.",
			e);
		e.printStackTrace();
		return;
	    }
	    backgroundEvents.sort(Comparator.comparingLong(e -> e.startTime));
	    Results backgroundResults = calculate(backgroundEvents, consequenceEvents, windowMillis);
	    backgroundResultsMap.put(consequenceBehavior, backgroundResults);

	    List<String> backgroundEventTimes = Lists.newArrayList();
	    for (DiscreteBehavior event : backgroundEvents) {
		backgroundEventTimes.add(Double.toString((double) event.startTime / 1000));
	    }
	    debugBackgroundRandomSamplingOutput.append("-------------------------------------------\n");
	    debugBackgroundRandomSamplingOutput.append(String.format("%c, %s:\n    %s\n", consequenceBehavior.key.c,
		    consequenceBehavior.description, String.join(", ", backgroundEventTimes)));
	}

	writeToFile(actualResultsMap, backgroundResultsMap, debugBackgroundRandomSamplingOutput);
    }

    private Results calculate(List<DiscreteBehavior> targetEvents, List<ContinuousBehavior> consequentEvents,
	    int windowMillis) {
	String operation = ConditionalProbabilityManager.operationTypeProperty().get();
	String calculation = ConditionalProbabilityManager.calculationTypeProperty().get();

	if (operation.equals(ConditionalProbabilityManager.OPERATION_ESTABLISHING)
		&& calculation.equals(ConditionalProbabilityManager.CALCULATION_BINARY)) {
	    return ConditionalProbability.binaryEO(targetEvents, consequentEvents, windowMillis);
	}
	if (operation.equals(ConditionalProbabilityManager.OPERATION_NON_ESTABLISHING)
		&& calculation.equals(ConditionalProbabilityManager.CALCULATION_BINARY)) {
	    return ConditionalProbability.binaryNonEO(targetEvents, consequentEvents, windowMillis);
	}
	if (operation.equals(ConditionalProbabilityManager.OPERATION_ESTABLISHING)
		&& calculation.equals(ConditionalProbabilityManager.CALCULATION_PROPORTIONAL)) {
	    return ConditionalProbability.proportionEO(targetEvents, consequentEvents, windowMillis);
	}
	return ConditionalProbability.proportionNonEO(targetEvents, consequentEvents, windowMillis);
    }

    private List<DiscreteBehavior> getTargetEvents(KeyBehaviorMapping targetBehavior) {
	return ConditionalProbability.getTargetEvents(targetBehavior, this.dataStream.discreteEvents,
		this.dataStream.continuousEvents);
    }

    private List<DiscreteBehavior> getBackgroundEvents(Results actualResults, KeyBehaviorMapping targetBehavior,
	    List<ContinuousBehavior> consequenceEvents) throws TooManyBackgroundEventsException {
	if (ConditionalProbabilityManager.backgroundRandomSamplingSelectedProperty().get()) {
	    long numEvents = actualResults.sampled; // TODO get correct number of events
	    if (ConditionalProbabilityManager.backgroundNumEventsProperty().get() > 0) {
		numEvents = ConditionalProbabilityManager.backgroundNumEventsProperty().get();
	    }
	    return ConditionalProbability.randomBackgroundEvents(targetBehavior, consequenceEvents,
		    this.dataStream.duration, numEvents);
	}
	return ConditionalProbability.completeBackgroundEvents(targetBehavior, consequenceEvents,
		this.dataStream.duration);
    }

    private void writeToFile(Map<KeyBehaviorMapping, Results> resultsMap,
	    Map<KeyBehaviorMapping, Results> backgroundResultsMap, StringBuilder debugBackgroundRandomSamplingOutput)
	    throws IOException, EncryptedDocumentException {
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
	int r = 0;

	r = writeExcelSummary(outputFile, wb, s, r);
	r = writeExcelHeaders(wb, s, r);
	writeExcelDetails(resultsMap, backgroundResultsMap, wb, s, r);

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

	if (ConditionalProbabilityManager.debugBackgroundRandomSamplingSelectedProperty().get()) {
	    String absoluteFilename = outputFile.getAbsolutePath() + ".background-samples.txt";
	    File debugFile = new File(absoluteFilename);
	    Files.write(debugBackgroundRandomSamplingOutput.toString().getBytes(), debugFile);
	}
    }

    private int writeExcelSummary(File outputFile, Workbook wb, Sheet s, int r) {
	Row row;
	Cell cell;

	CellStyle boldstyle = wb.createCellStyle();
	Font font = wb.createFont();
	font.setBold(true);
	boldstyle.setFont(font);

	row = s.createRow(r++);
	cell = row.createCell(0);
	cell.setCellValue("File");
	cell.setCellStyle(boldstyle);
	row.createCell(1).setCellValue(ConditionalProbabilityManager.fileProperty().get());

	row = s.createRow(r++);
	cell = row.createCell(0);
	cell.setCellValue("Target");
	cell.setCellStyle(boldstyle);
	String targetDescription = String.format("%s (%s)", this.selectedBehavior.description,
		this.selectedBehavior.key.c);
	row.createCell(1).setCellValue(targetDescription);

	row = s.createRow(r++);
	cell = row.createCell(0);
	cell.setCellValue("Window Size (seconds)");
	cell.setCellStyle(boldstyle);
	row.createCell(1).setCellValue(ConditionalProbabilityManager.windowProperty().get());

	row = s.createRow(r++);
	cell = row.createCell(0);
	cell.setCellValue("Calculation Type");
	cell.setCellStyle(boldstyle);
	row.createCell(1).setCellValue(ConditionalProbabilityManager.calculationTypeProperty().get());

	row = s.createRow(r++);
	cell = row.createCell(0);
	cell.setCellValue("Operation Type");
	cell.setCellStyle(boldstyle);
	row.createCell(1).setCellValue(ConditionalProbabilityManager.operationTypeProperty().get());

	return r;
    }

    private int writeExcelHeaders(Workbook wb, Sheet s, int r) {
	Row row;

	CellStyle boldstyle = wb.createCellStyle();
	Font font = wb.createFont();
	font.setBold(true);
	boldstyle.setFont(font);
	boldstyle.setAlignment(HorizontalAlignment.CENTER);

	r++; // skip row

	row = s.createRow(r++); // behavior/sample/condition/background
	row.createCell(0).setCellValue("Behavior");
	row.createCell(1).setCellValue("Sample");
	row.createCell(2).setCellValue("Condition");
	row.createCell(3).setCellValue("Background");
	row.cellIterator().forEachRemaining((c) -> c.setCellStyle(boldstyle));

	return r;
    }

    private void writeExcelDetails(Map<KeyBehaviorMapping, Results> resultsMap,
	    Map<KeyBehaviorMapping, Results> backgroundResultsMap, Workbook wb, Sheet s, int r) {
	CellStyle boldstyle = wb.createCellStyle();
	Font font = wb.createFont();
	font.setBold(true);
	boldstyle.setFont(font);

	CellStyle decimalstyle = wb.createCellStyle();
	DataFormat dataformat = wb.createDataFormat();
	decimalstyle.setDataFormat(dataformat.getFormat("0.000"));

	Set<Entry<KeyBehaviorMapping, Results>> entrySet = resultsMap.entrySet();
	ArrayList<Entry<KeyBehaviorMapping, Results>> entries = Lists.newArrayList(entrySet);
	entries.sort((e1, e2) -> -1 * ConditionalProbability.Results.compare.compare(e1.getValue(), e2.getValue()));
	for (Entry<KeyBehaviorMapping, Results> entry : entries) {
	    KeyBehaviorMapping key = entry.getKey();
	    Results actualResults = entry.getValue();
	    Results backgroundResults = backgroundResultsMap.get(key);

	    Row row = s.createRow(r++);
	    Cell cell;
	    int c = 0;
	    cell = row.createCell(c++);
	    cell.setCellValue(String.format("%s (%s)", key.description, key.key.c));
	    cell.setCellStyle(boldstyle);

	    row.createCell(c++).setCellValue(actualResults.sampled);
	    row.createCell(c++).setCellValue(actualResults.probability);
	    row.createCell(c++).setCellValue(backgroundResults.probability);
	    row.getCell(c - 2).setCellStyle(decimalstyle);
	    row.getCell(c - 1).setCellStyle(decimalstyle);
	}
    }
}
