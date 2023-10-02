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
import com.google.gwt.user.client.Window;

class DiodeElm extends ThermalControlElement {

    double kForward, kBackward;
    double cp;
    double rho;
    double responseTime;
    Direction direction;

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }


    public DiodeElm(int xx, int yy) {
        super(xx, yy);

    }

    public DiodeElm(int xa, int ya, int xb, int yb, int f,
                    StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);

    }

    @Override
    void drag(int xx, int yy) {
        xx = sim.snapGrid(xx);
        yy = sim.snapGrid(yy);
        if (noDiagonal) {
            if (Math.abs(x - xx) < Math.abs(y - yy)) {
                xx = x;
            } else {
                yy = y;
            }
        }
        x2 = xx;
        y2 = yy;
        setPoints();

        if (y == y2)
            direction = x2 < x ? Direction.LEFT : Direction.RIGHT;
        else if (x == x2)
            direction = y2 < y ? Direction.UP : Direction.DOWN;
    }

    @Override
    int getDumpType() {
        return 'd';
    }


    final int hs = (int) (lineThickness * 2);
    Polygon poly;
    Point cathode[];

    @Override
    void setPoints() {
        super.setPoints();
        calcLeads(16);
        cathode = newPointArray(2);
        Point pa[] = newPointArray(2);
        interpPoint2(lead1, lead2, pa[0], pa[1], 0, hs);
        interpPoint2(lead1, lead2, cathode[0], cathode[1], 1, hs);
        poly = createPolygon(pa[0], pa[1], lead2);
    }

    @Override
    void draw(Graphics g) {
        setBbox(point1, point2, hs);

        g.setColor(color);
        drawLine(g, point1, lead1, lineThickness, color);
        drawLine(g, lead2, point2, lineThickness, color);
        // draw arrow thingy
        g.fillPolygon(poly);

        // draw thing arrow is pointing to
        drawLine(g, cathode[0], cathode[1], lineThickness, color);
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
                EditInfo editInfo = new EditInfo("Length (" + sim.selectedLengthUnit.unitName + ")", length * CircuitElm.sim.selectedLengthUnit.conversionFactor);
                editInfo.editable = resizable;
                return editInfo;
            case 5:
                return new EditInfo("West contact resistance (mK/W)", westResistance);
            case 6:
                return new EditInfo("East contact resistance (mK/W)", eastResistance);
            case 7:
                return new EditInfo("Thermal Conductivity (forward)", kForward);
            case 8:
                return new EditInfo("Thermal Conductivity (backward)", kBackward);
            case 9:
                return new EditInfo("Specific Heat Capacity", cp);
            case 10:
                return new EditInfo("Density", rho);
            case 11:
                return new EditInfo("Response time", responseTime);
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
                kForward = ei.value;
                break;
            case 8:
                kBackward = ei.value;
                break;
            case 9:
                cp = ei.value;
                break;
            case 10:
                rho = ei.value;
                break;
            case 11:
                responseTime = ei.value;
                break;
        }



        updateElement();
    }


}
