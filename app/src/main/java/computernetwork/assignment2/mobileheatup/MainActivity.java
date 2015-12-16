package computernetwork.assignment2.mobileheatup;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends ActionBarActivity implements CheckBox.OnCheckedChangeListener{

    private SensorManager sensorManager;
    private SensorListener sensorListener;
    LocationManager locationManager;
    PowerManager.WakeLock wakeLock;
    MediaPlayer mediaPlayer;
    String videoUrl = "http://ge.tt/api/1/files/9Eog1lT2/0/blob?download"; //720p default


    private Handler timeHandler = new Handler();
    private Runnable timerCallback = new Runnable() {
        private int i=1;
        public void run() {
            CheckBox cbWebPage = (CheckBox) findViewById(R.id.cbWebPage);
            cbWebPage.setText("Load webpage every 10s (" + i + ")");
            EditText editUrl = (EditText) findViewById(R.id.editWebAddress);
            WebView webView = (WebView) findViewById(R.id.webView);
            webView.getSettings().setJavaScriptEnabled(true);
            class MyWebClient extends WebViewClient {
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    view.clearCache(true);
                    view.loadUrl(url);
                    return true;
                }
            };
            webView.setWebViewClient(new MyWebClient());
            webView.loadUrl(editUrl.getText().toString());
            i++;
            timeHandler.postDelayed(this, 200);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorListener = new SensorListener();

        ((CheckBox)findViewById(R.id.cbFlash)).setOnCheckedChangeListener(this);
        ((CheckBox)findViewById(R.id.cbBrightness)).setOnCheckedChangeListener(this);
        ((CheckBox)findViewById(R.id.cbVibrate)).setOnCheckedChangeListener(this);
        ((CheckBox)findViewById(R.id.cbLightSensor)).setOnCheckedChangeListener(this);
        ((CheckBox)findViewById(R.id.cbProximity)).setOnCheckedChangeListener(this);
        ((CheckBox)findViewById(R.id.cbAccelerometer)).setOnCheckedChangeListener(this);
        ((CheckBox)findViewById(R.id.cbGPS)).setOnCheckedChangeListener(this);
        ((CheckBox)findViewById(R.id.cbCamera)).setOnCheckedChangeListener(this);
        ((CheckBox)findViewById(R.id.cbYoutube)).setOnCheckedChangeListener(this);
        ((CheckBox)findViewById(R.id.cbWebPage)).setOnCheckedChangeListener(this);


        SurfaceView videoView = (SurfaceView) findViewById(R.id.videoView);
        SurfaceHolder videoHolder = videoView.getHolder();
        videoHolder.addCallback(new MediaPlayerCallback());
    }

    @Override
    protected void onResume () {
        super.onResume();

        Button btnKillBattery = (Button) findViewById(R.id.btnKillBattery);
        btnKillBattery.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckBox)findViewById(R.id.cbCamera)).setChecked(true);
                ((CheckBox)findViewById(R.id.cbYoutube)).setChecked(true);
                ((CheckBox)findViewById(R.id.cbFlash)).setChecked(true);
                ((CheckBox)findViewById(R.id.cbBrightness)).setChecked(true);
                ((CheckBox)findViewById(R.id.cbVibrate)).setChecked(true);
                ((CheckBox)findViewById(R.id.cbLightSensor)).setChecked(true);
                ((CheckBox)findViewById(R.id.cbProximity)).setChecked(true);
                ((CheckBox)findViewById(R.id.cbAccelerometer)).setChecked(true);
                ((CheckBox)findViewById(R.id.cbGPS)).setChecked(true);
                ((CheckBox)findViewById(R.id.cbWebPage)).setChecked(true);

            }
        });

        Button btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wakeLock != null)
                    wakeLock.release();

                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });

    }

    private Camera cam;
    private Vibrator vib;
    private Sensor lightSensor, proximitySensor, accelerometerSensor;

    public void onCheckedChanged(CompoundButton v, boolean checked) {


        switch(v.getId()) {

            case R.id.cbFlash:
                if (checked) {
                    if (!((CheckBox)findViewById(R.id.cbCamera)).isChecked())
                        cam = Camera.open();
                    Camera.Parameters p = cam.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    cam.setParameters(p);
                    cam.startPreview();
                }
                else {
                    if (!((CheckBox)findViewById(R.id.cbCamera)).isChecked()) {
                        //cam = Camera.open();
                        cam.stopPreview();
                        cam.release();
                    }
                }
                break;


            case R.id.cbBrightness:
                if (checked) {
                    WindowManager.LayoutParams layout = getWindow().getAttributes();
                    layout.screenBrightness = 1F;
                    getWindow().setAttributes(layout);
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Tag");
                    wakeLock.acquire();
                    ((CheckBox) v).setClickable(false);
                }
                break;


            case R.id.cbVibrate:
                if (checked) {
                    vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 5000};
                    vib.vibrate(pattern, 0);
                }
                else {
                    vib.cancel();
                }
                break;


            case R.id.cbLightSensor:
                if (checked) {
                    lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                    sensorManager.registerListener(sensorListener, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
                }
                else {
                    sensorManager.unregisterListener(sensorListener, lightSensor);
                    TextView tvLight = (TextView) findViewById(R.id.tvLight);
                    tvLight.setText("");
                }
                break;

            case R.id.cbProximity:
                if (checked) {
                    proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                    sensorManager.registerListener(sensorListener, proximitySensor, SensorManager.SENSOR_DELAY_FASTEST);
                }
                else {
                    sensorManager.unregisterListener(sensorListener, proximitySensor);
                    TextView tvProximity = (TextView) findViewById(R.id.tvProximity);
                    tvProximity.setText("");
                }
                break;


            case R.id.cbAccelerometer:
                if (checked) {
                    accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    sensorManager.registerListener(sensorListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
                }
                else {
                    sensorManager.unregisterListener(sensorListener, accelerometerSensor);
                    TextView tvAccelerometer = (TextView) findViewById(R.id.tvAccelerometer);
                    tvAccelerometer.setText(" ");
                }
                break;


            case R.id.cbGPS:
                if (checked) {
                    locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        locationManager.addGpsStatusListener(new GPSListener());
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener());
                        ((CheckBox) v).setClickable(false);
                        TextView tvGps = (TextView) findViewById(R.id.tvGPSLocation);
                        tvGps.setText("Obtaining GPS fix ...");
                    }
                    else {
                        Toast.makeText(MainActivity.this, "GPS is not available. Please enable GPS!", Toast.LENGTH_LONG).show();
                        ((CheckBox) v).setChecked(false);
                    }
                }
                break;

            case R.id.cbCamera:
                SurfaceView cameraView = (SurfaceView) findViewById(R.id.cameraView);
                if (checked) {
                    if (!((CheckBox)findViewById(R.id.cbFlash)).isChecked())
                        cam = Camera.open();
                    SurfaceHolder surfaceHolder = cameraView.getHolder();
                    try {
                        cam.setPreviewDisplay(surfaceHolder);
                        cam.setDisplayOrientation(90);
                        cam.startPreview();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (!((CheckBox)findViewById(R.id.cbFlash)).isChecked()) {
                        cam.stopPreview();
                        cam.release();
                    } else {
                        cam.stopPreview();
                        try {
                            cam.setPreviewDisplay(null);
                            cam.startPreview();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;

            case R.id.cbYoutube:
                if (checked) {
                    try {
                        mediaPlayer.setDataSource(videoUrl);
                        mediaPlayer.setOnPreparedListener(new MediaPlayerCallback());
                        mediaPlayer.setOnCompletionListener(new MediaPlayerCallback());

                        Toast.makeText(MainActivity.this, "Downloading video...", Toast.LENGTH_SHORT).show();
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                }
                break;

            case R.id.cbWebPage:
                if (checked) {
                    timeHandler.postDelayed(timerCallback, 100);
                } else {
                    timeHandler.removeCallbacks(timerCallback);
                }
                break;
        }
    }


    public void onRadioClicked(View v) {
        switch (v.getId()) {
            case R.id.rb4k:
                videoUrl = "http://ge.tt/api/1/files/9Phm2lT2/0/blob?download";
                break;
            case R.id.rb1080p:
                videoUrl = "http://ge.tt/api/1/files/3rkD2lT2/0/blob?download";
                break;
            case R.id.rb720p:
                videoUrl = "http://ge.tt/api/1/files/9Eog1lT2/0/blob?download";
                break;
            case R.id.rb480p:
                videoUrl = "http://ge.tt/api/1/files/8Ad00lT2/0/blob?download";
                break;
        }
    }


    private class SensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {

            switch (event.sensor.getType()) {
                case Sensor.TYPE_LIGHT:
                    TextView tvLight = (TextView) findViewById(R.id.tvLight);
                    tvLight.setText(event.values[0] + " lux");
                    break;

                case Sensor.TYPE_PROXIMITY:
                    TextView tvProximity = (TextView) findViewById(R.id.tvProximity);
                    tvProximity.setText(event.values[0] + " cm");
                    break;

                case Sensor.TYPE_ACCELEROMETER:
                    if (((CheckBox) findViewById(R.id.cbAccelerometer)).isChecked()) {
                        TextView tvAccelerometer = (TextView) findViewById(R.id.tvAccelerometer);
                        //tvAccelerometer.setText("X=" + event.values[0] + "cm " + "Y=" + event.values[1] + "cm " + "Z=" + event.values[2] + "cm");
                        tvAccelerometer.setText(String.format("X=%.2f Y=%.2f Z=%.2f", event.values[0], event.values[1], event.values[2]));
                    }
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //Meh! Not caring about accuracy now, just want to destroy the battery!
        }
    }

    private class GPSListener implements GpsStatus.Listener {
        private GpsStatus gpsStatus;
        @Override
        public void onGpsStatusChanged(int event) {
            gpsStatus = locationManager.getGpsStatus(gpsStatus);
            TextView tvGps = (TextView) findViewById(R.id.tvGPSLocation);

            int satellites = 0;
            Iterable<GpsSatellite> gpsSatellites = gpsStatus.getSatellites();
            for (GpsSatellite s : gpsSatellites) {
                if (s.usedInFix())
                    satellites++;
            }

            if (!tvGps.getText().toString().startsWith("Lat:"))
                tvGps.setText(satellites + " satellites in view");
        }
    }

    private class LocationListener implements android.location.LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            TextView tvGps = (TextView) findViewById(R.id.tvGPSLocation);
            tvGps.setText("Lat:" + location.getLatitude() + " Long:" + location.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
    }

    private class MediaPlayerCallback implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, SurfaceHolder.Callback {

        @Override
        public void onPrepared(MediaPlayer mp) {
            mediaPlayer.start();
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {}

        @Override
        public void onCompletion(MediaPlayer mp) {
         //   mediaPlayer.reset();
            Toast.makeText(MainActivity.this, "Playback completed, restarting...", Toast.LENGTH_SHORT).show();
            ((CheckBox) findViewById(R.id.cbYoutube)).setChecked(false);
            ((CheckBox) findViewById(R.id.cbYoutube)).setChecked(true);

        }
    }
}
