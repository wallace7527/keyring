package info.wangl.keyring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;


public class DBManager implements OnSharedPreferenceChangeListener {
    private static final String ENCRYPT_KEY = "Wang Lei" ;
    private DBHelper helper;
    private SQLiteDatabase db;
    private Integer mPasswordLength;
    private Integer mPasswordLevel;

    public DBManager(Context context) {
        helper = new DBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        mPasswordLength = Integer.valueOf(sharedPref.getString("password_length", "12"));
        mPasswordLevel = Integer.valueOf(sharedPref.getString("password_strength", "3"));
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * add keyinfos
     * @param keyinfos
     */
    public void addKeyInfos(List<KeyInfo> keyinfos) {
        db.beginTransaction();	//开始事务
        try {
            for (KeyInfo keyinfo : keyinfos) {
                addKeyInfo(keyinfo);
            }
            db.setTransactionSuccessful();	//设置事务成功完成
        } finally {
            db.endTransaction();	//结束事务
        }
    }

    public void addKeyInfo(KeyInfo keyinfo) {
        //new String[]{String.valueOf(keyinfo.catalog), keyinfo.title, keyinfo.username, keyinfo.password, keyinfo.url, keyinfo.notes}
        db.execSQL("INSERT INTO keyinfo VALUES(null, ?, ?, ?, ?, ?, ?, ?, null)", new Object[]{keyinfo.catalog, keyinfo.title, keyinfo.username, keyinfo.password, keyinfo.url, keyinfo.notes, keyinfo.image});
    }


    /**
     * delete old keyinfo
     * @param keyinfo
     */
    public void deleteKeyInfo(KeyInfo keyinfo) {
        ContentValues cv = new ContentValues();
        cv.put("remove", 1);
        db.update("keyinfo", cv, "_id = ?", new String[]{String.valueOf(keyinfo._id)});
        //db.delete("keyinfo", "_id = ?", new String[]{String.valueOf(keyinfo._id)});
    }

    public void cleanRecycleBin() {
        db.delete("keyinfo", "remove = 1", null);
    }

    public void restoreRecycleBin() {
        ContentValues cv = new ContentValues();
        cv.put("remove", 0);
        db.update("keyinfo", cv, "remove = 1", null);
    }

    /**
     * query all keyinfos, return list
     * @return List<KeyInfo>
     */
    public List<KeyInfo> getAllKeyInfos() {
        Cursor c = db.rawQuery("SELECT * FROM keyinfo where ifnull(remove,0) <> 1", null);

        return getKeyInfos(c);
    }

    @NonNull
    private List<KeyInfo> getKeyInfos(Cursor c) {
        ArrayList<KeyInfo> keyinfos = new ArrayList<KeyInfo>();
        while (c.moveToNext()) {
            KeyInfo keyinfo = new KeyInfo();
            keyinfo._id = c.getInt(c.getColumnIndex("_id"));
            keyinfo.catalog = c.getInt(c.getColumnIndex("catalog"));
            keyinfo.title = c.getString(c.getColumnIndex("title"));
            keyinfo.username = c.getString(c.getColumnIndex("username"));
            keyinfo.password = c.getString(c.getColumnIndex("password"));
            keyinfo.url = c.getString(c.getColumnIndex("url"));
            keyinfo.notes = c.getString(c.getColumnIndex("notes"));
            keyinfo.image = c.getBlob(c.getColumnIndex("image"));

            keyinfos.add(keyinfo);
        }
        c.close();
        return keyinfos;
    }

    public List<KeyInfo> getKeyInfosByCatalog(int catalog) {
        Cursor c = db.rawQuery("SELECT * FROM keyinfo where catalog = ? and ifnull(remove,0) <> 1 ", new String[]{String.valueOf(catalog)});

        return getKeyInfos(c);
    }


    public void addKeyCatalogs(List<KeyCatalog> catalogs) {
        db.beginTransaction();	//开始事务
        try {
            for (KeyCatalog catalog : catalogs) {
                addKeyCatalog(catalog);
            }
            db.setTransactionSuccessful();	//设置事务成功完成
        } finally {
            db.endTransaction();	//结束事务
        }
    }

    public void addKeyCatalog(KeyCatalog catalog) {
        db.execSQL("INSERT INTO keycatalog VALUES(null, ?)", new Object[]{catalog.name});
    }

    public void deleteKeyCatalog(KeyCatalog catalog) {
        db.delete("keycatalog", "_id = ?", new String[]{String.valueOf(catalog._id)});
    }

    public List<KeyCatalog> getAllCatalogs() {
        Cursor c = db.rawQuery("SELECT * from keycatalog order by _id", null);

        ArrayList<KeyCatalog> keyCatalogs = new ArrayList<>();
        while (c.moveToNext()) {
            KeyCatalog catalog = new KeyCatalog();
            catalog._id = c.getInt(c.getColumnIndex("_id"));
            catalog.name = c.getString(c.getColumnIndex("name"));

            keyCatalogs.add(catalog);
        }

        c.close();

        return keyCatalogs;
    }

    public void deleteAllCatalogs() {
        db.delete( "keycatalog", null, null);
    }

    public int minCatalogId() {
        int catalogId = 0;
        Cursor c = db.rawQuery("SELECT MIN(_id) FROM keycatalog", null);

        if (c.moveToNext()) {
            catalogId = c.getInt(0);
        }
        c.close();

        return catalogId;
    }


    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }


