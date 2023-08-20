package lahde.tccbuilder.client;

import com.google.gwt.core.client.GWT;
import lahde.tccbuilder.client.util.Locale;

import java.lang.Math;
import java.util.*;

import com.google.gwt.user.client.Window;

public class Component extends ThermalControlElement {

    public Component(int xx, int yy) {
        super(xx, yy);
    }

    public Component(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);
    }

}