package lahde.tccbuilder.client;
import com.google.gwt.user.client.ui.*;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;

import java.lang.Math;
import java.util.*;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Window;

public class ThermalControlElement extends CircuitElm implements Comparable<ThermalControlElement> {
    public double length;
    Color color;
    public String name;
    public int index;
    public int numCvs;
    public Material material;
    public double westResistance;
    public double eastResistance;
    public double volumeHeatGeneration;
    public ThermalControlElement westNeighbour;
    public ThermalControlElement eastNeighbour;
    public Simulation.BorderCondition westBoundary;
    public Simulation.BorderCondition eastBoundary;
    public double constRho;
    public double constCp;
    public double constK;
    public double hTransv;
    public double startTemperature;
    public double operatingMax;
    public double operatingMin;
    public boolean hasOperatingRange;
    public Vector<ControlVolume> cvs;

    public boolean isDisabled;

    public boolean field;
    public int fieldIndex;

    CirSim.LengthUnit DEFINED_LENGTH_UNIT;


    public ThermalControlElement(int xx, int yy) {
        super(xx, yy);
        initializeThermalControlElement();

        index = -1;
        for (ThermalControlElement c : sim.simulation1D.simTCEs) {
            if (c.index > index) index = c.index;
        }
        index++;
        material = sim.materialHashMap.get("100001-Inox");
        if (material != null && !material.isLoaded()) material.readFiles();

        isDisabled = false;
        field = false;
        fieldIndex = 0;
        buildThermalControlElement();

    }

    public ThermalControlElement(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        initializeThermalControlElement();
        index = Integer.parseInt(st.nextToken());
        material = sim.materialHashMap.get("100001-Inox");
        length = Double.parseDouble(st.nextToken());
        name = st.nextToken().replaceAll("#", " ");
        resizable = Boolean.parseBoolean(st.nextToken());
        constRho = Double.parseDouble(st.nextToken());
        constCp = Double.parseDouble(st.nextToken());
        constK = Double.parseDouble(st.nextToken());
        westResistance = Double.parseDouble(st.nextToken());
        eastResistance = Double.parseDouble(st.nextToken());
        numCvs = Integer.parseInt(st.nextToken());
        color = Color.translateColorIndex(Integer.parseInt(st.nextToken()));
        isDisabled = false;
        field = false;
        fieldIndex = Integer.parseInt(st.nextToken());
        buildThermalControlElement();
        int counter = 0;
        Material m = null;
        while (st.hasMoreTokens()) {
            int materialIndex = Integer.parseInt(st.nextToken(" "));
            m = sim.materialHashMap.get(sim.materialNames.get(materialIndex));
            if (m != null && !m.isLoaded()) m.readFiles();
            int number = Integer.parseInt(st.nextToken(" "));
            counter += number;
            for (int i = 0; i < number; i++) {
                cvs.get(i).material = m;
            }
            if (counter == numCvs) break;
        }
        material = m;

    }

    public void initializeThermalControlElement() {
        color = Color.gray;
        calculateLength();
        name = this.getClass().getSimpleName().replace("Elm", "");
        numCvs = 3;
        cvs = new Vector<ControlVolume>();
        westResistance = 0.0; // This is yet to be linked to the CV.
        eastResistance = 0.0;
        volumeHeatGeneration = 0.0;
        westNeighbour = null;
        eastNeighbour = null;
        westBoundary = Simulation.BorderCondition.CONVECTIVE;
        eastBoundary = Simulation.BorderCondition.CONVECTIVE;
        constRho = -1;
        constCp = -1;
        constK = -1;
        startTemperature = -1;
    }

    public void calculateLength() {
        length = Math.sqrt(Math.pow((y2 - y), 2) + Math.pow((x2 - x), 2)) / sim.selectedLengthUnit.conversionFactor;
        //gridSize px = 1 unit of measurement
        length /= sim.gridSize;
        if (cvs != null)
            buildThermalControlElement();
    }


