package com.monaem.ensismartparking;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Activity_List extends ListActivity {
    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> spotsList;
    Button bRefresh ;
    boolean is_connected_hotspot,is_connected_wifi;




    // url to get spots list
    private static String url_all_spots = "https://ensismartpark.000webhostapp.com/login_register/get_empty_spots.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_SPOTS = "spots";
    private static final String TAG_PID = "pid";
    private static final String TAG_REF = "ref";
    private static final String TAG_STATE = "state";
    private static final String TAG_UPDATE = "update";

    // products JSONArray
    JSONArray spots = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__list);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);



        // Hashmap for ListView
        spotsList = new ArrayList<HashMap<String, String>> ();

        // Loading spots in Background Thread
        new LoadAllSpots().execute();

        // Get listview
        ListView lv = getListView();

        bRefresh= (Button) findViewById(R.id.refreshButton);

        // on seleting single product
        // launching Edit Product Screen
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String pid = "spot"+ ((TextView) view.findViewById(R.id.pid)).getText()
                        .toString();

                if (is_connected_wifi || is_connected_hotspot) {

                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        SpotDesign.class);
                // sending pid to next activity
                in.putExtra(TAG_PID, pid);

                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                }else
                {

                    Toast.makeText(getApplicationContext(),
                            "Your Data is Off : Turn on data or WI-FI", Toast.LENGTH_SHORT)
                            .show();
                }

            }
        });

        bRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (is_connected_wifi || is_connected_hotspot) {
                Intent i = new Intent(Activity_List.this, Activity_List.class);  //your class
                overridePendingTransition( 0, 0);
                startActivity(i);
                overridePendingTransition( 0, 0);
                finish();
                }else
                {

                    Toast.makeText(getApplicationContext(),
                            "Your Data is Off : Turn on data or WI-FI", Toast.LENGTH_SHORT)
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

                    final Context ctx = Activity_List.this;
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
                final Context ctx = Activity_List.this;
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
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllSpots extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(Activity_List.this);
            pDialog.setMessage("Loading spots. Please wait...");

            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting spots from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            // getting JSON string from URL
            System.out.println("------------------------------------");
            JSONObject json = jParser.makeHttpRequest(url_all_spots, "GET", params);
            System.out.println("------------------------------------");
            // Check your log cat for JSON reponse
            Log.d("All Spots: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // spots found
                    // Getting Array of spots
                    spots = json.getJSONArray(TAG_SPOTS);

                    // looping through spots
                    for (int i = 0; i < spots.length(); i++) {
                        JSONObject c = spots.getJSONObject(i);

                        // Storing each json item in variable
                        String ref = c.getString(TAG_REF);
                        String id = c.getString(TAG_PID);
                        String update = c.getString(TAG_UPDATE);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_PID, id);
                        map.put(TAG_REF, "Spot N : "+ref);
                        map.put(TAG_STATE,"State : Empty");
                        map.put(TAG_UPDATE,"Last Update : "+update);



                        // adding HashList to ArrayList
                        spotsList.add(map);
                    }
                }/*else{

                    AlertDialog.Builder builder = new AlertDialog.Builder(Activity_List.this);
                    builder.setMessage("no free spot found ")
                            .setNegativeButton("retry",null)
                            .create()
                            .show();
                    System.out.println("----------------hey");
                }*/
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            Activity_List.this, spotsList,
                            R.layout.item_spot, new String[] { TAG_REF,
                            TAG_STATE,TAG_UPDATE,TAG_PID},
                            new int[] { R.id.tv_spot_ref, R.id.tv_spot_state , R.id.tv_spot_owner,R.id.pid});
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }


}
