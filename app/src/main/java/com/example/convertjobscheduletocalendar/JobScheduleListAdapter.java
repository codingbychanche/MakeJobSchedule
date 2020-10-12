package com.example.convertjobscheduletocalendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import CalendarMaker.CalendarEntry;

/**
 * Fills our job schedule list...
 */
public class JobScheduleListAdapter extends RecyclerView.Adapter<JobScheduleListAdapter.ViewHolder>{
    
    private List<CalendarEntry> jobScheduleListData=new ArrayList<>();
    private MainActivity mainActivity;
    private Context context;
    private Resources resources;

    public JobScheduleListAdapter(List<CalendarEntry> jobScheduleListData, MainActivity mainActivity, Context context) {
        this.jobScheduleListData = jobScheduleListData;
        this.mainActivity = mainActivity;
        this.context=context;

        resources=context.getResources();
    }

    interface receieve {
        void addToCalendarPressed(int position);
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.job_schedule_row_view_1, parent, false);
        JobScheduleListAdapter.ViewHolder vh = new JobScheduleListAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        TextView dateView = holder.mView.findViewById(R.id.date);
        TextView startTimeView=holder.mView.findViewById(R.id.start_time);
        TextView endTimeView=holder.mView.findViewById(R.id.end_time);
        TextView vagNumberView=holder.mView.findViewById(R.id.vag_number);
        TextView courseNumberView=holder.mView.findViewById(R.id.course_number);
        TextView loctionView=holder.mView.findViewById(R.id.location);
        TextView typeView=holder.mView.findViewById(R.id.type);
        TextView holidayView=holder.mView.findViewById(R.id.holiday_remark);
        TextView originalEntryView=holder.mView.findViewById(R.id.original_entry);

        dateView.setText(jobScheduleListData.get(position).getDate());
        startTimeView.setText(jobScheduleListData.get(position).getStartTime());
        endTimeView.setText(jobScheduleListData.get(position).getEndTime());
        vagNumberView.setText(jobScheduleListData.get(position).getVagNumber());
        courseNumberView.setText(jobScheduleListData.get(position).getCourseNumber());
        typeView.setText(jobScheduleListData.get(position).getType());
        loctionView.setText(jobScheduleListData.get(position).getLocation());
        holidayView.setText(jobScheduleListData.get(position).getHoliday());
        originalEntryView.setText(jobScheduleListData.get(position).getOrgiriginalEntry());

        // Set background color and options according to the status of the entry.
        final View backgroundView=holder.mView.findViewById(R.id.calendar_entry_view);
        final TextView addCalendarEntryView=backgroundView.findViewById(R.id.add_to_calendar);

        if (jobScheduleListData.get(position).isValidEntry) {

            //@rem:Shows how one can set the background resource of a view,(e.g. a custom drawable from an xml- file@@
            backgroundView.setBackgroundResource(R.drawable.background_gradient_green);
            //@@
            addCalendarEntryView.setVisibility(View.VISIBLE);

            // Buttons
            addCalendarEntryView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   mainActivity.addToCalendarPressed(position);
                }
            });
        }else {
            backgroundView.setBackgroundResource(R.drawable.background_gradient_red);
            addCalendarEntryView.setVisibility(View.GONE);
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
