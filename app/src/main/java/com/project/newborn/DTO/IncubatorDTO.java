package com.project.newborn.DTO;

import com.project.newborn.resources.BusinessException;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

public class IncubatorDTO {

    private String id;
    private String number;
    private String name;
    private String ipAddress;
    boolean active;

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Method to parse JSON data format to IncubatorDTO object. Used in GET requests
     * @param incubatorJSON
     * @return
     * @throws BusinessException
     */
    public static IncubatorDTO parseJSONToDTO(JSONObject incubatorJSON) throws BusinessException{
        IncubatorDTO incubatorDTO = new IncubatorDTO();
        try {
            incubatorDTO.setId(incubatorJSON.getString("id"));
            incubatorDTO.setNumber(incubatorJSON.getString("numero"));
            incubatorDTO.setName(incubatorJSON.getString("nombre"));
            incubatorDTO.setIpAddress(incubatorJSON.getString("direccion_ip"));
            incubatorDTO.setActive(incubatorJSON.getBoolean("activo"));
        } catch (Exception e) {
            throw new BusinessException("Formato de datos incorrecto en la peticion");
        }
        return incubatorDTO;
    }

    /**
     * Method to parse incubatorDTO object to RequestParam object. Used in POST requests
     * @param incubatorDTO
     * @return
     * @throws BusinessException
     */
    public static RequestParams parseDTOtoRequestParams(IncubatorDTO incubatorDTO) throws BusinessException{

        RequestParams requestParams = new RequestParams();
        try {
            if(incubatorDTO.getNumber() != null)
                requestParams.put("numero", incubatorDTO.getNumber());
            if(incubatorDTO.getName() != null)
                requestParams.put("nombre", incubatorDTO.getName());
            if(incubatorDTO.getIpAddress() != null)
                requestParams.put("direccion_ip", incubatorDTO.getIpAddress());
            if(incubatorDTO.getId() != null)
                requestParams.put("id", incubatorDTO.getId());

        } catch (Exception e) {
            throw new BusinessException("Formato de datos incorrecto");
        }

        return requestParams;
    }
}
