package com.example.sfu_interactive_map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.media.Image;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.LoginFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.material.navigation.NavigationView;
import com.google.protobuf.Internal;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.Inflater;

import dagger.MapKey;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private boolean mFirstLocationUpdate;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_CODE_PICK_PHOTO = 2;
    private boolean mLocationPermissionGranted;
    private boolean mGalleryPermissionGranted;
    private GoogleMap mMap;
    private Marker currMarker;
    private DrawerLayout dl;
    private Menu nav_menu;
    private NavigationView nav_view;
    private HashMap<Polygon, Object> polyObjMap;
    private Polygon prevPolygon;
    private int prevStrokeCol;
    private Object currSelectedObj;
    private Floor prevFloor;
    private final float bldvisibilityzoomlevel = 19.0f;
    private int numRequests = 0;
    private Bitmap currBitmap;
    private ImageButton searchBtn;
    private ImageView marker;
    private ImageView addImage;
    private RelativeLayout relayout;
    private FrameLayout flayout;

    private final String[] markerDialog = {
            "Update Image", "Remove Marker"
    };

    private final String[] infoWindowDialog = {
            "Update Title", "Update Description"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("onCreate state hit","device on create");
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        dl = (DrawerLayout) findViewById(R.id.drawer_layout);
        nav_view = (NavigationView) findViewById(R.id.navigation);
        nav_menu = nav_view.getMenu();
        searchBtn = (ImageButton) findViewById(R.id.searchBtn);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setFastestInterval(5 * 1000); // 10 seconds update
        mLocationPermissionGranted = false;
        mGalleryPermissionGranted = false;
        mFirstLocationUpdate = true;
        marker = (ImageView)findViewById(R.id.markerImage);
        addImage = (ImageView)findViewById(R.id.embeddedImage);
        flayout = (FrameLayout) findViewById(R.id.marker);
        currBitmap = null;
        currMarker = null;

    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirstLocationUpdate = true;
        Log.d("Sleep state hit","device on Pause");
        if (mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy state hit","device on destory");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("onStop state hit","device on stop");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume state hit","device on resume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("onRestart state hit","device on restart");
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
        TypedArray bgimageArr = res.obtainTypedArray(R.array.buildingdimages);
        TypedArray ta = res.obtainTypedArray(R.array.campus);
        String[][] campus_blds = new String[ta.length()][];
        final String[] colorArr = res.getStringArray(R.array.polygoncolors);
        for (int i = 0; i < ta.length(); i++) {
            int id = ta.getResourceId(i, 0);
            int bgimageID = bgimageArr.getResourceId(i, 0);
            campus_blds[i] = res.getStringArray(id);
            final Building bld = new Building(campus_blds[i][1],bgimageID);
            Log.d("hasFloor:", campus_blds[i][0]);
            if (Boolean.parseBoolean(campus_blds[i][0])) {
                String baseUrl = campus_blds[i][2];
                for (int j = 3; j < campus_blds[i].length; j++) {
                    //for some reason String.format(x,y) doesn't work. Could be something with the escaping chars
                    String[] str = campus_blds[i][j].split("_");
                    String url = baseUrl.replace("%1$s", str[1]);
                    bld.addFloors(new Floor(Integer.parseInt(str[0]), url, bld.getBackgroundImageID()));
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
                        Log.d("building name", bld.getBld_name());
                        List<List<LatLng>> ring = bld.getRings();
                        StringBuilder sb = new StringBuilder(colorArr[finalI]);
                        sb.insert(1, "1A");
                        bld.setFillCol(sb.toString());
                        bld.setStrokeCol(colorArr[finalI]);
                        bld.addPolygons(mMap);
                        for (Polygon p : bld.getPolygons()) {
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
        bgimageArr.recycle();
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Log.d("onMarkerDragStart", "starting to move marker");
                currMarker = marker;
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                currMarker = marker;
                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Marker Options");
                builder.setItems(markerDialog, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if ("Update Image".equals(markerDialog[which])) {
                            Toast.makeText(MapsActivity.this, "Update Image", Toast.LENGTH_SHORT).show();
                            getGalleryPermission();
                            Log.d("in Marker Clicker", "permission granted");

                        } else if ("Remove Marker".equals(markerDialog[which])) {
                            marker.remove();
                            Toast.makeText(MapsActivity.this, "Removed Marker", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                alert.getWindow().setLayout(650,530);
                return false;
            }
        });

        //this enables display of multi line text in marker InFoWindow
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                Context context = getApplicationContext();
                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);
                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());
                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());
                info.addView(title);
                info.addView(snippet);
                return info;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                currMarker = marker;
                final int layoutWidthChild = 500;
                final int layoutHeighChild = 100;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(layoutWidthChild,layoutHeighChild);
                layoutParams.gravity = Gravity.CENTER;
                layoutParams.setMargins(10,20,10,20);
                LinearLayout linearLayout = new LinearLayout(MapsActivity.this);
                final EditText editText1 = (EditText) getLayoutInflater().inflate(R.layout.edit_text1, null);
                final EditText editText2 = (EditText) getLayoutInflater().inflate(R.layout.edit_text2, null);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                linearLayout.addView(editText1,layoutParams);
                linearLayout.addView(editText2,layoutParams);
                final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setTitle("Customize Notes");
                builder.setView(linearLayout);
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(editText1.getText().length() != 0 && editText2.getText().length() != 0){
                            currMarker.setTitle(editText1.getText().toString());
                            currMarker.setSnippet(editText2.getText().toString());
                        }else{
                            Toast.makeText(MapsActivity.this, "Cannot Leave Empty Fields", Toast.LENGTH_SHORT).show();
                        }
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                alert.getWindow().setLayout(
                        (int)Math.floor(layoutWidthChild*1.5),
                        (int)Math.floor(layoutHeighChild*6.5)
                );
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                addImage.setBackground(getResources().getDrawable(R.drawable.add_image));
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Title")
                        .snippet("Short Description")
                        .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(flayout)))
                        .draggable(true)
                );
            }
        });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                if (!locationResult.getLocations().isEmpty()) {
                    Log.d("in location results" , "updating location");
                    if(mFirstLocationUpdate){
                        Location location = locationResult.getLastLocation();
                        LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                        LatLngBounds latLngBounds = getPolygonBounds(prevPolygon.getPoints());
                        List<LatLng> fitPoints = new ArrayList<>();
                        fitPoints.add(ll);
                        fitPoints.add(latLngBounds.getCenter());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(getPolygonBounds(fitPoints), 200), 1000, null);
                        mFirstLocationUpdate = false;
                    }
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if(!locationAvailability.isLocationAvailable()){
                    Log.d("onLocationAvailability", "Location Service Disabled");
                    if (mFusedLocationProviderClient != null) {
                        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                        mFirstLocationUpdate = true;
                    }
                }

            }
        };

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPrimSearchDialog();
            }
        });

        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                if (id == R.id.level1) {
                    getFloorFromBld((Building) currSelectedObj, 1);
                } else if (id == R.id.level2) {
                    getFloorFromBld((Building) currSelectedObj, 2);
                } else if (id == R.id.level3) {
                    getFloorFromBld((Building) currSelectedObj, 3);
                } else if (id == R.id.level4) {
                    getFloorFromBld((Building) currSelectedObj, 4);
                } else if (id == R.id.level5) {
                    getFloorFromBld((Building) currSelectedObj, 5);
                } else if (id == R.id.level6) {
                    getFloorFromBld((Building) currSelectedObj, 6);
                } else if (id == R.id.level7) {
                    getFloorFromBld((Building) currSelectedObj, 7);
                } else if (id == R.id.level8) {
                    getFloorFromBld((Building) currSelectedObj, 8);
                } else if (id == R.id.Navigate) {
                    getLocationPermission();
                }
                return true;
            }
        });

        mMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(Polygon polygon) {
                switchPolygon(polygon);
                LatLngBounds llb = getPolygonBounds(polygon.getPoints());
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(llb, 200), 1000, null);
                if (!dl.isDrawerOpen(Gravity.LEFT))
                    dl.openDrawer(Gravity.LEFT);
                currSelectedObj = polyObjMap.get(polygon);
                setMenuItem(nav_menu, currSelectedObj);

            }
        });

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                if (mMap.getCameraPosition().zoom > bldvisibilityzoomlevel) {
                    if (Building.getIsVisClick()) {
                        Building.setIsVisClick(false);
                        Building.allBuildingsInteract(Building.getIsVisClick());
                    }
                } else {
                    if (!Building.getIsVisClick()) {
                        Building.setIsVisClick(true);
                        Building.allBuildingsInteract(Building.getIsVisClick());
                    }
                }
            }
        });
    }

    private void switchPolygon(Polygon polygon) {
        if (prevPolygon != null && prevPolygon != polygon) {
            prevPolygon.setStrokeColor(prevStrokeCol);
            prevPolygon.setStrokeWidth(3);
        }
        prevPolygon = polygon;
        prevStrokeCol = prevPolygon.getStrokeColor();
        polygon.setStrokeColor(Color.GREEN);
        polygon.setStrokeWidth(7);
    }

    private void getFloorFromBld(final Building bld, int level) {
        final Floor flr = bld.getFloorsSparseArr().get(level);
        if (flr != null) {
            if (flr.getRooms().isEmpty()) {
                makeRequest(flr.getFlr_url(), new VolleyCallback() {
                    @Override
                    public void onSuccessResponse(JSONObject response) throws JSONException {
                        flr.parseFloorResponse(response);
                        flr.addFloorPolygons(bld.getFillCol(), bld.getStrokeCol(), mMap, true);
                        List<Room> rooms = flr.getRooms();
                        for (Room room : rooms) {
                            polyObjMap.put(room.getPolygon(), room);
                        }
                        if (prevFloor != null && prevFloor != flr) {
                            prevFloor.hideFloor();
                        }
                        dl.closeDrawer(Gravity.LEFT);
                        prevFloor = flr;
                        LatLngBounds llb = getPolygonBounds(prevPolygon.getPoints());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(llb, 200), 1000, null);
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) throws Exception {
                        //need to handle case for no internet
                        error.printStackTrace();
                    }
                });
            } else {
                //case where floor already exist
                if (prevFloor != null && prevFloor != flr) {
                    prevFloor.hideFloor();
                }
                prevFloor = flr;
                flr.showFloor();
            }
        } else {
            //handle no such floor
        }
    }

    private void setMenuItem(Menu menu, Object obj) {
        MenuItem bld_name = menu.findItem(R.id.Building);
        MenuItem abbr = menu.findItem(R.id.Abbr);
        MenuItem bld_code = menu.findItem(R.id.Bld_code);
        MenuItem show_floors = menu.findItem(R.id.Show_floors);
        MenuItem room_class = menu.findItem(R.id.Room_class);
        MenuItem room_name = menu.findItem(R.id.Room);
        MenuItem room_id = menu.findItem(R.id.Room_id);
        MenuItem navigate = menu.findItem(R.id.Navigate);
        View headerView = nav_view.getHeaderView(0);
        Drawable drawable;
        if (obj instanceof Building) { //bad practice to use instanceof can be avoided with polymorphism
            bld_name.setVisible(true);
            abbr.setVisible(true);
            bld_code.setVisible(true);
            show_floors.setVisible(true);
            room_class.setVisible(false);
            room_name.setVisible(false);
            room_id.setVisible(false);
            navigate.setVisible(true);
            drawable = ContextCompat.getDrawable(this, ((Building)obj).getBackgroundImageID());
            headerView.setBackground(drawable);
            bld_name.setTitle(String.format("Building Name: %s", ((Building) obj).getBld_name()));
            abbr.setTitle(String.format("Abbr: %s", ((Building) obj).getAbbr()));
            bld_code.setTitle(String.format("Building Code: %s", ((Building) obj).getBld_code()));
            show_floors.setTitle("Floors");
            SubMenu subMenu = show_floors.getSubMenu();
            for (int i = 0; i < subMenu.size(); i++) {
                if (((Building) obj).getFloorsSparseArr().get(i + 1) == null) {
                    subMenu.getItem(i).setVisible(false);
                } else {
                    subMenu.getItem(i).setVisible(true);
                }
            }
        } else if (obj instanceof Room) { //bad practice to use instanceof but just use for now
            Log.d("clicked on room polygon", ((Room) obj).getRm_id());
            bld_name.setVisible(true);
            abbr.setVisible(false);
            bld_code.setVisible(false);
            show_floors.setVisible(false);
            room_class.setVisible(true);
            room_name.setVisible(true);
            room_id.setVisible(true);
            navigate.setVisible(true);
            bld_name.setTitle(String.format("Building Name: %s", ((Room) obj).getBld_name()));
            room_class.setTitle(String.format("Room Class: %s", ((Room) obj).getRm_grp()));
            room_name.setTitle(String.format("Room Name: %s", ((Room) obj).getRm_type()));
            room_id.setTitle(String.format("Room ID: %s", ((Room) obj).getRm_id()));
        }
    }

    private LatLngBounds getPolygonBounds(final List<LatLng> points) {
        final LatLngBounds.Builder centerBuilder = LatLngBounds.builder();
        for (LatLng point : points) {
            centerBuilder.include(point);
        }
        return centerBuilder.build();
    }

    public void zoomToPosition(LatLng position, final float zoom, final int transms) {
        CameraPosition campos = new CameraPosition.Builder()
                .target(position)
                .zoom(zoom)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(campos), transms, null);
    }

    public interface VolleyCallback {
        void onSuccessResponse(JSONObject response) throws JSONException;

        void onErrorResponse(VolleyError error) throws Exception;
    }

    public void makeRequest(String url, final VolleyCallback callback) {
        JsonObjectRequest jsobjreq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Json response", response.toString());
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

    public void startPrimSearchDialog() {
        ArrayList<Building> blds = new ArrayList<Building>(Building.getBuildings());
        new SearchDialogCompat<>(this, "Search for Room in Building...", "Academic Quadrangle", null,
                blds, new SearchResultListener<Building>() {
            @Override
            public void onSelected(BaseSearchDialogCompat dialog, Building item, int position) {
                switchPolygon(item.getPolygons().get(0));
                currSelectedObj = item;
                setMenuItem(nav_menu, currSelectedObj);
                if (!dl.isDrawerOpen(Gravity.LEFT))
                    dl.openDrawer(Gravity.LEFT);
                LatLngBounds llb = getPolygonBounds(item.getPolygons().get(0).getPoints());
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(llb, 200), 1000, null);
                dialog.dismiss();
                startSecSearchDialog(item);

            }
        }).show();
    }

    public void startSecSearchDialog(final Building bld) {
        final ArrayList<Room> rooms = new ArrayList<>();
        numRequests = bld.getFloorsList().size();
        if (!bld.getFloorsList().isEmpty()) {
            for (final Floor floor : bld.getFloorsList()) {
                if (floor.getRooms().isEmpty()) {
                    makeRequest(floor.getFlr_url(), new VolleyCallback() {
                        @Override
                        public void onSuccessResponse(JSONObject response) throws JSONException {
                            floor.parseFloorResponse(response);
                            floor.addFloorPolygons(bld.getFillCol(), bld.getStrokeCol(), mMap, false);
                            rooms.addAll(floor.getRooms());
                            for (Room rm : floor.getRooms())
                                polyObjMap.put(rm.getPolygon(), rm);
                            if (--numRequests == 0)
                                showSecSearchResults(rooms);
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) throws Exception {
                            error.printStackTrace();
                            --numRequests;
                        }
                    });
                } else {
                    rooms.addAll(floor.getRooms());
                    if (--numRequests == 0)
                        showSecSearchResults(rooms);
                }
            }
        } else {
            Toast.makeText(this, "No floor plan for this building", Toast.LENGTH_SHORT).show();
        }

    }

    public void showSecSearchResults(ArrayList<Room> rooms) {
        new SearchDialogCompat<>(this, "Search for Room ID/Num...", "Room #3181", null,
                rooms, new SearchResultListener<Room>() {
            @Override
            public void onSelected(BaseSearchDialogCompat dialog, Room item, int position) {
                switchPolygon(item.getPolygon());
                if (prevFloor != null && prevFloor != item.getFloor())
                    prevFloor.hideFloor();
                prevFloor = item.getFloor();
                prevFloor.showFloor();
                currSelectedObj = item;
                setMenuItem(nav_menu, currSelectedObj);
                if (!dl.isDrawerOpen(Gravity.LEFT))
                    dl.openDrawer(Gravity.LEFT);
                LatLngBounds llb = getPolygonBounds(item.getPolygon().getPoints());
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(llb, 200), 1000, null);
                dialog.dismiss();
            }
        }).show();
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        mFirstLocationUpdate = true;
    }

    private void getGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            mGalleryPermissionGranted = true;
            getImageFromGallery(currMarker);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CODE_PICK_PHOTO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION){
                mLocationPermissionGranted = true;
                getDeviceLocation();
            }
            else if (requestCode == PERMISSIONS_REQUEST_CODE_PICK_PHOTO){
                Toast.makeText(this, "lol", Toast.LENGTH_SHORT).show();
                mGalleryPermissionGranted = true;
                getImageFromGallery(currMarker);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode != RESULT_CANCELED){
            if (requestCode == PERMISSIONS_REQUEST_CODE_PICK_PHOTO) {
                if(data == null)
                    return;
                Toast.makeText(this, "Selecting image", Toast.LENGTH_SHORT).show();
                //TO DO: handle if data == null
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                if (selectedImage != null) {
                    Cursor cursor = getContentResolver().query(
                            selectedImage,
                            filePathColumn,
                            null,
                            null,
                            null
                    );
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        String picturePath = cursor.getString(columnIndex);
                        int ori = getExifOrientation(picturePath);
                        currBitmap = BitmapFactory.decodeFile(picturePath);
                        Matrix mat = new Matrix();
                        mat.postRotate(ori);
                        currBitmap = Bitmap.createBitmap(
                                currBitmap,
                                0,
                                0,
                                currBitmap.getWidth(),
                                currBitmap.getHeight(),
                                mat,
                                true
                        );
                        currBitmap = Bitmap.createScaledBitmap(currBitmap, 1000, 1000,true);
                        cursor.close();
                        BitmapDrawable bd = new BitmapDrawable(getResources(), currBitmap);
                        addImage.setBackground(bd);
                        currMarker.setIcon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(flayout)));
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        if(!mLocationPermissionGranted)
            return;
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        if(!mMap.isMyLocationEnabled())
            mMap.setMyLocationEnabled(true);
    }

    public void getImageFromGallery(Marker marker){
        if(!mGalleryPermissionGranted)
            return;
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, PERMISSIONS_REQUEST_CODE_PICK_PHOTO);
    }

    public static Bitmap createDrawableFromView(View view ) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(),view.getHeight() ,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null){
            bgDrawable.draw(canvas);

        }
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
            }
        }
        return degree;
    }
}