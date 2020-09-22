package com.example.wifi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
    public static final int MESSAGE_READ = 1;
    private static final int PORT = 3000;
    public static final int VERIFY_PERMISSION_REQUEST = 12;
    private static final String TAG = "MainActivity";

    private Button btnOff, btnDiscover, btnSend;
    private ListView listView;
    private TextView readMsgBox, connectionStatus;
    private EditText writeMsg;
    private WifiManager wifiManager;

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    private List<WifiP2pDevice> wifiP2pDeviceArrayList = new ArrayList<>();
    private String[] deviceNameArray;
    private WifiP2pDevice[] deviceArray;

    private ServerClass serverClass;
    private ClientClass clientClass;
    private SendReceive sendReceive;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidget();
        executeListener();
    }

    private void initWidget()
    {
        btnOff = findViewById(R.id.onOff);
        btnDiscover = findViewById(R.id.discover);
        btnSend = findViewById(R.id.sendButton);
        listView = findViewById(R.id.peerListView);
        readMsgBox = findViewById(R.id.readMsg);
        connectionStatus = findViewById(R.id.connectionStatus);
        writeMsg = findViewById(R.id.writeMsg);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // WifiP2pManager class provides the API for managing wifi p2p connectivity.
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);

        assert wifiP2pManager != null;

        // WifiP2pManager.Channel : A channel that connects the application to the wifi p2p framework. Most p2p operations require a channel as an
        // argument. For anyone who have the same problem. Please make sure you are running the project with real android device not emulator.
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

        broadcastReceiver = new WifiBroadcastReceiver(wifiP2pManager, channel, this, connectivityManager);

        intentFilter = new IntentFilter();

        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    // A Handler allows you to send and process messages and runnable objects associated with a thread's Message queue. A handler instance is associated with a
    // single thread and that thread's message queue. When you create a new handler, it is bound to the thread/ message queue of the thread that is creating it.
    // It will deliver messages and runnable(s) to that message queue and executes them as they come out of the message queue. We need only one handler object
    // per Activity, and there is no need to manually register it. Your background thread can communicate with the handler, which will do all of its work on
    // the Activity's UI thread. In this article, you will learn how to use Handler in Android.

    // Uses of Handler :-
    // 1) To schedule messages and runnable(s) to be executed at some point in the future.
    // 2) To enqueue an action to be performed on a different than your own.
    // 3) Two options for communicating with the Handler are messages and Runnable objects.

    // What is a message?
    // Message contains a description and  an arbitrary data object that can be sent to a handler. While the construction of a message is public, the best
    // way to get one of these is to call Message.obtains() or one of the Handler.obtainMessage() method which will pull them from a pool of recycled objects.

    // How to send message objects?
    // 1) sendMessage() put the message in the queue immediately.
    // 2) sendMessageAtFrontOfQueue() puts the messages in the queue immediately and places the message at the front of all the messages. So your message will
    // have the highest priority than other messages.
    // 3) sendMessageAtTime() Put the message in the queue at the started time.
    // 4) sendMessageDelayed() put the messages in the queue after a delay, expressed in milliseconds.
    // 5) sendEmptyMessage() send an empty message to the queue

    // To process these messages the handler needs to implement handlerMessage(), which will be called with each message that appears on the message queue.

    Handler handler = new Handler(new Handler.Callback()
    {
        // Subclasses must implement this to receive messages.
        @Override
        public boolean handleMessage(@NonNull Message message)
        {
            switch(message.what)
            {
                case MESSAGE_READ:
                    byte[] readBuffer = (byte[]) message.obj;
                    String tempMsg = new String(readBuffer, 0, message.arg1);

                    readMsgBox.setText(tempMsg);

                    break;
            }

            return true;
        }
    });

    private void executeListener()
    {
        btnOff.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(wifiManager.isWifiEnabled())
                {
                    // This method was deprecated in API level 29. Starting with Build.VERSION_CODES#Q, applications are not allowed to enable/disable Wi-Fi.
                    // Compatibility Note: For applications targeting Build.VERSION_CODES.Q or above, this API will always fail and return false. If apps are
                    // targeting an older SDK (Build.VERSION_CODES.P or below), they can continue to use this API.
                    wifiManager.setWifiEnabled(false);
                    btnOff.setText("ON");
                }
                else
                {
                    wifiManager.setWifiEnabled(true);
                    btnOff.setText("OFF");
                }
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, VERIFY_PERMISSION_REQUEST);

                    return;
                }

                wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener()
                {
                    @Override
                    public void onSuccess()
                    {
                        connectionStatus.setText("Discovery Started");
                    }

                    @Override
                    public void onFailure(int i)
                    {
                        connectionStatus.setText("Discovery Failed");
                    }
                });
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                final WifiP2pDevice wifiP2pDevice = deviceArray[i];

                // WifiP2pConfig is a class representing a Wi-Fi P2p configuration for setting up a connection.
                WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
                wifiP2pConfig.deviceAddress = wifiP2pDevice.deviceAddress;
                wifiP2pConfig.wps.setup = WpsInfo.PBC;

                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, VERIFY_PERMISSION_REQUEST
                    );

                    return;
                }

                // connect() method will tell you about the success or the failure of the connection. Start a p2p connection to a device with the specified
                // configuration.
                wifiP2pManager.connect(channel, wifiP2pConfig, new WifiP2pManager.ActionListener()
                {
                    @Override
                    public void onSuccess()
                    {
                        Toast.makeText(MainActivity.this, "Connected to " + wifiP2pDevice.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i)
                    {
                        Toast.makeText(MainActivity.this, "Connect failed. Retry", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final String msg = writeMsg.getText().toString();

                // Error : NetworkOnMainThreadException --> The exception that is thrown when an application attempts to perform a networking operation on
                // its main thread. sendReceive.write() is a network operation hence wrapped inside another Thread.
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            sendReceive.write(msg.getBytes());
                        }
                        catch(IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }

    // For callback when peer list is available. Can be null.
    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener()
    {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList)
        {
            if(!wifiP2pDeviceList.getDeviceList().equals(wifiP2pDeviceArrayList))
            {
                // Log.d(TAG, "onPeersAvailable: " + wifiP2pDeviceList.getDeviceList());

                wifiP2pDeviceArrayList.clear();
                wifiP2pDeviceArrayList.addAll(wifiP2pDeviceList.getDeviceList());

                deviceNameArray = new String[wifiP2pDeviceList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];

                int index = 0;

                for(WifiP2pDevice wifiP2pDevice : wifiP2pDeviceList.getDeviceList())
                {
                    deviceArray[index] = wifiP2pDevice;
                    deviceNameArray[index] = wifiP2pDevice.deviceName;

                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);

                listView.setAdapter(adapter);
            }

            if(wifiP2pDeviceArrayList.size() == 0)
            {
                Toast.makeText(MainActivity.this, "No device found", Toast.LENGTH_SHORT).show();
            }
        }
    };

    // Interface for callback invocation when connection info is available.
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener()
    {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo)
        {
            // This class represents an Internet Protocol (IP) address. WifiP2pInfo is a class representing connection information about a Wi-Fi p2p group.
            // WifiP2pGroup is a class representing a Wi-Fi P2p group. A p2p group consists of a single group owner and one or more clients. In the case of
            // a group with only two devices, one will be the group owner and the other will be a group client. When determining which device should be the
            // group owner for the network, Wi-Fi Direct examines each device's power management, UI, and service capabilities and uses this information to
            // choose the device that can handle server responsibilities most effectively.
            InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;

            // groupFormed : Indicates if a p2p group has been successfully formed. isGroupOwner : Indicates if the current device is the group owner.
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)
            {
                connectionStatus.setText("Server");

                serverClass = new ServerClass();
                serverClass.start();
            }
            else if(wifiP2pInfo.groupFormed)
            {
                connectionStatus.setText("Client");

                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
            }
        }
    };

    public class ClientClass extends Thread
    {
        // Create a client Socket. The client uses the IP address and port of the server socket to connect to the server device. This class implements client
        // sockets (also called just "sockets"). A socket is an endpoint for communication between two machines.
        private Socket socket;
        private String hostAddress;

        public ClientClass(InetAddress hostAddress)
        {
            this.hostAddress = hostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run()
        {
            super.run();

            try
            {
                socket.bind(null);
                socket.connect(new InetSocketAddress(hostAddress, PORT), 2000);

                sendReceive = new SendReceive(socket);
                sendReceive.start();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public class ServerClass extends Thread
    {
        @Override
        public void run()
        {
            super.run();

            try
            {
                // Create a ServerSocket. This socket waits for a connection from a client on a specified port and blocks until it happens, so do this in a
                // background thread ( so created a separate Thread class ).
                ServerSocket serverSocket = new ServerSocket(PORT);

                // The server socket waits for a client connection (with the accept() method). This call blocks until a client connects, so call this is
                // another thread. When a connection happens, the server device can receive the data from the client. Carry out any actions with this data,
                // such as saving it to a file or presenting it to the user.
                Socket socket = serverSocket.accept();

                sendReceive = new SendReceive(socket);
                sendReceive.start();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    // Send data from the client to the server. When the client socket successfully connects to the server socket, you can send data from the client to the
    // server with byte stream.
    public class SendReceive extends Thread
    {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket socket)
        {
            this.socket = socket;

            try
            {
                // public InputStream getInputStream () : Returns an input stream for this socket.
                // If this socket has an associated channel then the resulting input stream delegates all of its operations to the channel. If the channel is
                // in non-blocking mode then the input stream's read operations will throw an IllegalBlockingModeException. Closing the returned InputStream
                // will close the associated socket.
                inputStream = socket.getInputStream();

                // public OutputStream getOutputStream () : Returns an output stream for this socket.
                // If this socket has an associated channel then the resulting output stream delegates all of its operations to the channel. If the channel is
                // in non-blocking mode then the output stream's write operations will throw an IllegalBlockingModeException. Closing the returned OutputStream
                // will close the associated socket.
                outputStream = socket.getOutputStream();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;

            while(socket != null)
            {
                try
                {
                    // public int read (byte[] b) : Reads some number of bytes from the input stream and stores them into the buffer array b. The number
                    // of bytes actually read is returned as an integer. This method blocks until input data is available, end of file is detected, or an
                    // exception is thrown. If the length of b is zero, then no bytes are read and 0 is returned; otherwise, there is an attempt to read at
                    // least one byte. If no byte is available because the stream is at the end of the file, the value -1 is returned; otherwise, at least
                    // one byte is read and stored into b.
                    bytes = inputStream.read(buffer);

                    if(bytes != -1)
                    {
                        // Message contains a description and  an arbitrary data object that can be sent to a handler. While the construction of a message is
                        // public, the best way to get one of these is to call Message.obtains() or one of the Handler.obtainMessage() method which will pull
                        // them from a pool of recycled objects.

                        // public final Message obtainMessage (int what, int arg1, int arg2, Object obj).
                        Message message = handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer);

                        handler.sendMessage(message);
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) throws IOException
        {
            // public void write (byte[] b) : Writes b.length bytes from the specified byte array to this output stream. The general contract for write(b)
            // is that it should have exactly the same effect as the call write(b, 0, b.length). write() will write the data to the outputStream instance
            // and same data will be read using inputStream instance. Both inputStream and outputStream instances are initialized using socket. User will
            // not receive his own message means say if you are the user and when you send some message ( i.e writing to the outputStream ) then that
            // message will be sent to all the users who are connected through the wifi but that message will not be received to the user himself.
            outputStream.write(bytes);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        unregisterReceiver(broadcastReceiver);
    }
}