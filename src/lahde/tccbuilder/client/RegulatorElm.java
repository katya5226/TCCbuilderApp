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

import com.google.gwt.canvas.dom.client.Context2d;

class RegulatorElm extends ThermalControlElement {

    double k1, k2;
    double cp1, cp2;
    double rho1, rho2;
    double responseTime;
    double temperature1, temperature2;

    public RegulatorElm(int xx, int yy) {
        super(xx, yy);
    }

    public RegulatorElm(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

    @Override
    int getDumpType() {
        return 'e';
    }

    @Override
    int getShortcut() {
        return 'e';
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
        g.context.setStrokeStyle(color.getHexValue());
        setBbox(point1, point2, hs);
        drawLine(g, point1, lead1, lineThickness, color);
        drawLine(g, lead2, point2, lineThickness, color);

        double len = distance(lead1, lead2);
        g.context.save();
        g.context.transform(((double) (lead2.x - lead1.x)) / len, ((double) (lead2.y - lead1.y)) / len, -((double) (lead2.y - lead1.y)) / len, ((double) (lead2.x - lead1.x)) / len, lead1.x, lead1.y);
        g.context.setStrokeStyle(color.getHexValue());
        g.context.setLineWidth(lineThickness);

        if (dn < 30) hs = 2;

        g.context.setLineCap(Context2d.LineCap.ROUND);
        g.context.beginPath();
        g.context.moveTo(0, 0);
        for (int i = 0; i < 4; i++) {
            g.context.lineTo(((1 + (4 * i)) * len) / 16, hs);
            g.context.lineTo(((3 + (4 * i)) * len) / 16, -hs);
        }
        g.context.lineTo(len, 0);
        g.context.stroke();
        g.context.setLineCap(Context2d.LineCap.BUTT);

        double arrowStartX = ((5) * len / 16);
        double arrowEndX = ((8) * len / 16) * 1.375;
        double arrowStartY = hs * 3;
        double arrowEndY = -hs * 3;


        drawLine(g, arrowStartX, arrowStartY, arrowEndX, arrowEndY, lineThickness);
        double lineAngle = Math.atan2(arrowEndY - arrowStartY, arrowEndX - arrowStartX);
        double arrowAngle = Math.PI / 6;
        double triangleSize = lineThickness * 1.5;

        g.context.beginPath();
        g.context.setLineWidth(lineThickness);
        g.context.setFillStyle(color.getHexValue());
        double x1 = arrowEndX - triangleSize * Math.cos(lineAngle - arrowAngle);
        double y1 = arrowEndY - triangleSize * Math.sin(lineAngle - arrowAngle);
        double x2 = arrowEndX - triangleSize * Math.cos(lineAngle + arrowAngle);
        double y2 = arrowEndY - triangleSize * Math.sin(lineAngle + arrowAngle);
        g.context.moveTo(x1, y1);
        g.context.lineTo(arrowEndX, arrowEndY);
        g.context.lineTo(x2, y2);
        g.context.lineTo(x1, y1);
        g.context.closePath();
        g.context.stroke();
        g.context.fill();
        g.context.restore();


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
                return new EditInfo("Length (" + sim.selectedLengthUnit.unitName + ")", length * CircuitElm.sim.selectedLengthUnit.conversionFactor);
            case 5:
                return new EditInfo("West contact resistance (mK/W)", westResistance);
            case 6:
                return new EditInfo("East contact resistance (mK/W)", eastResistance);
            case 7:
                return new EditInfo("Thermal Conductivity 1", k1);
            case 8:
                return new EditInfo("Thermal Conductivity 2", k2);
            case 9:
                return new EditInfo("Specific Heat Capacity 1", cp1);
            case 10:
                return new EditInfo("Specific Heat Capacity 2", cp2);
            case 11:
                return new EditInfo("Density 1 ", rho1);
            case 12:
                return new EditInfo("Density 2", rho2);
            case 13:
                return new EditInfo("Temperature 1 ", temperature1);
            case 14:
                return new EditInfo("Temperature 2", temperature2);
            case 15:
                return new EditInfo("Response time", responseTime);
            default:
                return null;
        }
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        Material m = null;
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
                double prevLength = length;
                length = (ei.value / sim.selectedLengthUnit.conversionFactor);

                double ratio = length / prevLength;
                int deltaX = (int) ((point2.x - point1.x) * ratio);
                point2.x = (point1.x + deltaX);
                point2.x = sim.snapGrid(point2.x);
                break;
            case 5:
                westResistance = ei.value;
                break;
            case 6:
                eastResistance = ei.value;
                break;
            case 7:
                k1 = ei.value;
                break;
            case 8:
                k2 = ei.value;
                break;
            case 9:
                cp1 = ei.value;
                break;
            case 10:
                cp2 = ei.value;
            case 11:
                rho1 = ei.value;
                break;
            case 12:
                rho2 = ei.value;
                break;
            case 13:
                temperature1 = ei.value;
                break;
            case 14:
                temperature2 = ei.value;
                break;
            case 15:
                responseTime = ei.value;
                break;
        }

        //TODO: Implement this with better functionality

        updateElement(m);
    }

}
