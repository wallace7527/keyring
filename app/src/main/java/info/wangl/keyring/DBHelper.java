package info.wangl.keyring;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "keyring.db";
    private static final int DATABASE_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS keyinfo" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, catalog INTEGER, title VARCHAR, username VARCHAR, password VARCHAR, url VARCHAR, notes TEXT, image BLOB, remove INTEGER)");

        db.execSQL("CREATE TABLE IF NOT EXISTS keycatalog" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR )");

        ContentValues contents = new ContentValues();
        contents.put("name", "General");
        db.insert("keycatalog", null, contents);

        contents.put("name", "Windows");
        db.insert("keycatalog", null, contents);

        contents.put("name", "Network");
        db.insert("keycatalog", null, contents);

        contents.put("name", "Internet");
        db.insert("keycatalog", null, contents);

        contents.put("name", "eMail");
        db.insert("keycatalog", null, contents);

        contents.put("name", "Homebanking");
        db.insert("keycatalog", null, contents);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("ALTER TABLE keyinfo ADD COLUMN remove INTEGER");
//        db.execSQL("UPDATE keyinfo SET remove = 0");
    }
}
