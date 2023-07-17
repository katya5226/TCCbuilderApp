package com.lushprojects.circuitjs1.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dev.jjs.ast.JField;
import com.google.gwt.http.client.*;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

import java.util.Arrays;
import java.util.Vector;

// edited


@SuppressWarnings("rawtypes")
public class Material {

    public boolean invariant;
    public boolean thermoelectric;
    public boolean hysteresis;
    public boolean nonInvariant;
    public boolean temperatureInducedPhaseChange;
    public boolean magnetocaloric;
    public boolean electrocaloric;
    public boolean barocaloric;
    public boolean elastocaloric;

    public int ID;
    public Vector<Double> interpTemps;

    public Vector<Double> rho;
    public Vector<Double> k;
    private final CirSim sim;
    public Vector<Vector<Double>> dTheating;
    public Vector<Vector<Double>> dTcooling;
    public Vector<Vector<Double>> cp;
    public Vector<Vector<Double>> cpHeating;
    public Vector<Vector<Double>> cpCooling;
    public Vector<Double> fields;

    public String name;
    public String label;


    public Vector<Double> temperaturesST;
    public Vector<Double> lowFieldEntropies;
    public Vector<Double> highFieldEntropies;
    public Vector<Double> derSTlow;
    public Vector<Double> derSThigh;
    double rhoRT;
    double cpRT;
    double kRT;
    double emissRT;
    double tRhoMin;
    double tRhoMax;
    double tCpMin;
    double tCpMax;
    double tKMin;
    double tKMax;
    double tEmissMin;
    double tEmissMax;
    double tMelt;

    boolean field;

    public Material(String materialName, CirSim sim) {
        this.name = materialName;
        this.sim = sim;
        this.field = false;
        this.interpTemps = new Vector<Double>();
        for (int i = 0; i < 20000; i++) {
            double temp = Math.round(0.1 * i * 10.0) / 10.0;
            interpTemps.add(temp);
        }
        this.rho = new Vector<Double>();
        this.k = new Vector<Double>();
        this.cp = new Vector<Vector<Double>>();
        this.dTheating = new Vector<Vector<Double>>();
        this.dTcooling = new Vector<Vector<Double>>();
        this.cpHeating = new Vector<Vector<Double>>();
        this.cpCooling = new Vector<Vector<Double>>();
        this.fields = new Vector<Double>();
        setFlags(sim.materialFlagText);
    }


    private void setFlags(String text) {
        RegExp pattern = RegExp.compile(name.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)") + ".*");
        MatchResult matcher = pattern.exec(text);
        if (matcher != null) {
            String[] line = matcher.getGroup(0).split(",", -1);
            invariant = line[1].equals("1");
            thermoelectric = line[2].equals("1");
            hysteresis = line[3].equals("1");
            nonInvariant = line[4].equals("1");
            temperatureInducedPhaseChange = line[5].equals("1");
            electrocaloric = line[6].equals("1");
            magnetocaloric = line[7].equals("1");
            barocaloric = line[8].equals("1");
            elastocaloric = line[9].equals("1");
            //GWT.log(Arrays.toString(line));
            //GWT.log(line.length+" "+invariant +" "+thermoelectric +" "+hysteresis+" "+nonInvariant+" "+temperatureInducedPhaseChange+" "+electrocaloric+" "+magnetocaloric+" "+barocaloric+" "+elastocaloric);
        } else
            GWT.log("No entry found in flag file for material \"" + name + "\"");
    }

