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

import com.google.gwt.user.client.Window;

class GroundElm extends ThermalControlElement {
    int symbolType;
    double temperature;

    public GroundElm(int xx, int yy) {
        super(xx, yy);
        symbolType = 0;
    }

    public GroundElm(int xa, int ya, int xb, int yb, int f,
                     StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
        if (st.hasMoreTokens()) {
            try {
                symbolType = Integer.parseInt(st.nextToken());
            } catch (Exception e) {
            }
        }
    }

    @Override
    String dump() {
        return super.dump() + " " + symbolType;
    }

    @Override
    int getDumpType() {
        return 'g';
    }

    @Override
    int getShortcut() {
        return 'g';
    }

    @Override
    int getPostCount() {
        return 1;
    }

    @Override
    void draw(Graphics g) {
        drawLine(g, point1, point2, lineThickness, color);
        if (symbolType == 0) {
            int i;
            for (i = 0; i != 3; i++) {
                int a = (int) ((lineThickness * 4) - (i * lineThickness));
                int b = (int) (i * lineThickness * 2); // -10;
                interpPoint2(point1, point2, ps1, ps2, 1 + b / dn, a);
                drawLine(g, ps1, ps2, lineThickness, color);
            }
        } else if (symbolType == 1) {
            interpPoint2(point1, point2, ps1, ps2, 1, 10);
            drawLine(g, ps1, ps2, lineThickness, color);
            int i;
            for (i = 0; i <= 2; i++) {
                Point p = interpPoint(ps1, ps2, i / 2.);
                drawLine(g, p.x, p.y, (int) (p.x - 5 * dpx1 + 8 * dx / dn), (int) (p.y + 8 * dy / dn - 5 * dpy1), lineThickness, color);
            }
        } else if (symbolType == 2) {
            interpPoint2(point1, point2, ps1, ps2, 1, 10);
            drawLine(g, ps1, ps2, lineThickness, color);
            int ps3x = (int) (point2.x + 10 * dx / dn);
            int ps3y = (int) (point2.y + 10 * dy / dn);
            drawLine(g, ps2.x, ps2.y, ps3x, ps3y, lineThickness, color);
            drawLine(g, ps1.x, ps1.y, ps3x, ps3y, lineThickness, color);
        } else {
            interpPoint2(point1, point2, ps1, ps2, 1, 10);
            drawLine(g, ps1, ps2, lineThickness, color);
        }
        interpPoint(point1, point2, ps2, 1 + 11. / dn);
        int hs = 12;
        setBbox(point1, point2, hs);
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

        //TODO: Implement this with better functionality
        updateElement();
    }
}
