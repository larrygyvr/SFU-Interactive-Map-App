package com.example.sfu_interactive_map;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.shapes.Shape;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import com.android.volley.RequestQueue;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.material.navigation.NavigationView;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DrawerLayout dl;
    private Menu nav_menu;
    private NavigationView nav_view;
    private HashMap<Polygon, Object> polyObjMap;
    private Polygon prevPolygon;
    private int prevStrokeCol;
    private Object currSelectedObj;
    private Floor prevFloor;
    private final float bldvisibilityzoomlevel = 19.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        dl = (DrawerLayout)findViewById(R.id.drawer_layout);
        nav_view = (NavigationView)findViewById(R.id.navigation);
        nav_menu = nav_view.getMenu();

    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        RequestQueue queue = HttpRequest.getInstance(this.getApplicationContext()).getRequestQueue();
        mMap = googleMap;

        // Add a marker in SFU and move the camera
        LatLng sfu_coord = new LatLng(49.278094, -122.919883);
        mMap.addMarker(new MarkerOptions().position(sfu_coord).title("Simon Fraser University"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sfu_coord));
        zoomToPosition(sfu_coord, 15.5f, 2000);

        currSelectedObj = null;
        prevFloor = null;
        polyObjMap = new HashMap<Polygon, Object>();
        Resources res = getResources();
        TypedArray ta = res.obtainTypedArray(R.array.campus);
        String[][] campus_blds = new String[ta.length()][];
        final String[] colorArr = res.getStringArray(R.array.polygoncolors);

        for(int i = 0; i<ta.length(); i++){
            int id = ta.getResourceId(i, 0);
            campus_blds[i]= res.getStringArray(id);
            final Building bld = new Building(campus_blds[i][1]);
            Log.d("hasFloor:" , campus_blds[i][0]);
            if(Boolean.parseBoolean(campus_blds[i][0])){
                String baseUrl = campus_blds[i][2];
                for(int j = 3; j<campus_blds[i].length; j++) {
                    //for some reason String.format(x,y) doesn't work. Could be something with the escaping chars
                    String[] str = campus_blds[i][j].split("_");
                    String url = baseUrl.replace("%1$s", str[1]);
                    bld.addFloors(new Floor(Integer.parseInt(str[0]), url));
                    //Log.d("floor level", str[0]);
                }
            }
            Building.addBuilding(bld);
            final int finalI = i;
            makeRequest(bld.getBld_url(), new VolleyCallback() {
                @Override
                public void onSuccessResponse(JSONObject response) {
                    try {
                        //logcat will truncates long responses...
                        bld.parseBldResponse(response);
                        Log.d("building name" ,bld.getBld_name());
                        List<List<LatLng>> ring = bld.getRings();
                        StringBuilder sb = new StringBuilder(colorArr[finalI]);
                        sb.insert(1,"1A");
                        bld.setFillCol(sb.toString());
                        bld.setStrokeCol(colorArr[finalI]);
                        bld.addPolygons(mMap);
                        for(Polygon p : bld.getPolygons()){
                            polyObjMap.put(p, bld);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onErrorResponse(VolleyError error) {
                    //need to handle case for no internet
                    error.printStackTrace();
                }
            });

        }
        ta.recycle();

        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id == R.id.level1){getFloorFromBld((Building) currSelectedObj, 1);}
                else if(id == R.id.level2){getFloorFromBld((Building) currSelectedObj, 2);}
                else if(id == R.id.level3){getFloorFromBld((Building) currSelectedObj, 3);}
                else if(id == R.id.level4){getFloorFromBld((Building) currSelectedObj, 4);}
                else if(id == R.id.level5){getFloorFromBld((Building) currSelectedObj, 5);}
                else if(id == R.id.level6){getFloorFromBld((Building) currSelectedObj, 6);}
                else if(id == R.id.level7){getFloorFromBld((Building) currSelectedObj, 7);}
                else if(id == R.id.level8){getFloorFromBld((Building) currSelectedObj, 8);}
                return true;
            }
        });

        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                if(prevPolygon != null && prevPolygon != polygon){
                    prevPolygon.setStrokeColor(prevStrokeCol);
                    prevPolygon.setStrokeWidth(3);
                }
                prevPolygon = polygon;
                prevStrokeCol = prevPolygon.getStrokeColor();
                polygon.setStrokeColor(Color.GREEN);
                polygon.setStrokeWidth(7);
                zoomToPolygon(polygon, 200, 1000);
                dl.openDrawer(Gravity.LEFT);
                currSelectedObj = polyObjMap.get(polygon);
                setMenuItem(nav_menu, currSelectedObj);
            }
        });

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                Log.d("zoomlevel is", ""+mMap.getCameraPosition().zoom);
                List<Building> blds = Building.getBuildings();
                for(Building bld :blds){
                    List<Polygon> bldPolygons = bld.getPolygons();
                    for(Polygon p : bldPolygons){
                        if(mMap.getCameraPosition().zoom > bldvisibilityzoomlevel){
                            p.setClickable(false);
                            p.setVisible(false);
                        }
                        else{
                            Log.d("hit here", ""+mMap.getCameraPosition().zoom);
                            p.setClickable(true);
                            p.setVisible(true);
                        }
                    }
                }
            }
        });
    }

    private void getFloorFromBld(final Building bld, int level){
        final Floor flr  = bld.getFloors().get(level);
        if(flr != null ){
            if(flr.getRooms().isEmpty()){
                makeRequest(flr.getFlr_url(), new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(JSONObject response) throws JSONException {
                        flr.parseFloorResponse(response);
                        flr.addFloorPolygons(bld.getFillCol(),bld.getStrokeCol(), mMap);
                        //flr.showFloor();
                        Log.d("floor size", ""+flr.getRooms().size());
                        List<Room> rooms = flr.getRooms();
                        for(Room room : rooms){
                            polyObjMap.put(room.getPolygon(), room);
                            Log.d("room id is", room.getRm_id());
                        }
                        dl.closeDrawer(Gravity.LEFT);
                        if(prevFloor != null && prevFloor != flr){
                            prevFloor.hideFloor();
                        }
                        prevFloor = flr;
                        zoomToPolygon(prevPolygon, 200, 1000);
                    }
                    @Override
                    public void onErrorResponse(VolleyError error) throws Exception {
                        //need to handle case for no internet
                        error.printStackTrace();
                    }
                });
            }
            else{
                //case where floor already exist
                if(prevFloor != null && prevFloor != flr){
                    prevFloor.hideFloor();
                }
                prevFloor = flr;
                flr.showFloor();
            }

        }
        else{
            //handle no such floor
        }
    }

    private void setMenuItem(Menu menu, Object obj){
        MenuItem bld_name = menu.findItem(R.id.Building);
        MenuItem abbr = menu.findItem(R.id.Abbr);
        MenuItem bld_code = menu.findItem(R.id.Bld_code);
        MenuItem show_floors = menu.findItem(R.id.Show_floors);
        MenuItem room_class = menu.findItem(R.id.Room_class);
        MenuItem room_name = menu.findItem(R.id.Room);
        MenuItem room_id = menu.findItem(R.id.Room_id);
        MenuItem navigate = menu.findItem(R.id.Navigate);

        if(obj instanceof  Building) { //bad practice to use instanceof can be avoided with polymorphism
            bld_name.setVisible(true);
            abbr.setVisible(true);
            bld_code.setVisible(true);
            show_floors.setVisible(true);
            room_class.setVisible(false);
            room_name.setVisible(false);
            room_id.setVisible(false);
            navigate.setVisible(true);
            bld_name.setTitle(String.format("Building Name: %s", ((Building) obj).getBld_name()));
            abbr.setTitle(String.format("Abbr: %s", ((Building) obj).getAbbr()));
            bld_code.setTitle(String.format("Building Code: %s", ((Building) obj).getBld_code()));
            show_floors.setTitle("Floors");
            SubMenu subMenu = show_floors.getSubMenu();
            for (int i = 0; i < subMenu.size(); i++) {
                if (((Building) obj).getFloors().get(i + 1) == null) {
                    subMenu.getItem(i).setVisible(false);
                } else {
                    subMenu.getItem(i).setVisible(true);
                }
            }
        }
        else if(obj instanceof Room){ //bad practice to use instanceof but just use for now
            Log.d("clicked on room polygon", ((Room)obj).getRm_id());
            bld_name.setVisible(true);
            abbr.setVisible(false);
            bld_code.setVisible(false);
            show_floors.setVisible(false);
            room_class.setVisible(true);
            room_name.setVisible(true);
            room_id.setVisible(true);
            navigate.setVisible(true);
            bld_name.setTitle(String.format("Building Name: %s",((Room) obj).getBld_name()));
            room_class.setTitle(String.format("Room Class: %s",((Room) obj).getRm_grp()));
            room_name.setTitle(String.format("Room Name: %s",((Room) obj).getRm_type()));
            room_id.setTitle(String.format("Room ID: %s",((Room) obj).getRm_id()));
        }
    }

    private void zoomToPolygon(final Polygon p, final int padding, final int transms) {
        final LatLngBounds.Builder centerBuilder = LatLngBounds.builder();
        for (LatLng point : p.getPoints()) {
            centerBuilder.include(point);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(centerBuilder.build(), padding), transms, null);
    }

    public void zoomToPosition(LatLng position, final float zoom, final int transms){
        CameraPosition campos = new CameraPosition.Builder()
                .target(position)
                .zoom(zoom)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(campos), transms, null);
    }

    //makes a polygon with given path and then draw it on the map
    /*public Polygon addPolygonToMap(PolygonOptions polyopt){
        Polygon p = mMap.addPolygon(polyopt);
        return p;
    }*/

    public interface  VolleyCallback{
        void onSuccessResponse(JSONObject response) throws JSONException;
        void onErrorResponse(VolleyError error) throws Exception;
    }

    public void makeRequest(String url, final VolleyCallback callback){
        JsonObjectRequest jsobjreq = new  JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Json response",response.toString());
                try {
                    callback.onSuccessResponse(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Response error", "Check internet conn");
                try {
                    callback.onErrorResponse(error);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        HttpRequest.getInstance(this.getApplicationContext()).addToRequestQueue(jsobjreq);
    }

}
