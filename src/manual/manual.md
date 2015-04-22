<link rel="stylesheet" href="markdown3.css">
<a name="introduction"></a>
# Introduction

The 3Bird Behavior Logger is a data collection tool for applied behavior analysis. The interface was designed for researchers conducting treatment while one or more data-collectors record behaviors. We recognize that every lab has different preferences on methods and so Behavior Logger allows for substantial customization. This manual details each of the screens and how to achieve some of the most common use-cases.

---

<a name="installation"></a>
# Installation

<a name="windows"></a>
## Windows

After downloading the installer (3Bird Behavior Logger.exe), double-click the file. ...

<a name="mac"></a>
## Mac

After downloading the installer (3Bird Behavior Logger.dmg), double-click the file. ...

---

<a name="start-menu"></a>
# Start Menu

*insert image of start menu*

1. **Schema List** - A list of current schemas saved on your computer.
2. **Create Schema** - Opens a menu for creating a new schema.
3. **Edit Schema** - Opens the selected schema into an edit menu.
4. **Preferences** - Opens a menu for customizing default values.
5. **IOA Calculator** - Opens a menu for calculating IOA between two data files.
6. **Key-Behavior Mappings** - A summary of the selected schema's key-behavior mappings.
7. **New Session Summary** - The details to be applied to the upcoming session.
  - *Duration* - The duration of the session (set by the schema).
  - *Observer* - The name of the observer collecting data.
  - *Therapist* - The name of the therapist performing the treatment.
  - *Condition* - The type of treatment being applied.
  - *Session #* - The number of the next session. This number will increment automatically between sessions.
  - *Data File* - The name of the data-file the next session will generate. Users can configure this in [Preferences](#preferences). The app will warn the user if this file will overwrite an existing one of the same name.

---

<a name="edit-schema"></a>
# Edit Schema

*insert image of edit schema menu*

1. **Client** - The identifier of the client.
2. **Project** - The research project this schema is for.
3. **Key-Behavior Mappings** - The keys used to record target behaviors.
  - *Cont.* - Check if this is a *continuous behavior* (ie. it has a duration rather than a count).
  - *Key* - A single character to represent the behavior. Accepted character include lower-case letters, digits, and some symbols ( ` '  - = [ ] ; , . \ / )
  - *Behavior* - A description of the target behavior.
4. **Add Row** - Adds a new row to the Mappings section.
5. **Delete Schema** - Deletes the current schema. Prompts the user for confirmation.
6. **Save Session Data To** - The directory session data-files will get saved to.
7. **Session Duration**
  - *infinite* - If checked, sessions will never end.
  - *timed* - If checked, sessions will be considered "finished" when they reach the set duration. If this field is set to zero, or left empty, it's the same as checking *infinite*.
  - *When timer finishes* - Check each action to take when a session is over.
8. **Cancel** - Discard all changes and return to Start Menu.
9. **Save** - Saves changes and returns to Start Menu. If this is a new schema, it will show up in the schema list.

---

<a name="preferences"></a>
# Preferences

*insert image of preferences menu*

1. **Default Save Location** - New schemas will start with this directory.
2. **Filename Format** - Since most researchers have a preferred naming structure for their data-files, we allow users to customize them. Check each component that you want to include in the filename. You can also drag-and-drop components to reorder them.
3. **Default Session Duration** - New schemas will start with this duration setting.

---

<a name="recording"></a>
# Recording

*insert image of the recording view*

1. **Behavior Summary** - Each row keeps track of the count/duration of each behavior.
2. **Play/Stop** - Starts and Stops the recording session. You can also press `spacebar` as a shortcut. You can only record behaviors while playing.
3. **Timer** - The timer will increase while playing. If the current schema has a set duration and the timer reaches it, it performs the checked actions as specified in the [edit-schema menu](#edit-schema).
4. **Start New Session** - Starts a new session with the same schema and same session-detais. The session #, however, will increment.
5. **Edit Uknown Behaviors** - Only visible if you have logged an unknown behavior, it will open a new menu for assigning values to any you might have logged.

<a name="logging-behaviors"></a>
## Logging Behaviors
While a session is playing, press any of the mapped keys to log a behavior. **Discrete** behaviors flash green and the counter will increment. **Continuous** behaviors toggle on and off.

<a name="logging-new-behaviors"></a>
## Logging New Behaviors

You can also log new behaviors on the fly while recording.

**Discrete** - press an unmapped key.  
**Continuous** - press an unmapped key while holding `ctrl`. Once the new continuous key is added, you don't have to hold `ctrl` to toggle it.

When you pause the session, you can click *Edit Unknown Behaviors* to assign values to any unknowns you might have logged. If you logged an unknown by mistake, you can ignore it.

<a name="session-output"></a>
## Session Output

The app saves logged behaviors in the directory specified by the Schema (see [Edit Schema](#edit-schema)). It outputs two files per session:

- a human-readable *Excel* file summarizing the session
- a *CSV* file meant for the [IOA Calculator](#ioa-calculator)

Tnames of these files can be configured in [Preferences](#preferences).

---

<a name="ioa-calculator"></a>
# IOA Calculator

*insert image of IOA calculator*

1. **File 1 / File 2** - Paths to the .csv data-files you wish to compare. You should upload the data output from your behavior recording sessions.
2. **Method** - The type of calculation to be done. See [IOA Methods](#ioa-methods).
3. **Block Size** - The number of seconds designated to each partition in partial/exact agreement.
4. **Threshold** - The number of seconds of leniency in time-window analysis.
5. **Close** - Closes the calculator window.
6. **Generate IOA File** - Calculates IOA between File 1 and File 2 and prompts the user save the result in an Excel file.

---

<a name="ioa-methods"></a>
# IOA Methods

Each method below details how to calculate a percent agreement for each behavior logged by two observers during a session. These methods were derived from two papers (see [References](#references)). 

<a name="exact-aggrement"></a>
## Exact Agreement

*Exact agreement* outputs a single percent agreement for a behavior between both observers. The data-logs from File 1 and File 2 get partitioned into intervals of a size specified by `block-size`. For each interval, the observers are considered in agreement if they both recorded the same number of occurrences within the interval. A behavior's percent agreement is equal to the number of agreements divided by the total number of intervals and multiplied by 100%.

<a name="partial-aggrement"></a>
## Partial Agreement

Similar to *exact agreement*, the data-logs from File 1 and File 2 get partitioned according to `block-size`. A score between 0 and 1 is calcuated per interval by dividing the smaller of the two behavior counts by the larger. If both counts are zero, the score equals 1. A behavior's percent agreement is equal to the sum of all scores divided by the number of intervals, multiplied by 100%.

<a name="time-window"></a>
## Time Window

*Time window* varies from the other two in that it generates two percent agreements for each discrete behavior (one for each observer) and one percent agreement for continuous behaviors.

For **discrete behaviors**, each occurrence within each data-log is given a score of 0 or 1. A behavor recorded by one observer gets a 1 if the other observer recorded the same behavior within &plusmn; `threshold` seconds. If the behavior does not have a match within the bounds set by `threshold`, it gets a score of 0. The discrete behavior's percent agreement for each observer is equal to the sum of the scores, divided by the total occurrences, and multipled by 100%.

For **continuous behaviors**, the `threshold` does not matter. The data-log is partitioned into 1-second intervals. A continuous behavior's percent agreement is equal to the number of intervals where both observers recorded it, divided by the number of intervals where either of the observers recorded it, multiplied by 100%.

---

<a name="references"></a>
# References

MacLean, W.E., Tapp, J.T., Johnson, W.L. (1985). Alternate Methods and Software for Calculating Interobserver Agreement for Continuous Observation Data. *Journal of Psychopathology and Behavioral Assessment, 8*.

Mudford, O.C., Taylor, S.A., & Martin, N.T. (2009). Continuous Recording and Interobserver Agreement Algorithms Reported in the Journal of Applied Behavior Analysis (1995-2005). *Journal of Applied Behavior Analysis, 42*, 165-169.*