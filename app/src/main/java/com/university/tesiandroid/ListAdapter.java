package com.university.tesiandroid;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Davide on 31/03/2016.
 */
public class ListAdapter extends BaseAdapter {
    private ArrayList<PointInfo> list;
    private Context context;

    private LayoutInflater mInflater;

    public ListAdapter(ArrayList<PointInfo> list, Context context) {
        this.list = list;
        this.context = context;

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int pos) {
        return 0;
    }

    public void updateList(ArrayList<PointInfo> newlist) {
        list.clear();
        list.addAll(newlist);
        this.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            Log.d("Test", "convertView null");
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.txtName = (TextView) convertView.findViewById(R.id.textName);
            holder.txtDistance = (TextView) convertView.findViewById(R.id.textDistance);
//            holder.txtLat = (TextView) convertView.findViewById(R.id.textLat);
//            holder.txtLong = (TextView) convertView.findViewById(R.id.textLong);
            holder.txtTags = (TextView) convertView.findViewById(R.id.textTags);

            convertView.setTag(holder);
        } else {
            Log.d("Test", "convertView OK");
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtName.setText("Name: " + list.get(position).getName());
        holder.txtDistance.setText("Dist: " + String.valueOf(list.get(position).getDistance()) + " m");
//        holder.txtLat.setText("Lat: " + String.valueOf(list.get(position).getLatitude()));
//        holder.txtLong.setText("Long: " + String.valueOf(list.get(position).getLongitude()));
        holder.txtTags.setText("Tag: " + list.get(position).getWikiText());

        return convertView;
    }

    static class ViewHolder {

        TextView txtName;
        TextView txtDistance;
//        TextView txtLat;
//        TextView txtLong;
        TextView txtTags;
    }
}
