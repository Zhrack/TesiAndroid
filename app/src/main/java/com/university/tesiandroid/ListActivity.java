package com.university.tesiandroid;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListActivity extends Activity {

    private ListView listView;

    private ListAdapter adapter;

    private ArrayList<PointInfo> points = new ArrayList<>();

    // Receives list requests from server
    private JsonObject jsonResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = (ListView) findViewById(R.id.list_home);

        adapter = new ListAdapter(points, getApplicationContext());
        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // ListView Clicked item index
                int itemPosition = position;

                // ListView Clicked item value
                PointInfo data = (PointInfo) listView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :" + itemPosition + "  Name : " + data.getName(), Toast.LENGTH_LONG)
                        .show();
            }

        });

        AppController.setCtx(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Test", "onResume");
        // get books from server
        askPoints();
    }

    public void askPoints()
    {
        StringRequest jsonObjReq = new StringRequest(Request.Method.POST,
                AppController.urlServer,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d("Test", response);

                        Gson gson = new Gson();
                        jsonResponse = gson.fromJson(response, JsonObject.class);
                        JsonArray jsonArray = jsonResponse.get("list").getAsJsonArray();

                        ArrayList<PointInfo> list = new ArrayList<>();

                        adapter.updateList(list);
                        Log.d("Test", "response ended");
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Test", error.toString());
                VolleyLog.d("Test", "Error: " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("option", "bookList");

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance(this).addToRequestQueue(jsonObjReq,
                "Volley");
    }
}
