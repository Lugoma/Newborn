package com.example.incubadora.resources;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Intent;
import android.content.res.Configuration;
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

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MqttServiceAlarm extends IntentService {

    private String ip, port;
    private volatile IMqttAsyncClient client;
    private String clientId;
    private HashMap<String, List<HashMap<String, String>>> alarmData = new HashMap<>();

    public MqttServiceAlarm() {
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ip = intent.getStringExtra("ip");
        port = intent.getStringExtra("port");
        alarmData = (HashMap<String, List<HashMap<String, String>>>) intent.getSerializableExtra("alarmData");
        setClientId();
        connect();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        android.os.Debug.waitForDebugger();
        super.onConfigurationChanged(newConfig);
    }

    @SuppressLint("HardwareIds")
    private void setClientId() {

        clientId = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }

    private void connect() {

        String broker = URLS.parseURLWithPortTCP(ip, port);
        IMqttToken token;
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setAutomaticReconnect(true);
        options.setConnectionTimeout(1000);

        try {
            client = new MqttAsyncClient(broker, clientId, new MemoryPersistence());
            token = client.connect(options);
            token.waitForCompletion(3500);
            Set<String> incubators = alarmData.keySet();

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    try {
                        client.disconnectForcibly();
                        client.connect();
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void messageArrived(String topic, MqttMessage msg) {

                    for(String incubatorName : incubators) {
                        for(HashMap<String, String> map : Objects.requireNonNull(alarmData.get(incubatorName))) {
                            String fullTopic = map.get("topic") + incubatorName;
                            if(topic.equals(fullTopic)) {
                                String maxLimit = map.get("limite_superior");
                                String minLimit = map.get("limite_inferior");

                                double receivedValue = Double.parseDouble(msg.toString());
                                String limitBreakMessage = "El sensor " + map.get("topic") + " de la incubadora " + incubatorName + " ha superado el limite ";
                                if(receivedValue > Double.parseDouble(maxLimit)) {
                                    ContextCompat.getMainExecutor(getApplicationContext()).execute(() -> {
                                        Toast.makeText(getApplicationContext(), limitBreakMessage + "superior", Toast.LENGTH_LONG).show();
                                    });
                                } else if (receivedValue < Double.parseDouble(minLimit))
                                    ContextCompat.getMainExecutor(getApplicationContext()).execute(() -> {
                                        Toast.makeText(getApplicationContext(), limitBreakMessage + "inferior", Toast.LENGTH_LONG).show();
                                    });
                            }
                        }
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
            });

            for(String incubatorName : incubators) {
                for(HashMap<String, String> map : Objects.requireNonNull(alarmData.get(incubatorName))) {
                    String topic = map.get("topic");
                    client.subscribe(topic + incubatorName, 0);
                }
            }
        } catch (Exception e) {
            new BusinessException("Se ha dado un problema con el servicio mqtt", this).showErrorMessage();
        }
    }
}
