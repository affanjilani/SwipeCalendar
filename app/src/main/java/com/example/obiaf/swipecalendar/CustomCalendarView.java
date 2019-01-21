package com.example.obiaf.swipecalendar;


import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


import static android.content.ContentValues.TAG;

/**
 * Created by obiaf on 5/30/2018.
 */

public class CustomCalendarView extends LinearLayout {
    Context context;

    //Attributes from XML
    int gridColor;
    int currentDayColor;
    int weekHeaderColor;
    int monthHeaderColor;
    String fontFile;
    boolean multiDaySelect;
    Typeface typeface;

    //View elements
    TextView monthHeader;
    //buttons
    ImageButton prevMonth;       //button for previous month
    ImageButton nextMonth;       //button for next month
    //The day of week headers
    TextView mDay,tDay,wDay,thDay,fDay,satDay,sunDay;
    ViewPager calendar;
    Calendar[] selectedDays;
    int posSelected;

    FragmentManager fm;

    private SelectionObserver selectionObserver;

    public CustomCalendarView(Context context, AttributeSet attrs){
        super(context,attrs);

        this.context = context;

        //get the fragment manager
        try{
            final FragmentActivity activity = (FragmentActivity) context;

            // Return the fragment manager
            this.fm = activity.getSupportFragmentManager();

        } catch (ClassCastException e) {
            Log.d(TAG, "Error getting the fragment manager.");
        }

        //Inflate layout
        LayoutInflater.from(context).inflate(R.layout.calendar_view, this, true);

        this.selectionObserver = new SelectionObserver();
        this.selectedDays = new Calendar[7];

        //initialize attributes elements and listeners
        loadAttributes(attrs);

        initElements(context);

        initListeners();
    }

    //Loads attributes from XML file into variables declared above
    private void loadAttributes(AttributeSet attrs){
        TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.CustomCalendarView);

        try{
            gridColor = attributes.getColor(R.styleable.CustomCalendarView_gridColor,
                    getResources().getColor((android.R.color.white)));

            currentDayColor = attributes.getColor(R.styleable.CustomCalendarView_currentDayColor,
                    getResources().getColor(R.color.sessionsBlue));

            weekHeaderColor = attributes.getColor(R.styleable.CustomCalendarView_weekHeaderColor,
                    getResources().getColor(android.R.color.white));

            monthHeaderColor = attributes.getColor(R.styleable.CustomCalendarView_monthHeaderColor,
                    getResources().getColor(android.R.color.white));

            fontFile = attributes.getString(R.styleable.CustomCalendarView_typeface);

            multiDaySelect = attributes.getBoolean(R.styleable.CustomCalendarView_multiDaySelect,false);
        }
        finally{
            attributes.recycle();
        }

    }

    private void initElements(Context context){

        //init typeface
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/"+fontFile);

        monthHeader = (TextView) findViewById(R.id.monthHeader);
        monthHeader.setTypeface(typeface);

        //buttons
        prevMonth = (ImageButton) findViewById(R.id.prev);
        nextMonth = (ImageButton) findViewById(R.id.next);

        //Week header
        mDay = (TextView) findViewById(R.id.mDay);
        mDay.setTypeface(typeface);

        tDay = (TextView) findViewById(R.id.tDay);
        tDay.setTypeface(typeface);

        wDay = (TextView) findViewById(R.id.wDay);
        wDay.setTypeface(typeface);

        thDay = (TextView) findViewById(R.id.thDay);
        thDay.setTypeface(typeface);

        fDay = (TextView) findViewById(R.id.fDay);
        fDay.setTypeface(typeface);

        satDay = (TextView) findViewById(R.id.satDay);
        satDay.setTypeface(typeface);

        sunDay = (TextView) findViewById(R.id.sunDay);
        sunDay.setTypeface(typeface);

        //Recycler view that will be populated by calendar grid
        calendar = (ViewPager) findViewById(R.id.calendarPager);
        final CalendarPagerAdapter pagerAdapter = new CalendarPagerAdapter(fm);
        calendar.setAdapter(pagerAdapter);

        calendar.post(new Runnable(){
            @Override
            public void run() {
                calendar.setCurrentItem(pagerAdapter.MAX/2);
            }
        });
    }

    //for both prev and next month buttons, will set smoothScroll to true once Calendar is snappier
    private void initListeners(){
        prevMonth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.setCurrentItem(calendar.getCurrentItem()-1,false);
            }
        });

        nextMonth.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar.setCurrentItem(calendar.getCurrentItem()+1,false);
            }
        });

        //Listener to be able to update the month header when swiped
        calendar.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                CalendarPagerAdapter adapter = (CalendarPagerAdapter) calendar.getAdapter();
                int offset = position - adapter.MAX/2;

                Calendar tmp = new GregorianCalendar();
                tmp.add(Calendar.MONTH,offset);

                String headerPattern = "MMMM yyyy";     //pattern for month and year representation
                SimpleDateFormat sdf = new SimpleDateFormat(headerPattern);
                String header = sdf.format(tmp.getTime());


                monthHeader.setText(header);


            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    //Adapter for ViewPager
    public class CalendarPagerAdapter extends FragmentStatePagerAdapter{
        public final int MAX = 100;

        public CalendarPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position){
            int offset = position - MAX/2;
            CalendarPagerFragment page = new CalendarPagerFragment();

            //make offset available for fragment onCreate
            Bundle bundle = new Bundle();
            bundle.putInt("offset",offset);

            page.setArguments(bundle);

            page.setTypeface(typeface);

            //sets the sessions to show for a given month
            page.setSessionSet(makeSessionSet());

            page.setObserver(selectionObserver);
            page.setSelectedDays(selectedDays);
            page.setSelectedPos(posSelected);
            return page;
        }

        @Override
        public int getCount(){
            return MAX;
        }
    }

    public void setFragmentManager(FragmentManager fm){
        this.fm = fm;
    }

    //just a generator of previewable information for testing purposes.
    public ArrayList<Session> makeSessionSet(){
        Session s1 = new Session((long)1,0,0,"ball and chill", "8 11", "12:34", "Basketball", "Affan", 5, 10, true);
        ArrayList<Session> sessionSet = new ArrayList<Session>();
        sessionSet.add(s1);
        return sessionSet;
    }

    //observer to retrieve date selection from the page fragments in viewPager
    public class SelectionObserver{

        public void update(Calendar[] cal, int selectedPos){
            selectedDays = cal;
            posSelected = selectedPos;
        }

    }

    //to retrieve the selected day. If it is in singleSelect mode then the selected day will be at pos 0
    //in multiSelect mode it is arranged by day of the week (sunday = 0, saturday = 6)
    public Calendar[] getSelectedDays(){ return this.selectedDays;}

}
