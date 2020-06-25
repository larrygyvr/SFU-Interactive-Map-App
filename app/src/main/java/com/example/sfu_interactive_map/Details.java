package com.example.sfu_interactive_map;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class Details extends AppCompatActivity {
    Toolbar toolbar;
    TextView title;
    TextView about;
    TextView description;
    TextView contact;
    TextView hours[];
    ImageView photo;
    NestedScrollView nestedListview;

    private List<Vote> votestypes;
    private Vote currVote;
    private LinearLayout voteLLayout1;
    private LinearLayout voteLLayout2;
    private LinearLayout voteLLayout3;
    private Intent data;


    protected void taste(@NonNull Vote vote, LinearLayout voteLLayout) {
        dialogVote(vote, voteLLayout);
    }

    protected void service(@NonNull Vote vote,LinearLayout voteLLayout) {
        dialogVote(vote, voteLLayout);
    }

    protected void cleanliness(@NonNull Vote vote,LinearLayout voteLLayout) {
        dialogVote(vote, voteLLayout);
    }

    protected void backToMain(View view) {
        super.onBackPressed();
        Intent intent;
        intent = new Intent(Details.this, ListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        hours = new TextView[7];
        data = getIntent();
        votestypes = Vote.getCurrVotes();
        //Toast.makeText(this, Long.toString(votestypes.get(0).getNum_votes()),Toast.LENGTH_SHORT ).show();
        Button buttonTaste = (Button) findViewById(R.id.buttonTaste);
        Button buttonService = (Button) findViewById(R.id.buttonService);
        Button buttonCleanliness = (Button) findViewById(R.id.buttonCleanliness);
        ImageButton buttonLeft = (ImageButton) findViewById(R.id.buttonLeft);

        title = (TextView) findViewById(R.id.title);
        about = (TextView)findViewById(R.id.about);
        description= (TextView)findViewById(R.id.description);
        contact = (TextView)findViewById(R.id.contact);
        hours[0] = (TextView)findViewById(R.id.monday);
        hours[1] = (TextView)findViewById(R.id.tuesday);
        hours[2] = (TextView)findViewById(R.id.wednesday);
        hours[3] = (TextView)findViewById(R.id.thursday);
        hours[4] = (TextView)findViewById(R.id.friday);
        hours[5] = (TextView)findViewById(R.id.saturday);
        hours[6] = (TextView)findViewById(R.id.sunday);
        photo = (ImageView)findViewById(R.id.photo);
        voteLLayout1 = (LinearLayout) findViewById(R.id.vote1);
        voteLLayout2 = (LinearLayout) findViewById(R.id.vote2);
        voteLLayout3 = (LinearLayout) findViewById(R.id.vote3);

        nestedListview = (NestedScrollView)findViewById(R.id.nestedListview);

        title.setText("Details");
        about.setText(data.getStringExtra("name"));
        description.setText(data.getStringExtra("description"));
        contact.setText(data.getStringExtra("contact"));
        hours[0].setText(data.getStringArrayListExtra("hours").get(0));
        hours[1].setText(data.getStringArrayListExtra("hours").get(1));
        hours[2].setText(data.getStringArrayListExtra("hours").get(2));
        hours[3].setText(data.getStringArrayListExtra("hours").get(3));
        hours[4].setText(data.getStringArrayListExtra("hours").get(4));
        hours[5].setText(data.getStringArrayListExtra("hours").get(5));
        hours[6].setText(data.getStringArrayListExtra("hours").get(6));
        Drawable drawable =  ContextCompat.getDrawable(this, data.getIntExtra("photo",0));
        photo.setBackground(drawable);
        ((TextView)voteLLayout1.getChildAt(0)).setText(String.format("%.1f",votestypes.get(0).getRating()));
        ((TextView)voteLLayout1.getChildAt(1)).setText(votestypes.get(0).getRate_type());
        ((TextView)voteLLayout1.getChildAt(2)).setText(Long.toString(votestypes.get(0).getNum_votes()));

        ((TextView)voteLLayout2.getChildAt(0)).setText(String.format("%.1f",votestypes.get(1).getRating()));
        ((TextView)voteLLayout2.getChildAt(1)).setText(votestypes.get(1).getRate_type());
        ((TextView)voteLLayout2.getChildAt(2)).setText(Long.toString(votestypes.get(1).getNum_votes()));

        ((TextView)voteLLayout3.getChildAt(0)).setText(String.format("%.1f",votestypes.get(2).getRating()));
        ((TextView)voteLLayout3.getChildAt(1)).setText(votestypes.get(2).getRate_type());
        ((TextView)voteLLayout3.getChildAt(2)).setText(Long.toString(votestypes.get(2).getNum_votes()));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        nestedListview.smoothScrollTo(0,0);
        buttonTaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currVote = votestypes.get(0);
                taste(currVote,voteLLayout1);
            }
        });
        buttonService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currVote = votestypes.get(1);
                service(currVote,voteLLayout2);
            }
        });
        buttonCleanliness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currVote = votestypes.get(2);
                cleanliness(currVote,voteLLayout3);
            }
        });
        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backToMain(view);
                //onBackPressed();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    protected void dialogVote(final Vote vote, final LinearLayout voteLLayout) {
        final Dialog dialog = new Dialog(Details.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_vote);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        final TextView num = (TextView) dialog.findViewById(R.id.num);

        Button plus = (Button) dialog.findViewById(R.id.plus);
        plus.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                int n = Integer.parseInt(num.getText().toString());
                if(n < 5) {
                    n+=1;
                    num.setText("" + String.valueOf(n));
                }
            }
        });

        Button minus = (Button) dialog.findViewById(R.id.minus);
        minus.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                int n = Integer.parseInt(num.getText().toString());
                if(n > 1) {
                    n-=1;
                    num.setText("" + String.valueOf(n));
                }
            }
        });

        Button submit = (Button)dialog.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int n = Integer.parseInt(num.getText().toString());
                vote.calcNewRating(n);
                //Toast.makeText(getApplicationContext(), "size: "+voteLLayout.getChildCount(), Toast.LENGTH_SHORT).show();
                TextView rating = (TextView) voteLLayout.getChildAt(0);
                rating.setText(String.format("%.1f",vote.getRating()));
                TextView numVotes = (TextView)voteLLayout.getChildAt(2);
                numVotes.setText(Long.toString(vote.getNum_votes()));
                dialog.dismiss();
            }
        });


        TextView title = (TextView) dialog.findViewById(R.id.title);
        title.setText(vote.getRate_type());

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}