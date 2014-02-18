package edu.calpoly.csc.mobileid;

import edu.calpoly.csc.mobileid.datatypes.AccelerometerFeatureSet;
import edu.calpoly.csc.mobileid.datatypes.ParcelableString;
import edu.calpoly.csc.mobileid.util.SimpleStatistics;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Bundle;
import android.view.View;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
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
import java.net.*;
import java.io.IOException;
import android.app.AlertDialog.Builder;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.Toast;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
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


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
      Log.i("tag", "called onCreate!");
      
      // wildly disgusting... but lets us pass the context into sub-methods.
      Log.i("tag", "started repeating service!");
      
		setContentView(R.layout.main);

		sensorMan = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorMan.registerListener(this,
				sensorMan.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		
      EditText userIdField = (EditText) findViewById(R.id.user_id);
      userIdField.addTextChangedListener(new TextWatcher() {

		    @Override
		    public void onTextChanged(CharSequence s, int start, int before, int count) {
		    }

		    @Override
		    public void beforeTextChanged(CharSequence s, int start, int count,
		            int after) {
		    }

		    @Override
		    public void afterTextChanged(Editable s) {
		        // we assign "theText" a value here
		       userId = s.toString();
		    }
		});
		
		

		Button start_col = (Button) findViewById(R.id.start_col);
		start_col.setVisibility(View.VISIBLE);
      start_col.setOnClickListener(new View.OnClickListener() {
         public void onClick(View view) {
            Log.i("tag", "clicked start col");

            findViewById(R.id.start_col).setVisibility(View.GONE);
            findViewById(R.id.end_col).setVisibility(View.VISIBLE);


            startService(new Intent(MainActivity.this, MyService.class));
             bindService(new Intent(MainActivity.this, MyService.class),
             mConnection, Context.BIND_AUTO_CREATE);

             Bundle bundle = new Bundle();
             bundle.putString(MyService.USER_ID_KEY, userId);
             final Message msg = Message.obtain(null, 1, null);
             msg.setData(bundle);
            
             
             Thread thread = new Thread() {
                @Override
                public void run() {
                   try {
                      while(!bound) {
                         Thread.sleep(1000);
                         Log.i("tag", "service not yet bound");
                      }
                      if(messenger == null) {
                         Log.i("tag", "error sending user id to service: messenger is null"
                               );
                         System.exit(1);
                      }
                     messenger.send(msg);
                  } catch (RemoteException e) {
                     // this case is pretty bad
                     Log.i("tag", "error sending user id to service: " + e);
                  } catch (Exception e) {
                     Log.i("tag", "error sending user id to service: " + e);
                  }
                   
                }
             };
             thread.start();
             
         }
      });

      Button end_col = (Button) findViewById(R.id.end_col);
      end_col.setVisibility(View.GONE);
      end_col.setOnClickListener(new View.OnClickListener() {
         public void onClick(View view) {
            Log.i("tag", "clicked end col");

            findViewById(R.id.end_col).setVisibility(View.GONE);
            findViewById(R.id.start_col).setVisibility(View.VISIBLE);
            // currently does nothing, long term should kill the program.
             unbindService(mConnection);
            stopService(new Intent(MainActivity.this, MyService.class));
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

	// start stack overflow code
   Messenger messenger;
   boolean bound = false;
   String userId;


   private ServiceConnection mConnection = new ServiceConnection() {

       @Override
       public void onServiceConnected(ComponentName className,
               IBinder service) {
          messenger = new Messenger(service);
          bound = true;
       }

       @Override
       public void onServiceDisconnected(ComponentName arg0) {
          bound = false;
       }
   };
}
