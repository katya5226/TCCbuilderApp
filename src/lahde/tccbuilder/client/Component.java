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
import lahde.tccbuilder.client.util.Locale;

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
    public int num_cvs;
    public double left_resistance;
    public double right_resistance;
    public Component left_neighbour;
    public Component right_neighbour;
    public int left_boundary;
    public int right_boundary;
    public double constRho;
    public double constCp;
    public double constK;

    public Vector<ControlVolume> cvs;

    public boolean isDisabled;
    public boolean field;
    public int fieldIndex;

    public Component(int xx, int yy) {
        super(xx, yy);
        initializeComponent();

        this.index = -1;
        for (Component c : sim.simComponents) {
            if (c.index > index)
                index = c.index;
        }
        this.index++;
        this.material = sim.materialHashMap.get("100001-Inox");

        if (!material.isLoaded())
            material.readFiles();

        isDisabled = false;
        this.field = false;
        fieldIndex = 1;
        buildComponent();

    }

    public Component(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        initializeComponent();

        this.index = Integer.parseInt(st.nextToken());
        this.material = sim.materialHashMap.get(st.nextToken(" "));
        if (!material.isLoaded()) {
            material.readFiles();
        }

        this.color = Color.translateColorIndex(Integer.parseInt(st.nextToken()));
        this.length = Double.parseDouble(st.nextToken());
        this.name = st.nextToken();
        this.num_cvs = Integer.parseInt(st.nextToken());
        isDisabled = false;
        this.field = false;
        fieldIndex = 1;
        buildComponent();

    }

    public void initializeComponent() {
        resistance = 1000;
        this.color = Color.green;
        calculateLength();
        this.name = "Component";
        this.num_cvs = 3;
        this.cvs = new Vector<ControlVolume>();
        this.left_resistance = 0.0; // This is yet to be linked to the CV.
        this.right_resistance = 0.0;
        this.left_neighbour = null;
        this.right_neighbour = null;
        this.left_boundary = 51;
        this.right_boundary = 52;
        this.constRho = -1;
        this.constCp = -1;
        this.constK = -1;
        double tmpDx = this.length / this.num_cvs;
        if (!(tmpDx < 1e-6) || tmpDx == 0) {
            this.set_dx(tmpDx);
            sim.simComponents.add(this);
            Collections.sort(sim.trackedTemperatures);
            sim.trackedTemperatures.add(this);
        }
    }

    public void calculateLength() {
        this.length = Math.sqrt(Math.pow((y2 - y), 2) + Math.pow((x2 - x), 2)) / sim.selectedLengthUnit.conversionFactor;
        //gridSize px = 1 unit of measurement
        this.length /= sim.gridSize;
    }

    public void buildComponent() {
        this.cvs.clear();
        for (int i = 0; i < this.num_cvs; i++) {
            this.cvs.add(new ControlVolume(i));
            this.cvs.get(i).parent = this;

            if (this.constRho != -1) {
                this.cvs.get(i).const_rho = this.constRho;
            }
            if (this.constCp != -1) {
                this.cvs.get(i).const_cp = this.constCp;
            }
            if (this.constK != -1) {
                this.cvs.get(i).const_k = this.constK;
            }
        }

        this.cvs.get(0).left_resistance = this.left_resistance;
        this.cvs.get(this.num_cvs - 1).right_resistance = this.right_resistance;
    }

    public void set_starting_temps(double start_temp) {
        for (int i = 0; i < this.num_cvs; i++) {
            this.cvs.get(i).temperature = start_temp;
            this.cvs.get(i).temperature_old = start_temp;
        }
    }

    public void updateModes() {
        for (int i = 0; i < this.num_cvs; i++) {
            ControlVolume cv = this.cvs.get(i);
            if (cv.temperature >= cv.temperature_old) {
                cv.mode = 1;
            } else if (cv.temperature < cv.temperature_old) {
                cv.mode = -1;
            }
        }
    }

    @Override
    public int compareTo(Component o) {
        return this.index - o.index;
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
        sb.append(num_cvs);

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

        double tmpDx = this.length / this.num_cvs;
        if (tmpDx < 1e-6 && tmpDx != 0) {
            //Window.alert("Component can't have a dx < 1µ, current is " + tmpDx);
            drawThickerLine(g, point1, point2, Color.red.getHexValue());
            this.set_dx(tmpDx);
            isDisabled = true;
        } else {
            drawThickerLine(g, point1, point2, color.getHexValue());
            this.set_dx(tmpDx);
            isDisabled = false;
        }


        doDots(g);

    }

    double[] listTemps() {
        double[] temps = new double[this.num_cvs];
        for (int i = 0; i < temps.length; i++) {
            temps[i] = Math.round(this.cvs.get(i).temperature * 100) / 100.0;
        }
        return temps;
    }

    void getInfo(String[] arr) {
        arr[0] = this.name;
        arr[1] = "Component index = " + this.index;
        arr[2] = "Material = " + this.material.materialName;
        arr[3] = "Length = " + CirSim.formatLength(this.length);
        arr[4] = "#CVs = " + this.num_cvs;
        arr[5] = "CV dx = " + CirSim.formatLength(this.cvs.get(0).dx);
    }

    @Override
    String getScopeText(int v) {
        return lahde.tccbuilder.client.util.Locale.LS("component") + ", " + getUnitText(resistance, Locale.ohmString);
    }


    @Override
    public EditInfo getEditInfo(int n) {
        switch (n) {
            case 0:
                return new EditInfo("Name", String.valueOf(this.name));
            case 1:
                return new EditInfo("Index", this.index);
            case 2:
                EditInfo ei = new EditInfo("Material", 0);
                ei.choice = new Choice();
                for (String m : sim.materialNames) {
                    ei.choice.add(m);
                }
                ei.choice.select(sim.materialNames.indexOf(this.material.materialName));
                return ei;
            case 3:
                return new EditInfo("Number of control volumes", (double) this.num_cvs);
            case 4:
                EditInfo ei2 = new EditInfo("Color", 0);
                ei2.choice = new Choice();
                for (int ch = 0; ch < sim.colorChoices.size(); ch++) {
                    ei2.choice.add(sim.colorChoices.get(ch));
                }

                ei2.choice.select(Color.colorToIndex(this.color));
                return ei2;
            case 5:
                return new EditInfo("Length (" + sim.selectedLengthUnit.unitName + ")", this.length * CircuitElm.sim.selectedLengthUnit.conversionFactor);
            case 6:
                return new EditInfo("Left contact resistance (mK/W)", this.left_resistance);
            case 7:
                return new EditInfo("Right contact resistance (mK/W)", this.right_resistance);
            default:
                return null;
        }
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {

        switch (n) {
            case 0:
                this.name = ei.textf.getText();
                break;
            case 1:
                this.index = (int) ei.value;
                break;
            case 2:
                this.material = sim.materialHashMap.get(sim.materialNames.get(ei.choice.getSelectedIndex()));
                if (!this.material.isLoaded())
                    this.material.readFiles();
                break;
            case 3:
                this.num_cvs = (int) ei.value;
                break;
            case 4:
                this.color = Color.translateColorIndex(ei.choice.getSelectedIndex());
                break;
            case 5:
                double prevLength = this.length;
                this.length = (ei.value / sim.selectedLengthUnit.conversionFactor);

                double ratio = length / prevLength;
                int deltaX = (int) ((point2.x - point1.x) * ratio);
                point2.x = (point1.x + deltaX);
                point2.x = sim.snapGrid(point2.x);
                break;
            case 6:
                this.left_resistance = ei.value;
                break;
            case 7:
                this.right_resistance = ei.value;
                break;
        }

        //TODO: Implement this with better functionality

        if (this.length / this.num_cvs < 1e-6) {
            String input = String.valueOf(this.num_cvs);
            if (!isDisabled)
                Window.alert("Component can't have a dx < 1e-6, current is " + (this.length / this.num_cvs) + "\n Please enter a smaller number of control volumes!");
            isDisabled = true;
        } else {
            this.set_dx(this.length / this.num_cvs);
            buildComponent();
            isDisabled = false;
        }

    }


    public void set_constant_parameters(String[] parameters, double[] values) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].equals("rho")) {
                for (ControlVolume cv : this.cvs) {
                    cv.const_rho = values[i];
                }
            }
            if (parameters[i].equals("cp")) {
                for (ControlVolume cv : this.cvs) {
                    cv.const_cp = values[i];
                }
            }
            if (parameters[i].equals("k")) {
                for (ControlVolume cv : this.cvs) {
                    cv.const_k = values[i];
                }
            }
        }
    }

    public void setConstProperties(Vector<Double> newProps) {
        if (newProps.size() != 3) {
            GWT.log("Vector of new properties must contain three values.");
        }
        for (int i = 0; i < this.num_cvs; i++) {
            this.cvs.get(i).const_rho = newProps.get(0);
            this.cvs.get(i).const_cp = newProps.get(1);
            this.cvs.get(i).const_k = newProps.get(2);
        }
    }

    public void setConstProperty(String property, double value) {
        if (property.equals("rho")) {
            for (int i = 0; i < this.num_cvs; i++) {
                this.cvs.get(i).const_rho = value;
            }
        }
        if (property.equals("cp")) {
            for (int i = 0; i < this.num_cvs; i++) {
                this.cvs.get(i).const_cp = value;
            }
        }
        if (property.equals("k")) {
            for (int i = 0; i < this.num_cvs; i++) {
                this.cvs.get(i).const_k = value;
            }
        }
    }

    public void set_dx(double dx) {

        for (ControlVolume cv : this.cvs) {
            cv.dx = dx;
        }
    }

    public void set_temperatures(double temp) {
        for (ControlVolume cv : this.cvs) {
            cv.temperature = temp;
            cv.temperature_old = temp;
        }
    }

    public void set_q_gen(double q_gen) {
        for (ControlVolume cv : this.cvs) {
            cv.q_gen = q_gen;
        }
    }

    public void set_resistance(String side, double r) {
        if (side.equals("left")) {
            this.left_resistance = r;
            this.cvs.get(0).left_resistance = r;
        }
        if (side.equals("right")) {
            this.right_resistance = r;
            this.cvs.get(this.num_cvs - 1).right_resistance = r;
        }
    }

    public void magnetize() {
        // Check if given component's' material's magnetocaloric flag is TRUE;
        // if not, abort and inform the user.
        for (int i = 0; i < cvs.size(); i++) {
            cvs.get(i).magnetize();
        }
        // GWT.log("Finished (de)magnetization.");
        this.field = !this.field;
    }

    @Override
    void stamp() {
        sim.stampResistor(nodes[0], nodes[1], resistance);
    }

}
