package com.android.waleed.arduino101led;

import android.app.AlarmManager;
import android.app.Service;
import android.bluetooth.BluetoothGatt;
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

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class LEDControlAcitivity extends ActionBarActivity {

    Button btnOn, btnOff;
    //SeekBar brightness;
    private BluetoothLeService mBluetoothLeService;
    private DeviceControlActivity DCA;
    private byte[] ON = {(byte)0xff,(byte)0xff,(byte)0xff};
    private byte[] OFF = {(byte)0x00,(byte)0x00,(byte)0x00};


    private String led = "19B10001-E8F2-537E-4F6C-D104768A1214";
    private String brightness = "19B10001-E8F2-537E-4F6C-D104768A1214";
    //private final static UUID SERVICE = UUID.fromString("19B10000-E8F2-537E-4F6C-D104768A1214");
    private BluetoothGatt mBluetoothGatt;

    private String mDeviceAddress;
    private final static String TAG = LEDControlAcitivity.class.getSimpleName();
    private boolean mConnected = false;
    AlarmManager aManager;
    private boolean sss = true;
    private Timer mTimer;

    SeekBar seekbar;
    TextView textView;
    int progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledcontrol_acitivity);

        //Button btnOn; //= (Button)findViewById(R.id.button);
        //btnOff = (Button)findViewById(R.id.button2);

        findViewById(R.id.button).setOnClickListener(mButton_OnClickListener);
        findViewById(R.id.button2).setOnClickListener(mButton_OnClickListener);

        seekbar = (SeekBar) findViewById(R.id.seekBar);
        seekbar.setMax(500);
        seekbar.setProgress(progress);

        textView = (TextView) findViewById(R.id.textView);
        textView.setText(" " + progress);
        textView.setTextSize(24);

        if(sss == true)
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                textView.setText("" + progress);

                mBluetoothLeService.write(UUID.fromString(brightness), Integer.toString(progress));
                sss = true;
                Log.e(TAG, "Brightness has changed");

                Log.e(TAG, Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mDeviceAddress ="98:4F:EE:0F:84:46";

        aManager = (AlarmManager) getSystemService(
                Service.ALARM_SERVICE);

       Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                //mBluetoothLeService.writebyte(UUID.fromString(led), "1");
                //sss = true;
            }
        }, 2000);
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

    //On click listener for button (ON)
    final OnClickListener mButton_OnClickListener = new OnClickListener() {
        public void onClick(final View v) {
            switch(v.getId()) {
                case R.id.button:
                    Log.e(TAG, "On Button is clicked");
                    mBluetoothLeService.write(UUID.fromString(led), "1");
                    sss = true;
                    break;
                case R.id.button2:
                    //Inform the user the button2 has been clicked
                    Log.e(TAG, "Off Button is clicked");
                    mBluetoothLeService.write(UUID.fromString(led), "0");
                    sss = false;
                    break;
            }
            //Inform the user the button has been clicked
           // Toast.makeText(this, "Button1 clicked.", Toast.LENGTH_SHORT).show();

            //mBluetoothLeService.readCharacteristic(DCA.globCharacterstic);



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


}
