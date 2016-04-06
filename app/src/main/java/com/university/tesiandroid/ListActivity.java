package com.university.tesiandroid;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    // LogCat tag
    private static final String TAG = ListActivity.class.getSimpleName();

    // GPS STUFF
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;
    private double latitude;
    private double longitude;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 5; // 1 meters

    // UI elements

    private ListView listView;
    private ListAdapter adapter;

    private Button toggleGPSbtn;
    private Button mapBtn;
    private TextView txtLatitude;
    private TextView txtLongitude;

    // Receives list requests from server
    private JsonObject jsonResponse;

    // Search radius to be sent to server
    private float searchRadius = 100000; // in meters

    private Intent mapIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = (ListView) findViewById(R.id.list_home);
        toggleGPSbtn = (Button) findViewById(R.id.toggleGPS_btn);
        mapBtn = (Button) findViewById(R.id.maps_btn);
        txtLatitude = (TextView) findViewById(R.id.txt_latitude);
        txtLongitude = (TextView) findViewById(R.id.txt_longitude);

        adapter = new ListAdapter(AppController.getInstance(this).getPoints(), getApplicationContext());
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

        toggleGPSbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePeriodicLocationUpdates();
            }
        });

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRequestingLocationUpdates)
                {
                    togglePeriodicLocationUpdates();
                }

                startActivity(mapIntent);
            }
        });

        AppController.setCtx(this);

        mapIntent = new Intent(this, MapsActivity.class);

        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        //askPoints();
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        stopLocationUpdates();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Once connected with google api, get the location
        getLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        // get points from server
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

        getLocation();

        txtLatitude.setText("Lat: " + String.valueOf(latitude));
        txtLongitude.setText("Long: " + String.valueOf(longitude));

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

                        JsonObject jsonObject = new JsonObject();
                        for (int i = 0; i < jsonArray.size(); ++i)
                        {
                            jsonObject = jsonArray.get(i).getAsJsonObject();

                            PointInfo data = new PointInfo();
                            data.setName(jsonObject.get("name").getAsString());
                            data.setLatitude(jsonObject.get("latitude").getAsDouble());
                            data.setLongitude(jsonObject.get("longitude").getAsDouble());
                            data.setDistance(jsonObject.get("distance").getAsFloat());
                            JsonElement wikiText = jsonObject.get("wikiText");
                            if(wikiText != null)
                            {
                                data.setWikiText(wikiText.getAsString());
                            }
                            else
                            {
                                data.setWikiText("NO");
                            }

                            list.add(data);
                        }

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
                params.put("option", "getPointList");
                Log.d(TAG, "sending latitude " + String.valueOf(latitude));
                Log.d(TAG, "sending longitude " + String.valueOf(longitude));
                params.put("latitude", String.valueOf(latitude));
                params.put("longitude", String.valueOf(longitude));
                params.put("radius", String.valueOf(searchRadius));

                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance(this).addToRequestQueue(jsonObjReq,
                "Volley");
    }

    /**
     * Method to display the location on UI
     * */
    private void getLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        }
        else{
            Log.d(TAG, "(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }

    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            toggleGPSbtn.setText(R.string.txt_toggleStop);

            // Starting the location updates
            startLocationUpdates();

            Log.d(TAG, "Periodic location updates started!");

        } else {
            mRequestingLocationUpdates = false;
            toggleGPSbtn.setText(R.string.txt_toggleStart);

            // Stopping the location updates
            stopLocationUpdates();

            Log.d(TAG, "Periodic location updates stopped!");
        }
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        Log.d(TAG, "latitude " + String.valueOf(latitude));
        Log.d(TAG, "longitude " + String.valueOf(longitude));

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }
}
