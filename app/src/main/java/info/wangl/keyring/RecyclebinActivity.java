package info.wangl.keyring;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecyclebinActivity extends AppCompatActivity {
    private DBManager mgr;
    private SimpleAdapter mRecycleAdapter;
    private ArrayList<HashMap<String, Object>> mListData = new ArrayList<HashMap<String, Object>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recyclebin);
        setupActionBar();

        mgr = new DBManager(this);

        //listView
        ListView listRecycle = (ListView) findViewById(R.id.listRecycle);
        String[] strings = {"image", "title", "username", "password", "url"};//Map的key集合数组
        int[] ids = {R.id.img, R.id.title, R.id.username, R.id.password, R.id.url};//对应布局文件的id

        List<KeyInfo> recycle = mgr.getRecycleBin();

        HashMap<String, Object> map = null;
        for (KeyInfo keyInfo:recycle) {
            map = new HashMap<String, Object>();
            map.put("id", keyInfo._id);
            map.put("title", keyInfo.title);
            map.put("username", keyInfo.username);
            map.put("password", keyInfo.password);
            map.put("url", keyInfo.url);
            map.put("image", R.drawable.ic_menu_slideshow);
            mListData.add(map);
        }

        mRecycleAdapter = new SimpleAdapter(this,
                mListData, R.layout.keyinfo_list_item7, strings, ids);
        listRecycle.setAdapter(mRecycleAdapter);//绑定适配器

        listRecycle.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //showKeyInfoMenuDialog((int)mListData.get(position).get("id"));
                return true;
            }
        });

        findViewById(R.id.recycle_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mgr.cleanRecycleBin();
                refreshList();
            }
        });

        findViewById(R.id.recycle_restore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mgr.restoreRecycleBin();
                refreshList();
            }
        });
    }

    private void refreshList() {
        mListData.clear();
        mRecycleAdapter.notifyDataSetChanged();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

}
