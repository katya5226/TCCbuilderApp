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

class CapacitorElm extends ThermalControlElement {
    Point plate1[], plate2[];

    public CapacitorElm(int xx, int yy) {
        super(xx, yy);
    }

    public CapacitorElm(int xa, int ya, int xb, int yb, int f,
                        StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

    @Override
    void reset() {
        super.reset();
    }

    @Override
    int getDumpType() {
        return 'c';
    }

    // used for PolarCapacitorElm
    Point platePoints[];

    @Override
    void setPoints() {
        super.setPoints();
        double f = (dn / 2 - 4) / dn;
        // calc leads
        lead1 = interpPoint(point1, point2, f);
        lead2 = interpPoint(point1, point2, 1 - f);
        // calc plates
        plate1 = newPointArray(2);
        plate2 = newPointArray(2);
        interpPoint2(point1, point2, plate1[0], plate1[1], f, 12);
        interpPoint2(point1, point2, plate2[0], plate2[1], 1 - f, 12);
    }

    @Override
    void draw(Graphics g) {
        int hs = 12;
        setBbox(point1, point2, hs);

        // draw first lead and plate
        g.setColor(Color.gray);
        drawThickLine(g, point1, lead1);
        drawThickLine(g, plate1[0], plate1[1]);
        drawThickLine(g, point2, lead2);

        if (platePoints == null)
            drawThickLine(g, plate2[0], plate2[1]);
        else {
            int i;
            for (i = 0; i != platePoints.length - 1; i++)
                drawThickLine(g, platePoints[i], platePoints[i + 1]);
        }


    }
    @Override
    public EditInfo getEditInfo(int n) {
        EditInfo out = super.getEditInfo(n);
        switch (n) {
            case 8:
                out = EditInfo.createCheckboxWithField("Constant Density", !(constRho == -1), constRho);
                break;
            case 9:
                out = EditInfo.createCheckboxWithField("Constant Specific Heat Capacity", !(constCp == -1), constCp);
                break;
            case 10:
                out = EditInfo.createCheckboxWithField("Constant Thermal Conductivity", !(constK == -1), constK);
                break;
        }
        return out;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        super.setEditValue(n, ei);
        GWT.log(ei.name);
        GWT.log(ei.value + "");
        switch (n) {
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
    }

    @Override
    int getShortcut() {
        return 'c';
    }

}
