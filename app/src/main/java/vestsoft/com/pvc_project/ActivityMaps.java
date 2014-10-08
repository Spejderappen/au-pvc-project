package vestsoft.com.pvc_project;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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
import vestsoft.com.pvc_project.Model.Reminder;


public class ActivityMaps extends Activity
        implements FriendsAdapter.FriendsAdapterCallback, LocationListener, GoogleMap.OnInfoWindowClickListener {

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
    private GetRemindersTask mGetRemindersTask = null;
    private ReminderTask mReminderTask = null;
    private SearchForRemindersTask mSearchForRemindersTask = null;

    private BluetoothAdapter btAdapter;

    private TextView tvFriendCloseToYou;

    private boolean reminderModeOn = false;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

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

        phone = sharedPrefs.getString("my_phone", "not set");

        StartUploadingMyPostion();
        getMyReminders();

        mFriendsMarkers = new ArrayList<Pair<Friend, Marker>>();
        StartUpdatingFriendsPositions();

        UploadBluetoothName();
        SearchForBtDevices();

        tvFriendCloseToYou = (TextView) findViewById(R.id.tvFriendCloseToYou);
        tvFriendCloseToYou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvFriendCloseToYou.setVisibility(View.INVISIBLE);
            }
        });

        startReminderSearch();
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

    Handler reminder;

    private void startReminderSearch() {
        reminder = new Handler();
        reminder.post(mRunnableReminder);
    }

    private Runnable mRunnableReminder = new Runnable() {

        @Override
        public void run() {
            if (mMyLastLocation != null) {
                LatLng latLng = new LatLng(mMyLastLocation.getLatitude(), mMyLastLocation.getLongitude());

                mSearchForRemindersTask = new SearchForRemindersTask(mMapReminders, latLng);
                mSearchForRemindersTask.execute((Void) null);
            }
            mHandler.postDelayed(mRunnable, UPDATE_FREQUENZY);
        }
    };

    private void getMyReminders() {
        mGetRemindersTask = new GetRemindersTask(phone);
        mGetRemindersTask.execute((Void) null);
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

    private void UploadBluetoothName() {
        String phone = sharedPrefs.getString("my_phone", "not set");
        String btName = btAdapter.getAddress();

        mUpladBTNameTask = new UploadBTNameTask(phone, btName);
        mUpladBTNameTask.execute((Void) null);
    }

    private void SearchForBtDevices() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0); // Makes the device always discoverable
        startActivity(discoverableIntent);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        filter.addAction(BluetoothDevice.ACTION_UUID);
