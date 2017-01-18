package com.github.lukaszbudnik.bluetoothrobotcontrol;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class BluetoothRobotControl extends AppCompatActivity {

    private static final String TAG = "BluetoothRobotControl";

    private static final int REQUEST_ENABLE_BT = 123;

    private static final List<String> PEER_DEVICES = Arrays.asList("HC-06");

    private BluetoothConnection bluetoothConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_panel);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            startConnection();
        }

        final List<Integer> ids = Arrays.asList(R.id.n, R.id.ne, R.id.e, R.id.se, R.id.s, R.id.sw, R.id.w, R.id.nw, R.id.rec);

        for (Integer id : ids) {
            Button button = (Button) findViewById(id);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Integer id = ids.indexOf(view.getId());
                    Log.i(TAG, "Button clicked = " + ((Button) view).getText().toString() + " code = " + id);
                    if (bluetoothConnection != null && bluetoothConnection.isConnected()) {
                        try {
                            bluetoothConnection.write(id);
                        } catch (IOException e) {
                            Log.e(TAG, "Error while sending messages to robot", e);
                            Toast.makeText(getApplicationContext(), "Error while sending messages to robot", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Could not connect to robot. Check connection.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            if (button instanceof ToggleButton) {
                ToggleButton tb = (ToggleButton) button;
                tb.setText("\u25EF");
                tb.setTextOff("\u25EF");
                tb.setTextOn("\u25CF");
                tb.setBackgroundColor(Color.RED);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                startConnection();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothConnection != null) {
            bluetoothConnection.cancel();
        }
    }

    protected void startConnection() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            Log.i(TAG, "Found paired device: " + device.getName());
            if (PEER_DEVICES.indexOf(device.getName()) >= 0) {
                Log.i(TAG, "Attempting to connect to: " + device.getName());
                bluetoothConnection = new BluetoothConnection(device);
                bluetoothConnection.start();
            }
        }
    }
}
