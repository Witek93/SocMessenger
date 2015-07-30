package pl.comarch.soc.socmessenger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import pl.comarch.soc.socmessenger.model.Message;


public class MessageDatabaseHelper {

    private static final String DB_NAME = "messages";
    private static final int DB_VERSION = 2;

    private DbHelper helper;

    public MessageDatabaseHelper(Context context) {
        helper = new DbHelper(context);
    }

    public long insert(String content, long currentMillis, String from, String to) {
        ContentValues values = new ContentValues();
        values.put("content", content);
        values.put("date", currentMillis);
        values.put("userFrom", from);
        values.put("userTo", to);
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.insert(DB_NAME, null, values);
    }


    public List<Message> selectAll(String to, String from) {
        String sql = "SELECT * FROM " + DB_NAME + " WHERE " +
                "userTo=\'" + to + "\' AND userFrom=\'" + from + "\' OR " +
                "userTo=\'" + from + "\' AND userFrom=\'" + to + "\'";
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, null);

        List<Message> messages = new LinkedList<>();
        if(cursor.moveToFirst()) {
            do {
                String author = cursor.getString(cursor.getColumnIndex("userFrom"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                long date = cursor.getLong(cursor.getColumnIndex("date"));
                Message message = new Message(content, author, new Date(date));
                messages.add(message);
            } while(cursor.moveToNext());
        }
        return messages;
    }


    public static class DbHelper extends SQLiteOpenHelper {

        public DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE IF NOT EXISTS " + DB_NAME +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "userFrom TEXT NOT NULL, " +
                    "userTo TEXT NOT NULL, " +
                    "date INTEGER, " +
                    "content TEXT NOT NULL)";
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
