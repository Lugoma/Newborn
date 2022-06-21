package com.example.incubadora.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.incubadora.DTO.CameraDTO;
import com.example.incubadora.DTO.IncubatorDTO;
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

import cz.msebera.android.httpclient.Header;

public class Incubator extends Activity {

    private Integer incubatorId;
    private Context context;
    private EditText etIncubatorNumber, etIncubatorName, etIncubatorIPAddress;
    private Button btnSave, btnRemove, btnActivateIncubator, newSensor, newCamera;
    private TextView tvIncubatorStatus;
    private LinearLayout lytStatus, lytSensorTitle, lytCameraTitle, lytNewSensor, lytNewCamera;
    private TableLayout tableSensorList, tableCameraList;

    @Override
    public void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        setContentView(R.layout.incubator);

        Bundle bundleActivity = getIntent().getExtras();
        if(bundleActivity != null) {
            String id = bundleActivity.getString("id");
            incubatorId = Integer.parseInt(id);
        }

        context = this;
        etIncubatorNumber = findViewById(R.id.etIncubatorNumber);
        etIncubatorName = findViewById(R.id.etIncubatorName);
        etIncubatorIPAddress = findViewById(R.id.etIncubatorIPAddress);
        btnActivateIncubator = findViewById(R.id.btnActivateIncubator);
        tvIncubatorStatus = findViewById(R.id.tvIncubatorStatus);
        btnSave = findViewById(R.id.btnSave);
        btnRemove = findViewById(R.id.btnRemove);
        lytStatus = findViewById(R.id.lytStatus);
        lytSensorTitle = findViewById(R.id.lytSensorTitle);
        lytCameraTitle = findViewById(R.id.lytCameraTitle);
        lytNewSensor = findViewById(R.id.lytNewSensor);
        lytNewCamera = findViewById(R.id.lytNewCamera);
        tableSensorList = findViewById(R.id.tableSensorList);
        tableCameraList = findViewById(R.id.tableCameraList);
        newSensor = findViewById(R.id.newSensor);
        newCamera = findViewById(R.id.newCamera);

