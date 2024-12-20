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

class SwitchElm extends ThermalControlElement {
    boolean momentary;
    // position 0 == closed, position 1 == open
    int position, posCount;

    double kOn, kOff;
    double cpOn, cpOff;
    double rhoOn, rhoOff;
    double responseTime;

    public SwitchElm(int xx, int yy) {
        super(xx, yy);
        material = sim.materialHashMap.get("000000-Constant properties");
        momentary = false;
        position = 1;
        posCount = 2;
    }

    SwitchElm(int xx, int yy, boolean mm) {
        super(xx, yy);
        material = sim.materialHashMap.get("000000-Constant properties");
        position = (mm) ? 1 : 0;
        momentary = mm;
        posCount = 2;
    }

    public SwitchElm(int xa, int ya, int xb, int yb, int f,
                     StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
        material = sim.materialHashMap.get("000000-Constant properties");
        position = Integer.parseInt(st.nextToken());
        momentary = st.nextToken().equals("true");
        kOn = Double.parseDouble(st.nextToken());
        kOff = Double.parseDouble(st.nextToken());
        posCount = 2;
    }

    @Override
    int getDumpType() {
        return 's';
    }


    @Override
    String dump() {
        return super.dump() + position + " " + momentary + " " + kOn + " " + kOff;
    }

    Point ps, ps2;

    @Override
    void setPoints() {
        super.setPoints();
        calcLeads(64);
        ps = new Point();
        ps2 = new Point();
    }

    final int openhs = 16;

    Rectangle getSwitchRect() {
        interpPoint(lead1, lead2, ps, 0, openhs);
        return new Rectangle(lead1).union(new Rectangle(lead2)).union(new Rectangle(ps));
    }

    @Override
    void draw(Graphics g) {
        int hs1 = (position == 1) ? 0 : 2;
        int hs2 = (position == 1) ? openhs : 2;
        setBbox(point1, point2, openhs);
        g.setColor(color);

        drawLine(g, point1, lead1, lineThickness, color);
        drawLine(g, lead2, point2, lineThickness, color);

        interpPoint(lead1, lead2, ps, 0, hs1);
        interpPoint(lead1, lead2, ps2, 1, hs2);

        drawLine(g, ps, ps2, lineThickness, color);
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
                // return new EditInfo("Length (" + sim.selectedLengthUnit.unitName + ")", length * CircuitElm.sim.selectedLengthUnit.conversionFactor);
                return new EditInfo("Length (mm)", length * 1e3);
            case 5:
                return new EditInfo("West contact resistance (m²K/W)", westResistance);
            case 6:
                return new EditInfo("East contact resistance (m²K/W)", eastResistance);
            case 7:
                return new EditInfo("Thermal Conductivity (W/mK) - ON", kOn);
            case 8:
                return new EditInfo("Thermal Conductivity (W/mK) - OFF", kOff);
            case 9:
                return new EditInfo("Specific Heat Capacity (J/kgK)", constCp);
            case 10:
                return new EditInfo("Density (kg/m³)", constRho);
            case 11:
                return new EditInfo("Response time (s)", responseTime);
            case 12:
                return new EditInfo("Heat loss rate to the ambient (W/(m³K))", hTransv);
            case 13:
                return new EditInfo("Actuation input power (W)", inputPower);
                // return new EditInfo("Actuation input power (W/m³)", inputPower);
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
                break;
            case 5:
                westResistance = ei.value;
                break;
            case 6:
                eastResistance = ei.value;
                break;
            case 7:
                constK = kOn = ei.value;
                break;
            case 8:
                constK = kOff = ei.value;
                break;
            case 9:
                constCp = cpOff = cpOn = ei.value;
                break;
            case 10:
                constRho = rhoOff = rhoOn = ei.value;
                break;
            case 11:
                responseTime = ei.value;
                break;
            case 12:
                hTransv = ei.value;
                break;
            case 13:
                inputPower = ei.value;
                break;
        }



        updateElement();
    }

    @Override
    public void initializeThermalControlElement() {
        super.initializeThermalControlElement();
        constK = kOff = kOn = -1;
        constCp = cpOff = cpOn = -1;
        constRho = rhoOff = rhoOn = -1;
        responseTime = 0.0;
    }

    void toggle() {
        position++;
        if (position >= posCount)
            position = 0;

        if (position == 0) {
            constCp = cpOn;
            constK = kOn;
            constRho = rhoOn;
        } else {
            constCp = cpOff;
            constK = kOff;
            constRho = rhoOff;
        }
        for (ControlVolume cv : cvs) {
            cv.constRho = constRho;
            cv.constK = constK;
            cv.constCp = constCp;
        }

    }
}
