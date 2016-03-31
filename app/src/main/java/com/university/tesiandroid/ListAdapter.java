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
    private ArrayList<BookInfo> list;
    private Context context;

    private LayoutInflater mInflater;

    public ListAdapter(ArrayList<BookInfo> list, Context context) {
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

    public void updateList(ArrayList<BookInfo> newlist) {
        list.clear();
        list.addAll(newlist);
        this.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            Log.d("Test", "convertView null");
            convertView = mInflater.inflate(R.layout.home_list_item, null);
            holder = new ViewHolder();
            holder.txtTitolo = (TextView) convertView.findViewById(R.id.titolo);

            convertView.setTag(holder);
        } else {
            Log.d("Test", "convertView OK");
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtTitolo.setText(list.get(position).getTitolo());

        return convertView;
    }

    static class ViewHolder {

        TextView txtTitolo;
    }
}
