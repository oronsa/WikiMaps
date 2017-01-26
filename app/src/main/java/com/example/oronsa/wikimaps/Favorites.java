package com.example.oronsa.wikimaps;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Locale;


public class Favorites extends Activity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites);
        databaseHelper = new DatabaseHelper(this);
        final ListView listView = (ListView) findViewById(R.id.list_view_favorites);
        TextView favorites_footer = (TextView) findViewById(R.id.favorites_footer_tv);
        TextView history_footer = (TextView) findViewById(R.id.history_footer_tv);
        TextView nearby_footer = (TextView) findViewById(R.id.nearby_footer_tv);
        ImageView image_result = (ImageView) findViewById(R.id.no_results_image);
        TextView header_title = (TextView) findViewById(R.id.header_title);
        ImageView trash = (ImageView)findViewById(R.id.trash_image);
        header_title.setText(R.string.header_title_favorites);
        favorites_footer.setTextColor(Color.parseColor("#02adef"));
        favorites_footer.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.favorites_blue, 0, 0);
        history_footer.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.history_icon, 0, 0);
        nearby_footer.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.navigation_icon, 0, 0);

        Cursor cursor = databaseHelper.getAllDataFavorites();
        final CustomAdapterFavorites adapter = new CustomAdapterFavorites(this,cursor);
        listView.setAdapter(adapter);

        if(listView.getCount()==0) {
            image_result.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }else{
            image_result.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);

        }
        nearby_footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Favorites.this, MapsActivity.class);
                startActivity(intent);
            }
        });
        history_footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Favorites.this, History.class);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView adapterView, final View view, final int position, long id) {

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Favorites.this);
                final LayoutInflater inflater = Favorites.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.custom_dialog_2, null);
                dialogBuilder.setView(dialogView);
                final AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                alertDialog.show();
                TextView google_map = (TextView) dialogView.findViewById(R.id.show_map_2);
                TextView new_tab = (TextView) dialogView.findViewById(R.id.open_new_2);
                TextView remove = (TextView) dialogView.findViewById(R.id.remove);
                new_tab.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.new_tab_info, 0, 0, 0);
                google_map.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.show_on_map_blue, 0, 0, 0);
                remove.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.trash_blue_icon, 0, 0, 0);

                new_tab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openWiki(((TextView) view.findViewById(R.id.item_title_favorites)).getText().toString());
                        alertDialog.dismiss();
                    }
                });
                remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        databaseHelper.deleteDataFavorites(((TextView) view.findViewById(R.id.item_title_favorites)).getText().toString());
                        alertDialog.dismiss();
                        finish();
                        startActivity(getIntent());
                    }
                });
                google_map.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Location location = new Location("");
                        location.setLatitude(0);
                        location.setLongitude(0);
                        Cursor cursor = databaseHelper.titleQueryFavorites(((TextView) view.findViewById(R.id.item_title_favorites)).getText().toString());
                        int latColIndex = cursor.getColumnIndex("LAT");
                        while (cursor.moveToNext()) {
                            location.setLatitude(Double.parseDouble(cursor.getString(latColIndex)));
                            Log.i("lan:", String.valueOf(location.getLatitude()));
                        }
                        cursor = databaseHelper.titleQueryFavorites(((TextView) view.findViewById(R.id.item_title_favorites)).getText().toString());
                        int lonColIndex = cursor.getColumnIndex("LON");
                        while (cursor.moveToNext()) {
                            location.setLongitude(Double.parseDouble(cursor.getString(lonColIndex)));
                            Log.i("lon:", String.valueOf(location.getLongitude()));
                        }
                        if(location.getLongitude() != 0 && location.getLatitude() != 0)
                            openGoogleMap(location);
                        cursor.close();
                        alertDialog.dismiss();
                    }
                });
            }
        });
        trash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getCount() != 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Favorites.this);
                    builder.setTitle("Warning");
                    builder.setMessage("Are you sure you want to delete all your favorites list?");

                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing but close the dialog
                            databaseHelper.deleteAllFavorites();
                            finish();
                            startActivity(getIntent());
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // Do nothing
                            dialog.dismiss();
                        }
                    });

                    AlertDialog alert = builder.create();
                    alert.show();

                }
            }
        });
    }
    private void openWiki(String title) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse
                ("http://en.wikipedia.org/wiki/" + title));
        startActivity(browserIntent);
    }
    private void openGoogleMap(Location location) {
        String uri = String.format(Locale.ENGLISH, "geo:%s,%s",location.getLatitude(),location.getLongitude());
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }
}
