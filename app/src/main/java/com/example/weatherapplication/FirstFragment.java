package com.example.weatherapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

public class FirstFragment extends Fragment {
    Map<String, String> weatherData;
    TextClock clock;
    TextView city;
    TextView temperature;
    TextView pressure;
    TextView description;
    TextView coordinates;
    TextView date;
    ImageView image;
    Button button;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);
        clock = view.findViewById(R.id.textClock);
        city = view.findViewById(R.id.cityTextView);
        temperature = view.findViewById(R.id.tempTextView);
        pressure = view.findViewById(R.id.pressureTextView);
        description = view.findViewById(R.id.descTextView);
        coordinates = view.findViewById(R.id.coordTextView);
        image = view.findViewById(R.id.imageView);
        date = view.findViewById(R.id.dateTextView);

        MainActivity mainActivity = (MainActivity) getActivity();
        try {
            weatherData = mainActivity.getCurrentWeatherData();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPreferences = mainActivity.getApplicationContext().getSharedPreferences("SHARED_PREF", 0);
        String degreeMeasurement = sharedPreferences.getString("temperature-measurement", "Celsius");

        if (Objects.equals(degreeMeasurement, "Celsius")) {
            temperature.setText("Temperature: " + weatherData.get("temperature-celsius") + " ℃");
        }
        else {
            temperature.setText("Temperature: " + weatherData.get("temperature-fahrenheit") + " ℉");
        }

        date.setText(weatherData.get("date"));
        clock.setTimeZone(getTimeZone(weatherData.get("timezone")));
        city.setText(weatherData.get("city") + " (" + weatherData.get("country") + ")");
        coordinates.setText(weatherData.get("coordinates"));
        pressure.setText("Pressure: " + weatherData.get("pressure") + " hPa");
        description.setText(weatherData.get("mainDescription") + '\n' + weatherData.get("additionalDescription"));
        Picasso.get().load(weatherData.get("iconLink")).into(image);
        return view;
    }

    public String getTimeZone(String seconds_str) {
        int offset = Integer.parseInt(seconds_str);
        TimeZone tz = TimeZone.getDefault();
        String[] availableIDs = tz.getAvailableIDs(offset * 1000);
        return availableIDs[0];
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }




}