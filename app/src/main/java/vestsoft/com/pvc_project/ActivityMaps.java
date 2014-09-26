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
import android.os.Looper;
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
    private final int UPDATE_FREQUENZY = 5000;

    private UpdatePositionTask mUpdatePostionTask = null;
    private FetchFriendsPositionsTask mUpdateFriendsPositionsTask = null;
    private UploadBTNameTask mUpladBTNameTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mMapsNavigationDrawerFragment = (MapsNavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mMapsNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

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
        StartUpdatingFriendsPositions();

        UploadBluetoothName();
    }

    private void UploadBluetoothName() {
        String phone = sharedPrefs.getString("my_phone","not set");
        String btName = null;
        

        mUpladBTNameTask = new UploadBTNameTask(phone,btName);
        mUpdatePostionTask.execute((Void) null);
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
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    private void StartUploadingMyPostion() {
        useHandler();
    }

    private void StartUpdatingFriendsPositions() {
        useUpdateFriendsHandler();
    }

    @Override
    public void onCheckBoxCheckedListener(Friend selectedFriend) {
        if (selectedFriend.isSelected())
            addFriend(selectedFriend, true);
        else
            removeFriend(selectedFriend);
    }

    public void addFriend(Friend selectedFriend, boolean moveToFriend) {
        Marker tempMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(selectedFriend.getLatitude(), selectedFriend.getLongitude()))
                .title(selectedFriend.getName())
                .snippet("Was here at " + selectedFriend.getDateTime())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mFriendsMarkers.add(new Pair<Friend, Marker>(selectedFriend, tempMarker));

        if (moveToFriend) {
            // Move camera to the position of the friend
            CameraUpdate cmUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(selectedFriend.getLatitude(), selectedFriend.getLongitude()), 15);
            mMap.moveCamera(cmUpdate);
        }
    }

    public void removeFriend(Friend selectedFriend) {
        List<Pair> foundPairs = new ArrayList<Pair>();
        for (Pair p : mFriendsMarkers) {
            if (((Friend)p.first).getPhone().equals(selectedFriend.getPhone())) {
                ((Marker) p.second).remove();
                foundPairs.add(p);
            }
        }
        if (foundPairs.size() > 0) {
            mFriendsMarkers.remove(foundPairs);
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
    private List<Pair<Friend, Marker>> mFriendsMarkers;

    private Location mMyLastLocation;

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
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
                        if (marker.equals(mMyMarker)) {

                        }
                        return false;
                    }
                });
            } catch (Exception e) {
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
                );

                CameraUpdate cmUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10);
                mMap.moveCamera(cmUpdate);
                mMyLastLocation = location;
            }
        } else {
            AskUserToTurnOnGPS();
        }
    }

    private void setUpButtons() {
        findViewById(R.id.imgBtnGoToMyLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMyLastLocation != null) {
                    mMyMarker.remove();
                    mMyMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mMyLastLocation.getLatitude(), mMyLastLocation.getLongitude())).title("MyLocation"));
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

        mMyMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("MyLocation"));
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

            mHandler.postDelayed(mRunnable, UPDATE_FREQUENZY);
        }
    };

    Handler mUpdateFriendsHandler;

    public void useUpdateFriendsHandler() {
        mUpdateFriendsHandler = new Handler(Looper.getMainLooper());
        mUpdateFriendsHandler.postDelayed(mRunnableUpdateFriends, UPDATE_FREQUENZY);
    }

    private Runnable mRunnableUpdateFriends = new Runnable() {

        @Override
        public void run() {
            mUpdateFriendsPositionsTask = new FetchFriendsPositionsTask(getApplicationContext());
            mUpdateFriendsPositionsTask.execute((Void) null);
            mUpdateFriendsHandler.postDelayed(mRunnableUpdateFriends, UPDATE_FREQUENZY);
        }
    };

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UpdatePositionTask extends AsyncTask<Void, Void, Boolean> {
        UpdatePositionTask() {
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                UpdateMyPosition();
            } catch (Exception e) {
                Log.e(PROJECT_NAME, e.getMessage());
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
            mUpdateFriendsPositionsTask = null;
        }

        private void UpdateMyPosition() {
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
        Context context;
        List<Friend> friendsUpdated = new ArrayList<Friend>();

        FetchFriendsPositionsTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                FetchFriendsPosition();
            } catch (Exception e) {
                Log.e(PROJECT_NAME, e.getMessage());
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (friendsUpdated.size() == mFriendsMarkers.size()) {
                if (friendsUpdated.size() > 0) {
                    for (Pair<Friend, Marker> friendMarkerPair : mFriendsMarkers) {
                        removeFriend(friendMarkerPair.first);
                    }

                    List<Pair<Friend, Marker>> mFriendsMarkersUpdated = new ArrayList<Pair<Friend, Marker>>();
                    for (Friend friend : friendsUpdated) {
//                      addFriend(friend, false);
                        Marker tempMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(friend.getLatitude(), friend.getLongitude()))
                                .title(friend.getName())
                                .snippet("Was here at " + friend.getDateTime())
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        mFriendsMarkersUpdated.add(new Pair<Friend, Marker>(friend, tempMarker));
                    }

                    mFriendsMarkers = mFriendsMarkersUpdated;
                }
            }
        }

        @Override
        protected void onCancelled() {
            mUpdatePostionTask = null;
            mUpdateFriendsPositionsTask = null;
        }

        private void FetchFriendsPosition() {
            Log.d("PVC", Integer.toString(mFriendsMarkers.size()));
            if (mFriendsMarkers.size() > 0) {
                List<Friend> friends = new ArrayList<Friend>();
                for (Pair<Friend, Marker> friend : mFriendsMarkers) {
                    friends.add(friend.first);
                }
                friendsUpdated = ServerCommunication.getExistingFriends(friends);
            }
        }
    }

    /**
     * Represents an asynchronous task used to upload
     * the bluetoothname of the user
     */
    public class UploadBTNameTask extends AsyncTask<Void, Void, Boolean> {

        private final String mPhoneNumber;
        private final String mBTName;

        UploadBTNameTask(String phonenumber, String BTName) {
            mPhoneNumber = phonenumber;
            mBTName = BTName;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // attempt authentication against a network service.

            Boolean result = false;
            try {
                Friend mySelf = new Friend();
                mySelf.setPhone(mPhoneNumber);
                mySelf.setBluetoothName(mBTName);
                result = true;//ServerCommunication.updateBluetoothName(mySelf);
            } catch (Exception e) {
                if (e.getMessage() != null)
                    Log.e("PVC",e.getMessage());
            }

            return result;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mUpladBTNameTask = null;

            if (success) {

            } else {

            }
        }

        @Override
        protected void onCancelled() {
            mUpladBTNameTask = null;
        }
    }
}
