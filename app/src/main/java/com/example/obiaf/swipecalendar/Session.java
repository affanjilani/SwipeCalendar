package com.example.obiaf.swipecalendar;

//import com.google.android.gms.maps.model.LatLng;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ServerValue;

import java.util.List;

/**
 * Created by Zachary Bys on 2017-11-19.
 */

public class Session {
    private Long sessionKey;
    private String sessionName;
    private double sessionLat;
    private double sessionLong;
    private String time;
    private String sport;
    private String date;
    //private FirebaseAuth auth;
    //private LatLng latlong;
    private String user;
    private int numusers;
    private int maxusers;
    private boolean isPrivate;
    private List<String> users;

    public Session(Long key, double longitude, double latitude, String name, String date, String time, String sport, int numusers, int maxusers, boolean isPrivate){
        sessionKey = key;
        sessionName = name;
        sessionLat = latitude;
        sessionLong = longitude;
        //this.latlong = new LatLng(latitude, longitude);
        this.time = time;
        this.sport = sport;
        this.date = date;
        this.numusers = numusers;
        this.maxusers = maxusers;
        this.isPrivate = isPrivate;
        //auth = FirebaseAuth.getInstance();
    }

    public Session(Long key, double longitude, double latitude, String name, String date, String time, String sport, String user, int numusers, int maxusers, boolean isPrivate){
        sessionKey = key;
        sessionName = name;
        sessionLat = latitude;
        sessionLong = longitude;
        //this.latlong = new LatLng(latitude, longitude);
        this.user = user;
        this.time = time;
        this.sport = sport;
        this.date = date;
        this.numusers = numusers;
        this.maxusers = maxusers;
        this.isPrivate = isPrivate;
        //auth = FirebaseAuth.getInstance();
    }

    public Session(){
        sessionKey = null;
        sessionName = null;
        sessionLat = 0;
        sessionLong = 0;
        //this.latlong = new LatLng(0, 0);
        this.time = null;
        this.date = null;
        this.sport = null;
        this.numusers = 0;
        this.maxusers = 10;
        this.isPrivate = false;
        //auth = FirebaseAuth.getInstance();
    }

    public Long getSessionKey(){return sessionKey;}
    //public double getSessionLat(){return latlong.latitude;}
    //public double getSessionLong(){return latlong.longitude;}
    public String getSessionName(){return sessionName;}
    public String getSessionSport(){return sport;}
    public String getSessionTime() {return time;}
    //public LatLng getLatLng() {return latlong;}
    public String getUser() {return user;}
    public List<String> getUsers() {return users;}
    public String getSessionDate(){return date;}
    public int getSessionNumUsers(){return numusers;}
    public int getSessionMaxUsers(){return maxusers;}
    public boolean getSessionPrivate(){return isPrivate;}

    public void setSessionKey(Long key){sessionKey = key;}
    public void setSessionName(String name){sessionName = name;}
    public void setSessionSport(String sport){this.sport = sport;}
    public void setSessionDate(String date){this.date = date;}
    public void setSessionTime(String time){this.time = time;}
    public void setUser(String user){this.user = user;}
    public void setUsers(List<String> users) {this.users = users;}
    public void setSessionNumUsers(int numusers){this.numusers = numusers;}
    public void setSessionMaxUsers(int maxusers){this.maxusers = maxusers;}
    public void setSessionPrivacy(boolean isPrivate){this.isPrivate = isPrivate;}
    /*public void setLatLng(LatLng latlong) {
        this.latlong = latlong;
        sessionLat = latlong.latitude;
        sessionLong = latlong.longitude;
    }*/



}
