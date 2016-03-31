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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListActivity extends Activity {

    private ListView listView;

    private ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = (ListView) findViewById(R.id.list_home);

        adapter = new ListAdapter(books, getApplicationContext());
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
                BookInfo data = (BookInfo) listView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :" + itemPosition + "  NomeLibro : " + data.getTitolo(), Toast.LENGTH_LONG)
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

                        ArrayList<BookInfo> list = new ArrayList<>();

                        adapter.updateList(list);
                        Log.d("Test", "response terminata");
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
                params.put("opzione", "bookList");

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance(this).addToRequestQueue(jsonObjReq,
                "Volley");
    }
}
