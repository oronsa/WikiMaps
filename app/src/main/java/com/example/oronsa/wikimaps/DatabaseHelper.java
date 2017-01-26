package com.example.oronsa.wikimaps;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "wikibase.db";
    private static final String FAVORITES_TABLE = "favorites_table";
    private static final String HISTORY_TABLE = "history_table";
    private static final String ID = "_id";
    private static final String TITLE = "TITLE";
    private static final String LAT = "LAT";
    private static final String LON = "LON";
    private static final String IMAGE_STRING = "IMAGE_STRING";
    private static final String DATE = "DATE";
    private static final int DATABASE_VERSION  = 1;

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//        context.deleteDatabase(DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + FAVORITES_TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT" +
                "," + TITLE + " TEXT," + IMAGE_STRING + " TEXT," + LAT + " FLOAT," + LON + " FLOAT)");
        db.execSQL("CREATE TABLE " + HISTORY_TABLE + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT" +
                "," + TITLE + " TEXT," + DATE + " TEXT," + IMAGE_STRING + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FAVORITES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + HISTORY_TABLE);
    }

    boolean insertDataFavorites(String title, Double lat, Double lon,String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TITLE, title);
        values.put(LAT, lat);
        values.put(LON, lon);
        values.put(IMAGE_STRING, image);
        Long result = db.insert(FAVORITES_TABLE, null, values);
        return result != -1;
    }
    boolean insertDataHistory(String title, String image,String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TITLE, title);
        values.put(IMAGE_STRING, image);
        values.put(DATE, date);
        Long result = db.insert(HISTORY_TABLE, null, values);
        return result != -1;
    }

    Cursor getAllDataFavorites() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + FAVORITES_TABLE, null);
    }
    Cursor getAllDataHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("select * from " + HISTORY_TABLE, null);
    }

    Integer deleteDataFavorites(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(FAVORITES_TABLE, "TITLE=?", new String[]{title});
    }
    Cursor titleQueryFavorites(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM "+ FAVORITES_TABLE +" WHERE TITLE=?", new String[]{title});
    }
    Cursor titleQueryHistory(String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("SELECT * FROM "+ HISTORY_TABLE +" WHERE TITLE=?", new String[]{title});
    }
    void deleteAllFavorites(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(FAVORITES_TABLE, null, null);
    }
    void deleteAllHistory(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(HISTORY_TABLE, null, null);
    }
}
