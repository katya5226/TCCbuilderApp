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

class HeatSourceSinkElm extends ThermalControlElement {
    double temperature;

    public HeatSourceSinkElm(int xx, int yy) {
        super(xx, yy);
    }

    public HeatSourceSinkElm(int xa, int ya, int xb, int yb, int f,
                             StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }


    @Override
    int getDumpType() {
        return 'h';
    }

    @Override
    int getShortcut() {
        return 'h';
    }

    @Override
    int getPostCount() {
        return 1;
    }

    @Override
    void draw(Graphics g) {
        g.setColor(color);

        drawThickLine(g, point1, point2);

        int height = 20;
        ps1 = new Point();
        ps1.x = point2.x + 10;
        ps1.y = point2.y - (height / 2);
        ps2 = new Point();
        ps2.x = point2.x + 10;
        ps2.y = point2.y + (height / 2);
        g.context.setLineCap(Context2d.LineCap.BUTT);
        g.context.setLineWidth(6);

        g.context.beginPath();
        g.context.moveTo(ps1.x, ps1.y);
        g.context.lineTo(ps2.x, ps2.y);
        g.context.stroke();

        ps1.x = point2.x;
        ps2.x = point2.x;

        g.context.setLineWidth(3);
        g.context.beginPath();
        g.context.moveTo(ps1.x, ps1.y);
        g.context.lineTo(ps2.x, ps2.y);
        g.context.stroke();

        interpPoint(point1, point2, ps2, 1 + 11. / dn);
        setBbox(point1, ps2, 11);
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
        Material m = null;
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

        //TODO: Implement this with better functionality

        updateElement(m);
    }
}
