package com.poc.dropme.Gadgets.Models;

import com.google.android.gms.maps.model.LatLng;

public class Sample {


    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    String latLng;
    String name;
    String number;

    public String getPicup() {
        return picup;
    }

    public void setPicup(String picup) {
        this.picup = picup;
    }

    public String getDrop() {
        return Drop;
    }

    public void setDrop(String drop) {
        Drop = drop;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    String picup;
    String Drop;
    String Status;

    public String getTlatLng() {
        return TlatLng;
    }

    public void setTlatLng(String tlatLng) {
        TlatLng = tlatLng;
    }

    String TlatLng;

    public String getTripID() {
        return TripID;
    }

    public void setTripID(String tripID) {
        TripID = tripID;
    }

    String TripID;

}
