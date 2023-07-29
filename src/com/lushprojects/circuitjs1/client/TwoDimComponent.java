package com.lushprojects.circuitjs1.client;

import com.google.gwt.core.client.GWT;
import com.lushprojects.circuitjs1.client.util.Locale;

import java.lang.Math;
import java.text.MessageFormat;
import java.util.*;

import com.google.gwt.user.client.Window;

public class TwoDimComponent extends CircuitElm implements Comparable<TwoDimComponent> {
    double resistance;
    Color color;
    double length, height;
    String name;
    int index;
    Material material;
    int numCvs, n, m; // total # of CVs and # of CVs in x and y directions
    double [] resistances;
    TwoDimComponent [] neighbours;
    int [] boundaries;
    double constRho, constCp, constK;
    Vector<TwoDimCV> cvs;
    boolean isDisabled;
    boolean field;
    int fieldIndex;

    public TwoDimComponent(int xx, int yy) {
        super(xx, yy);
        initializeComponent();
        index = -1;
        for (TwoDimComponent c : sim.simTwoDimComponents) {
            if (c.index > index)
                index = c.index;
        }
        index++;
        material = sim.materialHashMap.get("100001-Inox");

        if (!material.isLoaded())
            material.readFiles();

        isDisabled = false;
        field = false;
        fieldIndex = 1;
        buildComponent();

    }

    public TwoDimComponent(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        initializeComponent();

        index = Integer.parseInt(st.nextToken());
        material = sim.materialHashMap.get(st.nextToken(" "));
        if (!material.isLoaded()) {
            material.readFiles();
        }

        color = Color.translateColorIndex(Integer.parseInt(st.nextToken()));
        length = Double.parseDouble(st.nextToken());
        name = st.nextToken();
        numCvs = Integer.parseInt(st.nextToken());
        isDisabled = false;
        field = false;
        fieldIndex = 1;
        buildComponent();

    }

    public void initializeComponent() {
        resistance = 1000;
        color = Color.green;
        calculateLengthHeight();
        name = "TwoDimComponent";
        n = m = 3;
        numCvs = 9;
        cvs = new Vector<TwoDimCV>();
        resistances = new double []{0.0, 0.0, 0.0, 0.0};
        neighbours = new TwoDimComponent [4];
        boundaries = new int []{51, 51, 51, 51};
        constRho = -1;
        constCp = -1;
        constK = -1;
        double tmpDx = length / n;
        double tmpDy = height / m;
        if ((!(tmpDx < 1e-6) || tmpDx == 0) && (!(tmpDy < 1e-6) || tmpDy == 0)) {
            setDxDy(tmpDx, tmpDy);
            sim.simTwoDimComponents.add(this);  // TODO
            Collections.sort(sim.trackedTemperatures);  // TODO
            //sim.trackedTemperatures.add(this);  // TODO
        }
    }

    public void calculateLengthHeight() {
        // TODO: 2Dcomponent can only be made horizontaly,
        // as a rectangle with edge coordinates {(x, y), (x2, y), (x2, y2), (x, y2)},
        // where difference between x-es is length and difference between y-s is height. 
        length = (x2 - x) / sim.selectedLengthUnit.conversionFactor;
        height = (y2 - y) / sim.selectedLengthUnit.conversionFactor;
        //gridSize px = 1 unit of measurement
        length /= sim.gridSize;
        height /= sim.gridSize;
    }

