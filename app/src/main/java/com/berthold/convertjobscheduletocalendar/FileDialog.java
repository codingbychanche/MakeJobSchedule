/*
 * A simple file dialog activity.
 *
 */
package com.berthold.convertjobscheduletocalendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;

public class FileDialog extends AppCompatActivity {

    Bundle savedState;

    // GUI
    private ListView myListView;
    private ProgressBar progress;
    private ActionBar ab;

    // Filesystem
    private File currentPath;
    private String root = "/";
    private String lastPathVisited;
    private File[] fileObjects;                    // Contains the current folder's files as file- objects in the same order as 'directory'

    // List view
    private FileDialogFileListAdapter myListAdapter = null;

    // Async task
    private FileDialogFileListFiller task;

    // UI
    FloatingActionButton saveNow;

    // Return values
    // One or mor of these values may be written to the bundles 'Extras' when
    // the activity is finished. Which, depends on the task this activity was given by
    // the caller.
    public String status = "empty";
    public static final String RETURN_STATUS = "status";
    public static final String FOLDER_AND_FILE_PICKED = "folderAndFilePicked";
    public static final String JUST_THE_FOLDER_PICKED = "justFolder";

    // Activity control
    // You can configure this activity by setting the approbiate 'key' - 'value' in the Bundle's
    // extra's. Supported key/ value pairs are listed here.
    //
    // Alway's set the string "MY_TASK_FOR_TODAY_IS" as the key. Choose one of the listed
    // key- values to perform the desired action
    private String myTaskForTodayIs;
    public static final String MY_TASK_FOR_TODAY_IS = "myTaskForTodayIs";

    // Value
    // It tells the activity to act like a simple dir viewer. The activity will return the
    // full path of the file selected.
    public static final String GET_FILE_NAME_AND_PATH = "getFileAndPath";

    // If this flag is set, the calling activity requests a path
    // in order to write a file. If set, the following happens here:
    //
    //  1.  A save button is displayed. When pressed, the activity returns
    //      to the calling activity and provides the path which was displayed when
    //      the button was pressed. Now the caller can proceed and save it's data...
    //
    //  2.  When a file was picked, the full path is returned to the calling
    //      activity. A flag is returned, telling the caller, that a file was
    //      selected. The caller now can ask the user if he want's to replace this file...
    //
    //  If this flag was not set, the activity act's as a directory display tool which
    //  returns each readable file the user picked.
    public static final String SAVE_FILE = "saveFile";

    // Override last path visited
    // If pass this key with value 'true' if you do not wish fileCooser to open
    // the path which was opened when last session was finished
    public static final String OVERRIDE_LAST_PATH_VISITED = "overrideLastPathVisited";
    private Boolean overrideLastPathVisited = false;

    // Shared pref's
    // Last folder visited is saved here as an convenience for the user....
    SharedPreferences sharedPreferences;

