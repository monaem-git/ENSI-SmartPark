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
import com.monaem.ensismartparking.helper.SQLiteHandler;
import com.monaem.ensismartparking.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Activity_Login extends Activity {// LogCat tag
    private static final String TAG = Activity_Register.class.getSimpleName();
    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;
    boolean is_connected_hotspot,is_connected_wifi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__login);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);



        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // Session manager
        session = new SessionManager(getApplicationContext());

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(Activity_Login.this, Activity_Main.class);
            startActivity(intent);
            finish();
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                pDialog.setMessage("Logging in ...");
                showDialog();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        pDialog.dismiss();
                    }
                }, 25000);
                final String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();

                // Check for empty data in the form

                if (email.trim().length() > 0 && password.trim().length() > 5 && validateEmail(email) ) {

                    if (is_connected_wifi || is_connected_hotspot) {

                        Response.Listener<String> ResponseListener = new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                try {
                                    JSONObject JsonResponse = new JSONObject(response);
                                    boolean success = JsonResponse.getBoolean("success");

                                    if (success) {
                                        // user successfully logged in
                                        // Create login session
                                        session.setLogin(true);

                                        // Launch main activity
                                        String name = JsonResponse.getString("name");
                                        hideDialog();
                                        Intent intent = new Intent(Activity_Login.this, Activity_Main.class);
                                        // Inserting row in users table
                                        db.addUser(name, email);

                                        Activity_Login.this.startActivity(intent);
                                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

                                    } else

                                    {
                                        hideDialog();
                                        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_Login.this);
                                        builder.setMessage("Login failed")
                                                .setNegativeButton("retry", null)
                                                .create()
                                                .show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        };

                        LoginRequest LoginRequest = new LoginRequest(email, password, ResponseListener);
                        RequestQueue queue = Volley.newRequestQueue(Activity_Login.this);
                        queue.add(LoginRequest);
                    }else
                {
                    hideDialog();
                    Toast.makeText(getApplicationContext(),
                            "Your Data is Off : Turn on data or WI-FI", Toast.LENGTH_SHORT)
                            .show();
                }


                }else if(email.trim().length() > 0 && password.trim().length() > 0 && !validateEmail(email)) {
                    // Prompt user to enter credentials
                    hideDialog();
                    Toast.makeText(getApplicationContext(),
                            "Email invalid!", Toast.LENGTH_SHORT)
                            .show();

                } else if(email.trim().length() > 0 && password.trim().length() < 6 ) {
                    // Prompt user to enter credentials
                    hideDialog();
                    Toast.makeText(getApplicationContext(),
                            "Password  must have at least 6 characters", Toast.LENGTH_SHORT)
                            .show();

                }
                else {
                    // Prompt user to enter credentials
                    hideDialog();
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_SHORT)
                            .show();
                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        Activity_Register.class);
                startActivity(i);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), Activity_Register.class);
        startActivityForResult(myIntent, 0);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
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
                    final Context ctx = Activity_Login.this;
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
                final Context ctx = Activity_Login.this;
                final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
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


    public boolean validateEmail(String email) {

        Pattern pattern;
        Matcher matcher;
        String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();

    }


    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
