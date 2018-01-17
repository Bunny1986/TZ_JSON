package com.kenyrim.TZ_JSON;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private ListView lv;
    Handler mHandler;


    private static String url = "http://phisix-api3.appspot.com/stocks.json";

    ArrayList<HashMap<String, String>> currencyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.list);
        this.mHandler = new Handler();


        m_Runnable.run();
    }
    private final Runnable m_Runnable = new Runnable()
    {
        public void run()

        {
            currencyList = new ArrayList<>();
            new GetCurrency().execute();
            MainActivity.this.mHandler.postDelayed(m_Runnable,15000);
        }

    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                currencyList = new ArrayList<>();
                new GetCurrency().execute();
                Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT)
                        .show();
                break;
            default:
                break;
        }

        return true;
    }

    private class GetCurrency extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    JSONArray currencys = jsonObj.getJSONArray("stock");

                    for (int i = 0; i < currencys.length(); i++) {

                        JSONObject c = currencys.getJSONObject(i);

                        String name = c.getString("name");
                        String volume = c.getString("volume");

                        JSONObject price = c.getJSONObject("price");
                        BigDecimal am = new BigDecimal(price.getDouble("amount"));
                        BigDecimal am1 = am.setScale(2,BigDecimal.ROUND_DOWN);
                        String amount = am1.toString();

                        HashMap<String, String> currency = new HashMap<>();


                        currency.put("name", name);
                        currency.put("volume", volume);
                        currency.put("amount", amount);


                        currencyList.add(currency);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();

            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, currencyList,
                    R.layout.list_item, new String[]{
                    "name",
                    "volume",
                    "amount"},
                    new int[]{
                    R.id.name,
                    R.id.volume,
                    R.id.amount});

            lv.setAdapter(adapter);
        }
    }

}