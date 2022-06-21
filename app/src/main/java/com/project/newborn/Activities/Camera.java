package com.project.newborn.Activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.project.newborn.DTO.CameraDTO;
import com.project.newborn.R;
import com.project.newborn.httpInterfaces.HttpHandler;
import com.project.newborn.resources.BusinessException;
import com.project.newborn.resources.URLS;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;

public class Camera extends Activity {
    
    private Integer cameraId, incubatorId;
    private Context context;
    private EditText etCameraNumber, etCameraPort;
    private Spinner spinnerCameraType;
    private Button btnSave, btnRemove, btnActivateCamera;
    private TextView tvCameraStatus;
    private LinearLayout lytStatus;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    public void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        setContentView(R.layout.camera);

        Bundle bundleActivity = getIntent().getExtras();
        if (bundleActivity != null) {
            incubatorId = bundleActivity.getInt("incubatorId");
            String id = bundleActivity.getString("id");
            if (id != null)
                cameraId = Integer.parseInt(id);
        }

        context = this;
        etCameraNumber = findViewById(R.id.etCameraNumber);
        etCameraPort = findViewById(R.id.etCameraPort);
        spinnerCameraType = findViewById(R.id.spinnerCameraType);
        btnSave = findViewById(R.id.btnSave);
        btnRemove = findViewById(R.id.btnRemove);
        btnActivateCamera = findViewById(R.id.btnActivateCamera);
        tvCameraStatus = findViewById(R.id.tvCameraStatus);
        lytStatus = findViewById(R.id.lytStatus);

        setSaveCameraButton();
        setRemoveCameraButton();
    }

    @Override
    public void onResume() {

        super.onResume();
        loadAllCameraTypes();
        if (cameraId != null) {
            requestCamera();
        } else {
            lytStatus.setVisibility(View.GONE);
            btnRemove.setVisibility(View.GONE);
        }
    }

    /**
     * Method to fill spinner with all camera types
     */
    private void loadAllCameraTypes() {

        List<String> types = CameraDTO.getCameraTypes();
        arrayAdapter = new ArrayAdapter<>(context, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, types);
        spinnerCameraType.setAdapter(arrayAdapter);
    }

    /**
     * Method to request all camera data from server
     */
    private void requestCamera() {

        String url = URLS.URL_CAMARA + "/" + cameraId;
        HttpHandler.simpleObjectGETRequest(url, responseData -> {
            try {
                showCameraData(CameraDTO.parseJSONToDTO(responseData));

            } catch (BusinessException e) {
                BusinessException exception = new BusinessException("Hay un error en los datos recuperados del servidor", context);
                exception.showErrorMessage();
            }
        }, context);
    }

    /**
     * Method to show all camera data from cameraDTO
     * @param camera
     */
    private void showCameraData(CameraDTO camera) {

        etCameraNumber.setText(camera.getNumber());
        etCameraPort.setText(camera.getPort());
        String cameraType = CameraDTO.getCameraType(camera.getTypeId());
        spinnerCameraType.setSelection(arrayAdapter.getPosition(cameraType));
        String status = "Estado: " + (camera.isActive() ? "Activada" : "Desactivada");
        tvCameraStatus.setText(status);
        setUpActivateCameraButton(camera.isActive());
    }

    /**
     * Set up button to activate or deactivate a camera
     * @param active
     */
    private void setUpActivateCameraButton(Boolean active) {

        String text = active ? "Desactivar" : "Activar";
        btnActivateCamera.setText(text);
        btnActivateCamera.setOnClickListener(view -> {
            swapCameraStatus(!active);
            recreate();
        });
    }

    /**
     * Method to make server request with camera status change
     * @param newStatus
     */
    private void swapCameraStatus(boolean newStatus) {

        CameraDTO camera = new CameraDTO();
        camera.setId(String.valueOf(cameraId));
        String relativeUrl = URLS.URL_CAMARA + "/" + cameraId;
        try {
            RequestParams requestParams = CameraDTO.parseDTOtoRequestParams(camera);
            requestParams.put("activo", newStatus);
            HttpHandler.patch(relativeUrl, requestParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {}
            });
        } catch (BusinessException e) {
            BusinessException exception = new BusinessException("Error al cambiar el estado de la cámara", context);
            exception.showErrorMessage();
        }
    }

    /**
     * Method to configure save button
     */
    private void setSaveCameraButton() {

        btnSave.setOnClickListener(view -> {
            if(cameraId == null) {
                createNewCamera();
            } else {
                updateCamera();
            }
        });
    }

    /**
     * Method to create and post new camera
     */
    private void createNewCamera() {

        try {
            CameraDTO camera = collectCameraData();
            camera.setIncubatorId(String.valueOf(incubatorId));
            RequestParams requestParams = CameraDTO.parseDTOtoRequestParams(camera);
            requestParams.put("activo", false);

            HttpHandler.post(URLS.URL_CAMARA + "/", requestParams, new JsonHttpResponseHandler() {
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
     * Method to update camera data
     */
    private void updateCamera() {

        try {
            CameraDTO camera = collectCameraData();
            RequestParams requestParams = CameraDTO.parseDTOtoRequestParams(camera);
            String relativeUrl = URLS.URL_CAMARA + "/" + cameraId;

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
     * Method to collect camera data from interface
     * @return CameraDTO
     * @throws BusinessException
     */
    private CameraDTO collectCameraData() throws BusinessException {

        CameraDTO camera = new CameraDTO();

        if(validateData()) {
            if(numberIsDigit()) {
                camera.setNumber(etCameraNumber.getText().toString());
                camera.setPort(etCameraPort.getText().toString());
                String cameraType = spinnerCameraType.getSelectedItem().toString();
                camera.setTypeId(CameraDTO.getCameraTypes().indexOf(cameraType));
            } else {
                throw new BusinessException("El campo Número debe ser un dígito");
            }
        } else {
            throw new BusinessException("Se debe completar todos los campos");
        }
        return camera;
    }

    /**
     * Method to validate all user-inserted data. Checks if any is empty
     * @return Boolean
     */
    private Boolean validateData() {

        if(TextUtils.isEmpty(etCameraNumber.getText().toString()))
            return false;
        if(TextUtils.isEmpty(etCameraPort.getText().toString()))
            return false;
        return !TextUtils.isEmpty(spinnerCameraType.getSelectedItem().toString());
    }

    /**
     * Method to validate if number field is a digit
     * @return
     */
    private Boolean numberIsDigit() {

        return TextUtils.isDigitsOnly(etCameraNumber.getText());
    }

    /**
     * Configure remove camera button
     */
    private void setRemoveCameraButton() {

        btnRemove.setOnClickListener(view -> HttpHandler.delete(URLS.URL_CAMARA + "/" + cameraId, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                finish();
                Toast.makeText(context, "Cámara eliminada correctamente", Toast.LENGTH_SHORT).show();
            }
        }));
    }
}