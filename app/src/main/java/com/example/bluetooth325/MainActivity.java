package com.example.bluetooth325;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bluetooth325.bluetooth.BluetoothChatService;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, BluetoothDevicesAdapter.ListItemClickListener {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final String TAG = "MainActivity";

    // https://www.youtube.com/watch?v=UrV0bSrkB5g&list=PLGmd9-PCMLhbYEkrW6-1GoQwykStSRQgS&index=3
    private BluetoothAdapter mBluetoothAdapter; // 獲取本機設備 4:08
    private Button mButtonEnable;
    private Button mButtonSendMessage;
    private Button mButtonOnVisibility;
    private Button mButtonOnScanDevice;
    private RecyclerView mRecyclerViewDevices;
    private askPermissions ask = new askPermissions();

    // 存放查找到的設備陣列
    private ArrayList<BluetoothDevice> mDiscoveredDevices = new ArrayList<>();
    private ArrayList<BluetoothDevice> mPairedDevices = new ArrayList<>();


    private BluetoothDevicesAdapter mBluetoothDevicesAdapter;
    private BluetoothChatService mBluetoothConnection;
    private BluetoothDevice mBluetoothDevice;
    private EditText mEditTextMessageToSend;
    private TextView mTextViewInbox;
    private TextView mTextView_message;


    // 蓝牙状态广播接收者
    private final BroadcastReceiver mBroadcastModeChanged = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                switch (status) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.e(TAG, "蓝牙已关闭");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.e(TAG, "蓝牙已打开");
                       // startDiscovery();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.e(TAG, "蓝牙关闭中...");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.e(TAG, "蓝牙打开中...");
                        break;
                    default:
                        break;
                }
            }
        }
    };

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastDeviceFound = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if ( device != null ) {
                    Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                    // Add the name and address to an array adapter to show in a ListView

                    mDiscoveredDevices.add(device); // 存放查找到的設備陣列

                    // 加入呈現的列表
                    mBluetoothDevicesAdapter.addDevice(mDiscoveredDevices);

                    // android.permission.BLUETOOTH_ADMIN
                    // device.createBound() Android 4.4 3:26
                }

            }
        }
    };

    // 蓝牙基本功能实现（开启，扫描，配对，连接等）
    // https://mcl-123.github.io/2019/03/16/%E8%93%9D%E7%89%99%E5%9F%BA%E6%9C%AC%E5%8A%9F%E8%83%BD%E5%AE%9E%E7%8E%B0/
    // Create a BroadcastReceiver for ACTION_SCAN_MODE_CHANGE
    private final BroadcastReceiver mBroadcastScanModeChanged = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,0);
            if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                Log.e(TAG,"设备可见监听");
                mButtonOnVisibility.setBackgroundColor(Color.GREEN);
            } else {
                Log.e(TAG,"设备不可见监听");
                mButtonOnVisibility.setBackgroundColor(Color.GRAY);
            }
        }
    };


    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver mBroadcastBond = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED.");
                    //inside BroadcastReceiver4
                    mBluetoothDevice = device;
                }
                //case2: creating a bone
                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING.");
                }
                //case3: breaking a bond
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE.");
                }
            }
        }
    };

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            byte[] readBuf = (byte[]) msg.obj;
            int numberOfBytes = msg.arg1;

            // construct a string from the valid bytes in the buffer
            String readMessage = new String(readBuf, 0, numberOfBytes);
            mTextView_message.setText(readMessage);
            Log.d(TAG, readMessage);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ask.askPermissions(MainActivity.this);

        mButtonEnable = (Button) findViewById(R.id.button_main_enablebluetooth);
        mButtonSendMessage = (Button) findViewById(R.id.button_main_sendmessage);
        mButtonOnVisibility = (Button) findViewById(R.id.button_main_onvisibility);

        mButtonOnScanDevice = (Button) findViewById(R.id.button_main_onscan);


        mRecyclerViewDevices = (RecyclerView) findViewById(R.id.reciclerview_main);
        mEditTextMessageToSend = (EditText) findViewById(R.id.edittext_main_messagetosend);
        mTextViewInbox = (TextView) findViewById(R.id.textview_main_inbox);
        mTextView_message = (TextView) findViewById(R.id.textview_message);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mButtonEnable.setOnClickListener(this);
        mButtonSendMessage.setOnClickListener(this);
        mButtonOnVisibility.setOnClickListener(this);
        mButtonOnScanDevice.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerViewDevices.setLayoutManager(layoutManager);
        mRecyclerViewDevices.setHasFixedSize(true);

        mDiscoveredDevices = new ArrayList<>();
        mBluetoothDevicesAdapter = new BluetoothDevicesAdapter(mDiscoveredDevices, this);

        mRecyclerViewDevices.setAdapter(mBluetoothDevicesAdapter);

      //  IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
      //  registerReceiver(mBroadcastDeviceFound, discoverDevicesIntent);
      //  mBluetoothAdapter.startDiscovery();

        //showPairedDevices();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastDeviceFound);
        unregisterReceiver(mBroadcastBond);
        unregisterReceiver(mBroadcastScanModeChanged);
        mBluetoothConnection.stop();
    }

    // 獲取可連線的設備
    public void showPairedDevices() {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
// If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            mPairedDevices = new ArrayList<>();
            mPairedDevices.addAll(pairedDevices);
            mBluetoothDevicesAdapter = new BluetoothDevicesAdapter(mPairedDevices, this);
        }
        mRecyclerViewDevices.setAdapter(mBluetoothDevicesAdapter);
    }

    // 打開設備 4:13
    private void enableBluetooth() {

        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastModeChanged, filter1);


        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastScanModeChanged, filter);

        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                mButtonEnable.setBackgroundColor(Color.RED);

                mButtonSendMessage.setEnabled(false);
                mButtonOnVisibility.setEnabled(true);
                mButtonOnScanDevice.setEnabled(true);
            } else {
                mBluetoothAdapter.disable();
                mButtonEnable.setBackgroundColor(Color.GREEN);

                mButtonSendMessage.setEnabled(false);
                mButtonOnVisibility.setEnabled(false);
                mButtonOnScanDevice.setEnabled(false);
            }
            // showPairedDevices();
        }
    }

    // 啟動設備為可見性 1:44, 4:28
    public void onVisibility() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }

    // https://www.youtube.com/watch?v=UrV0bSrkB5g&list=PLGmd9-PCMLhbYEkrW6-1GoQwykStSRQgS&index=3
    // Android蓝牙开发3 android蓝牙设备的查找与绑定
    // 查找設備 12s
    public void onDiscoveryScan() {
        mDiscoveredDevices = new ArrayList<>();
        mPairedDevices = new ArrayList<>();

        mBluetoothAdapter.startDiscovery();
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastDeviceFound, filter); // Don't forget to unregister during onDestroy
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void bondDevice(BluetoothDevice device) {
//Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastBond, filter);
        device.createBond();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_main_enablebluetooth:
                Log.d(TAG, "enableBluetooth().");
                enableBluetooth();
                break;
            case R.id.button_main_onvisibility:
                Log.d(TAG, "onVisibility().");
                onVisibility();
                break;
            case R.id.button_main_sendmessage:
                Log.d(TAG, "sendMessage().");
                sendMessage();
                break;
            case R.id.button_main_onscan:
                Log.d(TAG, "onDiscoveryScan().");
                onDiscoveryScan();
                break;
        }
    }

    private void sendMessage() {
        byte[] bytes = mEditTextMessageToSend.getText().
                toString().getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
    }


    @Override
    public void onListItemClick(int clickedItemIndex) {
        //first cancel discovery because its very memory intensive.
        //mBluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = mDiscoveredDevices.get(clickedItemIndex);
        //BluetoothDevice device = mPairedDevices.get(clickedItemIndex);

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = device.getName();
        String deviceAddress = device.getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        mBluetoothConnection = new BluetoothChatService(MainActivity.this , mHandler);
        Log.d(TAG, "Connecting with " + device.getName());
        //device.createBond();
        mBluetoothConnection.connect( device , true );
    }
}


