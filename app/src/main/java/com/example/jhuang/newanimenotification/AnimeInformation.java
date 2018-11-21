package com.example.jhuang.newanimenotification;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class AnimeInformation {

    public String animeName;
    public boolean airing;

    public AnimeInformation() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public AnimeInformation(String animeName, boolean airing) {
        this.animeName = animeName;
        this.airing = airing;
    }

}