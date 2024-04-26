package com.tencent.libpag.sample.libpag_sample.utils;

import java.util.ArrayList;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.tencent.libpag.sample.libpag_sample.R;

public class GSensorDemoActivity extends AppCompatActivity {

    final String TAG = "GSensorDemoActivity";
    TextView gravityVisionData = null;
    TextView gravityDataVariance = null;
    Button gravityReset = null;
    SensorManager sensorManager = null;
    Sensor gSensor = null;
    final int cacheCapacity = 16;
    Float[] gsensorDataCache = new Float[cacheCapacity * 3];
    int currentIndex = 0;
    int dataSize = 0;
    float gravityVariance = 0f;
    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            String result = "x:" + event.values[0] + " ,y:" + event.values[1] + " ,z:" + event.values[2];
            Log.i(TAG, "on sensor changed," + result);
            currentIndex %= cacheCapacity;
            gsensorDataCache[currentIndex] =   event.values[0];
            gsensorDataCache[cacheCapacity + currentIndex] = event.values[1];
            gsensorDataCache[cacheCapacity * 2 + currentIndex] =  event.values[2];
            currentIndex ++;
            dataSize++;
            if (dataSize >= cacheCapacity) {
                refreshDataVariance();
                runOnUiThread(() ->{gravityVisionData.setText(result);});
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void refreshDataVariance() {
        float xav = 0;
        float yav = 0;
        float zav = 0;
        for (int i = 0; i < cacheCapacity; i++) {
            xav += gsensorDataCache[i];
            yav += gsensorDataCache[cacheCapacity + i];
            zav += gsensorDataCache[cacheCapacity * 2 + i];
        }
        xav = xav / cacheCapacity;
        yav = yav / cacheCapacity;
        zav = zav / cacheCapacity;

        float sumXSquare = 0;
        float sumYSquare = 0;
        float sumZSquare = 0;

        for (int i = 0; i< cacheCapacity; i++) {
            sumXSquare += Math.pow((gsensorDataCache[i] - xav), 2);
            sumYSquare += Math.pow((gsensorDataCache[cacheCapacity + i] - yav), 2);
            sumZSquare += Math.pow((gsensorDataCache[cacheCapacity * 2 + i] - zav), 2);
        }
        if((sumXSquare + sumYSquare + sumZSquare) / cacheCapacity > gravityVariance) {
            gravityVariance = (sumXSquare + sumYSquare + sumZSquare) / cacheCapacity;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gravityDataVariance.setText("1秒内加速度传感器数值方差:" + String.format("%.7f", gravityVariance));
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gsensor_demo);
        gravityVisionData = findViewById(R.id.gsensor_value);
        gravityDataVariance = findViewById(R.id.gsensor_data_variance);
        gravityReset = findViewById(R.id.reset_data);
        gravityReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < cacheCapacity * 3; i++) {
                    gsensorDataCache[i] = 0f;
                    dataSize = 0;
                    gravityVariance = 0f;
                }
                gravityDataVariance.setText("Reset Success");
            }
        });
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.i(TAG, "gsensor:" + gSensor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "on resume, register listener");
        sensorManager.registerListener(listener, gSensor, SensorManager.SENSOR_DELAY_UI, 100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(listener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}