package com.jpwolfso.geofencer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jpwolfso.geofencer.database.Beacons;
import com.jpwolfso.geofencer.database.GeoDatabase;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import static android.content.Context.LOCATION_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    MapView mapview;
    LocationManager locationManager;
    LocationProvider locationProvider;
    Location location;
    TextView textviewradius; // label that displays radius of geofence being created
    LinearLayout beaconconfig; // layout for creating a geofence on longpress on map
    SeekBar seekBar; // seekbar to choose geofence radius
    Button savemybeacon; // button to create and save the geofence
    Button cancelmybeacon;
    ArrayList<OverlayItem> personOverlayList;
    OverlayItem personIcon; // overlay for meeee
    ItemizedOverlayWithFocus<OverlayItem> personOverlay;
    GeoPoint geoPoint;
    Polygon circle; // geofence circle
    GeoPoint cpoint; // center of geofence
    Double crad; // radius of geofence
    Beacons beacons;
    MapEventsOverlay mapEventsOverlay;
    Boolean lazyfix;
    MapEventsReceiver mapEventsReceiver = new MapEventsReceiver() {
        @Override
        public boolean singleTapConfirmedHelper(GeoPoint p) {
            return false;
        }

        @Override
        public boolean longPressHelper(final GeoPoint p) {

            if (lazyfix) {
                generateCircle(mapview, p, ((double) seekBar.getProgress()));
                beaconconfig.setVisibility(View.VISIBLE);
            }

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    lazyfix = true;
                    longPressHelper(p);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            savemybeacon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    beaconconfig.setVisibility(View.GONE);

                    int beaconid = GeoDatabase.getInstance(getContext()).beaconsDao().getBeaconsList().size() + 1;
                    beacons = new Beacons(beaconid, p.getLatitude(), p.getLongitude(), Double.parseDouble(String.valueOf(seekBar.getProgress())), true);

                    lazyfix = false;
                    GeoDatabase.getInstance(getContext()).beaconsDao().insertBeacons(beacons);
                    Intent intent = new Intent(getActivity(), GeoService.class);
                    getActivity().startForegroundService(intent);

                }
            });

            cancelmybeacon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mapview.getOverlays().remove(circle);
                    mapview.invalidate();
                    beaconconfig.setVisibility(View.GONE);
                }
            });

            textviewradius.setText("Radius: " + seekBar.getProgress() + " m");

            return false;
        }
    };
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

            findMyLocation(mapview, geoPoint);

//            if (mapview.getOverlays().contains(circle)) {
//                geofenceAction(geoPoint,cpoint,crad);
//            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        setHasOptionsMenu(true);

        mapview = view.findViewById(R.id.mapview);
        textviewradius = view.findViewById(R.id.textview_radius);
        seekBar = view.findViewById(R.id.seekBar);
        savemybeacon = view.findViewById(R.id.button2);
        cancelmybeacon = view.findViewById(R.id.button);

        beaconconfig = view.findViewById(R.id.linearlayout2);
        beaconconfig.setVisibility(View.GONE);

        loadMap();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.appbar_buttons, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_findme) {

            try {
                locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, locationListener);
                locationProvider = locationManager.getProvider(LocationManager.NETWORK_PROVIDER);
                location = locationManager.getLastKnownLocation(locationProvider.getName());
                if (location != null) {
                    geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    findMyLocation(mapview, geoPoint);
                    mapview.getController().animateTo(geoPoint, 20.0, Long.parseLong("1500"));
                } else {
                    Toast.makeText(getContext(), "Please enable location.", Toast.LENGTH_SHORT).show();
                }


            } catch (SecurityException e) {
                Log.e("jpwolfso", String.valueOf(e));
            }

            return true;
        } else if (item.getItemId() == R.id.action_helpme) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Instructions")
                    .setMessage("Tap and hold anywhere on the map to configure a geofence. Tap on an existing geofence and click 'OK' to delete it.")
                    .setPositiveButton("Close", null)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        mapview.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        mapview.onPause();
    }

    protected void loadMap() {
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        Configuration.getInstance().load(getActivity().getApplicationContext(), PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()));
        mapview.setMultiTouchControls(true);
        mapview.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);

        mapview.setBuiltInZoomControls(false);
        mapview.setTilesScaledToDpi(true);


        BoundingBox boundingBox = new BoundingBox(85.05112877980658, 180.0, -85.05112877980658, -180.0);
        mapview.setScrollableAreaLimitDouble(boundingBox);

        mapview.setVerticalMapRepetitionEnabled(false);
        mapview.setHorizontalMapRepetitionEnabled(false);

        mapview.getController().setZoom(2.0);

        for (int i = 1; i <= GeoDatabase.getInstance(getContext()).beaconsDao().getBeaconsList().size(); i++) {
            Double latitude = GeoDatabase.getInstance(getContext()).beaconsDao().getBeaconLat(i);
            Double longitude = GeoDatabase.getInstance(getContext()).beaconsDao().getBeaconLong(i);
            Double radius = GeoDatabase.getInstance(getContext()).beaconsDao().getBeaconRad(i);
            generateCircle(mapview, new GeoPoint(latitude, longitude), Double.parseDouble(String.valueOf(radius)));
        }

        mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        mapview.getOverlays().add(mapEventsOverlay);

        lazyfix = !mapview.getOverlays().contains(circle);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }

    }

    protected void generateCircle(MapView myMapView, GeoPoint myGeoPoint, Double myRadius) {

        myMapView.getOverlays().remove(circle);
        circle = new Polygon(myMapView);
        circle.getFillPaint().setColor(Color.RED);
        circle.getFillPaint().setAlpha(100);
        circle.setOnClickListener(new Polygon.OnClickListener() {
            @Override
            public boolean onClick(Polygon polygon, MapView mapView, GeoPoint eventPos) {
                if (beaconconfig.getVisibility() == View.GONE) {
                    new AlertDialog.Builder(getActivity()).setTitle("Delete geofence?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mapview.getOverlays().remove(circle);
                                    mapview.invalidate();
                                    GeoDatabase.getInstance(getContext()).beaconsDao().deleteBeacons(cpoint.getLatitude(), cpoint.getLongitude(), crad);
                                    lazyfix = true;

                                    Intent intent = new Intent(getActivity(), GeoService.class);
                                    getActivity().stopService(intent);

                                }
                            }).show();
                }
                return false;
            }
        });

        circle.setPoints(Polygon.pointsAsCircle(myGeoPoint, myRadius));

        myMapView.getOverlays().add(circle);

        myMapView.invalidate();

        cpoint = myGeoPoint;
        crad = myRadius;

        mapEventsOverlay = new MapEventsOverlay(mapEventsReceiver);
        mapview.getOverlays().remove(mapEventsOverlay);
        mapview.getOverlays().add(mapEventsOverlay);
    }

    protected void findMyLocation(MapView myMapView, GeoPoint myGeoPoint) {

        if (myMapView.getOverlays().contains(personOverlay)) {
            mapview.getOverlays().remove(personOverlay);
        }

        personIcon = new OverlayItem(null, null, myGeoPoint);
        personIcon.setMarker(ResourcesCompat.getDrawable(getResources(), R.drawable.person, null));

        personOverlayList = new ArrayList<>();
        personOverlayList.add(personIcon);

        personOverlay = new ItemizedOverlayWithFocus<>(personOverlayList, null, getActivity().getApplicationContext());
        myMapView.getOverlays().add(personOverlay);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
