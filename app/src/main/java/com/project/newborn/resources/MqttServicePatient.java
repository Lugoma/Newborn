package com.project.newborn.resources;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.Random;


public class MqttServicePatient extends IntentService {

    private String ip, port;
    private IMqttAsyncClient client;
    private String clientId;
    private String incubatorId;
    private String incubatorName;
    private ArrayList<String> activeTopics = new ArrayList<>();

    public MqttServicePatient() {
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ip = intent.getStringExtra("ip");
        port = intent.getStringExtra("port");
        incubatorId = intent.getStringExtra("incubatorId");
        incubatorName = intent.getStringExtra("incubatorName");
        activeTopics = (ArrayList<String>) intent.getSerializableExtra("activeTopics");

        setClientId();
        connect();
    }

    private void setClientId() {

        Random rand = new Random();
        clientId = incubatorId + incubatorName + rand.nextInt(100);
    }

    /**
     * Method to connect and start communication with MQTT broker
     */
    private void connect() {

        String broker = URLS.parseURLWithPortTCP(ip, port);
        IMqttToken token;
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(1000);
        String errorMessage = "Se ha dado un problema con el servicio mqtt";

        try {
            client = new MqttAsyncClient(broker, clientId, new MemoryPersistence());
            token = client.connect(options);
            token.waitForCompletion(3500);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    try {
                        client.disconnectForcibly();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage msg) {

                    Intent intentBroadcast = new Intent();
                    intentBroadcast.putExtra("topic", topic.replace(incubatorName, ""));
                    intentBroadcast.putExtra("message", msg.toString());
                    intentBroadcast.putExtra("incubatorName", incubatorName);
                    intentBroadcast.setAction("com.example.incubadora");
                    sendBroadcast(intentBroadcast);

                    SharedPreferences preferences = getSharedPreferences("sensor_values", MODE_PRIVATE);
                    String disconnect = preferences.getString("stop_service", "");
                    if(!TextUtils.isEmpty(disconnect)) {
                        try {
                            client.disconnectForcibly();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
            });

            for(String topic : activeTopics) {
                client.subscribe(topic+incubatorName, 0);
            }

        } catch (Exception e) {
            ContextCompat.getMainExecutor(getApplicationContext()).execute(() -> {
                Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
            });
        }
    }
}
