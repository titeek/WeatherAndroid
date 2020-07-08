package com.example.astroweather;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.astroweather.Common.Common;
import com.example.astroweather.Model.WeatherResult;
import com.example.astroweather.Retrofit.IOpenWeatherMap;
import com.example.astroweather.Retrofit.RetrofitClient;


import java.util.ArrayList;


import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class FavCities extends AppCompatActivity {

    DatabaseHelper myDb;

    Button buttonAdd;
    Button buttonView;
    Button buttonDelete;
    EditText addCitiesText;

    IOpenWeatherMap mService;
    CompositeDisposable compositeDisposable;

    ListView listViewCities;
    ArrayList<String> arrayList;
    ArrayAdapter arrayAdapter;

    boolean delete = false;

    public FavCities() {
        compositeDisposable = new CompositeDisposable();
        Retrofit retrofit = RetrofitClient.getInstance();
        mService = retrofit.create(IOpenWeatherMap.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav_cities);

        myDb = new DatabaseHelper(this);
        listViewCities = (ListView) findViewById(R.id.listViewCities);
        buttonAdd = (Button) findViewById(R.id.buttonAdd);
        addCitiesText = (EditText) findViewById(R.id.addCitiesText);
        buttonView = (Button) findViewById(R.id.buttonView);
        buttonDelete = (Button) findViewById(R.id.buttonDelete);
        arrayList = new ArrayList<>();


        listViewCities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = myDb.getAllData();
                String city;

                if(!delete){
                    city = (String) arrayAdapter.getItem(position);
                    Log.d("xxx", ""+city);

                    Toast.makeText(FavCities.this, "City: " + arrayAdapter.getItem(position),Toast.LENGTH_SHORT).show();

                    while(cursor.moveToNext()) {

                        if(cursor.getString(1).equals(city)){
                            MainActivity.lat = Double.parseDouble(cursor.getString(2));
                            MainActivity.lon = Double.parseDouble(cursor.getString(3));
                            break;
                        }
                    }

                    //Toast.makeText(FavCities.this, "City: " + cursor.getString(1),Toast.LENGTH_SHORT).show();

                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(i);
                } else {
                    city = (String) arrayAdapter.getItem(position);

                    while(cursor.moveToNext()) {

                        if(cursor.getString(1).equals(city)){
                            Integer deletedRows = myDb.deleteData(cursor.getString(0));

                            if(deletedRows > 0) {
                                Toast.makeText(FavCities.this, cursor.getString(1) + " deleted!", Toast.LENGTH_SHORT).show();
                                delete = false;
                                showCities();
                            } else {
                                Toast.makeText(FavCities.this, "Error!", Toast.LENGTH_SHORT).show();
                            }

                            break;
                        }
                    }

                            /*Integer deletedID = Integer.parseInt(cursor.getString(0));
                            int pos = cursor.getPosition();

                            while(cursor.moveToNext()) {
                                myDb.updateBlank(String.valueOf(deletedID), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                                deletedID++;
                            }

                            cursor.moveToPosition(pos);

                            //myDb.updateBlank(cursor.getString(0), "", "0", "0");
                            //changeIDs(Integer.parseInt(cursor.getString(0)));*/
                }

            }
        });

        showCities();
        addData();
        viewAll();
        deleteDataById();
    }

    private void changeIDs(Integer ID) {
        Cursor cursor = myDb.getAllData();

        while(cursor.moveToNext()) {
            Integer IDnow = Integer.parseInt(cursor.getString(0));
            if(IDnow >= ID) {
                cursor.moveToNext();
                myDb.updateBlank(String.valueOf(IDnow), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                cursor.moveToPrevious();
            }
        }
    }

    public void showCities() {
        Cursor cursor = myDb.getAllData();
        arrayList.clear();

        if(cursor.getCount() == 0) {
            return;
        }

        while(cursor.moveToNext()) {
            arrayList.add(cursor.getString(1));
        }

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, arrayList);
        listViewCities.setAdapter(arrayAdapter);
    }

    public void deleteDataById() {
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete = true;
            }
        });
    }

    public void viewAll() {
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = myDb.getAllData();
                if(cursor.getCount() == 0) {
                    showMessage("Error", "Nothing to show!");
                    return;
                }

                StringBuffer stringBuffer = new StringBuffer();
                while(cursor.moveToNext()) {
                    stringBuffer.append("ID: " + cursor.getString(0)+"\n");
                    stringBuffer.append("City: " + cursor.getString(1)+"\n");
                    stringBuffer.append("Lat: " + cursor.getString(2)+"\n");
                    stringBuffer.append("Lon: " + cursor.getString(3)+"\n\n");
                }

                showMessage("Cities", stringBuffer.toString());
            }
        });
    }

    public void showMessage(String title, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.show();
    }

    public void addData() {
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String cityName = addCitiesText.getText().toString();
                getCoords(cityName);
                addCitiesText.setText("");
            }
        });

    }

    private void getCoords(final String cityName) {
        compositeDisposable.add(mService.getWeatherByCity(
                cityName,
                Common.APP_ID,
                "metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResult>() {
                    @Override
                    public void accept(WeatherResult weatherResult) throws Exception {

                        boolean isInserted = myDb.insertData(
                                cityName,
                                String.valueOf(weatherResult.getCoord().getLat()),
                                String.valueOf(weatherResult.getCoord().getLon()));

                        if(isInserted) {
                            Toast.makeText(FavCities.this, "City added!", Toast.LENGTH_SHORT).show();
                            showCities();
                        } else {
                            Toast.makeText(FavCities.this, "Error!", Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                    }
                })

        );
    }

}
