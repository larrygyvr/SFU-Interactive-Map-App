package com.example.sfu_interactive_map;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.TestLooperManager;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.TileOverlay;

public class ListActivity extends AppCompatActivity {
    ImageButton backButton;
    Toolbar toolbar;
    TextView title;
    RecyclerView recyclerView;

    private List<Place> placesList;
    private PlacesListViewAdapter placesListAdapter;
    private LinearLayoutManager layoutManager;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        backButton = findViewById(R.id.buttonLeft);
        recyclerView =(RecyclerView)findViewById(R.id.recyclerView);
        title = (TextView) findViewById(R.id.title);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        placesList = new ArrayList<>();
        placesListAdapter = new PlacesListViewAdapter(this, placesList);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHorizontalScrollBarEnabled(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(placesListAdapter);
        title.setText("Restaurants");
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        getPlaces();
    }

    private void getPlaces() {

        populatePlaces();
        placesList.addAll(Place.getPlaces());
        placesListAdapter.notifyDataSetChanged();

        recyclerView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                        for (int i = 0; i < recyclerView.getChildCount(); i++) {
                            View v = recyclerView.getChildAt(i);
                            v.setAlpha(0.0f);
                            v.animate().alpha(1.0f)
                                    .setDuration(300)
                                    .setStartDelay(i * 50)
                                    .start();
                        }

                        return true;
                    }
                });
    }

    public void populatePlaces(){
        if(Place.getPlaces() == null || Place.getPlaces().isEmpty()){
            Resources res = getResources();
            TypedArray photoArr = res.obtainTypedArray(R.array.photo);
            TypedArray ta = res.obtainTypedArray(R.array.places);
            String[][] placeAttrib = new String[ta.length()][];
            for(int i= 0; i<ta.length(); i++){
                int id = ta.getResourceId(i, 0);
                int photoID = photoArr.getResourceId(i,0);
                placeAttrib[i] = res.getStringArray(id);
                Place place = new Place(
                        placeAttrib[i][0],
                        placeAttrib[i][1],
                        photoID,
                        placeAttrib[i][2],
                        placeAttrib[i][3]
                    );
                place.setHours(
                        placeAttrib[i][4],
                        placeAttrib[i][5],
                        placeAttrib[i][6],
                        placeAttrib[i][7],
                        placeAttrib[i][8],
                        placeAttrib[i][9],
                        placeAttrib[i][10]
                );
                place.setVoteTypes("taste", "service", "cleanliness");
                Place.addPlace(place);
            }
            photoArr.recycle();
            ta.recycle();
        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}