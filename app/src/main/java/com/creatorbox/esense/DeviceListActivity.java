/*
Application Name: Smart EduBox
Created Date: Sept. 15, 2022
Company: CreatorBox Solutions
Developer: DGMJr.
*/

package com.creatorbox.esense;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

/**
 * OFFLINE MODE
 * DeviceListActivity handles the Bluetooth device discovery and selection.
 */
public class DeviceListActivity extends AppCompatActivity {

    private ListView listPairedDevices, listAvailableDevices;
    private ArrayAdapter<String> adapterPairedDevices, adapterAvailableDevices;

    private BluetoothAdapter bluetoothAdapter;

    private Context context;
    private ProgressBar progressScanDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        init();
    }

    private void init() {
        context = this;

        listPairedDevices = findViewById(R.id.list_paired_devices);
        listAvailableDevices = findViewById(R.id.list_available_devices);
        progressScanDevices = findViewById(R.id.progressScanDevices);

        adapterPairedDevices = new ArrayAdapter<String>(context, R.layout.bluetooth_device_list_item);
        adapterAvailableDevices = new ArrayAdapter<String>(context, R.layout.bluetooth_device_list_item);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        listPairedDevices.setAdapter(adapterPairedDevices);
        listPairedDevices.setOnItemClickListener((adapterView, view, i, l) -> {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent intent = new Intent();
            intent.putExtra("deviceAddress", address);
            setResult(RESULT_OK, intent);
            finish();
        });

        listAvailableDevices.setAdapter(adapterAvailableDevices);
        listAvailableDevices.setOnItemClickListener((adapterView, view, i, l) -> {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent intent = new Intent();
            intent.putExtra("deviceAddress", address);
            setResult(RESULT_OK, intent);
            finish();
        });

        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapterPairedDevices.add(device.getName() + "\n" + device.getAddress());
            }
        }

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, intentFilter);

        IntentFilter intentFilter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter1);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    adapterAvailableDevices.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressScanDevices.setVisibility(View.GONE);
                if (adapterAvailableDevices.getCount() == 0) {
                    //Toast.makeText(context, "No new devices found.", Toast.LENGTH_SHORT).show();
                    Log.d("DeviceListActivity","No new devices found.");
                } else {
                    //Toast.makeText(context, "Click on the device to start the chat.", Toast.LENGTH_SHORT).show();
                    Log.d("DeviceListActivity","Click on the device to start the chat.");
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bluetoothchat_menu_search_devices, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan_devices:
                scanDevices();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A function to scan for Bluetooth Devices.
     */
    private void scanDevices() {
        progressScanDevices.setVisibility(View.VISIBLE);
        adapterAvailableDevices.clear();

        Toast.makeText(context, "Scan Started.", Toast.LENGTH_SHORT).show();
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();
    }
}