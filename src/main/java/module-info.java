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

    opens com.behaviorlogger to javafx.fxml;
    opens com.behaviorlogger.controllers to javafx.fxml;

    exports com.behaviorlogger;
    exports com.behaviorlogger.controllers;
    exports com.behaviorlogger.models;
    exports com.behaviorlogger.models.schemas;
    exports com.behaviorlogger.models.sessions;
    exports com.behaviorlogger.models.behaviors;
}
