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

## Test
```bash
gradle test
```

## Run
```bash
gradle run
```

## Build
```bash
gradle clean jpackage
```

## Deploy
The archives/installers are stored in `s3://behaviorlogger.com/desktop-app/downloads/`.

### Linux
```bash
VERSION=1.1.3
mv build/jpackage/blocs/ build/blocs-${VERSION}
tar -C build -czvf build/blocs-${VERSION}.tar.gz blocs-${VERSION}
aws s3 cp build/blocs-${VERSION}.tar.gz s3://behaviorlogger.com/desktop-app/downloads/
```

### Mac
TODO

### Windows
Manually upload the build step's `.exe` file to `s3://behaviorlogger.com/desktop-app/downloads/`
