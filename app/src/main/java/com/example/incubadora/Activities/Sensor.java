package com.example.incubadora.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.incubadora.DTO.AlarmDTO;
import com.example.incubadora.DTO.SensorDTO;
import com.example.incubadora.DTO.TopicDTO;
import com.example.incubadora.R;
import com.example.incubadora.httpInterfaces.HttpHandler;
import com.example.incubadora.resources.BusinessException;
import com.example.incubadora.resources.URLS;
import com.example.incubadora.resources.UtilViews;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class Sensor extends Activity {

    private Integer sensorId, incubatorId;
    private Context context;
    private EditText etSensorNumber, etSensorName;
    private Spinner spinnerSensorType;
    private Button btnSave, btnRemove, btnActivateSensor, newAlarm;
    private TextView tvSensorStatus;
    private LinearLayout lytStatus, lytAlarmTitle, lytNewAlarm;
    private TableLayout tableAlarmList;
    private ArrayAdapter<String> arrayAdapter;
    private List<String> topicArray = new ArrayList<>();
    private List<String> topicIdArray = new ArrayList<>();


    @Override
    public void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        setContentView(R.layout.sensor);

        Bundle bundleActivity = getIntent().getExtras();
        if(bundleActivity != null) {
            incubatorId = bundleActivity.getInt("incubatorId");
            String id = bundleActivity.getString("id");
            if(id != null)
                sensorId = Integer.parseInt(id);
        }

        context = this;
        etSensorNumber = findViewById(R.id.etSensorNumber);
        etSensorName = findViewById(R.id.etSensorName);
        spinnerSensorType = findViewById(R.id.spinnerSensorType);
        btnSave = findViewById(R.id.btnSave);
        btnRemove = findViewById(R.id.btnRemove);
        btnActivateSensor = findViewById(R.id.btnActivateSensor);
        tvSensorStatus = findViewById(R.id.tvSensorStatus);
        lytStatus = findViewById(R.id.lytStatus);
        newAlarm = findViewById(R.id.newAlarm);
        lytNewAlarm = findViewById(R.id.lytNewAlarm);
        lytAlarmTitle = findViewById(R.id.lytAlarmTitle);
        tableAlarmList = findViewById(R.id.tableAlarmList);

        setSaveSensorButton();
        setRemoveSensorButton();
        setNewAlarmButton();
    }

    @Override
    public void onResume() {

        super.onResume();
        loadAllTopics();
    }

    /**
     * Method to request al topics from server
     */
    private void loadAllTopics() {

        HttpHandler.arrayObjectGETRequest(URLS.URL_TOPIC + "/", this::fillSpinner, context);
    }

    /**
     * Method to fill spinner with all topics
     * @param responseData
     */
    private void fillSpinner(JSONArray responseData) {
        try {
            topicIdArray = new ArrayList<>();
            topicArray = new ArrayList<>();
            for (int i = 0; i < responseData.length(); ++i) {
                TopicDTO topic = TopicDTO.parseJSONToDTO(responseData.getJSONObject(i));
                topicArray.add(topic.getName());
                topicIdArray.add(topic.getId());
            }

            arrayAdapter = new ArrayAdapter<>(context, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, topicArray);
            spinnerSensorType.setAdapter(arrayAdapter);
            if (sensorId != null) {
                requestSensor();
            } else {
                lytStatus.setVisibility(View.GONE);
                btnRemove.setVisibility(View.GONE);
                lytAlarmTitle.setVisibility(View.GONE);
                lytNewAlarm.setVisibility(View.GONE);
                tableAlarmList.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            BusinessException exception = new BusinessException("Error al recuperar la lista de topics", context);
            exception.showErrorMessage();
        }
    }

    /**
     * Method to request all sensor data from server
     */
    private void requestSensor() {

        String url = URLS.URL_SENSOR + "/" + sensorId;
        HttpHandler.simpleObjectGETRequest(url, responseData -> {
            try {
                SensorDTO sensor = SensorDTO.parseJSONToDTO(responseData);
                showSensorData(sensor);
                requestAlarm(sensor.getId());


            } catch (BusinessException e) {
                if(e.emptyContext())
                    e.setContext(context);
                e.showErrorMessage();
            }
        }, context);
    }

    /**
     * Method to show all sensor data from sensorDTO
     * @param sensor
     */
    private void showSensorData(SensorDTO sensor) {

        etSensorNumber.setText(sensor.getNumber());
        etSensorName.setText(sensor.getName());
        String topic = topicArray.get(topicIdArray.indexOf(sensor.getSensorTypeId()));
        spinnerSensorType.setSelection(arrayAdapter.getPosition(topic));
        String status = "Estado: " + (sensor.isActive() ? "Activado" : "Desactivado");
        tvSensorStatus.setText(status);
        setUpActivateSensorButton(sensor.isActive());
    }

    /**
     * Method to request sensor alarm
     * @param sensorId
     */
    private void requestAlarm(String sensorId) {

        String urlIncubator = URLS.URL_SENSOR + "/" + sensorId + URLS.URL_ALARMA;
        HttpHandler.simpleObjectGETRequest(urlIncubator, requestData -> {
            try {
                if(requestData != null && !requestData.isNull("id")) {
                    showAlarmData(requestData);
                    lytNewAlarm.setVisibility(View.GONE);
                } else {
                    tableAlarmList.setVisibility(View.GONE);
                    lytNewAlarm.setVisibility(View.VISIBLE);
                }

            } catch (BusinessException e) {
                if(e.emptyContext())
                    e.setContext(context);
                e.showErrorMessage();
            }
        }, context);
    }

    private void showAlarmData(JSONObject requestData) throws BusinessException{

        try {
            tableAlarmList.removeAllViews();
            tableAlarmList.setVisibility(View.VISIBLE);
            tableAlarmList.addView(setUpAlarmLegend());
            AlarmDTO alarm = AlarmDTO.parseJSONToDTO(requestData);

            TableRow tableRow = new TableRow(context);
            TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            tableRowParams.setMargins(3, 3,3,3);
            tableRow.setLayoutParams(tableRowParams);
            tableRow.setBackgroundResource(R.color.white);
            tableRow.addView(UtilViews.setUpTextView(context, alarm.getMaxLimit(), 24, Color.BLACK));
            tableRow.addView(UtilViews.setUpTextView(context, alarm.getMinLimit(), 24, Color.BLACK));
            tableRow.addView(UtilViews.setUpGoToButton(context, "ir a", alarm.getId(), Alarm.class));
            tableAlarmList.addView(tableRow);

        } catch (Exception e) {
            throw new BusinessException("Ha ocurrido un error al mostrar los datos, vuelva a intentarlo");
        }
    }

    /**
     * Configure alarm table legend
     * @return
     */
    private View setUpAlarmLegend() {

        TableRow tableRow = new TableRow(context);
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRowParams.setMargins(3, 3,3,3);
        tableRow.setLayoutParams(tableRowParams);
        tableRow.setBackgroundResource(R.color.white);

        tableRow.addView(UtilViews.setUpTextView(context, "Limite Superior",18, Color.BLACK));
        tableRow.addView(UtilViews.setUpTextView(context, "Limite Inferior",18, Color.BLACK));
        tableRow.addView(UtilViews.setUpTextView(context, "",12, Color.BLACK));

        return tableRow;
    }

    /**
     * Set up button to activate or deactivate a sensor
     * @param active
     */
    private void setUpActivateSensorButton(Boolean active) {

        String text = active ? "Desactivar" : "Activar";
        btnActivateSensor.setText(text);
        btnActivateSensor.setOnClickListener(view -> {
            swapSensorStatus(!active);
            recreate();
        });
    }

    /**
     * Method to make server request with sensor status change
     * @param newStatus
     */
    private void swapSensorStatus(boolean newStatus) {

        SensorDTO sensor = new SensorDTO();
        sensor.setId(String.valueOf(sensorId));
        String relativeUrl = URLS.URL_SENSOR + "/" + sensorId;
        try {
            RequestParams requestParams = SensorDTO.parseDTOtoRequestParams(sensor);
            requestParams.put("activo", newStatus);
            HttpHandler.patch(relativeUrl, requestParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {}
            });
        } catch (BusinessException e) {
            BusinessException exception = new BusinessException("Error al cambiar el estado del sensor", context);
            exception.showErrorMessage();
        }
    }

    /**
     * Method to configure save button
     */
    private void setSaveSensorButton() {

        btnSave.setOnClickListener(view -> {
            if(sensorId == null) {
                createNewSensor();
            } else {
                updateSensor();
            }
        });
    }

    /**
     * Method to create and post new sensor
     */
    private void createNewSensor() {

        try {
            SensorDTO sensor = collectSensorData();
            sensor.setIncubatorId(String.valueOf(incubatorId));
            RequestParams requestParams = SensorDTO.parseDTOtoRequestParams(sensor);
            requestParams.put("activo", false);

            HttpHandler.post(URLS.URL_SENSOR + "/", requestParams, new JsonHttpResponseHandler() {
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
     * Method to update sensor data
     */
    private void updateSensor() {

        try {
            SensorDTO sensor = collectSensorData();
            RequestParams requestParams = SensorDTO.parseDTOtoRequestParams(sensor);
            String relativeUrl = URLS.URL_SENSOR + "/" + sensorId;

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
     * Method to collect sensor data from interface
     * @return SensorDTO
     * @throws BusinessException
     */
    private SensorDTO collectSensorData() throws BusinessException {

        SensorDTO sensor = new SensorDTO();

        if(validateData()) {
            if(numberIsDigit()) {
                sensor.setNumber(etSensorNumber.getText().toString());
                sensor.setName(etSensorName.getText().toString());
                String topic = spinnerSensorType.getSelectedItem().toString();
                sensor.setSensorType(topic);
                sensor.setSensorTypeId(topicIdArray.get(topicArray.indexOf(topic)));
            } else {
                throw new BusinessException("El campo Número debe ser un dígito");
            }
        } else {
            throw new BusinessException("Se debe completar todos los campos");
        }
        return sensor;
    }

    /**
     * Method to validate all user-inserted data. Checks if any is empty
     * @return Boolean
     */
    private Boolean validateData() {

        if(TextUtils.isEmpty(etSensorNumber.getText().toString()))
            return false;
        if(TextUtils.isEmpty(etSensorName.getText().toString()))
            return false;
        return !TextUtils.isEmpty(spinnerSensorType.getSelectedItem().toString());
    }

    /**
     * Method to validate if number field is a digit
     * @return
     */
    private Boolean numberIsDigit() {

        return TextUtils.isDigitsOnly(etSensorNumber.getText());
    }

    /**
     * Configure remove sensor button
     */
    private void setRemoveSensorButton() {

        String url = URLS.URL_SENSOR + "/"+ sensorId + URLS.URL_ALARMA;
        HttpHandler.simpleObjectGETRequest(url, requestData -> {
            if(requestData != null && !requestData.isNull("id")) {
                btnRemove.setOnClickListener(view -> {
                    BusinessException exception = new BusinessException("No se puede eliminar un sensor con una alarma configurada", context);
                    exception.showErrorMessage();
                });
            } else {
                btnRemove.setOnClickListener(view -> HttpHandler.delete(URLS.URL_SENSOR + "/" + sensorId, null, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        finish();
                        Toast.makeText(context, "Sensor eliminado correctamente", Toast.LENGTH_SHORT).show();
                    }
                }));
            }
        }, context);
    }

    private void setNewAlarmButton() {
        newAlarm.setOnClickListener(view -> {
            Intent intent = new Intent(context, Alarm.class);
            intent.putExtra("sensorId", sensorId);
            startActivity(intent);
        });
    }
}
