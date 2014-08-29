package vestsoft.com.pvc_project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements LocationListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager mLocationManager;
    private Marker mMyMarker;
    private Location mMyLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpLocationManager();
        setUpMapIfNeeded();
        setUpButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
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
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
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
           mMyMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("MyLocation"));
            CameraUpdate cmUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10);
            mMap.moveCamera(cmUpdate);
            mMyLastLocation = location;
        }
        else {
            AskUserToTurnOnGPS();
        }
    }

    private void setUpButtons() {
        findViewById(R.id.btnGoToMyLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMyLastLocation != null) {
                    mMyMarker.remove();
                    mMyMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(mMyLastLocation.getLatitude(), mMyLastLocation.getLongitude())).title("MyLocation"));
                    CameraUpdate cmUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(mMyLastLocation.getLatitude(), mMyLastLocation.getLongitude()), 10);
                    mMap.moveCamera(cmUpdate);
                } else {
                    Toast.makeText(getBaseContext(), "Couldn't find your location", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setUpLocationManager() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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




    // Obsolete - 29-08-2014
    private Location getLastKnownLocation() {
        LocationManager  mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }
}
