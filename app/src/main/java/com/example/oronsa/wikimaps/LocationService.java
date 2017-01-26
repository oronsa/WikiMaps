package com.example.oronsa.wikimaps;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

public class LocationService extends Service {

    private LocationManager locationManager;
    final long MIN_TIME_FOR_UPDATE = 5000; //check myLocation every 5 seconds
    final float MIN_DIS_FOR_UPDATE = 100.00f;//check myLocation every 100 meter
    private int radius;
    //Strings to register to create intent filter for registering the receivers
    private static final String ACTION_STRING_SERVICE = "ToService";
    private static final String ACTION_STRING_ACTIVITY = "ToActivity";

    private BroadcastReceiver serviceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(), "received message in service..!", Toast.LENGTH_SHORT).show();
            Log.d("Service", "Sending broadcast to activity");
        }
    };
    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {

        public void onLocationChanged(Location location) {

            if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            // Called when a new location is found by the network location provider.
            Location currentLocation = new Location("");
            currentLocation.setLongitude(location.getLongitude());
            currentLocation.setLatitude(location.getLatitude());
            makeUseOfNewLocation(currentLocation);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("Service", "onCreate");
//register the receiver
        if (serviceReceiver != null) {
//Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_SERVICE"
            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_SERVICE);
//Map the intent filter to the receiver
            registerReceiver(serviceReceiver, intentFilter);
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return START_NOT_STICKY;
        }
        //to increase accuracy
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
        criteria.setAltitudeRequired(true);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setCostAllowed(true);

        String best = locationManager.getBestProvider(criteria, false);
        locationManager.requestLocationUpdates(best, MIN_TIME_FOR_UPDATE,
                MIN_DIS_FOR_UPDATE,
                locationListener);
        //get values from main activity
        radius = (int) intent.getExtras().get("radius");
        return START_REDELIVER_INTENT;
    }
    public void makeUseOfNewLocation(Location myLocation) {
        String result=buildUrlPath(myLocation);
        sendBroadcastMethod(result,myLocation);
    }
    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    String buildUrlPath(Location myLocation) {
        double lat = myLocation.getLatitude();
        double lon = myLocation.getLongitude();
        Log.i("radius", String.valueOf(radius));
        return  "https://en.wikipedia.org/w/api.php?action=query&list=geosearch&gsradius="+radius+"" +
                "&gscoord="+lat+"%7C"+lon+"&format=json&gslimit=500";
    }

    private void sendBroadcastMethod(String result,Location myLocation) {
        Intent new_intent = new Intent();
        new_intent.setAction(ACTION_STRING_ACTIVITY);
        new_intent.putExtra("message",result);
        new_intent.putExtra("location",myLocation);
        sendBroadcast(new_intent);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        stopSelf();
        locationManager.removeUpdates(locationListener);
        Log.d("Service", "onDestroy");
        unregisterReceiver(serviceReceiver);
    }

}