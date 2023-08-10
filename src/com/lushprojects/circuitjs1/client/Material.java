package com.lushprojects.circuitjs1.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

import java.lang.reflect.Field;
import java.util.Vector;

@SuppressWarnings("rawtypes")
public class Material {
    public boolean name;
    public boolean invariant;
    public boolean thermoelectric;
    public boolean temperatureInducedPhaseChange;
    public boolean electrocaloric;
    public boolean magnetocaloric;
    public boolean barocaloric;
    public boolean elastocaloric;
    public boolean cpThysteresis;
    public boolean dTThysteresis;
    public boolean kThysteresis;


    private final CirSim sim;
    public Vector<Double> interpTemps;

    public Vector<Double> rho;
    public Vector<Double> k;
    public Vector<Double> kTheating;
    public Vector<Double> kTcooling;
    public Vector<Vector<Double>> dT;
    public Vector<Vector<Double>> dTheating;
    public Vector<Vector<Double>> dTcooling;

    public Vector<Vector<Double>> cp;
    public Vector<Vector<Double>> cpHeating;
    public Vector<Vector<Double>> cpCooling;
    public Vector<Double> fields;

    public String materialName;
    boolean field;

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
    int id;
    private String shortName, longName;

    public Material(String materialName, CirSim sim) {
        this.materialName = materialName;
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
        RegExp pattern = RegExp.compile(materialName.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)") + ".*");
        MatchResult matcher = pattern.exec(text);
        if (matcher != null) {
            String[] line = matcher.getGroup(0).split(",", -1);
            invariant = line[1].equals("1");
            thermoelectric = line[2].equals("1");
            temperatureInducedPhaseChange = line[3].equals("1");
            electrocaloric = line[4].equals("1");
            magnetocaloric = line[5].equals("1");
            barocaloric = line[6].equals("1");
            elastocaloric = line[7].equals("1");
            cpThysteresis = line[8].equals("1");
            dTThysteresis = line[9].equals("1");
            kThysteresis = line[10].equals("1");
        } else
            GWT.log("No entry found in flag file for material \"" + materialName + "\"");
    }

