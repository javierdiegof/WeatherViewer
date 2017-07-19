package com.cursoslicad.android.weatherviewer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class WeatherActivity extends AppCompatActivity {
    private List<Weather> weatherList = new ArrayList<>();
    private WeatherAdapter weatherAdapter;
    private RecyclerView weatherRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        weatherRecyclerView = (RecyclerView) findViewById(R.id.weatherRecyclerView);
        weatherAdapter = new WeatherAdapter(WeatherActivity.this ,weatherList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        weatherRecyclerView.setLayoutManager(linearLayoutManager);
        weatherRecyclerView.setAdapter(weatherAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(weatherRecyclerView.getContext(),
                linearLayoutManager.getOrientation());
        weatherRecyclerView.addItemDecoration(dividerItemDecoration);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText locationEditText = (EditText) findViewById(R.id.locationEditText);
                URL url = createURL(locationEditText.getText().toString());
                if(url != null){
                    dismissKeyboard(locationEditText);
                    GetWeatherTask getLocalWeatherTask = new GetWeatherTask();
                    getLocalWeatherTask.execute(url);
                }
                else{
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            R.string.invalid_url, Snackbar.LENGTH_LONG).show();
                }

            }
        });
    }

    private URL createURL(String city){
        String apiKey = getString(R.string.api_key);
        String baseUrl = getString(R.string.web_service_url);
        // Método para dar formato de web
        // Se reemplazan algunos caracteres como " " en "+"
        try{
            String urlString = baseUrl + URLEncoder.encode(city, "UTF-8") +
                    "&units=metric&cnt=16&APPID=" + apiKey;
            return new URL(urlString);
        }
        // Arroja UnsupportedEncodingException y MalformedURLException
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void dismissKeyboard(View view){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    private class GetWeatherTask extends AsyncTask<URL, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(URL... params){
            HttpURLConnection connection = null;

            try{
                connection = (HttpURLConnection) params[0].openConnection();
                int response = connection.getResponseCode();

                if(response == HttpURLConnection.HTTP_OK){
                    StringBuilder builder = new StringBuilder();
                    try(BufferedReader reader = new BufferedReader((
                            new InputStreamReader(connection.getInputStream())))){

                        String line;
                        while((line = reader.readLine()) != null){
                            builder.append(line);
                        }
                    }
                    catch(IOException ioe){
                        Snackbar.make(findViewById(R.id.coordinatorLayout),R.string.read_error,
                                Snackbar.LENGTH_LONG).show();
                    }
                    return new JSONObject(builder.toString());
                }
                else{
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            R.string.connect_error, Snackbar.LENGTH_SHORT).show();
                }

            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally{
                connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject weather) {
            if (weather == null) {
                Snackbar.make(findViewById(R.id.coordinatorLayout),
                        R.string.connect_error, Snackbar.LENGTH_LONG).show();
                return;
            } else {
                convertJSONtoArrayList(weather);
                weatherAdapter.notifyDataSetChanged();
                weatherRecyclerView.smoothScrollToPosition(0);
            }
        }

    }

    private void convertJSONtoArrayList(JSONObject forecast) {
        weatherList.clear();

        try {
            JSONArray list = forecast.getJSONArray("list");

            for (int i = 0; i < list.length(); i++) {
                JSONObject day = list.getJSONObject(i);
                JSONObject temperatures = day.getJSONObject("temp");
                JSONObject weather = day.getJSONArray("weather").getJSONObject(0);

                weatherList.add(new Weather(
                        day.getLong("dt"),
                        temperatures.getDouble("min"),
                        temperatures.getDouble("max"),
                        day.getDouble("humidity"),
                        weather.getString("description"),
                        weather.getString("icon")
                ));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }






}
