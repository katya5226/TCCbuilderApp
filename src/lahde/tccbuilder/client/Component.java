/*    
    Copyright (C) Paul Falstad and Iain Sharp
    
    This file is part of CircuitJS1.

    CircuitJS1 is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    CircuitJS1 is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CircuitJS1.  If not, see <http://www.gnu.org/licenses/>.
*/

package lahde.tccbuilder.client;

import com.google.gwt.core.client.GWT;

import java.lang.Math;
import java.util.*;

import com.google.gwt.user.client.Window;

public class Component extends CircuitElm implements Comparable<Component> {
    double resistance;
    public double length;
    Color color;
    public String name;
    public int index;
    public Material material;
    public int numCvs;
    public double westResistance;
    public double eastResistance;
    public Component westNeighbour;
    public Component eastNeighbour;
    public int westBoundary;
    public int eastBoundary;
    public double constRho;
    public double constCp;
    public double constK;

    public Vector<ControlVolume> controlVolumes;

    public boolean isDisabled;
    public boolean field;
    public int fieldIndex;

    public Component(int xx, int yy) {
        super(xx, yy);
        initializeComponent();

        index = -1;
        for (Component c : sim.simComponents) {
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

    public Component(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
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
        calculateLength();
        name = "Component";
        numCvs = 3;
        controlVolumes = new Vector<ControlVolume>();
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
            setDx(tmpDx);
            sim.simComponents.add(this);
            Collections.sort(sim.trackedTemperatures);
            sim.trackedTemperatures.add(this);
        }
    }

    public void calculateLength() {
        length = Math.sqrt(Math.pow((y2 - y), 2) + Math.pow((x2 - x), 2)) / sim.selectedLengthUnit.conversionFactor;
        //gridSize px = 1 unit of measurement
        length /= sim.gridSize;
    }

    public void buildComponent() {
        controlVolumes.clear();
        for (int i = 0; i < numCvs; i++) {
            controlVolumes.add(new ControlVolume(i));
            controlVolumes.get(i).parent = this;

            if (constRho != -1) {
                controlVolumes.get(i).constRho = constRho;
            }
            if (constCp != -1) {
                controlVolumes.get(i).constCp = constCp;
            }
            if (constK != -1) {
                controlVolumes.get(i).constK = constK;
            }
        }

        controlVolumes.get(0).westResistance = westResistance;
        controlVolumes.get(numCvs - 1).eastResistance = eastResistance;
    }

    public void updateModes() {
        for (int i = 0; i < numCvs; i++) {
            ControlVolume cv = controlVolumes.get(i);
            if (cv.temperature >= cv.temperatureOld) {
                cv.mode = 1;
            } else {
                cv.mode = -1;
            }
        }
    }

    double[] listTemps() {
        double[] temps = new double[numCvs];
        for (int i = 0; i < temps.length; i++) {
            temps[i] = Math.round(controlVolumes.get(i).temperature * 100) / 100.0;
        }
        return temps;
    }

    @Override
    public int compareTo(Component o) {
        return index - o.index;
    }

    @Override
    int getDumpType() {
        return 520;
    }

    @Override
    String dump() {
        StringBuilder sb = new StringBuilder();

        sb.append(getDumpType()).append(" ");
        sb.append(point1.x).append(' ').append(point1.y).append(' ');
        sb.append(point2.x).append(' ').append(point2.y).append(' ');
        sb.append("0 ").append(index).append(' ');

        sb.append(material.materialName).append(' ');
        sb.append(Color.colorToIndex(color)).append(' ');

        sb.append(length).append(' ');
        sb.append(name).append(' ');
        sb.append(numCvs);

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
            //Window.alert("Component can't have a dx < 1µ, current is " + tmpDx);
            drawThickerLine(g, point1, point2, Color.red.getHexValue());
            setDx(tmpDx);
            isDisabled = true;
        } else {
            drawThickerLine(g, point1, point2, color.getHexValue());
            setDx(tmpDx);
            isDisabled = false;
        }


    }

    @Override
    void getInfo(String[] arr) {
        arr[0] = name;
        arr[1] = "Component index = " + index;
        arr[2] = "Material = " + material.materialName;
        arr[3] = "Length = " + CirSim.formatLength(length);
        arr[4] = "#CVs = " + numCvs;
        arr[5] = "CV dx = " + CirSim.formatLength(controlVolumes.get(0).dx);
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
                return new EditInfo("Length (" + sim.selectedLengthUnit.unitName + ")", length * CircuitElm.sim.selectedLengthUnit.conversionFactor);
            case 6:
                return new EditInfo("Left contact resistance (mK/W)", westResistance);
            case 7:
                return new EditInfo("Right contact resistance (mK/W)", eastResistance);
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
                westResistance = ei.value;
                break;
            case 7:
                eastResistance = ei.value;
                break;
        }

        //TODO: Implement this with better functionality

        if (length / numCvs < 1e-6) {
            String input = String.valueOf(numCvs);
            if (!isDisabled)
                Window.alert("Component can't have a dx < 1e-6, current is " + (length / numCvs) + "\n Please enter a smaller number of control volumes!");
            isDisabled = true;
        } else {
            setDx(length / numCvs);
            buildComponent();
            isDisabled = false;
        }

    }

    public void setConstProperties(Vector<Double> newProps) {
        if (newProps.size() != 3) {
            GWT.log("Vector of new properties must contain three values.");
        }
        for (int i = 0; i < numCvs; i++) {
            controlVolumes.get(i).constRho = newProps.get(0);
            controlVolumes.get(i).constCp = newProps.get(1);
            controlVolumes.get(i).constK = newProps.get(2);
        }
    }

    public void set_constant_parameters(String[] parameters, double[] values) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].equals("rho")) {
                for (ControlVolume cv : controlVolumes) {
                    cv.constRho = values[i];
                }
            }
            if (parameters[i].equals("cp")) {
                for (ControlVolume cv : controlVolumes) {
                    cv.constCp = values[i];
                }
            }
            if (parameters[i].equals("k")) {
                for (ControlVolume cv : controlVolumes) {
                    cv.constK = values[i];
                }
            }
        }
    }


    public void setConstProperty(String property, double value) {
        if (property.equals("rho")) {
            for (int i = 0; i < numCvs; i++) {
                controlVolumes.get(i).constRho = value;
            }
        }
        if (property.equals("cp")) {
            for (int i = 0; i < numCvs; i++) {
                controlVolumes.get(i).constCp = value;
            }
        }
        if (property.equals("k")) {
            for (int i = 0; i < numCvs; i++) {
                controlVolumes.get(i).constK = value;
            }
        }
    }

    public void setDx(double dx) {
        for (ControlVolume cv : controlVolumes) {
            cv.dx = dx;
        }
    }

    public void setQGenerated(double q_gen) {
        for (ControlVolume cv : controlVolumes) {
            cv.qGenerated = q_gen;
        }
    }

    public void setResistance(String side, double r) {
        if (side.equals("left")) {
            westResistance = r;
            controlVolumes.get(0).westResistance = r;
        }
        if (side.equals("right")) {
            eastResistance = r;
            controlVolumes.get(numCvs - 1).eastResistance = r;
        }
    }

    public void magnetize() {
        // Check if given component's' material's magnetocaloric flag is TRUE;
        // if not, abort and inform the user.
        for (int i = 0; i < controlVolumes.size(); i++) {
            controlVolumes.get(i).magnetize();
        }
        // GWT.log("Finished (de)magnetization.");
        field = !field;
    }


}