    public void buildThermalControlElement() {
        double newDX = length / numCvs;
        cvs.clear();
        for (int i = 0; i < numCvs; i++) {
            cvs.add(new ControlVolume(i));
            cvs.get(i).parent = this;
            cvs.get(i).material = material;
            cvs.get(i).dx = newDX;

            if (constRho != -1) {
                cvs.get(i).constRho = constRho;
            }
            if (constCp != -1) {
                cvs.get(i).constCp = constCp;
            }
            if (constK != -1) {
                cvs.get(i).constK = constK;
            }
            cvs.get(i).constQgen = volumeHeatGeneration;
            cvs.get(i).hTransv = hTransv;
        }
        cvs.get(0).westResistance = westResistance;
        cvs.get(numCvs - 1).eastResistance = eastResistance;

    }

    public void setTemperatures(double startTemp) {
        for (ControlVolume cv : cvs) {
            cv.temperature = startTemp;
            cv.temperatureOld = startTemp;
        }
    }

    public void setMaterial(Material m) {
        for (ControlVolume controlVolume : cvs) {
            controlVolume.material = m;
        }
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
        sb.append(name.replaceAll(" ", "#")).append(' ');
        sb.append(resizable).append(' ');
        sb.append(constRho).append(' ');
        sb.append(constCp).append(' ');
        sb.append(constK).append(' ');
        sb.append(westResistance).append(' ');
        sb.append(eastResistance).append(' ');
        sb.append(numCvs).append(' ');
        sb.append(Color.colorToIndex(color)).append(' ');
        sb.append(fieldIndex).append(' ');

        int counter = 0;
        int currentIndex = sim.materialNames.indexOf(material.materialName);
        sb.append(currentIndex).append(' ');
        for (ControlVolume cv : cvs) {
            int i = sim.materialNames.indexOf(cv.material.materialName);
            if (i != currentIndex) {
                sb.append(counter).append(' ');
                sb.append(i).append(' ');
                currentIndex = i;
                counter = 0;
            }
            counter++;
        }
        sb.append(counter).append(' ');

        // explanation for material indexes:
        // Inox Inox Inox Gd Inox Inox == 0 3 1 1 0 2
        // reads like this: 3 cv of material 0, 1 cv of material 1, 2 cv of material 0

//        GWT.log(sb.toString());
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

//        double tmpDx = length / numCvs;
//        if (tmpDx < 1e-6 && tmpDx != 0) {
//            //Window.alert("TCE can't have a dx < 1µ, current is " + tmpDx);
//            drawLine(g, point1, point2, lineThickness, Color.red);
//            set_dx(tmpDx);
//            isDisabled = true;
//        } else {
//            drawLine(g, point1, point2, lineThickness, color);
//            set_dx(tmpDx);
//            isDisabled = false;
//        }
        drawLine(g, point1, point2, lineThickness, color);

    }

