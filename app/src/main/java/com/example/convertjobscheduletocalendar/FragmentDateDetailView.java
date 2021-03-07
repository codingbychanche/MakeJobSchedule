package com.example.convertjobscheduletocalendar;

import android.os.Bundle;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;
import java.util.List;

import CalendarMaker.CalendarEntry;


public class FragmentDateDetailView extends Fragment {

    // Interface
    DateDetailView dateDetailView;

    // ViewModel
    static MainActivityViewModel mainActivityViewModel;

    // UI
    ImageButton addThisEntryToCalendarView, mailInquiryForThisEntryView;

    // Implement in order to invoke an appropriate reaction
    public interface DateDetailView {
        void hideFragmentCurrentlyShown();

        void addThisEntryToCalendar(int position);

        void mailInquiryForThisEntry(int position);
    }

    public FragmentDateDetailView() {
        // Empty!
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.fade_in));
        setExitTransition(inflater.inflateTransition(R.transition.fade_out));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_date_detail_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivityViewModel = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);
        dateDetailView = (DateDetailView) getActivity();

        // Hides this fragment
        Button okView = view.findViewById(R.id.ok_button);
        okView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDetailView.hideFragmentCurrentlyShown();
            }
        });

        // Adds the selected entry to the devices calendar
        addThisEntryToCalendarView = view.findViewById(R.id.add_just_this_entry_to_devices_calendar);
        addThisEntryToCalendarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDetailView.addThisEntryToCalendar(mainActivityViewModel.getCurrentJobScheduleListItemsIndex());
            }
        });

        // Sends a mail inquiry
        mailInquiryForThisEntryView = view.findViewById(R.id.mail_inquiry_for_this_entry);
        mailInquiryForThisEntryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDetailView.mailInquiryForThisEntry(mainActivityViewModel.getCurrentJobScheduleListItemsIndex());
            }
        });

        // Show details of entry currently selected
        TextView vagView = view.findViewById(R.id.vag_number);
        TextView courseNumber = view.findViewById(R.id.course_number);
        TextView startDateView = view.findViewById(R.id.begin_date);
        TextView endDateView = view.findViewById(R.id.end_date);
        TextView orgiriginalEntry = view.findViewById(R.id.original_entry);

        int currentEntry = mainActivityViewModel.getCurrentJobScheduleListItemsIndex();
        vagView.setText(mainActivityViewModel.getJobScheduleListData().get(currentEntry).getVagNumber());
        courseNumber.setText(mainActivityViewModel.getJobScheduleListData().get(currentEntry).getCourseNumber());

        int year = mainActivityViewModel.getJobScheduleListData().get(currentEntry).getYear() + 2000;
        int month = mainActivityViewModel.getJobScheduleListData().get(currentEntry).getMonth();
        int day = mainActivityViewModel.getJobScheduleListData().get(currentEntry).getDay();
        String startTime = mainActivityViewModel.getJobScheduleListData().get(currentEntry).getStartTime();
        String endTime = mainActivityViewModel.getJobScheduleListData().get(currentEntry).getEndTime();
        startDateView.setText(day + "." + month + "." + year + "  " + startTime);
        endDateView.setText(endTime);

        String originalEntry = mainActivityViewModel.getJobScheduleListData().get(currentEntry).getOrgiriginalEntry();
        orgiriginalEntry.setText(originalEntry);

        // Show whole course
        List <CalendarEntry>thisCourseByVAGNumber=new ArrayList<CalendarEntry>();
        thisCourseByVAGNumber=mainActivityViewModel.getMyCalendar().getCalenderEntrysMatchingVAG(mainActivityViewModel.getJobScheduleListData().get(currentEntry).getVagNumber());

        TextView firstDayOfCourseView=view.findViewById(R.id.course_begins_at);
        TextView lastDayOfCourseView=view.findViewById(R.id.course_ends_at);
        TextView daysRunningView=view.findViewById(R.id.number_of_days_running);

        int numberOfEntriesFound=thisCourseByVAGNumber.size()-1;
        int numberOfDaysRunning=thisCourseByVAGNumber.size();
        String startDate=thisCourseByVAGNumber.get(0).getDate();
        String endDate=thisCourseByVAGNumber.get(numberOfEntriesFound).getDate();

        firstDayOfCourseView.setText(startDate);
        lastDayOfCourseView.setText(endDate);
        daysRunningView.setText(numberOfDaysRunning+"");


    }
}
