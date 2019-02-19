package com.zbform.penform.adapter;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tstudy.blepenlib.BlePenManager;
import com.tstudy.blepenlib.data.BleDevice;
import com.zbform.penform.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeviceAdapter extends BaseAdapter {

    private Context context;
    private List<BleDevice> bleDeviceList;

    public DeviceAdapter(Context context) {
        this.context = context;
        bleDeviceList = new ArrayList<>();
    }

    public void addDevice(BleDevice bleDevice) {
        removeDevice(bleDevice);
        bleDeviceList.add(bleDevice);
        //正序比较
        Collections.sort(bleDeviceList);
    }

    public void addDevice(int position, BleDevice bleDevice) {
        removeDevice(bleDevice);
        if (position == 0) {
            bleDeviceList.add(position, bleDevice);

        } else {
            bleDeviceList.add(bleDevice);
            //正序比较
            Collections.sort(bleDeviceList);
        }
    }

    public void addDevice(List<BleDevice> scanResultList) {
//        removeDevice(bleDevice);
//        bleDeviceList.add(bleDevice);
        bleDeviceList.addAll(scanResultList);
    }

    public void removeDevice(BleDevice bleDevice) {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (bleDevice.getKey().equals(device.getKey())) {
                bleDeviceList.remove(i);
            }
        }
    }

    public void clearConnectedDevice() {
        List<BleDevice> bleDeviceList2 = new ArrayList<BleDevice>();

        int size = bleDeviceList.size();
        for (int i = 0; i < size; i++) {
            BleDevice device = bleDeviceList.get(i);
            if (BlePenManager.getInstance().isConnected(device)) {
                bleDeviceList2.add(device);
            }
        }
        bleDeviceList.removeAll(bleDeviceList2);
    }

    public void clearScanDevice() {
        List<BleDevice> bleDeviceList2 = new ArrayList<BleDevice>();

        int size = bleDeviceList.size();
        for (int i = 0; i < size; i++) {
            BleDevice device = bleDeviceList.get(i);
            if (!BlePenManager.getInstance().isConnected(device)) {
                bleDeviceList2.add(device);
            }
        }
        bleDeviceList.removeAll(bleDeviceList2);



    }

    public void clear() {
        clearConnectedDevice();
        clearScanDevice();
    }

    @Override
    public int getCount() {
        return bleDeviceList.size();
    }

    @Override
    public BleDevice getItem(int position) {
        if (position > bleDeviceList.size()) {
            return null;
        }
        return bleDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = View.inflate(context, R.layout.adapter_device, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.img_blue = convertView.findViewById(R.id.img_blue);
            holder.txt_name = convertView.findViewById(R.id.txt_name);
            holder.txt_mac = convertView.findViewById(R.id.txt_mac);
            holder.layout_idle = convertView.findViewById(R.id.layout_idle);
            holder.btn_connect = convertView.findViewById(R.id.btn_connect);
        }

        final BleDevice bleDevice = getItem(position);
        if (bleDevice != null) {
            boolean isConnected = BlePenManager.getInstance().isConnected(bleDevice);
            String name = bleDevice.getName();
            String mac = bleDevice.getMac();
            holder.txt_name.setText(name);
            holder.txt_mac.setText(mac);
            if (isConnected) {
//                holder.img_blue.setImageResource(R.mipmap.ic_blue_connected);
                holder.txt_name.setTextColor(0xFF1DE9B6);
                holder.txt_mac.setTextColor(0xFF1DE9B6);
                holder.layout_idle.setVisibility(View.GONE);
            } else {
//                holder.img_blue.setImageResource(R.mipmap.ic_blue_remote);
                holder.txt_name.setTextColor(0xFF000000);
                holder.txt_mac.setTextColor(0xFF000000);
                holder.layout_idle.setVisibility(View.VISIBLE);
            }
        }

        holder.btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onConnect(bleDevice);
                }
            }
        });


        return convertView;
    }

    class ViewHolder {
        ImageView img_blue;
        TextView txt_name;
        TextView txt_mac;
        LinearLayout layout_idle;
        TextView btn_connect;
    }

    public interface OnDeviceClickListener {
        void onConnect(BleDevice bleDevice);
    }

    private OnDeviceClickListener mListener;

    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.mListener = listener;
    }

}