//        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(ActionFoundReceiver, filter);

        BluetoothAdapter.getDefaultAdapter().startDiscovery();

        //useSearchForBtDeviesHandler();
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
            if (((Friend) p.first).getPhone().equals(selectedFriend.getPhone())) {
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
        unregisterReceiver(ActionFoundReceiver);
    }

    /**
     * STUFF REGARDING THE MAP
     */
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager mLocationManager;

    private Marker mMyMarker;
    private List<Pair<Friend, Marker>> mFriendsMarkers;

    private List<Pair<Reminder, Marker>> mMapReminders = new ArrayList<Pair<Reminder, Marker>>();

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

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.equals(mMyMarker)) {

                }
                return false;
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (reminderModeOn) {
                    Reminder reminder = new Reminder();
                    reminder.setLongitude(latLng.longitude);
                    reminder.setLatitude(latLng.latitude);
                    addReminderShowDialog(reminder);
                }
            }
        });

        mMap.setOnInfoWindowClickListener(this);
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

        findViewById(R.id.imgBtnToogleReminders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton imgBtn = (ImageButton) v;
                if (reminderModeOn) {
                    imgBtn.setImageResource(R.drawable.ic_reminder);
                } else {
                    imgBtn.setImageResource(R.drawable.ic_reminder_selected);
                }

                reminderModeOn = !reminderModeOn;

                for (Pair<Reminder, Marker> pair : mMapReminders) {
                    pair.second.setVisible(reminderModeOn);
                }
            }
        });
    }

    private void addReminderShowDialog(final Reminder reminder) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Add reminder");
        alert.setMessage("Type the reminder you want to be shown");
        alert.setCancelable(false);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String reminderText = input.getText().toString();

                reminder.setText(reminderText);

                addReminderToMap(reminder);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        AlertDialog alertToShow = alert.create();
        alertToShow.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE); // To show the keypad when the dialog is shown
        alertToShow.show();
    }

    private void editReminderShowDialog(final Pair<Reminder, Marker> pairReminderMarker) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Edit reminder");
        alert.setMessage("Type the reminder you want to be shown");
        alert.setCancelable(false);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setText(pairReminderMarker.first.getText());

        alert.setView(input);
        alert.setPositiveButton("Save reminder", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String reminderText = input.getText().toString();

                pairReminderMarker.first.setText(reminderText);
                pairReminderMarker.second.setSnippet(reminderText);

                // Just to make sure that the info text is updated
                pairReminderMarker.second.hideInfoWindow();
                pairReminderMarker.second.showInfoWindow();

                mReminderTask = new ReminderTask(pairReminderMarker.first, 3, phone);
                mReminderTask.execute((Void)null);
            }
        });

        alert.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                pairReminderMarker.second.remove();
                mMapReminders.remove(pairReminderMarker);

                mReminderTask = new ReminderTask(pairReminderMarker.first, 2, phone);
                mReminderTask.execute((Void)null);
            }
        });

        AlertDialog alertToShow = alert.create();
        alertToShow.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE); // To show the keypad when the dialog is shown
        alertToShow.show();
    }

    private void addReminderToMap(Reminder reminder) {
        Marker markerReminder = mMap.addMarker(new MarkerOptions().position(new LatLng(reminder.getLatitude(), reminder.getLongitude()))
                .title("Reminder")
                .snippet(reminder.getText())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        mMapReminders.add(new Pair<Reminder, Marker>(reminder, markerReminder));
        mReminderTask = new ReminderTask(reminder, 1, phone);
        mReminderTask.execute((Void)null);
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

    @Override
    public void onInfoWindowClick(Marker marker) {
        Pair<Reminder, Marker> pairReminderMarker = null;
        for (Pair<Reminder, Marker> pair : mMapReminders) {
            if (pair.second.equals(marker)) {
                pairReminderMarker = pair;
                break;
            }
        }

        editReminderShowDialog(pairReminderMarker);
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
     * Represents an task to get the positions of the friends and upload your own
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
            mUpladBTNameTask = null;
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
                result = ServerCommunication.updateBluetoothName(mySelf);
            } catch (Exception e) {
                if (e.getMessage() != null)
                    Log.e("PVC", e.getMessage());
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

    public class GetRemindersTask extends AsyncTask<Void, Void, Boolean> {

        private final String phone;
        List<Reminder> reminderList = null;

        GetRemindersTask(String phone) {
            this.phone = phone;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //            reminderTask = ServerCommunication.getReminders(phone);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            for (Reminder reminder : reminderList) {
                addReminderToMap(reminder);
            }
        }

        @Override
        protected void onCancelled() {
            mUpladBTNameTask = null;
        }
    }

    public class SearchForRemindersTask extends AsyncTask<Void, Void, Boolean> {

        private final List<Pair<Reminder, Marker>> reminderMarkerPairs;
        LatLng myPosition;

        SearchForRemindersTask(List<Pair<Reminder, Marker>> reminderMarkerPairs, LatLng myPosition) {
            this.reminderMarkerPairs = reminderMarkerPairs;
            this.myPosition = myPosition;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            for (Pair<Reminder, Marker> reminderMarkerPair : reminderMarkerPairs) {
                float[] distance = new float[0];
                Location.distanceBetween(myPosition.latitude, myPosition.longitude, reminderMarkerPair.second.getPosition().latitude, reminderMarkerPair.second.getPosition().longitude, distance);
            }
            return true;
        }


        @Override
        protected void onPostExecute(final Boolean success) {

        }

        @Override
        protected void onCancelled() {
            mUpladBTNameTask = null;
        }
    }

    public class ReminderTask extends AsyncTask<Void, Void, Boolean> {

        private final Reminder reminder;
        private final int reminderTask;
        private final String phone;
        List<Reminder> reminderList = null;

        ReminderTask(Reminder reminder, int reminderAction, String phone) {
            this.reminder = reminder;
            this.reminderTask = reminderAction;
            this.phone = phone;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            switch (reminderTask) {
                case 1:
                    createReminder();
                    break;
                case 2:
                    deleteReminder();
                    break;
                case 3:
                    editReminder();
                    break;

            }

            return true;
        }

        private void createReminder() {
            //  ServerCommunication.createReminder(reminder, phone);
        }

        private void deleteReminder() {
            //  ServerCommunication.deleteReminder(reminder);
        }

        private void editReminder() {
            //  ServerCommunication.editReminder(reminder);
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            // Nothing really happens here anyway..
        }

        @Override
        protected void onCancelled() {
            mUpladBTNameTask = null;
        }
    }

    private List<Friend> closeFriendsBefore = new ArrayList<Friend>();
    private List<Friend> closeFriendsNow = new ArrayList<Friend>();

    // <editor-fold desc="- Region - BroadCastReceiver">
    // Used when something is found from the bluetoothAdapter.startDiscovery();
    private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Some serious performance optimization could be done here
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                for (Friend friend : mMapsNavigationDrawerFragment.getFriends()) {
                    if (friend.getBluetoothName().equals(device.getAddress())) {
                        closeFriendsNow.add(friend);
                        if (!friend.isCloseToYou()) {
                            tvFriendCloseToYou.setText(friend.getName() + " is close to you");
                            tvFriendCloseToYou.setVisibility(View.VISIBLE);
                            friend.setCloseToYou(true);
                        }
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                for (Friend closeFriendBefore : closeFriendsBefore) {
                    boolean isClose = false;
                    for (Friend closeFriendNow : closeFriendsNow) {
                        if (closeFriendNow.getPhone().equals(closeFriendBefore.getPhone())) {
                            isClose = true;
                            break;
                        }
                    }
                    // If friend is not close now
                    if (!isClose) {
                        for (Friend friend : mMapsNavigationDrawerFragment.getFriends()) {
                            if (friend.getPhone().equals(closeFriendBefore.getPhone())) {
                                friend.setCloseToYou(false);
                                tvFriendCloseToYou.setText(friend.getName() + " is not close to you anymore");
                                tvFriendCloseToYou.setVisibility(View.VISIBLE);
                                break;
                            }
                        }
                    }
                }
                closeFriendsBefore = new ArrayList<Friend>(closeFriendsNow);
                closeFriendsNow.clear();

                btAdapter.startDiscovery();
            }
        }
    };
// </editor-fold>
}
