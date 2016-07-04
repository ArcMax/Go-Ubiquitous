package com.explore.archana.weatherwear;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.format.Time;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by archana on 6/23/2016.
 */
public class SimpleWatchFace {

    private static final String TAG = "SimpleWatchFace";
    private static final String TIME_FORMAT_WITHOUT_SECONDS = "%02d:%02d";

    private final Time time;
    private final Paint timePaint;
    private final Paint datePaint;
    private final Paint tempMaxPaint;
    private final Paint tempMinPaint;
    private final Paint backgroundPaint;
    private Context context;

    private Bitmap weatherImage,resizedBitmap;
    private  String maxTemp = "00"+ "\u00b0";
    private  String minTemp = "00"+ "\u00b0";

    public SimpleWatchFace(Context context, Paint timePaint, Paint datePaint, Paint tempMaxPaint, Paint tempMinPaint, Paint backPaint, Time time) {
        this.context = context;
        this.timePaint = timePaint;
        this.datePaint = datePaint;
        this.tempMaxPaint = tempMaxPaint;
        this.tempMinPaint = tempMinPaint;
        this.backgroundPaint = backPaint;
        this.time = time;

        weatherImage = BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher);
        resizedBitmap = Bitmap.createScaledBitmap(weatherImage, 40, 40, false);
    }

    public static SimpleWatchFace newInstance(Context context){
        Paint timePaint = new Paint();
        timePaint.setColor(Color.WHITE);
        timePaint.setShadowLayer(0, 2f, 2f, Color.WHITE);
        timePaint.setStyle(Paint.Style.FILL);
        timePaint.setAntiAlias(true);

        Paint datePaint = new Paint();
        datePaint.setColor(Color.WHITE);
        datePaint.setShadowLayer(0, 2f, 2f, Color.WHITE);
        datePaint.setStyle(Paint.Style.FILL);
        datePaint.setAntiAlias(true);

        Paint tempMaxPaint = new Paint();
        tempMaxPaint.setColor(Color.BLACK);
        tempMaxPaint.setShadowLayer(0, 2f, 2f, Color.BLACK);
        tempMaxPaint.setStyle(Paint.Style.FILL);
        tempMaxPaint.setAntiAlias(true);

        Paint tempMinPaint = new Paint();
        tempMinPaint.setColor(Color.BLACK);
        tempMinPaint.setShadowLayer(0, 2f, 2f, Color.BLACK);
        tempMinPaint.setStyle(Paint.Style.FILL);
        tempMinPaint.setAntiAlias(true);

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.DKGRAY);

        return new SimpleWatchFace(context, timePaint,datePaint, tempMaxPaint,tempMinPaint,backgroundPaint,new Time());
    }


    public void draw(Canvas canvas,Rect rect){

        canvas.drawRect(0, 0, rect.width(), rect.height(), backgroundPaint);
        time.setToNow();

        String timeText = String.format(TIME_FORMAT_WITHOUT_SECONDS, time.hour, time.minute);
        setTextSizeForWidth(timePaint, 200, timeText);
        float timeXoffset = computeXOffset(timeText, timePaint, rect);
        float timeYoffset = computeTimeYOffset(timeText, timePaint, rect);
        canvas.drawText(timeText, timeXoffset, timeYoffset, timePaint);

        DateFormat df5 = new SimpleDateFormat("E, MMM dd yyyy");
        Date date = new Date();
        String dateText = df5.format(date);
        setTextSizeForWidth(datePaint, 200, dateText);
        float dateXoffset = computeXOffset(dateText, datePaint, rect);
        float dateYoffset = computeDateYOffset(dateText, datePaint);
        canvas.drawText(dateText, dateXoffset, timeYoffset + dateYoffset, datePaint);

        setTextSizeForWidth(tempMaxPaint, 40, maxTemp);
        float tempMaxXoffset = computeXOffset(maxTemp, tempMaxPaint, rect);
        float tempMaxYoffset = computeDateYOffset(maxTemp, tempMaxPaint);
        canvas.drawText(maxTemp, tempMaxXoffset, timeYoffset + dateYoffset + tempMaxYoffset + 20, tempMaxPaint);

        setTextSizeForWidth(tempMinPaint, 40, minTemp);
        float tempMinYOffset = computeDateYOffset(minTemp, tempMinPaint);
        canvas.drawText(minTemp, tempMaxXoffset + 80, timeYoffset + dateYoffset + tempMinYOffset + 20, tempMinPaint);

        canvas.drawBitmap(resizedBitmap, rect.exactCenterX()-resizedBitmap.getWidth()-40 , timeYoffset + dateYoffset + 20, new Paint());
    }

    private float computeXOffset(String text, Paint paint, Rect watchBounds) {
        float centerX = watchBounds.exactCenterX();
        float timeLength = paint.measureText(text);
        return centerX - (timeLength/2f);
    }

    private float computeTimeYOffset(String timeText, Paint timePaint, Rect watchBounds) {
        float centerY = watchBounds.exactCenterY();
        Rect textBounds = new Rect();
        timePaint.getTextBounds(timeText, 0, timeText.length(), textBounds);
        int textHeight = textBounds.height();
        return centerY + (textHeight / 10f);
    }

    private static void setTextSizeForWidth(Paint paint, float desiredWidth, String text) {
        final float testTextSize = 48f;

        // Get the bounds of the text, using our testTextSize.
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // Calculate the desired size as a proportion of our testTextSize.
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();


        // Set the paint for that size.
        paint.setTextSize(desiredTextSize);
    }

    private float computeDateYOffset(String dateText, Paint datePaint) {
        Rect textBounds = new Rect();
        datePaint.getTextBounds(dateText, 0, dateText.length(), textBounds);
        return textBounds.height() + 10.0f;
    }

    public void updateMaxTemp(String max){
        Log.d(TAG,max);
        maxTemp = convertToDigit(max)+"\u00b0";

    }
    public void updateMinTemp(String min){
        Log.d(TAG,min);
        minTemp = convertToDigit(min)+"\u00b0";
    }

    public void createBitmap(int weatherId){
        if(weatherId != 00) {
            Log.d(TAG, Integer.toString(weatherId));
            weatherImage = getResizedBitmap(BitmapFactory.decodeResource(context.getResources(), getArtResourceForWeatherCondition(weatherId)),60,60);
        }
    }

    private static String convertToDigit(String d){
        if (Float.valueOf(d)==(float)Float.valueOf(d)){
            return Integer.toString(Float.valueOf(d).intValue());
        }else
            return String.format("%s",d);
    }


    public static int getArtResourceForWeatherCondition(int weatherId) {
        if (weatherId >= 200 && weatherId <= 232) {
            return R.mipmap.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.mipmap.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.mipmap.art_rain;
        } else if (weatherId == 511) {
            return R.mipmap.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.mipmap.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.mipmap.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.mipmap.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.mipmap.art_storm;
        } else if (weatherId == 800) {
            return R.mipmap.art_clear;
        } else if (weatherId == 801) {
            return R.mipmap.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.mipmap.art_clouds;
        }
        return -1;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION 
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP 
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP 
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
}
