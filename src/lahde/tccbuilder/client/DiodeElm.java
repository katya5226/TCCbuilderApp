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

class DiodeElm extends ThermalControlElement {

    public DiodeElm(int xx, int yy) {
        super(xx, yy);
    }

    public DiodeElm(int xa, int ya, int xb, int yb, int f,
                    StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

    @Override
    int getDumpType() {
        return 'd';
    }

    int getShortcut() {
        return 'd';
    }

    final int hs = 8;
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

        draw2Leads(g);

        g.setColor(Color.gray);
        // draw arrow thingy
        g.fillPolygon(poly);

        // draw thing arrow is pointing to
        drawThickLine(g, cathode[0], cathode[1]);
    }


}
