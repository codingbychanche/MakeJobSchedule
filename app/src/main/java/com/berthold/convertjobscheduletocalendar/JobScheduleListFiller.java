package com.berthold.convertjobscheduletocalendar;

import android.net.Uri;
import android.os.AsyncTask;
import android.widget.CheckBox;
import android.widget.RadioButton;

import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import CalendarMaker.CalendarEntry;
import CalendarMaker.MakeCalendar;

/**
 * Async- Task which reads and parses the job schedule selected by the user
 * from the file system.
 */
public class JobScheduleListFiller extends AsyncTask<String, CalendarEntry, String> {

    // File system
    private InputStream pathToCurrentCalendarFile;

    // Calendar list
    private RecyclerView.Adapter jobScheduleListAdapter;
    private List<CalendarEntry> jobScheduleListData = new ArrayList<>();

    // UI
    RadioButton selectAllView;
    RadioButton selectValidView;
    RadioButton selectInvalidView;
    CheckBox showOnlyFutureEventsView;


    /**
     * Creates a new job schedule.
     *
     * @param pathToCurrentCalendarFile
     * @param jobScheduleListData
     * @param jobScheduleListAdapter
     * @param selectAllView
     * @param selectInvalidView
     * @param selectValidView
     * @param showOnlyFutureEventsView
     * @throws InterruptedException
     */
    JobScheduleListFiller(InputStream pathToCurrentCalendarFile,
                          List <CalendarEntry>jobScheduleListData,
                          RecyclerView.Adapter jobScheduleListAdapter,
                          RadioButton selectAllView,
                          RadioButton selectInvalidView,
                          RadioButton selectValidView,
                          CheckBox showOnlyFutureEventsView) {

        this.pathToCurrentCalendarFile=pathToCurrentCalendarFile;
        this.jobScheduleListData=jobScheduleListData;
        this.jobScheduleListAdapter=jobScheduleListAdapter;
        this.selectAllView=selectAllView;
        this.selectInvalidView=selectInvalidView;
        this.selectValidView=selectValidView;
        this.showOnlyFutureEventsView=showOnlyFutureEventsView;
    }

    /**
     * Prepare stuff.....
     */
    @Override
    protected void onPreExecute() {

    }

    /**
     * Does all the work in the background
     * Rule! => Never change view elements of the UI- thread from here! Do it in 'onPublish'!
     */
    @Override
    protected String doInBackground(String... params) {

        jobScheduleListData.clear();

        List<CalendarEntry> calendar = new ArrayList();

        MakeCalendar myCalendar = new MakeCalendar(pathToCurrentCalendarFile);

        calendar = myCalendar.getRawCalendar();

        long currentTimeInMillisec = System.currentTimeMillis();

        if (myCalendar.hasError()) {
            // todo Add error message
        } else {
            for (CalendarEntry calendarEntry : calendar) {
                if(isCancelled())
                    break;

                Long currentEventTimeInMillisec = calendarEntry.getEventTimeInMillisec();

                // Add entry just obtained to list and publish
                if (showOnlyFutureEventsView.isChecked()) {
                    if (currentEventTimeInMillisec > currentTimeInMillisec)
                        addEvent(calendarEntry);
                } else
                    addEvent(calendarEntry);
            }

            // Wait a few seconds to allow the UI to react....
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // todo Add error description
            }
        }
        return "Done";
    }

    /**
     * Add a calendar entry to the calendar according
     * to view options selected by the user
     *
     * @global jobScheduleListData  Holding calendar entries currently visible to the user.
     * @param calendarEntry
     */
    private void addEvent(CalendarEntry calendarEntry) {

        if (selectAllView.isChecked()) {
            publishProgress(calendarEntry);
        }

        if (selectValidView.isChecked()) {
            if (calendarEntry.isValidEntry)
                publishProgress(calendarEntry);
        }

        if (selectInvalidView.isChecked()) {
            if (!calendarEntry.isValidEntry)
                publishProgress(calendarEntry);
        }
    }

    /**
     * Update UI- Thread
     *
     * @param calendarEntry
     */
    @Override
    protected void onProgressUpdate(CalendarEntry ... calendarEntry) {
        jobScheduleListData.add(calendarEntry[0]);
        jobScheduleListAdapter.notifyDataSetChanged();
    }

    /**
     * All done..
     *
     * @param result
     */

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
    }
}

