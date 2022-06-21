package com.example.incubadora.resources;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateManager {

    private static final String SERVER_FORMAT = "yyyy-MM-dd";
    private static final String CLIENT_FORMAT = "dd/MM/yyyy";

    /**
     * Method to parse date from server format to Date object
     * @param date
     * @return
     * @throws Exception
     */
    public static Date parseToDateFromServer(String date) throws Exception {

        SimpleDateFormat sdf = new SimpleDateFormat(SERVER_FORMAT, Locale.getDefault());
        return sdf.parse(date);
    }

    /**
     * Method to parse date from client format to Date
     * @param date
     * @return
     * @throws Exception
     */
    public static Date parseToDateFromClient(String date) throws Exception{

        SimpleDateFormat sdf = new SimpleDateFormat(CLIENT_FORMAT, Locale.getDefault());
        return sdf.parse(date);
    }

    /**
     * Method to parse date object to server format string
     * @param date
     * @return
     */
    public static String parseDateToServerFormat(Date date) {

        SimpleDateFormat sdf = new SimpleDateFormat(SERVER_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Method to parse date object to client format string
     * @param date
     * @return
     */
    public static String parseDateToClientFormat(Date date) {

        SimpleDateFormat sdf = new SimpleDateFormat(CLIENT_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }
}
