package com.example.sfu_interactive_map;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    List<LatLng> latlng_points;
    List<List<LatLng>> paths;
    Button add_coord;
    Button delete_coord;
    Polygon prevPolygon = null;

    final String url = "https://at-web27.its.sfu.ca/fsgis/rest/services/Vertisee/Vertisee_BuildingFloorplan_P/MapServer/45/query?f=json&text=Academic%20Quadrangle&returnGeometry=true&geometryType=esriGeometryEnvelope&inSR=4326&outFields=bl_name%2Crm_grp%2Crm_type%2Crm_id%2COBJECTID&outSR=4326";
    final String url1 = "https://at-web27.its.sfu.ca/fsgis/rest/services/Vertisee/Vertisee_BuildingFloorplan_P/MapServer/45/query?f=json&where=objectid%3E0&returnGeometry=true&geometryType=esriGeometryMultipoint&inSR=4326&outFields=bl_name%2Crm_grp%2Crm_type%2Crm_id%2COBJECTID&outSR=4326";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        add_coord = findViewById(R.id.add_coord);
        delete_coord = findViewById(R.id.delete_coord);

        add_coord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DEBUG","add coord clicked");
                makePolygons(paths);

            }
        });
        delete_coord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DEBUG","delete coord clicked");
                latlng_points.clear();
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        RequestQueue queue = HttpRequest.getInstance(this.getApplicationContext()).getRequestQueue();
        mMap = googleMap;

        paths = new ArrayList<List<LatLng>>();

        // Add a marker in SFU and move the camera
        LatLng sfu_coord = new LatLng(49.278094, -122.919883);
        mMap.addMarker(new MarkerOptions().position(sfu_coord).title("Simon Fraser University"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sfu_coord));
        zoomToPosition(sfu_coord);

        latlng_points = new ArrayList<LatLng>();
        latlng_points.add(new LatLng(49.279697195739182,-122.91796392610519));
        latlng_points.add(new LatLng(49.279366847056522,-122.91808293581235));
        latlng_points.add(new LatLng(49.279471242080916,-122.91875996639759));
        latlng_points.add(new LatLng(49.279464185059716,-122.91876250862985));
        latlng_points.add(new LatLng(49.279481096826643,-122.91887219023108));
        latlng_points.add(new LatLng(49.279488155017511,-122.91886964710051));
        latlng_points.add(new LatLng(49.27959123707231,-122.91953819758946));
        latlng_points.add(new LatLng(49.279921587768072,-122.91941919776379));
        paths.add(latlng_points);




        makeRequest(url1, new VolleyCallback() {
            @Override
            public void onSuccessResponse(JSONObject response) {
                try {
                    //logcat apparent truncates long responses...
                    parseJsonResponse(response);
                    Log.d("size of latlng: ", "size is " + latlng_points.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        /*mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d("DEBUG","onMapClick [" + latLng.latitude + " / " + latLng.longitude + "]");
                latlng_points.add(latLng);
            }
        });*/

        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                if(prevPolygon != null && prevPolygon != polygon){
                    prevPolygon.setStrokeColor(Color.CYAN);
                }
                prevPolygon = polygon;
                polygon.setStrokeColor(Color.RED);
            }
        });
    }

    //zoom to a position in 2 seconds
    public void zoomToPosition(LatLng position){
        CameraPosition campos = new CameraPosition.Builder()
                .target(position)
                .zoom(15)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(campos), 2000, null);
    }

    //makes polygons with multiple set of paths and returns a polygon list
    public List<Polygon> makePolygons(List<List<LatLng>> paths){
        List<Polygon> plist = new ArrayList<Polygon>();
        for(List<LatLng> path : paths){
            plist.add(drawPolygon(path));
        }
        return plist;
    }

    //makes a polygon with given path and then draw it on the map
    public Polygon drawPolygon(List<LatLng> path){
        LatLng[] array_latlngpts = new LatLng[path.size()];
        path.toArray(array_latlngpts);

        Log.d("DEBUG","points size is" + latlng_points.size());
        Polygon p = mMap.addPolygon(new PolygonOptions()
                .clickable(true)
                .add(array_latlngpts)
                .strokeWidth(3)
                .fillColor(Color.parseColor("#80D3D3D3"))
                .strokeColor(Color.CYAN));
        return p;
    }

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
    public void parseJsonResponse(JSONObject response) throws JSONException{
        JSONArray features = response.getJSONArray("features");
        for(int i = 0; i<features.length(); i++){
            JSONArray rings = features.getJSONObject(i).getJSONObject("geometry").getJSONArray("rings");
            for(int j=0; j<rings.length(); j++) {
                JSONArray ring = rings.getJSONArray(j);
                List<LatLng> latlng_path = new ArrayList<LatLng>();
                for(int k =0; k<ring.length()-1; k++){
                    LatLng point = new LatLng(ring.getJSONArray(k).getDouble(1), ring.getJSONArray(k).getDouble(0));
                    latlng_path.add(point);
                }
                paths.add(latlng_path);
            }
        }
    }
}
