package com.saarimtech.findmyphone;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {
    Context context;
    List<DeviceListModel> list;

    public DeviceListAdapter(Context context, List<DeviceListModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public DeviceListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.phoneitems, parent, false);
        return new DeviceListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceListAdapter.ViewHolder holder, int position) {
        holder.devicename.setText(list.get(position).getDevicename());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context,showOnMap.class);
                intent.putExtra("lattitude",list.get(holder.getAdapterPosition()).getLattitude());
                intent.putExtra("longitude",list.get(holder.getAdapterPosition()).getLongitude());
                intent.putExtra("devicename",list.get(holder.getAdapterPosition()).getDevicename());
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView devicename;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            devicename=itemView.findViewById(R.id.textView5);

        }
    }
}
