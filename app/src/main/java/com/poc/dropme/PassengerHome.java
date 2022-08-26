package com.poc.dropme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.maps.android.SphericalUtil;
import com.poc.dropme.DataBase.DbHelper;

import com.poc.dropme.Gadgets.BaseActivity;
import com.poc.dropme.Gadgets.DirectionParser.DirectionsJSONParser;
import com.poc.dropme.Gadgets.Models.Sample;
import com.poc.dropme.Gadgets.PrefManager;
import com.poc.dropme.Gadgets.SmsBroadcast.MessageListenerPassenger;
import com.poc.dropme.Gadgets.SmsBroadcast.SMSreceiverPassenger;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PassengerHome extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerDragListener, MessageListenerPassenger {
    private  final String TAG = "PassengerHome";
    private GoogleMap mMap;
    private Geocoder geocoder;
    final private int ACCESS_LOCATION_REQUEST_CODE = 10001;
    FusedLocationProviderClient fusedLocationProviderClient;
    com.google.android.gms.location.LocationRequest locationRequest;
    final private int FINE_LOCATION_ACCESS_REQUEST_CODE = 101;
    final private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10;
    final private int MY_PERMISSIONS_REQUEST_SMS_RECEIVE = 105;

    SMSreceiverPassenger smSreceiverPassenger;
    private Polyline mPolyline;

    DbHelper dbHelper;
    Marker  userMarker;
    LatLng current, toLocation;
    TextView date, time, distTV, timeTV, distDTV, timeDTV,DriverNameTV;

    AutoCompleteTextView myLocationEdt, toLocationEdt;
    Button find, call, track;
    LinearLayout det;
    ProgressBar progressBar;
    BottomSheetBehavior bottomSheetBehavior;
    boolean first = true;
    boolean locationParser=true;
    PrefManager mpref;
    String mobileNO, name;

    ArrayList<String> plac = new ArrayList<>();

    ArrayList<Sample> bookings = new ArrayList<>();
    HashMap<String, String> places = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = getLayoutInflater().inflate(R.layout.activity_passenger_home, frameLayout);

        dbHelper = new DbHelper(PassengerHome.this);
        mpref = new PrefManager(PassengerHome.this);
        smSreceiverPassenger.bindListenerPassenger(PassengerHome.this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this);
        mobileNO = mpref.getPhoneNo();
        name = mpref.getName();
        places.put("office","Radiant Info Systems,# 46,7th B Main Rd, 4th Block, Jayanagar, Bengaluru");
        places.put("Ganesha Temple","Ganesha Temple ,WHHJ+HV2, New Diagonal Rd, 7th Main Rd, 4th Block, Jayanagar, Bengaluru,");
        places.put("Union Bank","Union Bank of India,No. 429/31, 30th Cross Road, 4th Block, Jayanagar, Bengaluru, Karnataka 560011");
        places.put("BTM Layout","1005, 17th Main Rd, Tavarekere, Aicobo Nagar, 1st Stage, BTM 1st Stage, Bengaluru, Karnataka 560029");

        places.put("Glass House ","5R75+VHH, Yellur, Karnataka 574111");
        places.put("Main Gate","5R76+9VW Lachil, Karnataka");
        places.put("Stores","5R74+R69 Tenka, Karnataka");
        places.put("DM Plant","5R64+PMP Tenka, Karnataka");
        places.put("Service Building","5R52+FVG Tenka, Karnataka");
        places.put("CHP","5Q4W+JJ4 Tenka, Karnataka");
        places.put("Cargo Gate","5R43+2VH Tenka, Karnataka");
        Set<String> keys =places.keySet();
        for(String s :keys){
            plac.add(s);
        }


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // details for tracking location
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

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
        DriverNameTV = (TextView) findViewById(R.id.DriverNameTV);

        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);


        ArrayAdapter toadapter = new ArrayAdapter(PassengerHome.this, android.R.layout.simple_list_item_1, plac);
        toLocationEdt.setAdapter(toadapter);
        toLocationEdt.setThreshold(1);

        ArrayAdapter fromadapter = new ArrayAdapter(PassengerHome.this, android.R.layout.simple_list_item_1, plac);
        myLocationEdt.setAdapter(fromadapter);
        myLocationEdt.setThreshold(1);

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
                String toLoc = places.get(place);
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(parent.getApplicationWindowToken(), 0);
               if(userMarker!=null) {
                   userMarker.remove();
               }
                toLocationMarker(toLoc);
            }
        });

        myLocationEdt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                String place = parent.getItemAtPosition(i).toString();
                String toLoc = places.get(place);
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(parent.getApplicationWindowToken(), 0);
                if(userMarker!=null) {
                    userMarker.remove();
                }
                fromLocationMarker(toLoc);
            }
        });


        find.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {

                if (find.getText().toString().equalsIgnoreCase("book cabs")) {
                    if (current == null || toLocation == null) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Select Both Locations!", Toast.LENGTH_SHORT).show();
                    } else {
                        find.setText("Please Wait");
                        progressBar.setVisibility(View.VISIBLE);
                        if (ContextCompat.checkSelfPermission(PassengerHome.this, Manifest.permission.SEND_SMS)
                                != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(PassengerHome.this,
                                    Manifest.permission.SEND_SMS)) {

                            } else {
                                ActivityCompat.requestPermissions(PassengerHome.this, new String[]{Manifest.permission.SEND_SMS}, 0);
                            }
                        } else {
                            sentSms("");
                            mpref.saveDriverNO("");
                        }

                    }
                } else if (find.getText().toString().equalsIgnoreCase("Please Wait")) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    Toast.makeText(getApplicationContext(), "Please Wait Until Cab booking", Toast.LENGTH_SHORT).show();

                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }


            }
        });


        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mpref.getDriverNO().equalsIgnoreCase("") || mpref.getDriverNO() == null) {
                    Toast.makeText(getApplicationContext(), "Driver Number Not Available", Toast.LENGTH_SHORT).show();
                } else {
                    String num = mpref.getDriverNO();
                    System.out.println(num);
                    Uri u = Uri.parse("tel:" + num);
                    Intent i = new Intent(Intent.ACTION_DIAL, u);
                    try {
                        startActivity(i);
                    } catch (SecurityException s) {
                        Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mpref.getDlocation().equalsIgnoreCase("")||mpref.getDlocation()==null){
                    Toast.makeText(getApplicationContext(), "Track Failed please Contact Driver", Toast.LENGTH_LONG).show();
                    String num = mpref.getDriverNO();
                    System.out.println(num);

                    Uri u = Uri.parse("tel:" + num);
                    Intent h = new Intent(Intent.ACTION_DIAL, u);
                    try {
                        startActivity(h);
                    } catch (SecurityException s) {
                        Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_LONG).show();
                    }

                }else{
                    String [] driver =mpref.getDlocation().split(",");
                    String [] to =mpref.getTlocation().split(",");
                    String [] cur =mpref.getPlocation().split(",");
                    String address =getAddress(new LatLng(Double.parseDouble(to[0]),Double.parseDouble(to[1])));
                    toLocationMarker(address);

                    driverLocationMarker(new LatLng(Double.parseDouble(driver[0]),Double.parseDouble(driver[1])));
                    getDirectionsUrl(new LatLng(Double.parseDouble(driver[0]),Double.parseDouble(driver[1])),new LatLng(Double.parseDouble(cur[0]),Double.parseDouble(cur[1])));

                }
            }
        });


        ArrayList<Sample> alldata = new ArrayList<>();
        alldata = dbHelper.PreadBookings();
        try {
            Thread.sleep(4000);
        }catch (Exception e){

        }
        if (alldata.size() > 0) {

            bookings.add(alldata.get(0));
            if (bookings.get(0).getStatus().equalsIgnoreCase("Scheduled")) {
                find.setText("Driver Details");
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);

                builder.setTitle("Drop Me! ");
                builder.setMessage("Previous Trip is not Completed!");

                builder.setPositiveButton("TRACK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(mpref.getDlocation().equalsIgnoreCase("")||mpref.getDlocation()==null){
                            Toast.makeText(getApplicationContext(), "Track Failed please Contact Driver", Toast.LENGTH_LONG).show();
                            String num = mpref.getDriverNO();
                            System.out.println(num);

                            Uri u = Uri.parse("tel:" + num);
                            Intent h = new Intent(Intent.ACTION_DIAL, u);
                            try {
                                startActivity(h);
                            } catch (SecurityException s) {
                                Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_LONG).show();
                            }

                        }else{
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            String [] driver =mpref.getDlocation().split(",");
                            String [] to =mpref.getTlocation().split(",");
                            String [] cur =mpref.getPlocation().split(",");
                            String address =getAddress(new LatLng(Double.parseDouble(to[0]),Double.parseDouble(to[1])));
                            getDirectionsUrl(new LatLng(Double.parseDouble(driver[0]),Double.parseDouble(driver[1])),new LatLng(Double.parseDouble(cur[0]),Double.parseDouble(cur[1])));
                            toLocationMarker(address);
                            driverLocationMarker(new LatLng(Double.parseDouble(driver[0]),Double.parseDouble(driver[1])));

                        }

                    }
                });
                builder.setNegativeButton("ENDED", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dbHelper.updateStatus(bookings.get(0).getTripID());
                        Toast.makeText(PassengerHome.this, "ENDED", Toast.LENGTH_SHORT).show();

                        mpref.savePlocation("");
                        mpref.saveTlocation("");
                        mpref.saveDlocation("");
                        find.setText("book cabs");
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startSelectLocationUpdates();
        } else {
            askPermission();
        }
    }

    public String getAddress(LatLng latLng) {
        LatLng Location =latLng;
        String streetAddress = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(Location.latitude, Location.longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                streetAddress = address.getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return streetAddress;
    }

    private boolean sentSms(String mobileNumber) {
        boolean done = false;
        String mess = "POC,," + mobileNO + ",," +
                name + ",," + current.latitude + ",," + current.longitude + ",,"
                + toLocation.latitude + ",," + toLocation.longitude;
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(mobileNumber, null, mess, null, null);
            done = true;
            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            done = false;
            Toast.makeText(getApplicationContext(), "Some fields are Empty", Toast.LENGTH_LONG).show();
        }
        return done;
    }

    @SuppressLint("MissingPermission")
    private void startSelectLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, selectionCallback, Looper.getMainLooper());
    }

    LocationCallback selectionCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d(TAG, "onLocationResult: " + locationResult.getLastLocation());
            if (mMap != null) {
                setUserLocationMarker(locationResult.getLastLocation());

            }
        }
    };

    private void setUserLocationMarker(Location location) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mpref.savePlocation(location.getLatitude()+","+ location.getLongitude());

            LatLng curLocation = new LatLng(location.getLatitude(), location.getLongitude());
           // current = curLocation;
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    String streetAddress = address.getAddressLine(0);
                    //myLocationEdt.setText(streetAddress);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (first) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLocation, 16));
                first = false;
            }
        } else {
            //Ask for permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //We need to show user a dialog for displaying why the permission is needed and then ask for the permission...
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
            }

        }
    }


    private void askPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        Log.d(TAG, "onMapLongClick: " + latLng.toString());
        try {

            if (userMarker != null) {
                userMarker.remove();
            }

            toLocation =latLng;
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String streetAddress = address.getAddressLine(0);
                toLocationEdt.setText(streetAddress);
                userMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(streetAddress).draggable(true));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {
        Log.d(TAG, "onMarkerDragEnd: ");
    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {
        Log.d(TAG, "onMarkerDragEnd: ");
    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker) {
        Log.d(TAG, "onMarkerDragEnd: ");
        LatLng latLng = marker.getPosition();
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String streetAddress = address.getAddressLine(0);
                toLocationEdt.setText(streetAddress);
                userMarker.setTitle(streetAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerDragListener(this);

        // again checking permission if it is not ask!!
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            //zoomToUserLocation();

        } else {  //if its not then ask for permission
            askPermission();
        }
    }
    private void fromLocationMarker(String location) {
        double lat, lang;
        String place_name;
        List<Address> addressList = null;
        Geocoder geocoder;
        geocoder = new Geocoder(PassengerHome.this);
        try {
            addressList = geocoder.getFromLocationName(location, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addressList != null && addressList.size() >= 1) {
            Address address = addressList.get(0);
            lat = Double.parseDouble(String.valueOf(address.getLatitude()));
            lang = Double.parseDouble(String.valueOf(address.getLongitude()));
            mpref.saveTlocation(lat+","+lang);
            place_name = location;
            Log.d("onPostExecute", "Entered into showing locations");
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng latLng = new LatLng(lat, lang);
            current = latLng;


        }
    }


    private void toLocationMarker(String location) {
        double lat, lang;
        String place_name;
        List<Address> addressList = null;
        Geocoder geocoder;
        geocoder = new Geocoder(PassengerHome.this);
        try {
            addressList = geocoder.getFromLocationName(location, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addressList != null && addressList.size() >= 1) {
            Address address = addressList.get(0);
            lat = Double.parseDouble(String.valueOf(address.getLatitude()));
            lang = Double.parseDouble(String.valueOf(address.getLongitude()));
            mpref.saveTlocation(lat+","+lang);
            place_name = location;
            Log.d("onPostExecute", "Entered into showing locations");
            MarkerOptions markerOptions = new MarkerOptions();
            LatLng latLng = new LatLng(lat, lang);
            toLocation = latLng;
            // handleUserMap(latLng);
            markerOptions.position(latLng);
            markerOptions.title(place_name);
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            markerOptions.icon(icon);
            userMarker = mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        }
    }

    private void driverLocationMarker(LatLng location) {

        Log.d("onPostExecute", "Entered into showing locations");
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng latLng = location;
        markerOptions.position(latLng);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.redcar);
        markerOptions.icon(icon);
        userMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

        String to []=mpref.getTlocation().split(",");

        String url = getDirectionsUrl(current, new LatLng(Double.parseDouble(to[0]),Double.parseDouble(to[1])));
        DownloadTask task = new DownloadTask();
        task.execute(url);
    }


    @Override
    public void PassengerMessageReceived(String message) {

            if (mpref.getUsername().equalsIgnoreCase("employee")) {
                if (message.contains("POC_DRIVER")) {
                //confirm
                //parse CAB location
                find.setText("Driver Details");
                progressBar.setVisibility(View.GONE);
                String[] s = message.split(",,");
                String status = s[1];
                String []p = s[2].split(",");
                String []d = s[3].split(",");
                String ID = s[4];
                String lat = s[5];
                String lang = s[6];


                LatLng driver = new LatLng(Double.parseDouble(lat), Double.parseDouble(lang));
                driverLocationMarker(driver);
                mpref.saveDlocation(lat + "," + lang);
                    String pickup = getAddress(new LatLng(Double.parseDouble(p[0]), Double.parseDouble(p[1])));
                    String dropp = getAddress(new LatLng(Double.parseDouble(d[0]), Double.parseDouble(d[1])));



                    if (s[0].equalsIgnoreCase("POC_DRIVER") && status.equalsIgnoreCase("Scheduled")) {
                    boolean don = dbHelper.PinsertTrip(mpref.getPhoneNo(), mpref.getName(),
                            String.valueOf(current), pickup, dropp, status, ID);
                    Double distance = SphericalUtil.computeDistanceBetween(current, driver);
                    String dis = String.format("%.2f", distance / 1000);
                    System.out.println(dis);

                    String url = getDirectionsUrl(current, driver);
                    DownloadTask task = new DownloadTask();
                    task.execute(url);
                }
            }
        }
    }

    private void GetMessageDetails() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, MY_PERMISSIONS_REQUEST_SMS_RECEIVE);
        smSreceiverPassenger = new SMSreceiverPassenger();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        intentFilter.addDataScheme("sms");
        registerReceiver(smSreceiverPassenger, intentFilter);
    }

    /*===========================================================*/


    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Key
        String key = "key=" + getString(R.string.google_maps_key);

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception on download", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    /**
     * A class to download data from Google Directions URL
     */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            // CustomisedProgressDialog.SHOW_CUST_DIA(DriverHome.this,"please wait");
            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("DownloadTask", "DownloadTask : " + data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Directions for DESTINATION in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            // CustomisedProgressDialog.SHOW_CUST_DIA(DriverHome.this,"please wait");
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);

                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {

                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                if (mPolyline != null) {
                    //mPolyline.remove();
                    lineOptions.color(Color.MAGENTA);
                }
                mPolyline = mMap.addPolyline(lineOptions);
                if(locationParser){
                    String[] dis = DirectionsJSONParser.Driverdistance.split(",");
                    distDTV.setText(dis[0]);
                    timeDTV.setText(dis[1]);
                    DirectionsJSONParser.Driverdistance = "";
                    DirectionsJSONParser.Driverdistance = "";
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 16));
                    DirectionsJSONParser.Driverdistance = "";
                    locationParser=false;

                }

            } else
                Toast.makeText(getApplicationContext(), "No route is found", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSelectLocationUpdates();
                GetMessageDetails();
            } else {
                GetMessageDetails();
            }
        }
        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                Toast.makeText(this, "You can add geofence...", Toast.LENGTH_SHORT).show();


            } else {
                //We do not have the permission..
                Toast.makeText(this, "Background location access is necessary for geofence to trigger...", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == MY_PERMISSIONS_REQUEST_SMS_RECEIVE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (MY_PERMISSIONS_REQUEST_SMS_RECEIVE == requestCode) {
                    Log.i("SMS", "REQUEST_READ_SMS_PERMISSION Permission Granted");
                }

            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (MY_PERMISSIONS_REQUEST_SMS_RECEIVE == requestCode) {
                    // TODO REQUEST_READ_SMS_PERMISSION Permission is not Granted.
                    // TODO Request Not Granted.
                    // This code is for get permission from setting.
                    final Intent i = new Intent();
                    i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    i.addCategory(Intent.CATEGORY_DEFAULT);
                    i.setData(Uri.parse("package:" + getPackageName()));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(i);
                }
            }
        }
    }

}
