package ugm.sv.tugasakhir.reminder;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class DeviceScanActivity extends ListActivity {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private LeScanCallback mLeScanCallback = new LeScanCallback() {
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            DeviceScanActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    DeviceScanActivity.this.mLeDeviceListAdapter.addDevice(device);
                    DeviceScanActivity.this.mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    private boolean mScanning;

    private class LeDeviceListAdapter extends BaseAdapter {
        private LayoutInflater mInflator;
        private ArrayList<BluetoothDevice> mLeDevices = new ArrayList();

        public LeDeviceListAdapter() {
            this.mInflator = DeviceScanActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if (!this.mLeDevices.contains(device)) {
                this.mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return (BluetoothDevice) this.mLeDevices.get(position);
        }

        public void clear() {
            this.mLeDevices.clear();
        }

        public int getCount() {
            return this.mLeDevices.size();
        }

        public Object getItem(int i) {
            return this.mLeDevices.get(i);
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = this.mInflator.inflate(R.layout.activity_device_scan, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            BluetoothDevice device = (BluetoothDevice) this.mLeDevices.get(i);
            String deviceName = device.getName();
            if (deviceName == null || deviceName.length() <= 0) {
                viewHolder.deviceName.setText(R.string.unknown_device);
            } else {
                viewHolder.deviceName.setText(deviceName);
            }
            viewHolder.deviceAddress.setText(device.getAddress());
            return view;
        }
    }

    static class ViewHolder {
        TextView deviceAddress;
        TextView deviceName;

        ViewHolder() {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.title_devices);
        this.mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            Toast.makeText(this, R.string.ble_not_supported, 0).show();
            finish();
        }
        this.mBluetoothAdapter = ((BluetoothManager) getSystemService("bluetooth")).getAdapter();
        if (this.mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_ble_not_supported, 0).show();
            finish();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (this.mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_progress);
            Toast.makeText(this, "Scanning Device", Toast.LENGTH_LONG).show();
        } else {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                this.mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;
            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }
        return true;
    }

    protected void onResume() {
        super.onResume();
        if (!(this.mBluetoothAdapter.isEnabled() || this.mBluetoothAdapter.isEnabled())) {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
        }
        this.mLeDeviceListAdapter = new LeDeviceListAdapter();
        setListAdapter(this.mLeDeviceListAdapter);
        scanLeDevice(true);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == 0) {
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        this.mLeDeviceListAdapter.clear();
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        BluetoothDevice device = this.mLeDeviceListAdapter.getDevice(position);
        if (device != null) {
            Intent intent = new Intent(this, DeviceMainActivity.class);
            intent.putExtra(DeviceMainActivity.EXTRAS_DEVICE_NAME, device.getName());
            intent.putExtra(DeviceMainActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
            if (this.mScanning) {
                this.mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
                this.mScanning = false;
            }
            startActivity(intent);
        }
    }

    private void scanLeDevice(boolean enable) {
        if (enable) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    DeviceScanActivity.this.mScanning = false;
                    DeviceScanActivity.this.mBluetoothAdapter.stopLeScan(DeviceScanActivity.this.mLeScanCallback);
                    DeviceScanActivity.this.invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);
            this.mScanning = true;
            this.mBluetoothAdapter.startLeScan(this.mLeScanCallback);
        } else {
            this.mScanning = false;
            this.mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
        }
        invalidateOptionsMenu();
    }
}
