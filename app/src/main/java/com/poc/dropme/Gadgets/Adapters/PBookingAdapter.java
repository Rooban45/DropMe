package com.poc.dropme.Gadgets.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.poc.dropme.DataBase.DbHelper;
import com.poc.dropme.Gadgets.Models.Sample;
import com.poc.dropme.PassengerHome;
import com.poc.dropme.R;

import java.util.ArrayList;

public class PBookingAdapter extends RecyclerView.Adapter<PBookingAdapter.ViewHolder> {

    Context context;
    ArrayList<Sample> data;
    DbHelper dbHelper;

    public PBookingAdapter(Context context, ArrayList<Sample> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public PBookingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_p_bookings, parent, false);
        return new ViewHolder(view);
    }

    @Override
    @SuppressLint("RecyclerView")
    public void onBindViewHolder(@NonNull PBookingAdapter.ViewHolder holder, int position) {

        holder.from.setText(data.get(position).getPicup());
        holder.to.setText(data.get(position).getDrop());
        holder.status.setText(data.get(position).getStatus());
        if (data.get(position).getStatus().equalsIgnoreCase("Cancelled")) {
            holder.status.setTextColor(Color.parseColor("#e82a15"));
        }
        holder.from.setSelected(true);
        holder.to.setSelected(true);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle("Drop Me! ");
                builder.setMessage("Select one Action");

                builder.setPositiveButton("TRACK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                    }
                });
                builder.setNegativeButton("END", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dbHelper = new DbHelper(context.getApplicationContext());

                        dbHelper.updateStatus(data.get(position).getTripID());
                        Toast.makeText(context.getApplicationContext(), "TRIP ENDED", Toast.LENGTH_SHORT).show();
                        Intent in = new Intent(context.getApplicationContext(), PassengerHome.class);
                        context.startActivity(in);
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
        private TextView from, to, status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializing our text views
            from = itemView.findViewById(R.id.RVPickUpTV);
            to = itemView.findViewById(R.id.RVDropTV);
            status = itemView.findViewById(R.id.RVstatus);


        }
    }
}
