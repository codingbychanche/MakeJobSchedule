package com.example.convertjobscheduletocalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.loader.content.AsyncTaskLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CalendarContract;
import android.transition.Transition;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import CalendarMaker.*;
import berthold.filedialogtool.FileDialog;

/**
 * Reads a text file, checks for valid calendar entries and writes them to the devices calendar.
 *
 * @author Berthold Fritz
 */
public class MainActivity extends AppCompatActivity implements JobScheduleListAdapter.receieve {

    // Shared prefs
    SharedPreferences sharedPreferences;

    // File system
    public static File workingDir;
    public String appDir = "/Meine_Einsatzpläne";       // App's working dir..

    // File dialog tool
    private String pathToCurrentCalendarFile = appDir;
    private static final int ID_FILE_DIALOG = 1;
    private static final boolean OVERRIDE_LAST_PATH_VISITED = false;

    // Calendar list
    private RecyclerView jobScheduleListRecyclerView;
    private RecyclerView.Adapter jobScheduleListAdapter;
    private RecyclerView.LayoutManager jobScheduleListLayoutManager;
    private List<CalendarEntry> jobScheduleListData = new ArrayList<>();

    // Async Task
    JobScheduleListFiller task;

    // UI
    RadioButton selectAllView;
    RadioButton selectValidView;
    RadioButton selectInvalidView;
    CheckBox showOnlyFutureEventsView;

    // Misc
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        // File system
        //
        // @rem:Filesystem, creates public folder in the devices externalStorage dir...@@
        //
        // This seems to be the best practice. It creates a public folder.
        // This folder will not be deleted when the app is de- installed
        workingDir = Environment.getExternalStoragePublicDirectory(appDir);
        workingDir.mkdirs(); // Create dir, if it does not already exist

        // UI
        selectAllView = findViewById(R.id.select_all);
        selectValidView = findViewById(R.id.select_valid);
        selectInvalidView = findViewById(R.id.select_invalid);
        showOnlyFutureEventsView = findViewById(R.id.show_only_future_events);

        // Calendar list
        jobScheduleListRecyclerView = (RecyclerView) findViewById(R.id.job_schedule_list);
        jobScheduleListRecyclerView.setHasFixedSize(true);
        jobScheduleListLayoutManager = new LinearLayoutManager(this);
        jobScheduleListRecyclerView.setLayoutManager(jobScheduleListLayoutManager);
        jobScheduleListAdapter = new JobScheduleListAdapter(jobScheduleListData, this, context);
        jobScheduleListRecyclerView.setAdapter(jobScheduleListAdapter);

        // Was a calendar file opened previously?
        //
        // If so, proceed, if not, open the file dialog tool for the user allowing
        // him to pick a suitable file...
        pathToCurrentCalendarFile = currentStateRestoreFromSharedPref();

        if (!readAndParseJobSchedule(pathToCurrentCalendarFile))
            openFileDialog();

        selectAllView.toggle();
        readAndParseJobSchedule(pathToCurrentCalendarFile);

