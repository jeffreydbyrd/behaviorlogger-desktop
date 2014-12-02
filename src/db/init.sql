CREATE TABLE IF NOT EXISTS schemas (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  project TEXT NOT NULL,
  session_directory TEXT NOT NULL,
  duration INTEGER NOT NULL,
  pause_on_end INTEGER NOT NULL,
  color_on_end INTEGER NOT NULL,
  sound_on_end INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS key_behaviors (
  schema_id INTEGER NOT NULL,
  key CHAR(1) NOT NULL,
  behavior TEXT NOT NULL,
  is_continuous INTEGER NOT NULL,

  FOREIGN KEY (schema_id) REFERENCES schemas(id),
  PRIMARY KEY(schema_id, key)
);
