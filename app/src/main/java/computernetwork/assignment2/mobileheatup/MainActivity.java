package computernetwork.assignment2.mobileheatup;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity{

    private SensorManager sensorManager;
    private SensorListener sensorListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorListener = new SensorListener();
    }

    private Camera cam;
    private Vibrator vib;
    private Sensor lightSensor, proximitySensor, accelerometerSensor;

    public void onCheckBoxClicked(View v) {
        boolean checked = ((CheckBox) v).isChecked();

        switch(v.getId()) {

            case R.id.cbFlash:
                if (checked) {
                    cam = Camera.open();
                    Camera.Parameters p = cam.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    cam.setParameters(p);
                    cam.startPreview();
                }
                else {
                    if (cam == null)
                        cam = Camera.open();

                    cam.stopPreview();
                    cam.release();
                }
                break;


            case R.id.cbBrightness:
                if (checked) {
                    WindowManager.LayoutParams layout = getWindow().getAttributes();
                    layout.screenBrightness = 1F;
                    getWindow().setAttributes(layout);
                    ((CheckBox) v).setClickable(false);
                }
                break;


            case R.id.cbVibrate:
                if (checked) {
                    vib = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 5000, 100};
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
                        tvAccelerometer.setText("X=" + event.values[0] + "cm " + "Y=" + event.values[1] + "cm " + "Z=" + event.values[2] + "cm");
                    }
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}