    void readFiles() {

        String CORSproxy = "https://corsproxy.io/?";
        String baseURL = CORSproxy + "http://materials.tccbuilder.org/";
        //String baseURL = GWT.getModuleBaseURL() + "material_data/materials_library/";
        //String baseURL = "http://127.0.0.1:8888/";
        String url_info = baseURL + materialName + "/appInfo/info.json";
        String url_rho = baseURL + materialName + "/appInfo/rho.txt";
        String url_k = baseURL + materialName + "/appInfo/k.txt";

        if (!sim.awaitedResponses.contains(url_info)) {
            sim.awaitedResponses.add(url_info);
            readInfoFromURL(url_info);
        }
/*        if (!sim.awaitedResponses.contains(url_properties)) {
            sim.awaitedResponses.add(url_properties);
            readRTPropertiesFromURL(url_properties);
        }
        if (!sim.awaitedResponses.contains(url_ranges)) {
            sim.awaitedResponses.add(url_ranges);
            readRangesFromURL(url_ranges);
        }
        if (!sim.awaitedResponses.contains(url_k)) {
            sim.awaitedResponses.add(url_k);
            fillVectorFromURL(url_k, k);
        }*/
        if (!sim.awaitedResponses.contains(url_rho)) {
            sim.awaitedResponses.add(url_rho);
            fillVectorFromURL(url_rho, rho);
        }


        if (invariant) {
            String url_cp = baseURL + materialName + "/appInfo/cp.txt";
            if (!sim.awaitedResponses.contains(url_cp)) {
                sim.awaitedResponses.add(url_cp);
                Vector<Double> vector = new Vector<Double>();
                fillVectorFromURL(url_cp, vector);
                cp.add(vector);
            }
            if (!sim.awaitedResponses.contains(url_k)) {
                sim.awaitedResponses.add(url_k);
                fillVectorFromURL(url_k, k);
            }
        } else if (magnetocaloric || barocaloric || elastocaloric || electrocaloric) {
            String url_fields = baseURL + materialName + "/appInfo/Fields.txt";
            if (!sim.awaitedResponses.contains(url_fields)) {
                sim.awaitedResponses.add(url_fields);
                loadFieldsFromUrl(url_fields, new Callback() {
                    @Override
                    public void onSuccess(Object result) {
                        sim.awaitedResponses.remove(url_fields);
                        String url_cp, url_dT;
                        for (double field : fields) {
                            String fieldName = (field == 0) ? "0.0" : field < 0.1 ? String.valueOf(field) : NumberFormat.getFormat("#0.0").format(field);

                            Vector<Double> vector = new Vector<Double>();
                            if (cpThysteresis) {
                                url_cp = baseURL + materialName + "/appInfo/cp_" + fieldName + "T_cooling.txt";
                                sim.awaitedResponses.add(url_cp);
                                fillVectorFromURL(url_cp, vector);
                                dTcooling.add(vector);

                                url_cp = baseURL + materialName + "/appInfo/cp_" + fieldName + "T_heating.txt";
                                vector = new Vector<Double>();
                                sim.awaitedResponses.add(url_cp);
                                fillVectorFromURL(url_cp, vector);
                                dTheating.add(vector);
                            } else {
                                url_cp = baseURL + materialName + "/appInfo/cp_" + fieldName + "T.txt";
                                Vector<Double> field_cp = new Vector<Double>();
                                sim.awaitedResponses.add(url_cp);
                                fillVectorFromURL(url_cp, field_cp);
                                cp.add(field_cp);
                            }
                            if (field != 0) {
                                if (dTThysteresis) {
                                    url_dT = baseURL + materialName + "/appInfo/dT_" + fieldName + "T_cooling.txt";
                                    sim.awaitedResponses.add(url_dT);
                                    fillVectorFromURL(url_dT, vector);
                                    dTcooling.add(vector);

                                    vector = new Vector<Double>();
                                    url_dT = baseURL + materialName + "/appInfo/dT_" + fieldName + "T_heating.txt";
                                    sim.awaitedResponses.add(url_dT);
                                    fillVectorFromURL(url_dT, vector);
                                    dTheating.add(vector);
                                } else {
                                    url_dT = baseURL + materialName + "/appInfo/dT_" + fieldName + "T.txt";
                                    sim.awaitedResponses.add(url_dT);
                                    fillVectorFromURL(url_dT, vector);
                                    cp.add(vector);
                                }
                            }
                        }

                    }

                    @Override
                    public void onFailure(Object reason) {
                        sim.awaitedResponses.remove(url_fields);
                    }
                });
            }
        } else {
            String url_cp = baseURL + materialName + "/appInfo/cp.txt";
            if (!sim.awaitedResponses.contains(url_cp)) {
                sim.awaitedResponses.add(url_cp);
                Vector<Double> vector = new Vector<Double>();
                fillVectorFromURL(url_cp, vector);
                cp.add(vector);
            }
            if (!sim.awaitedResponses.contains(url_k)) {
                url_k = baseURL + materialName + "/appInfo/k_heating.txt";
                sim.awaitedResponses.add(url_k);
                fillVectorFromURL(url_k, kTheating);
                url_k = baseURL + materialName + "/appInfo/k_cooling.txt";
                sim.awaitedResponses.add(url_k);
                fillVectorFromURL(url_k, kTcooling);
            }
        }
    }


    boolean isLoaded() {
/*        boolean isLoaded = true;
        isLoaded = isLoaded && k.size() > 0;
        isLoaded = isLoaded && rho.size() > 0;
        isLoaded = isLoaded && cp.size() > 0;
        if (magnetocaloric) {
            isLoaded = isLoaded && fields != null && fields.size() > 0;
            isLoaded = isLoaded && dTcooling != null && dTcooling.size() > 0;
            isLoaded = isLoaded && dTheating != null && dTheating.size() > 0;
        }
        return isLoaded;*/
        return false;
    }


