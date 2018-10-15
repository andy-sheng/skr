package com.wali.live.watchsdk.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.base.dialog.MyAlertDialog;
import com.wali.live.watchsdk.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by zhangzhiyuan on 16-6-7.
 */
public class DateWheelDialog extends MyAlertDialog implements OnClickListener,WheelView.OnWheelChangedListener,DialogInterface.OnShowListener{

    private final OnDateSetListener mDateSetListener;
    private final Calendar mCalendar;
    private WheelView yearWheelView;
    private WheelView monthWheelView;
    private WheelView dayWheelView;
    private TextView myTitle;
    ArrayWheelAdapter yearAdapter;
    ArrayWheelAdapter monthAdapter;
    ArrayWheelAdapter dayAdapter;


    private String[] mYearArray;
    private String[] mNormalDayArray;
    private String[] mDayArray;

    private String[] mBigDayArray;
    private String[] mSmallDayArray;
    private String[] mSmallDayArray2;
    private String[] mMonthArray;
    //选择的年 日
    private int mSelectYear;
    private int mSelectMonth;
    private int mSelectDay;

    private String mTitleFormat;

    private Calendar mMinDate; // 设置最小日期

    private Calendar mMaxDate;  // 最大日期

    private int maxDateYear = 0;
    private int minDateYear = 0;
    private int maxDateMonth = 0;
    private int minDateMonth = 0;

