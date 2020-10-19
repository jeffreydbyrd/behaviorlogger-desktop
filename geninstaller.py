import os
import sys

poi_module_info = """
module poi {
    requires commons.math3;
    requires java.logging;
    requires org.apache.commons.codec;
    requires org.apache.commons.collections4;

    requires transitive SparseBitSet;
    requires transitive java.desktop;
    requires transitive java.xml;

    exports org.apache.poi;
    exports org.apache.poi.common;
    exports org.apache.poi.common.usermodel;
    exports org.apache.poi.common.usermodel.fonts;
    exports org.apache.poi.ddf;
    exports org.apache.poi.extractor;
    exports org.apache.poi.hpsf;
    exports org.apache.poi.hpsf.extractor;
    exports org.apache.poi.hpsf.wellknown;
    exports org.apache.poi.hssf;
    exports org.apache.poi.hssf.dev;
    exports org.apache.poi.hssf.eventmodel;
    exports org.apache.poi.hssf.eventusermodel;
    exports org.apache.poi.hssf.eventusermodel.dummyrecord;
    exports org.apache.poi.hssf.extractor;
    exports org.apache.poi.hssf.model;
    exports org.apache.poi.hssf.record;
    exports org.apache.poi.hssf.record.aggregates;
    exports org.apache.poi.hssf.record.cf;
    exports org.apache.poi.hssf.record.chart;
    exports org.apache.poi.hssf.record.common;
    exports org.apache.poi.hssf.record.cont;
    exports org.apache.poi.hssf.record.crypto;
    exports org.apache.poi.hssf.record.pivottable;
    exports org.apache.poi.hssf.usermodel;
    exports org.apache.poi.hssf.usermodel.helpers;
    exports org.apache.poi.hssf.util;
    // exports org.apache.poi.poifs;
    exports org.apache.poi.poifs.common;
    exports org.apache.poi.poifs.crypt;
    exports org.apache.poi.poifs.crypt.binaryrc4;
    exports org.apache.poi.poifs.crypt.cryptoapi;
    exports org.apache.poi.poifs.crypt.standard;
    exports org.apache.poi.poifs.crypt.xor;
    exports org.apache.poi.poifs.dev;
    exports org.apache.poi.poifs.eventfilesystem;
    exports org.apache.poi.poifs.filesystem;
    exports org.apache.poi.poifs.macros;
    exports org.apache.poi.poifs.nio;
    exports org.apache.poi.poifs.property;
    exports org.apache.poi.poifs.storage;
    exports org.apache.poi.sl.draw;
    exports org.apache.poi.sl.draw.binding;
    exports org.apache.poi.sl.draw.geom;
    exports org.apache.poi.sl.extractor;
    exports org.apache.poi.sl.image;
    exports org.apache.poi.sl.usermodel;
    exports org.apache.poi.ss;
    exports org.apache.poi.ss.extractor;
    exports org.apache.poi.ss.format;
    exports org.apache.poi.ss.formula;
    exports org.apache.poi.ss.formula.atp;
    exports org.apache.poi.ss.formula.constant;
    exports org.apache.poi.ss.formula.eval;
    exports org.apache.poi.ss.formula.eval.forked;
    exports org.apache.poi.ss.formula.function;
    exports org.apache.poi.ss.formula.functions;
    exports org.apache.poi.ss.formula.ptg;
    exports org.apache.poi.ss.formula.udf;
    exports org.apache.poi.ss.usermodel;
    exports org.apache.poi.ss.usermodel.charts;
    exports org.apache.poi.ss.usermodel.helpers;
    exports org.apache.poi.ss.util;
    exports org.apache.poi.ss.util.cellwalk;
    exports org.apache.poi.util;
    exports org.apache.poi.wp.usermodel;

}
"""


dir_target = "./target"
dir_lib = dir_target + "/lib"
dir_modinfo = dir_target + "/modinfo"

