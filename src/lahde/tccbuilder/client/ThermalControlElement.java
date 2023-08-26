package lahde.tccbuilder.client;

import com.google.gwt.core.client.GWT;

import java.lang.Math;
import java.util.*;

import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Window;

public class ThermalControlElement extends CircuitElm implements Comparable<ThermalControlElement> {
    public double length;
    Color color;
    public String name;
    public int index;
    public int numCvs;
    public double westResistance;
    public double eastResistance;
    public ThermalControlElement westNeighbour;
    public ThermalControlElement eastNeighbour;
    public int westBoundary;
    public int eastBoundary;
    public double constRho;
    public double constCp;
    public double constK;

    public Vector<ControlVolume> cvs;

    public boolean isDisabled;
    public boolean field;
    public int fieldIndex;

    public ThermalControlElement(int xx, int yy) {
        super(xx, yy);
        initializeThermalControlElement();

        index = -1;
        for (ThermalControlElement c : sim.simulation1D.simTCEs) {
            if (c.index > index)
                index = c.index;
        }
        index++;


        isDisabled = false;
        field = false;
        fieldIndex = 1;
        buildThermalControlElement();

    }

    public ThermalControlElement(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        initializeThermalControlElement();
        index = Integer.parseInt(st.nextToken());
        length = Double.parseDouble(st.nextToken());
        name = st.nextToken();
        numCvs = Integer.parseInt(st.nextToken());
        color = Color.translateColorIndex(Integer.parseInt(st.nextToken()));

        isDisabled = false;
        field = false;
        fieldIndex = 1;
        buildThermalControlElement();

        for (int i = 0; i < numCvs; i++) {
            int j = Integer.parseInt(st.nextToken());
            cvs.get(i).material = sim.materialHashMap.get(sim.materialNames.get(j));

        }
    }

    public void initializeThermalControlElement() {
        color = Color.gray;
        calculateLength();
        name = this.getClass().getSimpleName().replace("Elm", "");
        numCvs = 3;
        cvs = new Vector<ControlVolume>();
        westResistance = 0.0; // This is yet to be linked to the CV.
        eastResistance = 0.0;
        westNeighbour = null;
        eastNeighbour = null;
        westBoundary = 51;
        eastBoundary = 52;
        constRho = -1;
        constCp = -1;
        constK = -1;
        double tmpDx = length / numCvs;
        if (!(tmpDx < 1e-6) || tmpDx == 0) {
            set_dx(tmpDx);
            sim.simulation1D.simTCEs.add(this);
            Collections.sort(sim.trackedTemperatures);
            sim.trackedTemperatures.add(this);
        }
    }

    public void calculateLength() {
        length = Math.sqrt(Math.pow((y2 - y), 2) + Math.pow((x2 - x), 2)) / sim.selectedLengthUnit.conversionFactor;
        //gridSize px = 1 unit of measurement
        length /= sim.gridSize;
    }

