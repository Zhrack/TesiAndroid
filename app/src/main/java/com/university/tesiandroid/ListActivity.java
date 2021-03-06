package com.university.tesiandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    // LogCat tag
    private static final String TAG = ListActivity.class.getSimpleName();

    // GPS STUFF
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;


    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;

    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FASTEST_INTERVAL = 2000; // 1 sec
    private static int DISPLACEMENT = 0; // 1 meters

    // UI elements

    private ListView listView;
    private ListAdapter adapter;

    private Handler handler;

    private Button mapBtn;
    private TextView txtDevicePosition;
    private EditText inputRadius;

    // Receives list requests from server
    private JsonObject jsonResponse;

    private Intent mapIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        listView = (ListView) findViewById(R.id.list_home);
        mapBtn = (Button) findViewById(R.id.maps_btn);
        txtDevicePosition = (TextView) findViewById(R.id.txt_device_position);
        inputRadius = (EditText) findViewById(R.id.radius_input);

        handler = new Handler();

        adapter = new ListAdapter(LocationData.Instance().getPoints(), getApplicationContext(), handler);
        // Assign adapter to ListView
        listView.setAdapter(adapter);

        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRequestingLocationUpdates) {
                    stopLocationUpdates();
                }

                startActivity(mapIntent);
            }
        });

        inputRadius.setText("80000");

        AppController.setCtx(this);

        mapIntent = new Intent(this, MapsActivity.class);

        // First we need to check availability of play services
        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }

        mRequestingLocationUpdates = true;
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
    }

    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first

        if(mRequestingLocationUpdates)
        {
            stopLocationUpdates();
            mRequestingLocationUpdates = true;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Once connected with google api, get the location
        getLocation();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        LocationData locationData = LocationData.Instance();
        locationData.setmLastLocation(location);

        Toast.makeText(getApplicationContext(), "Location changed!",
                Toast.LENGTH_SHORT).show();

        getLocation();

        txtDevicePosition.setText("Position: " + String.valueOf(locationData.getLatitude()) + ", " +
                String.valueOf(locationData.getLongitude()));

        askPoints();
        Log.d(TAG, "location changed");
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

                        JsonObject jsonObject;
                        ArrayList<PointInfo> list = new ArrayList<>();
                        for (int i = 0; i < jsonArray.size(); ++i)
                        {
                            jsonObject = jsonArray.get(i).getAsJsonObject();

                            PointInfo data = new PointInfo();
                            data.setName(jsonObject.get("name").getAsString());
                            data.setLatitude(jsonObject.get("latitude").getAsDouble());
                            data.setLongitude(jsonObject.get("longitude").getAsDouble());
                            data.setDistance(jsonObject.get("distance").getAsInt());

                            JsonElement wikiElement = jsonObject.get("wikiText");
                            if(wikiElement != null)
                            {
                                data.setWikiText(wikiElement.getAsString());
                                data.setLanguage(jsonObject.get("language").getAsString());
                            }
                            else // item doesn't have a wiki, set empty string
                            {
                                data.setWikiText("");
                                data.setLanguage("");
                            }


                            list.add(data);
                        }
                        adapter.updateList(list);
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
                LocationData locationData = LocationData.Instance();
                params.put("option", "getPointList");
                Log.d(TAG, "sending latitude " + String.valueOf(locationData.getLatitude()));
                Log.d(TAG, "sending longitude " + String.valueOf(locationData.getLongitude()));
                params.put("latitude", String.valueOf(locationData.getLatitude()));
                params.put("longitude", String.valueOf(locationData.getLongitude()));
                params.put("radius", String.valueOf(inputRadius.getText()));

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
    public void getLocation() {

        LocationData locationData = LocationData.Instance();
        Location location = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if(location != null)
        {
            locationData.setmLastLocation(location);
        }
        else
        {
            Log.d(TAG, "(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest).setAlwaysShow(true);

        final Activity activity = this;
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    activity, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1000)
        {
            getLocation();

            askPoints();
        }
    }

    /**
     * Starting the location updates
     * */
    protected void startLocationUpdates() {

        LocationData locationData = LocationData.Instance();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

        Log.d(TAG, "latitude " + String.valueOf(locationData.getLatitude()));
        Log.d(TAG, "longitude " + String.valueOf(locationData.getLongitude()));

        mRequestingLocationUpdates = true;
        Log.d(TAG, "Periodic location updates started!");
    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);

        mRequestingLocationUpdates = false;
        Log.d(TAG, "Periodic location updates stopped!");
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
