package com.example.astroweather;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.astroweather.Common.Common;
import com.example.astroweather.Model.WeatherResult;
import com.example.astroweather.Retrofit.IOpenWeatherMap;
import com.example.astroweather.Retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


public class GeneralFragment extends Fragment {

    private Handler handlerTime = new Handler();

    String filename = "weatherGeneral.txt";

    ImageView imageWeather;
    TextView coordText, cityText, temperatureText, pressureText, descriptionText;

    LinearLayout weatherPanel;
    ProgressBar loading;

    IOpenWeatherMap mService;
    CompositeDisposable compositeDisposable;
    StringBuilder weatherInformation = new StringBuilder("");

    public GeneralFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        View itemView = inflater.inflate(R.layout.fragment_general, container, false);

        imageWeather = (ImageView) itemView.findViewById(R.id.imageWeather);
        cityText = (TextView) itemView.findViewById(R.id.cityText);
        coordText = (TextView) itemView.findViewById(R.id.coordText);
        temperatureText = (TextView) itemView.findViewById(R.id.temperatureText);
        pressureText = (TextView) itemView.findViewById(R.id.pressureText);
        descriptionText = (TextView) itemView.findViewById(R.id.descriptionText);

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
            setHasOptionsMenu(true);//jest net
            Log.d("xxx", "Internet Connection!");

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

                            //Load image
                            Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/")
                                    .append(weatherResult.getWeather().get(0).getIcon())
                                    .append(".png").toString()).into(imageWeather);

                            //Load info
                            cityText.setText(weatherResult.getName());

                            descriptionText.setText(new StringBuilder(weatherResult.getWeather().get(0).getDescription()));

                            if(unit.equals("metric")) {
                                temperatureText.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getTemp()))
                                        .append("°C"));
                            } else {
                                temperatureText.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getTemp()))
                                        .append("°F"));
                            }

                            pressureText.setText(new StringBuilder(String.valueOf(weatherResult.getMain().getPressure()))
                                    .append(" hPa").toString());

                            coordText.setText(new StringBuilder("").append(weatherResult.getCoord().toString()).append("").toString());

                            weatherInformation
                                    .append(",")
                                    .append(cityText.getText()).append(",")
                                    .append(descriptionText.getText()).append(",")
                                    .append(temperatureText.getText()).append(",")
                                    .append(pressureText.getText()).append(",")
                                    .append(coordText.getText()).append(",");

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
            setHasOptionsMenu(false);
            //nie ma neta
            /*Log.d("XXX", "No Internet connection!");
            Toast toast = Toast.makeText(getActivity(), "No Internet connection!\nWeather may be outdated!", Toast.LENGTH_LONG);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message); if( v != null) v.setGravity(Gravity.CENTER); toast.show();*/

            String result = ReadWriteClass.readFromFile((MainActivity)getActivity(), filename);
            if(result.isEmpty()) {
                Toast.makeText(getActivity(), "Data is not full!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("SSS", result);
                loading.setVisibility(View.GONE);
                weatherPanel.setVisibility(View.VISIBLE);
                String[] informationList = result.split(",");

                cityText.setText(informationList[1]);
                descriptionText.setText(informationList[2]);
                temperatureText.setText(informationList[3]);
                pressureText.setText(informationList[4]);
                coordText.setText(informationList[5]);
            }

        }

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

    private final Runnable updateTime = new Runnable() {
        @Override
        public void run() {
            TextView clock = (TextView) getView().findViewById(R.id.timeText);
            long clockTime = System.currentTimeMillis();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE dd.MM HH:mm:ss");
            String dateString = simpleDateFormat.format(clockTime);
            clock.setText(dateString);
            handlerTime.postDelayed(this, 1000);
        }
    };

    @Override
    public void onStop() {
        super.onStop();
        handlerTime.removeCallbacks(updateTime);
    }

    @Override
    public void onStart() {
        super.onStart();
        handlerTime.postDelayed(updateTime, 1);
    }
}
