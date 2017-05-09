package com.monaem.ensismartparking;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

public class SpotDesign extends Activity {

    private ImageView SpotImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_design);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        Intent i = getIntent();
        // Receiving the Data
        final String spodId = i.getStringExtra("pid");
        int SpotRid = getResources().getIdentifier(spodId, "drawable",  getPackageName());

        SpotImg = (ImageView) findViewById(R.id.iv_Spot);
        SpotImg.setBackgroundResource(SpotRid);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), Activity_List.class);
        startActivityForResult(myIntent, 0);
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        return true;

    }
}