    public KeyInfo getKeyInfoById(int id) {
        Cursor c = db.rawQuery("SELECT * FROM keyinfo where _id = ? and ifnull(remove,0) <> 1", new String[]{String.valueOf(id)});
        List<KeyInfo> list = getKeyInfos(c);
        if ( list.size() ==  1 ) {
            return list.get(0);
        }
        return null;
    }


    public void updateKeyInfo(KeyInfo keyInfo) {
        if (existsKeyInfo(keyInfo)) {
            //update
            _updateKeyInfo(keyInfo);
        } else
        {
            addKeyInfo(keyInfo);
        }
    }

    private void _updateKeyInfo(KeyInfo keyInfo) {
        ContentValues cv = new ContentValues();
        cv.put("title", keyInfo.title);
        cv.put("username", keyInfo.username);
        cv.put("password", keyInfo.password);
        cv.put("url", keyInfo.url);
        cv.put("notes", keyInfo.notes);
        cv.put( "catalog", keyInfo.catalog);
        if ( keyInfo.image != null ) {
            cv.put( "image", keyInfo.image);
        }

        String[] args = {String.valueOf(keyInfo._id)};
        db.update("keyinfo", cv, "_id=?",args );
    }

    private boolean existsKeyInfo(KeyInfo keyInfo) {
        int count = 0;
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM keyinfo where _id=? and ifnull(remove,0) <> 1", new String[]{String.valueOf(keyInfo._id)});

        if (c.moveToNext()) {
            count = c.getInt(0);
        }
        c.close();

        return (count != 0);
    }

    public HashMap<Integer, Integer> countByCatalog() {

        HashMap<Integer, Integer> cbc = new HashMap<Integer, Integer>();
        Cursor c = db.rawQuery("SELECT catalog, COUNT(catalog) AS c FROM keyinfo WHERE ifnull(remove,0) <> 1  GROUP BY catalog", null);
        while (c.moveToNext()) {
            cbc.put(c.getInt(0), c.getInt(1));
        }

        return cbc;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals("password_length")) {
            mPasswordLength = Integer.valueOf(sharedPreferences.getString("password_length", "12"));
        }else if (key.equals("password_strength")) {
            mPasswordLevel = Integer.valueOf(sharedPreferences.getString("password_strength", "3"));
        }
    }

    public String genPassword() {
        Password password = new Password(mPasswordLevel, mPasswordLength);
        try {
            return password.getRandomPassword();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<KeyInfo> searchData(String newText) {
        String kw = "%"+newText+"%";
        Cursor c = db.rawQuery("SELECT * FROM keyinfo where ifnull(remove,0) <> 1 and (title like ? or username like ? or url like ? or notes like ?)", new String[]{kw,kw,kw,kw});
        return getKeyInfos(c);
    }

    public int getCountOfRecycleBin() {
        int count = 0;
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM keyinfo where remove=1", null);

        if (c.moveToNext()) {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    public List<KeyInfo> getRecycleBin() {
        Cursor c = db.rawQuery("SELECT * FROM keyinfo where remove = 1", null);

        return getKeyInfos(c);
    }
}
