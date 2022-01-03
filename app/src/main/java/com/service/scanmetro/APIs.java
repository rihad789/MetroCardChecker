package com.service.scanmetro;

public class APIs {

    private static final String DOMAIN = "http://metrobangla.herokuapp.com/api/";
    private static final String URL_GET_TRAIN_LINE = DOMAIN + "retrieve_train_line";
    private static final String URL_GET_STATIONS = DOMAIN + "retrieve_station";
    private static  final String URL_JOURNEY_FARE=DOMAIN+"journey_Fare";

    public static String getUrlGetTrainLine() { return URL_GET_TRAIN_LINE; }

    public static String getUrlGetStations() { return URL_GET_STATIONS; }

    public static String getUrlJourneyFare() { return URL_JOURNEY_FARE; }
}