    /**
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateSetListener {

        /**
         * @param year The year that was set.
         * @param monthOfYear The month that was set (0-11) for compatibility
         *  with {@link Calendar}.
         * @param dayOfMonth The day of the month that was set.
         */
        void onDateSet(int year, int monthOfYear, int dayOfMonth);
    }


    public DateWheelDialog(Context context, OnDateSetListener listener) {
        super(context);
        mDateSetListener = listener;
        mCalendar = Calendar.getInstance();
        mSelectYear = mCalendar.get(Calendar.YEAR);
        mSelectMonth = mCalendar.get(Calendar.MONTH) + 1;
        mSelectDay = mCalendar.get(Calendar.DAY_OF_MONTH);

        //final Context themeContext = getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.date_pick_layout, null);

        yearWheelView = (WheelView) view.findViewById(R.id.year);
        monthWheelView = (WheelView) view.findViewById(R.id.month);
        dayWheelView = (WheelView) view.findViewById(R.id.day);
        monthWheelView.setType(WheelView.TYPE_MOUTH);

        myTitle = (TextView) view.findViewById(R.id.my_title);

        yearWheelView.addChangingListener(this);
        monthWheelView.addChangingListener(this);
        dayWheelView.addChangingListener(this);

        yearWheelView.setCyclic(true);
        monthWheelView.setCyclic(true);
        dayWheelView.setCyclic(true);
        mYearArray = getArrays(1900, mCalendar.get(Calendar.YEAR) + 1);
        mMonthArray = getArrays(1, 12);
        mNormalDayArray = getArrays(1, 30);
        mBigDayArray = getArrays(1, 31);
        mSmallDayArray = getArrays(1, 29);
        mSmallDayArray2 = getArrays(1, 28);
        yearAdapter = new ArrayWheelAdapter<>(mYearArray);
        monthAdapter = new ArrayWheelAdapter<>(mMonthArray);
        checkDayArray();
        yearWheelView.setLabel(context.getString(R.string.lable_year));
        monthWheelView.setLabel(context.getString(R.string.lable_month));
        dayWheelView.setLabel(context.getString(R.string.lable_day));

        yearWheelView.setAdapter(yearAdapter);
        monthWheelView.setAdapter(monthAdapter);
        dayWheelView.setAdapter(dayAdapter);
        yearWheelView.setCurrentItem(indexOfArray(String.valueOf(mSelectYear), mYearArray));
        monthWheelView.setCurrentItem(indexOfArray(String.valueOf(mSelectMonth), mMonthArray));
        dayWheelView.setCurrentItem(indexOfArray(String.valueOf(mSelectDay), mDayArray));
        setView(view);
        setButton(BUTTON_POSITIVE, context.getString(R.string.ok), this);
        setButton(BUTTON_NEGATIVE, context.getString(R.string.cancel), this);

//        setPositiveButtonTextColor(context.getResources().getColor(R.color.color_e5aa1e));
//        setTitleSize(DisplayUtils.dip2px(15f));
        updateTitle(mSelectYear, mSelectMonth, mSelectDay);
        setOnShowListener(this);
    }

    public void hideLabel( ) {
        yearWheelView.setLabel("");
        monthWheelView.setLabel("");
        dayWheelView.setLabel("");
    }

    public void setTitleFormat(String format) {
        mTitleFormat = format;
    }

    @Override
    public void onShow(DialogInterface dialog){

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            yearWheelView.setCurrentItem(indexOfArray(String.valueOf(mSelectYear), mYearArray));
            monthWheelView.setCurrentItem(indexOfArray(String.valueOf(mSelectMonth), mMonthArray));
            dayWheelView.setCurrentItem(indexOfArray(String.valueOf(mSelectDay), mDayArray));
            yearWheelView.invalidateLayouts();
            monthWheelView.invalidateLayouts();
            dayWheelView.invalidateLayouts();
            yearWheelView.invalidate();
            monthWheelView.invalidate();
            dayWheelView.invalidate();
        }
    }

    private String[] getArrays(final int start, final int end) {
        if (start > end) {
            return null;
        }
        String[] arrays = new String[end - start + 1];

        int len = end - start + 1;
        for (int i = 0; i < len; i++) {
            arrays[i] = String.valueOf(start + i);
        }
        return arrays;
    }
    private int indexOfArray(String str, String[] arrays) {
        if (TextUtils.isEmpty(str) || arrays == null) {
            return -1;
        }

        int index = 0;
        for (String item : arrays) {
            if (str.equalsIgnoreCase(item)) {
                break;
            }
            index++;
        }

        return index;
    }


    @Override
    public void onChanged(WheelView wheel, int oldValue, int newValue){
        if (oldValue == newValue) {
            return;
        }

        int i = wheel.getId();
        if (i == R.id.year) {
            mSelectYear = Integer.valueOf(mYearArray[newValue]);
            checkDayArray();
            updateWheel();

        } else if (i == R.id.month) {
            mSelectMonth = Integer.valueOf(mMonthArray[newValue]);
            checkDayArray();

        } else if (i == R.id.day) {
            mSelectDay = Integer.valueOf(mDayArray[newValue]);
            //mSelectDay = Integer.valueOf(mDayArray[dayWheelView.getCurrentItem()]);
        }

//        monthWheelView.invalidateLayouts();
//        monthWheelView.invalidate();

        updateTitle(mSelectYear, mSelectMonth, mSelectDay);
    }

    private void checkDayArray(){

        if(mSelectMonth==2){
            if((mSelectYear%4==0&&mSelectYear%100!=0)||mSelectYear%400==0){
                mDayArray=mSmallDayArray;
            }else{
                mDayArray=mSmallDayArray2;
            }
        }else if(mSelectMonth==4||mSelectMonth==6||mSelectMonth==9||mSelectMonth==11){
            mDayArray=mNormalDayArray;
        }else{
            mDayArray=mBigDayArray;
        }
        if(dayAdapter!=null){
            dayAdapter.setData(mDayArray);
            dayWheelView.invalidate();
        }else{
            dayAdapter = new ArrayWheelAdapter<String>(mDayArray);
        }

    }


    public void updateTitle(int year, int month, int day) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month-1);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        String title;
        if (TextUtils.isEmpty(mTitleFormat)) {
            title = DateUtils.formatDateTime(getContext(),
                    mCalendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE
                            | DateUtils.FORMAT_SHOW_WEEKDAY
                            | DateUtils.FORMAT_SHOW_YEAR
                            | DateUtils.FORMAT_ABBREV_MONTH
                            | DateUtils.FORMAT_ABBREV_WEEKDAY
            );
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat(mTitleFormat);
            title = formatter.format(mCalendar.getTimeInMillis());
        }
        setTitle(title);
    }

    public void setTitle(String title){
        myTitle.setText(title);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                if (mDateSetListener != null) {
                    // Clearing focus forces the dialog to commit any pending
                    // changes, e.g. typed text in a NumberPicker.
                    //mDatePicker.clearFocus();
                    mDateSetListener.onDateSet(mSelectYear, mSelectMonth, mSelectDay);
                }
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }

    public void setInitDate(int year, int month, int day) {
        if (yearWheelView == null || monthWheelView == null || dayWheelView == null) {
            return;
        }
        mSelectYear = year;
        mSelectMonth = month;
        mSelectDay = day;
        checkDayArray();
        yearWheelView.setCurrentItem(indexOfArray(String.valueOf(mSelectYear), mYearArray));
        monthWheelView.setCurrentItem(indexOfArray(String.valueOf(mSelectMonth), mMonthArray));
        dayWheelView.setCurrentItem(indexOfArray(String.valueOf(mSelectDay), mDayArray));
        updateTitle(year, month, day);
    }

    public WheelView getDayWheelView() {
        return dayWheelView;
    }

    public void setMinDate(long minDate) {
        mMinDate = Calendar.getInstance();
        mMinDate.setTimeInMillis(minDate);
        updateYearArray();
    }

    public void setMaxDate(long maxDate) {
        mMaxDate = Calendar.getInstance();
        mMaxDate.setTimeInMillis(maxDate);
        updateYearArray();
    }

    private void updateWheel() {
        if (mSelectYear == minDateYear) {
            mMonthArray = getArrays(minDateMonth, 12);
            monthAdapter.setData(mMonthArray);
            if (mSelectMonth < minDateMonth) {
                mSelectMonth = minDateMonth;
                monthWheelView.setCurrentItem(indexOfArray(String.valueOf(mSelectMonth), mMonthArray));
            }
            monthWheelView.invalidate();
        } else if (mSelectYear == maxDateYear) {
            mMonthArray = getArrays(1, maxDateMonth);
            monthAdapter.setData(mMonthArray);
            if (mSelectMonth > maxDateMonth) {
                mSelectMonth = maxDateMonth;
                monthWheelView.setCurrentItem(indexOfArray(String.valueOf(mSelectMonth), mMonthArray));
            }
            monthWheelView.invalidate();
        } else {
            mMonthArray = getArrays(1, 12);
            monthAdapter.setData(mMonthArray);
            monthWheelView.setCurrentItem(indexOfArray(String.valueOf(mSelectMonth), mMonthArray));
        }
    }

    private void updateYearArray() {
        if (mMaxDate != null) {
            maxDateYear = mMaxDate.get(Calendar.YEAR);
            maxDateMonth = mMaxDate.get(Calendar.MONTH) + 1;
        }
        if (mMinDate != null) {
            minDateYear = mMinDate.get(Calendar.YEAR);
            minDateMonth = mMinDate.get(Calendar.MONTH);
        }
        if (minDateYear > 0 && maxDateYear > minDateYear) {
            mYearArray = getArrays(minDateYear, maxDateYear);
            yearAdapter.setData(mYearArray);
            int items = maxDateYear - minDateYear + 1;
            if (items < 5) {
                yearWheelView.setCyclic(false);
            }
        }
    }

}
