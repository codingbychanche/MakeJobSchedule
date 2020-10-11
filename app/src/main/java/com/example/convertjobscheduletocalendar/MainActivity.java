package com.example.convertjobscheduletocalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import CalendarMaker.*;
import berthold.filedialogtool.FileDialog;

/**
 * Reads a text file, checks for valid calendar entries and writes them to the devices calendar.
 *
 * @author Berthold Fritz
 */
public class MainActivity extends AppCompatActivity {

    // Shared prefs
    SharedPreferences sharedPreferences;

    // File dialog tool
    private String pathToCurrentCalendarFile;
    private static final int ID_FILE_DIALOG = 1;
    private static final boolean OVERRIDE_LAST_PATH_VISITED = false;

    // Calendar list
    private RecyclerView jobScheduleListRecyclerView;
    private RecyclerView.Adapter jobScheduleListAdapter;
    private RecyclerView.LayoutManager jobScheduleListLayoutManager;
    private List<CalendarEntry> jobScheduleListData = new ArrayList<>();

    // Misc
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=getApplicationContext();

        // Was a calendar file opened previously?
        //
        // If so, proceed, if not, open the file dialog tool for the user allowing
        // him to pick a suitable file...
        pathToCurrentCalendarFile = currentStateRestoreFromSharedPref();

        if (!readAndParseJobSchedule(pathToCurrentCalendarFile))
            openFileDialog();
    }

    /**
     * Some System callbacks...
     *
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Calendar list
        // List of expenses
        RecyclerView expensesListRecyclerView = (RecyclerView) findViewById(R.id.job_schedule_list);
        expensesListRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager expensesListLayoutManager = new LinearLayoutManager(this);
        expensesListRecyclerView.setLayoutManager(expensesListLayoutManager);
        RecyclerView.Adapter expensesListAdapter = new JobScheduleListAdapter(jobScheduleListData, this,context);
        expensesListRecyclerView.setAdapter(expensesListAdapter);

        // Read job schedule
        ImageButton startButtonView = findViewById(R.id.start);
        startButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileDialog();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        currentStateSaveToSharedPref(pathToCurrentCalendarFile);
    }

    @Override
    public void onBackPressed() {
        currentStateSaveToSharedPref(pathToCurrentCalendarFile);
        finish();
    }

    /**
     * File Dialog Tool callback.
     *
     * @param reqCode Code which idendifies the fileDialogTool as the returning activity
     * @param resCode OK, error, etc...
     * @param data    Selected path.
     */
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if (resCode == RESULT_OK && reqCode == ID_FILE_DIALOG) {
            if (data.hasExtra("path")) {

                pathToCurrentCalendarFile = data.getExtras().getString("path");

                readAndParseJobSchedule(pathToCurrentCalendarFile);
            }
        }
    }

    /**
     * Read and parse a job schedule file.
     *
     * @param pathToCurrentCalendarFile
     * @return True is the file could be read, false if there was an i/o error...
     */
    private boolean readAndParseJobSchedule(String pathToCurrentCalendarFile) {

        List<CalendarEntry> calendar = new ArrayList();
        MakeCalendar myCalendar = new MakeCalendar(pathToCurrentCalendarFile);

        calendar = myCalendar.getRawCalendar();

        TextView linesReadView = findViewById(R.id.lines_read);
        TextView linesValidView = findViewById(R.id.lines_valid);
        TextView linesNotValidView = findViewById(R.id.lines_not_valid);

        linesReadView.setText("" + myCalendar.getTotalNumberOfLinesRead());
        linesValidView.setText("" + myCalendar.getNumberOfLinesValid());
        linesNotValidView.setText("" + myCalendar.getNumberOfLinesNotValid());

        if (myCalendar.hasError()) {
            return false;
        } else {
            for (CalendarEntry e : calendar) {
                //if (e.isValidEntry)
                jobScheduleListData.add(e);
            }
        }
        return true;
    }

    /**
     * Select a job schedule file via the file dialog tool.
     */
    private void openFileDialog() {
        Intent i = new Intent(MainActivity.this, FileDialog.class);
        i.putExtra(FileDialog.MY_TASK_FOR_TODAY_IS, FileDialog.SAVE_FILE);
        i.putExtra(FileDialog.OVERRIDE_LAST_PATH_VISITED, OVERRIDE_LAST_PATH_VISITED);
        startActivityForResult(i, ID_FILE_DIALOG);
    }

    /**
     * Save current state to sharedPreferences.
     */
    private void currentStateSaveToSharedPref(String pathToCurrentCalendarFile) {
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("pathToCurrentCalendarFile", pathToCurrentCalendarFile);
        editor.commit();
    }

    /**
     * Restore from shared pref's..
     */
    private String currentStateRestoreFromSharedPref() {
        sharedPreferences = getPreferences(MODE_PRIVATE);
        int key1DeffaultValue = -1;
        return pathToCurrentCalendarFile = sharedPreferences.getString("pathToCurrentCalendarFile", "-");

    }

}