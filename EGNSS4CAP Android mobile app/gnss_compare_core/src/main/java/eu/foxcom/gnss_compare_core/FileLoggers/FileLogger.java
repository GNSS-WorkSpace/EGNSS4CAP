/*
 * Copyright 2018 TFI Systems

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

package eu.foxcom.gnss_compare_core.FileLoggers;

import android.location.GnssMeasurementsEvent;
import android.os.Environment;
import android.util.Log;

import com.galfins.gogpsextracts.Coordinates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import eu.foxcom.gnss_compare_core.Constellations.Constellation;



public abstract class FileLogger {

    protected String TAG = "FileLogger";
    protected String initialLine = "";
    protected static final String ERROR_WRITING_FILE = "Problem writing to file.";

    protected String filePrefix;
    protected final Object mFileLock = new Object();
    protected BufferedWriter mFileWriter;
    protected File mFile;
    private boolean isStarted = false;

    public void setName(String calculationName) { filePrefix = calculationName; }

    public boolean isStarted() {
        return isStarted;
    }

    /**
     * Start a new file logging process.
     */
    public void startNewLog() {
        synchronized (mFileLock) {
            File baseDirectory;
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                baseDirectory = new File(Environment.getExternalStorageDirectory(), "GNSS Compare/" + filePrefix);
                baseDirectory.mkdirs();
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                Log.w(TAG, "Cannot write to external storage.");
                return;
            } else {
                Log.w(TAG, "Cannot read external storage.");
                return;
            }

            SimpleDateFormat formatter = new SimpleDateFormat("yyy_MM_dd_HH_mm_ss");
            Date now = new Date();
            String fileName = String.format("%s_%s_%s.txt", TAG, filePrefix, formatter.format(now));
            File currentFile = new File(baseDirectory, fileName);
            String currentFilePath = currentFile.getAbsolutePath();
            BufferedWriter currentFileWriter;
            try {
                currentFileWriter = new BufferedWriter(new FileWriter(currentFile));
            } catch (IOException e) {
                Log.w(TAG, "Could not open file: " + currentFilePath, e);
                return;
            }

            // initialize the contents of the file
            try {
                currentFileWriter.write(initialLine);
                currentFileWriter.newLine();
            } catch (IOException e) {
                Log.w(TAG, "Count not initialize file: " + currentFilePath, e);
                return;
            }

            if (mFileWriter != null) {
                try {
                    mFileWriter.close();
                } catch (IOException e) {
                    Log.w(TAG, "Unable to close all file streams.", e);
                    return;
                }
            }

            mFile = currentFile;
            mFileWriter = currentFileWriter;
            Log.i(TAG, "File opened: " + currentFilePath);

            isStarted = true;
        }
    }

    /**
     * Add new pose to the file
     */
    public abstract void addNewPose(Coordinates pose, Constellation constellation);

    /**
     * Send the current log via email or other options selected from a pop menu shown to the user. A
     * new log is started when calling this function.
     */
    public void closeLog() {
        if (mFile == null) {
            return;
        }
        if (mFileWriter != null) {
            try {
                mFileWriter.flush();
                mFileWriter.close();
                mFileWriter = null;
                Log.i(TAG, "File closed.");
                isStarted = false;
            } catch (IOException e) {
                Log.w(TAG, "Unable to close all file streams.", e);
                return;
            }
        }
    }

    public void onGnssMeasurementsReceived(GnssMeasurementsEvent event) {}

    /**
     * @return Name of the File Logger, which is to be displayed in the UI
     */
    public abstract String getName();

    private static HashMap<String, Class<? extends FileLogger>> registeredObjects = new HashMap<>();

    protected static void register(String fileLoggerName, Class<?extends FileLogger> objectClass) {
        if(!registeredObjects.containsKey(fileLoggerName))
            registeredObjects.put(fileLoggerName, objectClass);
    }

    public static Set<String> getRegistered(){
        return registeredObjects.keySet();
    }

    public static Class<? extends FileLogger> getClassByName(String name) {
        return registeredObjects.get(name);
    }

    private static boolean initialized = false;

    public static void initialize() {
        if(!initialized) {
            NmeaFileLogger.registerClass();
            SimpleFileLogger.registerClass();
            initialized = true;
        }
    }
}

/**
 * Created for the GSA in 2020-2021. Project management: SpaceTec Partners, software development: www.foxcom.eu
 */


