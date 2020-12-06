# Behavior Logger
AKA: "Behavior Logger Observational Collection System" or "BLOCS"

BLOCS is a data collection tool for applied behavior analysis. The interface
was designed for practitioners conducting treatment while one or more
data-collectors record behaviors. See [manual.md](resources/manual/manual.md) for more user information.

# Install

Prebuilt installers can be downloaded from https://behaviorlogger.com/desktop

# Development
## Requirements
- gradle
- java 14 or later
    - jdeps
    - javac
    - jar
    - jlink
    - jpackage
- python3

## Test
```bash
./gradlew test
```

## Run
```bash
./gradlew run
```

## Build
```bash
./gradlew jpackageImage
```
