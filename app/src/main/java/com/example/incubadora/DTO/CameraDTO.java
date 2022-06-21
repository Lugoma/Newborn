package com.example.incubadora.DTO;

import com.example.incubadora.resources.BusinessException;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CameraDTO {

    private String id, number, incubatorId, port;
    Integer typeId;
    private Boolean active;

    private static final List<String> cameraTypes = new ArrayList<String>() {
        {
            add("Normal");
            add("Térmica");
            add("3D");
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getIncubatorId() {
        return incubatorId;
    }

    public void setIncubatorId(String incubatorId) {
        this.incubatorId = incubatorId;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Method to get camara type name by its id
     * @param typeId
     * @return
     */
    public static String getCameraType(Integer typeId) {

        if(typeId >= 0 && typeId < cameraTypes.size())
            return cameraTypes.get(typeId);
        return "";
    }

    /**
     * Method to get all camera types
     * @return
     */
    public static List<String> getCameraTypes() {
        return cameraTypes;
    }

    /**
     * Method to parse JSON data format to CameraDTO object. Used in GET requests
     * @param cameraJSON
     * @return
     * @throws BusinessException
     */
    public static CameraDTO parseJSONToDTO(JSONObject cameraJSON) throws BusinessException {
        CameraDTO cameraDTO = new CameraDTO();
        try {
            cameraDTO.setId(cameraJSON.getString("id"));
            cameraDTO.setNumber(cameraJSON.getString("numero"));
            cameraDTO.setIncubatorId(cameraJSON.getString("incubadora"));
            cameraDTO.setActive(cameraJSON.getBoolean("activo"));
            cameraDTO.setTypeId(cameraJSON.getInt("tipo_camara"));
            cameraDTO.setPort(cameraJSON.getString("puerto"));
        } catch (Exception e) {
            throw new BusinessException("Formato de datos incorrecto en la peticion");
        }
        return cameraDTO;
    }

    /**
     * Method to parse CameraDTO object to RequestParam object. Used in POST requests
     * @param cameraDTO
     * @return
     * @throws BusinessException
     */
    public static RequestParams parseDTOtoRequestParams(CameraDTO cameraDTO) throws BusinessException {

        RequestParams requestParams = new RequestParams();
        try {
            if(cameraDTO.getNumber() != null)
                requestParams.put("numero", cameraDTO.getNumber());
            if(cameraDTO.getPort() != null)
                requestParams.put("puerto", cameraDTO.getPort());
            if(cameraDTO.getTypeId() != null)
                requestParams.put("tipo_camara", cameraDTO.getTypeId());
            if(cameraDTO.getIncubatorId() != null)
                requestParams.put("incubadora", cameraDTO.getIncubatorId());

        } catch (Exception e) {
            throw new BusinessException("Formato de datos incorrecto");
        }

        return requestParams;
    }
}
