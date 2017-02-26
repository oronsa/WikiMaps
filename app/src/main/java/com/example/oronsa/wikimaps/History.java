package com.example.oronsa.wikimaps;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class History extends Activity{
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);

        databaseHelper = new DatabaseHelper(this);
        ListView listView = (ListView)findViewById(R.id.list_view_history);
        TextView favorites_footer = (TextView) findViewById(R.id.favorites_footer_tv);
        TextView history_footer = (TextView) findViewById(R.id.history_footer_tv);
        TextView nearby_footer = (TextView) findViewById(R.id.nearby_footer_tv);
        ImageView image_result = (ImageView) findViewById(R.id.no_results_image);
        TextView header_title = (TextView) findViewById(R.id.header_title);
        ImageView trash = (ImageView)findViewById(R.id.trash_image);

        header_title.setText(R.string.header_title_history);
        favorites_footer.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.favorites_black, 0, 0);
        history_footer.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.history_blue_icon, 0, 0);
        nearby_footer.setCompoundDrawablesWithIntrinsicBounds(0, R.mipmap.navigation_icon, 0, 0);

        Cursor cursor = databaseHelper.getAllDataHistory();
        final CustomAdapterHistory adapter = new CustomAdapterHistory(this,cursor);
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
                Intent intent = new Intent(History.this, MapsActivity.class);
                startActivity(intent);
            }
        });
        favorites_footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(History.this, Favorites.class);
                startActivity(intent);
            }
        });
        trash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter.getCount() != 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(History.this);
                    builder.setTitle("Warning");
                    builder.setMessage("Are you sure you want to delete all your History list?");

                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            // Do nothing but close the dialog
                            databaseHelper.deleteAllHistory();
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
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openWiki(((TextView) view.findViewById(R.id.item_title_history)).getText().toString());
            }
        });
    }
    private void openWiki(String title) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse
                ("http://en.wikipedia.org/wiki/" + title));
        startActivity(browserIntent);
    }
}
