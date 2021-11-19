package com.berthold.convertjobscheduletocalendar;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import CalendarMaker.*;
import berthold.filedialogtool.FileDialog;

/**
 * Reads a text file, checks for valid calendar entries and writes them to the devices calendar.
 * <p>
 * Signed: With "GoogleKeyStore"- key... (opted in to Signing by Google)....
 *
 * @author Berthold Fritz
 */
public class MainActivity extends AppCompatActivity implements JobScheduleListAdapter.receieve, FragmentDateDetailView.DateDetailView, FragmentYesNoDialog.getDataFromFragment {

    // Shared prefs
    SharedPreferences sharedPreferences;

    //--------- For API levels <30 --------------------------------------------------
    // File system
    //public static File workingDir;
    //public String appDir = "/Meine_Einsatzpläne";       // App's working dir..
    private static final int ID_FILE_DIALOG = 1;
    private static final boolean OVERRIDE_LAST_PATH_VISITED = false;
    //--------- -----------------------------------------------------------------------

    // Permission request callback
    private static final int PERMISSION_REQUEST = 200;

    // Activitys
    ActivityResultLauncher loadFileActivityResult;

    // Calendar list
    private RecyclerView jobScheduleListRecyclerView;
    private RecyclerView.Adapter jobScheduleListAdapter;
    private RecyclerView.LayoutManager jobScheduleListLayoutManager;

    // UI
    private MainActivityViewModel mainActivityViewModel;
    RadioGroup radioGroupViewFilters;
    RadioButton selectAllView;
    RadioButton selectValidView;
    RadioButton selectInvalidView;
    CheckBox showOnlyFutureEventsView;
    TextView dayOfWeekView;
    TextView dateView;
    TextView startTimeView;
    TextView endTimeView;
    TextView vagNumberView;
    TextView courseNumberView;
    TextView loctionView;
    TextView typeView;
    TextView holidayView;
    SearchView searchView;

    // Misc
    private Context context;

    // Custom confirm dialog
    private static final int CONFIRM_DIALOG_CALLS_BACK_FOR_PERMISSIONS = 1;
    private static final int CONFIRM_DIALOG_CALLS_BACK_FOR_UPDATE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();

        /*--------------------- When target SDK is below 30 (Android 10 or less) ----------------------
        // File system
        //
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            // @rem:Filesystem, creates public folder in the devices externalStorage dir...@@
            //
            // This seems to be the best practice. It creates a public folder.
            // This folder will not be deleted when the app is de- installed
            workingDir = Environment.getExternalStoragePublicDirectory(appDir); // Has access restrictions..

            else

            // For API levels > 29 this gets the apps "private" dir.
            // It is not able to access root or the rest of the file tree.
            // Other apps can not access this dir.
            // When the app is de installed, the contents of this dir are also deleted.

            workingDir = getExternalFilesDir(appDir);

        Log.v("WORKINGDIR",workingDir.getPath());
        workingDir.mkdirs(); // Create dir, if it does not already exist
        -----------------------------------------------------------------------------------------------------*/

        // UI
        radioGroupViewFilters = findViewById(R.id.radioGroupViewFilter);
        selectAllView = findViewById(R.id.select_all);
        selectValidView = findViewById(R.id.select_valid);
        selectInvalidView = findViewById(R.id.select_invalid);
        showOnlyFutureEventsView = findViewById(R.id.show_only_future_events);
        dayOfWeekView = findViewById(R.id.day_of_week);
        dateView = findViewById(R.id.date);
        startTimeView = findViewById(R.id.start_time);
        endTimeView = findViewById(R.id.end_time);
        vagNumberView = findViewById(R.id.vag_number);
        courseNumberView = findViewById(R.id.course_number);
        loctionView = findViewById(R.id.location);
        typeView = findViewById(R.id.type);
        holidayView = findViewById(R.id.holiday_remark);

        // ViewModel
        mainActivityViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        // Filter settings
        selectAllView.setChecked(mainActivityViewModel.getShowAllEvents());

