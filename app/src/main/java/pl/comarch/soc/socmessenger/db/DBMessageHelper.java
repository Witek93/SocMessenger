package pl.comarch.soc.socmessenger.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import pl.comarch.soc.socmessenger.model.Message;


public class DBMessageHelper {

    private DbHelper helper;


    public DBMessageHelper(Context context) {
        helper = new DbHelper(context);
    }


    public long insert(String content, long currentTimeInMillis, String from, String to) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.COL_CONTENT, content);
        values.put(DbHelper.COL_DATE, currentTimeInMillis);
        values.put(DbHelper.COL_USERFROM, from);
        values.put(DbHelper.COL_USERTO, to);
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.insert(DbHelper.DB_NAME, null, values);
    }


    public List<Message> selectAll(String to, String from) {
        String sql = "SELECT * FROM " + DbHelper.DB_NAME + " WHERE " +
                "(" + DbHelper.COL_USERTO   + "=\'" + to   + "\' AND " +
                      DbHelper.COL_USERFROM + "=\'" + from + "\') " +
                " OR " +
                "(" + DbHelper.COL_USERTO   + "=\'" + from + "\' AND " +
                      DbHelper.COL_USERFROM + "=\'" + to   + "\')";
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);

        List<Message> messages = new LinkedList<>();
        if(cursor.moveToFirst()) {
            do {
                String author = cursor.getString(cursor.getColumnIndex(DbHelper.COL_USERFROM));
                String content = cursor.getString(cursor.getColumnIndex(DbHelper.COL_CONTENT));
                long date = cursor.getLong(cursor.getColumnIndex(DbHelper.COL_DATE));
                Message message = new Message(content, author, new Date(date));
                messages.add(message);
            } while(cursor.moveToNext());
        }
        return messages;
    }



    public static class DbHelper extends SQLiteOpenHelper {

        private static final int DB_VERSION = 2;
        private static final String DB_NAME = "messages";
        private static final String COL_ID = "id";
        private static final String COL_USERFROM = "userFrom";
        private static final String COL_USERTO = "userTo";
        private static final String COL_DATE = "date";
        private static final String COL_CONTENT = "content";

        public DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE IF NOT EXISTS " + DB_NAME +
                    "(" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USERFROM + " TEXT NOT NULL, " +
                    COL_USERTO   + " TEXT NOT NULL, " +
                    COL_DATE     + " INTEGER, " +
                    COL_CONTENT  + " TEXT NOT NULL )";
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String sql = "DROP TABLE IF EXISTS " + DB_NAME;
            db.execSQL(sql);
            onCreate(db);
        }
    }


}
