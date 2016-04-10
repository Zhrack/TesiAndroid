package com.university.tesiandroid;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

/**
 * Created by Davide on 31/03/2016.
 */
public class AppController extends Application {

    private static final int REQUEST_TIMEOUT_MS = 4884;

    public final static String MESSAGE_CHOSEN_BOOK = "MESSAGE_CHOSEN_BOOK";

//    private static final String basicURL = "http://192.168.1.56:8084/tesi/";
    private static final String basicURL = "http://tesi-env.us-west-2.elasticbeanstalk.com/";

    public static final String urlServer = basicURL + "Controller";

    public static final String TAG = AppController.class
            .getSimpleName();

    private RequestQueue mRequestQueue;

    private static AppController mInstance;
    private static Context ctx;

    public AppController(Context ctx)
    {
        this.ctx = ctx;
    }

    @Override
    public void onCreate() {
        super.onCreate();


        mInstance = this;
    }

    public static synchronized AppController getInstance(Context ctx) {
        if(mInstance == null)
            mInstance = new AppController(ctx);

        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(ctx);
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        // Imposta la Retry Policy
        req.setRetryPolicy(new DefaultRetryPolicy(
                REQUEST_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    public static void setCtx(Context ctx)
    {
        AppController.ctx = ctx;
    }
}
