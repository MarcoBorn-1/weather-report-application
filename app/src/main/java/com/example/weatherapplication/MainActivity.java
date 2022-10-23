package com.example.weatherapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    ArrayList<Map<String, String>> allWeatherData = new ArrayList<>();
    Map<String, String> currentWeatherData = new HashMap<String, String>();


    ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
    ViewPager2 viewPager;
    Fragment firstFragment;
    Fragment secondFragment;
    Fragment thirdFragment;

    public Map<String, String> getCurrentWeatherData() {
        return currentWeatherData;
    }

    public ArrayList<Map<String, String>> getAllWeatherData() {
        return allWeatherData;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);
        getWeatherDetails(this);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        overridePendingTransition(0, 0);
        this.recreate();
        overridePendingTransition(0, 0);
    }

    // ?q={city name}&appid={API key}
    private final String API_URL = "http://api.openweathermap.org/data/2.5/forecast";
    private final String API_KEY = "bda6cb741830948c25423e7259b487ef";

    public void getWeatherDetails(FragmentActivity fragmentActivity) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("SHARED_PREF", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String chosenCity = "";
        if (!sharedPreferences.contains("chosen-city")) {
            editor.putString("chosen-city", "Warsaw");
            if (!sharedPreferences.contains("city-list")) {
                HashSet<String> set = new HashSet<>();
                set.add("Warsaw");
                editor.putStringSet("city-list", set);
            }
            else {
                chosenCity = sharedPreferences.getString("chosen-city", null);
                Set<String> set = sharedPreferences.getStringSet("city-list", new HashSet<>());
                if (!set.contains(chosenCity)) {
                    set.add(chosenCity);
                    editor.putStringSet("city-list", set);
                }
            }
            editor.apply();
        }

        if (!sharedPreferences.contains("temperature-measurement")) {
            editor.putString("temperature-measurement", "Celsius");
            editor.apply();
        }

        Set<String> cities = sharedPreferences.getStringSet("city-list", null);
/*
        // TODO: remove -> Adding new city
        cities.remove("Lodz");
        editor.putStringSet("city-list", cities);

        // TODO: remove -> Changing chosen city
        editor.remove("chosen-city");
        editor.putString("chosen-city", "Warsaw");
        editor.apply();
*/
        String finalChosenCity = sharedPreferences.getString("chosen-city", null);

        for (String city: cities) {
            String tempUrl = API_URL + "?q=" + city + "&appid=" + API_KEY;
            StringRequest stringRequest = new StringRequest(Request.Method.POST, tempUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        JSONObject cityInfo = jsonResponse.getJSONObject("city");
                        String cityName = cityInfo.getString("name");
                        FileOutputStream fos = getApplicationContext().openFileOutput(cityName, Context.MODE_PRIVATE);
                        Writer output = null;
                        File file = new File(getApplicationContext().getDataDir() + "/" + cityName);
                        System.out.println(getApplicationContext().getDataDir() + "/" + cityName);
                        output = new BufferedWriter(new FileWriter(file));
                        output.write(jsonResponse.toString());
                        output.close();

                        if (city.equals(finalChosenCity)) {
                            setWeatherDataAndEnableFragments(jsonResponse, fragmentActivity);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Failed to acquire newest weather info. Check your internet connection.", Toast.LENGTH_SHORT).show();
                    boolean found = false;
                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("SHARED_PREF", 0);
                    String chosenCity = sharedPreferences.getString("chosen-city", null);
                    if (chosenCity != null) {
                        try {
                            String jsonString = readFile(getApplicationContext().getDataDir() + "/" + chosenCity, StandardCharsets.UTF_8);
                            System.out.println(jsonString);
                            JSONObject jsonObject = new JSONObject(jsonString);
                            setWeatherDataAndEnableFragments(jsonObject, fragmentActivity);
                            found = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!found){
                        // If there's no connection, and no data on device, shows this fragment

                        firstFragment = new NoConnectionFragment();
                        fragmentArrayList.add(firstFragment);

                        viewPager = findViewById(R.id.viewPager);
                        FragmentAdapter fragmentAdapter = new FragmentAdapter(fragmentActivity, fragmentArrayList);

                        viewPager.setAdapter(fragmentAdapter);
                    }
                }
            });
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
        }
    }

    public void setWeatherDataAndEnableFragments(JSONObject jsonResponse, FragmentActivity fragmentActivity) {
        try {
            DecimalFormat df = new DecimalFormat("#.##");
            JSONArray list = jsonResponse.getJSONArray("list");

            // Current weather
            for (int i = 0; i < 7; i++) {
                Map<String, String> weatherData = new HashMap<String, String>();
                JSONObject jsonCurrent = list.getJSONObject(i*2);
                String date = convertDate(jsonCurrent.getString("dt_txt"));
                weatherData.put("date", date);

                JSONObject jsonMain = jsonCurrent.getJSONObject("main");
                String temperature = df.format(jsonMain.getDouble("temp") - 272.15);
                String pressure = jsonMain.getString("pressure");
                String humidity = jsonMain.getString("humidity");
                weatherData.put("pressure", pressure);
                weatherData.put("temperature-celsius", temperature);
                weatherData.put("temperature-fahrenheit", df.format((((jsonMain.getDouble("temp") - 272.15) * 9) / 5) + 32));
                weatherData.put("humidity", humidity  + "%");

                // Weather descriptor

                JSONArray jsonWeatherArr = jsonCurrent.getJSONArray("weather");
                JSONObject jsonWeather = jsonWeatherArr.getJSONObject(0);

                String mainDesc = jsonWeather.getString("main");
                String additionalDesc = jsonWeather.getString("description");
                weatherData.put("mainDescription", mainDesc);
                weatherData.put("additionalDescription", additionalDesc);
                weatherData.put("iconLink", "https://openweathermap.org/img/wn/" + jsonWeather.getString("icon") + ".png");

                // Wind speed

                JSONObject jsonWind = jsonCurrent.getJSONObject("wind");
                String speed = jsonWind.getString("speed");
                weatherData.put("windSpeed", speed + " km/h");

                String degree = jsonWind.getString("deg");
                weatherData.put("windDegree", getWindDirection(degree));

                // Visibility

                String visibility = Double.toString(jsonCurrent.getDouble("visibility") / 100);
                weatherData.put("visibility", visibility + "%");

                // City informations (name, coordinates, sunrise, sunset)

                JSONObject cityInfo = jsonResponse.getJSONObject("city");
                String city = cityInfo.getString("name");
                String country = cityInfo.getString("country");
                String timezone = cityInfo.getString("timezone");
                weatherData.put("city", city);
                weatherData.put("country", country);
                weatherData.put("timezone", timezone);

                JSONObject jsonCoordinates = cityInfo.getJSONObject("coord");
                String coordinates =
                        "Latitude: " + jsonCoordinates.getString("lat") +
                                "\nLongitude: " + jsonCoordinates.getString("lon");
                weatherData.put("coordinates", coordinates);

                if (i == 0) {
                    currentWeatherData = weatherData;
                }
                else {
                    allWeatherData.add(weatherData);
                }
            }
            // Adding fragments

            firstFragment = new FirstFragment();
            secondFragment = new SecondFragment();
            thirdFragment = new ThirdFragment();

            fragmentArrayList.add(firstFragment);
            fragmentArrayList.add(secondFragment);
            fragmentArrayList.add(thirdFragment);

            viewPager = findViewById(R.id.viewPager);
            FragmentAdapter fragmentAdapter = new FragmentAdapter(fragmentActivity, fragmentArrayList);

            viewPager.setAdapter(fragmentAdapter);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getWindDirection(String degree) {
        int degrees = Integer.parseInt(degree);

        if (degrees >= 0 && degrees < 22.5) return "North";
        else if (degrees >= 22.5 && degrees < (3*22.5)) return "North-East";
        else if (degrees >= (3*22.5) && degrees < (5*22.5)) return "East";
        else if (degrees >= (5*22.5) && degrees < (7*22.5)) return "South-East";
        else if (degrees >= (7*22.5) && degrees < (9*22.5)) return "South";
        else if (degrees >= (9*22.5) && degrees < (11*22.5)) return "South-West";
        else if (degrees >= (11*22.5) && degrees < (13*22.5)) return "West";
        else if (degrees >= (13*22.5) && degrees < (15*22.5)) return "North-West";
        else return "North";
    }

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public void openMenu(View view) {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
    }

    public String convertDate(String oldDate) {
        // old format: 2022-05-18 18:00:00
        // new format: 18.05, 18:00
        String date = "";
        date += oldDate.substring(8,10);
        date += ".";
        date += oldDate.substring(5,7);
        date += ", ";
        date += oldDate.substring(11, 16);
        return date;
    }
}