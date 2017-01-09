package com.threebird.recorder.controllers;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.threebird.recorder.BehaviorLoggerApp;
import com.threebird.recorder.models.NewVersionManager;
import com.threebird.recorder.models.preferences.FilenameComponent;
import com.threebird.recorder.models.preferences.PreferencesManager;
import com.threebird.recorder.models.schemas.SchemaVersion;
import com.threebird.recorder.models.schemas.SchemasManager;
import com.threebird.recorder.models.sessions.RecordingManager;
import com.threebird.recorder.models.sessions.SessionManager;
import com.threebird.recorder.persistence.GsonUtils;
import com.threebird.recorder.utils.Alerts;
import com.threebird.recorder.utils.BehaviorLoggerUtil;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 * Controls the first view the user sees. All these member variables with the @FXML annotation are physical objects I
 * placed in Scene Builder and applied an 'id'. The 'id' must match the variable name. Methods with an @FXML annotation
 * are triggered by events (again, specified in scene builder)
 */
public class StartMenuController
{
  public static final String TITLE = "Start Menu";
  private static String homeURL = "http://3birdsoftware.com";

  @FXML private TableView< SchemaVersion > schemaTable;
  @FXML private TableColumn< SchemaVersion, String > clientCol;
  @FXML private TableColumn< SchemaVersion, String > projectCol;
  @FXML private Button createSchemaButton;
  @FXML private Button editSchemaBtn;
  @FXML private Button exportSchemaButton;

  @FXML private VBox rightSide;
  @FXML private VBox mappingsBox;
  @FXML private Label timeBox;

  @FXML private TextField observerField;
  @FXML private TextField therapistField;
  @FXML private TextField conditionField;
  @FXML private TextField locationField;
  @FXML private TextField sessionField;

  @FXML private Label filenameLbl;
  @FXML private ImageView warningImg;

  @FXML private VBox errMsgBox;

  @FXML private Button saveButton;
  @FXML private Button startButton;
  @FXML private Button helpButton;

  @FXML private Hyperlink hyperlink;

  /**
   * load up the FXML file we generated with Scene Builder, "schemas.fxml". This view is controlled by
   * SchemasController.java
   */
  public static void toStartMenuView()
  {
    String filepath = "views/start-menu.fxml";
    StartMenuController controller = BehaviorLoggerUtil.loadScene( filepath, TITLE );
    controller.init();
  }

  private void init()
  {
    setVisibility( false );
    initSchemaListView();
    initSessionDetails();

    if (!NewVersionManager.checked.get() && PreferencesManager.checkVersionProperty().get()) {
      checkVersion();
    } else {
      hyperlink.onActionProperty().set( e -> {
        setHyperlink( "3birdsoftware.com", homeURL );
      } );
    }
  }

  private void setHyperlink( String text, String url )
  {
    hyperlink.setText( text );
    try {
      URI uri = new URI( url );
      Desktop.getDesktop().browse( uri );
    } catch (Exception e1) {
      e1.printStackTrace();
    }
  }

  private void setVisibility( boolean visible )
  {
    rightSide.setVisible( visible );
    editSchemaBtn.setVisible( visible );
    exportSchemaButton.setVisible( visible );
  }

  /**
   * Initializes 'schemaList' and binds it to 'schemas'
   */
  private void initSchemaListView()
  {
    clientCol.setCellValueFactory( p -> new SimpleStringProperty( p.getValue().client ) );
    projectCol.setCellValueFactory( p -> new SimpleStringProperty( p.getValue().project ) );

    schemaTable.setItems( SchemasManager.schemas().filtered( s -> !s.archived ) );
    schemaTable.getSelectionModel()
               .selectedItemProperty()
               .addListener( this::onSchemaSelect );
    schemaTable.getSelectionModel().select( SchemasManager.getSelected() );
  }

  /**
   * Populates the 'mappingBox' with the currently selected Schema's key-behavior mappings
   */
  private void populateMappingsTable( SchemaVersion schema )
  {
    mappingsBox.getChildren().clear();

    schema.behaviors.values().forEach( mapping -> {
      Label contLbl = new Label();
      contLbl.setText( mapping.isContinuous ? "(cont.)" : "" );
      contLbl.setMinWidth( 45 );

      Label keyLbl = new Label( mapping.key.c + "" );
      keyLbl.setMinWidth( 15 );

      Label separator = new Label( ":" );

      Label behaviorLbl = new Label( mapping.description );
      behaviorLbl.setWrapText( true );

      HBox hbox = new HBox( contLbl, keyLbl, separator, behaviorLbl );
      hbox.setSpacing( 5 );
      mappingsBox.getChildren().add( hbox );
    } );
  }

