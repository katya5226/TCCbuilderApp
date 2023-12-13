package lahde.tccbuilder.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.*;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Window;

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
    public boolean cpFields;


    private final CirSim sim;
    public Vector<Double> interpTemps;

    public Vector<Double> rho;
    public Vector<Vector<Double>> k;
    public Vector<Vector<Double>> kHeating;
    public Vector<Vector<Double>> kCooling;
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
        if (materialName.equals("000000-Custom")) {
            this.rho = new Vector<Double>();
            this.k = new Vector<Vector<Double>>();
            this.cp = new Vector<Vector<Double>>();

            Vector<Double> kVector = new Vector<>();
            Vector<Double> cpVector = new Vector<>();
            this.k.add(kVector);
            this.cp.add(cpVector);

            for (int i = 0; i < interpTemps.size(); i++) {
                kVector.add(1.0);
                cpVector.add(100.0);
                rho.add(1000.0);
            }

        } else
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
            // cpFields = line[11].equals("1");
            //cpFields = false;
            //GWT.log(String.valueOf(cpFields));
        } else
            GWT.log("No entry found in flag file for material \"" + materialName + "\"");
    }

    void readFiles() {
        this.rho = new Vector<Double>();
        this.k = new Vector<Vector<Double>>();
        this.kHeating = new Vector<Vector<Double>>();
        this.kCooling = new Vector<Vector<Double>>();
        this.dT = new Vector<Vector<Double>>();
        this.dTheating = new Vector<Vector<Double>>();
        this.dTcooling = new Vector<Vector<Double>>();
        this.cp = new Vector<Vector<Double>>();
        this.cpHeating = new Vector<Vector<Double>>();
        this.cpCooling = new Vector<Vector<Double>>();
        this.fields = new Vector<Double>();
        String CORSproxy = "https://corsproxy.io/?";
        String baseURL = CORSproxy + "http://materials.tccbuilder.org/";
        //String baseURL = GWT.getModuleBaseURL() + "material_data/materials_library/";
        //String baseURL = "http://127.0.0.1:8888/";
        String url_info = baseURL + materialName + "/appInfo/info.json";
        String url_rho = baseURL + materialName + "/appInfo/rho.txt";


        if (!sim.awaitedResponses.contains(url_info)) {
            sim.awaitedResponses.add(url_info);
            readInfoFromURL(url_info, new Callback() {
                @Override
                public void onFailure(Object reason) {
                }

                @Override
                public void onSuccess(Object result) {
                    String url_k = baseURL + materialName + "/appInfo/k.txt";

                    if (!sim.awaitedResponses.contains(url_rho)) {
                        sim.awaitedResponses.add(url_rho);
                        fillVectorFromURL(url_rho, rho, 1000);
                    }

                    if (!cpFields) {
                        if (cpThysteresis) {
                            String url_cp = baseURL + materialName + "/appInfo/cp_cooling.txt";
                            Vector<Double> vector = new Vector<Double>();

                            sim.awaitedResponses.add(url_cp);
                            fillVectorFromURL(url_cp, vector, 200);
                            cpCooling.add(vector);

                            url_cp = baseURL + materialName + "/appInfo/cp_heating.txt";

                            sim.awaitedResponses.add(url_cp);
                            fillVectorFromURL(url_cp, vector, 200);
                            cpHeating.add(vector);
                        }
                        else {
                            String url_cp = baseURL + materialName + "/appInfo/cp.txt";
                            if (!sim.awaitedResponses.contains(url_cp)) {
                                sim.awaitedResponses.add(url_cp);
                                Vector<Double> vector = new Vector<Double>();
                                fillVectorFromURL(url_cp, vector, 200);
                                cp.add(vector);
                            }
                        }
                    } else {
                        String url_cp, url_dT;
                        for (double field : fields) {
                            String fieldName = (field == 0) ? "0.0" : field < 0.1 ? String.valueOf(field) : NumberFormat.getFormat("#0.0").format(field);
                            if (electrocaloric)
                                fieldName = String.valueOf(field);

                            Vector<Double> vector;
                            if (cpThysteresis) {
                                url_cp = baseURL + materialName + "/appInfo/cp_" + fieldName;
                                if (magnetocaloric)
                                    url_cp += "T_cooling.txt";
                                else if (electrocaloric)
                                    url_cp += "MVm_cooling.txt";
                                else if (elastocaloric || barocaloric)
                                    url_cp += "bar_cooling.txt";
                                vector = new Vector<Double>();

                                sim.awaitedResponses.add(url_cp);
                                fillVectorFromURL(url_cp, vector, 200);
                                cpCooling.add(vector);

                                url_cp = baseURL + materialName + "/appInfo/cp_" + fieldName;
                                if (magnetocaloric)
                                    url_cp += "T_heating.txt";
                                else if (electrocaloric)
                                    url_cp += "MVm_heating.txt";
                                else if (elastocaloric || barocaloric)
                                    url_cp += "bar_heating.txt";
                                vector = new Vector<Double>();

                                sim.awaitedResponses.add(url_cp);
                                fillVectorFromURL(url_cp, vector, 200);
                                cpHeating.add(vector);
                            } else {
                                url_cp = baseURL + materialName + "/appInfo/cp_" + fieldName;
                                if (magnetocaloric)
                                    url_cp += "T.txt";
                                else if (electrocaloric)
                                    url_cp += "MVm.txt";
                                else if (elastocaloric || barocaloric)
                                    url_cp += "bar.txt";
                                vector = new Vector<Double>();

                                sim.awaitedResponses.add(url_cp);
                                fillVectorFromURL(url_cp, vector, 200);
                                cp.add(vector);
                            }
                            if (field != 0) {
                                if (dTThysteresis) {
                                    url_dT = baseURL + materialName + "/appInfo/dT_" + fieldName;
                                    if (magnetocaloric)
                                        url_dT += "T_cooling.txt";
                                    else if (electrocaloric)
                                        url_dT += "MVm_cooling.txt";
                                    else if (elastocaloric || barocaloric)
                                        url_dT += "bar_cooling.txt";
                                    vector = new Vector<Double>();

                                    sim.awaitedResponses.add(url_dT);
                                    fillVectorFromURL(url_dT, vector, 0);
                                    dTcooling.add(vector);

                                    url_dT = baseURL + materialName + "/appInfo/dT_" + fieldName;
                                    if (magnetocaloric)
                                        url_dT += "T_heating.txt";
                                    else if (electrocaloric)
                                        url_dT += "MVm_heating.txt";
                                    else if (elastocaloric || barocaloric)
                                        url_dT += "bar_heating.txt";
                                    vector = new Vector<Double>();

                                    sim.awaitedResponses.add(url_dT);
                                    fillVectorFromURL(url_dT, vector, 0);
                                    dTheating.add(vector);
                                } else {
                                    url_dT = baseURL + materialName + "/appInfo/dT_" + fieldName;
                                    if (magnetocaloric)
                                        url_dT += "T.txt";
                                    else if (electrocaloric)
                                        url_dT += "MVm.txt";
                                    else if (elastocaloric || barocaloric)
                                        url_dT += "bar.txt";
                                    vector = new Vector<Double>();

                                    sim.awaitedResponses.add(url_dT);
                                    fillVectorFromURL(url_dT, vector, 0);
                                    dT.add(vector);
                                }
                            }

                        }

                    }
                    Vector vector;
                    if (kThysteresis) {
                        vector = new Vector<Double>();
                        url_k = baseURL + materialName + "/appInfo/k_cooling.txt";

                        sim.awaitedResponses.add(url_k);
                        fillVectorFromURL(url_k, vector, 1);
                        kCooling.add(vector);

                        url_k = baseURL + materialName + "/appInfo/k_heating.txt";
                        vector = new Vector<Double>();

                        sim.awaitedResponses.add(url_k);
                        fillVectorFromURL(url_k, vector, 1);
                        kHeating.add(vector);
                    } else {
                        url_k = baseURL + materialName + "/appInfo/k.txt";
                        vector = new Vector<Double>();

                        sim.awaitedResponses.add(url_k);
                        fillVectorFromURL(url_k, vector, 1);
                        k.add(vector);
                    }

                }
            });
        }

    }


    boolean isLoaded() {
        if (rho == null) return false;
        boolean isLoaded = true;
        isLoaded = isLoaded && !rho.isEmpty();
        if (invariant) {
            isLoaded = isLoaded && !cp.isEmpty();
            isLoaded = isLoaded && !k.isEmpty();
        } else if (magnetocaloric || barocaloric || electrocaloric || elastocaloric) {
            isLoaded = isLoaded && fields != null && !fields.isEmpty();
            if (dTThysteresis) {
                isLoaded = isLoaded && dTcooling != null && !dTcooling.isEmpty();
                isLoaded = isLoaded && dTheating != null && !dTheating.isEmpty();
            } else {
                isLoaded = isLoaded && !dT.isEmpty();
            }

            if (cpThysteresis) {
                isLoaded = isLoaded && cpCooling != null && !cpCooling.isEmpty();
                isLoaded = isLoaded && cpHeating != null && !cpHeating.isEmpty();
            } else {
                isLoaded = isLoaded && !cp.isEmpty();
            }

            if (kThysteresis) {
                isLoaded = isLoaded && kCooling != null && !kCooling.isEmpty();
                isLoaded = isLoaded && kHeating != null && !kHeating.isEmpty();
            } else {
                isLoaded = isLoaded && !k.isEmpty();
            }

        } else {
            isLoaded = isLoaded && !cp.isEmpty();
            if (kThysteresis) {
                isLoaded = isLoaded && kCooling != null && !kCooling.isEmpty();
                isLoaded = isLoaded && kHeating != null && !kHeating.isEmpty();
            } else {
                isLoaded = isLoaded && !k.isEmpty();
            }
        }
        return isLoaded;
    }


    public void showTemperatureRanges(Choice choice) {
        String flag = "";
        if (invariant) flag = "Invariant material";
        if (thermoelectric) flag = "Thermoelectric material";
        if (temperatureInducedPhaseChange) flag = "Material with a temperature-induced phase change";
        if (magnetocaloric) flag = "Magnetocaloric material";
        if (electrocaloric) flag = "Electrocaloric material";
        if (elastocaloric) flag = "Elastocaloric material";
        if (barocaloric) flag = "Barocaloric material";
        String message = flag + "\nTemperature ranges: \n" +
                "Density: " + (tRhoMin == -1 || tRhoMax == -1 ? "undefined" : (tRhoMin + " - " + tRhoMax + " K")) + "\n"
                + "Specific Heat Capacity: " + (tCpMin == -1 || tCpMax == -1 ? "undefined" : (tCpMin + " - " + tCpMax + " K")) + "\n"
                + "Thermal Conductivity: " + (tKMin == -1 || tKMax == -1 ? "undefined" : (tKMin + " - " + tKMax + " K")) + "\n"
                + "Emissivity: " + (tEmissMin == -1 || tEmissMax == -1 ? "undefined" : (tEmissMin + " - " + tEmissMax + " K")) + "\n";

        choice.setTitle(message);
    }


    void readInfoFromURL(String url, final Callback callback) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    GWT.log("File Error Response", exception);
                    callback.onFailure(null);
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
                        JSONArray fieldsArray = obj.get("fields") != null ? obj.get("fields").isArray() : new JSONArray();
                        GWT.log("FIELDS SIZE: " + String.valueOf(fieldsArray.size()));
                        // if (electrocaloric) fields.add((double) 0);  // TODO: CORRECT THIS!!
                        for (int i = 0; i < fieldsArray.size(); i++) {
                            fields.add(Double.parseDouble(fieldsArray.get(i).isNumber().toString()));
                        }
                        JSONObject rtProperties = obj.get("rt_properties").isObject();
                        rhoRT = rtProperties != null && rtProperties.get("density").isNumber() != null ? rtProperties.get("density").isNumber().doubleValue() : -1;
                        cpRT = rtProperties != null && rtProperties.get("specificHeatCapacity").isNumber() != null ? rtProperties.get("specificHeatCapacity").isNumber().doubleValue() : -1;
                        kRT = rtProperties != null && rtProperties.get("thermalConductivity").isNumber() != null ? rtProperties.get("thermalConductivity").isNumber().doubleValue() : -1;
                        emissRT = rtProperties != null && rtProperties.get("emissivity").isNumber() != null ? rtProperties.get("emissivity").isNumber().doubleValue() : -1;

                        JSONObject ranges = obj.get("ranges").isObject();
                        tRhoMax = !ranges.get("density").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("density").toString().replaceAll("[\"']", "").split("-")[1]) : -1;
                        tRhoMin = !ranges.get("density").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("density").toString().replaceAll("[\"']", "").split("-")[0]) : -1;
                        tCpMax = !ranges.get("specificHeatCapacity").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("specificHeatCapacity").toString().replaceAll("[\"']", "").split("-")[1]) : -1;
                        tCpMin = !ranges.get("specificHeatCapacity").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("specificHeatCapacity").toString().replaceAll("[\"']", "").split("-")[0]) : -1;
                        tKMax = !ranges.get("thermalConductivity").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("thermalConductivity").toString().replaceAll("[\"']", "").split("-")[1]) : -1;
                        tKMin = !ranges.get("thermalConductivity").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("thermalConductivity").toString().replaceAll("[\"']", "").split("-")[0]) : -1;
                        tEmissMax = !ranges.get("emissivity").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("emissivity").toString().replaceAll("[\"']", "").split("-")[1]) : -1;
                        tEmissMin = !ranges.get("emissivity").toString().replaceAll("[\"']", "").isEmpty() ? Double.parseDouble(ranges.get("emissivity").toString().replaceAll("[\"']", "").split("-")[0]) : -1;

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
                        callback.onSuccess(null);
                        sim.awaitedResponses.remove(url);
                    } else if (response.getStatusCode() == Response.SC_NOT_FOUND) {
                        GWT.log("File \"" + url + "\" not found");
                        callback.onFailure(null);
                        sim.awaitedResponses.remove(url);
                    } else {
                        GWT.log("Bad file server response: " + response.getStatusText());
                        callback.onFailure(null);
                        sim.awaitedResponses.remove(url);
                    }
                }
            });
        } catch (RequestException e) {
            GWT.log("failed file reading", e);
        }

    }


    void fillVectorFromURL(String url, Vector<Double> vector, double defaultValue) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    GWT.log("File Error Response", exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        String text = response.getText().trim();
                        for (String line : text.split("\n"))
                            vector.add(Double.parseDouble(line));
                        if (vector.size() == 1) {
                            for (int i = 1; i < 20000; i++)
                                vector.add(vector.get(0));
                        }
                        sim.awaitedResponses.remove(url);

                    } else if (response.getStatusCode() == Response.SC_NOT_FOUND) {
                        GWT.log("File \"" + url + "\" not found");
                        Window.alert(url + " not found, loading default value: " + defaultValue);
                        for (int i = 1; i < 20000; i++)
                            vector.add(defaultValue);
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

    @Override
    public String toString() {
        StringBuilder jsonStyleLog = new StringBuilder("{\n");

        jsonStyleLog.append("\"Material Name\": \"" + (materialName != null ? materialName : "N/A") + "\",\n");
        jsonStyleLog.append("\"Name\": " + name + ",\n");
        jsonStyleLog.append("\"Invariant\": " + invariant + ",\n");
        jsonStyleLog.append("\"Thermoelectric\": " + thermoelectric + ",\n");
        jsonStyleLog.append("\"Temperature Induced Phase Change\": " + temperatureInducedPhaseChange + ",\n");
        jsonStyleLog.append("\"Electrocaloric\": " + electrocaloric + ",\n");
        jsonStyleLog.append("\"Magnetocaloric\": " + magnetocaloric + ",\n");
        jsonStyleLog.append("\"Barocaloric\": " + barocaloric + ",\n");
        jsonStyleLog.append("\"Elastocaloric\": " + elastocaloric + ",\n");
        jsonStyleLog.append("\"Cp Thysteresis\": " + cpThysteresis + ",\n");
        jsonStyleLog.append("\"dT Thysteresis\": " + dTThysteresis + ",\n");
        jsonStyleLog.append("\"k Thysteresis\": " + kThysteresis + ",\n");
        jsonStyleLog.append("\"Interp Temps\": " + interpTemps.size() + ",\n");
        jsonStyleLog.append("\"Rho\": " + rho + ",\n");
        jsonStyleLog.append("\"K\": " + k + ",\n");
        jsonStyleLog.append("\"K Heating\": " + kHeating + ",\n");
        jsonStyleLog.append("\"K Cooling\": " + kCooling + ",\n");
        jsonStyleLog.append("\"dT\": " + dT + ",\n");
        jsonStyleLog.append("\"dT Heating\": " + dTheating + ",\n");
        jsonStyleLog.append("\"dT Cooling\": " + dTcooling + ",\n");
        jsonStyleLog.append("\"Cp\": " + cp + ",\n");
        jsonStyleLog.append("\"Cp Heating\": " + cpHeating + ",\n");
        jsonStyleLog.append("\"Cp Cooling\": " + cpCooling + ",\n");
        jsonStyleLog.append("\"Fields\": " + fields + ",\n");
        jsonStyleLog.append("\"Temperatures ST\": " + temperaturesST + ",\n");
        jsonStyleLog.append("\"Low Field Entropies\": " + lowFieldEntropies + ",\n");
        jsonStyleLog.append("\"High Field Entropies\": " + highFieldEntropies + ",\n");
        jsonStyleLog.append("\"Derivative ST Low\": " + derSTlow + ",\n");
        jsonStyleLog.append("\"Derivative ST High\": " + derSThigh + ",\n");
        jsonStyleLog.append("\"RhoRT\": " + rhoRT + ",\n");
        jsonStyleLog.append("\"CpRT\": " + cpRT + ",\n");
        jsonStyleLog.append("\"KRT\": " + kRT + ",\n");
        jsonStyleLog.append("\"EmissRT\": " + emissRT + ",\n");
        jsonStyleLog.append("\"TRhoMin\": " + tRhoMin + ",\n");
        jsonStyleLog.append("\"TRhoMax\": " + tRhoMax + ",\n");
        jsonStyleLog.append("\"TCpMin\": " + tCpMin + ",\n");
        jsonStyleLog.append("\"TCpMax\": " + tCpMax + ",\n");
        jsonStyleLog.append("\"TKMin\": " + tKMin + ",\n");
        jsonStyleLog.append("\"TKMax\": " + tKMax + ",\n");
        jsonStyleLog.append("\"TEmissMin\": " + tEmissMin + ",\n");
        jsonStyleLog.append("\"TEmissMax\": " + tEmissMax + ",\n");
        jsonStyleLog.append("\"TMelt\": " + tMelt + ",\n");
        jsonStyleLog.append("\"ID\": " + id + ",\n");
        jsonStyleLog.append("\"Short Name\": \"" + (shortName != null ? shortName : "N/A") + "\",\n");
        jsonStyleLog.append("\"Long Name\": \"" + (longName != null ? longName : "N/A") + "\"\n");

        jsonStyleLog.append("}");
        return jsonStyleLog.toString();


    }
}
