package com.example.sfu_interactive_map;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ir.mirrajabi.searchdialog.core.Searchable;

public class Room implements Searchable {
    //which group does room belong to
    private String bld_name;
    private String rm_grp;
    private String rm_type;
    private String rm_id;
    private List<LatLng> ring;
    private Polygon polygon;
    private Floor belongToFloor;

    Room(){
        this.bld_name = "";
        this.rm_grp = "";
        this.rm_type = "";
        this.rm_id = "";
        this.ring = new ArrayList<LatLng>();
        this.belongToFloor = null;
    }

    //get method
    public String getBld_name(){
        return this.bld_name;
    }

    public String getRm_grp(){
        return this.rm_grp;
    }

    public String getRm_type() {
        return this.rm_type;
    }

    public String getRm_id() {
        return this.rm_id;
    }

    public List<LatLng> getRing(){
        return this.ring;
    }
    public Polygon getPolygon() {
        return this.polygon;
    }
    public Floor getFloor(){
        return this.belongToFloor;
    }

    //set method
    public void setBld_name(String bl_name) {
        this.bld_name = bl_name;
    }

    public void setRm_grp(String rm_grp) {
        this.rm_grp = rm_grp;
    }

    public void setRm_type(String rm_type) {
        this.rm_type = rm_type;
    }

    public void setRm_id(String rm_id) {
        this.rm_id = rm_id;
    }

    public void addPoint(LatLng point){
        this.ring.add(point);
    }

    public void addPolygon(String fillCol, String strokeCol, GoogleMap mMap, boolean isVisClick){
        if(this.ring != null){
             this.polygon = mMap.addPolygon(new PolygonOptions()
                    .visible(isVisClick)
                    .addAll(ring)
                    .clickable(isVisClick)
                    .strokeWidth(3)
                    .fillColor(Color.parseColor(fillCol))
                    .strokeColor(Color.parseColor(strokeCol)));
        }
    }

    public void setFloor(Floor floor){
        this.belongToFloor = floor;
    }

    @Override
    public String getTitle() {
        return getRm_id();
    }
}
