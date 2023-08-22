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

class SwitchElm extends ThermalControlElement {
    boolean momentary;
    // position 0 == closed, position 1 == open
    int position, posCount;

    public SwitchElm(int xx, int yy) {
        super(xx, yy);
        momentary = false;
        position = 1;
        posCount = 2;
    }

    SwitchElm(int xx, int yy, boolean mm) {
        super(xx, yy);
        position = (mm) ? 1 : 0;
        momentary = mm;
        posCount = 2;
    }

    public SwitchElm(int xa, int ya, int xb, int yb, int f,
                     StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
        position = Integer.parseInt(st.nextToken());
        momentary = st.nextToken().equals("true");
        posCount = 2;
    }

    @Override
    int getDumpType() {
        return 's';
    }


    @Override
    int getShortcut() {
        return 's';
    }

    @Override
    String dump() {
        return super.dump() + position + " " + momentary;
    }

    Point ps, ps2;

    @Override
    void setPoints() {
        super.setPoints();
        calcLeads(64);
        ps = new Point();
        ps2 = new Point();
    }

    final int openhs = 16;

    Rectangle getSwitchRect() {
        interpPoint(lead1, lead2, ps, 0, openhs);
        return new Rectangle(lead1).union(new Rectangle(lead2)).union(new Rectangle(ps));
    }

    @Override
    void draw(Graphics g) {
        int hs1 = (position == 1) ? 0 : 2;
        int hs2 = (position == 1) ? openhs : 2;
        g.setColor(color);
        setBbox(point1, point2, openhs);

        draw2Leads(g);


        interpPoint(lead1, lead2, ps, 0, hs1);
        interpPoint(lead1, lead2, ps2, 1, hs2);

        drawThickLine(g, ps, ps2);
    }


    void toggle() {
        position++;
        if (position >= posCount)
            position = 0;
    }


}
