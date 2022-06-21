package com.project.newborn.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.project.newborn.DTO.AlarmDTO;
import com.project.newborn.DTO.CameraDTO;
import com.project.newborn.DTO.IncubatorDTO;
import com.project.newborn.DTO.PatientDTO;
import com.project.newborn.DTO.SensorDTO;
import com.project.newborn.DTO.TopicDTO;
import com.project.newborn.R;
import com.project.newborn.httpInterfaces.HttpHandler;
import com.project.newborn.resources.BusinessException;
import com.project.newborn.resources.DateManager;
import com.project.newborn.resources.MqttServicePatient;
import com.project.newborn.resources.URLS;
import com.project.newborn.resources.UtilViews;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class Patient extends Activity {

    private EditText etPatientName, etPatientSurname1, etPatientSurname2, etPatientBirthDate;
    private TextView tvNumberIncubator;
    private Integer patientId;
    private Button btnSave, btnRemove, btnUnassign, btnAssign;
    private LinearLayout lytIncubator, lytCameraTitle, lytSensorReceivedData, lytSensorData;
    private TableLayout tableCameraList;
    private Context context;
    private final ArrayList<String> activeTopics = new ArrayList<>();

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String topic = intent.getStringExtra("topic");
            String message = intent.getStringExtra("message");
            String incubatorName = intent.getStringExtra("incubatorName");
            if (TextUtils.isEmpty(message))
                message = "No detectado";

            SharedPreferences preferences = getSharedPreferences("sensor_values", MODE_PRIVATE);
            String textViewId = preferences.getString(topic+incubatorName, "");
            String text = "Sensor " + topic + ": " + message;
            if(!TextUtils.isEmpty(textViewId)) {
                TextView textView = findViewById(Integer.parseInt(textViewId));
                textView.setText(text);
            }
        }
    };

    @Override
    public void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        setContentView(R.layout.patient);

        Bundle bundleActivity = getIntent().getExtras();
        if(bundleActivity != null) {
            String id = bundleActivity.getString("id");
            patientId = Integer.parseInt(id);
        }

        context = this;
        etPatientName = findViewById(R.id.etPatientName);
        etPatientSurname1 = findViewById(R.id.etPatientSurname1);
        etPatientSurname2 = findViewById(R.id.etPatientSurname2);
        etPatientBirthDate = findViewById(R.id.etPatientBirthDate);
        btnSave = findViewById(R.id.btnSave);
        btnRemove = findViewById(R.id.btnRemove);
        btnUnassign = findViewById(R.id.btnUnassign);
        btnAssign = findViewById(R.id.btnAssign);
        tvNumberIncubator = findViewById(R.id.tvNumberIncubator);
        lytIncubator = findViewById(R.id.lytIncubator);
        lytCameraTitle = findViewById(R.id.lytcameraTitle);
        lytSensorReceivedData = findViewById(R.id.lytSensorReceivedData);
        tableCameraList = findViewById(R.id.tableCameraList);
        lytSensorData = findViewById(R.id.lytSensorData);

        setSavePatientButton();
    }

    /**
     * Launch MQTT service with all collected data
     */
    private void launchMQTTService(String incubatorId, String incubatorName) {

        Intent myMqttServiceIntent = new Intent(this, MqttServicePatient.class);
        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        String ipAddress = preferences.getString("ipAddress", "");
        myMqttServiceIntent.putExtra("ip", ipAddress);
        myMqttServiceIntent.putExtra("port", URLS.MQTT_BROKER_PORT);
        myMqttServiceIntent.putExtra("incubatorId", incubatorId);
        myMqttServiceIntent.putExtra("incubatorName", incubatorName);
        myMqttServiceIntent.putExtra("activeTopics", activeTopics);

        startService(myMqttServiceIntent);
    }

    @Override
    protected void onResume() {

        super.onResume();
        SharedPreferences preferences = getSharedPreferences("sensor_values", MODE_PRIVATE);
        preferences.edit().clear().apply();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.incubadora");
        registerReceiver(broadcastReceiver, intentFilter);

        if (patientId != null) {
            requestPatient();
        } else {
            lytIncubator.setVisibility(View.GONE);
            lytCameraTitle.setVisibility(View.GONE);
            tableCameraList.setVisibility(View.GONE);
            lytSensorReceivedData.setVisibility(View.GONE);
            btnRemove.setVisibility(View.GONE);
        }
    }

    /**
     * Method to request all patient data from server
     */
    private void requestPatient() {

        String url = URLS.URL_PACIENTE + "/" + patientId;
        HttpHandler.simpleObjectGETRequest(url, responseData -> {
            try {
                PatientDTO patient = PatientDTO.parseJSONToDTO(responseData);
                showPatientData(patient);
                if(!TextUtils.isEmpty(patient.getIncubatorId())) {
                    String url_incubator = url + URLS.URL_INCUBADORA;
                    HttpHandler.simpleObjectGETRequest(url_incubator, requestData -> {
                        try {
                            IncubatorDTO incubator = IncubatorDTO.parseJSONToDTO(requestData);
                            showIncubatorData(incubator);
                            setBtnAssign();
                            setBtnUnassign();
                            if(incubator.isActive()) {
                                showSensorReceivedData(incubator.getId(), incubator.getName());
                            } else {
                                hideSensorData();
                            }
                        } catch (BusinessException e) {
                            BusinessException exception = new BusinessException("Error al recuperar la incubadora del paciente", context);
                            exception.showErrorMessage();
                        }
                    }, context);
                    String url_camera = URLS.URL_INCUBADORA + "/" + patient.getIncubatorId() + URLS.URL_CAMARA;
                    HttpHandler.arrayObjectGETRequest(url_camera, requestData -> {
                        try {
                            showCameraData(requestData);
                        } catch (BusinessException e) {
                            if(e.emptyContext())
                                e.setContext(context);
                            e.showErrorMessage();
                        }
                    }, context);
                    setRemovePatientButton(true);
                }
                else {
                    hideIncubatorData();
                    hideCameraData();
                    hideSensorData();
                    setBtnAssign();
                    setRemovePatientButton(false);
                }

            } catch (BusinessException e) {
                if(e.emptyContext())
                    e.setContext(context);
                e.showErrorMessage();
            }
        }, context);
    }

    private void showSensorReceivedData(String incubatorId, String incubatorName) {

        String url_sensor = URLS.URL_INCUBADORA + "/" + incubatorId + URLS.URL_SENSOR_ACTIVO;
        lytSensorData.removeAllViews();

        HttpHandler.arrayObjectGETRequest(url_sensor, requestData -> {
            try {
                for (int i=0 ; i<requestData.length() ; ++i) {
                    lytSensorReceivedData.setVisibility(View.VISIBLE);
                    SensorDTO sensor = SensorDTO.parseJSONToDTO(requestData.getJSONObject(i));
                    if (sensor.isActive()) {
                        String url_topic = URLS.URL_TOPIC + "/" + sensor.getSensorTypeId();
                        HttpHandler.simpleObjectGETRequest(url_topic, topicJSON -> {
                            try {
                                TopicDTO topic = TopicDTO.parseJSONToDTO(topicJSON);

                                TextView textView = new TextView(context);
                                textView.setId(findUnusedId());
                                String text = "Sensor " + topic.getName() + ": " + "No detectado";
                                textView.setText(text);
                                lytSensorData.addView(textView);

                                SharedPreferences preferences = getSharedPreferences("sensor_values", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(topic.getName() + incubatorName, String.valueOf(textView.getId()));
                                editor.apply();

                                activeTopics.add(topic.getName());
                                if (activeTopics.size() == requestData.length())
                                    launchMQTTService(incubatorId, incubatorName);

                            } catch (BusinessException e) {
                                if (e.emptyContext())
                                    e.setContext(context);
                                e.showErrorMessage();
                            }
                        }, context);
                    }
                }
            } catch (Exception e) {
                new BusinessException("Se ha producido un error al recuperar los datos, vuelva a intentarlo", context).showErrorMessage();
            }
        }, context);
    }

    /**
     * Method to show all patient data from patientDTO
     * @param patient
     */
    private void showPatientData(PatientDTO patient) {

        etPatientName.setText(patient.getName());
        etPatientSurname1.setText(patient.getSurname1());
        etPatientSurname2.setText(patient.getSurname2());
        etPatientBirthDate.setText(DateManager.parseDateToClientFormat(patient.getBirthDate()));
    }

    /**
     * Method to set up screen when incubator is assigned
     * @param incubator
     */
    private void showIncubatorData(IncubatorDTO incubator) {

        tvNumberIncubator.setText(incubator.getNumber());
        btnUnassign.setVisibility(View.VISIBLE);
        btnAssign.setVisibility(View.INVISIBLE);
    }

    /**
     * Method to show camera table
     * @param cameraList
     * @throws BusinessException
     */
    private void showCameraData(JSONArray cameraList) throws BusinessException{
        try {
            lytCameraTitle.setVisibility(View.VISIBLE);
            tableCameraList.setVisibility(View.VISIBLE);
            tableCameraList.removeAllViews();
            tableCameraList.addView(setUpCameraStreamListLegend());
            for(int i=0 ; i<cameraList.length() ; ++i) {
                CameraDTO camera = CameraDTO.parseJSONToDTO(cameraList.getJSONObject(i));
                TableRow tableRow = new TableRow(context);
                TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                tableRowParams.setMargins(3,3,3,3);
                tableRow.setLayoutParams(tableRowParams);
                tableRow.setBackgroundResource(R.color.white);

                tableRow.addView(setUpCameraTextView(context, "Camara " + camera.getNumber() + " " + CameraDTO.getCameraType(camera.getTypeId())));
                String active = camera.isActive() ? "Activada" : "Desactivada";
                tableRow.addView(setUpCameraTextView(context, active));
                if(camera.isActive()) {
                    tableRow.addView(setUpStreamIconView(context, camera.getIncubatorId(), camera.getId()));
                }

                tableCameraList.addView(tableRow);
            }
        } catch (Exception e) {
            throw new BusinessException("Ha ocurrido un error al mostrar los datos, vuelva a intentarlo");
        }
    }

    /**
     * Configure camera stram list table legend
     * @return
     */
    private View setUpCameraStreamListLegend() {

        TableRow tableRow = new TableRow(context);
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRowParams.setMargins(3, 3,3,3);
        tableRow.setLayoutParams(tableRowParams);
        tableRow.setBackgroundResource(R.color.white);

        tableRow.addView(UtilViews.setUpTextView(context, "Nombre",20, Color.BLACK));
        tableRow.addView(UtilViews.setUpTextView(context, "Estado",20, Color.BLACK));
        tableRow.addView(UtilViews.setUpTextView(context, "",20, Color.BLACK));

        return tableRow;
    }

    /**
     * Configure text in camera list table
     * @param context
     * @param text
     * @return
     */
    private TableRow setUpCameraTextView(Context context, String text) {

        TableRow layout = new TableRow(context);
        layout.setBackgroundResource(R.color.black);

        TextView textView = new TextView(context);

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
        layoutParams.setMargins(1,1,1,1);
        textView.setText(text);
        textView.setLayoutParams(layoutParams);
        textView.setTextSize(20);
        textView.setBackgroundColor(Color.WHITE);
        textView.setGravity(Gravity.START);
        layout.addView(textView);

        return layout;
    }

    /**
     * Method to configure stream button.
     * @param context
     * @param incubatorId
     * @param cameraId
     * @return
     */
    private ImageButton setUpStreamIconView(Context context, String incubatorId, String cameraId) {
        ImageButton btnWatchCamera = new ImageButton(context);

        btnWatchCamera.setBackgroundResource(R.color.purple_700);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
        layoutParams.setMargins(1,1,1,1);
        layoutParams.width = TableRow.LayoutParams.MATCH_PARENT;
        layoutParams.height = TableRow.LayoutParams.MATCH_PARENT;
        btnWatchCamera.setLayoutParams(layoutParams);

        btnWatchCamera.setImageResource(R.drawable.eye_icon);
        btnWatchCamera.setOnClickListener(view -> {
            Intent intent = new Intent(context, CameraStream.class);
            intent.putExtra("incubatorId", incubatorId);
            intent.putExtra("cameraId", cameraId);
            startActivity(intent);
        });

        return btnWatchCamera;
    }

    /**
     * Method to set up screen when incubator is not assigned
     */
    private void hideIncubatorData() {

        tvNumberIncubator.setText("No");
        btnUnassign.setVisibility(View.INVISIBLE);
        btnAssign.setText("Asignar");
    }

    /**
     * Method to hide all camara data when incubator is not assigned
     */
    private void hideCameraData() {

        lytCameraTitle.setVisibility(View.GONE);
        tableCameraList.setVisibility(View.GONE);
    }

    /**
     * Method to hide all sensor data when incubator is not assigned
     */
    private void hideSensorData() {

        lytSensorReceivedData.setVisibility(View.GONE);
    }

    /**
     * Method to configure save button
     */
    private void setSavePatientButton() {

        btnSave.setOnClickListener(view -> {
            if(patientId == null) {
                createNewPatient();
            } else {
                updatePatient();
            }
        });
    }

    /**
     * Method to configure assign button
     */
    private void setBtnAssign() {

        btnAssign.setOnClickListener(view -> {
            Intent intent = new Intent(context, AssignIncubator.class);
            intent.putExtra("patientId", String.valueOf(patientId));
            startActivity(intent);
        });
    }

    /**
     * Method to configure unassign button
     */
    private void setBtnUnassign() {

        btnUnassign.setOnClickListener(view -> {
            unassignIncubator();
            deleteAllAlarms();
            recreate();
        });
    }

    /**
     * Method to create and post new patient
     */
    private void createNewPatient() {

        try {
            PatientDTO patient = collectPatientData();
            RequestParams requestParams = PatientDTO.parseDTOtoRequestParams(patient);

            HttpHandler.post(URLS.URL_PACIENTE + "/", requestParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                finish();
                Toast.makeText(context, "Creado correctamente", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (BusinessException e) {
            if(e.emptyContext())
                e.setContext(context);
            e.showErrorMessage();
        }
    }

    /**
     * Method to update patient data
     */
    private void updatePatient() {

        try {
            PatientDTO patient = collectPatientData();
            RequestParams requestParams = PatientDTO.parseDTOtoRequestParams(patient);
            String relativeUrl = URLS.URL_PACIENTE + "/" + patientId;

            HttpHandler.patch(relativeUrl, requestParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                finish();
                Toast.makeText(context, "Guardado correctamente", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (BusinessException e) {
            if(e.emptyContext())
                e.setContext(context);
            e.showErrorMessage();
        }
    }

    /**
     * Method to collect patient data from interface
     * @return
     * @throws BusinessException
     */
    private PatientDTO collectPatientData() throws BusinessException {

        PatientDTO patient = new PatientDTO();

        if(validateData()) {
            patient.setName(etPatientName.getText().toString());
            patient.setSurname1(etPatientSurname1.getText().toString());
            patient.setSurname2(etPatientSurname2.getText().toString());
            try {
                patient.setBirthDate(DateManager.parseToDateFromClient(etPatientBirthDate.getText().toString()));
            } catch (Exception e) {
                throw new BusinessException("El formato de la fecha es incorrecto");
            }

        } else {
            throw new BusinessException("Se debe completar todos los campos");
        }
        return patient;
    }

    /**
     * Method to validate all user-inserted data. Checks if any is empty
     * @return
     */
    private Boolean validateData() {

        if(TextUtils.isEmpty(etPatientName.getText().toString()))
            return false;
        if(TextUtils.isEmpty(etPatientSurname1.getText().toString()))
            return false;
        if(TextUtils.isEmpty(etPatientSurname2.getText().toString()))
            return false;
        return !TextUtils.isEmpty(etPatientBirthDate.getText().toString());
    }

    /**
     * Method to unassign patient
     */
    private void unassignIncubator() {

        PatientDTO patient = new PatientDTO();
        patient.setId(String.valueOf(patientId));
        String relativeUrl = URLS.URL_PACIENTE + "/" + patientId;
        try {
            RequestParams requestParams = PatientDTO.parseDTOtoRequestParams(patient);
            requestParams.put("incubadora", "");

            HttpHandler.patch(relativeUrl, requestParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {}
            });

        } catch (BusinessException e) {
            BusinessException exception = new BusinessException("No se pudo desasignar la incubadora del paciente", context);
            exception.showErrorMessage();
        }
    }

    /**
     * Method to remove patient. Can only remove unassigned patients
     */
    private void setRemovePatientButton(Boolean assigned) {

        if(!assigned) {
            btnRemove.setOnClickListener(view -> HttpHandler.delete(URLS.URL_PACIENTE + "/" + patientId, null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    finish();
                    Toast.makeText(context, "Se ha dado de alta al paciente", Toast.LENGTH_SHORT).show();
                }
            }));
        } else {
            btnRemove.setOnClickListener(view -> {
                BusinessException exception = new BusinessException("No se puede dar de alta un paciente asignado a una incubadora", context);
                exception.showErrorMessage();
            });
        }
    }

    /**
     * Method to find an unused ID from uid
     * @return
     */
    private Integer findUnusedId() {

        Integer id = 0;
        while (findViewById(++id) != null);
        return id;
    }

    /**
     * Method to delete all incubator's alarm when patient is unassigned.
     */
    private void deleteAllAlarms() {

        String errorMessage = "Error al eliminar las alarmas asociadas a esta incubadora";
        String url = URLS.URL_PACIENTE + "/" + patientId;
        HttpHandler.simpleObjectGETRequest(url, patientData -> {
            try {
                PatientDTO patient = PatientDTO.parseJSONToDTO(patientData);
                HttpHandler.arrayObjectGETRequest(URLS.URL_INCUBADORA + "/" + patient.getIncubatorId() + URLS.URL_SENSOR, sensorList -> {
                    for(int i=0 ; i<sensorList.length() ; ++i) {
                        try {
                            SensorDTO sensor = SensorDTO.parseJSONToDTO(sensorList.getJSONObject(i));
                            HttpHandler.simpleObjectGETRequest(URLS.URL_SENSOR + "/" + sensor.getId() + URLS.URL_ALARMA, alarmData -> {
                                try {
                                    if (alarmData != null && !alarmData.isNull("id")) {
                                        AlarmDTO alarm = AlarmDTO.parseJSONToDTO(alarmData);
                                        HttpHandler.delete(URLS.URL_ALARMA + "/" + alarm.getId(), null, new JsonHttpResponseHandler() {
                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                                Toast.makeText(context, "Se han eliminado las alarmas de esta incubadora. Reinicia la aplicación para aplicar los cambios", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } catch (BusinessException e) {
                                    BusinessException exception = new BusinessException(errorMessage, context);
                                    exception.showErrorMessage();
                                }
                            }, context);
                        } catch (Exception e) {
                            BusinessException exception = new BusinessException(errorMessage, context);
                            exception.showErrorMessage();
                        }
                    }
                }, context);

            } catch (BusinessException e) {
                BusinessException exception = new BusinessException(errorMessage, context);
                exception.showErrorMessage();
            }
        }, context);
    }

    @Override
    protected void onStop() {

        super.onStop();
        stopMqttService();
    }

    /**
     * Stops MQTT background service
     */
    private void stopMqttService() {

        SharedPreferences preferences = getSharedPreferences("sensor_values", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("stop_service", "stop");
        editor.apply();
        stopService(new Intent(context, MqttServicePatient.class));
        unregisterReceiver(broadcastReceiver);
    }
}
