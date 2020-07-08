package com.example.astroweather.Common;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Common {

    public static final String APP_ID = "53b0adda2b59056e70868da867487af6";
    public static Location current_location = null;

    public static String convertUnixToDate(int dt) {
        Date date = new Date(dt*1000L);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE dd.MM");
        String afterForm = simpleDateFormat.format(date);
        return afterForm;
    }
}
