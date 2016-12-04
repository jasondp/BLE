package jason.com.bledemo;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by boy on 2016/12/4.
 */

public class SearchAdapter extends BaseAdapter {

    private Context mContext;
    private List<BluetoothDevice> list;

    public SearchAdapter(Context context, List<BluetoothDevice> list) {
        this.mContext = context;
        this.list = list;

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.search_item_adapter, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name = (TextView) convertView.findViewById(R.id.name);
        holder.address = (TextView) convertView.findViewById(R.id.address);
        BluetoothDevice device = list.get(position);
        if (device != null) {
            holder.name.setText(device.getName());
            holder.address.setText(device.getAddress());
        }
        return convertView;
    }

    static class ViewHolder {
        TextView name, address;
    }
}