        getTodaysEvent();
    }

    /**
     * Some System callbacks...
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Read job schedule
        ImageButton startButtonView = findViewById(R.id.start);
        startButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentStateSaveToSharedPref(pathToCurrentCalendarFile);
                openFileDialog();
            }
        });

        // SHow info
        ImageButton infoButtonView = findViewById(R.id.info);
        infoButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentStateSaveToSharedPref(pathToCurrentCalendarFile);
                Intent i = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(i);
            }
        });

        // Filter settings
        selectAllView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readAndParseJobSchedule(pathToCurrentCalendarFile);
            }
        });

        selectInvalidView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readAndParseJobSchedule(pathToCurrentCalendarFile);
            }
        });

        selectValidView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readAndParseJobSchedule(pathToCurrentCalendarFile);
            }
        });

        // Show only future events?
        showOnlyFutureEventsView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                readAndParseJobSchedule(pathToCurrentCalendarFile);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
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

                String returnStatus = data.getExtras().getString(FileDialog.RETURN_STATUS);
                String pathSelected = data.getExtras().getString("path");

                Log.v("PATH_PATH", returnStatus);
                if (returnStatus.equals(FileDialog.FOLDER_AND_FILE_PICKED))
                    pathToCurrentCalendarFile = pathSelected;

                readAndParseJobSchedule(pathToCurrentCalendarFile);
            }
        }
    }

    /**
     * This adds the selected calendar entry to the devices calendar app
     *
     * @param position
     */
    @Override
    public void addToCalendarPressed(int position) {

        // This code creates an event and will open the calendar app's save dialog.
        // Doing so is quite good practice and a suitable method if
        // one wants to add only one event....
        //
        // Source: {@link https://stackoverflow.com/questions/4373074/how-to-launch-android-calendar-application-using-intent-froyo}
        int year = jobScheduleListData.get(position).getYear() + 2000;
        int month = jobScheduleListData.get(position).getMonth();
        int day = jobScheduleListData.get(position).getDay();
        int startH = jobScheduleListData.get(position).getStartTimeHours();
        int startM = jobScheduleListData.get(position).getStartTimeMinutes();
        int endH = jobScheduleListData.get(position).getEndTimeHours();
        int endM = jobScheduleListData.get(position).getEndTimeMinutes();

        if (endH == 0 && endM == 0) {
            endH = 23;
            endM = 59;
        }

        String courseNumber = jobScheduleListData.get(position).getCourseNumber();
        String location = jobScheduleListData.get(position).getLocation();
        String vag = jobScheduleListData.get(position).getVagNumber();
        String description = courseNumber + "  VAG:" + vag;

        //@rem; Shows how a date can be converted to millisec's@@
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(year, month - 1, day, startH, startM);
        Long startMillis = beginTime.getTimeInMillis();
        //@@
        Calendar endTime = Calendar.getInstance();
        endTime.set(year, month - 1, day, endH, endM);
        Long endMillis = endTime.getTimeInMillis();

        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", startMillis);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);

        // todo: Check {@link ClendarMaker}. Should return a prober description if
        // no start and end time where set....
        //
        // All day event if start time is 0:00, we assume that in this case
        // for this event no time was set. Convert to all day event:
        if (startH == 0 && endH == 23) {
            intent.putExtra("allDay", true);
        } else
            intent.putExtra("allDay", false);

        //intent.putExtra("rule", "FREQ=YEARLY");
        intent.putExtra("endTime", endMillis);
        intent.putExtra("title", description);
        startActivity(intent);
    }

    /**
     * Add selected event to an e- mail.
     *
     * @param position
     */

    @Override
    public void addToEMail(int position) {

        String courseNumber = jobScheduleListData.get(position).getCourseNumber();
        String vagNumber = jobScheduleListData.get(position).getVagNumber();
        String location = jobScheduleListData.get(position).getLocation();
        String message = jobScheduleListData.get(position).getOrgiriginalEntry();
        String date = jobScheduleListData.get(position).getDate();

        String subject = "Anfrage zu VAG:" + vagNumber + "//" + courseNumber + " in " + location + " am " + date;

        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, "");
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT, message);

        //need this to prompts email client only
        email.setType("message/rfc822");

        startActivity(Intent.createChooser(email, "Choose an Email client :"));
    }

    /**
     * Read and parse a job schedule file.
     *
     * @param pathToCurrentCalendarFile
     * @return True is the file could be read, false if there was an i/o error...
     */
    private boolean readAndParseJobSchedule(String pathToCurrentCalendarFile) {

        jobScheduleListData.clear();
        jobScheduleListAdapter.notifyDataSetChanged();

        List<CalendarEntry> calendar = new ArrayList();
        MakeCalendar myCalendar = new MakeCalendar(pathToCurrentCalendarFile);

        calendar = myCalendar.getRawCalendar();

        long currentTimeInMillisec = System.currentTimeMillis();

        if (myCalendar.hasError()) {
            return false;
        } else {
            for (CalendarEntry calendarEntry : calendar) {

                Long currentEventTimeInMillisec = calendarEntry.getEventTimeInMillisec();

                if (showOnlyFutureEventsView.isChecked()) {
                    if (currentEventTimeInMillisec > currentTimeInMillisec)
                        addEvent(calendarEntry);
                } else
                    addEvent(calendarEntry);
            }
        }
        jobScheduleListAdapter.notifyDataSetChanged();

        return true;
    }

    /**
     * Add a calendar entry to the calendar according
     * to view options selected by the user
     *
     * @param calendarEntry
     * @global jobScheduleListData  Holding calendar entries currently visible to the user.
     */
    private void addEvent(CalendarEntry calendarEntry) {

        if (selectAllView.isChecked()) {
            jobScheduleListData.add(calendarEntry);
        }

        if (selectValidView.isChecked()) {
            if (calendarEntry.isValidEntry)
                jobScheduleListData.add(calendarEntry);
        }

        if (selectInvalidView.isChecked()) {
            if (!calendarEntry.isValidEntry)
                jobScheduleListData.add(calendarEntry);
        }
    }

    /**
     * Get and show today's event permanently
     */
    private void getTodaysEvent() {

        TextView dayOfWeekView = findViewById(R.id.day_of_week);
        TextView dateView = findViewById(R.id.date);
        TextView startTimeView = findViewById(R.id.start_time);
        TextView endTimeView = findViewById(R.id.end_time);
        TextView vagNumberView = findViewById(R.id.vag_number);
        TextView courseNumberView = findViewById(R.id.course_number);
        TextView loctionView = findViewById(R.id.location);
        TextView typeView = findViewById(R.id.type);
        TextView holidayView = findViewById(R.id.holiday_remark);

        int[] dayOfWeek = {R.string.so, R.string.mo, R.string.di, R.string.mi, R.string.don, R.string.fr, R.string.sa};
        int positionOfTodaysEvent = 0;

        long currentTimeInMillisec = System.currentTimeMillis();
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.setTimeInMillis(currentTimeInMillisec);

        if (todaysDate.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && todaysDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {

            // Lookup the current day and display it permanently
            for (CalendarEntry entry : jobScheduleListData) {
                if (entry.compareThisEntrysDateWith(todaysDate) == entry.IS_TODAY && entry.isValidEntry) {

                    int dayNameResourche = dayOfWeek[entry.getDayOfWeekForThisDate() - 1];
                    dayOfWeekView.setText(context.getString(dayNameResourche));

                    dateView.setText(entry.getDate());

                    startTimeView.setText(entry.getStartTime());

                    endTimeView.setText(entry.getEndTime());

                    vagNumberView.setText(entry.getVagNumber());

                    courseNumberView.setText(HtmlCompat.fromHtml(ConvertUmlaut.toHtml(entry.getCourseNumber()), 0));

                    loctionView.setText(HtmlCompat.fromHtml(ConvertUmlaut.toHtml(entry.getLocation()), 0));

                    typeView.setText(entry.getType());

                    holidayView.setText(entry.getHoliday());

                    break;
                }
            }
        } else {
            dayOfWeekView.setText("WE");
        }
    }

    /**
     * Select a job schedule file via the file dialog tool.
     */
    private void openFileDialog() {
        Intent i = new Intent(MainActivity.this, FileDialog.class);
        i.putExtra(FileDialog.MY_TASK_FOR_TODAY_IS, FileDialog.GET_FILE_NAME_AND_PATH);
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
