package com.example.astroweather.Model;

import java.util.List;

public class Daily {
    public int dt;
    public int sunrise;
    public int sunset;
    public Temp temp;
    public FeelsLike feels_like;
    public int pressure;
    public int humidity;
    public double dew_point;
    public double wind_speed;
    public int wind_deg;
    public List<Weather> weather;
    public int clouds;
    public double rain;
    public double uvi;
}
