package com.example.sfu_interactive_map;

import androidx.annotation.IntRange;

import java.io.Serializable;
import java.util.List;

class Vote {
    private static List<Vote> currVotes;
    private static final int MIN_RATE = 1;
    private static final int MAX_RATE = 5;
    private String rate_type;
    private long num_votes;
    private float rating;

    public Vote(String rate_type){
        this.rate_type = rate_type;
        this.num_votes = 0;
        this.rating = 0f;
    }

    public static List<Vote> getCurrVotes() {
        return Vote.currVotes;
    }

    public String getRate_type() {
        return this.rate_type;
    }

    public long getNum_votes() {
        return this.num_votes;
    }

    public float getRating() {
        return this.rating;
    }

    public static void setCurrVotes(List<Vote> currVotes) {
        Vote.currVotes = currVotes;
    }

    public void setRate_type(String rate_type) {
        this.rate_type = rate_type;
    }

    public void setNum_votes(long num_votes) {
        this.num_votes = num_votes;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    //calculate the average rating after submitting a rating
    public void calcNewRating(@IntRange(from=MIN_RATE, to=MAX_RATE) long currRate){
        if(getNum_votes() != 0){
            float rating = getRating();
            float total_rating = rating * getNum_votes();
            setNum_votes(this.num_votes+1);
            float newRating = (total_rating + currRate) / getNum_votes();
            setRating(newRating);
        }
        else{
            setNum_votes(this.num_votes+1);
            setRating(currRate);
        }
    }
}
