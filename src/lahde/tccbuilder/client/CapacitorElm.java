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

class CapacitorElm extends CircuitElm {
    double capacitance;
    double compResistance, voltdiff;
    double initialVoltage;
    Point plate1[], plate2[];
    public static final int FLAG_BACK_EULER = 2;

    public CapacitorElm(int xx, int yy) {
        super(xx, yy);
        capacitance = 1e-5;
        initialVoltage = 1e-3;
    }

    public CapacitorElm(int xa, int ya, int xb, int yb, int f,
                        StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        capacitance = new Double(st.nextToken()).doubleValue();
        voltdiff = new Double(st.nextToken()).doubleValue();
        initialVoltage = 1e-3;
        try {
            initialVoltage = new Double(st.nextToken()).doubleValue();
        } catch (Exception e) {
        }
    }

    boolean isTrapezoidal() {
        return (flags & FLAG_BACK_EULER) == 0;
    }

    void reset() {
        super.reset();
        current = curcount = curSourceValue = 0;
        // put small charge on caps when reset to start oscillators
        voltdiff = initialVoltage;
    }

    void shorted() {
        super.reset();
        voltdiff = current = curcount = curSourceValue = 0;
    }

    int getDumpType() {
        return 'c';
    }

    String dump() {
        return super.dump() + " " + capacitance + " " + voltdiff + " " + initialVoltage;
    }

    // used for PolarCapacitorElm
    Point platePoints[];

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

        if (sim.dragElm != this) {
            drawDots(g, point1, lead1, curcount);
            drawDots(g, point2, lead2, -curcount);
        }
        if (sim.showValuesCheckItem.getState()) {
            String s = getShortUnitText(capacitance, "F");
            drawValues(g, s, hs);
        }
    }


    double curSourceValue;

    void getInfo(String arr[]) {
        arr[0] = "capacitor";

    }

    public EditInfo getEditInfo(int n) {
        if (n == 0)
            return new EditInfo("Capacitance (F)", capacitance, 1e-6, 1e-3);
        if (n == 1) {
            EditInfo ei = new EditInfo("", 0, -1, -1);
            ei.checkbox = new Checkbox("Trapezoidal Approximation", isTrapezoidal());
            return ei;
        }
        if (n == 2)
            return new EditInfo("Initial Voltage (on Reset)", initialVoltage);
        // if you add more things here, check PolarCapacitorElm
        return null;
    }

    public void setEditValue(int n, EditInfo ei) {
        if (n == 0)
            capacitance = (ei.value > 0) ? ei.value : 1e-12;
        if (n == 1) {
            if (ei.checkbox.getState())
                flags &= ~FLAG_BACK_EULER;
            else
                flags |= FLAG_BACK_EULER;
        }
        if (n == 2)
            initialVoltage = ei.value;
    }

    int getShortcut() {
        return 'c';
    }

    public double getCapacitance() {
        return capacitance;
    }

    public void setCapacitance(double c) {
        capacitance = c;
    }
}
