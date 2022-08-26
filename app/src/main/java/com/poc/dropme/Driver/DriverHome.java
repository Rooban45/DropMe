package com.poc.dropme.Driver;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
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

import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.maps.android.SphericalUtil;
import com.poc.dropme.DataBase.DbHelper;
import com.poc.dropme.Gadgets.BaseActivity;
import com.poc.dropme.Gadgets.DirectionParser.DirectionsJSONParser;
import com.poc.dropme.Gadgets.GeofenceBroadcastReceiver;
import com.poc.dropme.Gadgets.GeofenceHelper;
import com.poc.dropme.Gadgets.LocationCall;
import com.poc.dropme.Gadgets.Models.Sample;
import com.poc.dropme.Gadgets.PrefManager;
import com.poc.dropme.Gadgets.SmsBroadcast.MessageListener;

import com.poc.dropme.Gadgets.SmsBroadcast.SMSreceiver;

import com.poc.dropme.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DriverHome extends BaseActivity implements OnMapReadyCallback, MessageListener, GoogleMap.OnMapLongClickListener, LocationCall, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    Button tripStatusButton;
    BottomSheetBehavior bottomSheetBehavior;
    final int ACCESS_LOCATION_REQUEST_CODE = 1001;
    FusedLocationProviderClient cabLocationProvider;
    LocationRequest cabLocationRequest;
    private Geocoder geocoder;
    Marker  userMarker;
    int SeatCapacity;
    final private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    final private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    final private int MY_PERMISSIONS_REQUEST_SMS_RECEIVE = 10;
    boolean first = true;
    SMSreceiver smsReceiver;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;

    final private float GEOFENCE_RADIUS = 30;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";
    LatLng curLocation;
    private Polyline mPolyline;


    PrefManager mpref;
    DbHelper dbHelper;
    ArrayList<Sample> bookings = new ArrayList<>();
    RecyclerView passengers;
    LinearLayout headRv;
    String plat;
    String plang;
    String tlat;
    String tlong;

    TextView SeatsTV;
    GeofenceBroadcastReceiver mListenerPP;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.activity_driver_home, frameLayout);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        geocoder = new Geocoder(this);
        smsReceiver.bindListener(DriverHome.this);
        geocoder = new Geocoder(this);
        mListenerPP.bindListenergeo(DriverHome.this);
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);
        mpref = new PrefManager(DriverHome.this);
        dbHelper = new DbHelper(this);


        passengers = (RecyclerView) findViewById(R.id.PassengerRV);
        headRv = (LinearLayout) findViewById(R.id.headRVpassengers);
        tripStatusButton = (Button) findViewById(R.id.tripStatusButton);
        SeatsTV = (TextView) findViewById(R.id.SeatsTV);
        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        CheckTripdetails();

        if (mpref.gettripId() == null || mpref.gettripId().equalsIgnoreCase("")) {
            mpref.savetripId("0");
        }


        tripStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckTripdetails();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        // cab data
        cabLocationProvider = LocationServices.getFusedLocationProviderClient(this);
        cabLocationRequest = LocationRequest.create();
        cabLocationRequest.setInterval(2000);
        cabLocationRequest.setFastestInterval(2000);
        cabLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        BaseActivity.txt_card_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }


      private void CheckTripdetails(){
          bookings.clear();
          ArrayList<Sample> allBooking = new ArrayList<>();
          allBooking = dbHelper.readBookings();
          ArrayList<Sample> check = new ArrayList<>();

          if (allBooking.size() > 0) {
              for (Sample data : allBooking) {
                  if (data.getStatus().equalsIgnoreCase("Scheduled")) {
                      bookings.add(data);
                  }
              }
          }

          if (bookings.size() > 0) {
              BaseActivity.txt_card_value.setText(String.valueOf(bookings.size()));
              SeatCapacity = 6 - bookings.size();
              SeatsTV.setText("Seats Left :" + String.valueOf(SeatCapacity));
              passengers.setVisibility(View.VISIBLE);
              headRv.setVisibility(View.VISIBLE);
              BookingAdapter adapter = new BookingAdapter(this, bookings);
              LinearLayoutManager manager = new LinearLayoutManager(DriverHome.this, RecyclerView.VERTICAL, false);
              passengers.setLayoutManager(manager);
              passengers.setAdapter(adapter);

          } else {
              SeatsTV.setText("Seats Left :" + String.valueOf(6));
              passengers.setVisibility(View.GONE);
              headRv.setVisibility(View.GONE);
              mpref.saveTripStatus("");
          }
    }

    private void GetMessageDetails() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, MY_PERMISSIONS_REQUEST_SMS_RECEIVE);
        smsReceiver = new SMSreceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        intentFilter.addDataScheme("sms");
        registerReceiver(smsReceiver, intentFilter);
    }


    @SuppressLint("MissingPermission")
    private void cabLocationUpdates() {
        cabLocationProvider.requestLocationUpdates(cabLocationRequest, cabCallBack, Looper.getMainLooper());
    }

    LocationCallback cabCallBack = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d("", "onLocationResult: " + locationResult.getLastLocation());
            if (mMap != null) {
                setCabLocationMarker(locationResult.getLastLocation());
            }
        }
    };

    private void setCabLocationMarker(Location location) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            curLocation = new LatLng(location.getLatitude(),location.getLongitude());
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(), 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    String streetAddress = address.getAddressLine(0);
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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // again checking permission if it is not ask!!
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            //zoomToUserLocation();

        } else {  //if its not then ask for permission
            askPermission();
        }
    }

    private void askPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            //We can show user a dialog why this permission is necessary
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        handleUserMap(latLng);
    }

    @SuppressLint({"MissingPermission", "NewApi"})
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cabLocationUpdates();
                GetMessageDetails();
            } else {
                GetMessageDetails();
                //We can show a dialog that permission is not granted...
//                Toast.makeText(getApplicationContext(), "Permission is not Granted", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                mMap.setMyLocationEnabled(true);

            } else {
                //We do not have the permission..

            }
        }
        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show();
                GetMessageDetails();
            } else {
                //We do not have the permission..
                Toast.makeText(this, "Background location access is neccessary for geofences to trigger...", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == MY_PERMISSIONS_REQUEST_SMS_RECEIVE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "SMS permission granted", Toast.LENGTH_LONG).show();

            } else {
//                Toast.makeText(getApplicationContext(), "SMS faild, please try again.", Toast.LENGTH_LONG).show();
                return;
            }
        }

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

    @Override
    protected void onStart() {
        super.onStart();
        // check weather permission has granted or not !!
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            cabLocationUpdates();
        } else {
            // you need to request permissions...
            askPermission();
        }

    }

    @SuppressLint("NewApi")
    @Override
    public void messageReceived(String message) {

        if (mpref.getUsername().equalsIgnoreCase("driver")) {
            if (message.contains("POC")) {

                String[] datas = message.split(",,");

                if (mpref.getTripStatus().equalsIgnoreCase("OnGoing")) {

                    String number = datas[1];
                    String Name = datas[2];
                    plat = datas[3];
                    plang = datas[4];
                    tlat = datas[5];
                    tlong = datas[6];
                    String status = "Scheduled";

                    double distance = SphericalUtil.computeDistanceBetween(curLocation, new LatLng(Double.parseDouble(plat), Double.parseDouble(plang)));
                    double d = distance / 1000;
                    String dis = String.format("%.2f", distance / 1000) + "kms";
                    System.out.println(dis);
                    // seatcapcity?
                    if (d <= 12) {
                        String pickup = getAddress(new LatLng(Double.parseDouble(plat), Double.parseDouble(plang)));
                        String drop = getAddress(new LatLng(Double.parseDouble(tlat), Double.parseDouble(tlong)));
                        int id = Integer.parseInt(mpref.gettripId());
                        id++;
                        mpref.savetripId(String.valueOf(id));
                        toLocationMarker(new LatLng(Double.parseDouble(plat), Double.parseDouble(plang)));
                        dbHelper.insertTrip(number, Name, plat + "," + plang, pickup, drop, status, "POC" + mpref.gettripId(), tlat + "," + tlong);
                        sentSms(number, plat + "," + plang, tlat + "," + tlong, "POC" + mpref.gettripId());
                        ShowAlertDialog("Nearby Passenger Pickup", pickup, drop);
                    }
                } else {
                    if (datas[0].equalsIgnoreCase("POC")) {
                        // do geofencing with Messages
                        String number = datas[1];
                        String Name = datas[2];
                        plat = datas[3];
                        plang = datas[4];
                        tlat = datas[5];
                        tlong = datas[6];
                        String status = "Scheduled";
                        mpref.saveTripStatus("OnGoing");
                        mpref.savePlocation(plat + "," + plang);
                        mpref.saveTlocation(tlat + "," + tlong);

                        String pickup = getAddress(new LatLng(Double.parseDouble(plat), Double.parseDouble(plang)));
                        String drop = getAddress(new LatLng(Double.parseDouble(tlat), Double.parseDouble(tlong)));

                        int id = Integer.parseInt(mpref.gettripId());
                        id++;
                        mpref.savetripId(String.valueOf(id));

                        dbHelper.insertTrip(number, Name, plat + "," + plang, pickup, drop, status, "POC" + mpref.gettripId(), tlat + "," + tlong);
                        LatLng latLng = new LatLng(Double.parseDouble(plat), Double.parseDouble(plang));
                        handleUserMap(latLng);
                        sentSms(number,  plat + "," + plang, tlat + "," + tlong, "POC" + mpref.gettripId());
                        ShowAlertDialog("New Ride to... ", pickup, drop);
                    }

                }

            }
        }
    }

    private void ShowAlertDialog(String title, String pickup, String drop) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(pickup +"\n" + "  TO  " + "\n" + "\n" +drop);
        builder.setCancelable(false);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton("", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();

    }

    public String getAddress(LatLng latLng) {
        LatLng Location = new LatLng(latLng.latitude, latLng.longitude);
        String streetAddress = "";
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                streetAddress = address.getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return streetAddress;
    }

    private boolean sentSms(String mobileNumber, String pickup, String drop, String id) {
        boolean done = false;
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(mobileNumber, null, "POC_DRIVER,," + "Scheduled,,"
                    + pickup + ",," + drop + ",," + id + ",," + curLocation.latitude + ",," + curLocation.longitude + ",," + "KA 01 AK 5577" , null, null);
            done = true;
            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            done = false;
            Toast.makeText(getApplicationContext(), "Some fields are Empty", Toast.LENGTH_LONG).show();
        }
        return done;
    }


    private void handleUserMap(LatLng latLng) {
        // mMap.clear();
        addMarker(latLng);
        addCircle(latLng, GEOFENCE_RADIUS);
        addGeofence(latLng, GEOFENCE_RADIUS);
    }

    private void addGeofence(LatLng latLng, float radius) {

        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, latLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
        if (Build.VERSION.SDK_INT >= 29) {
            //We need background permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("driver", "onSuccess: Geofence Added...");
                                Toast.makeText(getApplicationContext(), "calling ", Toast.LENGTH_SHORT);
                                Log.d(TAG, "onSuccess: 222222222222222222222222222222222222222222222222");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                String errorMessage = geofenceHelper.getErrorString(e);
                                Log.d("driver", "onFailure: " + errorMessage);
                            }
                        });
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    //We show a dialog and ask for permission
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }
        }


    }

    private void addMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.addMarker(markerOptions);

    }

    private void addCircle(LatLng latLng, float radius) {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(latLng);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.argb(255, 255, 0, 0));
        circleOptions.fillColor(Color.argb(64, 255, 0, 0));
        circleOptions.strokeWidth(4);
        mMap.addCircle(circleOptions);
        String url = getDirectionsUrl(curLocation, latLng);

        // Start downloading json data from Google Directions API
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);
    }

    /*=================================== entered Geo Locations =====================================*/
    @Override
    public void enteredDwell(String s) {
        System.out.println(curLocation);

        System.out.println(s);
        String tLatlong = mpref.getTlocation();
        String[] Lall = tLatlong.split(",");

        String url = getDirectionsUrl(curLocation, new LatLng(Double.parseDouble(Lall[0]), Double.parseDouble(Lall[1])));
        toLocationMarker(new LatLng(Double.parseDouble(Lall[0]), Double.parseDouble(Lall[1])));
        // Start downloading json data from Google Directions API
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);

    }

    private void toLocationMarker(LatLng location) {

        Log.d("onPostExecute", "Entered into showing locations");
        MarkerOptions markerOptions = new MarkerOptions();
        LatLng latLng =location;
        markerOptions.position(latLng);
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        markerOptions.icon(icon);
        userMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

    }

    /*============================  LOCATION PARSER  ===============================*/


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

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return false;
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
                    mPolyline.remove();
                    lineOptions.color(Color.MAGENTA);
                }
                mPolyline = mMap.addPolyline(lineOptions);


            } else
                Toast.makeText(getApplicationContext(), "No route is found", Toast.LENGTH_LONG).show();
        }
    }


        /*==================================  ADAPTER  ======================================*/

    public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.ViewHolder> {

        Context context;
        ArrayList<Sample> data;
        DbHelper dbHelper;

        public BookingAdapter(Context context, ArrayList<Sample> data) {
            this.context = context;
            this.data = data;
        }

        @NonNull
        @Override
        public BookingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_passenger_in_car, parent, false);
            return new ViewHolder(view);
        }

        @Override
        @SuppressLint("RecyclerView")
        public void onBindViewHolder(@NonNull BookingAdapter.ViewHolder holder, int position) {
            holder.name.setText(data.get(position).getName());
            holder.from.setText(data.get(position).getPicup());
            holder.to.setText(data.get(position).getDrop());

            holder.end.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dbHelper = new DbHelper(context);
                    String id = data.get(position).getTripID();
                    dbHelper.updateStatusD(id);
                    Toast.makeText(context.getApplicationContext(), "TRIP COMPLETED", Toast.LENGTH_SHORT).show();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    notifyDataSetChanged();
                    mMap.clear();

                    CheckTripdetails();
                }
            });


            holder.det.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    builder.setTitle("Drop Me! ");
                    builder.setMessage("Select one Action");

                    builder.setPositiveButton("TRACK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            String [] to = data.get(position).getTlatLng().split(",");
                            String []passenger = data.get(position).getLatLng().split(",");

                            String url = getDirectionsUrl( new LatLng(Double.parseDouble(passenger[0]),Double.parseDouble(passenger[1])),new LatLng(Double.parseDouble(to[0]),Double.parseDouble(to[1])));
                            toLocationMarker( new LatLng(Double.parseDouble(passenger[0]),Double.parseDouble(passenger[1])));
                            toLocationMarker(new LatLng(Double.parseDouble(to[0]),Double.parseDouble(to[1])));
                            // Start downloading json data from Google Directions API
                            DownloadTask downloadTask = new DownloadTask();
                            downloadTask.execute(url);
                        }
                    });

                    builder.setNegativeButton("CALL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String number = data.get(position).getNumber();
                            Uri u = Uri.parse("tel:" +number);
                            Intent h = new Intent(Intent.ACTION_DIAL, u);
                            try {
                                startActivity(h);
                            }
                            catch (SecurityException s) {
                                Toast.makeText(getApplicationContext(), "An error occurred", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    builder.show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView from, to, name;
            ImageView end;
            LinearLayout det;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                // initializing our text views
                from = itemView.findViewById(R.id.RVPickUpTV);
                to = itemView.findViewById(R.id.RVDropTV);
                name = itemView.findViewById(R.id.RVpassengerTV);
                end = itemView.findViewById(R.id.endIV);
                det = itemView.findViewById(R.id.LinDetails);

            }
        }
    }

}