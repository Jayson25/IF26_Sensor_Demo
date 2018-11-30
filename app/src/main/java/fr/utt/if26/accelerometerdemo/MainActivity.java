package fr.utt.if26.accelerometerdemo;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor lightSensor;
    private Sensor stepCounter;
    private View view;

    //coordinates of the accelerometer
    private long accLastUpdate = 0;
    private long lightLastUpdate = 0;
    private long scLastUpdate = 0;
    private float last_x, last_y, last_z;

    //labels
    private TextView accDataLabel;
    private TextView lightDataLabel;
    private TextView stepCounterDataLabel;

    //image view for logo
    ImageView uttLogo;

    //allows to increase or decrease the sensitivity of the accelerometer
    private static final int SHAKE_TRESHOLD = 600;
    private static final int WHIP_TRESHOLD = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        view = this.getWindow().getDecorView();

        uttLogo = findViewById(R.id.uttLogo);
        uttLogo.setImageResource(R.drawable.logo);

        accDataLabel = findViewById(R.id.accDataLabel);
        lightDataLabel = findViewById(R.id.lightDataLabel);
        stepCounterDataLabel = findViewById(R.id.stepCounterDataLabel);

        //tells the application to access to the system's resources
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        //tells the application to listen to the accelerometer values
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //tells the application to read light sensor value
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        //tells the application to read the number of steps
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        //start sensor listener for the accelerometer
        sensorManager.registerListener(this, accelerometer, sensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, lightSensor, sensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);

        last_x = 0;
        last_y = 0;
        last_z = 0;
    }

    //it is a good practice to override onPause() and onResume(), since we don't want to listen the
    //accelerometer when the application hibernates

    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    protected void onResume(){
        super.onResume();
        sensorManager.registerListener(this, accelerometer, sensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, lightSensor, sensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, stepCounter, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        //making sure that the event comes from the accelerometer
        if (sensor.getType() == sensor.TYPE_ACCELEROMETER){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            //since the delay is extremely sensitive, we will add a delay. Refresh info every 100ms
            long currentTime = System.currentTimeMillis();
            long diffTime = (currentTime - accLastUpdate);

            if (diffTime > 100){
                accLastUpdate = currentTime;
                //calculate the speed of the shake
                float shakeSpeed = Math.abs(x + y + z - last_x - last_y - last_z)/diffTime * 10000;

                accDataLabel.setText("Accelerometer: \n\tx:" + x + "\n\ty:" + y + "\n\t  z:" + z+ "\n\tShake speed: " + shakeSpeed);

                //if the speed of our shake is more than 300 threshold
                if (shakeSpeed > SHAKE_TRESHOLD) {
                    Toast.makeText(this, (x<last_x)?"you're going right" : "you're going left", Toast.LENGTH_SHORT).show();
                }

                if (shakeSpeed > WHIP_TRESHOLD){
                    //media player for sound
                    final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.crack_the_whip);

                    //This element of the code allows to release the player once complete. It allows to avoid player from freezing
                    mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener(){
                        @Override
                        public void onCompletion(MediaPlayer media){
                            mp.release();
                        }
                    });

                    mp.start();
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }

        else if (sensor.getType() == sensor.TYPE_LIGHT){
            //get the light sensor value
            float light = event.values[0];

            long currentTime = System.currentTimeMillis();
            long diffTime = (currentTime - accLastUpdate);

            if (diffTime > 100){

                String intensity = "Light intensity: " + light;
                lightLastUpdate = currentTime;

                //Changing the color of the background depending on the light intensity of the environment
                if (light >= 100){
                    view.setBackgroundColor(Color.rgb(255, 112, 81));
                    lightDataLabel.setText(intensity + "\nVery bright environment");
                }

                else if (light >= 50){
                    view.setBackgroundColor(Color.WHITE);
                    lightDataLabel.setText(intensity + "\nNormal lighting");
                }

                else{
                    view.setBackgroundColor(Color.rgb(115,208,255));
                    lightDataLabel.setText(intensity + "\nVery Dim environment");
                }
            }
        }

        else if (sensor.getType() == sensor.TYPE_STEP_COUNTER){
            float steps = event.values[0];

            long currentTime = System.currentTimeMillis();
            long diffTime = (currentTime - scLastUpdate);

            if (diffTime > 100){

                stepCounterDataLabel.setText("Step counter: " + steps);
                scLastUpdate = currentTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
