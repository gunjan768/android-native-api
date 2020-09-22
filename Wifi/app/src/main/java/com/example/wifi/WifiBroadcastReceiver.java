package com.example.wifi;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.core.app.ActivityCompat;

// The android system notifies us about events of wifi using Broadcast that's why we created BroadcastReceiver.
public class WifiBroadcastReceiver extends BroadcastReceiver
{
    private static final String TAG = "WifiBroadcastReceiver";

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private MainActivity mainActivity;
    private ConnectivityManager connectivityManager;

    public WifiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, MainActivity mainActivity, ConnectivityManager connectivityManager)
    {
        this.manager = manager;
        this.channel = channel;
        this.mainActivity = mainActivity;
        this.connectivityManager = connectivityManager;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();

        // 1) WIFI_P2P_CONNECTION_CHANGED_ACTION : indicates whether Broadcast intent action indicating that the state of Wi-Fi p2p connectivity has changed.
        // All of these permissions are required to receive this broadcast: Manifest.permission.ACCESS_FINE_LOCATION and Manifest.permission.ACCESS_WIFI_STATE.
        // 2) WIFI_P2P_PEERS_CHANGED_ACTION : Broadcast intent action indicating that the available peer list has changed. This can be sent as a result of peers
        // being found, lost or updated. All of these permissions are required to receive this broadcast: Manifest.permission.ACCESS_FINE_LOCATION and
        // Manifest.permission.ACCESS_WIFI_STATE.
        // 3) WIFI_P2P_STATE_CHANGED_ACTION : Broadcast intent action to indicate whether Wi-Fi p2p is enabled or disabled. An extra EXTRA_WIFI_STATE provides
        // the state information as int.
        // WIFI_P2P_THIS_DEVICE_CHANGED_ACTION : Broadcast intent action indicating that this device details have changed.

        assert action != null;
        switch(action)
        {
            case WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION:
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

                if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                {
                    Toast.makeText(context, "Wifi is ON", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(context, "Wifi is OFF", Toast.LENGTH_SHORT).show();
                }

                break;

            case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                if(manager != null)
                {
                    if(ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                        return;
                    }

                    // requestPeers() : full list of current peers. Requires Manifest.permission.ACCESS_FINE_LOCATION.
                    manager.requestPeers(channel, mainActivity.peerListListener);
                }

                break;

            case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:

                if(ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, 1);

                    return ;
                }

                if(ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                    return ;
                }

                if(manager == null)
                {
                    return;
                }

                // NetworkInfo class was deprecated in API level 29.
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                assert networkInfo != null;
                if(networkInfo.isConnected())
                {
                    // requestConnectionInfo() : Request device connection info.
                    manager.requestConnectionInfo(channel, mainActivity.connectionInfoListener);
                }

                int info = getConnectionType();

                if(info != 0)
                {

                    // Do as per info value...
                }

                break;

            case WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION:


                break;

            default:
        }
    }

    @IntRange(from = 0, to = 3)
    private int getConnectionType()
    {
        // Returns connection type. 0: none, 1: mobile data, 2: wifi.
        int result = 0;

        if(Build.VERSION.SDK_INT >= 24)
        {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

            if(capabilities != null)
            {
                if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                {
                    result = 2;
                }
                else if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                {
                    result = 1;
                }
                else if(capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                {
                    result = 3;
                }
            }
        }
        else
        {
            if(connectivityManager != null)
            {
                NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

                if(activeNetwork != null)
                {
                    // connected to the internet.
                    if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                    {
                        result = 2;
                    }
                    else if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                    {
                        result = 1;
                    }
                    else if(activeNetwork.getType() == ConnectivityManager.TYPE_VPN)
                    {
                        result = 3;
                    }
                }
            }
        }

        return result;
    }
}