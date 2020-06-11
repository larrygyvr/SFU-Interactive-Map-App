package com.example.sfu_interactive_map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ir.mirrajabi.searchdialog.core.Searchable;

public class Floor {
    //level
    private int level;
    //rooms that belong to this floor
    private List<Room> rooms;
    //floor url
    private String flr_url;

    Floor(int level, String flr_url)
    {
        this.level = level;
        rooms = new ArrayList<Room>();
        this.flr_url = flr_url;
    }

    //get method
    public int getLevel(){
        return this.level;
    }

    public List<Room> getRooms(){
        return this.rooms;
    }

    public String getFlr_url(){
        return this.flr_url;
    }

    //set method
    public void setLevel(int level){
        this.level = level;
    }

    public void addRoom(Room room){
        this.rooms.add(room);
    }

    //methods
    public void showFloor(){
        if(this.rooms != null){
            for(Room room : this.rooms){
                room.getPolygon().setVisible(true);
                room.getPolygon().setClickable(true);
            }
        }
    }

    public void hideFloor(){
        if(this.rooms != null){
            for(Room room : this.rooms){
                room.getPolygon().setVisible(false);
                room.getPolygon().setClickable(false);
            }
        }
    }
    public void parseFloorResponse(JSONObject response) throws JSONException {
        JSONArray features = response.getJSONArray("features");
        for(int i = 0; i<features.length(); i++){
            Room room = new Room();
            JSONObject attrib = features.getJSONObject(i).getJSONObject("attributes");
            room.setBld_name(attrib.getString("bl_name"));
            room.setRm_grp(attrib.getString("rm_grp"));
            room.setRm_type(attrib.getString("rm_type"));
            room.setRm_id(attrib.getString("rm_id"));
            JSONArray rings = features.getJSONObject(i).getJSONObject("geometry").getJSONArray("rings");
            for(int j=0; j<rings.length(); j++) {
                JSONArray ring = rings.getJSONArray(j);
                for(int k =0; k<ring.length()-1; k++){
                    LatLng point = new LatLng(ring.getJSONArray(k).getDouble(1), ring.getJSONArray(k).getDouble(0));
                    room.addPoint(point);
                }
            }
            room.setFloor(this);
            addRoom(room);
        }

    }

    public void addFloorPolygons(String fillCol, String strokeCol, GoogleMap mMap, boolean isVisClick){
        for(Room room : this.rooms){
            room.addPolygon(fillCol, strokeCol, mMap, isVisClick);
        }
    }
}
