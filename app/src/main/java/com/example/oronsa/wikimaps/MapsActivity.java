package com.example.oronsa.wikimaps;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    Spinner radius_spinner;
    Intent intent;
    private static final String ACTION_STRING_SERVICE = "ToService";
    private static final String ACTION_STRING_ACTIVITY = "ToActivity";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 42;
    private Button searchBtn;
    private String maxRadius, currStringImage;
    private Location currLocation, destenation;
    private JSONArray tsmresponse;
    private GoogleMap mMap;
    private ArrayList<String> idList;
    private ArrayList<String> distList;
    private ArrayList<String> titleList;
    private ArrayList<String> lat;
    private ArrayList<String> lon;
    private TextView favorites, open_new, navigate, distance, title;
    private ImageView image;
    private AlertDialog.Builder dialogBuilder;
    private View dialogView;
    private Marker curMarker;
    public DatabaseHelper databaseHelper;
    private AlertDialog alertDialog;
    private LayoutInflater inflater;

    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            idList = new ArrayList<>();
            distList = new ArrayList<>();
            titleList = new ArrayList<>();
            lat = new ArrayList<>();
            lon = new ArrayList<>();
            Toast.makeText(getApplicationContext(), "received message in activity..!", Toast.LENGTH_SHORT).show();
            String result = (String) (intent.getExtras().get("message"));
            currLocation = (Location) (intent.getExtras().get("location"));
            if (result != null) {
                new GetDataTask(MapsActivity.this).execute(result);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        dialogBuilder = new AlertDialog.Builder(MapsActivity.this);
        inflater = MapsActivity.this.getLayoutInflater();
        databaseHelper = new DatabaseHelper(this);

        radius_spinner = (Spinner) findViewById(R.id.radius_spinner);
        searchBtn = (Button) findViewById(R.id.start_search);
        TextView favorites_footer = (TextView) findViewById(R.id.favorites_footer_tv);
        TextView history_footer = (TextView) findViewById(R.id.history_footer_tv);
        TextView nearby_footer = (TextView) findViewById(R.id.nearby_footer_tv);

        nearby_footer.setTextColor(Color.parseColor("#02adef"));
        favorites_footer.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.favorites_black, 0, 0);
        history_footer.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.history_icon, 0, 0);
        nearby_footer.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.navigation_blue, 0, 0);
        //create the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //ask for permissions
        ActivityCompat.requestPermissions(this
                , new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);

        if (activityReceiver != null) {
//Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_ACTIVITY"
            IntentFilter intentFilter = new IntentFilter(ACTION_STRING_ACTIVITY);
//Map the intent filter to the receiver
            registerReceiver(activityReceiver, intentFilter);
        }

        //listener to spinner
        radius_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position != 0) {
                    maxRadius = radius_spinner.getSelectedItem().toString();
                    searchBtn.setEnabled(true);
                }
                if (position == 0)
                    searchBtn.setEnabled(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });
        //listener for search button
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(MapsActivity.this
                        , new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

                maxRadius = extractTheValues(maxRadius);
                extractTheValues(maxRadius);
                intent = new Intent(getBaseContext(), LocationService.class);
                int maxRadiusInt = Integer.parseInt(maxRadius);
                intent.putExtra("radius", maxRadiusInt * 1000);
                sendBroadcastMethod();
                startService(intent);
            }
        });
        favorites_footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, Favorites.class);
                startActivity(intent);
            }
        });
        history_footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivity.this, History.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng israel = new LatLng(31.046051, 34.851612);
