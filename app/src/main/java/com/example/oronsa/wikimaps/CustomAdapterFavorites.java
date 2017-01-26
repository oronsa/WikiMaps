package com.example.oronsa.wikimaps;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

 class CustomAdapterFavorites extends CursorAdapter {

     CustomAdapterFavorites(Context context, Cursor cursor) {
        super(context,cursor,0);
     }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView title_page = (TextView) view.findViewById(R.id.item_title_favorites);
        title_page.setText(cursor.getString(1));

        ImageView image_page = (ImageView) view.findViewById(R.id.image_page_favorites);
        image_page.setImageBitmap(stringToBitmap(cursor.getString(2)));

    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_favorites, parent, false);
    }
    private Bitmap stringToBitmap(String str)
    {
        byte[] b = Base64.decode(str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(b, 0, b.length);

    }
}