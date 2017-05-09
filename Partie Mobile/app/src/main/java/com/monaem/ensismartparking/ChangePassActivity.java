package com.monaem.ensismartparking;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;


public class ChangePassActivity extends Activity {


    private Button btnConfirm;
    private EditText inputPassword;
    private EditText inputNewPassword;
    private ProgressDialog pDialog;
    boolean is_connected_hotspot,is_connected_wifi;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);



        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);


        inputPassword = (EditText) findViewById(R.id.password);
        inputNewPassword = (EditText) findViewById(R.id.new_password);
        btnConfirm= (Button) findViewById(R.id.btnRegister);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Register Button Click event
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {


                pDialog.setMessage("Changing Password ...");
                showDialog();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        pDialog.dismiss();
                    }
                }, 25000);

                Intent i1 = getIntent();
                // Receiving the Data
                String email = i1.getStringExtra("email");
                String password = inputPassword.getText().toString();
                String newpassword = inputNewPassword.getText().toString();

                Log.d("thabet ", email + " "+ password);

                if ( password.length()> 5  &&  newpassword.length()> 5 && !password.equals(newpassword)  ) {


                    if (is_connected_wifi || is_connected_hotspot) {

                        Response.Listener<String> responseListener = new Response.Listener<String>() {

                            @Override
                            public void onResponse(String response) {

                                try {
                                    JSONObject JsonResponse = new JSONObject(response);

                                    boolean success = JsonResponse.getBoolean("success");


                                    if (success) {
                                        hideDialog();
                                        Intent Intent = new Intent(ChangePassActivity.this, Activity_Main.class);
                                        ChangePassActivity.this.startActivity(Intent);
                                        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                                    }else {

                                        hideDialog();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ChangePassActivity.this);
                                        builder.setMessage("Changing Password failed : You have entered a false old password")
                                                .setNegativeButton("retry", null)
                                                .create()
                                                .show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        };

                        ChangePassRequest changepassRequest = new ChangePassRequest(email, password,newpassword, responseListener);
                        RequestQueue queue = Volley.newRequestQueue(ChangePassActivity.this);
                        queue.add(changepassRequest);
                    }else
                    {
                        hideDialog();
                        Toast.makeText(getApplicationContext(),
                                "Your Data is Off : Turn on data or WI-FI", Toast.LENGTH_SHORT)
                                .show();
                    }


                } else if ( password.length() > 5 && newpassword.length() <6 ) {
                    hideDialog();
                    Toast.makeText(getApplicationContext(),
                            "The new Password must have at least 6 characters", Toast.LENGTH_SHORT)
                            .show();

                }else if (password.length() < 6 && newpassword.length() > 5) {

                    hideDialog();
                    Toast.makeText(getApplicationContext(),
                            "Password must have at least 4 characters", Toast.LENGTH_SHORT)
                            .show();

                } else if (password.length() > 0 && newpassword.length() > 0 && password.equals(newpassword) ) {

                    hideDialog();
                    Toast.makeText(getApplicationContext(),
                            "You have entered the same password", Toast.LENGTH_SHORT)
                            .show();

                }

                else {
                    hideDialog();
                    Toast.makeText(getApplicationContext(),
                            "Please enter your details!", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });


    }



    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), Activity_Main.class);
        startActivityForResult(myIntent, 0);
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        return true;

    }

    @Override
    protected void onStart() {
        super.onStart();
        verify_conncetion();
        Log.d("onStart","onStart Login");
    }


    public  void verify_conncetion()
    {

        //creer deux BroadcastReceiver pour restent à l'ecoute de tous changement:

        /// ça pour le wifi
        this.registerReceiver(this.mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        /// ca pour le hotspot
        this.registerReceiver(this.mReceiver, new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED"));
    }


    public final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //cette methode est appelé ssi l'état de wifi est changé donc on fait notre test ici
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {

                // get Wi-Fi Hotspot state here

                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);

                if (WifiManager.WIFI_STATE_ENABLED == state % 10) {

                }
                else
                    is_connected_hotspot=false;

                if(!is_connected_wifi && !is_connected_hotspot)

                {
                    hideDialog();
                    final Context ctx = ChangePassActivity.this;
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setCancelable(false);
                    builder.setMessage("Your Data is Off");
                    builder.setTitle("Turn on data or WI-FI in settings");
                    builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            ctx.startActivity(new Intent(Settings.ACTION_SETTINGS));
                        }
                    });

                    builder.setNegativeButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Closes box
                            dialog.dismiss();
                        }
                    });



                    builder.show();
                }

            }
        }};
    public BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            //cette methode est appelé ssi l'état de hotspot est changé donc on fait notre test ici
            NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

            if(currentNetworkInfo.isConnected()){
                is_connected_wifi=true;
            }
            else {
                is_connected_wifi=false;
            }
            if(!is_connected_wifi && !is_connected_hotspot)

            {
                hideDialog();
                final Context ctx = ChangePassActivity.this;
                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setCancelable(false);
                builder.setMessage("Your Data is Off");
                builder.setTitle("Turn on data or WI-FI in settings");
                builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        ctx.startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }
                });

                builder.setNegativeButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Closes box
                        dialog.dismiss();
                    }
                });



                builder.show();
            }

        }
    };




    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
