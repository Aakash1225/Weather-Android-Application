package com.example.weather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV , temperatureTV , conditionTV;
    private TextInputEditText cityEdt;
    private ImageView backIV;
    private ImageView iconIV;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private final int PERMISSION_CODE = 1;
    private String cityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); //application for fullscreen
        setContentView(R.layout.activity_main);

        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        RecyclerView weatherRV = findViewById(R.id.idRVWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIVBlack);
        iconIV = findViewById(R.id.idTVIcon);
        ImageView searchIV = findViewById(R.id.idIVSearch);
        weatherRVModelArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this,weatherRVModelArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION} , PERMISSION_CODE);
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        cityName = getCityName(location.getLongitude(),location.getLatitude());
        getWeatherInfo(cityName);

        searchIV.setOnClickListener(v -> {
            String city = Objects.requireNonNull(cityEdt.getText()).toString();
            if (city.isEmpty()) {
                android.widget.Toast.makeText(MainActivity.this, "Please enter city Name", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                cityNameTV.setText(cityName);
                getWeatherInfo(city);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                android.widget.Toast.makeText(this,"Permission Granted" , android.widget.Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude , double latitude){
        String cityName = "Not Found";
        Geocoder gcd = new Geocoder(getBaseContext() , Locale.getDefault());
        try{
            List<Address> addresses = gcd.getFromLocation(latitude , longitude , 10);
            Log.d("cityname", "getCityName: " + addresses);

            for(Address adr : addresses){
                if(adr!= null){
                    Log.d("on", "getCityName: " + adr);
                    String city = adr.getLocality();
                    Log.d("TAG", "getCityName: " + city);
                    if(city!=null && !city.equals("")){
                        cityName = city;

                    }else {
                        Log.d("TAG", "CITY NOT FOUND");
                        android.widget.Toast.makeText(this, "User City not Found...", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }

    private void getWeatherInfo(String cityName){
        String url = "https://api.weatherapi.com/v1/forecast.json?key=2b82884e90d04605b41183051222703&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
//        String temp1 = "https://www.google.com";
        Log.d("onCreate", "getWeatherInfo: " + cityName);

        cityNameTV.setText(cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);







        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            Log.d("json", "getWeatherInfo: " + response);
            loadingPB.setVisibility(View.GONE);
            homeRL.setVisibility(View.VISIBLE);
            weatherRVModelArrayList.clear();

            try {
                String temperature = response.getJSONObject("current").getString("temp_c");
                temperatureTV.setText(temperature + "°C");
                int isDay = response.getJSONObject("current").getInt("is_day");
                String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
//                Picasso.get().load("https:".concat(conditionIcon)).into(iconIV);
                conditionTV.setText(condition);

                if(isDay == 1){
                    // morning
                    Picasso.get().load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.lovethispic.com%2Fimage%2F121991%2Fgolden-sunny-day&psig=AOvVaw2okIq2YlN7EaGIQjjIxoc2&ust=1650363955928000&source=images&cd=vfe&ved=0CAkQjRxqFwoTCJCPpv2ynfcCFQAAAAAdAAAAABAD").into(backIV);
                }else{
                    //night
                    Picasso.get().load("https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQQxU7_N5I_DcEjEMKG2RfvUhxA-_cu7ix18A&usqp=CAU").into(backIV);
                }

                JSONObject forecastObj = response.getJSONObject("forecast");
                JSONObject forecast0 = forecastObj.getJSONArray("forecast").getJSONObject(0);
                JSONArray hourArray = forecast0.getJSONArray("hour");

                for(int i =0 ; i < hourArray.length() ; i++){
                    JSONObject hourObj = hourArray.getJSONObject(i);
                    String time = hourObj.getString("time");
                    String temp = hourObj.getString("temp_c");
                    String img = hourObj.getJSONObject("condition").getString("icon");
                    String wind = hourObj.getString("wind_kph");
                    weatherRVModelArrayList.add(new WeatherRVModel(time , temp , img , wind));
                }
                weatherRVAdapter.notifyDataSetChanged();

            } catch (JSONException e ){
                e.printStackTrace();
            }


        }, error -> android.widget.Toast.makeText(MainActivity.this , error.toString() , Toast.LENGTH_LONG).show()) {


//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> headers= new HashMap<>();
//                headers.put()
//            }
        };

        requestQueue.add(jsonObjectRequest);

    }
}