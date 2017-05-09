package com.monaem.ensismartparking;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.monaem.ensismartparking.helper.SQLiteHandler;
import com.monaem.ensismartparking.helper.SessionManager;

import java.util.HashMap;

public class Activity_Main extends Activity {

    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout,btnConsult,btnChangePss;
    boolean is_connected_hotspot,is_connected_wifi;


    private SessionManager session;
    private SQLiteHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__main);

        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnConsult = (Button) findViewById(R.id.btnConsult);
        btnChangePss = (Button) findViewById(R.id.btnChangePass);


        // session manager
        session = new SessionManager(getApplicationContext());

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        final String email = user.get("email");
        final String password = user.get("password");


        // Displaying the user details on the screen
        txtName.setText(name);
        txtEmail.setText(email);

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });

        // Consult button click event
        btnConsult.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (is_connected_wifi || is_connected_hotspot) {

                Intent intent1 = new Intent(Activity_Main.this, Activity_List.class);
                startActivity(intent1);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                }else
                {
                    Toast.makeText(getApplicationContext(),
                            "Your Data is Off : Turn on data or WI-FI", Toast.LENGTH_SHORT)
                            .show();
                }

            }
        });

        btnChangePss.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (is_connected_wifi || is_connected_hotspot) {

                    Intent intent1 = new Intent(Activity_Main.this, ChangePassActivity.class);
                    intent1.putExtra("email", email);
                    intent1.putExtra("password", password);
                    startActivity(intent1);
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                }else
                {
                    Toast.makeText(getApplicationContext(),
                            "Your Data is Off : Turn on data or WI-FI", Toast.LENGTH_SHORT)
                            .show();
                }

            }
        });

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

                    final Context ctx = Activity_Main.this;
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
                final Context ctx = Activity_Main.this;
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

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

        // Launching the login activity
        Intent intent = new Intent(Activity_Main.this, Activity_Login.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        finish();
    }

}
