package com.example.jhuang.newanimenotification;

import android.app.Application;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.messaging.FirebaseMessaging;


public class FirebaseActions extends Application {


    private static FirebaseActions mContext;
    private static final String TAG = "FirebaseActions";

    @Override
    public void onCreate(){
        System.out.println("starting firebase app");
        super.onCreate();
        mContext = this;
        FirebaseMessaging.getInstance().subscribeToTopic("alldevices").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "subscribed to all devices");
            }
        });
    }

    public static FirebaseActions getContext(){
        return mContext;
    }



    public void subscribeToTopics(final String topic){
        FirebaseMessaging.getInstance().subscribeToTopic(topic.replaceAll("\\s+","")).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(),"Success! Subscribed to " + topic,Toast.LENGTH_LONG).show();
                Log.i(TAG, "subscribed to topic");

            }
        });
    }

    public void unsubscribeToTopics(final String topic){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic.replaceAll("\\s+","")).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(),"Success! Unsubscribed to " + topic,Toast.LENGTH_LONG).show();
                Log.i(TAG, "unsubscribed to topic");
            }
        });
    }


}
