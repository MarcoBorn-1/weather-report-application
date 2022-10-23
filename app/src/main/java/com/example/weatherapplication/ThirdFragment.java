package com.example.weatherapplication;

import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class ThirdFragment extends Fragment {
    ArrayList<Map<String, String>> weatherData;
    TextView time1;
    TextView time2;
    TextView time3;
    TextView time4;
    TextView time5;
    TextView time6;

    TextView temp1;
    TextView temp2;
    TextView temp3;
    TextView temp4;
    TextView temp5;
    TextView temp6;

    ImageView image1;
    ImageView image2;
    ImageView image3;
    ImageView image4;
    ImageView image5;
    ImageView image6;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_third, container, false);

        MainActivity mainActivity = (MainActivity) getActivity();
        try {
            weatherData = mainActivity.getAllWeatherData();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }

        SharedPreferences sharedPreferences = mainActivity.getApplicationContext().getSharedPreferences("SHARED_PREF", 0);
        String degreeMeasurement = sharedPreferences.getString("temperature-measurement", "Celsius");



        time1 = view.findViewById(R.id.weather1Time);
        time2 = view.findViewById(R.id.weather2Time);
        time3 = view.findViewById(R.id.weather3Time);
        time4 = view.findViewById(R.id.weather4Time);
        time5 = view.findViewById(R.id.weather5Time);
        time6 = view.findViewById(R.id.weather6Time);

        temp1 = view.findViewById(R.id.weather1Temp);
        temp2 = view.findViewById(R.id.weather2Temp);
        temp3 = view.findViewById(R.id.weather3Temp);
        temp4 = view.findViewById(R.id.weather4Temp);
        temp5 = view.findViewById(R.id.weather5Temp);
        temp6 = view.findViewById(R.id.weather6Temp);

        image1 = view.findViewById(R.id.weather1Image);
        image2 = view.findViewById(R.id.weather2Image);
        image3 = view.findViewById(R.id.weather3Image);
        image4 = view.findViewById(R.id.weather4Image);
        image5 = view.findViewById(R.id.weather5Image);
        image6 = view.findViewById(R.id.weather6Image);

        time1.setText(weatherData.get(0).get("date"));
        time2.setText(weatherData.get(1).get("date"));
        time3.setText(weatherData.get(2).get("date"));
        time4.setText(weatherData.get(3).get("date"));
        time5.setText(weatherData.get(4).get("date"));
        time6.setText(weatherData.get(5).get("date"));

        if (Objects.equals(degreeMeasurement, "Celsius")) {
            temp1.setText(weatherData.get(0).get("temperature-celsius") + " ℃");
            temp2.setText(weatherData.get(1).get("temperature-celsius") + " ℃");
            temp3.setText(weatherData.get(2).get("temperature-celsius") + " ℃");
            temp4.setText(weatherData.get(3).get("temperature-celsius") + " ℃");
            temp5.setText(weatherData.get(4).get("temperature-celsius") + " ℃");
            temp6.setText(weatherData.get(5).get("temperature-celsius") + " ℃");
        }
        else {
            temp1.setText(weatherData.get(0).get("temperature-fahrenheit") + " ℉");
            temp2.setText(weatherData.get(1).get("temperature-fahrenheit") + " ℉");
            temp3.setText(weatherData.get(2).get("temperature-fahrenheit") + " ℉");
            temp4.setText(weatherData.get(3).get("temperature-fahrenheit") + " ℉");
            temp5.setText(weatherData.get(4).get("temperature-fahrenheit") + " ℉");
            temp6.setText(weatherData.get(5).get("temperature-fahrenheit") + " ℉");
        }

        Picasso.get().load(weatherData.get(0).get("iconLink")).resize(200, 200).into(image1);
        Picasso.get().load(weatherData.get(1).get("iconLink")).resize(200, 200).into(image2);
        Picasso.get().load(weatherData.get(2).get("iconLink")).resize(200, 200).into(image3);
        Picasso.get().load(weatherData.get(3).get("iconLink")).resize(200, 200).into(image4);
        Picasso.get().load(weatherData.get(4).get("iconLink")).resize(200, 200).into(image5);
        Picasso.get().load(weatherData.get(5).get("iconLink")).resize(200, 200).into(image6);


        return view;
    }
}