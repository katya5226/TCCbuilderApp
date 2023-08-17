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

/*
:root {
  --black: #181C24;
  --blue: #2f3847;
  --deep-blue: #384354;
  --white: #F2F2F2;
  --red: #D93B3B;
  --deep-red: #8d2424;
  --yellow: #e7a217;
  --deep-yellow: #e7a117;
}
*/
public class Color {
    public final static Color white = new Color("#F2F2F2");
    public final static Color darkGray = new Color("#2f3847");
    public final static Color gray = new Color("#384354");
    public final static Color lightGray = new Color("#AEB6BF");
    public final static Color black = new Color("#181C24");
    public final static Color red = new Color("#D93B3B");
    public final static Color deepRed = new Color("#8d2424");
    public final static Color pink = new Color("#FFAFC0");
    public final static Color orange = new Color("#FFC800");
    public final static Color yellow = new Color("#e7a217");
    public final static Color deepYellow = new Color("#e7a117");
    public final static Color green = new Color("#00FF00");
    public final static Color magenta = new Color("#FF00FF");
    public final static Color cyan = new Color("#00FFFF");
    public final static Color blue = new Color("#273459");
    public final static Color deepBlue = new Color("#19223a");
    public static final Color NONE = new Color("");



    private int r, g, b;

    // only for special cases, like no color, or maybe named colors
    private String colorText = null;

    public static Color translateColorIndex(int i) {
        switch (i) {
            case 0:
                return white;
            case 1:
                return lightGray;
            case 2:
                return gray;
            case 3:
                return darkGray;
            case 4:
                return red;
            case 5:
                return pink;
            case 6:
                return orange;
            case 7:
                return yellow;
            case 8:
                return green;
            case 9:
                return magenta;
            case 10:
                return blue;
            default:
                return null;
        }
    }

    // <<Katni>> This needs to be fixed. 
    public static int colorToIndex(Color color) {
        if (color == white)
            return 0;
        if (color == lightGray)
            return 1;
        if (color == gray)
            return 2;
        if (color == darkGray)
            return 3;
        if (color == red)
            return 4;
        if (color == pink)
            return 5;
        if (color == orange)
            return 6;
        if (color == yellow)
            return 7;
        if (color == green)
            return 8;
        if (color == magenta)
            return 9;
        if (color == blue)
            return 10;
        else
            return 0;
    }

    public Color(String colorText) {
        this.colorText = colorText;
        if (colorText.startsWith("#") && colorText.length() == 7) {
            String rs = colorText.substring(1, 3);
            String gs = colorText.substring(3, 5);
            String bs = colorText.substring(5, 7);
            r = Integer.parseInt(rs, 16);
            g = Integer.parseInt(gs, 16);
            b = Integer.parseInt(bs, 16);
        }
    }

    // create mixture of c1 and c2
    public Color(Color c1, Color c2, double mix) {
        double m0 = 1 - mix;
        this.r = (int) (c1.r * m0 + c2.r * mix);
        this.g = (int) (c1.g * m0 + c2.g * mix);
        this.b = (int) (c1.b * m0 + c2.b * mix);
    }

    public Color(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getRed() {
        return r;
    }

    public int getGreen() {
        return g;
    }

    public int getBlue() {
        return b;
    }

    public String getHexValue() {
        if (colorText != null) {
            return colorText;
        }

        return "#"
                + pad(Integer.toHexString(r))
                + pad(Integer.toHexString(g))
                + pad(Integer.toHexString(b));
    }

    private String pad(String in) {
        if (in.length() == 0) {
            return "00";
        }
        if (in.length() == 1) {
            return "0" + in;
        }
        return in;
    }

    public String toString() {
        if (colorText != null) {
            return colorText;
        }
        return "red=" + r + ", green=" + g + ", blue=" + b;
    }
}