    /**
     * Activity starts here...
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_chooser_main);
        savedState = savedInstanceState;

        ab = getSupportActionBar();
        ab.setTitle(getString(R.string.activity_name));

        // What do you wan't me to do???
        // The string 'myTaskForTodayIs' contains the task to be performed (e.g. 'SAVE_FILE')
        Bundle extra = getIntent().getExtras();
        myTaskForTodayIs = extra.getString(MY_TASK_FOR_TODAY_IS);

        // UI

        // Display the save button only, if this activity was called in order to
        // save a file....
        saveNow = (FloatingActionButton) findViewById(R.id.returnAndSave);
        if (!myTaskForTodayIs.equals(SAVE_FILE))
            saveNow.setVisibility(View.GONE);

        // Create list view
        myListView = (ListView) findViewById(R.id.mylist);
        ArrayList<FileDialogFileListOneEntry> dir = new ArrayList<>();       // Array containing the current path's dir
        myListAdapter = new FileDialogFileListAdapter(this, dir);
        myListView.setAdapter(myListAdapter);

        // Get current path from calling activity
        if (extra.get("path") != null) root = extra.get("path").toString();
        if (extra.get(OVERRIDE_LAST_PATH_VISITED) != null) {
            overrideLastPathVisited = extra.getBoolean(OVERRIDE_LAST_PATH_VISITED);
        }
        // If previously a path was saved, get it and use passed path as default.
        // This is convenient for the user because he always returns to the folder he
        // previously left by either selecting a file or pressing the back/ up button.
        if (!overrideLastPathVisited) {
            sharedPreferences = getPreferences(MODE_PRIVATE);
            root = sharedPreferences.getString("lastPathVisited", root);
        }

        currentPath = new File(root);
        if (!permissionIsDenied("READ_EXTERNAL_STORAGE"))
            refreshFiles(currentPath, myListAdapter);
        // toDo: Show that permission to access file system is not granted.

        // List view
        myListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // If a file was selected, get it's full path name and return to
                // the calling activity
                if (fileObjects[position].isFile() && fileObjects[position].canRead()) {

                    // Save path from which this file was picked from
                    // Current path does not contain a file name yet.....
                    lastPathVisited = currentPath.toString();

                    // Get current path+ file and return to calling activity
                    currentPath = fileObjects[position].getAbsoluteFile();
                    status = FOLDER_AND_FILE_PICKED;
                    finishIt();
                }

                // If list item is a directory, show it's files
                if (fileObjects[position].isDirectory()) {

                    // File should be readable and it should exist (e.g. folder 'sdcard')
                    // it might be shown but it could not be mounted yet. In that case
                    // it does not exist :-)
                    if (fileObjects[position].canRead() && fileObjects[position].exists()) {
                        // Get path and display it
                        currentPath = fileObjects[position].getAbsoluteFile();

                        // Update last path visited
                        lastPathVisited = currentPath.toString();
                        ActionBar ab = getSupportActionBar();
                        ab.setSubtitle(currentPath.toString());

                        // Refresh file object list and file ListArray
                        // Update listView => show changes
                        refreshFiles(currentPath, myListAdapter);
                        myListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        // Save button
        // Will not be displayed if 'MY_TASK_FOR_TODAY_IS' is set to 'GET_FILE_NAME_AND_PATH'
        saveNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = JUST_THE_FOLDER_PICKED;
                finishIt();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle s) {
        super.onSaveInstanceState(s);
        Log.v("Main", "Save state-------" + currentPath + "  " + task);
        s.putString("path", currentPath.toString());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (task != null) task.cancel(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (savedState != null) currentPath = new File(savedState.getString("path"));
        // Display current path
        ab.setSubtitle(currentPath.toString());
        refreshFiles(currentPath, myListAdapter);
    }

    /**
     * If Back button was pressed
     *
     * @rem:Shows how to check if the back button was pressed@@
     * <p>
     * This makes shure, that, even if the bacl- button was pressed
     * file picker is finished via it's on finish method....
     */
    @Override
    public void onBackPressed() {
        lastPathVisited = currentPath.toString();
        finishIt();
    }

    /**
     * Options menu
     *
     * @param menu
     * @since 7/2017
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_chooser, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // If up button was pressed, move to parent dir or leave activity if
        // we are already at "/"
        if (id == R.id.goup) {
            if (currentPath.toString().equals("/")) {
                // Cancel async task, if picture thumbnails for the
                // current dir are created....

                finishIt();
            } else {
                File parent = currentPath.getParentFile();
                currentPath = parent;
                ActionBar ab = getSupportActionBar();
                ab.setSubtitle(currentPath.toString());
                refreshFiles(parent, myListAdapter);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Refresh array containing file- objects
     */
    private void refreshFiles(File path, FileDialogFileListAdapter a) {
        if (task != null) task.cancel(true);
        // Get all file objects of the current path
        File files = new File(path.getPath());
        File[] fo = files.listFiles();
        // Sort by kind, and do not show files that are not readable
        if (fo != null)
            fileObjects = SortFiles.byKind(fo);

        // Clear dir array
        a.clear();
        progress = (ProgressBar) findViewById(R.id.pbar);
        task = new FileDialogFileListFiller(a, fileObjects, getApplicationContext(), progress);
        task.execute();
        return;
    }

    /**
     * Leave activity
     */
    public void finishIt() {
        task.cancel(true);

        // Enter the caller class's name here (second parameter)
        Intent i = new Intent(this, CallingActivitysInstance.class);
        i.putExtra("path", currentPath.toString());

        // Set return status
        i.putExtra(RETURN_STATUS, status);

        // Save current path to android's 'shared preferences'
        // This ensures that the user always returns to the last folder
        // visited.
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("lastPathVisited", lastPathVisited);
        editor.commit();

        // That's it....
        setResult(RESULT_OK, i);
        super.finish();
    }

    /**
     * Checks if a permission is granted or not.
     *
     * @param permission
     * @return true if the specified permission is granted.
     */
    private boolean permissionIsDenied(String permission) {
        //@rem: Shows how to check permissions
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                == PackageManager.PERMISSION_DENIED)
            return true;
        else
            return false;
        //@@
    }
}
