package com.poc.dropme.Gadgets;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.poc.dropme.Bookings.PassengerBookings;
import com.poc.dropme.Gadgets.Adapters.PBookingAdapter;
import com.poc.dropme.LoginActivity;
import com.poc.dropme.R;

import java.util.Calendar;

public class BaseActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    protected FrameLayout frameLayout;
    private Context context;
    private Toolbar toolbar;
    private static TextView txt_menuTitle;
    public static TextView txt_username;
    public static TextView txt_email;
    public static TextView txt_change_pass;
    public static TextView txt_card_value;

    public ImageView img_menuOption, image_profile, img_menu_add_cart;
    public FloatingActionButton img_menu_prac_test;

    private DrawerLayout drawer;

    PrefManager prefManager;

    ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);


        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        prefManager = new PrefManager(BaseActivity.this);


        prefManager = new PrefManager(BaseActivity.this);
        context = this;
        initView();
        frameLayout = (FrameLayout) findViewById(R.id.container);
        FloatingActionButton fab = findViewById(R.id.img_menu_prac_test);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        if (txt_username.getText().equals("")) {
            //  startActivity(new Intent(BaseActivity.this, LoginActivity.class));
        } else {
            txt_email.setText(prefManager.getEmail());
            txt_username.setText(prefManager.getUsername());
        }
        BaseActivity.txt_change_pass.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Launching News Feed Screen
                clearSharedPreference();
            }
        });


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void clearSharedPreference() {
        prefManager.saveEmail("");
        prefManager.saveUsername("");
        startActivity(new Intent(BaseActivity.this, LoginActivity.class));
        finish();

    }


    private void initView() {
        toolbar = findViewById(R.id.toolbar);
        img_menu_prac_test = findViewById(R.id.img_menu_prac_test);
        img_menuOption = findViewById(R.id.img_menuOption);
        img_menu_add_cart = findViewById(R.id.img_menu_add_cart);
        txt_card_value = findViewById(R.id.txt_card_value);


        img_menuOption.setBackgroundResource(R.drawable.ic_menu);
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerview = navigationView.getHeaderView(0);
        navigationView.setItemIconTintList(null);
        image_profile = headerview.findViewById(R.id.image_profile);
        txt_username = headerview.findViewById(R.id.txt_username);
        txt_email = headerview.findViewById(R.id.txt_email);
        txt_change_pass = headerview.findViewById(R.id.txt_change_pass);
        toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        img_menuOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        img_menu_add_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "You Click On Trips", Toast.LENGTH_SHORT).show();
            }
        });
        txt_change_pass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "You Click On Reset Password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.base, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;

        if (id == R.id.nav_home) {
            intent = new Intent(this, PassengerBookings.class);
            startActivity(intent);
            finish();
        }
        /*
        else if (id == R.id.nav_gallery) {
            intent=new Intent(this, Cancellation.class);
            startActivity(intent);
            finish();

        }

         else if (id == R.id.nav_share) {

            Toast.makeText(context, "You Click On Share", Toast.LENGTH_SHORT).show();
            intent=new Intent(this, MapsActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_send) {

            Toast.makeText(context, "You Click On Send", Toast.LENGTH_SHORT).show();

        }
*/

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}