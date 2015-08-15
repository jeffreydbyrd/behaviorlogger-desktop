package com.threebird.recorder.utils.resources;

import java.io.File;

interface ResourceService
{
  File getResources();

  File getDb();

  File getPrefs();

  File getSessionDetails();

  File getIoaDetails();
  
  File getManual();
}