//        mMap.addMarker(new MarkerOptions().position(israel).title("Marker in israel"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(israel));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                dialogView = inflater.inflate(R.layout.custom_dialog, null);
                dialogBuilder.setView(dialogView);
                alertDialog = dialogBuilder.create();
                title = (TextView) dialogView.findViewById(R.id.title);
                distance = (TextView) dialogView.findViewById(R.id.distance);
                navigate = (TextView) dialogView.findViewById(R.id.navigate);
                favorites = (TextView) dialogView.findViewById(R.id.favorites);
                open_new = (TextView) dialogView.findViewById(R.id.open_new);
                String textStr = marker.getTitle();
                title.setText(textStr);
                image = (ImageView) dialogView.findViewById(R.id.imageView);
                getImageUrl(marker.getTitle());
                String imagePath = getImageUrl(marker.getTitle());

                navigate.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.navigation_blue, 0, 0, 0);
                open_new.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.new_tab_info, 0, 0, 0);
                favorites.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.favorites_blue, 0, 0, 0);
                curMarker = marker;
                new GetImageTask(MapsActivity.this).execute(imagePath);
                return true;
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.w("MainActivity", "Permissions was generated");
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                } else {
                    Log.e("MainActivity", "Permissions was denied");
                }
            }
        }
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    public void showDropDown(View view) {
        radius_spinner.performClick();
    }

    private String extractTheValues(String maxRad) {
        String arr[] = maxRad.split(" ", 2);
        return maxRadius = arr[0];

    }

    private void addItems() throws ExecutionException, InterruptedException, JSONException {
        int results = tsmresponse.length();
        for (int i = 0; i < results; i++) {
            idList.add(tsmresponse.getJSONObject(i).getString("pageid"));
            distList.add(tsmresponse.getJSONObject(i).getString("dist") + "m");
            titleList.add(tsmresponse.getJSONObject(i).getString("title"));
            lat.add(tsmresponse.getJSONObject(i).getString("lat"));
            lon.add(tsmresponse.getJSONObject(i).getString("lon"));
        }
        for (int i = 0; i < results; i++) {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(Double.parseDouble(lat.get(i)),(Double.parseDouble(lon.get(i)))))
                    .title(titleList.get(i))
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.info_icon)));
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currLocation.getLatitude(), currLocation.getLongitude()), 13));

    }
    private void sendBroadcastMethod() {
        Intent new_intent = new Intent();
        new_intent.setAction(ACTION_STRING_SERVICE);
        sendBroadcast(new_intent);
    }
    private String getImageUrl(String title) {
        String temp1 =title.replaceAll(" ","%20");
        String temp2 =temp1.replaceAll("'","%27");
        return "https://en.wikipedia.org/w/api.php?action=query&prop=pageimages&format=json&piprop=original&titles="+temp2;
    }
    public void navGoogleMap(Location destLocation){
        Intent navigation = new Intent(Intent.ACTION_VIEW, Uri
                .parse("http://maps.google.com/maps?saddr="
                        + currLocation.getLatitude() + ","
                        + currLocation.getLongitude() + "&daddr="
                        + destLocation.getLatitude() + "," + destLocation.getLongitude()));
        navigation.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(navigation);
    }
    private void openWiki(String title) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse
                ("http://en.wikipedia.org/wiki/" + title));
        startActivity(browserIntent);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Activity", "onDestroy");
        stopService(intent);
        unregisterReceiver(activityReceiver);
    }
    private String bitmapToString(Bitmap bitmap)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bytes = stream.toByteArray();
        return  Base64.encodeToString(bytes, Base64.DEFAULT);

    }

    class GetImageTask extends AsyncTask<String, String, String> {
        Context context ;
        GetImageTask(Context context) {
            this.context = context;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return getData(params[0]);
            } catch (IOException ex) {
                return "network error!";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObjectURL = new JSONObject(result);
                JSONObject queryObject = jsonObjectURL.getJSONObject("query");
                JSONObject pagesObject = queryObject.getJSONObject("pages");
                int index = -1;
                for (int i = 0; i < titleList.size(); i++) {
                    if (titleList.get(i).equals(curMarker.getTitle())) {
                        index = i;
                        break;
                    }
                }
                String disStr = getResources().getString(R.string.distance) + " " + distList.get(index);
                distance.setText(disStr);
                JSONObject idObject = pagesObject.getJSONObject(idList.get(index));
                if (idObject.has("thumbnail")) {
                    JSONObject thumbnailObject = idObject.getJSONObject("thumbnail");
                    new DownloadImagesTask(MapsActivity.this).execute(thumbnailObject.get("original").toString());
                }else if(!idObject.has("thumbnail")) {
                    new DownloadImagesTask(MapsActivity.this).execute("https://d8nz9a88rwsc9.cloudfront.net/wp-content/uploads/2014/01/wikipedia.jpg");
                }
                destenation = new Location("");
                LatLng latLng = curMarker.getPosition();
                destenation.setLatitude(latLng.latitude);
                destenation.setLongitude(latLng.longitude);
                navigate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        navGoogleMap(destenation);
                    }
                });
                open_new.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openWiki(curMarker.getTitle());
                    }
                });
                favorites.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Check if already exist in db
                        Cursor c = databaseHelper.titleQueryFavorites(curMarker.getTitle());
                        if(c.getCount()==0) {
                            databaseHelper.insertDataFavorites(curMarker.getTitle(),destenation.getLatitude(), destenation.getLongitude(),currStringImage);
                            alertDialog.dismiss();
                            Toast.makeText(getBaseContext(), "This page successfully added to your favorites", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getBaseContext(), "This page already exist in your favorites", Toast.LENGTH_LONG).show();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String getData(String urlPath) throws IOException {
            StringBuilder result = new StringBuilder();
            BufferedReader bufferedReader = null;
            try {
                URL url = new URL(urlPath);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line).append("\n");

                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
            return result.toString();
        }

    }
    class GetDataTask extends AsyncTask<String, String, String> {
        Context context ;
        GetDataTask(Context context) {
            this.context = context;
        }
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("loading data...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return getData(params[0]);
            } catch (IOException ex) {
                return "network error!";
            }
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject myResponse = jsonObject.getJSONObject("query");
                tsmresponse = myResponse.getJSONArray("geosearch");
                mMap.clear();
                addItems();
            } catch (InterruptedException | ExecutionException | JSONException e) {
                e.printStackTrace();
            }
        }

        private String getData(String urlPath) throws IOException {
            StringBuilder result = new StringBuilder();
            BufferedReader bufferedReader = null;
            try {
                URL url = new URL(urlPath);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line).append("\n");

                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
            return result.toString();
        }
    }

    class DownloadImagesTask extends AsyncTask<String, Void, Bitmap> {

        private Context context ;
        DownloadImagesTask(Context context) {
            this.context = context;
        }
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("loading data...");
            progressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            Bitmap bm = null;
            try {
                bm= Picasso.with(context).load(urls[0]).resize(100,100).get();
            } catch (IOException e) {
                urls[0]="https://d8nz9a88rwsc9.cloudfront.net/wp-content/uploads/2014/01/wikipedia.jpg";
                try {
                    bm= Picasso.with(context).load(urls[0]).resize(100,100).get();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (progressDialog != null) {
                progressDialog.dismiss();
                alertDialog.show();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                currStringImage = bitmapToString(result);
                Cursor c = databaseHelper.titleQueryHistory(curMarker.getTitle());
                if(c.getCount()==0) {
                    Date currDate = new Date();
                    String fDate = new SimpleDateFormat("dd-MM-yyyy",Locale.getDefault()).format(currDate);
                    databaseHelper.insertDataHistory(curMarker.getTitle(),currStringImage,fDate);
                }
            }
            image.setImageBitmap(result);
        }
    }
}




