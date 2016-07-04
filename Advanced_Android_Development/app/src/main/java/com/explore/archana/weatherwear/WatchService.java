package com.explore.archana.weatherwear;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.WatchfaceSyncCommons;
import com.explore.archana.weatherwear.data.WeatherContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by archana on 6/26/2016.
 */
public class WatchService extends IntentService implements
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    public static final String TAG = "WatchService";
    public static final String UPDATE_WATCHFACE = "UPDATE WATCHFACE";
    private GoogleApiClient mGoogleApiClient;

    public WatchService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "watch service onhandle intent");
        if(intent!=null && intent.getAction()!=null && intent.getAction().equals(UPDATE_WATCHFACE)) {
            mGoogleApiClient = new GoogleApiClient.Builder(WatchService.this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Wearable.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        String locationQuery = Utility.getPreferredLocation(this);
        Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());
        Log.d(TAG, String.valueOf(weatherUri));

        Cursor cursor = getContentResolver().query(weatherUri,new String[]{
           WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
        },null,null,null);

        Log.d(TAG, String.valueOf(cursor));
        if(cursor.moveToFirst()){
            String weatherId = cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            String maxTemp = cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));
            String minTemp = cursor.getString(cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));

            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(WatchfaceSyncCommons.KEY_PATH);
            putDataMapRequest.getDataMap().putString(WatchfaceSyncCommons.KEY_WEATHER_ID, weatherId);
            putDataMapRequest.getDataMap().putString(WatchfaceSyncCommons.KEY_MAX_TEMP, maxTemp);
            putDataMapRequest.getDataMap().putString(WatchfaceSyncCommons.KEY_MIN_TEMP, minTemp);

            PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);

            Log.d(TAG, weatherId +"\n" + maxTemp + "\n" + minTemp + "\n");
        }
        cursor.close();


    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "onConnectionSuspended");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");

    }
}