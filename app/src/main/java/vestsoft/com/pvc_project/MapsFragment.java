package vestsoft.com.pvc_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import vestsoft.com.pvc_project.Model.Friend;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapsFragment#//newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class MapsFragment extends Fragment implements LocationListener {
//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager mLocationManager;

    private Marker mMyMarker;
    private List<Pair<Friend,Marker>> mFriendsMarkers;

    private Location mMyLastLocation;
    private View view;

    private OnFragmentInteractionListener mListener;


//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment MapsFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static MapsFragment newInstance(String param1, String param2) {
//        MapsFragment fragment = new MapsFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpLocationManager();
        //setUpMapIfNeeded(); is done in onCreateView()
        //setUpButtons(); is done in onCreateView()

        mFriendsMarkers = new ArrayList<Pair<Friend, Marker>>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view  = inflater.inflate(R.layout.fragment_maps, container, false);
        setUpMapIfNeeded();
        setUpButtons();
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void addFriend(Friend selectedFriend) {
        Marker tempMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(10.0, 10.0))
                .title(selectedFriend.getName())
                .snippet("Was here at <some datetime>"));
        mFriendsMarkers.add(new Pair<Friend, Marker>(selectedFriend,tempMarker));
    }

    public void removeFriend(Friend selectedFriend) {
        Pair foundPair = null;
        for (Pair p : mFriendsMarkers){
            if (p.first.equals(selectedFriend)){
                ((Marker)p.second).remove();
                foundPair = p;
                break;
            }
        }
        if (foundPair != (null)){
            mFriendsMarkers.remove(foundPair);
        }
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
        public void onFragmentInteraction(Uri uri);
    }


    // From MapsActivity
    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the MapFragment.
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_friends);
            try {
                mMap = mapFragment.getMap();
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        if (marker.equals(mMyMarker)){

                        }
                        return  false;
                    }
                });
            }
            catch (Exception e){
                Log.e("Map Fragment",e.getMessage() );
            }

            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location !=  null){
            mMyMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("My location")
                    .snippet("This is a snippet"));

            CameraUpdate cmUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10);
            mMap.moveCamera(cmUpdate);
            mMyLastLocation = location;
        }
        else {
            AskUserToTurnOnGPS();
        }
    }

    private void setUpButtons() {
        view.findViewById(R.id.btnGoToMyLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMyLastLocation != null) {
                    mMyMarker.remove();
                    mMyMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mMyLastLocation.getLatitude(), mMyLastLocation.getLongitude())).title("MyLocation").snippet("This is a snippet"));
                    CameraUpdate cmUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mMyLastLocation.getLatitude(), mMyLastLocation.getLongitude()), 12);
                    mMap.moveCamera(cmUpdate);
                } else {
                   // Toast.makeText(view.getBaseContext(), "Couldn't find your location", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setUpLocationManager() {
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, // The provicer we want to use
                5000, // How often the location  mininum is requested, in milliseconds
                10, // the minimum distance interval for notifications, in meters
                this);
    }

    private void AskUserToTurnOnGPS() {
        // Show dialog
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity(), AlertDialog.THEME_HOLO_DARK);
        builder.setTitle("GPS");
        builder.setMessage("Please turn on the GPS");
        builder.setCancelable(true);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                dialog.cancel();
            }
        });
        AlertDialog turnOnGPSAlert = builder.create();
        turnOnGPSAlert.show();
    }


    @Override
    public void onLocationChanged(Location location) {
        if (mMyMarker != null) {
            mMyMarker.remove();
        }

        mMyMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("MyLocation").snippet("This is a snippet"));
        mMyLastLocation = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        //Toast.makeText(getBaseContext(), "Gps turned on ", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        //Toast.makeText(getBaseContext(), "Gps turned off ", Toast.LENGTH_LONG).show();
    }
}
