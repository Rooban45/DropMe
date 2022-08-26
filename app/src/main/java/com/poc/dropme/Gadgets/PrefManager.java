package com.poc.dropme.Gadgets;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Class for Shared Preference
 */
public class PrefManager {

    private final String appPreferences = "com.poc.dropme";

    private final SharedPreferences appShared;
    private SharedPreferences.Editor appEditor;

    public PrefManager(Context context) {
        this.appShared = context.getSharedPreferences(appPreferences, Activity.MODE_PRIVATE);
        this.appEditor = appShared.edit();

    }

    public void saveName(String val) {
        appEditor.putString("Name", val);
        appEditor.commit();
    }

    public String getName() {
        String Name = appShared.getString("Name", "");
        return Name;
    }

    public void saveTripStatus(String val) {
        appEditor.putString("Trip", val);
        appEditor.commit();
    }

    public String getTripStatus() {
        String Name = appShared.getString("Trip", "");
        return Name;
    }


    public void saveEmail(String val) {
        appEditor.putString("Email", val);
        appEditor.commit();
    }

    public String getEmail() {
        String Email = appShared.getString("Email", "");
        return Email;
    }


    public void savePhoneNo(String val) {
        appEditor.putString("PhoneNo", val);
        appEditor.commit();
    }

    public String getPhoneNo() {
        String PhoneNo = appShared.getString("PhoneNo", "");
        return PhoneNo;
    }


    public void saveUsername(String val) {
        appEditor.putString("username", val);
        appEditor.commit();
    }

    public String getUsername() {
        String City = appShared.getString("username", "");
        return City;
    }


    public void savetripId(String val) {
        appEditor.putString("tripId", val);
        appEditor.commit();
    }

    public String gettripId() {
        String tripId = appShared.getString("tripId", "");
        return tripId;
    }

    public void saveDriverNO(String val) {
        appEditor.putString("driverNO", val);
        appEditor.commit();
    }

    public String getDriverNO() {
        String driverNO = appShared.getString("driverNO", "");
        return driverNO;
    }

    public void savePlocation(String val) {
        appEditor.putString("Plocation", val);
        appEditor.commit();
    }

    public String getPlocation() {
        String Plocation = appShared.getString("Plocation", "");
        return Plocation;
    }


    public void saveTlocation(String val) {
        appEditor.putString("tlocation", val);
        appEditor.commit();
    }

    public String getTlocation() {
        String tlocation = appShared.getString("tlocation", "");
        return tlocation;
    }

    public void saveDlocation(String val) {
        appEditor.putString("Dlocation", val);
        appEditor.commit();
    }

    public String getDlocation() {
        String Dlocation = appShared.getString("Dlocation", "");
        return Dlocation;
    }

}
