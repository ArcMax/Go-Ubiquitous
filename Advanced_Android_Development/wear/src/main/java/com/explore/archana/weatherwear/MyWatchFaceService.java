/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.explore.archana.weatherwear;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;

import com.example.WatchfaceSyncCommons;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class MyWatchFaceService extends CanvasWatchFaceService {

    @Override
    public Engine onCreateEngine() {
        return new MyWatchFaceEngine();
    }
    
    private class MyWatchFaceEngine extends Engine implements
            GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

        private static final String TAG = "MyWatchFaceService";
        private final long TICK_PERIOD_MILLIS = TimeUnit.SECONDS.toMillis(1);

        private SimpleWatchFace watchFace;
        private Handler timeTick;

        private GoogleApiClient googleApiClient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            Log.d(TAG, "onCreate");

            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_HIDDEN)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            timeTick = new Handler(Looper.myLooper());
            startTimerIfNecessary();
            watchFace = SimpleWatchFace.newInstance(MyWatchFaceService.this);

            googleApiClient = new GoogleApiClient.Builder(MyWatchFaceService.this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Wearable.API)
                    .build();
        }


        private void startTimerIfNecessary() {
            timeTick.removeCallbacks(timeRunnable);
            if(isVisible() && !isInAmbientMode())
                timeTick.post(timeRunnable);
        }

        private void releaseGoogleApiClient() {
            if (googleApiClient != null && googleApiClient.isConnected()) {
                Wearable.DataApi.removeListener(googleApiClient,dataListener);
                googleApiClient.disconnect();
            }
        }

        private final Runnable timeRunnable = new Runnable() {
            @Override
            public void run() {
                onSecondTick();
                if(isVisible() && !isInAmbientMode()){
                    timeTick.postDelayed(this,TICK_PERIOD_MILLIS);
                }
            }
        };

        private void onSecondTick() {
            invalidateIfNecessary();
        }

        private void invalidateIfNecessary() {
            if (isVisible() && !isInAmbientMode()) {
                invalidate();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if(visible){
                googleApiClient.connect();
            }else{
                releaseGoogleApiClient();
            }
            startTimerIfNecessary();
        }


        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            invalidate();
            startTimerIfNecessary();
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onDestroy() {
            timeTick.removeCallbacks(timeRunnable);
            releaseGoogleApiClient();
            super.onDestroy();

        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            watchFace.draw(canvas, bounds);
        }


        @Override
        public void onConnected(Bundle bundle) {
            Log.d(TAG, "connected GoogleAPI");
            Wearable.DataApi.addListener(googleApiClient, dataListener);
            Wearable.DataApi.getDataItems(googleApiClient).setResultCallback(bufferResultCallback);

        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.e(TAG, "suspended GoogleAPI");
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.e(TAG, "connectionFailed GoogleAPI");

        }

        private final DataApi.DataListener dataListener = new DataApi.DataListener() {
            @Override
            public void onDataChanged(DataEventBuffer dataEvent) {
                Log.d(TAG,"data event");
                for (DataEvent buffer:dataEvent){
                    if(buffer.getType() == DataEvent.TYPE_CHANGED){
                        DataItem item = buffer.getDataItem();
                        processesConfigurationFor(item);
                    }
                }

                dataEvent.release();
                invalidateIfNecessary();
            }
        };

        private void processesConfigurationFor(DataItem item) {
            Log.d(TAG, " process config");
            if(WatchfaceSyncCommons.KEY_PATH.equals(item.getUri().getPath())) {
                Log.d(TAG, " process config"+item.getUri().getPath());
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();

                if(dataMap.containsKey(WatchfaceSyncCommons.KEY_MAX_TEMP)){
                    String updateMax = dataMap.getString(WatchfaceSyncCommons.KEY_MAX_TEMP);
                    watchFace.updateMaxTemp(updateMax);
                }
                if(dataMap.containsKey(WatchfaceSyncCommons.KEY_MIN_TEMP)){
                    String updateMin = dataMap.getString(WatchfaceSyncCommons.KEY_MIN_TEMP);
                    watchFace.updateMinTemp(updateMin);
                }
                if(dataMap.containsKey(String.valueOf(WatchfaceSyncCommons.KEY_WEATHER_ID))){
                    String updateBitmap = dataMap.getString(WatchfaceSyncCommons.KEY_WEATHER_ID);
                    Log.d(TAG,"weatherID"+updateBitmap);
                    watchFace.createBitmap(Integer.parseInt(updateBitmap));
                }
            }
        }

        private final ResultCallback<DataItemBuffer> bufferResultCallback = new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                for(DataItem item:dataItems) {
                    processesConfigurationFor(item);
                }
                dataItems.release();
                invalidateIfNecessary();
            }
        };
    }

}
