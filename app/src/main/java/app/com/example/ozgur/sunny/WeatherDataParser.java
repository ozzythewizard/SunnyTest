package app.com.example.ozgur.sunny;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherDataParser {

    /**
     * Given a string of the form returned by the api call:
     * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7
     * retrieve the maximum temperature for the day indicated by dayIndex
     * (Note: 0-indexed, so 0 would refer to the first day).
     */
    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex)
            throws JSONException {
        // TODO: add parsing code here
        try {

            JSONObject weather = new JSONObject(weatherJsonStr);
            JSONArray days = weather.getJSONArray("list");
            JSONObject dayInfo = days.getJSONObject(dayIndex);
            JSONObject tempInfo = dayInfo.getJSONObject("temp");

          //  Log.i("Forecast Max Temp", (tempInfo.getDouble("max"));

            return tempInfo.getDouble("max");

        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return -1;
    }

    /* The date/time conversion code is going to be moved outside the asynctask later,
 * so for convenience we're breaking it out into its own method now.
 */
    public static String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }

    public static String getReadableDateString(long time, String dFormat){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat(dFormat);
        return format.format(date).toString();
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    public static String formatHighLows(double high, double low, Context act) {
        // For presentation, assume the user doesn't care about tenths of a degree.

        String unit = PreferenceManager.getDefaultSharedPreferences(act).getString(act.getString(R.string.pref_unit_key), act.getString(R.string.pref_unit_default));
        Log.v("FetchWeather",unit);
        //if imperial
        if (unit.equals("1")) {
           high=  ((high * 1.8) + 32);
           low =((low * 1.8) + 32);
        }
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        Log.v("FetchWeather", roundedHigh + "/" + roundedLow);

        return roundedHigh + "/" + roundedLow;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    public static String[] getWeatherDataFromJson(String forecastJsonStr, int numDays, Context act)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_SPEED = "speed";
        final String OWM_DEGREE = "deg";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;
            String pressure;
            String humidity;
            String speed;
            String deg;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime = dayForecast.getLong(OWM_DATETIME);
           // day = getReadableDateString(dateTime,"EEEE|MMM|d");
            day = getReadableDateString(dateTime);
            //pressure = Long.toString(Math.round(dayForecast.getDouble(OWM_PRESSURE)));
            //speed = Long.toString(Math.round(dayForecast.getDouble(OWM_SPEED)));
            //humidity = Long.toString(Math.round(dayForecast.getDouble(OWM_HUMIDITY)));
            //deg = Long.toString(Math.round(dayForecast.getDouble(OWM_DEGREE)));


            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low, act);
            //resultStrs[i] = day + "|" + description + "|" + high+ "|" + low+ "|" + pressure+ "|" + speed+ "|" + humidity+ "|" + deg;
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        return resultStrs;
    }

}