        setSaveIncubatorButton();
        setNewSensorButton();
        setNewCameraButton();
    }

    @Override
    protected void onResume() {

        super.onResume();
        if (incubatorId != null) {
            setRemoveIncubatorButton();
            requestIncubator();
        } else {
            lytStatus.setVisibility(View.GONE);
            lytSensorTitle.setVisibility(View.GONE);
            lytCameraTitle.setVisibility(View.GONE);
            lytNewSensor.setVisibility(View.GONE);
            lytNewCamera.setVisibility(View.GONE);
            tableSensorList.setVisibility(View.GONE);
            tableCameraList.setVisibility(View.GONE);
            btnRemove.setVisibility(View.GONE);
        }
    }

    /**
     * Method to request all incubator data from server
     */
    private void requestIncubator() {

        String url = URLS.URL_INCUBADORA + "/" + incubatorId;
        HttpHandler.simpleObjectGETRequest(url, responseData -> {
            try {
                IncubatorDTO incubator = IncubatorDTO.parseJSONToDTO(responseData);
                showIncubatorData(incubator);
                requestSensors(incubator.getId());
                requestCameras(incubator.getId());

            } catch (BusinessException e) {
                BusinessException exception = new BusinessException("Hay un error en los datos recuperados del servidor", context);
                exception.showErrorMessage();
            }
        }, context);
    }

    /**
     * Method to show all incubator data from incubatorDTO
     * @param incubator
     */
    private void showIncubatorData(IncubatorDTO incubator) {

        etIncubatorNumber.setText(incubator.getNumber());
        etIncubatorName.setText(incubator.getName());
        etIncubatorIPAddress.setText(incubator.getIpAddress());
        String status = "Estado: " + (incubator.isActive() ? "Activada" : "Desactivada");
        tvIncubatorStatus.setText(status);
        setUpActivateIncubatorButton(incubator.isActive());
    }

    /**
     * Method to request all incubator sensors data
     * @param incubatorId
     */
    private void requestSensors(String incubatorId) {

        String urlIncubator = URLS.URL_INCUBADORA + "/" + incubatorId + URLS.URL_SENSOR;
        HttpHandler.arrayObjectGETRequest(urlIncubator, requestData -> {
            try {
                showSensorsData(requestData);

            } catch (BusinessException e) {
                if(e.emptyContext())
                    e.setContext(context);
                e.showErrorMessage();
            }
        }, context);
    }

    /**
     * Method to show sensors data on screen
     * @param sensorList
     * @throws BusinessException
     */
    private void showSensorsData(JSONArray sensorList) throws BusinessException {

        try {
            tableSensorList.removeAllViews();
            tableSensorList.addView(setUpSensorListLegend());
            for(int i=0 ; i<sensorList.length() ; ++i) {

                SensorDTO sensor = SensorDTO.parseJSONToDTO(sensorList.getJSONObject(i));
                int marginTop = (i==0) ? 3 : 0;
                String relativeUrl = URLS.URL_TOPIC + "/" + sensor.getSensorTypeId();
                HttpHandler.simpleObjectGETRequest(relativeUrl, requestData -> {
                    try {
                        TopicDTO topic = TopicDTO.parseJSONToDTO(requestData);
                        sensor.setSensorType(topic.getName());
                        TableRow tableRow = new TableRow(context);

                        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                        tableRowParams.setMargins(3, marginTop,3,3);
                        tableRow.setLayoutParams(tableRowParams);
                        tableRow.setBackgroundResource(R.color.white);

                        tableRow.addView(UtilViews.setUpTextView(context, sensor.getNumber(), 12, Color.BLACK));
                        tableRow.addView(UtilViews.setUpTextView(context, sensor.getSensorType(), 12, Color.BLACK));
                        String active = sensor.isActive() ? "Activado" : "Desactivado";
                        Integer color = sensor.isActive() ? Color.BLUE : Color.BLACK;
                        tableRow.addView(UtilViews.setUpTextView(context, active, 12, color));
                        tableRow.addView(UtilViews.setUpGoToButton(context, "ir a", sensor.getId(), Sensor.class));

                        tableSensorList.addView(tableRow);
                    } catch (BusinessException e) {
                        if(e.emptyContext())
                            e.setContext(context);
                        e.showErrorMessage();
                    }
                }, context);
            }

        } catch (Exception e) {
            throw new BusinessException("Ha ocurrido un error al mostrar los datos, vuelva a intentarlo");
        }
    }

    /**
     * Configure sensor list table legend
     * @return
     */
    private View setUpSensorListLegend() {

        TableRow tableRow = new TableRow(context);
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRowParams.setMargins(3, 3,3,3);
        tableRow.setLayoutParams(tableRowParams);
        tableRow.setBackgroundResource(R.color.white);

        tableRow.addView(UtilViews.setUpTextView(context, "Id",16, Color.BLACK));
        tableRow.addView(UtilViews.setUpTextView(context, "Tipo",16, Color.BLACK));
        tableRow.addView(UtilViews.setUpTextView(context, "Estado",16, Color.BLACK));
        tableRow.addView(UtilViews.setUpTextView(context, "",16, Color.BLACK));


        return tableRow;
    }

    /**
     * Method to request all incubator cameras data
     * @param incubatorId
     */
    private void requestCameras(String incubatorId) {

        String urlIncubator = URLS.URL_INCUBADORA + "/" + incubatorId + URLS.URL_CAMARA;
        HttpHandler.arrayObjectGETRequest(urlIncubator, requestData -> {
            try {
                showCamerasData(requestData);

            } catch (BusinessException e) {
                if(e.emptyContext())
                    e.setContext(context);
                e.showErrorMessage();
            }
        }, context);
    }

    /**
     * Method to show sensors data on screen
     * @param cameraList
     * @throws BusinessException
     */
    private void showCamerasData(JSONArray cameraList) throws BusinessException {

        try {
            tableCameraList.removeAllViews();
            tableCameraList.addView(setUpCameraListLegend());
            for(int i=0 ; i<cameraList.length() ; ++i) {

                CameraDTO camera = CameraDTO.parseJSONToDTO(cameraList.getJSONObject(i));
                int marginTop = (i==0) ? 3 : 0;
                TableRow tableRow = new TableRow(context);

                TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                tableRowParams.setMargins(3,marginTop,3,3);
                tableRow.setLayoutParams(tableRowParams);
                tableRow.setBackgroundResource(R.color.white);

                tableRow.addView(UtilViews.setUpTextView(context, camera.getNumber(), 12, Color.BLACK));
                tableRow.addView(UtilViews.setUpTextView(context, CameraDTO.getCameraType(camera.getTypeId()), 12, Color.BLACK));
                String active = camera.isActive() ? "Activado" : "Desactivado";
                Integer color = camera.isActive() ? Color.BLUE : Color.BLACK;
                tableRow.addView(UtilViews.setUpTextView(context, active, 12, color));
                tableRow.addView(UtilViews.setUpGoToButton(context, "ir a", camera.getId(), Camera.class));

                tableCameraList.addView(tableRow);
            }

        } catch (Exception e) {
            throw new BusinessException("Ha ocurrido un error al mostrar los datos, vuelva a intentarlo");
        }
    }

    /**
     * Configure camera list table legend
     * @return
     */
    private View setUpCameraListLegend() {

        TableRow tableRow = new TableRow(context);
        TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        tableRowParams.setMargins(3, 3,3,3);
        tableRow.setLayoutParams(tableRowParams);
        tableRow.setBackgroundResource(R.color.white);

        tableRow.addView(UtilViews.setUpTextView(context, "Id",16, Color.BLACK));
        tableRow.addView(UtilViews.setUpTextView(context, "Tipo",16, Color.BLACK));
        tableRow.addView(UtilViews.setUpTextView(context, "Estado",16, Color.BLACK));
        tableRow.addView(UtilViews.setUpTextView(context, "",16, Color.BLACK));

        return tableRow;
    }

    /**
     * Set up button to activate or deactivate an incubator
     * @param active
     */
    private void setUpActivateIncubatorButton(Boolean active) {

        String text = active ? "Desactivar" : "Activar";
        btnActivateIncubator.setText(text);

        if(active) {
            String url = URLS.URL_INCUBADORA + "/"+ incubatorId + URLS.URL_PACIENTE;
            HttpHandler.simpleObjectGETRequest(url, requestData -> {
                if(requestData != null && !requestData.isNull("id")) {
                    btnActivateIncubator.setOnClickListener(view -> {
                        BusinessException exception = new BusinessException("No se puede desactivar una incubadora asignada a un paciente", context);
                        exception.showErrorMessage();
                    });
                } else {
                    btnActivateIncubator.setOnClickListener(view -> {
                        swapIncubatorStatus(false);
                        recreate();
                    });
                }
            }, context);
        } else {
            btnActivateIncubator.setOnClickListener(view -> {
                swapIncubatorStatus(true);
                recreate();
            });
        }
    }

    /**
     * Method to make server request with incubator status change
     * @param newStatus
     */
    private void swapIncubatorStatus(boolean newStatus) {

        IncubatorDTO incubator = new IncubatorDTO();
        incubator.setId(String.valueOf(incubatorId));
        String relativeUrl = URLS.URL_INCUBADORA + "/" + incubatorId;
        try {
            RequestParams requestParams = IncubatorDTO.parseDTOtoRequestParams(incubator);
            requestParams.put("activo", newStatus);
            HttpHandler.patch(relativeUrl, requestParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {}
            });
        } catch (BusinessException e) {
            BusinessException exception = new BusinessException("Error al cambiar el estado de la incubadora", context);
            exception.showErrorMessage();
        }
    }

    /**
     * Method to configure save button
     */
    private void setSaveIncubatorButton() {

        btnSave.setOnClickListener(view -> {
            if(incubatorId == null) {
                createNewIncubator();
            } else {
                updateIncubator();
            }
        });
    }

    /**
     * Method to create and post new incubator
     */
    private void createNewIncubator() {

        try {
            IncubatorDTO incubator = collectIncubatorData();
            RequestParams requestParams = IncubatorDTO.parseDTOtoRequestParams(incubator);
            requestParams.put("activo", false);

            HttpHandler.post(URLS.URL_INCUBADORA + "/", requestParams, new JsonHttpResponseHandler() {
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
     * Method to update incubator data
     */
    private void updateIncubator() {

        try {
            IncubatorDTO incubator = collectIncubatorData();
            RequestParams requestParams = IncubatorDTO.parseDTOtoRequestParams(incubator);
            String relativeUrl = URLS.URL_INCUBADORA + "/" + incubatorId;

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
     * Method to collect incubator data from interface
     * @return IncubatorDTO
     * @throws BusinessException
     */
    private IncubatorDTO collectIncubatorData() throws BusinessException {

        IncubatorDTO incubator = new IncubatorDTO();

        if(validateData()) {
            if(numberIsDigit()) {
                incubator.setNumber(etIncubatorNumber.getText().toString());
                incubator.setName(etIncubatorName.getText().toString());
                incubator.setIpAddress(etIncubatorIPAddress.getText().toString());
            } else {
                throw new BusinessException("El campo Número debe ser un dígito");
            }
        } else {
            throw new BusinessException("Se debe completar todos los campos");
        }
        return incubator;
    }

    /**
     * Method to validate all user-inserted data. Checks if any is empty
     * @return Boolean
     */
    private Boolean validateData() {

        if(TextUtils.isEmpty(etIncubatorNumber.getText().toString()))
            return false;
        if(TextUtils.isEmpty(etIncubatorName.getText().toString()))
            return false;
        return !TextUtils.isEmpty(etIncubatorIPAddress.getText().toString());
    }

    /**
     * Method to validate if number field is a digit
     * @return
     */
    private Boolean numberIsDigit() {

        return TextUtils.isDigitsOnly(etIncubatorNumber.getText());
    }

    /**
     * Set up newSensor button
     */
    private void setNewSensorButton() {

        newSensor.setOnClickListener(view -> {
            Intent intent = new Intent(context, Sensor.class);
            intent.putExtra("incubatorId", incubatorId);
            startActivity(intent);
        });
    }

    /**
     * Set up newCamera button
     */
    private void setNewCameraButton() {

        newCamera.setOnClickListener(view -> {
            Intent intent = new Intent(context, Camera.class);
            intent.putExtra("incubatorId", incubatorId);
            startActivity(intent);
        });
    }

    /**
     * Method to remove incubator. Can only remove empty incubators
     */
    private void setRemoveIncubatorButton() {

        String errorContentMessage = "No se puede eliminar una incubadora con sensores o cámaras";
        String urlSensor = URLS.URL_INCUBADORA + "/" + incubatorId + URLS.URL_SENSOR;
        HttpHandler.arrayObjectGETRequest(urlSensor, requestData -> {
            if(requestData != null && requestData.length() != 0) {
                btnRemove.setOnClickListener(view -> {
                    BusinessException exception = new BusinessException(errorContentMessage, context);
                    exception.showErrorMessage();
                });
            } else {
                String urlCamera = URLS.URL_INCUBADORA + "/" + incubatorId + URLS.URL_CAMARA;
                HttpHandler.arrayObjectGETRequest(urlCamera, responseData -> {
                    if (responseData != null && responseData.length() != 0) {
                        btnRemove.setOnClickListener(view -> {
                            BusinessException exception = new BusinessException(errorContentMessage, context);
                            exception.showErrorMessage();
                        });
                    } else {
                        String urlPatient = URLS.URL_INCUBADORA + "/" + incubatorId + URLS.URL_PACIENTE;
                        HttpHandler.simpleObjectGETRequest(urlPatient, data -> {
                            if (data != null && !data.isNull("id")) {
                                btnRemove.setOnClickListener(view -> {
                                    BusinessException exception = new BusinessException("No se puede eliminar una incubadora asignada a un paciente", context);
                                    exception.showErrorMessage();
                                });
                            } else {
                                btnRemove.setOnClickListener(view -> HttpHandler.delete(URLS.URL_INCUBADORA + "/" + incubatorId, null, new JsonHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        finish();
                                        Toast.makeText(context, "Incubadora eliminada correctamente", Toast.LENGTH_SHORT).show();
                                    }
                                }));
                            }
                        }, context);
                    }
                }, context);
            }
        }, context);
    }
}
