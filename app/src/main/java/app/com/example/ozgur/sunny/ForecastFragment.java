package app.com.example.ozgur.sunny;

/**
 * Created by Ozgur on 18.09.2014.
 */

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter = null;
    private View rootView;
    private final String LOG_TAG = FetchWeatherClass.class.getSimpleName();

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId())
        {
            case R.id.action_refresh:
                UpdateWeather();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void UpdateWeather()
    {
        String loc = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));

        FetchWeatherClass fetch = new FetchWeatherClass();
        fetch.execute(loc);
    }

    @Override
    public void onStart(){
        super.onStart();
        UpdateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());

        ListView mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT,mForecastAdapter.getItem(i));
                startActivity(intent);
            }
        });

        return rootView;
    }

    private class FetchWeatherClass extends AsyncTask<String, Void, String[]> {
        protected String[] doInBackground(String... postalCode) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            Uri.Builder builder = null;
            String[] result = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //
                builder = new Uri.Builder();
                builder.scheme("http");
                builder.authority("api.openweathermap.org");
                builder.appendPath("data").appendPath("2.5").appendPath("forecast").appendPath("daily");
                builder.appendQueryParameter("q", postalCode[0]);
                builder.appendQueryParameter("mode", "json");
                builder.appendQueryParameter("units", "metric");
                builder.appendQueryParameter("cnt", "7");

                //"http://api.openweathermap.org/data/2.5/forecast/daily?id=311046&mode=json&units=metric&cnt=7"
                URL url = new URL(builder.build().toString());

                Log.v(LOG_TAG, builder.build().toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return WeatherDataParser.getWeatherDataFromJson(forecastJsonStr, 7, getActivity());
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
         /*   return forecastJsonStr;*/
            return null;
        }

        protected void onPostExecute(String[] result) {
            try {
                if (result != null) {
                    mForecastAdapter.clear();
                    for(String s : result)
                      mForecastAdapter.add(s);
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }

    }
}