    void cvNeighbours() {
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                int k = j * n + i;
                TwoDimCV cv = cvs.get(k);
                if (j == 0 && m != 1) {
                    cv.neighbours[3] = cvs.get(k + n);
                    if (i == 0) {
                        cv.neighbours[1] = cvs.get(k + 1);
                    }
                    else if (i == n - 1) {
                        cv.neighbours[0] = cvs.get(k - 1);
                    }
                    else {
                        cv.neighbours[0] = cvs.get(k - 1);
                        cv.neighbours[1] = cvs.get(k + 1);
                    }
                }
                else if ((0 < j) && (j < m - 1)) {
                    cv.neighbours[3] = cvs.get(k + n);
                    cv.neighbours[2] = cvs.get(k - n);
                    if (i == 0) {
                        cv.neighbours[1] = cvs.get(k + 1);
                    }   
                    else if (i == n - 1) {
                        cv.neighbours[0] = cvs.get(k - 1);
                    }
                    else {
                        cv.neighbours[0] = cvs.get(k - 1);
                        cv.neighbours[1] = cvs.get(k + 1);
                    }
                }
                else if (j == m - 1) {
                    cv.neighbours[2] = cvs.get(k - n);
                    if (i == 0) {
                        cv.neighbours[1] = cvs.get(k + 1);
                    }
                    else if (i == n - 1) {
                        cv.neighbours[0] = cvs.get(k - 1);
                    }
                    else {
                        cv.neighbours[0] = cvs.get(k - 1);
                        cv.neighbours[1] = cvs.get(k + 1);
                    }
                }
            }        
        }
    }

    public void buildComponent() {
        cvs.clear();
        for (int i = 0; i < numCvs; i++) {
            cvs.add(new TwoDimCV(i, this));
            if (constRho != -1) {
                cvs.get(i).constRho = constRho;
            }
            if (constCp != -1) {
                cvs.get(i).constCp = constCp;
            }
            if (constK != -1) {
                cvs.get(i).constK = constK;
            }
        }
        for (int j = 0; j < m; j++) {
            cvs.get(j * n).resistances[0] = resistances[0];
        }
        for (int j = 0; j < m; j++) {
            cvs.get((j + 1) * n - 1).resistances[1] = resistances[1];
        }
        for (int i = 0; i < n; i++) {
            cvs.get(i).resistances[2] = resistances[2];
        }
        for (int i = (m - 1) * n; i < numCvs; i++) {
            cvs.get(i).resistances[3] = resistances[3];
        }
        cvNeighbours();
        setxy(0.0, 0.0);
        calculateConductivities();
    }

    void calculateConductivities() {
        for (TwoDimCV cv : cvs) {
            cv.calculateConductivities();
        }
    }

    public void setTemperatures(double temp) {
        for (int i = 0; i < numCvs; i++) {
            cvs.get(i).temperature = temp;
            cvs.get(i).temperatureOld = temp;
        }
    }

    public void setxy(double xOffset, double yOffset) {
        for (int i = 0; i < numCvs; i++) {
            cvs.get(i).setxy(xOffset, yOffset);
        }
    }

    public void updateModes() {
        for (int i = 0; i < numCvs; i++) {
            TwoDimCV cv = cvs.get(i);
            if (cv.temperature >= cv.temperatureOld) {
                cv.mode = 1;
            } else if (cv.temperature < cv.temperatureOld) {
                cv.mode = -1;
            }
        }
    }

    @Override
    public int compareTo(TwoDimComponent o) {
        return index - o.index;
    }


    int getDumpType() {
        return 'r';
    }

    String dump() {  // TODO
        return "520 " + point1.x + " " + point1.y + " " + point2.x + " " +
        point2.y + " 0 " + index + " " + material.materialName + " " +
        Color.colorToIndex(color) + " " + length + " " + name + " " + numCvs + "\n";
    }

    Point ps3, ps4;

    void setPoints() {
        super.setPoints();
        calcLeads(32);
        ps3 = new Point();
        ps4 = new Point();
    }

    void draw(Graphics g) {
        int hs = 13;
        setBbox(point1, point2, hs);

        double tmpDx = length / numCvs;
        if (tmpDx < 1e-6 && tmpDx != 0) {
            //Window.alert("TwoDimComponent can't have a dx < 1µ, current is " + tmpDx);
            drawThickerLine(g, point1, point2, Color.red.getHexValue());
            setDxDy(tmpDx, 0.01);
            isDisabled = true;
        } else {
            drawThickerLine(g, point1, point2, color.getHexValue());
            setDxDy(tmpDx, 0.01);
            isDisabled = false;
        }


        doDots(g);
        drawPosts(g);

    }

    void calculateCurrent() {
        current = (volts[0] - volts[1]) / resistance;
        // System.out.print(this + " res current set to " + current + "\n");
    }

    void stamp() {
        sim.stampResistor(nodes[0], nodes[1], resistance);
    }


    double[] listTemps() {
        double[] temps = new double[numCvs];
        for (int i = 0; i < temps.length; i++) {
            temps[i] = Math.round(cvs.get(i).temperature * 100) / 100.0;
        }
        return temps;
    }

    void getInfo(String[] arr) {
        arr[0] = name;
        // getBasicInfo(arr);
        arr[1] = "TwoDimComponent index = " + String.valueOf(index);
        arr[2] = "Material = " + material.materialName;

        double tmpLength = length;
        if (tmpLength < 1e-3) { // less than 1 millimeter
            tmpLength = length * 1e6; // convert to micrometers
            arr[3] = "Length = " + (Math.round(tmpLength * 1000) / 1000.0) + " µm";
        } else if (tmpLength < 1) { // less than 1 meter
            tmpLength = length * 1e3; // convert to millimeters
            arr[3] = "Length = " + (Math.round(tmpLength * 1000) / 1000.0) + " mm";
        } else {
            arr[3] = "Length = " + (Math.round(tmpLength * 1000) / 1000.0) + " m";
        }

        arr[4] = "#CVs = " + numCvs;

        double dx = cvs.get(0).dx; // dx is in meters
        if (dx < 1e-3) { // less than 1 millimeter
            dx = dx * 1e6; // convert to micrometers
            arr[5] = "CV dx = " + (Math.round(dx * 1000) / 1000.0) + " µm";
        } else if (dx < 1) { // less than 1 meter
            dx = dx * 1e3; // convert to millimeters
            arr[5] = "CV dx = " + (Math.round(dx * 1000) / 1000.0) + " mm";
        } else {
            arr[5] = "CV dx = " + (Math.round(dx * 1000) / 1000.0) + " m";
        }
    }

    @Override
    String getScopeText(int v) {
        return Locale.LS("component") + ", " + getUnitText(resistance, Locale.ohmString);
    }

    /*  */

    public EditInfo getEditInfo(int n) {
        switch (n) {
            case 0:
                return new EditInfo("Name", String.valueOf(name));
            case 1:
                return new EditInfo("Index", index);
            case 2:
                EditInfo ei = new EditInfo("Material", 0);
                ei.choice = new Choice();
                for (String m : sim.materialNames) {
                    ei.choice.add(m);
                }
                ei.choice.select(sim.materialNames.indexOf(material.materialName));
                return ei;
            case 3:
                return new EditInfo("Number of control volumes", (double) numCvs);
            case 4:
                EditInfo ei2 = new EditInfo("Color", 0);
                ei2.choice = new Choice();
                for (int ch = 0; ch < sim.colorChoices.size(); ch++) {
                    ei2.choice.add(sim.colorChoices.get(ch));
                }

                ei2.choice.select(Color.colorToIndex(color));
                return ei2;
            case 5:
                return new EditInfo("Length (" + sim.selectedLengthUnit.unitName + ")", length);
            case 6:
                return new EditInfo("Left contact resistance (mK/W)", resistances[0]);
            case 7:
                return new EditInfo("Right contact resistance (mK/W)", resistances[1]);
            default:
                return null;
        }
    }

    public void setEditValue(int n, EditInfo ei) {

        switch (n) {
            case 0:
                name = ei.textf.getText();
                break;
            case 1:
                index = (int) ei.value;
                break;
            case 2:
                material = sim.materialHashMap.get(sim.materialNames.get(ei.choice.getSelectedIndex()));
                if (!material.isLoaded())
                    material.readFiles();
                break;
            case 3:
                numCvs = (int) ei.value;
                break;
            case 4:
                color = Color.translateColorIndex(ei.choice.getSelectedIndex());
                break;
            case 5:
                double prevLength = length;
                length = (ei.value / sim.selectedLengthUnit.conversionFactor);

                double ratio = length / prevLength;
                int deltaX = (int) ((point2.x - point1.x) * ratio);
                point2.x = (point1.x + deltaX);
                point2.x = sim.snapGrid(point2.x);
                break;
            case 6:
                resistances[0] = ei.value;
                break;
            case 7:
                resistances[1] = ei.value;
                break;
        }

        //TODO: Implement this with better functionality

        if (length / numCvs < 1e-6) {
            String input = String.valueOf(numCvs);
            if (!isDisabled)
                Window.alert("TwoDimComponent can't have a dx < 1e-6, current is " + (length / numCvs) + "\n Please enter a smaller number of control volumes!");
            isDisabled = true;
        } else {
            setDxDy(length / numCvs, 0.01);
            buildComponent();
            isDisabled = false;
        }

    }

    int getShortcut() {
        return 0;
    }


    double getResistance() {
        return resistance;
    }

    void setResistance(double r) {
        resistance = r;
    }

    public void setConstProperties(Vector<Double> newProps) {
        if (newProps.size() != 3) {
            GWT.log("Vector of new properties must contain three values.");
        }
        for (int i = 0; i < numCvs; i++) {
            cvs.get(i).constRho = newProps.get(0);
            cvs.get(i).constCp = newProps.get(1);
            cvs.get(i).constK = newProps.get(2);
        }
    }

    public void setConstProperty(String property, double value) {
        if (property.equals("rho")) {
            for (int i = 0; i < numCvs; i++) {
                cvs.get(i).constRho = value;
            }
        }
        if (property.equals("cp")) {
            for (int i = 0; i < numCvs; i++) {
                cvs.get(i).constCp = value;
            }
        }
        if (property.equals("k")) {
            for (int i = 0; i < numCvs; i++) {
                cvs.get(i).constK = value;
            }
        }
    }

    public void setDxDy(double dx, double dy) {
        for (TwoDimCV cv : cvs) {
            cv.dx = dx;
            cv.dy = dy;
        }
    }

    public void setQgen(double qGen) {
        for (TwoDimCV cv : cvs) {
            cv.qGen = qGen;
        }
    }

    public void setConstactResistance(String side, double r) {
        if (side.equals("west")) {
            resistances[0] = r;
            for (int j = 0; j < m; j++) {
                cvs.get(j * n).resistances[0] = resistances[0];
            }
        }
        if (side.equals("east")) {
            resistances[1] = r;
            for (int j = 0; j < m; j++) {
                cvs.get((j + 1) * n - 1).resistances[1] = resistances[1];
            }
        }
        if (side.equals("south")) {
            resistances[2] = r;
            for (int i = 0; i < n; i++) {
                cvs.get(i).resistances[2] = resistances[2];
            }
        }
        if (side.equals("north")) {
            resistances[3] = r;
            for (int i = (m - 1) * n; i < numCvs; i++) {
                cvs.get(i).resistances[3] = resistances[3];
            }
        }
    }

    public void magnetize() {
        // Check if given component's' material's magnetocaloric flag is TRUE;
        // if not, abort and inform the user.
        for (int i = 0; i < cvs.size(); i++) {
            cvs.get(i).magnetize();
        }
        // GWT.log("Finished (de)magnetization.");
        field = !field;
    }

}
