package com.example.obiaf.swipecalendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    //TODO:Remove this code when integrating into app
    public Bitmap imageBitmap;
    public static Bitmap resizedBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO:Remove this code when integrating into app


        setContentView(R.layout.activity_main);
        CustomCalendarView cv = (CustomCalendarView) findViewById(R.id.customCalendarView);
        cv.setFragmentManager(getSupportFragmentManager());

    }

}