    void drawCVTemperatures(Graphics g, Point pa, Point pb) {
        Context2d ctx = g.context;
        double x = Math.min(pa.x, pb.x);
        double y = Math.min(pa.y, pb.y);
        double width = Math.abs(pa.x - pb.x);
        double cvWidth = width / numCvs;
        double height = lineThickness;
        double cvHeight = height;
        ctx.setStrokeStyle(Color.deepBlue.getHexValue());
        ctx.setLineWidth(0.5);
        for (int i = 0; i < cvs.size(); i++) {
            ControlVolume cv = cvs.get(i);
            double cvX = x + i * cvWidth;
            double cvY = y - (height / 2);

            double temperatureRange = sim.simulation1D.maxTemp - sim.simulation1D.minTemp;
            double temperatureRatio = (cv.temperature - sim.simulation1D.minTemp) / temperatureRange;
            temperatureRatio = Math.min(temperatureRatio, 1.0);
            temperatureRatio = Math.max(temperatureRatio, 0.0);

            String cvColor = CirSim.getMixedColor(temperatureRatio);
            ctx.setFillStyle(cvColor.equals("#000") ? color.getHexValue() : cvColor);
            ctx.strokeRect(cvX, cvY, cvWidth, cvHeight);
            ctx.fillRect(cvX, cvY, cvWidth, cvHeight);
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
        arr[2] = "Material = " + material.materialName;  // TODO: list materials of CVs
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
                        if (m != null) m.showTemperatureRanges(ei.choice);
                    }
                });

                ei.choice.select(sim.materialNames.indexOf(material.materialName));

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
                return new EditInfo("West contact resistance (m²K/W)", westResistance);
            case 7:
                return new EditInfo("East contact resistance (m²K/W)", eastResistance);
            case 8:
                return new EditInfo("Heat generation (W/m³)", volumeHeatGeneration);
            case 9:
                return EditInfo.createCheckboxWithField("Constant Density (kg/m³)", !(constRho == -1), constRho);
            case 10:
                return EditInfo.createCheckboxWithField("Constant Specific Heat Capacity (J/kgK)", !(constCp == -1), constCp);
            case 11:
                return EditInfo.createCheckboxWithField("Constant Thermal Conductivity (W/mK)", !(constK == -1), constK);
            case 12:
                return new EditInfo("Initial Temperature (K)", startTemperature);
            case 13:
                return new EditInfo("Heat loss rate to the ambient (W/(m³K))", hTransv);
            case 14:
                //return EditInfo.createCheckbox("Turn on external field", field);
                EditInfo ei3 = EditInfo.createButton("Toggle external field");
                ei3.button.addClickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        field = !field;
                        Window.alert("Field = " + String.valueOf(field));
                    }
                });
                return ei3;
            // case 15:
            //     return new EditInfo("Field", String.valueOf(field), false);
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
                Material m = sim.materialHashMap.get(sim.materialNames.get(ei.choice.getSelectedIndex()));
                if (m != null) {
                    material = m;
                    setMaterial(m);
                    if (!m.isLoaded()) m.readFiles();
                }
                break;
            case 3:
                numCvs = (int) ei.value;
                break;
            case 4:
                color = Color.translateColorIndex(ei.choice.getSelectedIndex());
                break;
            case 5:
                setNewLength(ei.value);
                break;
            case 6:
                westResistance = ei.value;
                break;
            case 7:
                eastResistance = ei.value;
                break;
            case 8:
                volumeHeatGeneration = ei.value;
                break;
            case 9:
                constRho = ei.value;
                break;
            case 10:
                constCp = ei.value;
                break;
            case 11:
                constK = ei.value;
                break;
            case 12:
                startTemperature = (double) ei.value;
                if (startTemperature >= 0) {
                    setTemperatures(startTemperature);
                    // GWT.log(String.valueOf(cvs.get(0).temperature));
                }
                break;
            case 13:
                hTransv = (double) ei.value;
                break;
            case 14:
                break;
        }
        updateElement();

    }

    public void setNewLength(Double value) {
        double calculatedLength = (value / sim.selectedLengthUnit.conversionFactor);
        if (!resizable && calculatedLength != length) {
            Window.alert("Warning, element not resizeable!");
            return;
        }
        double prevLength = length;
        length = calculatedLength;


        double ratio = length / prevLength;
        if (y == y2) {
            int deltaX = (int) ((point2.x - point1.x) * ratio);
            drag(sim.snapGrid(point1.x + deltaX), y);
        } else {
            int deltaY = (int) ((point2.y - point1.y) * ratio);
            drag(x, sim.snapGrid(point1.y + deltaY));
        }
    }

    void updateElement() {
        if (length / numCvs < 1e-6) {
            if (!isDisabled)
                Window.alert("TCE can't have a dx < 1e-6, current is " + (length / numCvs) + "\n Please enter a smaller number of control volumes!");
            isDisabled = true;
        } else {
//            set_dx(length / numCvs);
            buildThermalControlElement();
            isDisabled = false;
        }

        sim.reorderByIndex();
    }

    String getOperatingRangeString() {
        if (hasOperatingRange)
            return "Ideal operating range: " + operatingMin + "K - " + operatingMax + "K";
        else
            return null;
    }

    public void setConstProperty(Simulation.Property property, double value) {
        switch (property) {
            case DENSITY:
                for (ControlVolume cv : cvs) {
                    cv.constRho = value;
                }
            case SPECIFIC_HEAT_CAPACITY:
                for (ControlVolume cv : cvs) {
                    cv.constCp = value;
                }
            case THERMAL_CONDUCTIVITY:
                for (ControlVolume cv : cvs) {
                    cv.constK = value;
                }
        }
    }

    public void set_dx(double dx) {
        for (ControlVolume cv : cvs) {
            cv.dx = dx;
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

    public void ePolarize() {
        // Check if given TCE's material's electrocaloric flag is TRUE;
        // if not, abort and inform the user.
        for (int i = 0; i < cvs.size(); i++) {
            cvs.get(i).ePolarize();
        }
        // GWT.log("Finished (de)polarization.");
        field = !field;
    }


}