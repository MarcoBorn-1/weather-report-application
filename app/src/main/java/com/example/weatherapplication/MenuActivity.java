package com.example.weatherapplication;

import static com.example.weatherapplication.MainActivity.readFile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MenuActivity extends AppCompatActivity {
    Set<String> cities;
    Spinner spinner;
    String chosenCity;
    Switch aSwitch;

    ArrayList<Map<String, String>> allWeatherData = new ArrayList<>();
    Map<String, String> currentWeatherData = new HashMap<String, String>();

    EditText addCityEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("SHARED_PREF", 0);
        cities = sharedPreferences.getStringSet("city-list", null);
        if (cities == null) {
            cities = new HashSet<>();
        }
        List<String> cityList = new ArrayList<>();
        cityList.addAll(cities);

        spinner = (Spinner) findViewById(R.id.spinner);
        addCityEditText = findViewById(R.id.cityAddEditText);
        chosenCity = sharedPreferences.getString("chosen-city", null);
        aSwitch = findViewById(R.id.switch1);

        String measurement = sharedPreferences.getString("temperature-measurement", "Celsius");

        aSwitch.setChecked(measurement.equals("Fahrenheit"));

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, cityList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setSelection((chosenCity == null) ? 0 : cityList.indexOf(chosenCity), false);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextSize(40);
                chosenCity = parent.getItemAtPosition(position).toString();
            }
            @Override
            public void onNothingSelected(AdapterView <?> parent) {
            }
        });
        spinner.setAdapter(arrayAdapter);

    }
    private final String API_URL = "http://api.openweathermap.org/data/2.5/forecast";
    private final String API_KEY = "bda6cb741830948c25423e7259b487ef";

    public void deleteCity(View view) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("SHARED_PREF", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> cityList = sharedPreferences.getStringSet("city-list", null);
        String mainCity = sharedPreferences.getString("chosen-city", null);
        if (cityList != null) {
            if (cityList.size() == 1) {
                Toast.makeText(getApplicationContext(), "You can't delete your last city!", Toast.LENGTH_SHORT).show();
                return;
            }
            cityList.remove(chosenCity);
            if (Objects.equals(chosenCity, mainCity)) {
                editor.putString("chosen-city", (String) cityList.toArray()[0]);
            }
            editor.putStringSet("city-list", cityList);
        }

        editor.apply();

        File file = new File(getFilesDir(), "my_filename");
        file.delete();

        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    public void returnToMain(View view) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("SHARED_PREF", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("chosen-city");
        editor.putString("chosen-city", chosenCity);
        editor.apply();
        finish();
        /*
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);*/
    }

    public void addNewCity(View view) {
        Editable city = addCityEditText.getText();
        String tempUrl = API_URL + "?q=" + city + "&appid=" + API_KEY;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, tempUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("SHARED_PREF", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
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
                    Toast.makeText(getApplicationContext(), "New city added: " + cityName, Toast.LENGTH_SHORT).show();

                    Set<String> set = sharedPreferences.getStringSet("city-list", null);
                    set.add(cityName);
                    editor.putStringSet("city-list", set);

                    editor.apply();
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "No such city, or no connection to API servers!", Toast.LENGTH_LONG).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    public void getWeatherData(View view) {
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
                            Toast.makeText(getApplicationContext(), "Weather saved.", Toast.LENGTH_SHORT).show();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Failed to acquire newest weather info. Check your internet connection.", Toast.LENGTH_SHORT).show();

                    }
                });
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(stringRequest);
            }
        }

    public void readSwitchValue(View view) {
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("SHARED_PREF", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (((Switch) findViewById(R.id.switch1)).isChecked()) {
            editor.putString("temperature-measurement", "Fahrenheit");
        }
        else {
            editor.putString("temperature-measurement", "Celsius");
        }
        editor.apply();
    }
}
