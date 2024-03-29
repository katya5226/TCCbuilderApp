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

import com.google.gwt.core.client.GWT;

class ConduitElm extends ThermalControlElement {
    public ConduitElm(int xx, int yy) {
        super(xx, yy);
    }

    public ConduitElm(int xa, int ya, int xb, int yb, int f,
                      StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

    @Override
    void draw(Graphics g) {
        int hs = 12;
        setBbox(point1, point2, hs);
        g.setColor(color);
        drawLine(g, point1, point2,lineThickness,color);
    }




    @Override
    int getDumpType() {
        return 'w';
    }


}
