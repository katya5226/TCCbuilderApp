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
import com.google.gwt.core.client.GWT;

class HeatSourceSinkElm extends ThermalControlElement {
    double temperature;

    public HeatSourceSinkElm(int xx, int yy) {
        super(xx, yy);
    }

    public HeatSourceSinkElm(int xa, int ya, int xb, int yb, int f,
                             StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);

    }


//
//    @Override
//    void draggingDone() {
//        super.draggingDone();
//        int tmpX = x;
//        int tmpY = y;
//        x = x2;
//        y = y2;
//        x2 = tmpX;
//        y2 = tmpY;
//    }

    @Override
    int getDumpType() {
        return 'h';
    }


    @Override
    int getPostCount() {
        return 1;
    }

    @Override
    void draw(Graphics g) {
        setBbox(point1, point2, 12);
        g.setColor(color);

        Point lead = new Point(point2.x, point2.y);
/*        lead.x += (int) (lineThickness * 3);
        interpPoint(point1, point2, lead, 0, lineThickness * 3);*/
        //interpPoint(point1, point2, lead, 3 * lineThickness / dn, lineThickness * 3);

        interpPoint(point1, point2, lead, 1 - (3 * lineThickness / dn), 0);

        drawLine(g, lead, point1, lineThickness, color);

        //normal line
        interpPoint(lead, point2, ps2, 0, lineThickness * 3);
        drawLine(g, lead, ps2, lineThickness, color);
        interpPoint(lead, point2, ps2, 0, -lineThickness * 3);
        drawLine(g, lead, ps2, lineThickness, color);

        //thick line
        interpPoint(point1, point2, ps2, 1, lineThickness * 3);
        drawLine(g, point2, ps2, lineThickness * 2, color);
        interpPoint(point1, point2, ps2, 1, -lineThickness * 3);
        drawLine(g, point2, ps2, lineThickness * 2, color);

    }


    @Override
    public EditInfo getEditInfo(int n) {
        switch (n) {
            case 0:
                return new EditInfo("Name", String.valueOf(name));
            case 1:
                return new EditInfo("Index", index);
            case 2:
                EditInfo ei2 = new EditInfo("Color", 0);
                ei2.choice = new Choice();
                for (int ch = 0; ch < sim.colorChoices.size(); ch++) {
                    ei2.choice.add(sim.colorChoices.get(ch));
                }

                ei2.choice.select(Color.colorToIndex(color));
                return ei2;
            case 3:
                return new EditInfo("Temperature", temperature);

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
                color = Color.translateColorIndex(ei.choice.getSelectedIndex());
                break;
            case 3:
                temperature = (int) ei.value;
                break;

        }

        updateElement();
    }
}
