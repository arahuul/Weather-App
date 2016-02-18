package com.example.arahuul.MyWeatherApp;


import android.content.Intent;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.arahuul.MyWeatherApp.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    //private ArrayAdapter<String> adapter;
    private static final int FORECAST_LOADER = 0;
    private ForecastAdapter adapter;

    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
     private static final String[] FORECAST_COLUMNS = {
         // In this case the id needs to be fully qualified with a table name, since
         // the content provider joins the location & weather tables in the background
         // (both have an _id column)
         // On the one hand, that's annoying.  On the other, you can search the weather table
         // using the location set by the user, which is only in the Location table.
         // So the convenience is worth it.
          WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
          WeatherContract.WeatherEntry.COLUMN_DATE,
          WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
          WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
          WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
          WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
          WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
          WeatherContract.LocationEntry.COLUMN_COORD_LAT,
          WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

        // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
        // must change.
        static final int COL_WEATHER_ID = 0;
        static final int COL_WEATHER_DATE = 1;
        static final int COL_WEATHER_DESC = 2;
        static final int COL_WEATHER_MAX_TEMP = 3;
        static final int COL_WEATHER_MIN_TEMP = 4;
        static final int COL_LOCATION_SETTING = 5;
        static final int COL_WEATHER_CONDITION_ID = 6;
        static final int COL_COORD_LAT = 7;
        static final int COL_COORD_LONG = 8;


    public MainActivityFragment() {

    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


    }

    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
            inflater.inflate(R.menu.forecastfragment, menu);
        }

        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_refresh) {
                updateWeather();
                return true;
            }

            if(id==R.id.action_settings)
            {
                Intent i=new Intent(getContext(),SettingsActivity.class);
                startActivity(i);
                return  true;
            }

            return super.onOptionsItemSelected(item);
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


/*
        //create a string of app names
        String[] appnames = new String[]{
                "SPORTIFY STREAMER",
                "SCORES APP",
                "LIBRARY APP",
                "BUILD IT BUIGGER",
                "RAHUUL"
        };

        //create array adapter
        List<String> apps=new ArrayList<String>(Arrays.asList(appnames));
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, new ArrayList<String>());*/
/*
        //NEW CODE
        // Sort order:  Ascending, by date.

        String sortOrder= WeatherContract.WeatherEntry.COLUMN_DATE+" ASC";


        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting,System.currentTimeMillis());

        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);

        adapter=new ForecastAdapter(getActivity(), cur, 0);
*/
        adapter =new ForecastAdapter(getActivity(),null,0);

        View rootView =inflater.inflate(R.layout.fragment_main, container, false);

        final ListView lv = (ListView) rootView.findViewById(R.id.list);

        lv.setAdapter(adapter);
        //OLD CODE
        /*lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String forecast=adapter.getItem(position);
               // Toast toast=Toast.makeText(getContext(),forecast,Toast.LENGTH_SHORT);
               // toast.show();

                Intent i=new Intent(getActivity(),DetailActivity.class)
                            .putExtra("forecast",forecast);
                startActivity(i);


        });*/
        //NEW CODE
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
                            ));
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER,null,this);
        super.onActivityCreated(savedInstanceState);
    }

        // since we read the location when we create the loader, all we need to do is restart things
     void onLocationChanged( ) {
                updateWeather();
                getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
     }


    private void updateWeather() {
        //FetchWeatherTask weatherTask=new FetchWeatherTask();
        //weatherTask.execute("94043");
        // FetchWeatherTask weatherTask=new FetchWeatherTask(getActivity(),adapter);
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        // SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // String location = prefs.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);
    }


//    @Override
//    public void onStart()
//    {
//        super.onStart();
//        updateWeather();
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";


        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    adapter.swapCursor(cursor);
     }

    @Override
     public void onLoaderReset(Loader<Cursor> cursorLoader) {
     adapter.swapCursor(null);
      }


}
