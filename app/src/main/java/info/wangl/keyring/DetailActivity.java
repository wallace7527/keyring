package info.wangl.keyring;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

    private DBManager mgr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setupActionBar();

        mgr = new DBManager(this);

        KeyInfo ki = mgr.getKeyInfoById(getIntent().getIntExtra("keyinfo_id",0));

        if (ki != null) {
            ((TextView)findViewById(R.id.textTitle)).setText(ki.title);
            ((TextView)findViewById(R.id.textUsername)).setText(ki.username);
            ((TextView)findViewById(R.id.textPassword)).setText(ki.password);
            ((TextView)findViewById(R.id.textUrl)).setText(ki.url);
            ((TextView)findViewById(R.id.textNotes)).setText(ki.notes);
        }

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}
