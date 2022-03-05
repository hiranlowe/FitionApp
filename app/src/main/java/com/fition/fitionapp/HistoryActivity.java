package com.fition.fitionapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HistoryActivity extends AppCompatActivity {

    TextView textView;
    List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    Map<String, String> datum = new HashMap<String, String>(2);

    ListView listView;
    SwipeRefreshLayout mySwipeRefreshLayout;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        listView = findViewById(R.id.listView2);
        mySwipeRefreshLayout = findViewById(R.id.swiperefresh);
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("Activity History");

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        // create object of MyAsyncTasks class and execute it
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (listView.getChildAt(0) != null) {
                    mySwipeRefreshLayout.setEnabled(listView.getFirstVisiblePosition() == 0 && listView.getChildAt(0).getTop() == 0);
                }
            }
        });
        new AsyncDownload().execute();
        mySwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        System.out.println("Refreshing");

                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        new AsyncDownload().execute();
                        mySwipeRefreshLayout.setRefreshing(false);
                    }
                }
        );


    }

    private void showActi(JSONArray jsonArray, SimpleAdapter adapter){
//
        data = new ArrayList<Map<String, String>>();
        try {
//            for (int i = 0; i < jsonArray.length(); i++) {
//                String sentiment=jsonArray.getJSONObject(i).getString("sentiment");
//                String sentiment_text=jsonArray.getJSONObject(i).getString("sentiment_text");
//                String probability=jsonArray.getJSONObject(i).getString("probability");
//                String current_time=jsonArray.getJSONObject(i).getString("current_time");
//                datum = new HashMap<String, String>(2);
//                datum.put("First Line", "Activity -"+" "+sentiment_text);
//                datum.put("Second Line", "Probability -"+probability+" "+"Time - "+current_time);
////                datum.put(sentiment+" "+sentiment_text, probability+" "+current_time);
//                data.add(datum);
//            }
            System.out.println(jsonArray.getJSONObject(0).getString("sentiment"));
            for (int i = jsonArray.length()-1; i > -1; i--) {
                String sentiment=jsonArray.getJSONObject(i).getString("sentiment");
                String sentiment_text=jsonArray.getJSONObject(i).getString("sentiment_text");
                String probability=jsonArray.getJSONObject(i).getString("probability");
                String current_time=jsonArray.getJSONObject(i).getString("current_time");
                datum = new HashMap<String, String>(2);
                datum.put("First Line", "Activity -"+" "+sentiment_text);
                datum.put("Second Line", "Probability -"+probability+" "+"Time - "+current_time);
//                datum.put(sentiment+" "+sentiment_text, probability+" "+current_time);
                data.add(datum);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            adapter.notifyDataSetChanged();
        }


        return;
    }

    public class AsyncDownload extends AsyncTask<String, String, String> {

        String myUrl = "http://10.0.2.2:8000/history";
        ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // display a progress dialog to show the user what is happening
            progressDialog = new ProgressDialog(HistoryActivity.this);
            progressDialog.setMessage("processing results");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            try {
                URL url;
                HttpURLConnection urlConnection = null;
                try {
                    url = new URL(myUrl);
                    //open a URL coonnection

                    urlConnection = (HttpURLConnection) url.openConnection();

                    InputStream in = urlConnection.getInputStream();

                    InputStreamReader isw = new InputStreamReader(in);

                    int data = isw.read();

                    while (data != -1) {
                        result += (char) data;
                        data = isw.read();

                    }

                    // return the data to onPostExecute method
                    return result;

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "Exception: " + e.getMessage();
            }
            return result;
        }



        @Override
        protected void onPostExecute(String s) {
            // show results
            // dismiss the progress dialog after receiving data from API
            progressDialog.dismiss();
//            datum.put("First Line", "First line of text");
//            datum.put("Second Line","Second line of text");
//            data.add(datum);
            SimpleAdapter adapter = new SimpleAdapter(HistoryActivity.this, data,
                    R.layout.list_view,
                    new String[] {"First Line", "Second Line" },
                    new int[] {R.id.text1, R.id.text2 });
            listView.setAdapter(adapter);

//            datum.put("First Line", "Activity -"+" "+"sentiment_text");
//            datum.put("Second Line", "Probability -"+"probability"+" "+"Time - "+"current_time");
////                datum.put(sentiment+" "+sentiment_text, probability+" "+current_time);
//            data.add(datum);
//            adapter.notifyDataSetChanged();

//            final List<String> ListElementsArrayList = new ArrayList<>(Arrays.asList(ListElements));
//            final ArrayAdapter<String> adapter = new ArrayAdapter<>
//                    (HistoryActivity.this, android.R.layout.simple_list_item_1, ListElementsArrayList);
//            final ArrayAdapter<String> adapter = new ArrayAdapter(HistoryActivity.this, android.R.layout.simple_list_item_2, android.R.id.text1, ListElements) {
//                @Override
//                public View getView(int position, View convertView, ViewGroup parent) {
//                    System.out.println("here5");
//                    View view = super.getView(position, convertView, parent);
//                    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
//                    TextView text2 = (TextView) view.findViewById(android.R.id.text2);
////                    String pred = ListElements[position];
//                    System.out.println(position);
////                    String[] parts = pred.split("-");
//                    text1.setText("hi");
//                    text2.setText("hey");
//                    return view;
//                }
//            };
            try {

                JSONArray jsonArray = new JSONArray(s);

//                JSONArray jsonArray1 = jsonObject.getJSONArray();
                System.out.println(jsonArray.getJSONObject(0));
//                String data = jsonArray.getJSONObject(0).getString("title");
//                textView.setText(data);

//                showActi(jsonArray, adapter);
                try {
//            for (int i = 0; i < jsonArray.length(); i++) {
//                String sentiment=jsonArray.getJSONObject(i).getString("sentiment");
//                String sentiment_text=jsonArray.getJSONObject(i).getString("sentiment_text");
//                String probability=jsonArray.getJSONObject(i).getString("probability");
//                String current_time=jsonArray.getJSONObject(i).getString("current_time");
//                datum = new HashMap<String, String>(2);
//                datum.put("First Line", "Activity -"+" "+sentiment_text);
//                datum.put("Second Line", "Probability -"+probability+" "+"Time - "+current_time);
////                datum.put(sentiment+" "+sentiment_text, probability+" "+current_time);
//                data.add(datum);
//            }
                    System.out.println(jsonArray.getJSONObject(0).getString("sentiment"));
                    for (int i = jsonArray.length()-1; i > -1; i--) {
                        String sentiment=jsonArray.getJSONObject(i).getString("sentiment");
                        String sentiment_text=jsonArray.getJSONObject(i).getString("sentiment_text");
                        String probability=jsonArray.getJSONObject(i).getString("probability");
                        String current_time=jsonArray.getJSONObject(i).getString("current_time");
                        datum = new HashMap<String, String>(2);
                        datum.put("First Line", "Activity -"+" "+sentiment_text);
                        datum.put("Second Line", "Probability -"+probability+" "+"Time - "+current_time);
//                datum.put(sentiment+" "+sentiment_text, probability+" "+current_time);
                        data.add(datum);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    adapter.notifyDataSetChanged();
                }

//                for (int i = 0; i < jsonArray.length(); i++) {
//                    String userId=jsonArray.getJSONObject(i).getString("userId");
//                    String title=jsonArray.getJSONObject(i).getString("title");
//                    String body=jsonArray.getJSONObject(i).getString("body");
//                }
//                JSONObject jsonObject1 =jsonArray1.getJSONObject(index_no);
//                String id = jsonObject1.getString("id");
//                String name = jsonObject1.getString("name");
//                String my_users = "User ID: "+id+"\n"+"Name: "+name;
//
//                //Show the Textview after fetching data
//                resultsTextView.setVisibility(View.VISIBLE);
//
//                //Display data with the Textview
//                resultsTextView.setText(my_users);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

