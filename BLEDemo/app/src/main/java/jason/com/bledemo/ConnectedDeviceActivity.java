package jason.com.bledemo;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by boy on 2016/12/4.
 */

public class ConnectedDeviceActivity extends Activity {

    private String name, address, data;
    private TextView nameTv, addressTv, stateTv, dataTv;
    private ConnectedDeviceService mConnectedService;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<>();
    private ExpandableListView mGattServicesList;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private BroadcastReceiver bluetoothListenerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ConnectedDeviceService.ACTION_GATT_CONNECTED:
                    stateTv.setText("connected");
                    break;
                case ConnectedDeviceService.ACTION_GATT_DISCONNECTED:
                    stateTv.setText("disconnected");
                    break;
                case ConnectedDeviceService.ACTION_GATT_SERVICES_DISCOVERED:
                    List<BluetoothGattService> deviceAllService = mConnectedService.getDeviceAllService();
                    setExpandableData(deviceAllService);
                    break;
                case ConnectedDeviceService.ACTION_DATA_AVAILABLE:
                    data = intent.getStringExtra(ConnectedDeviceService.EXTRA_DATA);
                    if (data != null) {
                        dataTv.setText(data);
                    }
                    break;

            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connected_activity);
        init();
        initData();

    }

    private void init() {
        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        name = intent.getStringExtra("name");
        connectedDevice();
        nameTv = (TextView) findViewById(R.id.connected_device_name);
        addressTv = (TextView) findViewById(R.id.show_device_address);
        stateTv = (TextView) findViewById(R.id.connected_device_state);
        dataTv = (TextView) findViewById(R.id.show_device_service_data);
        mGattServicesList = (ExpandableListView) findViewById(R.id.show_device_add_service);
        mGattServicesList.setOnChildClickListener(servicesListClickListener);

    }

    private void initData() {
        nameTv.setText("Name :" + name);
        addressTv.setText("Address:" + address);
    }

    private void connectedDevice() {
        Intent intent = new Intent(this, ConnectedDeviceService.class);
        bindService(intent, myServiceConnection, Service.BIND_AUTO_CREATE);
    }

    private ServiceConnection myServiceConnection = new ServiceConnection() {


        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mConnectedService = ((ConnectedDeviceService.MyBinder) iBinder).getService();
            if (mConnectedService != null) {
                mConnectedService.connectDevice(address);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onResume() {
        super.onPostResume();
        registerReceiver(bluetoothListenerReceiver, getDiverseFilter());
        if (mConnectedService != null) {
            mConnectedService.connectDevice(address);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(bluetoothListenerReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(myServiceConnection);
    }

    public IntentFilter getDiverseFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectedDeviceService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(ConnectedDeviceService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(ConnectedDeviceService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(ConnectedDeviceService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void setExpandableData(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<>();
        mGattCharacteristics = new ArrayList<>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private final ExpandableListView.OnChildClickListener servicesListClickListener =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mConnectedService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mConnectedService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mConnectedService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
            };


}
