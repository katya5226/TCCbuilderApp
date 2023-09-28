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

import com.google.gwt.canvas.dom.client.Context2d.LineCap;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.i18n.client.NumberFormat;
import lahde.tccbuilder.client.util.Locale;

// circuit element class
public abstract class CircuitElm implements Editable {
    // scratch points for convenience
    static Point ps1, ps2;

    static CirSim sim;
    static public Color selectColor;

    static NumberFormat showFormat;
    static CircuitElm mouseElmRef = null;

    double lineThickness = 6;
    double postSize = 3;

    // initial point where user created element. For simple two-terminal elements,
    // this is the first node/post.
    int x, y;

    // point to which user dragged out element. For simple two-terminal elements,
    // this is the second node/post
    int x2, y2;

    int flags;

    // length along x and y axes, and sign of difference
    int dx, dy, dsign;

    int lastHandleGrabbed = -1;

    // length of element
    double dn;

    double dpx1, dpy1;

    // (x,y) and (x2,y2) as Point objects
    Point point1, point2;

    // lead points (ends of wire stubs for simple two-terminal elements)
    Point lead1, lead2;

    Rectangle boundingBox;

    // if subclasses set this to true, element will be horizontal or vertical only
    boolean noDiagonal = true;

    public boolean selected;
    public boolean resizable = true;

    int getDumpType() {

        throw new IllegalStateException(); // Seems necessary to work-around what appears to be a compiler
        // bug affecting OTAElm to make sure this method (which should really be
        // abstract) throws
        // an exception. If you're getting this, try making small update to
        // CompositeElm.java and try again
    }

    static void initClass(CirSim s) {
        sim = s;
        ps1 = new Point();
        ps2 = new Point();

    }

    // create new element with one post at xx,yy, to be dragged out by user
    CircuitElm(int xx, int yy) {
        x = x2 = xx;
        y = y2 = yy;
        flags = 0;

        initBoundingBox();
    }

    // create element between xa,ya and xb,yb from undump
    CircuitElm(int xa, int ya, int xb, int yb, int f) {
        x = xa;
        y = ya;
        x2 = xb;
        y2 = yb;
        flags = f;

        initBoundingBox();
    }

    void initBoundingBox() {
        boundingBox = new Rectangle();
        boundingBox.setBounds(min(x, x2), min(y, y2), abs(x2 - x) + 1, abs(y2 - y) + 1);
    }

    // dump component state for export/undo
    String dump() {
        int t = getDumpType();
        return (t < 127 ? ((char) t) + " " : t + " ") + x + " " + y + " " + x2 + " " + y2 + " " + flags;
    }

    // handle reset button
    void reset() {
    }

    void draw(Graphics g) {
    }


    void delete() {
        if (mouseElmRef == this) mouseElmRef = null;
        sim.deleteSliders(this);
    }

    // calculate post locations and other convenience values used for drawing.
    // Called when element is moved
    void setPoints() {
        dx = x2 - x;
        dy = y2 - y;
        dn = Math.sqrt(dx * dx + dy * dy);
        dpx1 = dy / dn;
        dpy1 = -dx / dn;
        dsign = (dy == 0) ? sign(dx) : sign(dy);
        point1 = new Point(x, y);
        point2 = new Point(x2, y2);
    }

    // calculate lead points for an element of length len. Handy for simple
    // two-terminal elements.
    // Posts are where the user connects wires; leads are ends of wire stubs drawn
    // inside the element.
    void calcLeads(int len) {
        if (dn < len || len == 0) {
            lead1 = point1;
            lead2 = point2;
            return;
        }
        lead1 = interpPoint(point1, point2, (dn - len) / (2 * dn));
        lead2 = interpPoint(point1, point2, (dn + len) / (2 * dn));
    }

    // calculate point fraction f between a and b, linearly interpolated
    Point interpPoint(Point a, Point b, double f) {
        Point p = new Point();
        interpPoint(a, b, p, f);
        return p;
    }

    // calculate point fraction f between a and b, linearly interpolated, return it
    // in c
    void interpPoint(Point a, Point b, Point c, double f) {
        c.x = (int) Math.floor(a.x * (1 - f) + b.x * f + .48);
        c.y = (int) Math.floor(a.y * (1 - f) + b.y * f + .48);
    }