deps = [
    # Java FX
    {"jar": "javafx-base-14-{platform}.jar",
     "modname": "javafx.base",
     "modular?": True, },
    {"jar": "javafx-base-14.jar",
     "modname": "javafx.baseEmpty"},
    {"jar": "javafx-graphics-14-{platform}.jar",
     "modname": "javafx.graphics",
     "modular?": True, },
    {"jar": "javafx-graphics-14.jar",
     "modname": "javafx.graphicsEmpty"},
    {"jar": "javafx-controls-14-{platform}.jar",
     "modname": "javafx.controls",
     "modular?": True, },
    {"jar": "javafx-controls-14.jar",
     "modname": "javafx.controlsEmpty"},

    # FXML
    {"jar": "javafx-fxml-14-{platform}.jar",
     "modname": "javafx.fxml",
     "modular?": True, },
    {"jar": "javafx-fxml-14.jar",
     "modname": "javafx.fxmlEmpty"},

    # JUNIT
    {"jar": "hamcrest-core-1.3.jar",
     "modname": "hamcrest.core"},
    {"jar": "junit-4.13.jar",
     "modname": "junit"},

    # Google Guava
    {"jar": "animal-sniffer-annotations-1.17.jar",
     "modname": "animal.sniffer.annotations"},
    {"jar": "checker-qual-2.8.1.jar",
     "modname": "org.checkerframework.checker.qual"},
    {"jar": "j2objc-annotations-1.3.jar",
     "modname": "j2objc.annotations"},
    {"jar": "error_prone_annotations-2.3.2.jar",
     "modname": "error.prone.annotations"},
    {"jar": "jsr305-3.0.2.jar",
     "modname": "jsr305"},
    {"jar": "listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar",
     "modname": "listenablefuture"},
    {"jar": "failureaccess-1.0.1.jar",
     "modname": "failureaccess"},
    {"jar": "guava-28.0-jre.jar",
     "modname": "com.google.common"},

    # GSON
    {"jar": "gson-2.8.6.jar",
     "modname": "com.google.gson",
     "modular?": True, },

    # Commons CSV
    {"jar": "commons-csv-1.8.jar",
     "modname": "commons.csv"},

    # Apache POI
    {"jar": "commons-codec-1.13.jar",
     "modname": "org.apache.commons.codec"},
    {"jar": "commons-collections4-4.4.jar",
     "modname": "org.apache.commons.collections4"},
    {"jar": "commons-math3-3.6.1.jar",
     "modname": "commons.math3"},
    {"jar": "SparseBitSet-1.2.jar",
     "modname": "SparseBitSet"},
    {"jar": "poi-4.1.2.jar",
     "modname": "poi",
     "module-info": poi_module_info},

    # Apache HTTP Client
    {"jar": "httpcore-4.4.13.jar",
     "modname": "org.apache.httpcomponents.httpcore"},
    {"jar": "commons-logging-1.2.jar",
     "modname": "commons.logging"},
    {"jar": "httpclient-4.5.12.jar",
     "modname": "org.apache.httpcomponents.httpclient"},

    # Joda Time
    {"jar": "joda-time-2.10.6.jar",
     "modname": "org.joda.time"},

    # SQLite
    {"jar": "sqlite-jdbc-3.32.3.2.jar",
     "modname": "sqlite.jdbc"},
]


def main():
    platform = "linux"
    if len(sys.argv) > 1:
        platform = sys.argv[1]

    for dep in deps:
        dep["jar"] = dep["jar"].format(platform=platform)

    classpath_sep = ":"
    if platform == "win":
        classpath_sep = ";"

    if bash(f"mvn clean prepare-package") != 0:
        print("mvn clean prepare-package failed")
        return

    modular_deps = []

    for dep in deps:
        jarpath = f"{dir_lib}/{dep['jar']}"
        if dep.get("modular?"):
            print("[DEBUG] skipping modular jar")
            modular_deps.append(dep)
            continue

        module_path = classpath_sep.join(
            [f'{dir_lib}/{dep["jar"]}' for dep in modular_deps])
        modules = ",".join([dep["modname"] for dep in modular_deps])
        module_info_dir = f"{dir_modinfo}/{dep['modname']}"
        module_info_file = f"{module_info_dir}/module-info.java"

        if dep.get("module-info"):
            os.makedirs(module_info_dir)
            with open(module_info_file, "w") as f:
                f.write(dep["module-info"])
        else:
            # build jdeps command
            jdeps_cmd = f"jdeps --ignore-missing-deps --generate-module-info {dir_modinfo}"
            if dep.get("multi-release?"):
                jdeps_cmd = jdeps_cmd + f" --multi-release=15"
            if len(modular_deps) > 0:
                jdeps_cmd = jdeps_cmd + \
                    f" --module-path {module_path} --add-modules {modules}"
            jdeps_cmd = jdeps_cmd + f" {jarpath}"
            if bash(jdeps_cmd) != 0:
                print("jdeps failed")
                return

        # compile module-info.java
        javac_cmd = f"javac -p {module_path} --add-modules {modules} --patch-module {dep['modname']}={jarpath} {module_info_file} -d {module_info_dir}"
        if bash(javac_cmd) != 0:
            print("javac failed")
            return

        # update original jar with module-info.class
        jar_cmd = f"jar uf {dir_lib}/{dep['jar']} -C  {module_info_dir} module-info.class"
        if bash(jar_cmd) != 0:
            print("jar failed")
            return

        modular_deps.append(dep)

    module_path = classpath_sep.join([f'{dir_lib}/{dep["jar"]}' for dep in modular_deps])
    modules = ",".join([dep["modname"] for dep in modular_deps])
    jlink_cmd = f"jlink --module-path ./target/classes/{classpath_sep}{module_path} --add-modules behaviorlogger --output target/behaviorlogger --compress 2 --launcher launcher=behaviorlogger/com.behaviorlogger.BehaviorLoggerApp"
    if bash(jlink_cmd) != 0:
        print("jlink failed")
        return

    jpackage_cmd = "jpackage --runtime-image target/behaviorlogger -n blocs -d target"
    if bash(jpackage_cmd) != 0:
        print("jpackage failed")


def bash(cmd):
    print(f"[DEBUG] running {cmd}")
    return os.system(cmd)


if __name__ == "__main__":
    main()
