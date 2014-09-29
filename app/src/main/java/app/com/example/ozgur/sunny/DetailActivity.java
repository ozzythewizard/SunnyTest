package app.com.example.ozgur.sunny;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;



public class DetailActivity extends ActionBarActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId())
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {
        private final String LOG_TAG = this.getClass().getSimpleName();
        private final String FORECAST_SHARE_HASHTAG = "#Sunshine App";
        private String mForecastString;
        private  ShareActionProvider mShareActionProvider;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            Intent intent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
                mForecastString = intent.getStringExtra(Intent.EXTRA_TEXT);
            ((TextView)rootView.findViewById(R.id.txtDay)).setText(mForecastString);
            return rootView;
        }

        @Override
        public void onCreateOptionsMenu( Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.detailfragment, menu);

            MenuItem item = menu.findItem(R.id.share);
            mShareActionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(item);

            if(mShareActionProvider != null)
                mShareActionProvider.setShareIntent(CreateForecastShareIntent());
            else
            {
                Log.d(LOG_TAG,"ShareActionProvide is null");
            }
        }

        private Intent CreateForecastShareIntent(){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET); //when you click back makes sure that you go back to your own app. not somewhere else
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, mForecastString + " " + FORECAST_SHARE_HASHTAG);
            return intent;
        }
    }
}
