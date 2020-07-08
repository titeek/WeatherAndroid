package com.example.astroweather.Model;

import java.util.List;

public class WeatherForecastResult {
    public double lat;
    public double lon;
    public String timezone;
    public int timezone_offset;
    public Current current;
    public List<Hourly> hourly;
    public List<Daily> daily;

}
