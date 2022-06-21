package com.example.incubadora.DTO;

import com.example.incubadora.resources.BusinessException;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

public class AlarmDTO {

    String id, maxLimit, minLimit, sensorId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaxLimit() {
        return maxLimit;
    }

    public void setMaxLimit(String maxLimit) {
        this.maxLimit = maxLimit;
    }

    public String getMinLimit() {
        return minLimit;
    }

    public void setMinLimit(String minLimit) {
        this.minLimit = minLimit;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * Method to parse JSON data format to AlarmJSON object. Used in GET requests
     * @param alarmJSON
     * @return
     * @throws BusinessException
     */
    public static AlarmDTO parseJSONToDTO(JSONObject alarmJSON) throws BusinessException {
        AlarmDTO alarmDTO = new AlarmDTO();
        try {
            alarmDTO.setId(alarmJSON.getString("id"));
            alarmDTO.setMaxLimit(alarmJSON.getString("limite_superior"));
            alarmDTO.setMinLimit(alarmJSON.getString("limite_inferior"));
            alarmDTO.setSensorId(alarmJSON.getString("sensor"));

        } catch (Exception e) {
            throw new BusinessException("Formato de datos incorrecto en la peticion");
        }
        return alarmDTO;
    }

    /**
     * Method to parse AlarmDTO object to RequestParam object. Used in POST requests
     * @param alarmDTO
     * @return
     * @throws BusinessException
     */
    public static RequestParams parseDTOtoRequestParams(AlarmDTO alarmDTO) throws BusinessException {

        RequestParams requestParams = new RequestParams();
        try {
            if(alarmDTO.getMaxLimit() != null)
                requestParams.put("limite_superior", alarmDTO.getMaxLimit());
            if(alarmDTO.getMinLimit() != null)
                requestParams.put("limite_inferior", alarmDTO.getMinLimit());
            if(alarmDTO.getSensorId() != null)
                requestParams.put("sensor", alarmDTO.getSensorId());
        } catch (Exception e) {
            throw new BusinessException("Formato de datos incorrecto");
        }

        return requestParams;
    }
}
