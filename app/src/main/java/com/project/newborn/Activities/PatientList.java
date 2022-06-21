package com.project.newborn.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.project.newborn.DTO.AlarmDTO;
import com.project.newborn.DTO.IncubatorDTO;
import com.project.newborn.DTO.PatientDTO;
import com.project.newborn.DTO.SensorDTO;
import com.project.newborn.DTO.TopicDTO;
import com.project.newborn.R;
import com.project.newborn.httpInterfaces.HttpHandler;
import com.project.newborn.resources.BusinessException;
import com.project.newborn.resources.MqttServiceAlarm;
import com.project.newborn.resources.URLS;
import com.project.newborn.resources.UtilViews;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PatientList extends Activity {

    private JSONArray patientList = new JSONArray();
    private TableLayout tblyPatientList;
    private Button btnIncubatorList;
    private Context context;
    private FloatingActionButton btnNewPatient;
    private final HashMap<String, List<HashMap<String, String>>> alarmData = new HashMap<>();

    @Override
    public void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        setContentView(R.layout.patient_list);
        context = this;

        tblyPatientList = findViewById(R.id.tblyIncubatorsList);
        btnIncubatorList = findViewById(R.id.btnIncubatorList);
        btnNewPatient = findViewById(R.id.btnNewPatient);

        setBtnNewPatient();
        setBtnIncubatorList();
        startMQTTService();
    }

    @Override
    protected void onResume() {

        super.onResume();
        requestPatientList();
    }

    /**
     * Run all method calls to start MQTT service
     */
    private void startMQTTService() {

        // start mqtt service
        requestMQTTAlarmData();
    }

    /**
     * Request all data from alarms to launch MQTT service
     */
    private void requestMQTTAlarmData() {

        String errorMessage = "Se ha producido un error al iniciar el servicio MQTT";
        try {
            HttpHandler.arrayObjectGETRequest(URLS.URL_ALARMA + "/", alarmList -> {
                try {
                    for (int i=0; i<alarmList.length(); ++i) {
                        int index = i;
                        AlarmDTO alarm = AlarmDTO.parseJSONToDTO(alarmList.getJSONObject(i));
                        HttpHandler.simpleObjectGETRequest(URLS.URL_SENSOR + "/" + alarm.getSensorId(), sensorJSON -> {
                            try {
                                SensorDTO sensor = SensorDTO.parseJSONToDTO(sensorJSON);
                                if (sensor.isActive()) {
                                    HttpHandler.simpleObjectGETRequest(URLS.URL_TOPIC + "/" + sensor.getSensorTypeId(), topicJSON -> {
                                        try {
                                            TopicDTO topic = TopicDTO.parseJSONToDTO(topicJSON);
                                            HttpHandler.simpleObjectGETRequest(URLS.URL_INCUBADORA + "/" + sensor.getIncubatorId(), incubatorJSON -> {
                                                try {
                                                    IncubatorDTO incubator = IncubatorDTO.parseJSONToDTO(incubatorJSON);
                                                    if (incubator.isActive()) {
                                                        HashMap<String, String> data = new HashMap<>();
                                                        data.put("limite_superior", alarm.getMaxLimit());
                                                        data.put("limite_inferior", alarm.getMinLimit());
                                                        data.put("topic", topic.getName());

                                                        if (alarmData.containsKey(incubator.getName()))
                                                            alarmData.get(incubator.getName()).add(data);
                                                        else {
                                                            List<HashMap<String, String>> list = new ArrayList<>();
                                                            list.add(data);
                                                            alarmData.put(incubator.getName(), list);
                                                        }
                                                        if (index == alarmList.length() - 1)
                                                            launchMQTTService();
                                                    }
                                                } catch (Exception e) {
                                                    new BusinessException(errorMessage, context).showErrorMessage();
                                                }
                                            }, context);
                                        } catch (Exception e) {
                                            new BusinessException(errorMessage, context).showErrorMessage();
                                        }
                                    }, context);
                                }

                            } catch (Exception e) {
                                new BusinessException(errorMessage, context).showErrorMessage();
                            }
                        }, context);
                    }
                } catch (Exception e) {
                    new BusinessException(errorMessage, context).showErrorMessage();
                }
            }, context);
        } catch (Exception e) {
            new BusinessException(errorMessage, context).showErrorMessage();
        }
    }

    /**
     * Launch MQTT service with all collected data
     */
    private void launchMQTTService() {

        Intent myMqttServiceIntent = new Intent(this, MqttServiceAlarm.class);
        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        String ipAddress = preferences.getString("ipAddress", "");
        myMqttServiceIntent.putExtra("ip", ipAddress);
        myMqttServiceIntent.putExtra("port", URLS.MQTT_BROKER_PORT);
        myMqttServiceIntent.putExtra("alarmData", alarmData);

        startService(myMqttServiceIntent);
    }

    /**
     * Request and show all patients in system
     */
    private void requestPatientList() {

        tblyPatientList.removeAllViews();
        HttpHandler.arrayObjectGETRequest(URLS.URL_PACIENTE + "/", responseData -> {
            patientList = responseData;
            tblyPatientList.addView(setUpPatientListLegend());
            for(int i=0 ; i<patientList.length() ; ++i) {
                TableRow tableRow = new TableRow(context);

                TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                tableRowParams.setMargins(3,3,3,3);
                tableRow.setLayoutParams(tableRowParams);
                tableRow.setBackgroundResource(R.color.white);

                try {
                    PatientDTO patient = PatientDTO.parseJSONToDTO(patientList.getJSONObject(i));

                    tableRow.addView(UtilViews.setUpTextView(context, patient.getPatientFullName(), 16, Color.BLACK));
                    tableRow.addView(UtilViews.setUpGoToButton(context, "ir a", patient.getId(), Patient.class));
                    tblyPatientList.addView(tableRow);
                } catch (Exception e) {
                    BusinessException exception = new BusinessException("Error al recuperar la lista de pacientes", context);
                    exception.showErrorMessage();
                }
            }
        }, context);
    }

    /**
     * Configure patient list table legend
     * @return
     */
    private View setUpPatientListLegend() {

        TableRow tableRow = new TableRow(context);
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRowParams.setMargins(3, 3,3,3);
        tableRow.setLayoutParams(tableRowParams);
        tableRow.setBackgroundResource(R.color.white);

        tableRow.addView(UtilViews.setUpTextView(context, "Nombre del paciente",20, Color.BLACK));
        tableRow.addView(UtilViews.setUpTextView(context, "",24, Color.BLACK));

        return tableRow;
    }

    /**
     * Set up button add new patient
     */
    private void setBtnNewPatient() {

        btnNewPatient.setOnClickListener(view -> {
            Intent intent = new Intent(context, Patient.class);
            startActivity(intent);
        });
    }

    /**
     * Set up button access to incubator list
     */
    private void setBtnIncubatorList() {

        btnIncubatorList.setOnClickListener(view -> {
            Intent intent = new Intent(context, IncubatorList.class);
            startActivity(intent);
        });
    }
}
