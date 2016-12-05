package jason.com.bledemo;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {


    private Button searchButton;
    private ListView mListView;

    private BluetoothAdapter bluetoothAdapter;
    private Handler mHandler;
    private static final int SCAN_TIME = 10000;
    private boolean mScanning = true;
    private List<SearchDeviceBean> saveSearchDeviceList;
    private SearchAdapter adpater;
    private static final int REQUEST_ENABLE_BT = 10;
    private ProgressDialog progress;

    private BroadcastReceiver searchDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        saveSearchDeviceList = new ArrayList<>();
        mListView = (ListView) findViewById(R.id.show_all_device);
        searchButton = (Button) findViewById(R.id.search_device_main);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = new Handler();
        adpater = new SearchAdapter(this, saveSearchDeviceList);
        mListView.setAdapter(adpater);
        mListView.setOnItemClickListener(this);
        progress = new ProgressDialog(this);
        progress.setMessage("please wait...");
        searchButton.setOnClickListener(this);
    }

    public void searchDevice(boolean enable) {

        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bluetoothAdapter.stopLeScan(mLeScanCallback);
                    mScanning = true;
                    progress.dismiss();
                    searchButton.setText("Search device");
                }
            }, SCAN_TIME);
            bluetoothAdapter.startLeScan(mLeScanCallback);
            searchButton.setText("Stop search");
            mScanning = false;
            progress.show();
        } else {
            bluetoothAdapter.stopLeScan(mLeScanCallback);
            searchButton.setText("search device");
            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : bondedDevices) {
                SearchDeviceBean bean = new SearchDeviceBean();
                bean.setAddress(device.getAddress());
                bean.setName(device.getName());
                saveSearchDeviceList.add(bean);
            }
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            if (saveSearchDeviceList.size() > 0) {
                for (int position = 0; position < saveSearchDeviceList.size(); position++) {
                    SearchDeviceBean searchDeviceBean = saveSearchDeviceList.get(position);
                    if (!searchDeviceBean.getName().equals(bluetoothDevice.getName() + "") | !searchDeviceBean.getAddress().equals(bluetoothDevice.getAddress())) {
                        SearchDeviceBean bean = new SearchDeviceBean();
                        bean.setName(bluetoothDevice.getName());
                        bean.setAddress(bluetoothDevice.getAddress());
                        saveSearchDeviceList.add(bean);
                    }
                }
            } else {
                SearchDeviceBean bean = new SearchDeviceBean();
                bean.setName(bluetoothDevice.getName());
                bean.setAddress(bluetoothDevice.getAddress());
                saveSearchDeviceList.add(bean);
            }


            adpater.notifyDataSetChanged();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT) {
            searchDevice(true);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        SearchDeviceBean searchDeviceBean = saveSearchDeviceList.get(i);
        Intent intent = new Intent(this, ConnectedDeviceActivity.class);
        intent.putExtra("name", searchDeviceBean.getName());
        intent.putExtra("address", searchDeviceBean.getAddress());
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            searchDevice(true);
        }
    }
}
