package com.example.sfu_interactive_map;


import android.graphics.Color;
import android.os.Build;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.mirrajabi.searchdialog.core.Searchable;

public class Building implements Searchable {

    //is visible and clickable
    private static boolean isVisClick;
    //list of all buildings
    private static List<Building> buildings;
    //Full name of building
    private String bld_name;
    //Abbreviation of building name
    private String abbr;
    //The building code (may not need to be used)
    private String bld_code;
    //Building description
    private String bld_descr;
    //all floors belonging to this bld sparse array
    private SparseArray<Floor> floorsSparseArr;
    //all floors belonging to this bld list
    private List<Floor> floorsList;
    //http request for overview building
    private String bld_url;
    //rings that belong to this building
    private List<List<LatLng>> rings;
    //polygonOptionals that belong to this building
    private List<Polygon> polygons;
    //polygon fillColor
    private String fillCol;
    //polygon strokeColor
    private String strokeCol;

    public Building(String url){
        this.bld_name = "";
        this.abbr = "";
        this.bld_code = "";
        this.bld_descr = "";
        this.floorsSparseArr =  new SparseArray<Floor>();
        this.floorsList = new ArrayList<Floor>();
        this.bld_url = url;
        this.rings = new ArrayList<List<LatLng>>();
        this.polygons = new ArrayList<Polygon>();
        this.fillCol = "";
        this.strokeCol = "";
    }

    //get methods
    public static boolean getIsVisClick(){return Building.isVisClick;}
    public String getBld_name(){
        return this.bld_name;
    }
    public String getAbbr(){
        return this.abbr;
    }
    public String getBld_code(){
        return this.bld_code;
    }
    public String getBld_descr(){
        return this.bld_descr;
    }
    public SparseArray<Floor> getFloorsSparseArr(){
        return this.floorsSparseArr;
    }
    public List<Floor> getFloorsList(){return this.floorsList;}
    public String getBld_url(){
        return this.bld_url;
    }
    public List<List<LatLng>>getRings(){
        return this.rings;
    }
    public List<Polygon> getPolygons(){
        return this.polygons;
    }
    public String getFillCol() {
        return fillCol;
    }
    public String getStrokeCol(){
        return this.strokeCol;
    }

    //set methods
    public static void setIsVisClick(boolean isVisClick){
        Building.isVisClick = isVisClick;
    }

    public void setBld_name(String bld_name){
        this.bld_name = bld_name;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }

    public void setBld_code(String bld_code) {
        this.bld_code = bld_code;
    }

    public void setBld_descr(String bld_descr) {
        this.bld_descr = bld_descr;
    }

    public void addFloors(Floor floor) {
        if(this.floorsSparseArr != null){
            this.floorsSparseArr.append(floor.getLevel(), floor);
            this.floorsList.add(floor);
        }
    }


    public void addRing(List<LatLng> path) {
        this.rings.add(path);
    }

    public static List<Building> getBuildings(){
        return Building.buildings;
    }
    public static void addBuilding(Building building){
        if(Building.buildings == null){
            Building.buildings = new ArrayList<Building>();
            Building.setIsVisClick(true);
        }
        Building.buildings.add(building);
    }

    public void addPolygons(GoogleMap mMap){
        if(!this.rings.isEmpty()){
            PolygonOptions primPolyOpt = new PolygonOptions()
                    .visible(true)
                    .addAll(rings.get(0))
                    .clickable(true)
                    .strokeWidth(3)
                    .fillColor(Color.parseColor(getFillCol()))
                    .strokeColor(Color.parseColor(getStrokeCol()));
            if(rings.size() > 1){
                if(this.abbr.equals("CSTN")){
                    for(int i = 1; i<rings.size(); i++){
                        PolygonOptions secPolyOpt = new PolygonOptions()
                                .visible(true)
                                .addAll(rings.get(i))
                                .clickable(true)
                                .strokeWidth(3)
                                .fillColor(Color.parseColor(getFillCol()))
                                .strokeColor(Color.parseColor(getStrokeCol()));
                        this.polygons.add(mMap.addPolygon(secPolyOpt));
                    }
                }else{
                    for(int i = 1; i<rings.size(); i++)
                        primPolyOpt.addHole(rings.get(i));
                }
            }
            this.polygons.add(mMap.addPolygon(primPolyOpt));
        }
    }

    public void setFillCol(String fillCol) {
        this.fillCol = fillCol;
    }

    public void setStrokeCol(String strokeCol) {
        this.strokeCol = strokeCol;
    }

    //methods
    public static void allBuildingsInteract(boolean isVisClick){
        List<Building> blds = Building.getBuildings();
        if(blds!= null && !blds.isEmpty()){
            for(Building bld : blds){
                List<Polygon> ps = bld.getPolygons();
                for(Polygon p : ps){
                    p.setVisible(isVisClick);
                    p.setClickable(isVisClick);
                }
            }
        }
    }

    public void parseBldResponse(JSONObject response) throws JSONException {
        JSONArray features = response.getJSONArray("features");
        JSONObject attrib =  features.getJSONObject(0).getJSONObject("attributes");
        setBld_name(attrib.getString("Building"));
        setAbbr(attrib.getString("Abbr"));
        setBld_code(attrib.getString("bl_id"));
        for(int i = 0; i<features.length(); i++){
            JSONArray rings = features.getJSONObject(i).getJSONObject("geometry").getJSONArray("rings");
            for(int j=0; j<rings.length(); j++) {
                JSONArray ring = rings.getJSONArray(j);
                List<LatLng> latlng_path = new ArrayList<LatLng>();
                for(int k =0; k<ring.length()-1; k++){
                    LatLng point = new LatLng(ring.getJSONArray(k).getDouble(1), ring.getJSONArray(k).getDouble(0));
                    latlng_path.add(point);
                }
                addRing(latlng_path);
            }
        }
    }

    @Override
    public String getTitle() {
        return getBld_name();
    }

}