    /**
     * Returns a point fraction f along the line between a and b and offset
     * perpendicular by g
     *
     * @param a 1st Point
     * @param b 2nd Point
     * @param f Fraction along line
     * @param g Fraction perpendicular to line Returns interpolated point in c
     */
    void interpPoint(Point a, Point b, Point c, double f, double g) {
        int gx = b.y - a.y;
        int gy = a.x - b.x;
        g /= Math.sqrt(gx * gx + gy * gy);
        c.x = (int) Math.floor(a.x * (1 - f) + b.x * f + g * gx + .48);
        c.y = (int) Math.floor(a.y * (1 - f) + b.y * f + g * gy + .48);
    }

    /**
     * Calculates two points fraction f along the line between a and b and offest
     * perpendicular by +/-g
     *
     * @param a 1st point (In)
     * @param b 2nd point (In)
     * @param c 1st point (Out)
     * @param d 2nd point (Out)
     * @param f Fraction along line
     * @param g Fraction perpendicular to line
     */
    void interpPoint2(Point a, Point b, Point c, Point d, double f, double g) {
        // int xpd = b.x-a.x;
        // int ypd = b.y-a.y;
        double gx = b.y - a.y;
        double gy = a.x - b.x;
        g /= Math.sqrt(gx * gx + gy * gy);
        c.x = (int) Math.floor(a.x * (1 - f) + b.x * f + g * gx + .48);
        c.y = (int) Math.floor(a.y * (1 - f) + b.y * f + g * gy + .48);
        d.x = (int) Math.floor(a.x * (1 - f) + b.x * f - g * gx + .48);
        d.y = (int) Math.floor(a.y * (1 - f) + b.y * f - g * gy + .48);
    }

    Point[] newPointArray(int n) {
        Point a[] = new Point[n];
        while (n > 0) a[--n] = new Point();
        return a;
    }

    Polygon createPolygon(Point a, Point b, Point c) {
        Polygon p = new Polygon();
        p.addPoint(a.x, a.y);
        p.addPoint(b.x, b.y);
        p.addPoint(c.x, c.y);
        return p;
    }

    // draw second point to xx, yy
    void drag(int xx, int yy) {
        xx = sim.snapGrid(xx);
        yy = sim.snapGrid(yy);
        if (noDiagonal) {
            if (Math.abs(x - xx) < Math.abs(y - yy)) {
                xx = x;
            } else {
                yy = y;
            }
        }
        x2 = xx;
        y2 = yy;
        setPoints();
    }

    void move(int dx, int dy) {
        x += dx;
        y += dy;
        x2 += dx;
        y2 += dy;
        boundingBox.translate(dx, dy);
        setPoints();
    }

    // called when an element is done being dragged out; returns true if it's zero
    // size and should be deleted
    boolean creationFailed() {
        return (x == x2 && y == y2);
    }

    // determine if moving this element by (dx,dy) will put it on top of another
    // element
    boolean allowMove(int dx, int dy) {
        int nx = x + dx;
        int ny = y + dy;
        int nx2 = x2 + dx;
        int ny2 = y2 + dy;
        int i;
        for (i = 0; i != sim.elmList.size(); i++) {
            CircuitElm ce = sim.getElm(i);
            if (ce.x == nx && ce.y == ny && ce.x2 == nx2 && ce.y2 == ny2) return false;
            if (ce.x == nx2 && ce.y == ny2 && ce.x2 == nx && ce.y2 == ny) return false;
        }
        return true;
    }

    void movePoint(int n, int dx, int dy) {
        // modified by IES to prevent the user dragging points to create zero sized
        // nodes
        // that then render improperly
        int oldx = x;
        int oldy = y;
        int oldx2 = x2;
        int oldy2 = y2;
        if (noDiagonal) {
            if (x == x2) dx = 0;
            else dy = 0;
        }
        if (n == 0) {
            x += dx;
            y += dy;
        } else {
            x2 += dx;
            y2 += dy;
        }
        if (x == x2 && y == y2) {
            x = oldx;
            y = oldy;
            x2 = oldx2;
            y2 = oldy2;
        }
        setPoints();
    }

    int getNumHandles() {
        return getPostCount();
    }

