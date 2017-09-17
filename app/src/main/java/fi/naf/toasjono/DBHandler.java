package fi.naf.toasjono;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "TOASjono";
    // Contacts table name
    private static final String TABLE_QUEUE = "jono";
    // Shops Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_NUMBER = "number";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_QUEUE_TABLE = "CREATE TABLE " + TABLE_QUEUE + "("
                + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_NAME + " TEXT, "
                + KEY_NUMBER + " INTEGER" + ")";
        db.execSQL(CREATE_QUEUE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUEUE);
        // Creating tables again
        onCreate(db);
    }

    // Adding new shop
    public void addQueue(int key, String name, int queue) {
        if(getQueue(key) != 0)
            return;

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_ID, key);
        values.put(KEY_NAME, name);
        values.put(KEY_NUMBER, queue);

        // Inserting Row
        db.insert(TABLE_QUEUE, null, values);
        db.close(); // Closing database connection
    }

    public void updateQueue(int key, String name, int queue) {
        if(getQueue(key) == 0) {
            addQueue(key, name, queue);
        } else {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(KEY_NAME, name);
            values.put(KEY_NUMBER, queue);

            db.update(TABLE_QUEUE, values, KEY_ID + " = ?",
                new String[] {
                    Integer.toString(key)
                }
            );
        }
    }

    public int getQueue(int key) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_QUEUE, new String[] { KEY_ID,
                        KEY_NAME, KEY_NUMBER }, KEY_ID + "=?",
                new String[] { String.valueOf(key) }, null, null, null, null);

        int queue = 0;

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            queue = Integer.parseInt(cursor.getString(2));
        }

        return queue;
    }

    public List<TOASPosition> getAllToas() {
        List<TOASPosition> toasList = new ArrayList<TOASPosition>();

        String selectQuery = "SELECT * FROM " + TABLE_QUEUE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                TOASPosition toas = new TOASPosition(Integer.parseInt(cursor.getString(0)), cursor.getString(1), Integer.parseInt(cursor.getString(2)));
                toasList.add(toas);
            } while (cursor.moveToNext());
        }
// return contact list
        return toasList;
    }
}