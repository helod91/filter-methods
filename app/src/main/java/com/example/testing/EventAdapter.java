package com.example.testing;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testing.db.Events;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressLint("NotifyDataSetChanged")
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

    private List<Events> data = new ArrayList<>();

    public void setData(List<Events> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout item = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_event,
                parent,
                false
        );

        return new ViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.primaryLocation.setText(data.get(position).getPrimary_Location());
        holder.secondaryLocation.setText(data.get(position).getSecondary_Location());
        holder.type.setText(data.get(position).getEvent_Type());
        holder.time.setText(data.get(position).getTrigger_Time());

        try {
            Date triggerTime = dateFormat.parse(data.get(position).getTrigger_Time());
            Date resetTime = dateFormat.parse(data.get(position).getReset_Time());

            if (triggerTime != null && resetTime != null) {
                long difference = resetTime.getTime() - triggerTime.getTime();
                long differenceInSeconds = (difference / 1000) % 60;
                long differenceInMinutes = (difference / (1000 * 60)) % 60;

                if (differenceInMinutes != 0) {
                    holder.duration.setText(differenceInMinutes + "m " + differenceInSeconds + "s");
                } else {
                    holder.duration.setText(differenceInSeconds + "s");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView primaryLocation, secondaryLocation, type, time, duration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            primaryLocation = itemView.findViewById(R.id.event_primary_location);
            secondaryLocation = itemView.findViewById(R.id.event_secondary_location);
            type = itemView.findViewById(R.id.event_type);
            time = itemView.findViewById(R.id.event_time);
            duration = itemView.findViewById(R.id.event_duration);
        }
    }
}