    void drawHandles(Graphics g, Color c) {
        postSize+=1;
        g.context.setStrokeStyle(c.getHexValue());
        g.context.setFillStyle(c.getHexValue());
        g.fillRect((int) (x - postSize), (int) (y - postSize), (int) (postSize * 2), (int) (postSize * 2));
        if (getNumHandles() > 1) {
            g.fillRect((int) (x2 - postSize), (int) (y2 - postSize), (int) (postSize * 2), (int) (postSize * 2));
        }
        postSize-=1;

    }

    int getHandleGrabbedClose(int xtest, int ytest, int deltaSq, int minSize) {
        lastHandleGrabbed = -1;
        if (Graphics.distanceSq(x, y, x2, y2) >= minSize) {
            if (Graphics.distanceSq(x, y, xtest, ytest) <= deltaSq) lastHandleGrabbed = 0;
            else if (getNumHandles() > 1 && Graphics.distanceSq(x2, y2, xtest, ytest) <= deltaSq) lastHandleGrabbed = 1;
        }
        return lastHandleGrabbed;
    }

    // int getVoltageSource() { return voltSource; } // Never used except for debug
    // code which is commented out

    int getPostCount() {
        return 2;
    }

    // get position of nth node
    Point getPost(int n) {
        return (n == 0) ? point1 : (n == 1) ? point2 : null;
    }


    void drawPost(Graphics g, Point pt) {
        g.context.save();
        g.context.setStrokeStyle((!needsHighlight() ? Color.white : Color.cyan).getHexValue());
        g.context.setFillStyle((!needsHighlight() ? Color.white : Color.cyan).getHexValue());
        g.fillOval((int) (pt.x - postSize), (int) (pt.y - postSize), (int) (postSize * 2), (int) (postSize * 2));
        g.context.restore();

    }

    void drawPosts(Graphics g) {
        drawPost(g, point1);
        if (getNumHandles() > 1) drawPost(g, point2);
    }

    // set/adjust bounding box used for selecting elements. getCircuitBounds() does
    // not use this!
    void setBbox(int x1, int y1, int x2, int y2) {
        if (x1 > x2) {
            int q = x1;
            x1 = x2;
            x2 = q;
        }
        if (y1 > y2) {
            int q = y1;
            y1 = y2;
            y2 = q;
        }
        boundingBox.setBounds(x1, y1, x2 - x1 + 1, y2 - y1 + 1);
    }

    // set bounding box for an element from p1 to p2 with width w
    void setBbox(Point p1, Point p2, double w) {
        setBbox(p1.x, p1.y, p2.x, p2.y);
        int dpx = (int) (dpx1 * w);
        int dpy = (int) (dpy1 * w);
        adjustBbox(p1.x + dpx, p1.y + dpy, p1.x - dpx, p1.y - dpy);
    }

    // enlarge bbox to contain an additional rectangle
    void adjustBbox(int x1, int y1, int x2, int y2) {
        if (x1 > x2) {
            int q = x1;
            x1 = x2;
            x2 = q;
        }
        if (y1 > y2) {
            int q = y1;
            y1 = y2;
            y2 = q;
        }
        x1 = min(boundingBox.x, x1);
        y1 = min(boundingBox.y, y1);
        x2 = max(boundingBox.x + boundingBox.width, x2);
        y2 = max(boundingBox.y + boundingBox.height, y2);
        boundingBox.setBounds(x1, y1, x2 - x1, y2 - y1);
    }

    // needed for calculating circuit bounds (need to special-case centered text
    // elements)
    boolean isCenteredText() {
        return false;
    }

    static void drawLine(Graphics g, double x1, double y1, double x2, double y2, double thickness, Color color) {
        g.setLineWidth(thickness);
        g.setColor(color);
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
        g.setLineWidth(1.0);
    }

    static void drawLine(Graphics g, double x1, double y1, double x2, double y2, double thickness) {
        drawLine(g, x1, y1, x2, y2, thickness, Color.gray);
    }

    static void drawLine(Graphics g, Point pa, Point pb, double thickness, Color color) {
        drawLine(g, pa.x, pa.y, pb.x, pb.y, thickness, color);
    }

