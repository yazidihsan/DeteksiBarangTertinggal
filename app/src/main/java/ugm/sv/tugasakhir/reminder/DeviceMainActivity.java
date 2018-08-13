package ugm.sv.tugasakhir.reminder;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import ugm.sv.tugasakhir.reminder.BluetoothLeService.LocalBinder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class DeviceMainActivity extends Activity {
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final UUID BLE_RX_TX = UUID.fromString(SampleGattAttributes.BLE_RX_TX);
    private static final String TAG = DeviceMainActivity.class.getSimpleName();
    public static int counter = 0;
    public static int[] rssiArray = new int[11];
    private String[] Data_Array;
    private Handler Handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (DeviceMainActivity.this.connect_switch == 1) {
                DeviceMainActivity.this.signal_value.setText(Integer.toString(DeviceMainActivity.this.rssi));
                DeviceMainActivity.this.read_distance = (double) (DeviceMainActivity.this.getDistance(69, DeviceMainActivity.this.rssi) * 100.0f);
                DeviceMainActivity.this.distance_value.setText(Double.toString(DeviceMainActivity.this.read_distance));
                DeviceMainActivity.this.write_BLE_char("Rssi:" + Integer.toString(DeviceMainActivity.this.rssi) + "Distance:" + Integer.toString((int) DeviceMainActivity.this.read_distance));
                //Activate Alarm
                if (read_distance > 500){
                    Intent alarmIntent = new Intent(DeviceMainActivity.this, AlarmActivity.class);
                    startActivity(alarmIntent);
                    System.exit(0);
                }
            }
        }
    };

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private String ReceiveData;
    private Switch auto_switch_btn;
    private Timer auto_timer = new Timer(true);
    auto_TimerTask auto_timerTask;
    private TextView signal_value;
    private BluetoothGattCharacteristic characteristicRX;
    private BluetoothGattCharacteristic characteristicTX;
    int connect_switch = 1;
    private Context context = this;
    private double read_distance;
    private TextView distance_value;
    private Button manual_send_btn;
    private TextView get_data;
    private TextView isSerial;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceAddress;
    private String mDeviceName;

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                DeviceMainActivity.this.mConnected = true;
                DeviceMainActivity.this.updateConnectionState(R.string.connected);
                DeviceMainActivity.this.invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                DeviceMainActivity.this.mConnected = false;
                DeviceMainActivity.this.updateConnectionState(R.string.disconnected);
                DeviceMainActivity.this.invalidateOptionsMenu();
                DeviceMainActivity.this.clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                DeviceMainActivity.this.displayGattServices(DeviceMainActivity.this.mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                DeviceMainActivity deviceMainActivity = DeviceMainActivity.this;
                BluetoothLeService mBluetoothLeService = DeviceMainActivity.this.mBluetoothLeService;
                deviceMainActivity.ReceiveData = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                DeviceMainActivity.this.get_data.setText(DeviceMainActivity.this.ReceiveData);
            }
        }
    };

    private Handler mHandler;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            DeviceMainActivity.this.mBluetoothLeService = ((LocalBinder) service).getService();
            if (!DeviceMainActivity.this.mBluetoothLeService.initialize()) {
                Log.e(DeviceMainActivity.TAG, "Unable to Initialize Bluetooth");
                DeviceMainActivity.this.finish();
            }
            DeviceMainActivity.this.mBluetoothLeService.connect(DeviceMainActivity.this.mDeviceAddress);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            DeviceMainActivity.this.mBluetoothLeService = null;
        }
    };
    int rssi;
    private Timer timer = new Timer(true);
    int user_rssi_set;

    public class MyTimerTask extends TimerTask {
        public void run() {
            if (DeviceMainActivity.counter == 11) {
                DeviceMainActivity.this.rssi = DeviceMainActivity.this.calculate_average_rssi();
                DeviceMainActivity.counter = 0;
            }
            DeviceMainActivity.rssiArray[DeviceMainActivity.counter] = DeviceMainActivity.this.mBluetoothLeService.getbluetoothrssi();
            DeviceMainActivity.counter++;
        }
    }

    public class auto_TimerTask extends TimerTask {
        public void run() {
            Message msg = new Message();
            msg.what = 0;
            DeviceMainActivity.this.Handler.sendMessage(msg);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_main);
        layout_init();
        this.manual_send_btn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (DeviceMainActivity.this.connect_switch == 1) {
                    DeviceMainActivity.this.signal_value.setText(Integer.toString(DeviceMainActivity.this.rssi));
                    DeviceMainActivity.this.read_distance = (double) (DeviceMainActivity.this.getDistance(69, DeviceMainActivity.this.rssi) * 100.0f);
                    DeviceMainActivity.this.distance_value.setText(Double.toString(DeviceMainActivity.this.read_distance));
                    DeviceMainActivity.this.write_BLE_char("Rssi:" + Integer.toString(DeviceMainActivity.this.rssi) + "Distance:" + Integer.toString((int) DeviceMainActivity.this.read_distance));
                }
            }
        });

        this.auto_switch_btn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DeviceMainActivity.this.manual_send_btn.setVisibility(4);
                    DeviceMainActivity.this.show_data();
                    return;
                }
                DeviceMainActivity.this.manual_send_btn.setVisibility(0);
                DeviceMainActivity.this.auto_timerTask.cancel();
            }
        });
    }
    private void clearUI() {
        ((TextView) findViewById(R.id.device_address)).setText("No Address");
        this.mConnectionState.setText("Disconnect");
        this.distance_value.setText("0");
        this.signal_value.setText("0");
        this.get_data.setText("No Data");
        this.isSerial.setText("SERIAL STATUS : OFF");
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode != 4) {
            return super.onKeyDown(keyCode, event);
        }
        finish();
        this.timer.cancel();
        this.auto_timer.cancel();
        return true;
    }

    private void layout_init() {
        Intent intent = getIntent();
        this.mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        this.mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        ((TextView) findViewById(R.id.device_address)).setText(this.mDeviceAddress);
        this.mConnectionState = findViewById(R.id.connection_state);
        this.isSerial = findViewById(R.id.serial_status);
        this.distance_value = findViewById(R.id.distance_value);
        this.signal_value = findViewById(R.id.signal_value);
        this.get_data = findViewById(R.id.get_data_value);
        this.manual_send_btn = findViewById(R.id.manual_button);
        this.auto_switch_btn = findViewById(R.id.auto_switch);
        this.auto_switch_btn.setChecked(false);
        this.Data_Array = new String[50];
        getActionBar().setTitle(this.mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        bindService(new Intent(this, BluetoothLeService.class), this.mServiceConnection, 1);
        this.timer.schedule(new MyTimerTask(), 2000, 200);
    }

    private void write_BLE_char(String str) {
        if (this.characteristicTX != null) {
            this.characteristicTX.setValue(str);
            if (this.mBluetoothLeService != null) {
                this.mBluetoothLeService.writeCharacteristic(this.characteristicTX);
                return;
            } else {
                Log.d(TAG, "mBluetoothLeService == null");
                return;
            }
        }
        Log.d(TAG, "mBLEGattCharacteristic == null");
    }

    protected void onResume() {
        super.onResume();
        registerReceiver(this.mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (this.mBluetoothLeService != null) {
            Log.d(TAG, "Connect Request Result=" + this.mBluetoothLeService.connect(this.mDeviceAddress));
        }
    }

    protected void onPause() {
        super.onPause();
        unregisterReceiver(this.mGattUpdateReceiver);
    }

    protected void onDestroy() {
        super.onDestroy();
        unbindService(this.mServiceConnection);
        this.mBluetoothLeService = null;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (this.mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            this.connect_switch = 1;
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            this.connect_switch = 0;
        }
        return true;
    }

    public void show_data() {
        this.auto_timerTask = new auto_TimerTask();
        new Timer().scheduleAtFixedRate(this.auto_timerTask, (long) 2000, 4000);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                onBackPressed();
                return true;
            case R.id.menu_connect:
                this.mBluetoothLeService.connect(this.mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                this.mBluetoothLeService.disconnect();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            public void run() {
                DeviceMainActivity.this.mConnectionState.setText(resourceId);
                ((TextView) DeviceMainActivity.this.findViewById(R.id.device_address)).setText(DeviceMainActivity.this.mDeviceAddress);
            }
        });
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices != null) {
            String unknownServiceString = getResources().getString(R.string.unknown_service);
            ArrayList<HashMap<String, String>> gattServiceData = new ArrayList();
            for (BluetoothGattService gattService : gattServices) {
                HashMap<String, String> currentServiceData = new HashMap();
                String uuid = gattService.getUuid().toString();
                currentServiceData.put("NAME", SampleGattAttributes.lookup(uuid, unknownServiceString));
                if (SampleGattAttributes.lookup(uuid, unknownServiceString) == "BLE Serial") {
                    this.isSerial.setText("SERIAL STATUS : ON");
                } else {
                    this.isSerial.setText("SERIAL STATUS : OFF");
                }
                currentServiceData.put("UUID", uuid);
                gattServiceData.add(currentServiceData);
                this.characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_BLE_RX_TX);
                this.characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_BLE_RX_TX);
            }
            this.mBluetoothLeService.setCharacteristicNotification(this.characteristicRX, true);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0.0) {
            return -1.0;
        }
        double ratio = (rssi * 1.0) / ((double) txPower);
        if (ratio < 1.0) {
            return Math.pow(ratio, 10.0);
        }
        return (0.89976 * Math.pow(ratio, 7.7095)) + 0.111;
    }

    public float getDistance(int txpower, int rssi) {
        return (float) Math.pow(10.0, (double) ((float) (((double) (Math.abs(rssi) - txpower)) / 20.0)));
    }

    private int calculate_average_rssi() {
        Arrays.sort(rssiArray);
        return ((rssiArray[4] + rssiArray[5]) + rssiArray[6]) / 3;
    }
}