    void readFiles() {
        String url_info = GWT.getModuleBaseURL() + "material_data/materials_library/" + name + "/info.txt";
        String url_properties = GWT.getModuleBaseURL() + "material_data/materials_library/" + name + "/RT_properties.txt";
        String url_ranges = GWT.getModuleBaseURL() + "material_data/materials_library/" + name + "/Ranges.txt";
        String url_rho = GWT.getModuleBaseURL() + "material_data/materials_library/" + name + "/rho.txt";
        String url_k = GWT.getModuleBaseURL() + "material_data/materials_library/" + name + "/k.txt";

        sim.awaitedResponses.add(url_info);
        sim.awaitedResponses.add(url_properties);
        sim.awaitedResponses.add(url_ranges);
        sim.awaitedResponses.add(url_rho);
        sim.awaitedResponses.add(url_k);

        readInfoFromURL(url_info);
        readRTPropertiesFromURL(url_properties);
        readRangesFromURL(url_ranges);
        fillVectorFromURL(url_rho, rho);
        fillVectorFromURL(url_k, k);

        if (invariant) {
            String url_cp = GWT.getModuleBaseURL() + "material_data/materials_library/" + name + "/cp.txt";
            sim.awaitedResponses.add(url_cp);
            Vector<Double> vector = new Vector<Double>();
            fillVectorFromURL(url_cp, vector);
            cp.add(vector);

        }
        if (magnetocaloric) {
            String url_fields = GWT.getModuleBaseURL() + "material_data/materials_library/" + name + "/Fields.txt";
            sim.awaitedResponses.add(url_fields);
            loadFieldsFromUrl(url_fields, new Callback() {
                @Override
                public void onSuccess(Object result) {
                    sim.awaitedResponses.remove(url_fields);
                    String url_cp, url_dT;
                    for (double field : fields) {
                        String fieldName = NumberFormat.getFormat("#0.0").format(field);
                        url_cp = GWT.getModuleBaseURL() + "material_data/materials_library/" + name + "/cp_" + fieldName + "T.txt";
                        Vector<Double> field_cp = new Vector<Double>();
                        sim.awaitedResponses.add(url_cp);
                        fillVectorFromURL(url_cp, field_cp);
                        cp.add(field_cp);

                        if (field != 0) {
                            Vector<Double> field_dT = new Vector<Double>();
                            url_dT = GWT.getModuleBaseURL() + "material_data/materials_library/" + name + "/dT_" + fieldName + "T_cooling.txt";
                            sim.awaitedResponses.add(url_dT);
                            fillVectorFromURL(url_dT, field_dT);
                            dTcooling.add(field_dT);

                            url_dT = GWT.getModuleBaseURL() + "material_data/materials_library/" + name + "/dT_" + fieldName + "T_heating.txt";
                            field_dT = new Vector<Double>();
                            sim.awaitedResponses.add(url_dT);
                            fillVectorFromURL(url_dT, field_dT);
                            dTheating.add(field_dT);
                        }
                    }

                }

                @Override
                public void onFailure(Object reason) {
                    sim.awaitedResponses.remove(url_fields);
                }
            });
        }

    }

    boolean isLoaded() {
        return cp.size() > 0 && k.size() > 0 && rho.size() > 0;
    }

    void readRTPropertiesFromURL(String url) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    GWT.log("File Error Response", exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        String text = response.getText();
                        JSONValue val = JSONParser.parseStrict(text);
                        JSONObject obj = val.isObject();
                        rhoRT = Double.parseDouble(obj.get("density").toString().replaceAll("\"", ""));
                        cpRT = Double.parseDouble(obj.get("specificHeatCapacity").toString().replaceAll("\"", ""));
                        rhoRT = Double.parseDouble(obj.get("thermalConductivity").toString().replaceAll("\"", ""));
                        emissRT = Double.parseDouble(obj.get("emissivity").toString().replaceAll("\"", ""));

                        sim.awaitedResponses.remove(url);
                    } else if (response.getStatusCode() == Response.SC_NOT_FOUND) {
                        GWT.log("File \"" + url + "\" not found");
                        sim.awaitedResponses.remove(url);
                    } else {
                        GWT.log("Bad file server response: " + response.getStatusText());
                        sim.awaitedResponses.remove(url);
                    }
                }
            });
        } catch (RequestException e) {
            GWT.log("failed file reading", e);
        }

    }

    void readRangesFromURL(String url) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    GWT.log("File Error Response", exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        String text = response.getText();
                        JSONValue val = JSONParser.parseStrict(text);
                        JSONObject obj = val.isObject();
                        tRhoMin = Double.parseDouble(obj.get("density").toString().replaceAll("\"", "").split("-")[0]);
                        tRhoMax = Double.parseDouble(obj.get("density").toString().replaceAll("\"", "").split("-")[1]);
                        tCpMin = Double.parseDouble(obj.get("specificHeatCapacity").toString().replaceAll("\"", "").split("-")[0]);
                        tCpMax = Double.parseDouble(obj.get("specificHeatCapacity").toString().replaceAll("\"", "").split("-")[1]);
                        tKMin = Double.parseDouble(obj.get("thermalConductivity").toString().replaceAll("\"", "").split("-")[0]);
                        tKMax = Double.parseDouble(obj.get("thermalConductivity").toString().replaceAll("\"", "").split("-")[1]);
/*                        tEmissMin = Double.parseDouble()(obj.get("emissivity").toString().replaceAll("\"","")".split("-")[0]);
                        tEmissMax = Double.parseDouble()(obj.get("emissivity").toString().replaceAll("\"","")".split("-")[1]);*/

                        sim.awaitedResponses.remove(url);
                    } else if (response.getStatusCode() == Response.SC_NOT_FOUND) {
                        GWT.log("File \"" + url + "\" not found");
                        sim.awaitedResponses.remove(url);
                    } else {
                        GWT.log("Bad file server response: " + response.getStatusText());
                        sim.awaitedResponses.remove(url);
                    }
                }
            });
        } catch (RequestException e) {
            GWT.log("failed file reading", e);
        }

    }

    void readInfoFromURL(String url) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    GWT.log("File Error Response", exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        String text = response.getText();
                        String[] info = text.split(",");
                        ID = Integer.parseInt(info[0]);
                        label = info[1];
                        tMelt = Double.parseDouble(info[2]);
                        sim.awaitedResponses.remove(url);
                    } else if (response.getStatusCode() == Response.SC_NOT_FOUND) {
                        GWT.log("File \"" + url + "\" not found");
                        sim.awaitedResponses.remove(url);
                    } else {
                        GWT.log("Bad file server response: " + response.getStatusText());
                        sim.awaitedResponses.remove(url);
                    }
                }
            });
        } catch (RequestException e) {
            GWT.log("failed file reading", e);
        }

    }

    void fillVectorFromURL(String url, Vector<Double> vector) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    GWT.log("File Error Response", exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        String text = response.getText();
                        for (String line : text.split("\n"))
                            vector.add(Double.parseDouble(line));
                        sim.awaitedResponses.remove(url);

                    } else if (response.getStatusCode() == Response.SC_NOT_FOUND) {
                        GWT.log("File \"" + url + "\" not found");
                        sim.awaitedResponses.remove(url);
                    } else {
                        GWT.log("Bad file server response: " + response.getStatusText());
                        sim.awaitedResponses.remove(url);
                    }
                }
            });
        } catch (RequestException e) {
            GWT.log("failed file reading", e);
        }

    }

    void loadFieldsFromUrl(String url, final Callback callback) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    // Handle error
                    GWT.log("File Error Response", exception);
                    callback.onFailure(exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        String text = response.getText();
                        for (String line : text.split("\n"))
                            fields.add(Double.parseDouble(line));
                        callback.onSuccess(response);
                    } else if (response.getStatusCode() == Response.SC_NOT_FOUND) {
                        GWT.log("File \"" + url + "\" not found");
                        callback.onFailure(response.getStatusText());
                    } else {
                        GWT.log("Bad file server response: " + response.getStatusText());
                        callback.onFailure(response.getStatusText());
                    }
                }
            });
        } catch (RequestException e) {
            GWT.log("failed file reading", e);
            callback.onFailure(e);
        }

    }
