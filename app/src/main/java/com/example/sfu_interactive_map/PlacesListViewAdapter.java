package com.example.sfu_interactive_map;

import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import de.hdodenhof.circleimageview.CircleImageView;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class PlacesListViewAdapter extends RecyclerView.Adapter<PlacesListViewAdapter.ViewHolder> {
    private Context context;
    private List<Place> placesList;
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView placeName, hours;
        public CircleImageView photo;
        public RelativeLayout layout;
        public ViewHolder(View view) {
            super(view);
            layout = (RelativeLayout) view.findViewById(R.id.layout);
            placeName = (TextView) view.findViewById(R.id.place_name);
            hours = (TextView) view.findViewById(R.id.operating_hours);
            photo = (CircleImageView) view.findViewById(R.id.place);
            view.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    loadIntent(getAdapterPosition());
                }
            });
            layout.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    loadIntent(getAdapterPosition());
                }
            });
        }
    }

    private void loadIntent(int position) {
        Intent intent;
        intent = new Intent(context, Details.class);
        Place place = placesList.get(position);
        intent.putExtra("in", "ls");
        intent.putExtra("name", place.getName());
        intent.putExtra("description", place.getDescription());
        intent.putExtra("location",place.getAddress());
        intent.putExtra("photo", place.getPhoto());
        intent.putExtra("contact", place.getContact());
        intent.putStringArrayListExtra("hours", (ArrayList<String>) place.getHours());
        Vote.setCurrVotes(place.getVoteTypes());
        context.startActivity(intent);
    }

    public PlacesListViewAdapter(Context context, List<Place> placesList) {
        this.context = context;
        this.placesList = placesList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_style, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Place place = placesList.get(position);
        holder.placeName.setText(place.getName());
        holder.hours.setText(getDayofWeek(place.getHours()));
        Drawable drawable =  ContextCompat.getDrawable(context, place.getPhoto());
        holder.photo.setImageDrawable(drawable);
    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }

    public void clear() {
        placesList.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Place> list) {
        placesList.addAll(list);
        notifyDataSetChanged();
    }

    public void add(Place Place) {
        placesList.add(Place);
        notifyDataSetChanged();
    }

    public String getDayofWeek(List<String> weekhours){
        Map<Integer, String> daymap =  new HashMap<Integer, String>();
        daymap.put(Calendar.MONDAY, "mon");
        daymap.put(Calendar.TUESDAY, "tue");
        daymap.put(Calendar.WEDNESDAY, "wed");
        daymap.put(Calendar.THURSDAY, "thu");
        daymap.put(Calendar.FRIDAY, "fri");
        daymap.put(Calendar.SATURDAY, "sat");
        daymap.put(Calendar.SUNDAY, "sun");
        Calendar calendar = Calendar.getInstance();
        Integer dayofweek = calendar.get(calendar.DAY_OF_WEEK);
        for(String dayhours  : weekhours)
            if(dayhours.toLowerCase().startsWith(daymap.get(dayofweek)))
                return dayhours;
        return "N/A";
    }
}