    static void drawLine(Graphics g, Point pa, Point pb, double thickness) {
        drawLine(g, pa.x, pa.y, pb.x, pb.y, thickness);
    }

    static void drawLine(Graphics g, Point pa, Point pb) {
        drawLine(g, pa.x, pa.y, pb.x, pb.y, 3.0);
    }


    static String format(double v, boolean sf) {
        return NumberFormat.getFormat("####.").format(v);
    }

    static String getShortUnitText(double v, String u) {
        return getUnitText(v, u, true);
    }

    private static String getUnitText(double v, String u, boolean sf) {
        String sp = sf ? "" : " ";
        double va = Math.abs(v);
        if (va < 1e-14)
            // this used to return null, but then wires would display "null" with 0V
            return "0" + sp + u;
        if (va < 1e-9) return format(v * 1e12, sf) + sp + "p" + u;
        if (va < 1e-6) return format(v * 1e9, sf) + sp + "n" + u;
        if (va < 1e-3) return format(v * 1e6, sf) + sp + Locale.muString + u;
        if (va < 1) return format(v * 1e3, sf) + sp + "m" + u;
        if (va < 1e3) return format(v, sf) + sp + u;
        if (va < 1e6) return format(v * 1e-3, sf) + sp + "k" + u;
        if (va < 1e9) return format(v * 1e-6, sf) + sp + "M" + u;
        if (va < 1e12) return format(v * 1e-9, sf) + sp + "G" + u;
        return NumberFormat.getFormat("#.##E000").format(v) + sp + u;
    }

    // get component info for display in lower right
    void getInfo(String arr[]) {
    }


    @Override
    public EditInfo getEditInfo(int n) {
        return null;
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
    }

    boolean needsHighlight() {
        return mouseElmRef == this || selected || sim.plotYElm == this;
    }

    boolean isSelected() {
        return selected;
    }

    void setSelected(boolean x) {
        selected = x;
    }

    void selectRect(Rectangle r, boolean add) {
        if (r.intersects(boundingBox)) selected = true;
        else if (!add) selected = false;
    }

    static int abs(int x) {
        return x < 0 ? -x : x;
    }

    static int sign(int x) {
        return (x < 0) ? -1 : (x == 0) ? 0 : 1;
    }

    static int min(int a, int b) {
        return (a < b) ? a : b;
    }

    static int max(int a, int b) {
        return (a > b) ? a : b;
    }

    static double distance(Point p1, Point p2) {
        double x = p1.x - p2.x;
        double y = p1.y - p2.y;
        return Math.sqrt(x * x + y * y);
    }

    Rectangle getBoundingBox() {
        return boundingBox;
    }

    boolean needsShortcut() {
        return getShortcut() > 0;
    }

    int getShortcut() {
        return 0;
    }

    void setMouseElm(boolean v) {
        if (v) mouseElmRef = this;
        else if (mouseElmRef == this) mouseElmRef = null;
    }

    void draggingDone() {

    }

    String dumpModel() {
        return null;
    }

    boolean isMouseElm() {
        return mouseElmRef == this;
    }

    void flipPosts() {
        int oldx = x;
        int oldy = y;
        x = x2;
        y = y2;
        x2 = oldx;
        y2 = oldy;
        setPoints();
    }

    String getClassName() {
        return getClass().getName().replace("lahde.tccbuilder.client.", "");
    }

    native JsArrayString getJsArrayString() /*-{
        return [];
    }-*/;

    JsArrayString getInfoJS() {
        JsArrayString jsarr = getJsArrayString();
        String arr[] = new String[20];
        getInfo(arr);
        int i;
        for (i = 0; arr[i] != null; i++)
            jsarr.push(arr[i]);
        return jsarr;
    }

    native void addJSMethods() /*-{
        var that = this;
        this.getType = $entry(function () {
            return that.@lahde.tccbuilder.client.CircuitElm::getClassName()();
        });
        this.getInfo = $entry(function () {
            return that.@lahde.tccbuilder.client.CircuitElm::getInfoJS()();
        });


        this.getPostCount = $entry(function () {
            return that.@lahde.tccbuilder.client.CircuitElm::getPostCount()();
        });
    }-*/;

    native JavaScriptObject getJavaScriptObject() /*-{
        return this;
    }-*/;

}