package com.example.convertjobscheduletocalendar;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class FragmentDateDetailView extends DialogFragment {

    // Fragments UI components
    Button cancelButton;

    // ViewModel
    static MainActivityViewModel mainActivityViewModel;

    public FragmentDateDetailView() {
        // Constructor must be empty....
    }

    public static FragmentDateDetailView newInstance(String titel) {
        FragmentDateDetailView frag= new FragmentDateDetailView();
        Bundle args = new Bundle();
        args.putString("titel", titel);
        frag.setArguments(args);

        return frag;
    }

    // Inflate fragment layout
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_date_detail_view, container);

    }

    // This fills the layout with data
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mainActivityViewModel= ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);

        TextView vagView=view.findViewById(R.id.vag_number);
        TextView courseNumber=view.findViewById(R.id.course_number);
        TextView startDateView=view.findViewById(R.id.begin_date);
        TextView endDateView=view.findViewById(R.id.end_date);
        TextView orgiriginalEntry=view.findViewById(R.id.original_entry);

        int currentEntry=mainActivityViewModel.getCurrentJobScheduleListItemsIndex();
        vagView.setText(mainActivityViewModel.getJobScheduleListData().get(currentEntry).getVagNumber());
        courseNumber.setText(mainActivityViewModel.getJobScheduleListData().get(currentEntry).getCourseNumber());

        int year = mainActivityViewModel.getJobScheduleListData().get(currentEntry).getYear() + 2000;
        int month = mainActivityViewModel.getJobScheduleListData().get(currentEntry).getMonth();
        int day = mainActivityViewModel.getJobScheduleListData().get(currentEntry).getDay();
        String startTime = mainActivityViewModel.getJobScheduleListData().get(currentEntry).getStartTime();
        startDateView.setText(day+"."+month+"."+year+" / "+startTime);

        String originalEntry = mainActivityViewModel.getJobScheduleListData().get(currentEntry).getOrgiriginalEntry();
        orgiriginalEntry.setText(originalEntry);

        // Cancel button was pressed
        cancelButton = (Button) view.findViewById(R.id.ok_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               dismiss();
            }
        });
    }
}