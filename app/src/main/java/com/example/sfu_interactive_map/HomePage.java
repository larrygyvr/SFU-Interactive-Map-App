package com.example.sfu_interactive_map;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

public class HomePage extends AppCompatActivity {
    private ImageView[] images;
    private Button findYourClass;
    private Button notableLocations;
    final int[] img_ind = {
            R.drawable.home_page_aq1,
            R.drawable.home_page_aq2,
            R.drawable.home_page_aq3,
            R.drawable.home_page_aq4,
            R.drawable.home_page_blu_hall1,
            R.drawable.home_page_blu_hall2,
            R.drawable.home_page_blu_hall3,
            R.drawable.home_page_conv_mall1,
            R.drawable.home_page_conv_mall2,
            R.drawable.home_page_green_house1,
            R.drawable.home_page_saywell_hall1,
            R.drawable.home_page_saywell_hall2
    };
    int count1 = 0;
    int count2 = 1;
    boolean firsttime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        findYourClass = (Button)findViewById(R.id.findYourClass);
        notableLocations = (Button)findViewById(R.id.notableLocations);

        images = new ImageView[2];
        images[0] = (ImageView)findViewById(R.id.img1);
        images[1] = (ImageView)findViewById(R.id.img2);
        images[1].setVisibility(View.INVISIBLE);

        final Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        final Animation animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        final Animation animationFadeIn2 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        final Animation animationFadeOut2 = AnimationUtils.loadAnimation(this, R.anim.fade_out);

        findYourClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MapsActivity.class);
                startActivity(intent);
            }
        });

        Animation.AnimationListener animListener1 = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if(firsttime){
                    images[1].startAnimation(animationFadeIn2);
                    firsttime = false;
                }
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if(animation == animationFadeIn){
                    images[0].startAnimation(animationFadeOut);
                }
                else if (animation == animationFadeOut){
                    count1+=2;
                    images[0].setImageResource(img_ind[count1%(img_ind.length)]);
                    images[0].startAnimation(animationFadeIn);
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        };

        Animation.AnimationListener animListener2 = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if(animation == animationFadeIn2){
                    images[1].startAnimation(animationFadeOut2);
                }
                else if (animation == animationFadeOut2){
                    count2+=2;
                    images[1].setImageResource(img_ind[count2%(img_ind.length)]);
                    images[1].startAnimation(animationFadeIn2);
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        };

        animationFadeIn.setAnimationListener(animListener1);
        animationFadeOut.setAnimationListener(animListener1);

        animationFadeIn2.setAnimationListener(animListener2);
        animationFadeOut2.setAnimationListener(animListener2);

        images[0].setImageResource(img_ind[0]);
        images[1].setImageResource(img_ind[1]);

        images[0].startAnimation(animationFadeOut);
    }
}