    public void showTemperatureRanges(int i) {
        String message =
                "Density: " + (tRhoMin == -1 || tRhoMax == -1 ? "undefined" : (tRhoMin + " - " + tRhoMax + " K")) + "\n"
                        + "Specific Heat Capacity: " + (tCpMin == -1 || tCpMax == -1 ? "undefined" : (tCpMin + " - " + tCpMax + " K")) + "\n"
                        + "Thermal Conductivity: " + (tKMin == -1 || tKMax == -1 ? "undefined" : (tKMin + " - " + tKMax + " K")) + "\n"
                        + "Emissivity: " + (tEmissMin == -1 || tEmissMax == -1 ? "undefined" : (tEmissMin + " - " + tEmissMax + " K")) + "\n";

        if (CirSim.editDialog != null && ((EditDialog) sim.editDialog).rangesHTML != null)
            ((EditDialog) sim.editDialog).rangesHTML[i].setTitle(message);
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
                        JSONValue val = JSONParser.parseStrict(text);
                        JSONObject obj = val.isObject();
                        id = obj.get("id").isNumber() != null ? (int) obj.get("id").isNumber().doubleValue() : -1;
                        tMelt = obj.get("melting_point").isNumber() != null ? obj.get("melting_point").isNumber().doubleValue() : -1;
                        shortName = obj.get("short_name") != null ? obj.get("short_name").toString() : "";
                        longName = obj.get("long_name") != null ? obj.get("long_name").toString() : "";

                        JSONObject rtProperties = obj.get("rt_properties").isObject();
                        rhoRT = rtProperties != null && rtProperties.get("density").isNumber() != null ? rtProperties.get("density").isNumber().doubleValue() : -1;
                        cpRT = rtProperties != null && rtProperties.get("specificHeatCapacity").isNumber() != null ? rtProperties.get("specificHeatCapacity").isNumber().doubleValue() : -1;
                        kRT = rtProperties != null && rtProperties.get("thermalConductivity").isNumber() != null ? rtProperties.get("thermalConductivity").isNumber().doubleValue() : -1;
                        emissRT = rtProperties != null && rtProperties.get("emissivity").isNumber() != null ? rtProperties.get("emissivity").isNumber().doubleValue() : -1;

                        JSONObject ranges = obj.get("ranges").isObject();
                        tRhoMax = !ranges.get("density").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("density").toString().replaceAll("[\"']", "").split("-")[0]) : -1;
                        tRhoMin = !ranges.get("density").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("density").toString().replaceAll("[\"']", "").split("-")[1]) : -1;
                        tCpMax = !ranges.get("specificHeatCapacity").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("specificHeatCapacity").toString().replaceAll("[\"']", "").split("-")[0]) : -1;
                        tCpMin = !ranges.get("specificHeatCapacity").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("specificHeatCapacity").toString().replaceAll("[\"']", "").split("-")[1]) : -1;
                        tKMax = !ranges.get("thermalConductivity").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("thermalConductivity").toString().replaceAll("[\"']", "").split("-")[0]) : -1;
                        tKMin = !ranges.get("thermalConductivity").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("thermalConductivity").toString().replaceAll("[\"']", "").split("-")[1]) : -1;
                        tEmissMax = !ranges.get("emissivity").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("emissivity").toString().replaceAll("[\"']", "").split("-")[0]) : -1;
                        tEmissMin = !ranges.get("emissivity").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("emissivity").toString().replaceAll("[\"']", "").split("-")[1]) : -1;

/*                        GWT.log("id: " + id);
                        GWT.log("tMelt: " + tMelt);
                        GWT.log("shortName: " + shortName);
                        GWT.log("longName: " + longName);
                        GWT.log("rhoRT: " + rhoRT);
                        GWT.log("cpRT: " + cpRT);
                        GWT.log("kRT: " + kRT);
                        GWT.log("emissRT: " + emissRT);
                        GWT.log("tRhoMax: " + tRhoMax);
                        GWT.log("tRhoMin: " + tRhoMin);
                        GWT.log("tCpMax: " + tCpMax);
                        GWT.log("tCpMin: " + tCpMin);
                        GWT.log("tKMax: " + tKMax);
                        GWT.log("tKMin: " + tKMin);
                        GWT.log("tEmissMax: " + tEmissMax);
                        GWT.log("tEmissMin: " + tEmissMin);*/
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
}
