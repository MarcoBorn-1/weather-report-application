package com.example.weatherapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

public class SecondFragment extends Fragment {
    Map<String, String> weatherData;

    TextView cityName;
    TextView windSpeed;
    TextView windDirection;
    TextView humidity;
    TextView visibility;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_second, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        try {
            weatherData = mainActivity.getCurrentWeatherData();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }

        cityName = view.findViewById(R.id.cityTextView2);
        windSpeed = view.findViewById(R.id.windSpeedTextView);
        windDirection = view.findViewById(R.id.windDirectionTextView);
        humidity = view.findViewById(R.id.humidityTextView);
        visibility = view.findViewById(R.id.visibilityTextView);

        cityName.setText(weatherData.get("city") + " (" + weatherData.get("country") + ")");
        windSpeed.setText(weatherData.get("windSpeed"));
        windDirection.setText(weatherData.get("windDegree"));
        humidity.setText(weatherData.get("humidity"));
        visibility.setText(weatherData.get("visibility"));

        return view;
    }
}