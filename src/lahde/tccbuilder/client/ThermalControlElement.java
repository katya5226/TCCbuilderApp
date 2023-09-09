package lahde.tccbuilder.client;

import com.google.gwt.canvas.dom.client.Context2d;
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
    public Material material;
    public double westResistance;
    public double eastResistance;
    public ThermalControlElement westNeighbour;
    public ThermalControlElement eastNeighbour;
    public int westBoundary;
    public int eastBoundary;
    public double constRho;
    public double constCp;
    public double constK;
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

        isDisabled = false;
        field = false;
        fieldIndex = 1;
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
        numCvs = Integer.parseInt(st.nextToken());
        color = Color.translateColorIndex(Integer.parseInt(st.nextToken()));
        isDisabled = false;
        field = false;
        fieldIndex = 1;
        buildThermalControlElement();
        int counter = 0;
        Material m = null;
        while (st.hasMoreTokens()) {
            int materialIndex = Integer.parseInt(st.nextToken(" "));
            m = sim.materialHashMap.get(sim.materialNames.get(materialIndex));
            int number = Integer.parseInt(st.nextToken(" "));
            counter += number;
            for (int i = 0; i < number; i++)
                cvs.get(i).material = m;
            if (counter == numCvs) break;
        }
        material = m;
        if (material != null && !material.isLoaded()) material.readFiles();
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
//            sim.simulation1D.simTCEs.add(this);
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
            cvs.get(i).material = material;

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
        sb.append(numCvs).append(' ');
        sb.append(Color.colorToIndex(color)).append(' ');

        int counter = 0;
        int currentIndex = sim.materialNames.indexOf(cvs.get(0).material.materialName);
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

        double tmpDx = length / numCvs;
        if (tmpDx < 1e-6 && tmpDx != 0) {
            //Window.alert("TCE can't have a dx < 1µ, current is " + tmpDx);
            drawLine(g, point1, point2, lineThickness, Color.red);
            set_dx(tmpDx);
            isDisabled = true;
        } else {
            drawLine(g, point1, point2, lineThickness, color);
            set_dx(tmpDx);
            isDisabled = false;
        }

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
        ctx.strokeRect(x, y, width, height);
        for (int i = 0; i < cvs.size(); i++) {
            ControlVolume cv = cvs.get(i);
            double cvX = x + i * cvWidth;
            double cvY = y - (height / 2);

            double temperatureRange = sim.simulation1D.maxTemp - sim.simulation1D.minTemp;
            double temperatureRatio = (cv.temperature - sim.simulation1D.minTemp) / temperatureRange;

            Color color1 = Color.blue;
            Color color2 = Color.white;
            Color color3 = Color.red;

            int red = (int) (color1.getRed() * (1 - temperatureRatio) + color2.getRed() * temperatureRatio);
            int green = (int) (color1.getGreen() * (1 - temperatureRatio) + color2.getGreen() * temperatureRatio);
            int blue = (int) (color1.getBlue() * (1 - temperatureRatio) + color2.getBlue() * temperatureRatio);

            red = (int) (red * (1 - temperatureRatio) + color3.getRed() * temperatureRatio);
            green = (int) (green * (1 - temperatureRatio) + color3.getGreen() * temperatureRatio);
            blue = (int) (blue * (1 - temperatureRatio) + color3.getBlue() * temperatureRatio);

            String cvColor = "#" + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue);
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
        int deltaX = (int) ((point2.x - point1.x) * ratio);
        drag(sim.snapGrid(point1.x + deltaX), y);
    }

    void updateElement() {
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
        sim.reorderByIndex();
    }

    String getOperatingRangeString() {
        if (hasOperatingRange)
            return "Ideal operating range: " + operatingMin + "-" + operatingMax + "K";
        else
            return null;
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