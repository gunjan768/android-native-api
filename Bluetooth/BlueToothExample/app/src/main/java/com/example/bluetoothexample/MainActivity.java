package com.example.bluetoothexample;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_DISCOVER_BT = 2;

    TextView mStatusBlueTv, mPairedTv;
    ImageView mBlueIv;
    Button mOnBtn, mOffBtn, mDiscoverBtn, mPairedBtn;

    BluetoothAdapter mBlueAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStatusBlueTv = findViewById(R.id.statusBluetoothTv);
        mPairedTv     = findViewById(R.id.pairedTv);
        mBlueIv       = findViewById(R.id.bluetoothIv);
        mOnBtn        = findViewById(R.id.onBtn);
        mOffBtn       = findViewById(R.id.offBtn);
        mDiscoverBtn  = findViewById(R.id.discoverableBtn);
        mPairedBtn    = findViewById(R.id.pairedBtn);

        //adapter
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

        //check if bluetooth is available or not
        if(mBlueAdapter == null)
        {
            mStatusBlueTv.setText("Bluetooth is not available");
        }
        else
        {
            mStatusBlueTv.setText("Bluetooth is available");
        }

        //set image according to bluetooth status(on/off)
        if(mBlueAdapter.isEnabled())
        {
            mBlueIv.setImageResource(R.drawable.ic_action_on);
        }
        else
        {
            mBlueIv.setImageResource(R.drawable.ic_action_off);
        }

        // The REQUEST_ENABLE_BT constant passed to startActivityForResult() is a locally defined integer that must be greater than 0. The system passes this
        // constant back to you in your onActivityResult() implementation as the requestCode parameter. If enabling Bluetooth succeeds, your activity receives the
        // RESULT_OK result code in the onActivityResult() callback. If Bluetooth was not enabled due to an error (or the user responded "No") then the result code
        // is RESULT_CANCELED.

        //on btn click
        mOnBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!mBlueAdapter.isEnabled())
                {
                    showToast("Turning On Bluetooth...");

                    //intent to on bluetooth
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                }
                else
                {
                    showToast("Bluetooth is already on");
                }
            }
        });

        //off btn click
        mOffBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mBlueAdapter.isEnabled())
                {
                    mBlueAdapter.disable();
                    showToast("Turning Bluetooth Off");
                    mBlueIv.setImageResource(R.drawable.ic_action_off);
                }
                else
                {
                    showToast("Bluetooth is already off");
                }
            }
        });

        //discover bluetooth btn click
        mDiscoverBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(!mBlueAdapter.isDiscovering())
                {
                    showToast("Making Your Device Discoverable");

                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);

                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                }
            }
        });

        //get paired devices btn click
        mPairedBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mBlueAdapter.isEnabled())
                {
                    mPairedTv.setText("Paired Devices");
                    Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();

                    for(BluetoothDevice device: devices)
                    {
                        mPairedTv.append("\nDevice: " + device.getName()+ ", " + device);
                    }
                }
                else
                {
                    //bluetooth is off so can't get paired devices
                    showToast("Turn on bluetooth to get paired devices");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK)
                {
                    //bluetooth is on
                    mBlueIv.setImageResource(R.drawable.ic_action_on);
                    showToast("Bluetooth is on");
                }
                else
                {
                    //user denied to turn bluetooth on
                    showToast("couldn't on the bluetooth");
                }

            break;

            case REQUEST_DISCOVER_BT:
                if(resultCode == RESULT_CANCELED)
                {
                    //user denied to turn bluetooth on
                    showToast("couldn't on the bluetooth");
                }
                else
                {
                    mBlueIv.setImageResource(R.drawable.ic_action_on);
                }

            break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //toast message function
    private void showToast(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}