module behaviorlogger {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires com.google.common;
    requires java.desktop;
    requires junit;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires com.google.gson;
    requires org.joda.time;
    requires poi;
    requires sqlite.jdbc;

    opens com.behaviorlogger;
    opens com.behaviorlogger.controllers;
    opens com.behaviorlogger.models;
    opens com.behaviorlogger.models.preferences;
    opens com.behaviorlogger.models.behaviors;
    opens com.behaviorlogger.models.ioa;
    opens com.behaviorlogger.models.schemas;
    opens com.behaviorlogger.models.sessions;
    opens com.behaviorlogger.persistence;
    opens com.behaviorlogger.persistence.recordings;

    exports com.behaviorlogger;
    exports com.behaviorlogger.controllers;
    exports com.behaviorlogger.models;
    exports com.behaviorlogger.models.preferences;
    exports com.behaviorlogger.models.schemas;
    exports com.behaviorlogger.models.ioa;
    exports com.behaviorlogger.models.sessions;
    exports com.behaviorlogger.models.behaviors;
    exports com.behaviorlogger.persistence;
    exports com.behaviorlogger.persistence.recordings;
}
