package com.berthold.convertjobscheduletocalendar;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import CalendarMaker.CalendarEntry;
import CalendarMaker.MakeCalendar;

/**
 * Main activity's view model.
 */
public class MainActivityViewModel extends ViewModel {

    // Data
    private int currentJobScheduleItemsIndex;
    private MakeCalendar mycalendar;
    private List<CalendarEntry> jobScheduleListData = new ArrayList<>();

    // Filter settings
    private boolean isShowOnlyFutureEvents = false;
    private boolean showAllEvents = true;
    private boolean showValid = false;
    private boolean showInvalid = false;
    private String currentCourseNumberDisplayed, currentVAGNumberDisplayed;
    private String currentSearchQuery = "";

    // --------------------------------------------- Live data ------------------------------------------------------------------
    /**
     * Calendars revision date.
     */
    public MutableLiveData<String> calendarsRevisionDateAndTime;

    public MutableLiveData<String> calendarWasupdated() {
        if (calendarsRevisionDateAndTime == null)
            calendarsRevisionDateAndTime = new MutableLiveData<>();
        return calendarsRevisionDateAndTime;
    }
    // ----------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes the current job schedule.
     * Take care to invoke this, when a new jobschedule has been loaded
     * from the filesystem.
     *
     * @param mycalendar
     */
    public void setMycalendar(MakeCalendar mycalendar) {
        this.mycalendar = mycalendar;
    }

    /**
     * @return The current job schedule.
     */
    public MakeCalendar getMyCalendar() {
        return mycalendar;
    }

    /**
     * Sets the current entry in the job schedule list.
     * Take care to invoke this every time when an entry was
     * picked from the job schedule list view.
     *
     * @param currentEntry
     */
    public void setCurrentJobScheduleListItemsIndex(int currentEntry) {
        this.currentJobScheduleItemsIndex = currentEntry;
    }

    /**
     * @return The index of the current entry (e.g. selected job schedule list item).
     */
    public int getCurrentJobScheduleListItemsIndex() {
        return currentJobScheduleItemsIndex;
    }

    public void setJobScheduleListData(List<CalendarEntry> jobScheduleListData) {
        this.jobScheduleListData = jobScheduleListData;
    }

    /**
     * @return The current job schedule list, filtered as specified in the current filter settings.
     */
    public List<CalendarEntry> getJobScheduleListData() {
        return jobScheduleListData;
    }

    /**
     * Returns a list of all courses in this calendar and their associated
     * VAG-numbers.
     *
     * @param courseNumber If a course number is passed, return only this course.
     * @return List of course numbers and their associated VAG- numbers.
     */
    public List<String> getAllVAGNumbers(String courseNumber) {
        return mycalendar.getCourseList();
    }

    //
    //
    //------------------- The following methods are called sequentially in order to filter the list according to the users settings ------------------------------
    //
    //

    /**
     * Read and parse a job schedule file.
     * <p>
     * Gets either all or only future events.
     *
     * @param pathToCurrentCalendarFile
     * @return True is the file could be read, false if there was an i/o error...
     */
    public boolean readAndParseJobSchedule(InputStream pathToCurrentCalendarFile, String search) {

        getJobScheduleListData().clear();

        List<CalendarEntry> rawCalendar = new ArrayList();
        setMycalendar(new MakeCalendar(pathToCurrentCalendarFile));

        // Get unsorted, unfiltered calendar
        rawCalendar = getMyCalendar().getRawCalendar();
        //getAndShowTodaysEvent(rawCalendar);

        long currentTimeInMillisec = System.currentTimeMillis();

        if (getMyCalendar().hasError()) {
            String error = getMyCalendar().getErorrDescription();
            // todo Display error message
            return false;
        } else {

            for (CalendarEntry calendarEntry : rawCalendar) {
                Long currentEventTimeInMillisec = calendarEntry.getEventTimeInMillisec();

                if (isShowOnlyFutureEvents) {
                    if (currentEventTimeInMillisec >= currentTimeInMillisec)
                        addEvent(calendarEntry, search);
                } else
                    addEvent(calendarEntry, search);
            }
        }

        //
        // Publish revision date and time via the associated observer
        //
        String revisionDate = getMyCalendar().getCalendarRevisionDate();
        String revisionTime = getMyCalendar().getCalendarRevisionTime();
        String revisionDateAndTime = revisionDate + " // " + revisionTime;
        calendarsRevisionDateAndTime.postValue(revisionDateAndTime);

        return true;
    }

    /**
     * Checks calendar entries according to the users filter settings.
     *
     * @param calendarEntry Inside the view model: jobScheduleListData  Holding calendar entries currently visible to the user.
     */
    private void addEvent(CalendarEntry calendarEntry, String search) {

        if (getShowAllEvents()) {
            publishEvent(calendarEntry, search);
        }

        if (getShowValid()) {
            if (calendarEntry.isValidEntry)
                publishEvent(calendarEntry, search);
        }

        if (getShowInvalid()) {
            if (!calendarEntry.isValidEntry)
                publishEvent(calendarEntry, search);
        }
    }

    /**
     * Takes the already filtered entry and checks if the users search criteria
     * apply and if so, finally adds them to the list of events to be displayed.
     *
     * @param calendarEntry
     * @param search
     */
    public void publishEvent(CalendarEntry calendarEntry, String search) {

        if (search.isEmpty())
            getJobScheduleListData().add(calendarEntry);
        else {
            Pattern p = Pattern
                    .compile("(?i)(" + search + ")");
            Matcher s = p.matcher(calendarEntry.getOrgiriginalEntry());

            if (s.find()) {
                getJobScheduleListData().add(calendarEntry);
            }
        }
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Returns the version from the app's Google Play store listing...
     *
     * @param c
     * @return A String containing the version tag.
     */
    public String getAppVersionfromGooglePlay(Context c) {
        String latest;
        VersionChecker vc = new VersionChecker();

        try {
            latest = vc.execute().get();
        } catch (Exception e) {
            latest = "-";
        }
        return latest;
    }

    public String getCurrentVAGNumberDisplayed() {
        if (currentVAGNumberDisplayed == null)
            return "*";
        else
            return currentVAGNumberDisplayed;
    }

    /**
     * Get search query.
     *
     * @return The search query currently entered inside the search view inside the action bar.
     */
    public String getCurrentSearchQuery() {
        return currentSearchQuery;
    }

    /**
     * Set search query.
     *
     * @param currentSearchQuery The current search query entered inside the search view inside of the action bar.
     */
    public void setCurrentSearchQuery(String currentSearchQuery) {
        this.currentSearchQuery = currentSearchQuery;
    }

    //
    // Filter settings
    //
    public void setShowOnlyFutureEvents(boolean state) {
        isShowOnlyFutureEvents = state;
    }

    public boolean getIsShowOnlyFutureEvents() {
        return isShowOnlyFutureEvents;
    }

    public void setShowAllEvents(boolean state) {
        showAllEvents = state;
    }

    public boolean getShowAllEvents() {
        return showAllEvents;
    }

    public void setShowValid(boolean state) {
        showValid = state;
    }

    public boolean getShowValid() {
        return showValid;
    }

    public void setShowInvalid(boolean state) {
        showInvalid = state;
    }

    public boolean getShowInvalid() {
        return showInvalid;
    }

    public void setCurrentCourseNumberDisplayed(String c) {
        currentCourseNumberDisplayed = c;
    }

    public String getCurrentCourseNumberDisplayed() {
        return currentCourseNumberDisplayed;
    }

    public void setCurrentVAGNumberDisplayed(String v) {
        currentVAGNumberDisplayed = v;
    }


}
