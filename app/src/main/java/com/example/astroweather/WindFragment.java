package com.example.astroweather;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.astroweather.Common.Common;
import com.example.astroweather.Model.WeatherResult;
import com.example.astroweather.Retrofit.IOpenWeatherMap;
import com.example.astroweather.Retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class WindFragment extends Fragment {

    StringBuilder weatherInformation = new StringBuilder("");
    String filename = "weatherWind.txt";

    TextView speedText, directoryText, humidityText, visibilityText, cloudText;
    //private String unit = "metric";

    LinearLayout weatherPanel;
    ProgressBar loading;

    IOpenWeatherMap mService;
    CompositeDisposable compositeDisposable;




    public WindFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View itemView = inflater.inflate(R.layout.fragment_wind, container, false);

        speedText = (TextView) itemView.findViewById(R.id.speedText);
        directoryText = (TextView) itemView.findViewById(R.id.directoryText);
        humidityText = (TextView) itemView.findViewById(R.id.humidityText);
        visibilityText = (TextView) itemView.findViewById(R.id.visibilityText);
        cloudText = (TextView) itemView.findViewById(R.id.cloudText);

        weatherPanel = (LinearLayout) itemView.findViewById(R.id.weatherPanel);
        loading = (ProgressBar) itemView.findViewById(R.id.loading);

        getWeatherInformation(((MainActivity)getActivity()).unit);

        return itemView;
    }

    private void getWeatherInformation(final String unit) {

        Context context = (MainActivity)getActivity();

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected) {

            compositeDisposable.add(mService.getWeatherByLatLng(
                    String.valueOf(((MainActivity)getActivity()).lat),
                    String.valueOf(((MainActivity)getActivity()).lon),
                    Common.APP_ID,
                    unit)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<WeatherResult>() {
                        @Override
                        public void accept(WeatherResult weatherResult) throws Exception {


                            //Load info

                            if(unit.equals("metric")) {
                                speedText.setText(new StringBuilder(String.valueOf(weatherResult.getWind().getSpeed())).append("\nm/s"));
                            } else {
                                speedText.setText(new StringBuilder(String.valueOf(weatherResult.getWind().getSpeed())).append(" miles/s"));
                            }

                            directoryText.setText(new StringBuilder(windDirection(weatherResult.getWind().getDeg())));
                            humidityText.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getHumidity())).append("%"));
                            cloudText.setText(new StringBuilder(String.valueOf(weatherResult.getClouds().getAll())).append("%"));
                            visibilityText.setText(new StringBuilder(String.valueOf(weatherResult.getVisibility() / 1000)).append(" km"));

                            weatherInformation
                                    .append(",")
                                    .append(speedText.getText()).append(",")
                                    .append(directoryText.getText()).append(",")
                                    .append(humidityText.getText()).append(",")
                                    .append(cloudText.getText()).append(",")
                                    .append(visibilityText.getText()).append(",");

                            Log.d("XXX", weatherInformation.toString());

                            loading.setVisibility(View.GONE);
                            weatherPanel.setVisibility(View.VISIBLE);

                            ReadWriteClass.writeToFile(weatherInformation.toString(), (MainActivity)getActivity(), filename);


                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            //Toast.makeText(getActivity(), ""+throwable.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })

            );

        } else {
            Log.d("XXX", "No Internet connection!");
            Toast toast = Toast.makeText(getActivity(), "No Internet connection!\nWeather may be outdated!", Toast.LENGTH_SHORT);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message); if( v != null) v.setGravity(Gravity.CENTER); toast.show();

            String result = ReadWriteClass.readFromFile((MainActivity)getActivity(), filename);
            if(result.isEmpty()) {
                Toast.makeText(getActivity(), "Data is not full!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("SSS", result);
                loading.setVisibility(View.GONE);
                weatherPanel.setVisibility(View.VISIBLE);
                String[] informationList = result.split(",");

                speedText.setText(informationList[1]+"\n");
                directoryText.setText(informationList[2]);
                humidityText.setText(informationList[3]);
                cloudText.setText(informationList[4]);
                visibilityText.setText(informationList[5]);
            }



        }

    }

    private String windDirection(double windDegrees) {

        if(windDegrees > 348.75 && windDegrees <= 11.25) {
            return "N";
        } else if(windDegrees > 11.25 && windDegrees <= 33.75) {
            return "NNE";
        } else if(windDegrees > 33.75 && windDegrees <= 56.25) {
            return "NE";
        } else if(windDegrees > 56.25 && windDegrees <= 78.75) {
            return "ENE";
        } else if(windDegrees > 78.75 && windDegrees <= 101.25) {
            return "E";
        } else if(windDegrees > 101.25 && windDegrees <= 123.75) {
            return "ESE";
        } else if(windDegrees > 123.75 && windDegrees <= 146.25) {
            return "SE";
        } else if(windDegrees > 146.25 && windDegrees <= 168.75) {
            return "SSE";
        } else if(windDegrees > 168.75 && windDegrees <= 191.25) {
            return "S";
        } else if(windDegrees > 191.25 && windDegrees <= 213.75) {
            return "SSW";
        } else if(windDegrees > 213.75 && windDegrees <= 236.25) {
            return "SW";
        } else if(windDegrees > 236.25 && windDegrees <= 258.75) {
            return "WSW";
        } else if(windDegrees > 258.75 && windDegrees <= 281.25) {
            return "W";
        } else if(windDegrees > 281.25 && windDegrees <= 303.75) {
            return "WNW";
        } else if(windDegrees > 303.75 && windDegrees <= 326.25) {
            return "NW";
        } else
            return "NNW";

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.astro_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.item1)
        {
            Toast.makeText(getActivity(), "Data updated", Toast.LENGTH_SHORT).show();
            getWeatherInformation(((MainActivity)getActivity()).unit);
        }
        else if((item.getItemId() == R.id.tempC) && (((MainActivity)getActivity()).unit.equals("imperial")))
        {
            Toast.makeText(getActivity(), "Units updated - Metric", Toast.LENGTH_SHORT).show();
            ((MainActivity)getActivity()).unit = "metric";
            getWeatherInformation(((MainActivity)getActivity()).unit);
        }
        else if((item.getItemId() == R.id.tempF) && (((MainActivity)getActivity()).unit.equals("metric")))
        {
            Toast.makeText(getActivity(), "Units updated - Imperial", Toast.LENGTH_SHORT).show();
            ((MainActivity)getActivity()).unit = "imperial";
            getWeatherInformation(((MainActivity)getActivity()).unit);
        }
        else if(item.getItemId() == R.id.city) {
            Intent favCity = new Intent(this.getActivity(), FavCities.class);
            startActivity(favCity);
        }

        return true;
    }
}
