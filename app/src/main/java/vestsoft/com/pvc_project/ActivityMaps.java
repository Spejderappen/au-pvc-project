package vestsoft.com.pvc_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import vestsoft.com.api.ServerCommunication;
import vestsoft.com.pvc_project.Model.Friend;


public class ActivityMaps extends Activity
        implements FriendsAdapter.FriendsAdapterCallback, LocationListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private MapsNavigationDrawerFragment mMapsNavigationDrawerFragment;

    private SharedPreferences sharedPrefs;

    private final String PROJECT_NAME = "PVC_Project";
    private final int UPDATE_FREQUENZY = 60000;

    private UpdatePositionTask mUpdatePostionTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mMapsNavigationDrawerFragment = (MapsNavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mMapsNavigationDrawerFragment.setUp( R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        // Set up layout containing the map
//        mMapsFragment = new MapsFragment();
//        FragmentManager fragmentManager = getFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.container, mMapsFragment)
//                .commit();

        setUpLocationManager();
        setUpMapIfNeeded();
        setUpButtons();

        sharedPrefs = getSharedPreferences(PROJECT_NAME, MODE_PRIVATE);

        StartUploadingMyPostion();

        mFriendsMarkers = new ArrayList<Pair<Friend, Marker>>();
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        StartUploadingMyPostion();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mHandler != null){
            mHandler.removeCallbacks(mRunnable);
        }
    }

    private void StartUploadingMyPostion(){
        useHandler();
    }

    @Override
    public void onCheckBoxCheckedListener(Friend selectedFriend) {
        if (selectedFriend.isSelected())
            addFriend(selectedFriend);
        else
            removeFriend(selectedFriend);
    }

    public void addFriend(Friend selectedFriend) {
        Marker tempMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(selectedFriend.getLatitide(), selectedFriend.getLongitude()))
                .title(selectedFriend.getName())
                .snippet("Was here at " + selectedFriend.getDateTime())
        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mFriendsMarkers.add(new Pair<Friend, Marker>(selectedFriend,tempMarker));

        // Move camera to the position of the friend
        
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler = null;
    }

    /**
     * STUFF REGARDING THE MAP
     */
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager mLocationManager;

    private Marker mMyMarker;
    private List<Pair<Friend,Marker>> mFriendsMarkers;

    private Location mMyLastLocation;

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
                Log.e("Map Fragment", e.getMessage());
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
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                mMyMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                        .title("My location")
                        .snippet("This is a snippet"));

                CameraUpdate cmUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10);
                mMap.moveCamera(cmUpdate);
                mMyLastLocation = location;
            }
        }
        else {
            AskUserToTurnOnGPS();
        }
    }

    private void setUpButtons() {
        findViewById(R.id.imgBtnGoToMyLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMyLastLocation != null) {
                    mMyMarker.remove();
                    mMyMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mMyLastLocation.getLatitude(), mMyLastLocation.getLongitude())).title("MyLocation").snippet("This is a snippet"));
                    CameraUpdate cmUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mMyLastLocation.getLatitude(), mMyLastLocation.getLongitude()), 12);
                    mMap.moveCamera(cmUpdate);
                } else {
                     Toast.makeText(getApplicationContext(), "Couldn't find your location", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // <editor-fold desc="- Region - Things regarding Location/GPS">
    private void setUpLocationManager() {
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, // The provicer we want to use
                5000, // How often the location  mininum is requested, in milliseconds
                10, // the minimum distance interval for notifications, in meters
                this);
    }

    private void AskUserToTurnOnGPS() {
        // Show dialog
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle("GPS");
        builder.setMessage("Please turn on the GPS");
        builder.setCancelable(true);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
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
// </editor-fold>

    /**
     * Represents an  task to get the positions of the friends and upload your own
     */
    Handler mHandler;
    public void useHandler() {
        mHandler = new Handler();
        mHandler.post(mRunnable);
    }

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            mUpdatePostionTask = new UpdatePositionTask();
            mUpdatePostionTask.execute((Void) null);
            //FetchFriendsPosition();
            mHandler.postDelayed(mRunnable, UPDATE_FREQUENZY);
        }
    };

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UpdatePositionTask extends AsyncTask<Void, Void, Boolean> {
        UpdatePositionTask( ) {        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
               UpdateMyPosition();
            } catch (Exception e) {
                Log.e(PROJECT_NAME,e.getMessage());
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
           // Don't really do anything
        }

        @Override
        protected void onCancelled() {
            mUpdatePostionTask = null;
        }

        private void UpdateMyPosition(){
            if (mMyLastLocation != null) {
                Friend mySelf = new Friend();
                mySelf.setLatitude(mMyLastLocation.getLatitude());
                mySelf.setLongitude(mMyLastLocation.getLongitude());
                mySelf.setPhone(sharedPrefs.getString("my_phone", "not found"));
                ServerCommunication.updatePosition(mySelf);
            }
        }
    }

    /**
            * Represents an asynchronous login/registration task used to authenticate
    * the user.
            */
    public class FetchFriendsPositionsTask extends AsyncTask<Void, Void, Boolean> {
        FetchFriendsPositionsTask( ) {        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                FetchFriendsPosition();
            } catch (Exception e) {
                Log.e(PROJECT_NAME,e.getMessage());
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            // Don't really do anything
        }

        @Override
        protected void onCancelled() {
            mUpdatePostionTask = null;
        }

        private void FetchFriendsPosition(){
            // ServerCommunication.UploadMyPostion(mFriendsMarkers);
        }
    }
}
