package com.smile.wheelviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smile.timeselectview.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class TimePickerView extends LinearLayout implements View.OnClickListener{
    private WheelView mWvYear;
    private WheelView mWvMonth;
    private WheelView mWvDay;
    private OnDateSelectedListener mSelectedListener;
    private TextView mTvConfirm;

    public TimePickerView(Context context) {
        this(context, null);
    }

    public TimePickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        initDate();
        initListener();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_time_picker, this);
        mWvYear = (WheelView) findViewById(R.id.year);
        mWvMonth = (WheelView) findViewById(R.id.month);
        mWvDay = (WheelView) findViewById(R.id.day);
        mTvConfirm = (TextView) findViewById(R.id.tv_btn_confirm);
    }

    private void initDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String[] split = sdf.format(new Date()).split("-");
        int currentYear = Integer.parseInt(split[0]);
        int currentMonth = Integer.parseInt(split[1]);
        int currentDay = Integer.parseInt(split[2]);

        mWvYear.setData(getYearData(currentYear));
        mWvYear.setDefault(1);
        mWvMonth.setData(getMonthData());
        mWvMonth.setDefault(currentMonth - 1);
        mWvDay.setData(getDayData(getMaxDay(currentYear, currentMonth)));
        mWvDay.setDefault(currentDay - 1);
    }

    private void initListener(){
        mTvConfirm.setOnClickListener(this);
        mWvYear.setOnSelectListener(new WheelView.OnSelectListener() {
            @Override
            public void endSelect(int id, String text) {
                changeDayData();
            }

            @Override
            public void selecting(int id, String text) {

            }
        });

        mWvMonth.setOnSelectListener(new WheelView.OnSelectListener() {
            @Override
            public void endSelect(int id, String text) {
                changeDayData();
            }

            @Override
            public void selecting(int id, String text) {

            }
        });
    }

    private ArrayList<String> getYearData(int currentYear) {
        ArrayList<String> list = new ArrayList<>();
        for (int i = currentYear + 1; i >= 1900; i--) {
            list.add(String.valueOf(i));
        }
        return list;
    }

    private ArrayList<String> getMonthData() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            list.add(String.valueOf(i));
        }
        return list;
    }

    private ArrayList<String> getDayData(int maxDay){
        ArrayList<String> list = new ArrayList<>();
        for (int i=1;i <= maxDay;i++){
            list.add(String.valueOf(i));
        }
        return list;
    }

    private int getMaxDay(int year,int month){
        if (month == 2){
            if (isLeapYear(year)){
                return 29;
            }else {
                return 28;
            }
        }else if (month == 1 || month == 3 || month == 5 || month == 7
                || month == 8 || month ==10 || month == 12){
            return 31;
        }else {
            return 30;
        }
    }

    private boolean isLeapYear(int year){
        return (year % 100 == 0 && year % 400 == 0)
                || (year % 100 != 0 && year % 4 == 0);
    }

    private void changeDayData(){
        int selectDay = getDay();
        int currentYear = getYear();
        int currentMonth = getMonth();
        int maxDay = getMaxDay(currentYear,currentMonth);

        mWvDay.setData(getDayData(maxDay));

        if (selectDay > maxDay){
            mWvDay.setDefault(maxDay - 1);
        }else {
            mWvDay.setDefault(selectDay - 1);
        }

    }


    public int getYear(){
        return Integer.parseInt(mWvYear.getSelectedText());
    }

    public int getMonth(){
        return Integer.parseInt(mWvMonth.getSelectedText());
    }

    public int getDay(){
        return Integer.parseInt(mWvDay.getSelectedText());
    }

    public void setSelectedListener(OnDateSelectedListener listener){
        this.mSelectedListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_btn_confirm){
            if (mSelectedListener != null){
                mSelectedListener.selectedDate(getYear(), getMonth(),getDay());
            }
        }
    }

    public interface OnDateSelectedListener{
        void selectedDate(int year,int month,int day);
    }
}
