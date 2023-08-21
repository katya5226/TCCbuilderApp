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

class GroundElm extends ThermalControlElement {
    int symbolType;

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
        g.setColor(Color.gray);
        drawThickLine(g, point1, point2);
        if (symbolType == 0) {
            int i;
            for (i = 0; i != 3; i++) {
                int a = 10 - i * 4;
                int b = i * 5; // -10;
                interpPoint2(point1, point2, ps1, ps2, 1 + b / dn, a);
                drawThickLine(g, ps1, ps2);
            }
        } else if (symbolType == 1) {
            interpPoint2(point1, point2, ps1, ps2, 1, 10);
            drawThickLine(g, ps1, ps2);
            int i;
            for (i = 0; i <= 2; i++) {
                Point p = interpPoint(ps1, ps2, i / 2.);
                drawThickLine(g, p.x, p.y, (int) (p.x - 5 * dpx1 + 8 * dx / dn), (int) (p.y + 8 * dy / dn - 5 * dpy1));
            }
        } else if (symbolType == 2) {
            interpPoint2(point1, point2, ps1, ps2, 1, 10);
            drawThickLine(g, ps1, ps2);
            int ps3x = (int) (point2.x + 10 * dx / dn);
            int ps3y = (int) (point2.y + 10 * dy / dn);
            drawThickLine(g, ps1.x, ps1.y, ps3x, ps3y);
            drawThickLine(g, ps2.x, ps2.y, ps3x, ps3y);
        } else {
            interpPoint2(point1, point2, ps1, ps2, 1, 10);
            drawThickLine(g, ps1, ps2);
        }
        interpPoint(point1, point2, ps2, 1 + 11. / dn);
        setBbox(point1, ps2, 11);
    }


}
