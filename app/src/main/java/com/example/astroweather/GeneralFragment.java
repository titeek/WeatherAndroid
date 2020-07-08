package com.example.astroweather;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
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

import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;


public class GeneralFragment extends Fragment {

    private Handler handlerTime = new Handler();


    ImageView imageWeather;
    TextView coordText, cityText, temperatureText, pressureText, descriptionText;

    LinearLayout weatherPanel;
    ProgressBar loading;

    IOpenWeatherMap mService;
    CompositeDisposable compositeDisposable;


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

        /*compositeDisposable.add(mService.getWeatherByLatLng(String.valueOf(((MainActivity)getActivity()).lat),
                 String.valueOf(((MainActivity)getActivity()).lon),*/

        //cos sie wali z current location...

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

                         loading.setVisibility(View.GONE);
                         weatherPanel.setVisibility(View.VISIBLE);




                     }
                 }, new Consumer<Throwable>() {
                     @Override
                     public void accept(Throwable throwable) throws Exception {
                         Toast.makeText(getActivity(), ""+throwable.getMessage(), Toast.LENGTH_LONG).show();
                     }
                 })

         );
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
        //showParameters(astroCalculator);
        handlerTime.postDelayed(updateTime, 1);
    }
}
