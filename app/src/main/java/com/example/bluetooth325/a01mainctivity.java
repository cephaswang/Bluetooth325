package com.example.bluetooth325;

// 蓝牙基本功能实现（开启，扫描，配对，连接等）
// https://mcl-123.github.io/2019/03/16/%E8%93%9D%E7%89%99%E5%9F%BA%E6%9C%AC%E5%8A%9F%E8%83%BD%E5%AE%9E%E7%8E%B0/

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;


@SuppressLint("Registered")
public class a01mainctivity extends AppCompatActivity {

    private static final int STATE_CONNECTED = 1;
    private final String TAG  = "a01mainctivity";
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private askPermissions ask = new askPermissions();
    private boolean isBluetoothConnected = false;

    // 存放查找到的設備陣列
    private ArrayList<BluetoothDevice> myBlueToothDevices = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ask.askPermissions(a01mainctivity.this);

        // 设备是否支持蓝牙功能
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "不支持蓝牙模块", Toast.LENGTH_SHORT).show();
            return;
        }

        // 蓝牙的打开
        if (bluetoothAdapter.isEnabled()) {
            // 蓝牙已打开
        } else {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // 打开蓝牙的同时想设置让自己手机蓝牙多少秒可见可以使用，默认120s，最多300s
            intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(intent);
        }

    }

    // 蓝牙搜索
    public void blueScan () {
        // 如果是在搜索的状态，则需要取消搜索
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        // 搜索蓝牙
        bluetoothAdapter.startDiscovery();

        // 本地蓝牙名称
        String name = bluetoothAdapter.getName();
        // 本地蓝牙地址
        String address = bluetoothAdapter.getAddress();
    }

    public void  changeBluetoothStatus (){

    }

    public void  startDiscovery (){
        // 搜索蓝牙
        bluetoothAdapter.startDiscovery();
    }


// 蓝牙设备广播接收者
        private BroadcastReceiver deviceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    Log.e(TAG,"开始搜索");

                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    Log.e(TAG,"查找到设备完成");

                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    String name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                    if (name != null) {
                        //  https://developer.android.com/reference/android/bluetooth/BluetoothDevice#EXTRA_UUID
                        int rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                        int uuid = intent.getExtras().getShort(BluetoothDevice.EXTRA_UUID);

                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        // MyBlueToothDevice myDevice = new MyBlueToothDevice(device, name, rssi);
                        myBlueToothDevices.add(device);
                        Log.e("=====","搜索到设备: " + name);
                    }

                } else if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                    int scanMode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,0);
                    if (scanMode == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        Log.e(TAG,"设备可见监听");
                    } else {
                        Log.e(TAG,"设备不可见监听");
                    }

                } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (remoteDevice == null) {
                        Log.e(TAG,"没有绑定设备");
                        return;
                    }

                    int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,0);
                    if (status == BluetoothDevice.BOND_BONDED) {
                        Log.e(TAG,"绑定设备完成: " + remoteDevice.getName());
                    } else if (status == BluetoothDevice.BOND_BONDING) {
                        Log.e(TAG,"绑定设备中: " + remoteDevice.getName());
                    } else if (status == BluetoothDevice.BOND_NONE) {
                        Log.e(TAG,"取消绑定: ");
                    }
                } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                    isBluetoothConnected = true;
                    changeBluetoothStatus();
                    Log.e(TAG,"已经连接上");
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    Log.e(TAG,"断开连接");
                    isBluetoothConnected = false;
                    changeBluetoothStatus();
                }
            }
        };


// 蓝牙状态广播接收者
    private BroadcastReceiver statusReceiver  = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (status) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.e(TAG, "蓝牙已关闭");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.e(TAG, "蓝牙已打开");
                        startDiscovery();
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
    };


    public void reg(){
        // 注册广播
        IntentFilter deviceIntentFilter = new IntentFilter();
        deviceIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        deviceIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        deviceIntentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        deviceIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        deviceIntentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        deviceIntentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        deviceIntentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        deviceIntentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(deviceReceiver, deviceIntentFilter);

        IntentFilter stateIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(statusReceiver, stateIntentFilter);
    }

    // 判断是否已连接蓝牙设备
    public static boolean isConnected() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Method getConnectionState = adapter.getClass().getMethod("getConnectionState");
            int state = (int) getConnectionState.invoke(adapter);
            Log.e("TAG", "isConnected: " + (state == BluetoothProfile.STATE_CONNECTED));
            return state == STATE_CONNECTED;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取已连接蓝牙设备
    public static BluetoothDevice getConnectedDevice() {
        if (!isConnected()) {
            return null;
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();

        try {
            Method isConnectedMethod = BluetoothDevice.class.getMethod("isConnected");
            for (BluetoothDevice device : bondedDevices) {
                boolean isConnected = (boolean) isConnectedMethod.invoke(device);
                if (isConnected) {
                    return device;
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }

}