  private void initSessionDetails()
  {
    observerField.setText( SessionManager.getObserver() );
    therapistField.setText( SessionManager.getTherapist() );
    conditionField.setText( SessionManager.getCondition() );
    locationField.setText( SessionManager.getLocation() );
    sessionField.setText( SessionManager.getSessionNumber().toString() );

    observerField.textProperty().addListener( ( o, old, newV ) -> SessionManager.setObserver( newV ) );
    therapistField.textProperty().addListener( ( o, old, newV ) -> SessionManager.setTherapist( newV ) );
    conditionField.textProperty().addListener( ( o, old, newV ) -> SessionManager.setCondition( newV ) );
    locationField.textProperty().addListener( ( o, old, newV ) -> SessionManager.setLocation( newV ) );

    EventHandler< ? super KeyEvent > limitLenth100 = BehaviorLoggerUtil.createFieldLimiter( 100 );
    observerField.setOnKeyTyped( limitLenth100 );
    therapistField.setOnKeyTyped( limitLenth100 );
    conditionField.setOnKeyTyped( limitLenth100 );
    locationField.setOnKeyTyped( limitLenth100 );

    // Put in some idiot-proof logic for the session # (limit to just digits,
    // prevent exceeding max_value)
    EventHandler< ? super KeyEvent > limitSessionField =
        BehaviorLoggerUtil.createFieldLimiter( "0123456789".toCharArray(), 9 );
    sessionField.setOnKeyTyped( limitSessionField );
    sessionField.textProperty().addListener( ( o, old, newV ) -> {
      String text = sessionField.getText().trim();
      if (Strings.isNullOrEmpty( text )) {
        SessionManager.setSessionNumber( 0 );
      } else {
        SessionManager.setSessionNumber( Integer.valueOf( text.trim() ) );
      }
    } );

    SessionManager.observerProperty().addListener( ( o, old, newV ) -> updateFilenameLabel() );
    SessionManager.therapistProperty().addListener( ( o, old, newV ) -> updateFilenameLabel() );
    SessionManager.conditionProperty().addListener( ( o, old, newV ) -> updateFilenameLabel() );
    SessionManager.locationProperty().addListener( ( o, old, newV ) -> updateFilenameLabel() );
    SessionManager.sessionNumberProperty().addListener( ( o, old, newV ) -> updateFilenameLabel() );
    PreferencesManager.filenameComponents().addListener( (ListChangeListener< FilenameComponent >) c -> {
      updateFilenameLabel();
    } );
  }

  private void updateFilenameLabel()
  {
    if (RecordingManager.getFileName() == null) {
      filenameLbl.setText( "" );
      return;
    }

    String text = String.format( "%s (.raw/.xls)", RecordingManager.getFileName() );
    filenameLbl.setText( text );

    boolean isConflicting = dataFilenameHasConflict();
    warningImg.setVisible( isConflicting );
    filenameLbl.setTextFill( isConflicting ? Color.ORANGE : Color.BLACK );
  }

  private boolean dataFilenameHasConflict()
  {
    String fullFileName = RecordingManager.getFullFileName();
    File fCsv = new File( fullFileName + ".raw" );
    File fXls = new File( fullFileName + ".xls" );

    boolean isConflicting = fCsv.exists() || fXls.exists();
    return isConflicting;
  }

  /**
   * On schema-select: if the new value is null, hide right-hand-side. Otherwise, populate right-hand-side with new
   * schema's data
   */
  private void onSchemaSelect( ObservableValue< ? extends SchemaVersion > ov,
                               SchemaVersion oldV,
                               SchemaVersion newV )
  {
    SchemasManager.setSelected( newV );
    if (newV != null) {
      setVisibility( true );
      populateMappingsTable( newV );
      timeBox.setText( BehaviorLoggerUtil.millisToTimestamp( newV.duration ) );
      updateFilenameLabel();
    } else {
      setVisibility( false );
    }
  }

  @FXML private void onCreateSchemaClicked( ActionEvent evt )
  {
    EditSchemaController.toEditSchemaView( null );
  }

  @FXML private void onEditSchemaClicked( ActionEvent evt )
  {
    EditSchemaController.toEditSchemaView( SchemasManager.getSelected() );
  }

  @FXML private void onImportSchemaPressed()
  {
    FileChooser fileChooser = new FileChooser();
    ExtensionFilter extFilter = new FileChooser.ExtensionFilter( "Schema files (*.schema)", "*.schema" );
    fileChooser.getExtensionFilters().add( extFilter );
    File newFile = fileChooser.showOpenDialog( BehaviorLoggerUtil.dialogStage.get() );

    if (newFile == null) {
      return;
    }

    // try {
    // List< SchemaVersion > versionset = GsonUtils.< List< SchemaVersion > > get( newFile, Lists.newArrayList() );
    // } catch (IOException e) {
    // Alerts.error( "Error Importing Schema", "There was a problem while importing the Schema.", e );
    // e.printStackTrace();
    // return;
    // }

    //
    // try {
    // Optional< SchemaVersion > oldOpt = Schemas.getForUuid( schema.uuid );
    //
    // if (oldOpt.isPresent()) {
    // SchemaVersion old = oldOpt.get();
    // if (old.version < schema.version) {
    // Schemas.update( schema );
    // SchemasManager.schemas().remove( old );
    // SchemasManager.schemas().add( schema );
    // }
    // } else {
    // SchemasManager.create( schema );
    // }
    //
    // schemaTable.refresh();
    // if (!schema.archived) {
    // schemaTable.getSelectionModel().select( schema );
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // Alerts.error( "Error saving Schema", "There was a problem while saving your schema.", e );
    // }
  }

