package com.example.jhuang.newanimenotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.jhuang.newanimenotification.FirebaseActions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {
    public RecyclerView recyclerView;
    private Adapter adapter;
    private LinearLayoutManager layoutManager;


    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private CredentialsConfig credentials = new CredentialsConfig();
    private String apiKey = credentials.getApiKey();
    private String userName = credentials.getUserName();
    private String password = credentials.getPassword();


    private static final String TAG = "MainActivity";

    // dummy list of items to be populated manually
    List<Model> items = new ArrayList<>();

    private void fillItems() {



        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this,new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = "https://iid.googleapis.com/iid/info/" + newToken + "?details=true";

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {
                                try{
                                    JSONObject body = new JSONObject(response);
                                    final Set<String> subscribedTopicsList = new HashSet<String>(Lists.newArrayList(body.getJSONObject("rel").getJSONObject("topics").keys()));
//                                    Log.d(TAG,"Response: " + response.toString());

                                    mDatabase = FirebaseDatabase.getInstance().getReference();
                                    mAuth = FirebaseAuth.getInstance();
                                    mAuth.signInWithEmailAndPassword(userName, password);
                                    Query query = mDatabase.child("anime");
                                    query.addListenerForSingleValueEvent(

                                            new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
                                                    final Adapter adapter = new Adapter(subscribedTopicsList);
                                                    layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
                                                    recyclerView.setLayoutManager(layoutManager);
                                                    recyclerView.setAdapter(adapter);
                                                    ArrayList<String> airingAnimeList = new ArrayList<String>();
                                                    HashMap<String, Object> animeList = (HashMap<String, Object>) dataSnapshot.getValue();
                                                    Iterator it = animeList.entrySet().iterator();
                                                    while(it.hasNext()){
                                                        HashMap.Entry pair = (HashMap.Entry)it.next();
                                                        HashMap<String, String> anime = (HashMap<String,String>)pair.getValue();
                                                        airingAnimeList.add(anime.get("name"));
                                                    }
                                                    Collections.sort(airingAnimeList);
                                                    System.out.println("sorted the list");
                                                    int position = 0;
                                                    for(String anime : airingAnimeList){
                                                        Model model = new Model();
                                                        model.setPosition(position+1);
                                                        model.setAnimeName(anime);
                                                        items.add(model);
                                                        position++;
                                                    }
                                                    adapter.loadItems(items);
                                                }
                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                    //handle databaseError
                                                    Log.w(TAG, "get list failed", databaseError.toException());
                                                }
                                            });

                                } catch(JSONException e){
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
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
                        params.put("Authorization", "key=" + apiKey);
                        return params;
                    }
                };
                queue.add(stringRequest);
            }
        });
    }



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            Log.i(TAG, "creating channel");
            String channelId  = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }


        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }



        fillItems();

    }


}

