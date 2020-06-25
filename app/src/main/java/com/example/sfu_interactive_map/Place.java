package com.example.sfu_interactive_map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Place {
    private static List<Place> places;
    private String name;
    private String description;
    private int photo;
    private String contact;
    private String address;
    private List<String> hours;
    private List<Vote> voteTypes;

    public Place(
            String name,
            String description,
            int photo,
            String contact,
            String address
    ){
        this.name = name;
        this.description = description;
        this.photo = photo;
        this.contact = contact;
        this.address = address;
        this.hours = new ArrayList<String>();
        this.voteTypes = new ArrayList<Vote>();
    }

    //get methods
    public static List<Place> getPlaces(){
        return Place.places;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public int getPhoto() {
        return this.photo;
    }

    public String getContact() {
        return this.contact;
    }

    public String getAddress() {
        return this.address;
    }

    public List<String> getHours() {
        return this.hours;
    }

    public List<Vote> getVoteTypes() {
        return this.voteTypes;
    }

    //set methods
    public static void addPlace(Place place){
        if(Place.places == null){
            Place.places = new ArrayList<Place>();
        }
        Place.places.add(place);
    }
    

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setHours(String ... hours) {
        this.hours.addAll(Arrays.asList(hours));
    }

    public void addVoteType(Vote voteType){
        this.voteTypes.add(voteType);
    }

    public void setVoteTypes(String ... rate_types) {
        for(String rate_type : rate_types)
            addVoteType(new Vote(rate_type));
    }
}
