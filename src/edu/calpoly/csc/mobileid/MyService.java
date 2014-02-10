package edu.calpoly.csc.mobileid;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;

import edu.calpoly.csc.mobileid.datatypes.AccelerometerFeatureSet;
import edu.calpoly.csc.mobileid.util.SimpleStatistics;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class MyService extends Service implements SensorEventListener {

   int counter = 0;
   Timer timer = new Timer();

   double ax = 0, ay = 0, az = 0;

   ArrayList<Double> xs = new ArrayList<Double>();
   ArrayList<Double> ys = new ArrayList<Double>();
   ArrayList<Double> zs = new ArrayList<Double>();

   /**
    * Record data from each accelerometer.
    * 
    * @author Cameron Stearns
    * 
    */
   class CollectionTask extends TimerTask {
      @Override
      public void run() {
         xs.add(ax);
         ys.add(ay);
         zs.add(az);
         
         if (counter >= 200) {
            Log.i("tag", "counter >= 200");

            Log.i("tag", "calling updateAFS");

            //Log.i("tag", "xs: " + xs);
            //Log.i("tag", "ys: " + ys);
            //Log.i("tag", "zs: " + zs);

            AccelerometerFeatureSet afs = updateAFS();
            Log.i("tag", "called updateAFS");

            Gson gson = new Gson();
            
            String g = afs.toRecord();//gson.toJson(afs);
            Log.i("tag", "generated json");
            //Log.i("tag", "afs: " + afs);
            //Log.i("tag", "g: " + g);

            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://71.94.58.146:8081");

            try {
               post.setEntity(new StringEntity(g, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
               Log.e("tag", "UnsupportedEncodingException", e);

            }
            post.setHeader("Content-Type", "application/json");
            HttpResponse response = null;
            try {
               response = client.execute(post);
            } catch (ClientProtocolException e) {
               Log.e("tag", "ClientProtocolException", e);
            } catch (IOException e) {
               Log.e("tag", "IOException", e);
            }
            Log.i("tag", "Response Status: " + response.getStatusLine());
            //AsyncHttpPost asyncHttpPost = new AsyncHttpPost(g);
            Log.i("tag", "created post. executing...");

            //asyncHttpPost.execute();
            Log.i("tag", "executed successfully! Resetting values!");

            
            counter = 0;
            xs = new ArrayList<Double>();
            ys = new ArrayList<Double>();
            zs = new ArrayList<Double>();
         } else {
            counter++;
         }
      }
   };

   private SensorManager sensorMan;

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      Log.i("tag", "called onStartCommand!!!: " + this.getApplicationContext());

      sensorMan = (SensorManager) getSystemService(SENSOR_SERVICE);
      sensorMan.registerListener(this,
            sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL);

      counter = 0;
      xs = new ArrayList<Double>();
      ys = new ArrayList<Double>();
      zs = new ArrayList<Double>();

      timer = new Timer();
      timer.scheduleAtFixedRate(new CollectionTask(), 0, 50);

      return Service.START_NOT_STICKY;
   }

   @Override
   public IBinder onBind(Intent intent) {
      Log.i("tag", "called onBind!!!");
      return null;
   }

   @Override
   public void onAccuracyChanged(Sensor arg0, int arg1) {
   }

   @Override
   public void onSensorChanged(SensorEvent event) {
      if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
         ax = event.values[0];
         ay = event.values[1];
         az = event.values[2];
      }
   }

   public AccelerometerFeatureSet updateAFS() {
      AccelerometerFeatureSet feature = new AccelerometerFeatureSet();

      List<Double> subXs = xs.subList(0, 200);
      List<Double> subYs = ys.subList(0, 200);
      List<Double> subZs = zs.subList(0, 200);
      feature.setAverageX(SimpleStatistics.calculateAverage(subXs));
      feature.setAverageY(SimpleStatistics.calculateAverage(subYs));
      feature.setAverageZ(SimpleStatistics.calculateAverage(subZs));

      feature.setStdDevX(SimpleStatistics.standardDev(subXs));
      feature.setStdDevY(SimpleStatistics.standardDev(subYs));
      feature.setStdDevZ(SimpleStatistics.standardDev(subZs));

      feature.setAvgAbsDistX(SimpleStatistics.averageAbsoluteDifference(subXs));
      feature.setAvgAbsDistY(SimpleStatistics.averageAbsoluteDifference(subYs));
      feature.setAvgAbsDistZ(SimpleStatistics.averageAbsoluteDifference(subZs));

      feature.setAvgResAccl(SimpleStatistics.averageResultantAccl(subXs, subYs,
            subZs));

      System.out.println("x");
      feature.setTimeBtwnPkX(SimpleStatistics.timeBtwnPks(subXs));
      System.out.println("y");
      feature.setTimeBtwnPkY(SimpleStatistics.timeBtwnPks(subYs));
      System.out.println("z");
      feature.setTimeBtwnPkZ(SimpleStatistics.timeBtwnPks(subZs));

      feature.setBinX(SimpleStatistics.bins(subXs));
      feature.setBinY(SimpleStatistics.bins(subYs));
      feature.setBinZ(SimpleStatistics.bins(subZs));
      return feature;
   }

   public class AsyncHttpPost extends AsyncTask<String, String, String> {
      String mJson = null;

      public AsyncHttpPost(String json) {
         super();
         Log.i("tag", "putting in json...");

         mJson = json;
         Log.i("tag", "I can't think of a reason why this is failing.");

      }

      @Override
      protected String doInBackground(String... params) {
         Log.i("tag", "Calling http request now.");
         HttpClient httpclient = new DefaultHttpClient();
         HttpPost httppost = new HttpPost("http://71.94.58.146:8080/data");
         HttpResponse response = null;
         String ret;

         try {
            httppost.setEntity(new StringEntity(mJson, "UTF-8"));
            httppost.setHeader("Content-Type", "application/json");

            response = httpclient.execute(httppost);
            ret = EntityUtils.toString(response.getEntity());

         } catch (ClientProtocolException e) {
            Log.e("tag", "client error", e);
            return Log.getStackTraceString(e);

         } catch (Exception e) {
            Log.e("tag", "weird http related error", e);
            return Log.getStackTraceString(e);
         }

         Log.i("tag", "Posting: " + mJson);
         return ret;
      }

      @Override
      protected void onPostExecute(String result) {
         Log.i("tag", "Posted: " + mJson);
         Log.i("tag", "Result: " + result);
      }

   }
}
