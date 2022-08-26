package com.poc.dropme.Bookings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.poc.dropme.DataBase.DbHelper;
import com.poc.dropme.Driver.DriverHome;

import com.poc.dropme.Gadgets.Adapters.BookingAdapter;
import com.poc.dropme.Gadgets.Adapters.PBookingAdapter;
import com.poc.dropme.Gadgets.BaseActivity;
import com.poc.dropme.Gadgets.Models.Sample;
import com.poc.dropme.Gadgets.PrefManager;
import com.poc.dropme.PassengerHome;
import com.poc.dropme.R;

import java.util.ArrayList;

public class PassengerBookings extends BaseActivity {

    DbHelper dbHelper;
    ArrayList<Sample> bookings = new ArrayList<>();
    PBookingAdapter Padapter;
    BookingAdapter adapter;
    RecyclerView passengersB;
    TextView noData;
    PrefManager mpref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = getLayoutInflater().inflate(R.layout.activity_passenger_bookings, frameLayout);

        dbHelper = new DbHelper(PassengerBookings.this);
        mpref = new PrefManager(this);

        passengersB = (RecyclerView) findViewById(R.id.PbookinsRV);
        noData = (TextView) findViewById(R.id.NodataTV);


        if (mpref.getUsername().equalsIgnoreCase("Employee")) {
            bookings = dbHelper.PreadBookings();
            if (bookings.size() > 0) {
                noData.setVisibility(View.GONE);
                passengersB.setVisibility(View.VISIBLE);

                Padapter = new PBookingAdapter(this, bookings);
                LinearLayoutManager manager = new LinearLayoutManager(PassengerBookings.this, RecyclerView.VERTICAL, false);
                passengersB.setLayoutManager(manager);
                passengersB.setAdapter(Padapter);

            } else {
                noData.setVisibility(View.VISIBLE);
                passengersB.setVisibility(View.GONE);
            }
        }else{
            bookings = dbHelper.readBookings();
            if (bookings.size() > 0) {
                noData.setVisibility(View.GONE);
                passengersB.setVisibility(View.VISIBLE);

                adapter = new BookingAdapter(this, bookings);
                LinearLayoutManager manager = new LinearLayoutManager(PassengerBookings.this, RecyclerView.VERTICAL, false);
                passengersB.setLayoutManager(manager);
                passengersB.setAdapter(adapter);

            } else {
                noData.setVisibility(View.VISIBLE);
                passengersB.setVisibility(View.GONE);
            }

        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mpref.getUsername().equalsIgnoreCase("Employee")) {
            startActivity(new Intent(PassengerBookings.this, PassengerHome.class));
        }else{
            startActivity(new Intent(PassengerBookings.this, DriverHome.class));
        }
    }
}