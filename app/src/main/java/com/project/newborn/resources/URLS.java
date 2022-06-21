package com.project.newborn.resources;

public class URLS {

    public static String SERVER_IP;
    public static String MQTT_BROKER_PORT = "1883";
    public static final String BASE_URL = "/api";
    public static final String URL_PACIENTE = "/paciente";
    public static final String URL_INCUBADORA = "/incubadora";
    public static final String URL_INCUBADORA_ACTIVA = "/incubadora_activa";
    public static final String URL_CAMARA = "/camara";
    public static final String URL_SENSOR = "/sensor";
    public static final String URL_SENSOR_ACTIVO = "/sensor_activo";
    public static final String URL_TOPIC = "/topic";
    public static final String URL_ALARMA = "/alarma";

    /**
     * Method to create a tcp url by ip and port
     * @param ip
     * @param port
     * @return
     */
    public static String parseURLWithPortTCP(String ip, String port) {

        return "tcp://" + ip + ":" + port;
    }

    /**
     * Method to create a http url by ip and port
     * @param ip
     * @param port
     * @return
     */
    public static String parseURLWithPortHTTP(String ip, String port) {

        return "http://" + ip + ":" + port;
    }

    /**
     * Method to set server ip value
     * @param ip
     * @param port
     */
    public static void setServerIP(String ip, String port) {

        SERVER_IP = parseURLWithPortHTTP(ip, port);
    }
}
