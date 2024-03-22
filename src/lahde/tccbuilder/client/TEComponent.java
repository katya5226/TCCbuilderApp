package lahde.tccbuilder.client;

import com.google.gwt.core.client.GWT;
import lahde.tccbuilder.client.util.Locale;

import java.lang.Math;
import java.util.*;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Window;

public class TEComponent extends ThermalControlElement {

    double constElResistivity;
    double constSeebeck;
    double elCurrent;

    public TEComponent(int xx, int yy) {
        super(xx, yy);
        lineThickness = 25;
    }

    public TEComponent(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super();
        x = xa;
        y = ya;
        x2 = xb;
        y2 = yb;
        flags = f;
        initBoundingBox();
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
        volumeHeatGeneration = Double.parseDouble(st.nextToken());
        hTransv = Double.parseDouble(st.nextToken());
        numCvs = Integer.parseInt(st.nextToken());
        color = Color.translateColorIndex(Integer.parseInt(st.nextToken()));
        isDisabled = false;
        field = false;
        fieldIndex = Integer.parseInt(st.nextToken());
        constSeebeck = Double.parseDouble(st.nextToken());
        constElResistivity = Double.parseDouble(st.nextToken());
        elCurrent = Double.parseDouble(st.nextToken());
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
        lineThickness = 25;
    }
    @Override
    int getDumpType() {
        return 530;
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
                return new EditInfo("Length (mm)", length * 1e3);
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
                return new EditInfo("Cross Area (mm²)", crossArea);
            case 15:
                return EditInfo.createCheckboxWithField("Constant Seebeck Coefficient (\u03BCV/K)", !(constSeebeck == -1), constSeebeck);
            case 16:
                return EditInfo.createCheckboxWithField("Constant electrical resistivity (\u03BC\u03A9m)", !(constElResistivity == -1), constElResistivity);
            case 17:
                return new EditInfo("Electric current (A)", elCurrent);
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
                }
                break;
            case 13:
                hTransv = (double) ei.value;
                break;
            case 14:
                crossArea = (double) ei.value;  // * 1.0e-6;
                break;
            case 15:
                constSeebeck = (double) ei.value;  // * 1.0e-6;
                break;
            case 16:
                constElResistivity = (double) ei.value; // * 1.0e-6;
                break;
            case 17:
                elCurrent = (double) ei.value;
                break;
        }
        updateElement();

    }

    @Override
    public void initializeThermalControlElement() {
        super.initializeThermalControlElement();
        constSeebeck = -1;
        constElResistivity = -1;
        elCurrent = 0.0;
    }

    @Override
    public void buildThermalControlElement() {
        super.buildThermalControlElement();
        for (ControlVolume cv : cvs) {
            if (constSeebeck != -1) {
                cv.constSeeb = constSeebeck;
            }
            if (constElResistivity != -1) {
                cv.constElResistivity = constElResistivity;
            }
        }
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
        sb.append(volumeHeatGeneration).append(' ');
        sb.append(hTransv).append(' ');
        sb.append(numCvs).append(' ');
        sb.append(Color.colorToIndex(color)).append(' ');
        sb.append(fieldIndex).append(' ');
        sb.append(constSeebeck).append(' ');
        sb.append(constElResistivity).append(' ');
        sb.append(elCurrent).append(' ');

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
    
}