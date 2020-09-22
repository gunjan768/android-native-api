package com.example.bluetoothfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_ENABLE_BT = 1;
    TextView statusTextView;
    Button searchButton;

    static ArrayList<String> bluetoothDevices = new ArrayList<>();
    ArrayList<String> addresses = new ArrayList<>();

    RecyclerView recyclerView;
    static MyAdapter myAdapter;
    BluetoothAdapter bluetoothAdapter;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // As there are lots of requests available like request for Bluetooth .... etc. requestCode ensures the specific type of permission associated with it.
        if(requestCode == 1)
        {
            // If this if condition satisfies then it means that user has granted the permission.
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // This is weird but we have again check for the permission check for the specific type ( here it is for "ACCESS_FINE_LOCATION" ).
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    startSearchingForLocalDevices();
                }
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

             // Log.i("Actionnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn", action);

            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                statusTextView.setText("Finished");
                searchButton.setEnabled(true);
            }
            else if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String name = device.getName();
                String address = device.getAddress();
                String rssi = Integer.toString(intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE));

                // Log.i("Device Found","Name: " + name + " Address: " + address + " RSSI: " + rssi);

                if(!addresses.contains(address))
                {
                    addresses.add(address);
                    String deviceString = "";

                    if(name == null || name.equals(""))
                    {
                        deviceString = address + " - RSSI " + rssi + "dBm";
                    }
                    else
                    {
                        deviceString = name + " - RSSI " + rssi + "dBm";
                    }

                    bluetoothDevices.add(deviceString);
                    myAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK)
                {
                    askPermissionForFineAccessLocation();
                }
                else
                {
                    showToast("couldn't on the bluetooth");
                }

                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void askPermissionForFineAccessLocation()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // requestPermissions() will ask for user permission. It will trigger onRequestPermissionsResult() method defined above. As there are lots of requests
            // available like request for Bluetooth .... etc. requestCode ensures the specific type of permission associated with it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else
        {
            startSearchingForLocalDevices();
        }
    }

    private void startSearchingForLocalDevices()
    {
        statusTextView.setText("Searching...");
        searchButton.setEnabled(false);

        bluetoothDevices.clear();

        addresses.clear();
        bluetoothAdapter.startDiscovery();
    }

    public void searchClicked(View view)
    {
        if(!bluetoothAdapter.isEnabled()) 
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            askPermissionForFineAccessLocation();
        }
    }

    public void setUpRecyclerView()
    {
        recyclerView = findViewById(R.id.recyclerView);
        myAdapter = new MyAdapter(this);

        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpRecyclerView();

        statusTextView = findViewById(R.id.statusTextView);
        searchButton = findViewById(R.id.searchButton);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void showToast(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}