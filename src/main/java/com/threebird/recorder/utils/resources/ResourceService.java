package com.threebird.recorder.utils.resources;

import java.io.File;

interface ResourceService
{
  boolean createDB();

  String getDbPath();

  File getPrefs();

  File getResources();
}