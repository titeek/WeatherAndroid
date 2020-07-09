package com.example.astroweather;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.astroweather.Common.Common;
import com.example.astroweather.Model.WeatherForecastResult;
import com.example.astroweather.Model.WeatherResult;
import com.example.astroweather.Retrofit.IOpenWeatherMap;
import com.example.astroweather.Retrofit.RetrofitClient;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class NextDaysFragment extends Fragment {

    private StringBuilder weatherInformation = new StringBuilder("");
    private String filename = "weatherForecast.txt";
    private String directory;
    private String filenameImg1 = "weather1.jpg";
    private String filenameImg2 = "weather2.jpg";
    private String filenameImg3 = "weather3.jpg";
    private String filenameImg4 = "weather4.jpg";
    private String path = "/data/user/0/com.example.astroweather/app_weatherImages/";

    ImageView nextDayOneWeatherImage, nextDayTwoWeatherImage, nextDayThreeWeatherImage, nextDayFourWeatherImage;
    TextView nextDayOneText, nextDayTwoText, nextDayThreeText, nextDayFourText;
    TextView nextDayOneTempText, nextDayTwoTempText, nextDayThreeTempText, nextDayFourTempText;

    LinearLayout weatherPanel;
    ProgressBar loading;

    IOpenWeatherMap mService;
    CompositeDisposable compositeDisposable;


    public NextDaysFragment() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View itemView = inflater.inflate(R.layout.fragment_next_days, container, false);

        nextDayOneWeatherImage = (ImageView) itemView.findViewById(R.id.nextDayOneWeatherImage);
        nextDayTwoWeatherImage = (ImageView) itemView.findViewById(R.id.nextDayTwoWeatherImage);
        nextDayThreeWeatherImage = (ImageView) itemView.findViewById(R.id.nextDayThreeWeatherImage);
        nextDayFourWeatherImage = (ImageView) itemView.findViewById(R.id.nextDayFourWeatherImage);

        nextDayOneText = (TextView) itemView.findViewById(R.id.nextDayOneText);
        nextDayTwoText = (TextView) itemView.findViewById(R.id.nextDayTwoText);
        nextDayThreeText = (TextView) itemView.findViewById(R.id.nextDayThreeText);
        nextDayFourText = (TextView) itemView.findViewById(R.id.nextDayFourText);

        nextDayOneTempText = (TextView) itemView.findViewById(R.id.nextDayOneTempText);
        nextDayTwoTempText = (TextView) itemView.findViewById(R.id.nextDayTwoTempText);
        nextDayThreeTempText = (TextView) itemView.findViewById(R.id.nextDayThreeTempText);
        nextDayFourTempText = (TextView) itemView.findViewById(R.id.nextDayFourTempText);

        weatherPanel = (LinearLayout) itemView.findViewById(R.id.weatherPanel);
        loading = (ProgressBar) itemView.findViewById(R.id.loading);

        getForecastWeatherInformation(((MainActivity)getActivity()).unit);

        return itemView;
    }


    private void getForecastWeatherInformation(final String unit) {

        Context context = (MainActivity)getActivity();

        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            setHasOptionsMenu(true);
            compositeDisposable.add(mService.getForecastWeatherByLatLng(
                    String.valueOf(((MainActivity)getActivity()).lat),
                    String.valueOf(((MainActivity)getActivity()).lon),
                    Common.APP_ID,
                    unit)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<WeatherForecastResult>() {
                        @Override
                        public void accept(WeatherForecastResult weatherForecastResult) throws Exception {


                            //Load info
                            displayWeatherForecast(weatherForecastResult, unit);

                            weatherInformation
                                    .append(",")
                                    .append(nextDayOneTempText.getText()).append(",")
                                    .append(nextDayTwoTempText.getText()).append(",")
                                    .append(nextDayThreeTempText.getText()).append(",")
                                    .append(nextDayFourTempText.getText()).append(",")
                                    .append(nextDayOneText.getText()).append(",")
                                    .append(nextDayTwoText.getText()).append(",")
                                    .append(nextDayThreeText.getText()).append(",")
                                    .append(nextDayFourText.getText()).append(",");

                            //Log.d("XXX", weatherInformation.toString());

                            ReadWriteClass.writeToFile(weatherInformation.toString(), (MainActivity)getActivity(), filename);


                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            //Toast.makeText(getActivity(), ""+throwable.getMessage(), Toast.LENGTH_LONG).show();
                            Log.d("xxx", ""+throwable.getMessage());


                        }
                    })

            );

        } else {
            setHasOptionsMenu(false);
            /*Log.d("XXX", "No Internet connection!");
            Toast toast = Toast.makeText(getActivity(), "No Internet connection!\nWeather may be outdated!", Toast.LENGTH_SHORT);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message); if( v != null) v.setGravity(Gravity.CENTER); toast.show();*/

            String result = ReadWriteClass.readFromFile((MainActivity)getActivity(), filename);
            if(result.isEmpty()) {
                Toast.makeText(getActivity(), "Data is not full!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("SSS", result);
                loading.setVisibility(View.GONE);
                weatherPanel.setVisibility(View.VISIBLE);
                String[] informationList = result.split(",");

                nextDayOneTempText.setText(informationList[1]);
                nextDayTwoTempText.setText(informationList[2]);
                nextDayThreeTempText.setText(informationList[3]);
                nextDayFourTempText.setText(informationList[4]);
                nextDayOneText.setText(informationList[5]);
                nextDayTwoText.setText(informationList[6]);
                nextDayThreeText.setText(informationList[7]);
                nextDayFourText.setText(informationList[8]);

                try {
                    File f = new File(path, filenameImg1);
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                    nextDayOneWeatherImage.setImageBitmap(b);

                    f = new File(path, filenameImg2);
                    b = BitmapFactory.decodeStream(new FileInputStream(f));
                    nextDayTwoWeatherImage.setImageBitmap(b);

                    f = new File(path, filenameImg3);
                    b = BitmapFactory.decodeStream(new FileInputStream(f));
                    nextDayThreeWeatherImage.setImageBitmap(b);

                    f = new File(path, filenameImg4);
                    b = BitmapFactory.decodeStream(new FileInputStream(f));
                    nextDayFourWeatherImage.setImageBitmap(b);
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }



        }

    }

    private void displayWeatherForecast(WeatherForecastResult weatherForecastResult, final String unit) {

        //Load image
        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/")
                .append(weatherForecastResult.daily.get(1).weather.get(0).getIcon())
                .append(".png").toString()).into(nextDayOneWeatherImage);

        BitmapDrawable drawable1 = (BitmapDrawable) nextDayOneWeatherImage.getDrawable();
        Bitmap bitmap1 = drawable1.getBitmap();

        directory = ReadWriteClass.saveToInternalStorage((MainActivity)getActivity(), bitmap1, filenameImg1);


        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/")
                .append(weatherForecastResult.daily.get(2).weather.get(0).getIcon())
                .append(".png").toString()).into(nextDayTwoWeatherImage);

        BitmapDrawable drawable2 = (BitmapDrawable) nextDayTwoWeatherImage.getDrawable();
        Bitmap bitmap2 = drawable2.getBitmap();

        directory = ReadWriteClass.saveToInternalStorage((MainActivity)getActivity(), bitmap2, filenameImg2);

        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/")
                .append(weatherForecastResult.daily.get(3).weather.get(0).getIcon())
                .append(".png").toString()).into(nextDayThreeWeatherImage);

        BitmapDrawable drawable3 = (BitmapDrawable) nextDayThreeWeatherImage.getDrawable();
        Bitmap bitmap3 = drawable3.getBitmap();

        directory = ReadWriteClass.saveToInternalStorage((MainActivity)getActivity(), bitmap3, filenameImg3);


        Picasso.get().load(new StringBuilder("https://openweathermap.org/img/wn/")
                .append(weatherForecastResult.daily.get(4).weather.get(0).getIcon())
                .append(".png").toString()).into(nextDayFourWeatherImage);

        BitmapDrawable drawable4 = (BitmapDrawable) nextDayFourWeatherImage.getDrawable();
        Bitmap bitmap4 = drawable4.getBitmap();

        directory = ReadWriteClass.saveToInternalStorage((MainActivity)getActivity(), bitmap4, filenameImg4);

        Log.d("DIR", directory);

        if(unit.equals("metric")) {
            nextDayOneTempText.setText(new StringBuilder(String.valueOf(weatherForecastResult.daily.get(1).temp.day))
                    .append("°C"));

            nextDayTwoTempText.setText(new StringBuilder(String.valueOf(weatherForecastResult.daily.get(2).temp.day))
                    .append("°C"));

            nextDayThreeTempText.setText(new StringBuilder(String.valueOf(weatherForecastResult.daily.get(3).temp.day))
                    .append("°C"));

            nextDayFourTempText.setText(new StringBuilder(String.valueOf(weatherForecastResult.daily.get(4).temp.day))
                    .append("°C"));
        } else {
            nextDayOneTempText.setText(new StringBuilder(String.valueOf(weatherForecastResult.daily.get(1).temp.day))
                    .append("°F"));

            nextDayTwoTempText.setText(new StringBuilder(String.valueOf(weatherForecastResult.daily.get(2).temp.day))
                    .append("°F"));

            nextDayThreeTempText.setText(new StringBuilder(String.valueOf(weatherForecastResult.daily.get(3).temp.day))
                    .append("°F"));

            nextDayFourTempText.setText(new StringBuilder(String.valueOf(weatherForecastResult.daily.get(4).temp.day))
                    .append("°F"));
        }

        nextDayOneText.setText(Common.convertUnixToDate(weatherForecastResult.daily.get(1).dt));
        nextDayTwoText.setText(Common.convertUnixToDate(weatherForecastResult.daily.get(2).dt));
        nextDayThreeText.setText(Common.convertUnixToDate(weatherForecastResult.daily.get(3).dt));
        nextDayFourText.setText(Common.convertUnixToDate(weatherForecastResult.daily.get(4).dt));


        loading.setVisibility(View.GONE);
        weatherPanel.setVisibility(View.VISIBLE);


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
            getForecastWeatherInformation(((MainActivity)getActivity()).unit);
        }
        else if((item.getItemId() == R.id.tempC) && (((MainActivity)getActivity()).unit.equals("imperial")))
        {
            Toast.makeText(getActivity(), "Units updated - Metric", Toast.LENGTH_SHORT).show();
            ((MainActivity)getActivity()).unit = "metric";
            getForecastWeatherInformation(((MainActivity)getActivity()).unit);
        }
        else if((item.getItemId() == R.id.tempF) && (((MainActivity)getActivity()).unit.equals("metric")))
        {
            Toast.makeText(getActivity(), "Units updated - Imperial", Toast.LENGTH_SHORT).show();
            ((MainActivity)getActivity()).unit = "imperial";
            getForecastWeatherInformation(((MainActivity)getActivity()).unit);
        }
        else if(item.getItemId() == R.id.city) {
            Intent favCity = new Intent(this.getActivity(), FavCities.class);
            startActivity(favCity);
        }

        return true;
    }
}
