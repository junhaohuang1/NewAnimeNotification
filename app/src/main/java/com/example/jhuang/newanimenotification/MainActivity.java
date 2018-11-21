package com.example.jhuang.newanimenotification;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;


import com.example.jhuang.newanimenotification.AnimeInformation;


public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private static final String TAG = "MainActivity";

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
                        for (String temp : animeNames) {
                            CheckBox cb = new CheckBox(getApplicationContext());
                            cb.setText(temp);
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
