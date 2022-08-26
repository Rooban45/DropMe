package com.poc.dropme.DataBase;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.poc.dropme.Gadgets.Models.Sample;

import java.util.ArrayList;


public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "DROPME_POC.db";
    private static final int DB_VERSION = 1;

    String TableName1 = "Bookings";
    public static final String COL_1 = "name";
    public static final String COL_2 = "latlang";
    public static final String COL_3 = "mobile";
    public static final String COL_4 = "picup";
    public static final String COL_5 = "Dest";
    public static final String COL_6 = "status";
    public static final String COL_7 = "TripId";
    public static final String COL_8 = "Tlatlang";

    String TableName2 = "PBookings";
    public static final String PCOL_1 = "name";
    public static final String PCOL_2 = "latlang";
    public static final String PCOL_3 = "mobile";
    public static final String PCOL_4 = "picup";
    public static final String PCOL_5 = "Dest";
    public static final String PCOL_6 = "status";
    public static final String PCOL_7 = "TripId";

    @Override
    public void onCreate(SQLiteDatabase db) {
        /**INSERT TICKET DETAILS INFO  **/
        String CREATE_BOOKINGS_TABLE = "CREATE TABLE IF NOT EXISTS " +TableName1
                + "( "+"SLNO"+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_1 + " VARCHAR, "
                + COL_2 + " VARCHAR, "
                + COL_3 + " VARCHAR, "
                + COL_4 + " VARCHAR, "
                + COL_5 + " VARCHAR, "
                + COL_6 + " VARCHAR,"
                + COL_7 + " VARCHAR ,"
                + COL_8 + " VARCHAR ); ";

        String CREATE_PBOOKINGS_TABLE = "CREATE TABLE IF NOT EXISTS " +TableName2
                + "( "+"SLNO"+ " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PCOL_1 + " VARCHAR, "
                + PCOL_2 + " VARCHAR, "
                + PCOL_3 + " VARCHAR, "
                + PCOL_4 + " VARCHAR, "
                + PCOL_5 + " VARCHAR, "
                + PCOL_6 + " VARCHAR,"
                + PCOL_7 + " VARCHAR ); ";

        db.execSQL(CREATE_BOOKINGS_TABLE);
        db.execSQL(CREATE_PBOOKINGS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TableName1);
        onCreate(db);
    }

    public DbHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @SuppressLint("Range")
    public ArrayList<Sample> readBookings() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * from Bookings ", null);
        ArrayList<Sample> Bookings = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                Sample datas = new Sample();

                datas.setName(c.getString(c.getColumnIndex("name")));
                datas.setLatLng(c.getString(c.getColumnIndex("latlang")));
                datas.setNumber(c.getString(c.getColumnIndex("mobile")));
                datas.setPicup(c.getString(c.getColumnIndex("picup")));
                datas.setDrop(c.getString(c.getColumnIndex("Dest")));
                datas.setStatus(c.getString(c.getColumnIndex("status")));
                datas.setTripID(c.getString(c.getColumnIndex("TripId")));
                datas.setTlatLng(c.getString(c.getColumnIndex("Tlatlang")));
                Bookings.add(datas);

            } while (c.moveToNext());
        }


        return Bookings;
    }


    @SuppressLint("Range")
    public ArrayList<Sample> readDBookings() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * from Bookings order by SLNO desc", null);
        ArrayList<Sample> Bookings = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                Sample datas = new Sample();

                datas.setName(c.getString(c.getColumnIndex("name")));
                datas.setLatLng(c.getString(c.getColumnIndex("latlang")));
                datas.setNumber(c.getString(c.getColumnIndex("mobile")));
                datas.setPicup(c.getString(c.getColumnIndex("picup")));
                datas.setDrop(c.getString(c.getColumnIndex("Dest")));
                datas.setStatus(c.getString(c.getColumnIndex("status")));
                datas.setTripID(c.getString(c.getColumnIndex("TripId")));
                datas.setTlatLng(c.getString(c.getColumnIndex("Tlatlang")));
                Bookings.add(datas);

            } while (c.moveToNext());
        }


        return Bookings;
    }
    public boolean insertTrip(String number, String name, String s, String pickup, String drop, String status,String tripId,String Tlatlang){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues mCv = new ContentValues();

        mCv.put("name", name);
        mCv.put("latlang", s);
        mCv.put("mobile", number);
        mCv.put("picup", pickup);
        mCv.put("Dest", drop);
        mCv.put("status", status);
        mCv.put("tripId", tripId);
        mCv.put("Tlatlang", Tlatlang);

        long result = db.insert(TableName1, null, mCv);

        if (result == -1)
            return false;
        else
            return true;
    }

    public boolean PinsertTrip(String number, String name, String s, String pickup, String drop, String status,String tripId){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues mCv = new ContentValues();

        mCv.put("name", name);
        mCv.put("latlang", s);
        mCv.put("mobile", number);
        mCv.put("picup", pickup);
        mCv.put("Dest", drop);
        mCv.put("status", status);
        mCv.put("tripId", tripId);

        
        long result = db.insert(TableName2, null, mCv);

        if (result == -1)
            return false;
        else
            return true;
    }

    @SuppressLint("Range")
    public ArrayList<Sample> PreadBookings() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * from PBookings  order by SLNO desc ", null);
        ArrayList<Sample> Bookings = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                Sample datas = new Sample();

                datas.setName(c.getString(c.getColumnIndex("name")));
                datas.setLatLng(c.getString(c.getColumnIndex("latlang")));
                datas.setNumber(c.getString(c.getColumnIndex("mobile")));
                datas.setPicup(c.getString(c.getColumnIndex("picup")));
                datas.setDrop(c.getString(c.getColumnIndex("Dest")));
                datas.setStatus(c.getString(c.getColumnIndex("status")));
                datas.setTripID(c.getString(c.getColumnIndex("TripId")));
                Bookings.add(datas);

            } while (c.moveToNext());
        }


        return Bookings;
    }


    public void updateStatus(String id ) {

        String q ="UPDATE PBookings SET status = 'ENDED' WHERE TripId="+"'"+id+"'";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(q);
        db.close();

    }

    public void updateStatusD(String id ) {

        String q ="UPDATE Bookings SET status = 'ENDED' WHERE TripId="+"'"+id+"'";
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(q);
        db.close();

    }
}
