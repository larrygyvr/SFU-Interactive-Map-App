package com.example.sfu_interactive_map;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.List;

//Singleton class for sending http requests
//better implementation practice than using a global variable for a request queue
public class HttpRequest {

    private static Context mCtx;
    private static HttpRequest mInstance;
    private RequestQueue mRequestQueue;


    private HttpRequest(Context context)
    {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized HttpRequest getInstance(Context context){
        if(mInstance == null){
            mInstance = new HttpRequest(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue(){
        if(mRequestQueue == null){
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public void addToRequestQueue(Request req){
            getRequestQueue().add(req);
    }

}
