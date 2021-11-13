package com.berthold.convertjobscheduletocalendar;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import CalendarMaker.CalendarEntry;
import CalendarMaker.ConvertUmlaut;

/**
 * Fills our job schedule list...
 */
public class JobScheduleListAdapter extends RecyclerView.Adapter<JobScheduleListAdapter.ViewHolder> {

    private List<CalendarEntry> jobScheduleListData = new ArrayList<>();
    private MainActivity mainActivity;
    private Context context;
    private Resources resources;

    int[] dayOfWeek = {R.string.so, R.string.mo, R.string.di, R.string.mi, R.string.don, R.string.fr, R.string.sa};

    public JobScheduleListAdapter(List<CalendarEntry> jobScheduleListData, MainActivity mainActivity, Context context) {
        this.jobScheduleListData = jobScheduleListData;
        this.mainActivity = mainActivity;
        this.context = context;

        resources = context.getResources();
    }

    interface receieve {
        void showEntryDetailView(int position);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;

        public ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public JobScheduleListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.job_schedule_row_view, parent, false);
        JobScheduleListAdapter.ViewHolder vh = new JobScheduleListAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.showEntryDetailView(position);
            }
        });

        TextView dayOfWeekView = holder.mView.findViewById(R.id.day_of_week);
        TextView dateView = holder.mView.findViewById(R.id.date);
        TextView startTimeView = holder.mView.findViewById(R.id.start_time);
        TextView endTimeView = holder.mView.findViewById(R.id.end_time);
        TextView vagNumberView = holder.mView.findViewById(R.id.vag_number);
        TextView courseNumberView = holder.mView.findViewById(R.id.course_number);
        TextView loctionView = holder.mView.findViewById(R.id.location);
        TextView typeView = holder.mView.findViewById(R.id.type);
        TextView holidayView = holder.mView.findViewById(R.id.holiday_remark);

        //
        // Populate views
        //
        int dayNameResource = dayOfWeek[(jobScheduleListData.get(position).getDayOfWeekForThisDate()) - 1];
        if (jobScheduleListData.get(position).isChildOfAnotherEntry)
            dayOfWeekView.setText(" ");
        else
            dayOfWeekView.setText(context.getString(dayNameResource));

        dateView.setText(jobScheduleListData.get(position).getDate());
        startTimeView.setText(jobScheduleListData.get(position).getStartTime());
        endTimeView.setText(jobScheduleListData.get(position).getEndTime());
        vagNumberView.setText(jobScheduleListData.get(position).getVagNumber());

        courseNumberView.setText(HtmlCompat.fromHtml(ConvertUmlaut.toHtml(jobScheduleListData.get(position).getCourseNumber()), 0));

        typeView.setText(jobScheduleListData.get(position).getType());
        loctionView.setText(HtmlCompat.fromHtml(ConvertUmlaut.toHtml(jobScheduleListData.get(position).getLocation()), 0));

        holidayView.setText(jobScheduleListData.get(position).getHoliday());

        // Set background color and options according to the status of the entry.
        final View backgroundView = holder.mView.findViewById(R.id.calendar_entry_view);

        if (jobScheduleListData.get(position).isValidEntry) {

            //@rem:Shows how one can set the background resource of a view,(e.g. a custom drawable from an xml- file@@
            backgroundView.setBackgroundResource(R.drawable.background_gradient_green);
            //@@
            //addCalendarEntryView.setVisibility(View.VISIBLE);

            vagNumberView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainActivity.setFilterVagNumber(jobScheduleListData.get(position).getVagNumber());
                }
            });
        } else {
            backgroundView.setBackgroundResource(R.drawable.background_gradient_red);
            //addCalendarEntryView.setVisibility(View.GONE);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (jobScheduleListData != null)
            return jobScheduleListData.size();
        return 0;
    }
}
