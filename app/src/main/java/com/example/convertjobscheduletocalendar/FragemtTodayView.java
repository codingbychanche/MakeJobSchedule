package com.example.convertjobscheduletocalendar;

import android.content.Context;
import android.os.Bundle;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import CalendarMaker.CalendarEntry;
import CalendarMaker.ConvertUmlaut;
import CalendarMaker.MakeCalendar;

public class FragemtTodayView extends Fragment {

    // ViewModel
    static MainActivityViewModel mainActivityViewModel;


    public FragemtTodayView() {
        // Empty!
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_today_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context=getActivity().getApplicationContext();
        mainActivityViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        List <CalendarEntry> mycalendar= mainActivityViewModel.getMyCalendar().getRawCalendar();
        int[] dayOfWeek = {R.string.so, R.string.mo, R.string.di, R.string.mi, R.string.don, R.string.fr, R.string.sa};

        // UI
        TextView dayOfWeekView =view.findViewById(R.id.day_of_week);
        TextView dateView = view.findViewById(R.id.date);
        TextView startTimeView = view.findViewById(R.id.start_time);
        TextView endTimeView = view.findViewById(R.id.end_time);
        TextView vagNumberView = view.findViewById(R.id.vag_number);
        TextView courseNumberView = view.findViewById(R.id.course_number);
        TextView loctionView = view.findViewById(R.id.location);
        TextView typeView = view.findViewById(R.id.type);
        TextView holidayView = view.findViewById(R.id.holiday_remark);

        // Get current day's event
        long currentTimeInMillisec = System.currentTimeMillis();
        Calendar todaysDate = Calendar.getInstance();
        todaysDate.setTimeInMillis(currentTimeInMillisec);

        if (todaysDate.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY || todaysDate.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {

            for (CalendarEntry entry : mycalendar) {
                if (entry.compareThisEntrysDateWith(todaysDate) == entry.HAS_SAME_DATE && entry.isValidEntry) {
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
                } else {
                    // Nothing found, nothing to display
                    //clearTodaysEventView();
                }
            }

        } else {
            //clearTodaysEventView();
            dayOfWeekView.setText("WE");
        }

    }
}
