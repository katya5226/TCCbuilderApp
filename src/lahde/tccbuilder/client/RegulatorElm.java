
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

import com.google.gwt.canvas.dom.client.CanvasGradient;
import lahde.tccbuilder.client.util.Locale;

class RegulatorElm extends CircuitElm {
    double resistance;

    public RegulatorElm(int xx, int yy) {
        super(xx, yy);
        resistance = 1000;
    }

    public RegulatorElm(int xa, int ya, int xb, int yb, int f,
                        StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        resistance = new Double(st.nextToken()).doubleValue();
    }

    @Override
    int getDumpType() {
        return 'r';
    }

    @Override
    String dump() {
        return super.dump() + " " + resistance;
    }

    Point ps3, ps4;

    @Override
    void setPoints() {
        super.setPoints();
        calcLeads(32);
        ps3 = new Point();
        ps4 = new Point();
    }

    @Override
    void draw(Graphics g) {
        int segments = 16;
        int i;
        int ox = 0;
        int hs = 6;
        setBbox(point1, point2, hs);
        draw2Leads(g);

        double len = distance(lead1, lead2);
        g.context.save();
        g.context.transform(((double) (lead2.x - lead1.x)) / len, ((double) (lead2.y - lead1.y)) / len, -((double) (lead2.y - lead1.y)) / len, ((double) (lead2.x - lead1.x)) / len, lead1.x, lead1.y);
        g.context.setLineWidth(2);

        g.setColor(Color.gray);
        if (dn < 30)
            hs = 2;
        g.context.beginPath();
        g.context.moveTo(0, 0);
        for (i = 0; i < 4; i++) {
            g.context.lineTo((1 + 4 * i) * len / 16, hs);
            g.context.lineTo((3 + 4 * i) * len / 16, -hs);
        }
        g.context.lineTo(len, 0);
        g.context.stroke();
        g.context.beginPath();

        double arrowStartX = ((5) * len / 16);
        double arrowEndX = ((8) * len / 16) * 1.375;
        double arrowStartY = hs * 3;
        double arrowEndY = -hs * 3;

        g.context.beginPath();
        g.context.moveTo(arrowStartX, arrowStartY);
        g.context.lineTo(arrowEndX, arrowEndY);
        g.context.stroke();

        double arrowAngle = Math.atan2(arrowEndY - arrowStartY, arrowEndX - arrowStartX);
        double triangleSize = 4;

        g.context.beginPath();
        g.context.moveTo(arrowEndX - triangleSize * Math.cos(arrowAngle - Math.PI / 6), arrowEndY - triangleSize * Math.sin(arrowAngle - Math.PI / 6));
        g.context.lineTo(arrowEndX, arrowEndY);
        g.context.lineTo(arrowEndX - triangleSize * Math.cos(arrowAngle + Math.PI / 6), arrowEndY - triangleSize * Math.sin(arrowAngle + Math.PI / 6));
        g.context.stroke();
        g.context.restore();

/*        if (sim.showValuesCheckItem.getState()) {
            String s = getShortUnitText(resistance, "");
            drawValues(g, s, hs + 2);
        }*/
    }


    @Override
    void getInfo(String arr[]) {
        arr[0] = "resistor";
    }

    @Override
    public EditInfo getEditInfo(int n) {
        // ohmString doesn't work here on linux
        if (n == 0)
            return new EditInfo("Resistance (ohms)", resistance, 0, 0);
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        resistance = (ei.value <= 0) ? 1e-9 : ei.value;
    }

    @Override
    int getShortcut() {
        return 'r';
    }

    double getResistance() {
        return resistance;
    }

    void setResistance(double r) {
        resistance = r;
    }
}