    public void buildThermalControlElement() {
        cvs.clear();
        for (int i = 0; i < numCvs; i++) {
            cvs.add(new ControlVolume(i));
            cvs.get(i).parent = this;

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

        cvs.get(0).westResistance = westResistance;
        cvs.get(numCvs - 1).eastResistance = eastResistance;
    }

    public void set_starting_temps(double start_temp) {
        for (int i = 0; i < numCvs; i++) {
            cvs.get(i).temperature = start_temp;
            cvs.get(i).temperatureOld = start_temp;
        }
    }

    public void setMaterial(Material m) {
        for (ControlVolume controlVolume : cvs)
            controlVolume.material = m;
    }

    public void updateModes() {
        for (int i = 0; i < numCvs; i++) {
            ControlVolume cv = cvs.get(i);
            if (cv.temperature >= cv.temperatureOld) {
                cv.mode = 1;
            } else {
                cv.mode = -1;
            }
        }
    }

    @Override
    public int compareTo(ThermalControlElement o) {
        return this.index - o.index;
    }


    @Override
    String dump() {
        StringBuilder sb = new StringBuilder();

        sb.append(getDumpType()).append(" ");
        sb.append(point1.x).append(' ').append(point1.y).append(' ');
        sb.append(point2.x).append(' ').append(point2.y).append(' ');
        sb.append("0 ");
        sb.append(index).append(' ');

        sb.append(length).append(' ');
        sb.append(name).append(' ');
        sb.append(numCvs).append(' ');
        sb.append(Color.colorToIndex(color)).append(' ');


        for (ControlVolume cv : cvs) {
            int i = sim.materialNames.indexOf(cv.material.materialName);
            sb.append(i).append(" ");
        }
        // sb.append(material.materialName).append(' ');  // TODO


        return sb.toString();
    }


    @Override
    void setPoints() {
        super.setPoints();
        calcLeads(32);
    }

    @Override
    void draw(Graphics g) {
        int hs = 13;
        setBbox(point1, point2, hs);

        double tmpDx = length / numCvs;
        if (tmpDx < 1e-6 && tmpDx != 0) {
            //Window.alert("TCE can't have a dx < 1µ, current is " + tmpDx);
            drawThickestLine(g, point1, point2, Color.red.getHexValue());
            set_dx(tmpDx);
            isDisabled = true;
        } else {
            drawThickestLine(g, point1, point2, color.getHexValue());
            set_dx(tmpDx);
            isDisabled = false;
        }


    }

    double[] listTemps() {
        double[] temps = new double[numCvs];
        for (int i = 0; i < temps.length; i++) {
            temps[i] = Math.round(cvs.get(i).temperature * 100) / 100.0;
        }
        return temps;
    }

    @Override
    void getInfo(String[] arr) {
        arr[0] = name;
        arr[1] = "TCE index = " + index;
        arr[2] = "Material = "; // + this.material.materialName;  // To do: list materials of CVs
        arr[3] = "Length = " + CirSim.formatLength(length);
        arr[4] = "#CVs = " + numCvs;
        arr[5] = "CV dx = " + CirSim.formatLength(cvs.get(0).dx);
    }


    @Override
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
                ei.choice.addMouseOverHandler(new MouseOverHandler() {
                    @Override
                    public void onMouseOver(MouseOverEvent e) {
                        Material m = sim.materialHashMap.get(ei.choice.getSelectedItemText());
                        if (m != null)
                            m.showTemperatureRanges(ei.choice);
                    }
                });
                ei.choice.select(sim.materialNames.indexOf("100001-Inox"));

                // ei.choice.select(sim.materialNames.indexOf(this.material.materialName));  // TODO
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
                return new EditInfo("Length (" + sim.selectedLengthUnit.unitName + ")", length * CircuitElm.sim.selectedLengthUnit.conversionFactor);
            case 6:
                return new EditInfo("West contact resistance (mK/W)", westResistance);
            case 7:
                return new EditInfo("East contact resistance (mK/W)", eastResistance);
            case 8:
                return EditInfo.createCheckboxWithField("Constant Density", !(constRho == -1), constRho);
            case 9:
                return EditInfo.createCheckboxWithField("Constant Specific Heat Capacity", !(constCp == -1), constCp);
            case 10:
                return EditInfo.createCheckboxWithField("Constant Thermal Conductivity", !(constK == -1), constK);
            default:
                return null;
        }
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        Material m = null;
        switch (n) {
            case 0:
                name = ei.textf.getText();
                break;
            case 1:
                index = (int) ei.value;
                break;
            case 2:
                m = sim.materialHashMap.get(sim.materialNames.get(ei.choice.getSelectedIndex()));
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
                westResistance = ei.value;
                break;
            case 7:
                eastResistance = ei.value;
                break;
            case 8:
                constRho = ei.value;
                break;
            case 9:
                constCp = ei.value;
                break;
            case 10:
                constK = ei.value;
                break;

        }

        //TODO: Implement this with better functionality

        updateElement(m);
    }

    void updateElement(Material m) {
        if (length / numCvs < 1e-6) {
            String input = String.valueOf(numCvs);
            if (!isDisabled)
                Window.alert("TCE can't have a dx < 1e-6, current is " + (length / numCvs) + "\n Please enter a smaller number of control volumes!");
            isDisabled = true;
        } else {
            set_dx(length / numCvs);
            buildThermalControlElement();
            isDisabled = false;
        }
        if (m != null) {
            if (!m.isLoaded()) {
                m.readFiles();
            }
            setMaterial(m);
        }
    }


    public void set_constant_parameters(String[] parameters, double[] values) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].equals("rho")) {
                for (ControlVolume cv : cvs) {
                    cv.constRho = values[i];
                }
            }
            if (parameters[i].equals("cp")) {
                for (ControlVolume cv : cvs) {
                    cv.constCp = values[i];
                }
            }
            if (parameters[i].equals("k")) {
                for (ControlVolume cv : cvs) {
                    cv.constK = values[i];
                }
            }
        }
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
                this.cvs.get(i).constK = value;
            }
        }
    }

    public void set_dx(double dx) {

        for (ControlVolume cv : cvs) {
            cv.dx = dx;
        }
    }

    public void set_temperatures(double temp) {
        for (ControlVolume cv : cvs) {
            cv.temperature = temp;
            cv.temperatureOld = temp;
        }
    }

    public void set_q_gen(double qGen) {
        for (ControlVolume cv : cvs) {
            cv.qGenerated = qGen;
        }
    }

    public void set_resistance(String side, double r) {
        if (side.equals("west")) {
            westResistance = r;
            cvs.get(0).westResistance = r;
        }
        if (side.equals("east")) {
            eastResistance = r;
            cvs.get(numCvs - 1).eastResistance = r;
        }
    }

    public void magnetize() {
        // Check if given TCE's material's magnetocaloric flag is TRUE;
        // if not, abort and inform the user.
        for (int i = 0; i < cvs.size(); i++) {
            cvs.get(i).magnetize();
        }
        // GWT.log("Finished (de)magnetization.");
        field = !field;
    }


}