/*
    void loadThermalFileFromURL(String url, String property, final Callback callback) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    // Handle error
                    GWT.log("File Error Response", exception);
                    callback.onFailure(exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        String text = response.getText();
                        if (!readThermalData(text, property))
                            GWT.log("File \"" + url + "\" is empty");
                        callback.onSuccess(response);
                    } else if (response.getStatusCode() == Response.SC_NOT_FOUND) {
                        GWT.log("File \"" + url + "\" not found");
                        callback.onFailure(response.getStatusText());
                    } else {
                        GWT.log("Bad file server response: " + response.getStatusText());
                        callback.onFailure(response.getStatusText());
                    }
                }
            });
        } catch (RequestException e) {
            GWT.log("failed file reading", e);
            callback.onFailure(e);
        }
    }

    boolean readThermalData(String text, String property) {
*//*        this.temps.clear();
        switch (property) {
            case "rho":
                this.rho.clear();
                break;
            case "cp":
                this.cp.clear();
                break;
            case "k":
                this.k.clear();
                break;
        }*//*
        try {
            String[] tmp;
            for (String line : text.split("\n")) {
                if (line.length() > 0) {
                    tmp = line.replaceAll("\t", " ").replaceAll("\\s+", " ").split(" ");
                    *//*if (tmp.length == 1) {
                        switch (property) {
                            case "rho":
                                for (Double temp : temps)
                                    this.rho.add(Double.parseDouble(tmp[0]));
                                break;
                            case "cp":
                                for (Double temp : temps)
                                    this.cp.add(Double.parseDouble(tmp[0]));
                                break;
                            case "k":
                                for (Double temp : temps)
                                    this.k.add(Double.parseDouble(tmp[0]));
                                break;
                        }
                    }*//*
                    switch (property) {
                        case "rho":
                            this.rho.add(Double.parseDouble(tmp[0]));
                            break;
                        case "cp":
                            this.cp.add(Double.parseDouble(tmp[0]));
                            break;
                        case "k":
                            this.k.add(Double.parseDouble(tmp[0]));
                            break;

                    }
                } else {
                    return false;
                }
            }

        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }*/
}
