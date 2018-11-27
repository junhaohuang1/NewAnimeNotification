package com.example.jhuang.newanimenotification;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


import com.example.jhuang.newanimenotification.AnimeInformation;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;


public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private static final String TAG = "MainActivity";



    public void getSubscribedTopics(View view){

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "https://iid.googleapis.com/iid/info/" + newToken + "?details=true";

                // Request a string response from the provided URL.
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {


                                Log.d(TAG,"Response: " + response.toString());
                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                NetworkResponse response = error.networkResponse;
                                if (error instanceof ServerError && response != null) {
                                    try {
                                        String res = new String(response.data,
                                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                        // Now you can use any deserializer to make sense of data
                                        JSONObject obj = new JSONObject(res);
                                    } catch (UnsupportedEncodingException e1) {
                                        // Couldn't properly decode data to string
                                        e1.printStackTrace();
                                    } catch (JSONException e2) {
                                        // returned data is not JSONObject?
                                        e2.printStackTrace();
                                    }
                                }
                            }
                        }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Log.d("Response", "putting in header");
                        Map<String, String>  params = new HashMap<String, String>();
                        params.put("Content-Type", "application/json; charset=UTF-8");
                        params.put("Authorization", "key=" + "apiKey");
                        return params;
                    }
                };
                queue.add(jsonObjectRequest);
            }
        });


    }

    public void subscribeToTopics(final String topic){
        FirebaseMessaging.getInstance().subscribeToTopic(topic.replaceAll("\\s+","")).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(),"Success! Subscribed to " + topic,Toast.LENGTH_LONG).show();
            }
        });


    }

    public void unsubscribeToTopics(final String topic){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic.replaceAll("\\s+","")).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(),"Success! Unubscribed to " + topic,Toast.LENGTH_LONG).show();
            }
        });
    }



    // Add the request to the RequestQueue.


    public void createAnimeList(){
        final ViewGroup layout = (ViewGroup) findViewById(R.id.layout);
        final ArrayList<String> animeNames = new ArrayList<String>();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        Query query = mDatabase.child("anime");
        query.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        HashMap<String, Object> animeList = (HashMap<String,Object>) dataSnapshot.getValue();
                        Iterator it = animeList.entrySet().iterator();
                        while(it.hasNext()){
                            HashMap.Entry pair = (HashMap.Entry)it.next();
//                            Log.i(TAG,("key pair is " + pair.getKey() + " = " + pair.getValue()));
//                            Log.i(TAG,(pair.getValue().getClass().toString()));
                            HashMap<String, String> anime = (HashMap<String,String>)pair.getValue();
//                            Log.i(TAG,("name is " + anime.get("name")));
                            animeNames.add(anime.get("name"));
                            it.remove();
                        }
                        Collections.sort(animeNames);
                        for (final String temp : animeNames) {
                            int chkId = 1001;
                            CheckBox cb = new CheckBox(getApplicationContext());
                            cb.setText(temp);
                            cb.setId(++chkId);
                            cb.setTextColor(Color.BLACK);
                            cb.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CheckBox currentCB = (CheckBox) v;
                                    if(currentCB.isChecked()){
                                        unsubscribeToTopics(temp);
                                    } else {
                                        subscribeToTopics(temp);
                                    }
                                }
                            });
                            layout.addView(cb);
                        }
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                        Log.w(TAG, "get list failed", databaseError.toException());
                    }
                });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createAnimeList();
    }
}

