package lahde.tccbuilder.client;

import com.google.gwt.core.client.GWT;
import lahde.tccbuilder.client.util.Locale;

import java.lang.Math;
import java.util.*;

import com.google.gwt.user.client.Window;

public class TEHeatEngine extends ThermalControlElement {

    public double ZT;
    public double thermalResistance;

    public TEHeatEngine(int xx, int yy) {
        super(xx, yy);
        lineThickness = 50;
        crossArea = 0.001;
    }

    public TEHeatEngine(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
        lineThickness = 50;
        crossArea = 0.001;
    }

    double[] coldHotTemperatures() {
        ModelMethods.CVinterface cvInter = new ModelMethods.CVinterface();
        cvInter.calculateCoefficients(cvs.get(0).westNeighbour, cvs.get(0));
        double T1 = cvInter.T1;
        // double T1 = cvInter.T2;
        cvInter.calculateCoefficients(cvs.get(cvs.size() - 1), cvs.get(cvs.size() - 1). eastNeighbour);
        // double T2 = cvInter.T1;
        double T2 = cvInter.T2;
        double Tc = T1 > T2 ? T2 : T1;
        double Th = T1 > T2 ? T1 : T2;
        double[] temps = {Tc, Th};
        return temps;
    }

    public double efficiency() {
        double[] temps = coldHotTemperatures();
        double Tc = temps[0];
        double Th = temps[1];
        double eta = (1 - Tc / Th) * (Math.pow(1 + ZT, 0.5) - 1) / (Math.pow(1 + ZT, 0.5) + Tc / Th);
        return eta;
    }

    public Double[] outputPower() {
        double[] temps = coldHotTemperatures();
        double Tc = temps[0];
        double Th = temps[1];
        double p1 = Math.pow((Th - Tc), 2) / 4;
        double p = Math.abs(Th - Tc) * cvs.get(0).k() * length * efficiency() / crossArea;
        if(constK != -1){
            p = Math.abs(Th - Tc) * efficiency() / (constK * length / crossArea);            
        }
        setQgen(-1 * p / length);
        Double[] pp = {p1, p};
        return pp;
    }

    @Override
    public EditInfo getEditInfo(int n) {
        switch (n) {
            case 0:
                return new EditInfo("Name", String.valueOf(name));
            case 1:
                return new EditInfo("Index", index);
            case 2:
                return new EditInfo("Number of control volumes", (double) numCvs);
            case 3:
                EditInfo ei2 = new EditInfo("Color", 0);
                ei2.choice = new Choice();
                for (int ch = 0; ch < sim.colorChoices.size(); ch++) {
                    ei2.choice.add(sim.colorChoices.get(ch));
                }

                ei2.choice.select(Color.colorToIndex(color));
                return ei2;
            case 4:
                return new EditInfo("Length (mm)", length * 1e3);
            case 5:
                return new EditInfo("Cross area (m²)", crossArea);
            case 6:
                return new EditInfo("West contact resistance (m²K/W)", westResistance);
            case 7:
                return new EditInfo("East contact resistance (m²K/W)", eastResistance);
            case 8:
                return new EditInfo("ZT value (/)", ZT);
            case 9:
                return new EditInfo("Heat generation (W/m³)", volumeHeatGeneration);
            case 10:
                return EditInfo.createCheckboxWithField("Constant Density (kg/m³)", !(constRho == -1), constRho);
            case 11:
                return EditInfo.createCheckboxWithField("Constant Specific Heat Capacity (J/kgK)", !(constCp == -1), constCp);
            case 12:
                return EditInfo.createCheckboxWithField("Constant Thermal Conductivity (W/mK)", !(constK == -1), constK);
            case 13:
                return new EditInfo("Initial Temperature (K)", startTemperature);
            case 14:
                return new EditInfo("Heat loss rate to the ambient (W/(m³K))", hTransv);
            default:
                return null;
        }
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        switch (n) {
            case 0:
                name = ei.textf.getText();
                break;
            case 1:
                index = (int) ei.value;
                break;
            case 2:
                numCvs = (int) ei.value;
                break;
            case 3:
                color = Color.translateColorIndex(ei.choice.getSelectedIndex());
                break;
            case 4:
                setNewLength(ei.value);
                // length = ei.value / 1e3;
                break;
            case 5:
                crossArea = ei.value;
                break;
            case 6:
                westResistance = ei.value;
                break;
            case 7:
                eastResistance = ei.value;
                break;
            case 8:
                ZT = ei.value;
                break;                
            case 9:
                volumeHeatGeneration = ei.value;
                break;
            case 10:
                constRho = ei.value;
                break;
            case 11:
                constCp = ei.value;
                break;
            case 12:
                constK = ei.value;
                break;
            case 13:
                startTemperature = (double) ei.value;
                if (startTemperature >= 0) {
                    setTemperatures(startTemperature);
                    // GWT.log(String.valueOf(cvs.get(0).temperature));
                }
                break;
            case 14:
                hTransv = (double) ei.value;
                break;
        }
        updateElement();
    }

    @Override
    int getDumpType() {
        return 700;
    }

    @Override
    void draw(Graphics g) {
        setBbox(point1, point2, lineThickness);
        Point a = new Point(x, y - (int)(lineThickness / 2));
        Point b = new Point(x2, y - (int)(lineThickness / 2));
        Point c = new Point(x2, y + (int)(lineThickness / 2));
        Point d = new Point(x, y + (int)(lineThickness / 2));
        drawLine(g, a, b, 6, color);
        drawLine(g, b, c, 6, color);
        drawLine(g, c, d, 6, color);
        drawLine(g, d, a, 6, color);
    }
}