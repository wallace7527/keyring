package info.wangl.keyring;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final Integer KEYINFO_NEW_ID = -1;
    private DBManager mgr;
    private int mCatalogId = 0;
    private SimpleAdapter mKeyInfoAdapter;
    private ArrayList<HashMap<String, Object>> mListData;
    private List<KeyCatalog> mCatalogs;

    public interface OnConfirmListener { public void onConfirmClick(); }


    private void showConfirmDialog(String title, String message, final OnConfirmListener onConfirmListener){
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setIcon(android.R.drawable.ic_delete);
        normalDialog.setTitle(title);
        normalDialog.setMessage(message);
        normalDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onConfirmListener.onConfirmClick();
                    }
                });
        normalDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        // 显示
        normalDialog.show();
    }

    private void showDeleteKeyCatalogDialog(){
        final List<KeyCatalog> catalogs = mgr.getAllCatalogs();
        final String [] items = new String[catalogs.size()];

        for( int i = 0; i < catalogs.size(); i++ )
        {
            items[i] = catalogs.get(i).name;
        }

        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        listDialog.setTitle("删除类别");
        listDialog.setItems (items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                showConfirmDialog("删除确认",
                        "删除类别将清空该类别下所有信息，不可恢复，确认要这样做吗？",
                        new OnConfirmListener() {
                            @Override
                            public void onConfirmClick() {
                                mgr.deleteKeyCatalog(catalogs.get(which));
                                refreshMenu();
                            }
                        });
            }
        });
        listDialog.show();
    }

    private void showKeyCatalogDialog() {

        final EditText editText = new EditText(MainActivity.this);
        editText.setSingleLine();
        AlertDialog.Builder inputDialog =
                new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("输入类别名称").setView(editText);
        inputDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        KeyCatalog catalog = new KeyCatalog();
                        catalog.name = editText.getText().toString();

                        mgr.addKeyCatalog(catalog);
                        refreshMenu();
                    }
                }).show();
    }


    private void showKeyInfoDialog(final int id) {

        KeyInfo keyInfo = mgr.getKeyInfoById( id );

        AlertDialog.Builder keyInfoDialog =
                new AlertDialog.Builder(MainActivity.this){

                };
        final View dialogView = LayoutInflater.from(MainActivity.this)
                .inflate(R.layout.dialog_keyinfo,null);
        Spinner spinner = (Spinner)dialogView.findViewById(R.id.spinner);
        spinner.setPrompt("请选择类别");

        int pos = 0;

        ArrayList<HashMap<String, Object>> catalogList = new ArrayList<>();

        for (int i = 0; i < mCatalogs.size(); i++ ) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("name", mCatalogs.get(i).name);
            map.put("id", mCatalogs.get(i)._id);
            catalogList.add(map);
            if ( keyInfo != null ) {
                if (mCatalogs.get(i)._id == keyInfo.catalog) {
                    pos = i;
                }
            }else {
                if (mCatalogs.get(i)._id == mCatalogId) {
                    pos = i;
                }
            }
        }

        SimpleAdapter catalogAdapter = new SimpleAdapter(MainActivity.this, catalogList, android.R.layout.simple_spinner_item, new String[]{"name"}, new int[]{android.R.id.text1});
        catalogAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);//设置显示风格
        spinner.setAdapter(catalogAdapter); //把设配器的内容放到Spinner中

        spinner.setSelection(pos);

        keyInfoDialog.setTitle("输入秘钥信息");
        keyInfoDialog.setView(dialogView);
        keyInfoDialog.setNegativeButton( "关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                });

        keyInfoDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        KeyInfo keyInfo = new KeyInfo();
                        HashMap<String, Object> map  = (HashMap<String, Object>) ((Spinner) dialogView.findViewById(R.id.spinner)).getSelectedItem();

                        if ( !KEYINFO_NEW_ID.equals(id) ) {
                            keyInfo._id = id;
                        }
                        keyInfo.catalog = (int) map.get("id");
                        keyInfo.title = getTextString(R.id.editTitle);
                        keyInfo.username = getTextString(R.id.editUsername);
                        keyInfo.password = getTextString(R.id.editPassword);
                        keyInfo.url = getTextString(R.id.editUrl);
                        keyInfo.notes = getTextString(R.id.editNotes);
                        keyInfo.image = null;

                        mgr.updateKeyInfo(keyInfo);
                        refreshListViewKeyInfoData(mListData);
                        mKeyInfoAdapter.notifyDataSetChanged();
                    }

                    @NonNull
                    private String getTextString(int resId) {
                        return ((EditText) dialogView.findViewById(resId)).getText().toString();
                    }
                });

        //初始化数据
        if ( keyInfo != null ) {
            ((EditText) dialogView.findViewById(R.id.editTitle)).setText(keyInfo.title);
            ((EditText) dialogView.findViewById(R.id.editUsername)).setText(keyInfo.username);
            ((EditText) dialogView.findViewById(R.id.editPassword)).setText(keyInfo.password);
            ((EditText) dialogView.findViewById(R.id.editUrl)).setText(keyInfo.url);
            ((EditText) dialogView.findViewById(R.id.editNotes)).setText(keyInfo.notes);
        }
        keyInfoDialog.show();
    }

    private void showEditDeleteDialog(final int id) {
        final String[] items = { "删除","修改" };
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(MainActivity.this);
        //istDialog.setTitle("我是一个列表Dialog");
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    //删除
                    KeyInfo keyInfo = new KeyInfo();
                    keyInfo._id = id;
                    mgr.deleteKeyInfo(keyInfo);
                    refreshListViewKeyInfoData(mListData);
                    mKeyInfoAdapter.notifyDataSetChanged();
                }else if (which == 1) {
                    //修改
                    showKeyInfoDialog(id);
                }

            }
        });
        listDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mgr = new DBManager(this);
        mCatalogId = mgr.minCatalogId();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showKeyInfoDialog(KEYINFO_NEW_ID);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        refreshMenu();

        navigationView.setNavigationItemSelectedListener(this);

        //listView
        ListView listViewKeyInfo = (ListView) findViewById(R.id.listKeyInfo);
        String[] strings = {"image", "title", "username", "password", "url"};//Map的key集合数组
        int[] ids = {R.id.img, R.id.title, R.id.username, R.id.password, R.id.url};//对应布局文件的id
        mListData = new ArrayList<HashMap<String, Object>>();
        refreshListViewKeyInfoData(mListData);
        mKeyInfoAdapter = new SimpleAdapter(this,
                mListData, R.layout.keyinfo_list_item7, strings, ids);
        listViewKeyInfo.setAdapter(mKeyInfoAdapter);//绑定适配器

        listViewKeyInfo.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showEditDeleteDialog((int)mListData.get(position).get("id"));
                return true;
            }
        });

    }

    private void refreshListViewKeyInfoData(ArrayList<HashMap<String, Object>> list) {

        list.clear();
        HashMap<String, Object> map = null;
        List<KeyInfo> keyInfos = mgr.getKeyInfosByCatalog(mCatalogId);
        for (KeyInfo keyInfo:keyInfos) {
            map = new HashMap<String, Object>();
            map.put("id", keyInfo._id);
            map.put("title", keyInfo.title);
            map.put("username", keyInfo.username);
            map.put("password", keyInfo.password);
            map.put("url", keyInfo.url);
            map.put("image", R.drawable.ic_menu_slideshow);
            list.add(map);
        }

    }

    private void refreshMenu() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.clear();
        SubMenu menuCatalog = menu.addSubMenu("Catalog");
        SubMenu menuManager = menu.addSubMenu("Manager");

        mCatalogs = mgr.getAllCatalogs();

        int [] ic_menus = new int[]{R.drawable.ic_menu_camera, R.drawable.ic_menu_gallery, R.drawable.ic_menu_slideshow, R.drawable.ic_menu_manage, R.drawable.ic_menu_share, R.drawable.ic_menu_send};

        for( int i = 0; i < mCatalogs.size(); i++) {
            menuCatalog.add(0,i,i,mCatalogs.get(i).name).setIcon(ic_menus[i%ic_menus.length]);//.setIcon(i%ic_menus.length);
        }

        menuManager.add(0,R.id.add_catalog,1,"Add Catalog");
        menuManager.add(0,R.id.del_catalog,2,"Delete Catalog");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mgr.closeDB();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id >= 0 && id < 100) {
            boolean bChanged = false;

            int catalogId = mCatalogs.get(id)._id;
            if ( mCatalogId != catalogId )
                bChanged = true;

            mCatalogId = catalogId;

            if ( bChanged ) {
                refreshListViewKeyInfoData(mListData);
                mKeyInfoAdapter.notifyDataSetChanged();
            }

        } else if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if ( id == R.id.del_catalog) {
            showDeleteKeyCatalogDialog();
        } else if ( id == R.id.add_catalog) {
            showKeyCatalogDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
