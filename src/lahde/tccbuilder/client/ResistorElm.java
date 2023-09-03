
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
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import lahde.tccbuilder.client.util.Locale;

class ResistorElm extends ThermalControlElement {

    public ResistorElm(int xx, int yy) {
        super(xx, yy);
    }

    public ResistorElm(int xa, int ya, int xb, int yb, int f,
                       StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

    @Override
    int getDumpType() {
        return 'r';
    }




    Point ps3, ps4;

    @Override
    void setPoints() {
        super.setPoints();
        calcLeads(128);
        ps3 = new Point();
        ps4 = new Point();
    }

    @Override
    void draw(Graphics g) {
        int hs = (int) lineThickness * 2;
        setBbox(point1, point2, hs);
        g.setColor(color);

        drawLine(g, point1, lead1,lineThickness,color);
        drawLine(g, lead2, point2,lineThickness,color);

        //   double segf = 1./segments;
        double len = distance(lead1, lead2);
        g.context.save();
        g.context.setLineWidth(lineThickness);
        g.context.transform(((double) (lead2.x - lead1.x)) / len, ((double) (lead2.y - lead1.y)) / len, -((double) (lead2.y - lead1.y)) / len, ((double) (lead2.x - lead1.x)) / len, lead1.x, lead1.y);


        if (dn < 30)
            hs = 2;

        g.context.setLineCap(Context2d.LineCap.ROUND);
        g.context.beginPath();
        g.context.moveTo(0, 0);
        for (int i = 0; i < 4; i++) {
            g.context.lineTo(((1 + (4 * i)) * len) / 16, hs);
            g.context.lineTo(((3 + (4 * i)) * len) / 16, -hs);
        }
        g.context.lineTo(len, 0);
        g.context.stroke();

        g.context.restore();
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
        updateElement();
    }

}
