package jason.com.bledemo;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by boy on 2016/12/4.
 */
public class ConnectedDeviceService extends Service {

    public final static String ACTION_GATT_CONNECTED =
            "jason.com.mybluetoothdemo.GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "jason.com.mybluetoothdemo.GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "jason.com.mybluetoothdemo.GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "jason.com.mybluetoothdemo.DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "jason.com.mybluetoothdemo.DATA";

    private BluetoothGatt bluetoothGatt;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mRemoteDevice;

    //    public final static UUID UUID_HEART_RATE_MEASUREMENT =
    //            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getInstance();
    }

    public class MyBinder extends Binder {
        ConnectedDeviceService getService() {
            return ConnectedDeviceService.this;
        }
    }

    public void connectDevice(String address) {
        mRemoteDevice = mBluetoothAdapter.getRemoteDevice(address);
        bluetoothGatt = mRemoteDevice.connectGatt(getApplicationContext(), false, bluetoothGattCallBack);
    }

    private void getInstance() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    /**
     * 获取全部的服务
     */
    public List<BluetoothGattService> getDeviceAllService() {
        if (bluetoothGatt == null)
            return null;
        return bluetoothGatt.getServices();
    }


    /**
     * 蓝牙连接成功后的回掉
     */
    private BluetoothGattCallback bluetoothGattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            /**
             * 链接状态改变的回调
             * BluetoothProfile保存着ble的profile
             * STATE_CONNECTED 当前装填链接
             * STATE_DISCONNECTED当前状态为断开
             */
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //使用gatt读取服务
                bluetoothGatt.discoverServices();
                sendBroadcastTellActivity(ACTION_GATT_CONNECTED);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {

                sendBroadcastTellActivity(ACTION_GATT_DISCONNECTED);
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            /**
             * 读取到服务的回调
             * BluetoothGatt.GATT_SUCCESS代表通道打通，发现设备服务
             */
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendBroadcastTellActivity(ACTION_GATT_SERVICES_DISCOVERED);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            /**
             * 读取到特征值的回调
             * 读取到特征值使用广播将其发送出去
             */
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendBroadcastTellActivity(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            /**
             * 为特征写入数据的回调
             */
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            /**
             * 特征变化的回调
             * 特征值变化发送新读取到的特征值
             */
            sendBroadcastTellActivity(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    private void sendBroadcastTellActivity(String actionDataAvailable, BluetoothGattCharacteristic characteristic) {
        Intent intent = new Intent(actionDataAvailable);
        /**
         * 解析UUid并通过广播发送
         */
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

    public void close() {
        if (bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    /**
     * @param characteristic
     * @param enabled
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }


    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }
        /**
         * 读取特征里面的值
         */
        bluetoothGatt.readCharacteristic(characteristic);
    }

    private void sendBroadcastTellActivity(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }
}
