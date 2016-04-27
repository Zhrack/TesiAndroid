package com.university.tesiandroid;

import android.content.Context;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Davide on 31/03/2016.
 */
public class ListAdapter extends BaseAdapter {
    // LogCat tag
    private static final String TAG = ListAdapter.class.getSimpleName();

    private ArrayList<PointInfo> list;
    private boolean wikiParserThreadAvailable;
    private Context context;
    private TextToSpeech textToSpeech;

    private LayoutInflater mInflater;

    public ListAdapter(ArrayList<PointInfo> list, Context context, Handler handler) {
        this.list = list;
        this.context = context;

        textToSpeech=new TextToSpeech(this.context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        wikiParserThreadAvailable = true;

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
//            Log.d("Test", "convertView null");
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.txtName = (TextView) convertView.findViewById(R.id.textName);
            holder.txtDistance = (TextView) convertView.findViewById(R.id.textDistance);
            holder.audioBtn = (ImageButton)convertView.findViewById(R.id.audio_btn);

            convertView.setTag(holder);
        } else {
//            Log.d("Test", "convertView OK");
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtName.setText(list.get(position).getName());
        holder.txtDistance.setText(String.valueOf(list.get(position).getDistance()) + " m");

        final PointInfo data = list.get(position);

        if(data.getLanguage().equals("")) // item doesn't have wiki, deactivate button
        {
            hideAudioButton(holder);
        }
        else
        {
            showAudioButton(holder);
            holder.audioBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ListView Clicked item value
                    speak(data);
                }
            });
        }
        return convertView;
    }

    static class ViewHolder {
        TextView txtName;
        TextView txtDistance;
        ImageButton audioBtn;
    }

    public boolean isWikiParserThreadAvailable() {
        return wikiParserThreadAvailable;
    }

    public void setWikiParserThreadAvailable(boolean wikiParserThreadAvailable)
    {
        this.wikiParserThreadAvailable = wikiParserThreadAvailable;
    }

    public void speak(PointInfo data)
    {
        if(data.getLanguage().equals("it"))
            textToSpeech.setLanguage(Locale.ITALY);
        else if(data.getLanguage().equals("en"))
            textToSpeech.setLanguage(Locale.UK);
        else
            Log.d(TAG, "wiki language don't available: " + data.getLanguage());

        textToSpeech.speak(data.getWikiText(), TextToSpeech.QUEUE_FLUSH, null);
    }

    public void showAudioButton(ViewHolder holder)
    {
        holder.audioBtn.setAlpha(1.0f);
    }

    public void hideAudioButton(ViewHolder holder)
    {
        holder.audioBtn.setAlpha(0.0f);
        holder.audioBtn.setOnClickListener(null);
    }

}
