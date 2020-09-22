package com.example.bluetoothfinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;


public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>
{
    Context context;

    public MyAdapter(Context ct)
    {
        context = ct;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view =  inflater.inflate(R.layout.my_row, parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position)
    {
        holder.blueToothDeviceTextView.setText(MainActivity.bluetoothDevices.get(position));

        holder.mainLayout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

            }
        });
    }

    @Override
    public int getItemCount()
    {
        return MainActivity.bluetoothDevices.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        ConstraintLayout mainLayout;
        TextView blueToothDeviceTextView;

        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);

            blueToothDeviceTextView = itemView.findViewById(R.id.blueToothDeviceTextView);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}