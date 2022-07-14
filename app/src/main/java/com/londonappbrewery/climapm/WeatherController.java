package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    // Constants:
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "179086309e058ba8f5debebde6a45f96";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;
    final int REQUEST_CODE = 123;

    // TODO: Set LOCATION_PROVIDER here:
    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;
                                          //Allocating the Location Provider with the help of Location Manager


    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;

    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);


        // TODO: Add an OnClickListener to the changeCityButton here:
        changeCityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                                 // Declaring the Intent message object for Loading the Change City Layout
                Intent myIntent = new Intent(WeatherController.this, ChangeCityController.class);
                startActivity(myIntent);
                                               //Staring the new layout activity
            }
        });

    }


    // TODO: Add onResume() here:
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Clima", "onResume() has called");

        Intent myIntent = getIntent();                                // Getting the new cuty Intent with the help of Extra function
        String city = myIntent.getStringExtra("City");

        if(city != null) {
            Log.d("Clima", "Getting weather for new City");
            getWeatherForNewCity(city);
        } else {
            Log.d("Clima", "Getting weather for current location");
            getWeatherForCurrentLocation();
        }
    }


    // TODO: Add getWeatherForNewCity(String city) here:
  private void  getWeatherForNewCity(String city) {
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);
        letsDoSomeNetworking(params);

    };


    // TODO: Add getWeatherForCurrentLocation() here:
    protected void getWeatherForCurrentLocation() {

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {                     // Event of Location Listener called during Change of location and retriving the LAT & LONG
                Log.d("Clima", "onLocationChanged() callback received");

               String latitude = String.valueOf(location.getLatitude());
                String longitude = String.valueOf(location.getLongitude());

                Log.d("Clima", "Longitude is "+ longitude);
                Log.d("Clima", "Latitude is "+ latitude);

                RequestParams params = new RequestParams();  // Creating a parameters Object to store the LAT, LONG & appId
                params.put("lat", latitude);                                        // and passing them to the Networking Method
                params.put("lon", longitude);
                params.put("appId", APP_ID);
                letsDoSomeNetworking(params);


            }

            @Override                                                //Events on Location Listerner during individual calls
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Log statements to help you debug your app.
                Log.d("Clima", "onStatusChanged() callback received. Status: " + status);
                Log.d("Clima", "2 means AVAILABLE, 1: TEMPORARILY_UNAVAILABLE, 0: OUT_OF_SERVICE");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Clima", "onProviderEnabled() callback received. Provider: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Clima", "onProviderDisabled() callback received. Provider: " + provider);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE );
            return;
        }

        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {                                      // method() called after Permission result given by the user and work accordingly as the result

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Clima", "onRequestPermissionResult() : Permission Granted!");
                getWeatherForCurrentLocation();
            } else {
                Log.d("Clima", "Permission Denied : ()");
            }
        }
    }
    // TODO: Add letsDoSomeNetworking(RequestParams params) here:
    private void letsDoSomeNetworking(RequestParams params) {                       // Method for getting the weather from the Internet website

        AsyncHttpClient client = new AsyncHttpClient();        // Client Object for getting the Weather Info
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler() {

            @Override                                                                                                         //JSON is the added dependency for getting a easy work with already written programs(see build.gradle Module)
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("Clima", "Success! JSON:  "+ response.toString());

                updateUI(response);    //calling the method with the Object Variable containing all the infos of weather condition

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {
                Log.d("Clima", "Fail! "+ e.toString());
                Log.d("Clima", "Status Code: "+ statusCode);
                Toast.makeText(WeatherController.this, "Request Failure!", Toast.LENGTH_SHORT).show();
            }
        });
    }



    // TODO: Add updateUI() here:
    private void updateUI(JSONObject jObj) {                                   //Method for Updating the layout for the User to view

        WeatherDataModel weatherData = WeatherDataModel.fromJson(jObj);   //calling the fromJson()  method containing the whole retrived values from the website(without calling a Constructor)
        mTemperatureLabel.setText(weatherData.getTemperature());
        mCityLabel.setText(weatherData.getCity());

        int resourceID = getResources().getIdentifier(weatherData.getIconName(), "drawable", getPackageName());
        mWeatherImage.setImageResource(resourceID);
    }
    



    // TODO: Add onPause() here:
    @Override
    protected void onPause() {
        super.onPause();

        if(mLocationManager != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }



}
