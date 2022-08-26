package com.poc.dropme.Passenger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.poc.dropme.DataBase.DbHelper;
import com.poc.dropme.Gadgets.Models.Sample;
import com.poc.dropme.Gadgets.PrefManager;
import com.poc.dropme.Gadgets.SmsBroadcast.SMSreceiverPassenger;
import com.poc.dropme.PassengerHome;
import com.poc.dropme.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class PassengerActivity extends AppCompatActivity {
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private Geocoder geocoder;
    private int ACCESS_LOCATION_REQUEST_CODE = 10001;
    FusedLocationProviderClient fusedLocationProviderClient;
    com.google.android.gms.location.LocationRequest locationRequest, selectionRequest;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 101;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10;
    private int MY_PERMISSIONS_REQUEST_SMS_RECEIVE = 105;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1001;
    SMSreceiverPassenger smsReceiver;
    private Polyline mPolyline;

    DbHelper dbHelper;
    Marker userLocationMarker, userMarker, mCurrLocationMarker, dummy;
    LatLng current, toLocation;
    TextView date, time, distTV, timeTV, distDTV, timeDTV;

    AutoCompleteTextView myLocationEdt, toLocationEdt;
    private long pressedTime;
    Button find, call, track;
    LinearLayout det;
    ProgressBar progressBar;
    BottomSheetBehavior bottomSheetBehavior;
    boolean first = true;
    PrefManager mpref;

    String[] plac = {"Radiant Info Systems,# 46, 7th B Main Rd, 4th Block, Jayanagar, Bengaluru, Karnataka 560011", "Ganesha Temple ,WHHJ+HV2, New Diagonal Rd, 7th Main Rd, 4th Block, Jayanagar, Bengaluru, Karnataka 560011", "Union Bank of India,No. 429/31, 30th Cross Road, 4th Block, Jayanagar, Bengaluru, Karnataka 560011"
            , "1005, 17th Main Rd, Tavarekere, Aicobo Nagar, 1st Stage, BTM 1st Stage, Bengaluru, Karnataka 560029"};
    String[] latlan = {"12.9304043,77.5813366.", "12.9294475,77.5812293", "12.9286789,77.5785509 "};

    ArrayList<Sample> bookings = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);


        myLocationEdt = (AutoCompleteTextView) findViewById(R.id.my_locationET);
        toLocationEdt = (AutoCompleteTextView) findViewById(R.id.to_locationET);
        find = (Button) findViewById(R.id.findBtn);
        det = (LinearLayout) findViewById(R.id.disDet);
        distTV = (TextView) findViewById(R.id.distance);
        timeTV = (TextView) findViewById(R.id.estTime);
        distDTV = (TextView) findViewById(R.id.disDTV);
        timeDTV = (TextView) findViewById(R.id.estTimeDTV);
        date = (TextView) findViewById(R.id.date);
        time = (TextView) findViewById(R.id.time);
        call = (Button) findViewById(R.id.callPTV);
        track = (Button) findViewById(R.id.trackPTV);
        progressBar = (ProgressBar) findViewById(R.id.progress_horizontal);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);



        ArrayAdapter toadapter = new ArrayAdapter(PassengerActivity.this, android.R.layout.simple_list_item_1, plac);
        toLocationEdt.setAdapter(toadapter);
        toLocationEdt.setThreshold(1);


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpledateformat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat simpletime = new SimpleDateFormat("HH:mm:ss");
        date.setText(simpledateformat.format(calendar.getTime()).toString());
        time.setText(simpletime.format(calendar.getTime()).toString());
        // sentSms("1223");

        toLocationEdt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                String place = parent.getItemAtPosition(i).toString();
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(parent.getApplicationWindowToken(), 0);
               // toLocationMarker(place);
            }
        });


        find.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {

                if (find.getText().toString().equalsIgnoreCase("book cabs")) {
                    if (false) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Select Both Locations!", Toast.LENGTH_SHORT).show();
                    } else {
                        find.setText("Please Wait");
                        progressBar.setVisibility(View.VISIBLE);
                        askpermission();
                    }
                } else if (find.getText().toString().equalsIgnoreCase("Please Wait")) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    Toast.makeText(getApplicationContext(), "Please Wait Until Cab booking", Toast.LENGTH_SHORT).show();

                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }


            }
        });

    }

    private void askpermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 0);
            }
        }else{
            sentSms("1223");
        }
    }

    private boolean sentSms(String mobileNumber) {
        boolean done = false;
        String mess ="POC," ;
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(mobileNumber, null,mess, null, null);
            done = true;
            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            done = false;
            Toast.makeText(getApplicationContext(), "Some fields are Empty", Toast.LENGTH_LONG).show();
        }
        return done;
    }


}