        // Calendar list
        jobScheduleListRecyclerView = (RecyclerView) findViewById(R.id.job_schedule_list);
        jobScheduleListRecyclerView.setHasFixedSize(true);
        jobScheduleListLayoutManager = new LinearLayoutManager(this);
        jobScheduleListRecyclerView.setLayoutManager(jobScheduleListLayoutManager);

        jobScheduleListAdapter = new JobScheduleListAdapter(mainActivityViewModel.getJobScheduleListData(), this, context);
        jobScheduleListRecyclerView.setAdapter(jobScheduleListAdapter);

        // From here on the app checks if it has all required permissions and
        // if there are updates available.
        //
        // If permissions dialog is shown, wee need to notify the routine
        // which checks for updates, not to show the update info dialog...
        //Boolean permissionDialogIsNotShown = true;

        //
        // Callback for file Picker if using Android 11 or higher....
        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            loadFileActivityResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Log.v("RETURN_DATA_", result.getData().toString());

                        // The result contains an uri which can be used to perform operations on the
                        // document the user selected.
                        //
                        Uri uri;
                        uri = result.getData().getData();
                        readAndParseJobSchedule(getCalendarFilesInputStream(uri), mainActivityViewModel.getCurrentSearchQuery());
                        savePathToCurrentCalendarFileToSp(uri);
                    }
                }
            });
        }

        // Was a calendar file opened previously?
        //
        // If so, proceed, if not, open the file dialog tool for the user allowing
        // him to pick a suitable file. If permissions are ot granted yet, ask the user
        // to do so.
        if (!readAndParseJobSchedule(getCalendarFilesInputStream(restorePathOfCurrentCalendarFileFromSp()), mainActivityViewModel.getCurrentSearchQuery())) {

            //
            // Check for permissions.
            // The permissions to be checked have to be defined inside the manifest file.
            //
            String[] perms = {"android.permission.WRITE_CALENDAR", "android.permission.READ_EXTERNAL_STORAGE"};
            int permsRequestCode = PERMISSION_REQUEST;

            // Opens a system dialog requesting permissions, if none of the
            // permissions asked for were granted already....
            requestPermissions(perms, permsRequestCode);

            openFileDialog();

            /*
            // OLD, not very clean solution. Kept it here to show how not to do it!
            //
            // Permissions to access internal filesystem ('/SDCard')?
            //
            if (permissionIsDenied("ACTION_READ_INTERNAL_STORAGE")) {
                permissionDialogIsNotShown = false;
                String dialogText = getResources().getString(R.string.ask_for_device_permissions_file_system);
                String ok = getResources().getString(R.string.PERM_OK_Button);
                String cancel = getResources().getString(R.string.PERM_CANCEL_BUTTON);
                showConfirmDialog(CONFIRM_DIALOG_CALLS_BACK_FOR_PERMISSIONS, FragmentYesNoDialog.SHOW_AS_YES_NO_DIALOG, dialogText.toString(), ok, cancel);
            } else {
                openFileDialog();
            }
            */
        }

        //
        // Check if there is a newer version of this app available at the play store.
        //
        //if (showUpdateInfo() && permissionDialogIsNotShown) {
        if (showUpdateInfo()) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(10000);
                    } catch (Exception e) {
                    }

                    String currentVersion = GetThisAppsVersion.thisVersion(getApplicationContext());
                    String latestVersionInGooglePlay = mainActivityViewModel.getAppVersionfromGooglePlay(getApplicationContext());

                    if (!latestVersionInGooglePlay.equals(currentVersion)) {
                        saveTimeUpdateInfoLastOpened();
                        String dialogText = getResources().getString(R.string.dialog_new_version_available) + " " + latestVersionInGooglePlay;
                        String ok = getResources().getString(R.string.do_update_confirm_button);
                        String cancel = getResources().getString(R.string.no_udate_button);
                        showConfirmDialog(CONFIRM_DIALOG_CALLS_BACK_FOR_UPDATE, FragmentYesNoDialog.SHOW_AS_YES_NO_DIALOG, dialogText.toString(), ok, cancel);
                    }
                }
            });
            t.start();
        }

        //
        // Init filter settings
        //
        radioGroupViewFilters.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mainActivityViewModel.setShowAllEvents(false);
                mainActivityViewModel.setShowValid(false);
                mainActivityViewModel.setShowInvalid(false);
                switch (checkedId) {
                    case R.id.select_all:
                        mainActivityViewModel.setShowAllEvents(true);
                        break;

                    case R.id.select_invalid:
                        mainActivityViewModel.setShowInvalid(true);
                        break;

                    case R.id.select_valid:
                        mainActivityViewModel.setShowValid(true);
                        break;
                }
                readAndParseJobSchedule(getCalendarFilesInputStream(restorePathOfCurrentCalendarFileFromSp()), mainActivityViewModel.getCurrentSearchQuery());
                jobScheduleListAdapter.notifyDataSetChanged();
            }
        });

        //
        // Show only future events?
        //
        showOnlyFutureEventsView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mainActivityViewModel.setShowOnlyFutureEvents(isChecked);
                readAndParseJobSchedule(getCalendarFilesInputStream(restorePathOfCurrentCalendarFileFromSp()), mainActivityViewModel.getCurrentSearchQuery());
                jobScheduleListAdapter.notifyDataSetChanged();
            }
        });

        // Update today's event
        getAndShowTodaysEvent(mainActivityViewModel.getMyCalendar().getRawCalendar());

        // refresh
        readAndParseJobSchedule(getCalendarFilesInputStream(restorePathOfCurrentCalendarFileFromSp()), mainActivityViewModel.getCurrentSearchQuery());

    }

    /**
     * Shows a confirm dialog.
     *
     * @param reqCode
     * @param kindOfDialog
     * @param dialogText
     * @param confirmButton
     * @param cancelButton
     */
    private void showConfirmDialog(int reqCode, int kindOfDialog, String dialogText, String confirmButton, String cancelButton) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentYesNoDialog fragmentDeleteRegex =
                FragmentYesNoDialog.newInstance(reqCode, kindOfDialog, null, dialogText, confirmButton, cancelButton);
        fragmentDeleteRegex.show(fm, "fragment_dialog");
    }

    /**
     * Callback for {@link FragmentYesNoDialog} events.
     *
     * @param reqCode
     * @param dialogTextEntered
     * @param buttonPressed
     */
    @Override
    public void getDialogInput(int reqCode, String dialogTextEntered, String buttonPressed) {

        //
        // Grand permission?
        //
        if (reqCode == CONFIRM_DIALOG_CALLS_BACK_FOR_PERMISSIONS) {

            // This is the old version of the permission checker
            if (buttonPressed.equals(FragmentYesNoDialog.BUTTON_OK_PRESSED)) {

                // @rem:Shows how to open the Android systems settings activity fro this app.
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                //@@
            } else {
                String denied = getResources().getString(R.string.permission_denied);
                Toast.makeText(MainActivity.this, denied, Toast.LENGTH_LONG).show();
            }
        }

        //
        // Update this app?
        //
        if (reqCode == CONFIRM_DIALOG_CALLS_BACK_FOR_UPDATE) {
            if (buttonPressed.equals(FragmentYesNoDialog.BUTTON_OK_PRESSED)) {
                Intent openPlayStoreForUpdate = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.berthold.convertjobscheduletocalendar&hl=de"));
                startActivity(openPlayStoreForUpdate);
            }
        }
    }

    /**
     * Checks if a permission is granted or not.
     *
     * @param permission
     * @return true if the specified permission is granted.
     */
    private boolean permissionIsDenied(String permission) {
        //@rem: Shows how to check permissions
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                == PackageManager.PERMISSION_DENIED)
            return true;
        else
            return false;
        //@@
    }

    /**
     * Some System callbacks...
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------------- ----------

    /**
     * Read and parse a job schedule file.
     *
     * @param pathToCurrentCalendarFile
     * @return True is the file could be read, false if there was an i/o error...
     */
    private boolean readAndParseJobSchedule(InputStream pathToCurrentCalendarFile, String search) {

        mainActivityViewModel.getJobScheduleListData().clear();
        jobScheduleListAdapter.notifyDataSetChanged();

        List<CalendarEntry> rawCalendar = new ArrayList();
        mainActivityViewModel.setMycalendar(new MakeCalendar(pathToCurrentCalendarFile));

        // Get unsorted, unfiltered calendar
        rawCalendar = mainActivityViewModel.getMyCalendar().getRawCalendar();
        getAndShowTodaysEvent(rawCalendar);

        long currentTimeInMillisec = System.currentTimeMillis();

        if (mainActivityViewModel.getMyCalendar().hasError()) {
            String error = mainActivityViewModel.getMyCalendar().getErorrDescription();
            Log.v("ERROR__", error);
            return false;
        } else {

            String vagNumber = mainActivityViewModel.getCurrentVAGNumberDisplayed();

            for (CalendarEntry calendarEntry : rawCalendar) {

                Long currentEventTimeInMillisec = calendarEntry.getEventTimeInMillisec();

                if (vagNumber.equals("*")) {
                    if (showOnlyFutureEventsView.isChecked()) {
                        if (currentEventTimeInMillisec >= currentTimeInMillisec)
                            addEvent(calendarEntry, search);
                    } else
                        addEvent(calendarEntry, search);

                } else if (calendarEntry.getVagNumber().equals(vagNumber)) {
                    if (showOnlyFutureEventsView.isChecked()) {
                        if (currentEventTimeInMillisec >= currentTimeInMillisec)
                            addEvent(calendarEntry, search);
                    } else
                        addEvent(calendarEntry, search);
                }
            }
        }
        jobScheduleListAdapter.notifyDataSetChanged();

        String revisionDate = mainActivityViewModel.getMyCalendar().getCalendarRevisionDate();
        String revisionTime = mainActivityViewModel.getMyCalendar().getCalendarRevisionTime();
        getSupportActionBar().setSubtitle(revisionDate + "//" + revisionTime);

        return true;
    }

    /**
     * Add a calendar entry to the calendar according
     * to view options selected by the user
     *
     * @param calendarEntry Inside the view model: jobScheduleListData  Holding calendar entries currently visible to the user.
     */
    private void addEvent(CalendarEntry calendarEntry, String search) {

        if (mainActivityViewModel.getShowAllEvents()) {
            publishEvent(calendarEntry, search);
        }

        if (mainActivityViewModel.getShowValid()) {
            if (calendarEntry.isValidEntry)
                publishEvent(calendarEntry, search);
        }

        if (mainActivityViewModel.getShowInvalid()) {
            if (!calendarEntry.isValidEntry)
                publishEvent(calendarEntry, search);
        }

    }

    /**
     * Takes the already filtered entry and checks if the users search criteria
     * apply....
     *
     * @param calendarEntry
     * @param search
     */
    public void publishEvent(CalendarEntry calendarEntry, String search) {
        if (search.isEmpty())
            mainActivityViewModel.getJobScheduleListData().add(calendarEntry);
        else {
            //
            // from here we check if the query given matches any of the fields of the calendar
            // and if so, all matching entrys are added..
            //
            if (mainActivityViewModel.getMyCalendar().checkIfRegexPatternMatches(MakeCalendar.locationPattern, search)) {
                if (calendarEntry.getLocation().equals(search))
                    mainActivityViewModel.getJobScheduleListData().add(calendarEntry);
            }
            if (mainActivityViewModel.getMyCalendar().checkIfRegexPatternMatches(MakeCalendar.vagNumberPattern, search)) {
                if (calendarEntry.getVagNumber().equals(search))
                    mainActivityViewModel.getJobScheduleListData().add(calendarEntry);
            }
            if (mainActivityViewModel.getMyCalendar().checkIfRegexPatternMatches(MakeCalendar.courseNumberPattern, search)) {
                if (calendarEntry.getCourseNumber().equals(search))
                    mainActivityViewModel.getJobScheduleListData().add(calendarEntry);
            }
            if (mainActivityViewModel.getMyCalendar().checkIfRegexPatternMatches(MakeCalendar.typePattern, search)) {
                if (calendarEntry.getType().equals(search))
                    mainActivityViewModel.getJobScheduleListData().add(calendarEntry);
            }
            if (mainActivityViewModel.getMyCalendar().checkIfRegexPatternMatches(MakeCalendar.datePattern, search)) {
                if (calendarEntry.getDate().equals(search))
                    mainActivityViewModel.getJobScheduleListData().add(calendarEntry);
            }
        }
    }
    //------------------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Callback invoked when the permissions granted dialog was closed.
     *
     * @param permsRequestCode
     * @param permissions
     * @param grantResults
     */
    @Override

    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(permsRequestCode, permissions, grantResults);
        switch (permsRequestCode) {

            case PERMISSION_REQUEST:

                boolean calendarAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean fileSystemAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                //
                // Insert actions to be performed, right after the permission was granted....
                if (calendarAccepted)

                    if (fileSystemAccepted)

                        break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //savePathToCurrentCalendarFileToSp(pathToCurrentCalendarFile);
    }

    @Override
    public void onBackPressed() {
        //savePathToCurrentCalendarFileToSp(pathToCurrentCalendarFile);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);

        searchView=(SearchView) menu.findItem(R.id.search_field).getActionView();

        // @rem:Shows how to create a search view and how to use it
        // The search view...
        //
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                readAndParseJobSchedule(getCalendarFilesInputStream(restorePathOfCurrentCalendarFileFromSp()), query);
                mainActivityViewModel.setCurrentSearchQuery(query);
                return false;
            }

            //
            // This is invoked whenever the "search" button on the keyboard was pressed.
            //
            @Override
            public boolean onQueryTextChange(String sq) {

                return false;
            }
        });
        //@@

        //
        // When the search view's close button was pressed....
        //
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.setQuery("", true);
                readAndParseJobSchedule(getCalendarFilesInputStream(restorePathOfCurrentCalendarFileFromSp()), "");
                mainActivityViewModel.setCurrentSearchQuery("");
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.load) {
            //savePathToCurrentCalendarFileToSp(pathToCurrentCalendarFile);
            /*
            if (permissionIsDenied("READ_EXTERNAL_STORAGE")) {
                // Todo Even when permissions are allowed, this block is execuded, why. Seems this only works in "on Create"
            } else
            */

            openFileDialog();
            return true;
        }
        if (id == R.id.info) {
            //savePathToCurrentCalendarFileToSp(pathToCurrentCalendarFile);
            Intent i = new Intent(MainActivity.this, InfoActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This adds the selected calendar entry to the devices calendar app
     *
     * @param position
     */
    @Override
    public void addThisEntryToCalendar(int position) {

        // This code creates an event and will open the calendar app's save dialog.
        // Doing so is quite good practice and a suitable method if
        // one wants to add only one event....
        //
        // Source: {@link https://stackoverflow.com/questions/4373074/how-to-launch-android-calendar-application-using-intent-froyo}
        int year = mainActivityViewModel.getJobScheduleListData().get(position).getYear() + 2000;
        int month = mainActivityViewModel.getJobScheduleListData().get(position).getMonth();
        int day = mainActivityViewModel.getJobScheduleListData().get(position).getDay();
        int startH = mainActivityViewModel.getJobScheduleListData().get(position).getStartTimeHours();
        int startM = mainActivityViewModel.getJobScheduleListData().get(position).getStartTimeMinutes();
        int endH = mainActivityViewModel.getJobScheduleListData().get(position).getEndTimeHours();
        int endM = mainActivityViewModel.getJobScheduleListData().get(position).getEndTimeMinutes();

        if (endH == 0 && endM == 0) {
            endH = 23;
            endM = 59;
        }

        String courseNumber = mainActivityViewModel.getJobScheduleListData().get(position).getCourseNumber();
        String location = mainActivityViewModel.getJobScheduleListData().get(position).getLocation();
        String vag = mainActivityViewModel.getJobScheduleListData().get(position).getVagNumber();
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
    public void mailInquiryForThisEntry(int position) {

        String courseNumber = mainActivityViewModel.getJobScheduleListData().get(position).getCourseNumber();
        String vagNumber = mainActivityViewModel.getJobScheduleListData().get(position).getVagNumber();
        String location = mainActivityViewModel.getJobScheduleListData().get(position).getLocation();
        String message = mainActivityViewModel.getJobScheduleListData().get(position).getOrgiriginalEntry();
        String date = mainActivityViewModel.getJobScheduleListData().get(position).getDate();

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
     * Add's a whole course (based on it's VAG- number) to the
     * devices calendar app.
     *
     * @param position
     */
    @Override
    public void addThisCourseToCalendar(int position) {

        String vagNumber = mainActivityViewModel.getJobScheduleListData().get(position).getCourseNumber();

        // Get the current course
        List<CalendarEntry> thisCourseByVAGNumber = new ArrayList<CalendarEntry>();
        thisCourseByVAGNumber = mainActivityViewModel.getMyCalendar().getCalenderEntrysMatchingVAG(mainActivityViewModel.getJobScheduleListData().get(position).getVagNumber());
        int numberOfEntriesFound = thisCourseByVAGNumber.size() - 1;
        int numberOfDaysRunning = thisCourseByVAGNumber.size();

        // Get first day of course
        int yearStart = thisCourseByVAGNumber.get(0).getYear() + 2000;
        int monthStart = thisCourseByVAGNumber.get(0).getMonth();
        int dayStart = thisCourseByVAGNumber.get(0).getDay();
        int startH = thisCourseByVAGNumber.get(0).getStartTimeHours();
        int startM = thisCourseByVAGNumber.get(0).getStartTimeMinutes();

        if (startH == 0 && startM == 0) {
            startH = 23;
            startM = 59;
        }

        Calendar beginTime = Calendar.getInstance();
        beginTime.set(yearStart, monthStart - 1, dayStart, startH, startM);
        Long startMillis = beginTime.getTimeInMillis();

        // Get last day of course
        int yearEnd = thisCourseByVAGNumber.get(numberOfEntriesFound).getYear() + 2000;
        int monthEnd = thisCourseByVAGNumber.get(numberOfEntriesFound).getMonth();
        int dayEnd = thisCourseByVAGNumber.get(numberOfEntriesFound).getDay();
        int endH = thisCourseByVAGNumber.get(numberOfEntriesFound).getEndTimeHours();
        int endM = thisCourseByVAGNumber.get(numberOfEntriesFound).getEndTimeMinutes();

        if (endH == 0 && endM == 0) {
            endH = 23;
            endM = 59;
        }

        Calendar endTime = Calendar.getInstance();
        endTime.set(yearEnd, monthEnd - 1, dayEnd, endH, endM);
        Long endMillis = endTime.getTimeInMillis();

        // Get other data
        String location = mainActivityViewModel.getJobScheduleListData().get(position).getLocation();
        String vag = mainActivityViewModel.getJobScheduleListData().get(position).getVagNumber();
        String description = vagNumber + "  VAG:" + vag + " Dauer " + numberOfDaysRunning + " Tage";

        // Create calendar entry, start devices calendar app
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", startMillis);
        intent.putExtra("endTime", endMillis);
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);


        intent.putExtra("title", description);
        startActivity(intent);
    }

    /**
     * Starts the devices e- mail app to start an inquiry for
     * a whole course.
     *
     * @param position
     */
    @Override
    public void mailInquiryForThisCourse(int position) {

        String vagNumber = mainActivityViewModel.getJobScheduleListData().get(position).getVagNumber();
        String courseNumber = mainActivityViewModel.getJobScheduleListData().get(position).getCourseNumber();
        String originalEntry = mainActivityViewModel.getJobScheduleListData().get(position).getOrgiriginalEntry();

        // Get the current course
        List<CalendarEntry> thisCourseByVAGNumber = new ArrayList<CalendarEntry>();
        thisCourseByVAGNumber = mainActivityViewModel.getMyCalendar().getCalenderEntrysMatchingVAG(mainActivityViewModel.getJobScheduleListData().get(position).getVagNumber());
        int numberOfEntriesFound = thisCourseByVAGNumber.size() - 1;
        int numberOfDaysRunning = thisCourseByVAGNumber.size();

        String startDate = thisCourseByVAGNumber.get(0).getDate();
        String startTime = thisCourseByVAGNumber.get(0).getStartTime();
        String startLocation = thisCourseByVAGNumber.get(0).getLocation();

        String endDate = thisCourseByVAGNumber.get(numberOfEntriesFound).getDate();
        String endTime = thisCourseByVAGNumber.get(numberOfEntriesFound).getEndTime();

        String subject = "Anfrage zu VAG:" + vagNumber + " Kurs:" + courseNumber;
        String message = "Beginnt am " + startDate + "//" + startTime + " Uhr und endet am " + endDate + " um " + endTime + " (Dauer " + numberOfDaysRunning + " Tage) Ort am ersten Tag:" + startLocation + " ORIGINAL:" + originalEntry;

        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, "");
        email.putExtra(Intent.EXTRA_SUBJECT, subject);
        email.putExtra(Intent.EXTRA_TEXT, message);

        //need this to prompts email client only
        email.setType("message/rfc822");

        startActivity(Intent.createChooser(email, "Choose an Email client :"));
    }

    /**
     * ToDo Gets all calendar entries matching a given vag number.
     *
     * @param vagNumber
     */
    public void setFilterVagNumber(String vagNumber) {
        List<CalendarEntry> result = mainActivityViewModel.getMyCalendar().getCalenderEntrysMatchingVAG(vagNumber);
    }

    /**
     * Shows a detailed view of the selected entry
     *
     * @param position
     */
    @Override
    public void showEntryDetailView(int position) {
        mainActivityViewModel.setCurrentJobScheduleListItemsIndex(position);

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();

        ft.setReorderingAllowed(true);
        ft.replace(R.id.fragment_container, new FragmentDateDetailView(), "Fragment_1");
        ft.commitAllowingStateLoss();
    }

    /**
     * Hides the fragment invoking this callback via it's associated interface
     */
    @Override
    public void hideFragmentCurrentlyShown() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //@rem Shows how to search a fragment by it's tag and how to hide it@@
        FragmentDateDetailView dateDetailView_1 = (FragmentDateDetailView) fragmentManager.findFragmentByTag("Fragment_1");
        //@@
        fragmentTransaction.hide(dateDetailView_1);
        fragmentTransaction.commit();
    }

    /**
     * Get and show today's event permanently.
     * <p>
     * Updates the associated fragment.
     */
    private void getAndShowTodaysEvent(List<CalendarEntry> rawCalendar) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.setReorderingAllowed(true);
        ft.replace(R.id.fragment_today_view, new FragemtTodayView(), "Fragment_2");
        ft.commitAllowingStateLoss();
    }

    /**
     * Select a job schedule file via the file dialog tool.
     */
    private void openFileDialog() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            //
            // For Android vesions < 11
            //
            Intent i = new Intent(MainActivity.this, FileDialog.class);
            i.putExtra(FileDialog.MY_TASK_FOR_TODAY_IS, FileDialog.GET_FILE_NAME_AND_PATH);
            i.putExtra(FileDialog.OVERRIDE_LAST_PATH_VISITED, OVERRIDE_LAST_PATH_VISITED);
            startActivityForResult(i, ID_FILE_DIALOG);
        } else {
            //
            // For Android versions =>11
            //
            // This will open the devices file picker and lets
            // one pick a file....
            //
            Intent pickFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pickFileIntent.addCategory(Intent.CATEGORY_OPENABLE);
            pickFileIntent.setType("text/*");
            loadFileActivityResult.launch(pickFileIntent);
        }
    }

    /**
     * File Dialog Tool callback.
     * For SDK < Android 11 (HONEYCOMP)
     *
     * @param reqCode Code which idendifies the fileDialogTool as the returning activity
     * @param resCode OK, error, etc...
     * @param data    Selected path.
     */
    /*
    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);

        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.HONEYCOMB) {
            if (resCode == RESULT_OK && reqCode == ID_FILE_DIALOG) {
                if (data.hasExtra("path")) {

                    String returnStatus = data.getExtras().getString(FileDialog.RETURN_STATUS);
                    String pathSelected = data.getExtras().getString("path");

                    Log.v("PATH_", pathSelected);

                    if (returnStatus.equals(FileDialog.FOLDER_AND_FILE_PICKED)) {
                        pathToCurrentCalendarFile = pathSelected;
                        //readAndParseJobSchedule(pathToCurrentCalendarFile);
                    }
                }
            }
        }
    }
    */

    /**
     * Saves timestamp when update info was shown and
     * a boolean set to true which tells us that the
     * info has been shown at least once.
     */
    private void saveTimeUpdateInfoLastOpened() {
        Long currentTime = System.currentTimeMillis();
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("lastUpdateInfo", currentTime);
        editor.putBoolean("hasBeenShownAtLastOnce", true);
        editor.commit();
    }

    /**
     * Checks if update info is allowed to be shown again.
     *
     * @return true if allowed, false if not.
     */
    private boolean showUpdateInfo() {
        sharedPreferences = getPreferences(MODE_PRIVATE);

        // If the update info has not been shown at least once, then
        // show it now, for the first time and as a result, also save the
        // time last shown for the first time.
        Boolean hasBeenShowedAtLeastOnce = sharedPreferences.getBoolean("hasBeenShownAtLastOnce", false);
        if (!hasBeenShowedAtLeastOnce)
            return true;    // Don't show

        // Update info has been shown at least for one time. So check
        // when this was and if enough time has passed to allow it
        // to be shown again....
        Long currentTime = System.currentTimeMillis();

        // Time in Millisec. which has to be passed until update info is
        // allowed to be shown again since the  last time it
        // appeared on the screen;
        int timeDiffUntilNextUpdateInfo = 8 * 60 * 60 * 1000; // Show once every eight hours.....

        Long lastTimeOpened = sharedPreferences.getLong("lastUpdateInfo", currentTime);

        Log.v("TIMETIME", " Last:" + lastTimeOpened + "   current:" + currentTime + "  Diff:" + (currentTime - lastTimeOpened));

        if ((currentTime - lastTimeOpened) > timeDiffUntilNextUpdateInfo)
            return true;    // Show
        else
            return false;   // Don't show
    }

    /**
     * Retrieves the input stream associatedwith the calendar file.
     *
     * @param uri
     * @return Input stream for the associated calendar file.
     */
    private InputStream getCalendarFilesInputStream(Uri uri) {
        InputStream inputStream;
        try {
            inputStream = getContentResolver().openInputStream(uri);
        } catch (Exception e) {
            inputStream = null;
        }
        return inputStream;
    }

    /**
     * Save path to current calendar file shared preferences.
     */
    private void savePathToCurrentCalendarFileToSp(Uri pathToCurrentCalendarFile) {
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("pathToCurrentCalendarFile", pathToCurrentCalendarFile.toString());
        editor.commit();
    }

    /**
     * Restore path to current calendar file from shared pref's..
     */
    private Uri restorePathOfCurrentCalendarFileFromSp() {
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        String path = sharedPreferences.getString("pathToCurrentCalendarFile", "-");
        return Uri.parse(path);
    }
}