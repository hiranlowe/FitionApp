package com.fition.fitionapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;



public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "extramsg";
    private static final String CHANNEL_ID = "test";
    private Button btnConnect;
    private TextView viewText;
    final String host = "3da5a785b21c44fc9f3ff0d47f7b11d4.s1.eu.hivemq.cloud";
    final String username = "fitiontest";
    final String password = "Fition@123";;



    ListView listview;
    Button addButton;
    FloatingActionButton fab;

//    EditText GetValue;
    List <String> msgs = new ArrayList<>();
    String[] ListElements = new String[] {
            "Falling - 15.45 PM 08-02-2022",
            "Walking - 13.20 PM 15-02-2022",
            "Walking - 08.37 PM 18-02-2022",
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        createNotificationChannel();
//        Toast.makeText(MainActivity.this, "channel done", Toast.LENGTH_SHORT).show();

        setContentView(R.layout.activity_main);
        startService(new Intent(this, BackgroundService.class));
        listview = findViewById(R.id.listView1);
        addButton = findViewById(R.id.button1);
        fab = findViewById(R.id.fab);
//        GetValue = findViewById(R.id.editText1);

        final List<String> ListElementsArrayList = new ArrayList<>(Arrays.asList(ListElements));
        final ArrayAdapter<String> adapter = new ArrayAdapter<>
                (MainActivity.this, android.R.layout.simple_list_item_1, ListElementsArrayList);
        listview.setAdapter(adapter);

        addButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        // Stuff that updates the UI

                    }
                });
                //create an MQTT client
                final Mqtt5BlockingClient client = MqttClient.builder()
                        .useMqttVersion5()
                        .serverHost(host)
                        .serverPort(8883)
                        .sslWithDefaultConfig()
                        .buildBlocking();
//
//                //connect to HiveMQ Cloud with TLS and username/pw
                client.connectWith()
                        .simpleAuth()
                        .username(username)
                        .password(UTF_8.encode(password))
                        .applySimpleAuth()
                        .send();
//
                Toast.makeText(MainActivity.this, "connected", Toast.LENGTH_SHORT).show();
//
//                //subscribe to the topic "my/test/topic"
                client.subscribeWith()
                        .topicFilter("my/test/topic")
                        .send();
//
//                //set a callback that is called when a message is received (using the async API style)
                client.toAsync().publishes(ALL, publish -> {
                    System.out.println("Received message: " + publish.getTopic() + " -> " + UTF_8.decode(publish.getPayload().get()));
                    String msg = String.valueOf(UTF_8.decode(publish.getPayload().get()));
                    msgs.add(msg);
                    System.out.println(msgs.size());
                    System.out.println(msgs.get(0));

                    try{
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                // Stuff that updates the UI
                                showActi(msg, adapter, ListElementsArrayList);

                            }
                        });

                    }catch (Exception e){
                        System.out.println("Error" + e.getMessage());
                    }finally {
                        sendNot(msgs);
                    }





                    //disconnect the client after a message was received
//                    client.disconnect();

                });

                System.out.println(msgs.size());
                //publish a message to the topic "my/test/topic"
                client.publishWith()
                        .topic("my/test/topic")
                        .payload(UTF_8.encode("Hello"))
                        .send();
            }
        });

//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
////                        .setAction("Action", null).show();
//
//            }
//        });


    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNot(List <String> list){
        String var = list.get(list.size()-1);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Alert!")
                .setContentText(var)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(var))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);

        // notificationId is a unique int for each notification that you must define
        int notificationId = list.size()-1;
        notificationManager.notify(notificationId, builder.build());
        return;
    }

    private void showActi(String msg, ArrayAdapter<String> adapter, List<String> ListElementsArrayList){
        ListElementsArrayList.add(msg);
        adapter.notifyDataSetChanged();
        return;
    }


    /** Called when the user taps the fab */
    public void getHistory(View view) {


        Intent intent = new Intent(this, HistoryActivity.class);
        String message = "Please wait.";
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }


}