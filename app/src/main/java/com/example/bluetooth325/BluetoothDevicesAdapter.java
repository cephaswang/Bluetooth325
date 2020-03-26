package com.example.bluetooth325;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;



/**
 * Created by joaquinalan on 21/05/2017.
 */

public class BluetoothDevicesAdapter extends RecyclerView.Adapter<BluetoothDevicesAdapter.BluetoothDeviceViewHolder> {
    private static final String TAG = "Adapter RecyclerView";
    //private ArrayList<BluetoothDevice> mDevices;
    private ArrayList<BluetoothDevice>  mDevices;
    private final ListItemClickListener mOnClickListener;

    public BluetoothDevicesAdapter(ArrayList devices, ListItemClickListener mOnClickListener) {
        this.mDevices = devices;
        this.mOnClickListener = mOnClickListener;
    }

    public void addDevice(ArrayList devices) {
        mDevices = devices;
        //mDevices.add(device);
        notifyDataSetChanged();
        //notifyItemInserted(mDevices.size() - 1);
//        for (BluetoothDevice dev : mDevices) {
//            Log.d(TAG, dev.getName());
//        }

    }

    @Override
    public BluetoothDeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForNumberItem = R.layout.item_device;
        LayoutInflater inflater = LayoutInflater.from(context);

        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForNumberItem, parent, shouldAttachToParentImmediately);
        BluetoothDeviceViewHolder viewHolder = new BluetoothDeviceViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BluetoothDeviceViewHolder holder, int position) {
        BluetoothDevice device = mDevices.get(position);
        String name = device.getName();

        holder.mTextViewDeviceName.setText(name);
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }

    public class BluetoothDeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView mTextViewDeviceName;

        public BluetoothDeviceViewHolder(View itemView) {
            super(itemView);
            mTextViewDeviceName = (TextView) itemView.findViewById(R.id.textview_number);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mOnClickListener.onListItemClick(getAdapterPosition());
        }
    }

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }
}
