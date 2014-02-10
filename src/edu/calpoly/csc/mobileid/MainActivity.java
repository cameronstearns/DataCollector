package edu.calpoly.csc.mobileid;

import edu.calpoly.csc.mobileid.datatypes.AccelerometerFeatureSet;
import edu.calpoly.csc.mobileid.util.SimpleStatistics;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Bundle;
import android.view.View;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.EditText;
import android.widget.TextView;
import android.location.LocationManager;
import android.content.Context;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.ClientProtocolException;
import android.os.AsyncTask;
import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.IOException;
import android.app.AlertDialog.Builder;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.Toast;
import android.content.DialogInterface;
import android.util.Log;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MainActivity extends Activity implements SensorEventListener {
	private SensorManager sensorMan;
	double ax, ay, az;

	int counter = 0;
	ArrayList<Double> xs = new ArrayList<Double>();
	ArrayList<Double> ys = new ArrayList<Double>();
	ArrayList<Double> zs = new ArrayList<Double>();
	Timer timer = new Timer();

	private SensorEventListener listener = this;
	private AccelerometerFeatureSet feature = new AccelerometerFeatureSet();
	public final static String EXTRA_MESSAGE = "edu.calpoly.csc.mobileid.MESSAGE";

	/**
	 * Start the sensor analysis.
	 * 
	 * @author Cameron Stearns
	 */
	class StartTask extends TimerTask {
		@Override
		public void run() {
		}
	};

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
			counter++;

			// Toast.makeText(getApplicationContext(), "col",
			// Toast.LENGTH_SHORT)
			// .show();
		}
	};

	/**
	 * Stop sensor analysis and calculate statistics.
	 * 
	 * @author Cameron Stearns
	 * 
	 */
	class FinishTask extends TimerTask {
		@Override
		public void run() {
		};
	}

	public List<AccelerometerFeatureSet> updateAFS() {
		List<AccelerometerFeatureSet> afsList = new ArrayList<AccelerometerFeatureSet>();

		for (int i = 200; i < counter; i += 200) {
			feature = new AccelerometerFeatureSet();

			List<Double> subXs = xs.subList(i - 200, i);
			List<Double> subYs = ys.subList(i - 200, i);
			List<Double> subZs = zs.subList(i - 200, i);
			feature.setAverageX(SimpleStatistics.calculateAverage(subXs));
			feature.setAverageY(SimpleStatistics.calculateAverage(subYs));
			feature.setAverageZ(SimpleStatistics.calculateAverage(subZs));

			feature.setStdDevX(SimpleStatistics.standardDev(subXs));
			feature.setStdDevY(SimpleStatistics.standardDev(subYs));
			feature.setStdDevZ(SimpleStatistics.standardDev(subZs));

			feature.setAvgAbsDistX(SimpleStatistics
					.averageAbsoluteDifference(subXs));
			feature.setAvgAbsDistY(SimpleStatistics
					.averageAbsoluteDifference(subYs));
			feature.setAvgAbsDistZ(SimpleStatistics
					.averageAbsoluteDifference(subZs));

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
			afsList.add(feature);
		}
		return afsList;
	}

	public class AsyncHttpPost extends AsyncTask<String, String, String> {
		private HashMap<String, String> mData = null;
		String mJson = null;

		public AsyncHttpPost(HashMap<String, String> data) {
			mData = data;
		}

		public AsyncHttpPost(String json) {
			mJson = json;
		}

		@Override
		protected String doInBackground(String... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://71.94.58.146:8080/data");
			HttpResponse response = null;
			String ret;

			try {
				// ArrayList<NameValuePair> nameValuePairs = new
				// ArrayList<NameValuePair>();
				// Iterator<String> it = mData.keySet().iterator();
				// while(it.hasNext()) {
				// String key = it.next();
				// nameValuePairs.add(new BasicNameValuePair(key,
				// mData.get(key)));
				// }
				//
				// httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
				// "UTF-8"));

				httppost.setEntity(new StringEntity(mJson, "UTF-8"));
				httppost.setHeader("Content-Type", "application/json");

				response = httpclient.execute(httppost);
				ret = EntityUtils.toString(response.getEntity());

			} catch (ClientProtocolException e) {
				return Log.getStackTraceString(e);

			} catch (Exception e) {
				return Log.getStackTraceString(e);
			}
			return ret;
		}

		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT)
					.show();
		}

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
      Log.i("tag", "called onCreate!");
      
      startService(new Intent(this, MyService.class));
      Log.i("tag", "started repeating service!");
      
		setContentView(R.layout.main);

		sensorMan = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorMan.registerListener(this,
				sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		// Timer timer = new Timer();

		Button start_col = (Button) findViewById(R.id.start_col);
		findViewById(R.id.user_id);
		start_col.setVisibility(View.VISIBLE);
		start_col.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				findViewById(R.id.start_col).setVisibility(View.GONE);
				findViewById(R.id.end_col).setVisibility(View.VISIBLE);

				// start collection

				timer = new Timer();
				timer.scheduleAtFixedRate(new CollectionTask(), 3000, 50);
			}
		});

		Button end_col = (Button) findViewById(R.id.end_col);
		end_col.setVisibility(View.GONE);
		end_col.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				findViewById(R.id.end_col).setVisibility(View.GONE);
				findViewById(R.id.start_col).setVisibility(View.VISIBLE);

				timer.cancel();
				// finish collection
				// send post

				List<AccelerometerFeatureSet> afs = updateAFS();

				counter = 0;
				xs = new ArrayList<Double>();
				ys = new ArrayList<Double>();
				zs = new ArrayList<Double>();
				println("asdf");
				
				Gson gson = new Gson();
				String g = gson.toJson(afs);

				Toast.makeText(getApplicationContext(), g, Toast.LENGTH_SHORT)
						.show();
				// String g = "{\"value\":\"yes\"}";

				AsyncHttpPost asyncHttpPost = new AsyncHttpPost(g);
				asyncHttpPost.execute();

			}
		});
	}

	/**
	 * * May be causing a memory leak.
	 * */
	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			((TextView) findViewById(R.id.summary)).setText(String
					.valueOf(feature.toString()));
		}
	};

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			ax = event.values[0];
			ay = event.values[1];
			az = event.values[2];

			((TextView) findViewById(R.id.x_text)).setText(String.valueOf(ax));
			((TextView) findViewById(R.id.y_text)).setText(String.valueOf(ay));
			((TextView) findViewById(R.id.z_text)).setText(String.valueOf(az));
		}
	}

}
