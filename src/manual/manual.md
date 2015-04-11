<a name="introduction"></a>
# Introduction

The 3Bird Behavior Logger is a data collection tool for applied behavior analysis. The interface was designed for researchers conducting treatment while one or more data-collectors record behaviors. We recognize that every lab has different preferences on methods and so Behavior Logger allows for substantial customization. This manual details each of the screens and how to achieve some of the most common use-cases.

<a name="installation"></a>
# Installation

<a name="windows"></a>
## Windows

After downloading the installer (3Bird Behavior Logger.exe), double-click the file. ...

<a name="mac"></a>
## Mac

After downloading the installer (3Bird Behavior Logger.dmg), double-click the file. ...

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
  - *Data File* - The name of the data-file the next session will generate. Users can configure this in [Preferences](#preferences).

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

<a name="preferences"></a>
# Preferences

*insert image of preferences menu*