  @FXML private void onExportBtnPressed()
  {
    SchemaVersion selected = SchemasManager.getSelected();
    FileChooser fileChooser = new FileChooser();
    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter( "Schema files (*.schema)", "*.schema" );
    fileChooser.getExtensionFilters().add( extFilter );

    if (Strings.isNullOrEmpty( selected.client )) {
      fileChooser.setInitialFileName( selected.project );
    } else if (Strings.isNullOrEmpty( selected.project )) {
      fileChooser.setInitialFileName( selected.client );
    } else {
      fileChooser.setInitialFileName( selected.client + "-" + selected.project );
    }

    File result = fileChooser.showSaveDialog( BehaviorLoggerApp.STAGE );

    if (result != null) {
      try {
        GsonUtils.save( result, selected );
      } catch (Exception e) {
        Alerts.error( "Failed to Export", "There was a problem while exporting the selected schema.", e );
        e.printStackTrace();
      }
    }
  }

  private boolean validate()
  {
    String cssRed = "-fx-background-color:#FFDDDD;-fx-border-color: #f00;";
    boolean valid = true;

    ImmutableMap< String, FilenameComponent > displayToComp =
        Maps.uniqueIndex( PreferencesManager.filenameComponents(), c -> c.name );

    Map< FilenameComponent, TextField > compToField = Maps.newHashMap();
    compToField.put( displayToComp.get( "Observer" ), observerField );
    compToField.put( displayToComp.get( "Therapist" ), therapistField );
    compToField.put( displayToComp.get( "Condition" ), conditionField );
    compToField.put( displayToComp.get( "Location" ), locationField );
    compToField.put( displayToComp.get( "Session Number" ), sessionField );

    for (Entry< FilenameComponent, TextField > entry : compToField.entrySet()) {
      FilenameComponent comp = entry.getKey();
      TextField field = entry.getValue();
      if (comp.enabled && Strings.isNullOrEmpty( field.getText() )) {
        field.setStyle( cssRed );
        Label lbl = new Label( "- " + comp.name + " is required for your data file's name." );
        lbl.setTextFill( Color.RED );
        errMsgBox.getChildren().add( lbl );
        valid = false;
      } else {
        field.setStyle( "" );
      }
    }

    if (!valid) {
      Label lbl = new Label( "You can edit the file name in the Preferences menu." );
      lbl.setTextFill( Color.RED );
      errMsgBox.getChildren().add( lbl );
    }

    return valid;
  }

  @FXML private void onPrefsClicked( ActionEvent evt )
  {
    PreferencesController.showPreferences();
  }

  @FXML private void onStartClicked( ActionEvent evt )
  {
    if (!validate()) {
      return;
    }

    boolean isConflicting = dataFilenameHasConflict();
    if (isConflicting) {
      String msg =
          "Starting this session will overwrite an existing data file.\n"
              + "Click Cancel to stay here or Ok to proceed.";

      Alerts.confirm( "File already exists.", null, msg, RecordingController::toRecordingView );
    } else {
      RecordingController.toRecordingView();
    }
  }

  @FXML private void onIoaBtnPressed()
  {
    IoaCalculatorController.showIoaCalculator();
  }

  @FXML private void onHelpBtnPressed()
  {
    BehaviorLoggerUtil.openManual( "start-menu" );
  }

  private void checkVersion()
  {
    NewVersionManager.checked.set( true );
    String version = BehaviorLoggerApp.version;

    new Thread( () -> {
      try {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
          HttpGet httpget = new HttpGet( "http://3birdsoftware.com/bl-version.txt" );

          ResponseHandler< String > responseHandler = response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status == 200) {
              HttpEntity entity = response.getEntity();
              return entity != null ? EntityUtils.toString( entity ) : null;
            }

            return null;
          };

          String v = httpclient.execute( httpget, responseHandler ).trim();

          boolean newVersionAvailable = !version.equals( v );

          Platform.runLater( () -> {
            String text = newVersionAvailable ? "New Version Available!" : "3birdsoftware.com";
            String url = newVersionAvailable ? NewVersionController.URL : homeURL;
            hyperlink.setText( text );
            hyperlink.onActionProperty().set( ( event ) -> {
              setHyperlink( text, url );
            } );

            // We only want to interrupt the user if they are on the start-menu and
            boolean onStartMenu = BehaviorLoggerApp.STAGE.getTitle().equals( StartMenuController.TITLE );
            boolean displayDialog = !Strings.isNullOrEmpty( v )
                && !version.equals( v )
                && !PreferencesManager.lastVersionCheckProperty().get().equals( v )
                    & onStartMenu;

            if (displayDialog) {
              NewVersionController.show( v );
            }
          } );
        } finally {
          httpclient.close();
        }
      } catch (Exception e) {
        // Eat the Exception because this function isn't essential
        e.printStackTrace();
      }
    } ).start();
  }
}
