package com.berthold.convertjobscheduletocalendar;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import CalendarMaker.CalendarEntry;
import CalendarMaker.MakeCalendar;

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
     * @return The curent job schedule list, filtered as specified in the current filter settings.
     */
    public List<CalendarEntry> getJobScheduleListData() {
        return jobScheduleListData;
    }

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

    // Filter settings
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

    public String getCurrentVAGNumberDisplayed() {
        if (currentVAGNumberDisplayed==null)
            return "*";
        else
            return currentVAGNumberDisplayed;
    }
}
