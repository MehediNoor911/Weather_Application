package com.example.weather_app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;

import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    String api = "2e3985c0b43166ccaf2427d4cbfca95c";
    String city = "Dhaka";
    private TextView cityTextView, temperatureTextView, weatherTextView;
    private TextView maxTempTextView, minTempTextView, dayTextView, dateTextView;
    private TextView humidityTextView, windTextView, conditionTextView;
    private TextView sunriseTextView, sunsetTextView, seaTextView;
    private LottieAnimationView weatherAnimation;
    private  SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        cityTextView = findViewById(R.id.city);
        temperatureTextView = findViewById(R.id.temp);
        weatherTextView = findViewById(R.id.weather);
        maxTempTextView = findViewById(R.id.maxTemp);
        minTempTextView = findViewById(R.id.minTemp);
        dayTextView = findViewById(R.id.day);
        dateTextView = findViewById(R.id.date);
        humidityTextView = findViewById(R.id.humidity);
        windTextView = findViewById(R.id.wind);
        conditionTextView = findViewById(R.id.condition);
        sunriseTextView = findViewById(R.id.sunrise);
        sunsetTextView = findViewById(R.id.sunset);
        seaTextView = findViewById(R.id.sea);
        searchView = findViewById(R.id.searchView);
        weatherAnimation = findViewById(R.id.lottieAnimationView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                city = query; // Update the city variable with the user's input
                new WeatherTask().execute(); // Execute the WeatherTask with the new city
                searchView.clearFocus(); // Hide the keyboard and clear focus from the SearchView
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Execute the default weather task (e.g., for the default city)
        new WeatherTask().execute();
    }

    public TextView getWeatherTextView() {
        return weatherTextView;
    }

    public void setWeatherTextView(TextView weatherTextView) {
        this.weatherTextView = weatherTextView;
    }

    public SearchView getSearchView() {
        return searchView;
    }

    public void setSearchView(SearchView searchView) {
        this.searchView = searchView;
    }

    class WeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Show the progress loader
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... args) {

            // Make the HTTP request
            String response = HttpRequest.executeGet("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + api + "&units=metric");
            return response;
        }


        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);

                // Parsing JSON data
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject sys = jsonObj.getJSONObject("sys");
                JSONObject wind = jsonObj.getJSONObject("wind");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

                // Get timezone offset in seconds
                int timezoneOffset = jsonObj.getInt("timezone");

                // Create a TimeZone object using the offset
                TimeZone timeZone = TimeZone.getTimeZone("GMT" + (timezoneOffset >= 0 ? "+" : "") + (timezoneOffset / 3600));

                // Create date formatter with timezone
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.ENGLISH);
                SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.ENGLISH);
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);

                // Set the timezone for all formatters
                dayFormat.setTimeZone(timeZone);
                dateTimeFormat.setTimeZone(timeZone);
                timeFormat.setTimeZone(timeZone);

                // Get current timestamp and adjust for city's timezone
                long currentTime = System.currentTimeMillis();

                // Format the dates using city's timezone
                String dayString = dayFormat.format(new Date(currentTime));
                String dateTimeString = dateTimeFormat.format(new Date(currentTime));
                String sunriseTime = timeFormat.format(new Date(sys.getLong("sunrise") * 1000L));
                String sunsetTime = timeFormat.format(new Date(sys.getLong("sunset") * 1000L));

                String city = jsonObj.getString("name");
                String country = sys.getString("country");
                String temperature = main.getString("temp") + "°C";
                String minTemp = "Min Temp: " + main.getString("temp_min") + "°C";
                String maxTemp = "Max Temp: " + main.getString("temp_max") + "°C";
                String weatherDescription = weather.getString("description").toUpperCase();
                String windSpeed = wind.getString("speed") + " m/s";
                String humidity = main.getString("humidity") + "%";
                String seaLevel = main.has("sea_level") ? main.getString("sea_level") + " hPa" : "Sea Level: N/A";

                cityTextView.setText(city + ", " + country);
                temperatureTextView.setText(temperature);
                String condition = weather.getString("main");
                weatherTextView.setText(condition.toUpperCase());
                updateWeatherAnimation(condition);
                maxTempTextView.setText(maxTemp);
                minTempTextView.setText(minTemp);
                dayTextView.setText(dayString);
                dateTextView.setText(dateTimeString);
                humidityTextView.setText(humidity);
                windTextView.setText(windSpeed);
                sunriseTextView.setText(sunriseTime);
                sunsetTextView.setText(sunsetTime);
                seaTextView.setText(seaLevel);
                conditionTextView.setText(condition.toUpperCase());

                // Hide loader, show content
                findViewById(R.id.loader).setVisibility(View.GONE);

            } catch (Exception e) {
                findViewById(R.id.loader).setVisibility(View.GONE);
                e.printStackTrace();
            }
        }
    }

    private void updateWeatherAnimation(String condition) {
        // Get the root layout
        View rootLayout = findViewById(R.id.rootLayout);

        // Set appropriate Lottie animation and background theme based on weather condition
        switch (condition.toLowerCase()) {
            case "clear":
                weatherAnimation.setAnimation(R.raw.sun);
                rootLayout.setBackgroundResource(R.drawable.sunny_background2);
                break;
            case "rain":
                weatherAnimation.setAnimation(R.raw.rain);
                rootLayout.setBackgroundResource(R.drawable.rainy_background);
                break;
            case "clouds":
                weatherAnimation.setAnimation(R.raw.cloudy2);
                rootLayout.setBackgroundResource(R.drawable.cloudy_background);
                break;
            case "thunderstorm":
                weatherAnimation.setAnimation(R.raw.thunder);
                rootLayout.setBackgroundResource(R.drawable.thunderstrom_background);
                break;
            case "snow":
                weatherAnimation.setAnimation(R.raw.snow);
                rootLayout.setBackgroundResource(R.drawable.snow_background);
                break;
            case "haze":
                weatherAnimation.setAnimation(R.raw.haze);
                rootLayout.setBackgroundResource(R.drawable.haze_background);
                break;
            case "mist":
                weatherAnimation.setAnimation(R.raw.mist);
                rootLayout.setBackgroundResource(R.drawable.mist_background);
                break;
            default:
                weatherAnimation.setAnimation(R.raw.weather2);
                rootLayout.setBackgroundResource(R.drawable.gradient_background);
                break;
        }

        // Play the Lottie animation
        weatherAnimation.playAnimation();
    }


    public String getAPI() {
        return api;
    }
}
