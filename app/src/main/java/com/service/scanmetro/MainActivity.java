package com.service.scanmetro;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    AppCompatButton btnScan, check_journey_validity;

    private static final String URL_GET_STATIONS = APIs.getUrlGetStations();
    private static final String URL_GET_TRAIN_LINE = APIs.getUrlGetTrainLine();
    private static final String URL_JOURNEY_FARE = APIs.getUrlJourneyFare();

    ProgressDialog retrieve_line_data, retrieve_station_data, check_JourneyFare;
    ArrayList<Stations_Pojo_Class> stationList;
    ArrayList<Line_Pojo_Class> lineList;
    Spinner line_spinner, station_spinner;
    String line_id = "", station_id = "";
    AppCompatEditText editText_card_scan_result;
    TextView journeyFareValidity;
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Scan Metro Card");

        btnScan = findViewById(R.id.scan_qr_code_button);
        check_journey_validity = findViewById(R.id.check_journey_validity);
        journeyFareValidity = findViewById(R.id.journeyFareValidity);


        line_spinner = findViewById(R.id.line_spinner);
        station_spinner = findViewById(R.id.station_spinner);
        editText_card_scan_result = findViewById(R.id.editText_card_scan_result);

        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(false);

        initializeProgressDialog();
        getTrainLineData();

        btnScan.setOnClickListener(view -> {

            IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
            integrator.setPrompt("Scan Metro Card");
            integrator.setCameraId(0);
            integrator.setOrientationLocked(false);
            integrator.setBeepEnabled(true);
            integrator.setBarcodeImageEnabled(true);
            integrator.initiateScan();

        });

        check_journey_validity.setOnClickListener(v -> {
            String scan_result= Objects.requireNonNull(editText_card_scan_result.getText()).toString();

            if(checkInput(scan_result,line_id,station_id))
            {
                journeyFare(line_id, station_id, scan_result);
            }

        });
    }

    private boolean checkInput(String card_no, String line_id, String station_id) {

        if (card_no.isEmpty()) {
            Toast.makeText(this, "Please Scan a Metro Card", Toast.LENGTH_SHORT).show();
            return false;
        } else if (line_id.isEmpty()) {
            Toast.makeText(this, "Please Select a Train Line", Toast.LENGTH_SHORT).show();
            return false;
        } else if (station_id.isEmpty()) {
            Toast.makeText(this, "Please Scan a Station", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Log.e("Scan*******", "Cancelled scan");

            } else {
                Log.e("Scan", "Scanned");
//                tv_qr_readTxt.setText(result.getContents());
                editText_card_scan_result.setText(result.getContents());
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initializeProgressDialog() {

        retrieve_line_data = new ProgressDialog(MainActivity.this);
        retrieve_line_data.setMessage("Retrieving Line Data...");
        retrieve_line_data.setCancelable(false);

        retrieve_station_data = new ProgressDialog(MainActivity.this);
        retrieve_station_data.setMessage("Retrieving Stations Data...");
        retrieve_station_data.setCancelable(false);

        check_JourneyFare = new ProgressDialog(MainActivity.this);
        check_JourneyFare.setMessage("Checking Data Data...");
        check_JourneyFare.setCancelable(false);

    }

    public void getTrainLineData() {
        retrieve_line_data.show();
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, URL_GET_TRAIN_LINE, null, response -> {

            lineList = new ArrayList<>();
            lineList.add(new Line_Pojo_Class("", "Select Train Line"));

            try {

                JSONArray lineArray = response.getJSONArray("trainLineData");
                //now looping through all the elements of the json array
                for (int i = 0; i < lineArray.length(); i++) {
                    //getting the json object of the particular index inside the array
                    JSONObject lineData = lineArray.getJSONObject(i);
                    String id = lineData.getString("id");
                    String name = lineData.getString("name");
                    lineList.add(new Line_Pojo_Class(id, name));
                }

                //creating custom adapter object
                setValueToTrainLineSpinner();
                retrieve_line_data.dismiss();

            } catch (JSONException e) {
                retrieve_line_data.dismiss();
                e.printStackTrace();
            }
        }, error -> {
            retrieve_line_data.dismiss();
            error.printStackTrace();
        });

        requestQueue.add(jsonObjectRequest);
    }

    private void setValueToTrainLineSpinner() {

        ArrayAdapter<Line_Pojo_Class> lineAdapter = new ArrayAdapter<Line_Pojo_Class>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, lineList);
        line_spinner.setAdapter(lineAdapter);

        line_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Line_Pojo_Class lineData = (Line_Pojo_Class) parent.getItemAtPosition(position);
                line_id = lineData.id;

                //Toast.makeText(Fare_And_Route_Activity.this, "Line ID:" + lineData.id, Toast.LENGTH_SHORT).show();

                if (!lineData.id.equals("")) {
                    getStationData(lineData.id);
                } else {
                    station_spinner.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void getStationData(String LineID) {

        retrieve_station_data.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_GET_STATIONS,
                response -> {

                    stationList = new ArrayList<Stations_Pojo_Class>();
                    stationList.add(new Stations_Pojo_Class("", "Select Station"));

                    try {

                        JSONObject obj = new JSONObject(response);
                        JSONArray stationArray = obj.getJSONArray("stationData");

                        //now looping through all the elements of the json array
                        for (int i = 0; i < stationArray.length(); i++) {
                            //getting the json object of the particular index inside the array
                            JSONObject stationData = stationArray.getJSONObject(i);
                            String id = stationData.getString("id");
                            String name = stationData.getString("name");
                            stationList.add(new Stations_Pojo_Class(id, name));

                        }

                        //creating custom adapter object
                        setValueToStationSpinner();
                        retrieve_station_data.dismiss();


                    } catch (JSONException e) {
                        e.printStackTrace();
                        retrieve_station_data.dismiss();
                        Toast.makeText(MainActivity.this, "JSONException:" + e, Toast.LENGTH_SHORT).show();
                    }

                }, error -> {
            retrieve_station_data.dismiss();
            Toast.makeText(MainActivity.this, " Volley Error:" + error.toString(), Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", LineID);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);

    }

    private void setValueToStationSpinner() {

        ArrayAdapter<Stations_Pojo_Class> stationAdapter = new ArrayAdapter<Stations_Pojo_Class>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, stationList);
        station_spinner.setAdapter(stationAdapter);
        station_spinner.setVisibility(View.VISIBLE);

        station_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Stations_Pojo_Class stationData = (Stations_Pojo_Class) parent.getItemAtPosition(position);
                station_id = stationData.id;

                //Toast.makeText(Fare_And_Route_Activity.this, "Station ID:" + station_id, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void journeyFare(String line_id, String station_id, String card_no) {
        check_JourneyFare.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_JOURNEY_FARE,
                response -> {

                    try {
                        JSONObject obj = new JSONObject(response);
                        //now looping through all the elements of the json array
                        String Status = obj.getString("Status");
                        if (Status.equals("1")) {
                            String Message = obj.getString("Message");
                            journeyFareValidity.setText(Message);
                            Toast.makeText(this, "" + Message, Toast.LENGTH_LONG).show();
                            check_JourneyFare.dismiss();

                            builder.setMessage(Message);

                            builder.setPositiveButton(
                                    "Continue",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            editText_card_scan_result.setText("");
                                            dialog.cancel();
                                        }
                                    });

                            AlertDialog alert11 = builder.create();
                            alert11.show();

                        } else {
                            String Message = obj.getString("Message");
                            journeyFareValidity.setText(Message);
                            Toast.makeText(this, "" + Message, Toast.LENGTH_LONG).show();
                            check_JourneyFare.dismiss();
                        }


                        //creating custom adapter object

                    } catch (JSONException e) {
                        e.printStackTrace();
                        check_JourneyFare.dismiss();
                        Toast.makeText(MainActivity.this, "JSONException:" + e, Toast.LENGTH_SHORT).show();
                    }

                }, error -> {
            check_JourneyFare.dismiss();
            Toast.makeText(MainActivity.this, " Volley Error:" + error.toString(), Toast.LENGTH_SHORT).show();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("line_id", line_id);
                params.put("station_id", station_id);
                params.put("card_no", card_no);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


    }


}