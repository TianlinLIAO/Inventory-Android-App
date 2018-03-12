package com.example.android.inventory.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Tianlin Liao on 4/4/2017.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + "(" +
                    ProductContract.ProductEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    ProductContract.ProductEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                    ProductContract.ProductEntry.COLUMN_PRICE + " REAL NOT NULL DEFAULT 0.00, " +
                    ProductContract.ProductEntry.COLUMN_QUANTITY + " INTEGER DEFAULT 0, " +
                    ProductContract.ProductEntry.COLUMN_SUPPLIER + " TEXT NOT NULL, " +
                    ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL + " TEXT, " +
                    ProductContract.ProductEntry.COLUMN_SUPPLIER_TEL + " TEXT, " +
                    ProductContract.ProductEntry.COLUMN_IMAGE + " TEXT NOT NULL);";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ProductContract.ProductEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "product.db";

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    public void sellOneItem(long itemId, int quantity) {
        SQLiteDatabase database = getWritableDatabase();
        if (quantity == 0) return;
        quantity--;
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_QUANTITY, quantity);
        String selection = ProductContract.ProductEntry.COLUMN_ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(itemId)};
        database.update(ProductContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    public Cursor readProduct() {
        SQLiteDatabase database = getReadableDatabase();
        String[] projection = {
                ProductContract.ProductEntry.COLUMN_ID,
                ProductContract.ProductEntry.COLUMN_NAME,
                ProductContract.ProductEntry.COLUMN_PRICE,
                ProductContract.ProductEntry.COLUMN_QUANTITY,
                ProductContract.ProductEntry.COLUMN_SUPPLIER,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductContract.ProductEntry.COLUMN_SUPPLIER_TEL,
                ProductContract.ProductEntry.COLUMN_IMAGE
        };
        Cursor cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, null, null, null, null, null);
        return cursor;
    }
}
