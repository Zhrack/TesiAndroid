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
    private String firstParagraphText;
    private boolean wikiParserThreadAvailable;
    private ThreadPoolExecutor executor;
    private Context context;
    private TextToSpeech textToSpeech;

    private Handler UIHandler;

    private LayoutInflater mInflater;

    private ListAdapter adapter;

    public ListAdapter(ArrayList<PointInfo> list, Context context, Handler handler) {
        this.adapter = this;
        this.list = list;
        this.context = context;
        this.UIHandler = handler;

        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_CORES);

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

    public void updateWikiText(int index, String text)
    {
        list.get(index).setWikiText(text);
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
        holder.audioBtn.setOnClickListener(null);

        final int pos = position;
        final ViewHolder hold = holder;
        final PointInfo data = list.get(pos);
        switch (data.getWikiLoaded())
        {
            default:
            case PointInfo.WIKI_NOT_PRESENT:
                Log.d(TAG, data.getName() + ": WIKI NOT PRESENT " + String.valueOf(pos));
                hold.audioBtn.setVisibility(View.GONE);
                break;
            case PointInfo.WIKI_TO_PROCESS:
                Log.d(TAG, data.getName() + ": WIKI TO PROCESS " + String.valueOf(pos));

            case PointInfo.WIKI_READY:
                hold.audioBtn.setVisibility(View.VISIBLE);
                holder.audioBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // ListView Clicked item value

                        switch (data.getWikiLoaded())
                        {
                            case PointInfo.WIKI_TO_PROCESS:
                                // start thread to connect to wikipedia
                                wikiParserThreadAvailable = false;

                                executor.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Document doc = Jsoup.connect(AppController.http + data.getLanguage() + AppController.urlWiki + data.getWikiText()).get();

                                            Elements paragraphs = doc.select("#mw-content-text div > p");

                                            Element firstParagraph = paragraphs.first();
                                            firstParagraphText = firstParagraph.text();
                                            // sometimes the coordinates are taken as first paragraph, check for that
                                            if(firstParagraphText.startsWith("Coord"))
                                            {
                                                firstParagraph = paragraphs.get(1);
                                                firstParagraphText = firstParagraph.text();
                                            }

                                            Log.d(TAG, firstParagraphText);


                                            UIHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    adapter.updateWikiText(pos, firstParagraphText);

                                                    PointInfo tempData = list.get(pos);
                                                    tempData.setWikiLoaded(PointInfo.WIKI_READY);
                                                    data.setAudioButtonVisible(true);
                                                    speak(tempData);
                                                    wikiParserThreadAvailable = true;
                                                }
                                            });

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            UIHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
//                                                    adapter.updateWikiText(pos, "");
                                                    data.setWikiText("");
                                                    data.setWikiLoaded(PointInfo.WIKI_NOT_PRESENT);
                                                    data.setAudioButtonVisible(false);
                                                    hold.audioBtn.setVisibility(View.GONE);
                                                    wikiParserThreadAvailable = true;

                                                    Toast.makeText(context, R.string.wiki_not_available, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }
                                });
                                break;
                            case PointInfo.WIKI_READY:
                                Log.d(TAG, "WIKI READY");
                                speak(data);
                                break;
                            default:
                                Log.d(TAG, "wiki loaded status doesn't exist: " + String.valueOf(data.getWikiLoaded()));
                                break;
                        }
                    }
                });
                break;
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
}
