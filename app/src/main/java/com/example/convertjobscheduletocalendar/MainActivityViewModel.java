package com.example.convertjobscheduletocalendar;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import CalendarMaker.CalendarEntry;
import CalendarMaker.MakeCalendar;

public class MainActivityViewModel extends ViewModel {

    // Filter settings
    private boolean isShowOnlyFutureEvents = false;
    private boolean showAllEvents = true;
    private boolean showValid = false;
    private boolean showInvalid = false;

    /**
     * @return The calendar object provided by {@link MakeCalendar}.

    public MakeCalendar getCalendar() {
        return myCalendar;
    }

    /**
     * @return The job schedule list, extracted from
     * a calendar object which was created by {@link MakeCalendar}
     * and which is filled with all entry's according to the current settings.

    public List<CalendarEntry> getJobScheduleListData() {
        return jobScheduleListData;
    }

    public void setJobScheduleListData(List<CalendarEntry> jobScheduleListData) {
        this.jobScheduleListData = jobScheduleListData;
    }
    */
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


}
