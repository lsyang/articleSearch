package com.codepath.nytimessearch;

/**
 * Created by lsyang on 3/18/17.
 */

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;


public class FilterFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener,
        OnItemSelectedListener {
    private TextView mEditBeginDate;
    private Spinner mSortOrder;
    private Button saveBtn;
    private Button changeDateBtn;

    private boolean isNew;
    private int year;
    private int month;
    private int day;
    private int sortOrder;

    public interface FilterDialogListener {
        void onFinishEditDialog(int year, int month, int day, int sortOrder);
    }


    public FilterFragment(){
    }

    public static FilterFragment newInstance(int year, int month, int day, int sortOrder){
        FilterFragment frag = new FilterFragment();
        Bundle args = new Bundle();
        args.putString("header", "Article Filter Settings");
        args.putInt("year", year);
        args.putInt("month", month);
        args.putInt("day", day);
        args.putInt("sortOrder", sortOrder);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_filter_article, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstaceState){
        super.onViewCreated(view, savedInstaceState);
        mEditBeginDate = (TextView) view.findViewById(R.id.txt_beginDate);
        mSortOrder = (Spinner) view.findViewById(R.id.txt_sortOrder);
        saveBtn = (Button) view.findViewById(R.id.edit_save);
        changeDateBtn = (Button) view.findViewById(R.id.edit_date);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(),
                R.array.sort_order, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortOrder.setOnItemSelectedListener(this);
        mSortOrder.setAdapter(adapter);

        String header = getArguments().getString("header", "");
        year = getArguments().getInt("year", 2017);
        month = getArguments().getInt("month", 01);
        day = getArguments().getInt("day", 01);
        sortOrder = getArguments().getInt("sortOrder", 0);


        getDialog().setTitle(header);
        displayDueDate();
        int spinnerPosition = adapter.getPosition(String.valueOf(sortOrder));
        mSortOrder.setSelection(spinnerPosition);

        saveBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onSave();
            }
        });
        changeDateBtn.setOnClickListener(new OnClickListener()

        {
            @Override
            public void onClick(View v)
            {
                showDatePickerDialog();
            }
        });

        getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        super.onResume();
    }

    public void showDatePickerDialog() {
        DialogFragment datePickerFragment = new DatePickerFragment();
        datePickerFragment.setTargetFragment(FilterFragment.this, 300);
        datePickerFragment.show(getFragmentManager(), "timePicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        displayDueDate();
    }

    private void displayDueDate() {
        SimpleDateFormat outputFmt = new SimpleDateFormat("MM/dd/yyyy");
        String currentDateTimeString = "Begin Date: " + outputFmt.format(new Date(year - 1900, month, day));
        mEditBeginDate.setText(currentDateTimeString);
    }

    public void onSave() {
        FilterDialogListener listener = (FilterDialogListener) getActivity();
        listener.onFinishEditDialog(year, month, day, sortOrder);
        dismiss();
    }

    public void onItemSelected(AdapterView<?> parent, View view,
            int pos, long id) {
        sortOrder = pos;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        sortOrder = 0;
    }
}
