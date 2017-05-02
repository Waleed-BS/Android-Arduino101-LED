package com.android.waleed.arduino101led;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;


public class LEDControlAcitivity extends ActionBarActivity {

    private BluetoothLeService mBluetoothLeService;
    private String mDeviceAddress = "98:4F:EE:0F:84:46";

//  private byte[] ON = {(byte)0xff,(byte)0xff,(byte)0xff};
//  private byte[] OFF = {(byte)0x00,(byte)0x00,(byte)0x00};

    private String led = "19B10001-E8F2-537E-4F6C-D104768A1214";

    private final static String TAG = LEDControlAcitivity.class.getSimpleName();

    private boolean mConnected = false;

    SeekBar brightness;
    TextView textView;
    int progress = 0;

    Button fadeButton;
    Button blinkButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledcontrol_acitivity);

        //Button btnOn; //= (Button)findViewById(R.id.button);
        //btnOff = (Button)findViewById(R.id.button2);

        findViewById(R.id.button).setOnClickListener(mButton_OnClickListener);
        findViewById(R.id.button2).setOnClickListener(mButton_OnClickListener);
        findViewById(R.id.button3).setOnClickListener(mButton_OnClickListener);
        findViewById(R.id.button4).setOnClickListener(mButton_OnClickListener);

        // to be able to use setEnabled function
        fadeButton = (Button) findViewById(R.id.button3);
        blinkButton = (Button) findViewById(R.id.button4);

        brightness = (SeekBar) findViewById(R.id.seekBar);
        brightness.setMax(255);
        brightness.setProgress(progress);

        textView = (TextView) findViewById(R.id.textView);
        textView.setText(" " + progress);
        textView.setTextSize(24);

        brightness.setEnabled(false);
        fadeButton.setEnabled(false);
        blinkButton.setEnabled(false);

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                textView.setText("" + progress);

                //double p = 255 * ((double) progress / 100.0);

                //String val = Integer.toString( (int) p);

               // Log.i(TAG, "Val: " + Double.toString(p));

                mBluetoothLeService.write(UUID.fromString(led), Integer.toString(progress));

                Log.i(TAG, "Brightness has changed");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;

                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;

                invalidateOptionsMenu();

            }
        }
    };

    //--------------------------------------------------------------------------------------------------
    //On click listener for button (ON & OFF)
    final OnClickListener mButton_OnClickListener = new OnClickListener() {
        public void onClick(final View v) {
            switch(v.getId()) {
                case R.id.button:
                    mBluetoothLeService.write(UUID.fromString(led), "127");
                    brightness.setEnabled(true);
                    fadeButton.setEnabled(true);
                    blinkButton.setEnabled(true);
                    msgOn();
                    break;

                case R.id.button2:
                    //Inform the user the button2 has been clicked
                    mBluetoothLeService.write(UUID.fromString(led), "0");
                    brightness.setEnabled(false);
                    fadeButton.setEnabled(false);
                    blinkButton.setEnabled(false);
                    msgOff();
                    break;
                case R.id.button3:
                    //Inform the user the button3 has been clicked
                    mBluetoothLeService.write(UUID.fromString(led), "256");
                    msgFade();
                    break;
                case R.id.button4:
                    //Inform the user the button4 has been clicked
                    mBluetoothLeService.write(UUID.fromString(led), "257");
                    msgBlink();
                    break;
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;

    }

    protected void msgOn(){
        Log.i(TAG, "On Button is clicked");
        Toast.makeText(this, "LED is ON", Toast.LENGTH_SHORT).show();
    }

    protected void msgOff(){
        Log.i(TAG, "Off Button is clicked");
        Toast.makeText(this, "LED is OFF", Toast.LENGTH_SHORT).show();
    }

    protected void msgFade(){
        Log.i(TAG, "Fade Button is clicked");
        Toast.makeText(this, "LED is Fading", Toast.LENGTH_SHORT).show();
    }

    protected void msgBlink(){
        Log.i(TAG, "Blink Button is clicked");
        Toast.makeText(this, "LED is Blinking", Toast.LENGTH_SHORT).show();
    }
}
