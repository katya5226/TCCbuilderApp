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

// Test by Oskar

package com.lushprojects.circuitjs1.client;

// GWT conversion (c) 2015 by Iain Sharp

// For information about the theory behind this, see Electronic Circuit & System Simulation Methods by Pillage
// or https://github.com/sharpie7/circuitjs1/blob/master/INTERNALS.md

import java.util.Vector;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
// import java.security.InvalidParameterException;
// import java.util.InputMismatchException;
import java.lang.Math;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.storage.client.Storage;

import static com.google.gwt.event.dom.client.KeyCodes.*;

import com.lushprojects.circuitjs1.client.util.Locale;
import com.lushprojects.circuitjs1.client.util.PerfMonitor;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.user.client.rpc.AsyncCallback;

@SuppressWarnings({"Convert2Lambda", "CStyleArrayDeclaration"})
public class CirSim implements MouseDownHandler, MouseMoveHandler, MouseUpHandler, ClickHandler, DoubleClickHandler,
        ContextMenuHandler, NativePreviewHandler, MouseOutHandler, MouseWheelHandler {

    Random random;
    Button resetButton;
    Button quickResetButton;
    Button testButton;
    Button runStopButton;
    MenuItem aboutItem;
    MenuItem importFromLocalFileItem, importFromTextItem, exportAsUrlItem, exportAsLocalFileItem, exportAsTextItem,
            printItem, recoverItem, saveFileItem;
    MenuItem importFromDropboxItem;
    MenuItem undoItem, redoItem, cutItem, copyItem, pasteItem, selectAllItem, optionsItem;
    MenuBar optionsMenuBar;
    CheckboxMenuItem dotsCheckItem;
    CheckboxMenuItem voltsCheckItem;
    CheckboxMenuItem tempsCheckItem;
    CheckboxMenuItem powerCheckItem;
    CheckboxMenuItem smallGridCheckItem;
    CheckboxMenuItem crossHairCheckItem;
    CheckboxMenuItem showValuesCheckItem;
    CheckboxMenuItem euroResistorCheckItem;
    CheckboxMenuItem euroGatesCheckItem;
    CheckboxMenuItem printableCheckItem;
    CheckboxMenuItem conventionCheckItem;
    CheckboxMenuItem noEditCheckItem;
    CheckboxMenuItem mouseWheelEditCheckItem;
    Label titleLabel;
    Scrollbar speedBar;
    ListBox scale, dimensionality;
    HTML cyclicOperationLabel;

    MenuBar elmMenuBar;
    MenuItem elmEditMenuItem;
    MenuItem elmCutMenuItem;
    MenuItem elmCopyMenuItem;
    MenuItem elmDeleteMenuItem;
    MenuItem elmTempsMenuItem;
    MenuItem elmFlipMenuItem;
    MenuItem elmSplitMenuItem;
    MenuItem elmSliderMenuItem;
    MenuBar mainMenuBar;
    MenuBar subcircuitMenuBar[];

    String lastCursorStyle;
    boolean mouseWasOverSplitter = false;

    // Class addingClass;
    PopupPanel contextPanel = null;
    int mouseMode = MODE_SELECT;
    int tempMouseMode = MODE_SELECT;
    String mouseModeStr = "Select";
    static final double pi = 3.14159265358979323846;
    static final int MODE_ADD_ELM = 0;
    static final int MODE_DRAG_ALL = 1;
    static final int MODE_DRAG_ROW = 2;
    static final int MODE_DRAG_COLUMN = 3;
    static final int MODE_DRAG_SELECTED = 4;
    static final int MODE_DRAG_POST = 5;
    static final int MODE_SELECT = 6;
    static final int MODE_DRAG_SPLITTER = 7;
    static final int infoWidth = 160;
    int dragGridX, dragGridY, dragScreenX, dragScreenY, initDragGridX, initDragGridY;
    long mouseDownTime;
    long zoomTime;
    int mouseCursorX = -1;
    int mouseCursorY = -1;
    Rectangle selectedArea;
    int gridSize, gridMask, gridRound;
    boolean dragging;
    boolean analyzeFlag, needsStamp, savedFlag;
    boolean dumpMatrix;
    boolean dcAnalysisFlag;
    // boolean useBufferedImage;
    boolean isMac;
    String ctrlMetaKey;
    int pause = 10;
    double t;
    int scopeSelected = -1;
    int scopeMenuSelected = -1;
    int menuScope = -1;
    int menuPlot = -1;
    int hintType = -1, hintItem1, hintItem2;
    String stopMessage;

    // current timestep (time between iterations)
    double timeStep;

    // maximum timestep (== timeStep unless we reduce it because of trouble
    // converging)
    double maxTimeStep;
    double minTimeStep;

    // accumulated time since we incremented timeStepCount
    double timeStepAccum;

    // incremented each time we advance t by maxTimeStep
    int timeStepCount;

    boolean adjustTimeStep;
    boolean developerMode;
    static final int HINT_LC = 1;
    static final int HINT_RC = 2;
    static final int HINT_3DB_C = 3;
    static final int HINT_TWINT = 4;
    static final int HINT_3DB_L = 5;
    Vector<CircuitElm> elmList;
    Vector<Adjustable> adjustables;
    // Vector setupList;
    CircuitElm dragElm, menuElm, stopElm;
    CircuitElm elmArr[];

    private CircuitElm mouseElm = null;
    boolean didSwitch = false;
    int mousePost = -1;
    CircuitElm plotXElm, plotYElm;
    int draggingPost;
    double circuitMatrix[][], circuitRightSide[], lastNodeVoltages[], nodeVoltages[], origRightSide[], origMatrix[][];
    RowInfo circuitRowInfo[];
    int circuitPermute[];
    boolean simRunning;
    boolean circuitNonLinear;
    int voltageSourceCount;
    int circuitMatrixSize, circuitMatrixFullSize;
    boolean circuitNeedsMap;
    // public boolean useFrame;
    ArrayList<Component> trackedTemperatures;
    boolean showResistanceInVoltageSources;
    boolean hideInfoBox;
    static Dialog editDialog, customLogicEditDialog, diodeModelEditDialog;
    static ScrollValuePopup scrollValuePopup;
    static Dialog dialogShowing;
    static AboutBox aboutBox;
    // Class dumpTypes[], shortcuts[];
    String shortcuts[];
    String clipboard;
    String recovery;
    Rectangle circuitArea;
    Vector<UndoItem> undoStack, redoStack;
    double transform[];
    boolean unsavedChanges;

    DockLayoutPanel layoutPanel;
    MenuBar menuBar;
    MenuBar fileMenuBar;
    VerticalPanel verticalPanel;
    CellPanel buttonPanel;
    private boolean mouseDragging;
    double scopeHeightFraction = 0.2;

    Vector<CheckboxMenuItem> mainMenuItems = new Vector<CheckboxMenuItem>();
    Vector<String> mainMenuItemNames = new Vector<String>();

    LoadFile loadFileInput;
    Frame iFrame;

    Canvas cv;
    Context2d cvcontext;

    // canvas width/height in px (before device pixel ratio scaling)
    int canvasWidth, canvasHeight;

    static final int MENUBARHEIGHT = 30;
    static int VERTICALPANELWIDTH = 240; // default
    static final int POSTGRABSQ = 25;
    static final int MINPOSTGRABSIZE = 256;
    final Timer timer = new Timer() {
        public void run() {
            updateCircuit();
        }
    };
    final int FASTTIMER = 16;

    // ************************************** Katni
    // *************************************************
    public Vector<String> materialNames;
    public HashMap<String, Material> materialHashMap;
    public Vector<String> colorChoices;
    public TCC heatCircuit;
    public Vector<Component> simComponents;
    public Vector<TCE> simTCEs;


    public int num_cvs;
    // public int[] special_boundaries;
    public ControlVolume[] sim_cvs;
    public double[] underdiag;
    public double[] diag;
    public double[] upperdiag;
    public double[] rhs;
    public int left_boundary;
    public int right_boundary;
    public double h_left;
    public double h_right;
    public double temp_left;
    public double temp_right;
    public double qIn;
    public double qOut;
    public double startTemp;
    public double ambient_temperature;
    public double[] start_temperatures;
    public ArrayList<Double> times;
    public ArrayList<Double[]> temperatures;
    public double total_time;
    public boolean reach_steady;
    public boolean cyclic;
    public double time;
    public int multipl;
    public int tt;
    public double cpart_t;
    public double dt;
    public int cycle;
    public Vector<CyclePart> cycleParts;
    public CyclePart cyclePart;
    public double cyclePartTime;
    public int cyclePartIndex;
    public int numCycleParts;

    public Vector<Double> cparts_duration;
    public int printing_interval;
    public int[] num_cpart_steps;
    public int[][] cycle_part_start_indeces;
    // public double sim_file;
    // public double temperatures_file;
    public int ud;
    public String[] user_defined_functions;
    double[] x_prev;
    double[] x_mod;

    boolean viewTempsInGraph = true;
    double minTemp, maxTemp;
    public String materialFlagText;
    ArrayList<String> awaitedResponses;
    int testCounter = 0;
    Timer testTimer;


    public enum LengthUnit {
        MICROMETER(1e6, "µm"),
        MILLIMETER(1e3, "mm"),
        CENTIMETER(1e2, "cm"),
        METER(1, "m"),
        KILOMETER(1e-3, "km");

        final double conversionFactor;
        final String unitName;

        LengthUnit(double conversionFactor, String unitName) {
            this.conversionFactor = conversionFactor;
            this.unitName = unitName;
        }
    }


    public LengthUnit selectedLengthUnit = LengthUnit.MILLIMETER;

    String printComponents() {
        String comps = "";
        for (Component c : simComponents) {
            comps += c.index + (c == simComponents.get(simComponents.size() - 1) ? "" : ", ");
        }
        return comps;
    }

    // *********************************************************************************************

    int getrand(int x) {
        int q = random.nextInt();
        if (q < 0)
            q = -q;
        return q % x;
    }

    static native float devicePixelRatio() /*-{
        return window.devicePixelRatio;
    }-*/;

    void checkCanvasSize() {
        if (cv.getCoordinateSpaceWidth() != (int) (canvasWidth * devicePixelRatio()))
            setCanvasSize();
    }

    public void setCanvasSize() {
        int width, height;
        width = (int) RootLayoutPanel.get().getOffsetWidth();
        height = (int) RootLayoutPanel.get().getOffsetHeight();
        height = height - MENUBARHEIGHT;
        width = width - VERTICALPANELWIDTH;
        width = Math.max(width, 0); // avoid exception when setting negative width
        height = Math.max(height, 0);
        if (cv != null) {
            cv.setWidth(width + "PX");
            cv.setHeight(height + "PX");
            canvasWidth = width;
            canvasHeight = height;
            float scale = devicePixelRatio();
            cv.setCoordinateSpaceWidth((int) (width * scale));
            cv.setCoordinateSpaceHeight((int) (height * scale));
        }

        setCircuitArea();

        // recenter circuit in case canvas was hidden at startup
        if (transform[0] == 0)
            centreCircuit();
    }

    void setCircuitArea() {
        int height = canvasHeight;
        int width = canvasWidth;
        int h = 0;
        circuitArea = new Rectangle(0, 0, width, height - h);
    }

    native String decompress(String dump) /*-{
        return $wnd.LZString.decompressFromEncodedURIComponent(dump);
    }-*/;

    // Circuit applet;

    CirSim() {
        // super("Circuit Simulator v1.6d");
        // applet = a;
        // useFrame = false;
        theSim = this;

    }

    String startCircuit = null;
    String startLabel = null;
    String startCircuitText = null;
    String startCircuitLink = null;
    // String baseURL = "http://www.falstad.com/circuit/";

    public void init() {

        // Katni

        boolean printable = false;
        boolean convention = true;
        boolean euroRes = false;
        boolean usRes = false;
        boolean running = false; // Katni
        boolean hideSidebar = false;
        boolean hideMenu = false;
        boolean noEditing = false;
        boolean mouseWheelEdit = false;
        MenuBar m;

        CircuitElm.initClass(this);
        readRecovery();

        QueryParameters qp = new QueryParameters();
        String positiveColor = null;
        String negativeColor = null;
        String neutralColor = null;
        String selectColor = null;
        String currentColor = null;
        String mouseModeReq = null;
        boolean euroGates = false;

        try {
            // baseURL = applet.getDocumentBase().getFile();
            // look for circuit embedded in URL
            // String doc = applet.getDocumentBase().toString();
            String cct = qp.getValue("cct");
            if (cct != null)
                startCircuitText = cct.replace("%24", "$");
            if (startCircuitText == null)
                startCircuitText = getElectronStartCircuitText();
            String ctz = qp.getValue("ctz");
            if (ctz != null)
                startCircuitText = decompress(ctz);
            startCircuit = qp.getValue("startCircuit");
            startLabel = qp.getValue("startLabel");
            startCircuitLink = qp.getValue("startCircuitLink");
            euroRes = qp.getBooleanValue("euroResistors", false);
            euroGates = qp.getBooleanValue("IECGates", getOptionFromStorage("euroGates", weAreInGermany()));
            usRes = qp.getBooleanValue("usResistors", false);
            running = qp.getBooleanValue("running", false); // Katni
            hideSidebar = qp.getBooleanValue("hideSidebar", false);
            hideMenu = qp.getBooleanValue("hideMenu", false);
            printable = qp.getBooleanValue("whiteBackground", getOptionFromStorage("whiteBackground", false));
            convention = qp.getBooleanValue("conventionalCurrent", getOptionFromStorage("conventionalCurrent", true));
            noEditing = !qp.getBooleanValue("editable", true);
            mouseWheelEdit = qp.getBooleanValue("mouseWheelEdit", getOptionFromStorage("mouseWheelEdit", true));
            positiveColor = qp.getValue("positiveColor");
            negativeColor = qp.getValue("negativeColor");
            neutralColor = qp.getValue("neutralColor");
            selectColor = qp.getValue("selectColor");
            currentColor = qp.getValue("currentColor");
            mouseModeReq = qp.getValue("mouseMode");
            hideInfoBox = qp.getBooleanValue("hideInfoBox", false);
        } catch (Exception e) {
        }

        boolean euroSetting = false;
        if (euroRes)
            euroSetting = true;
        else if (usRes)
            euroSetting = false;
        else
            euroSetting = getOptionFromStorage("euroResistors", !weAreInUS(true));

        transform = new double[6];
        String os = Navigator.getPlatform();
        isMac = (os.toLowerCase().contains("mac"));
        ctrlMetaKey = (isMac) ? Locale.LS("Cmd-") : Locale.LS("Ctrl-");

        shortcuts = new String[127];

        layoutPanel = new DockLayoutPanel(Unit.PX);

        fileMenuBar = new MenuBar(true);

        if (isElectron())
            fileMenuBar.addItem(menuItemWithShortcut("window", "New Window...", Locale.LS(ctrlMetaKey + "N"),
                    new MyCommand("file", "newwindow")));

        fileMenuBar.addItem(iconMenuItem("doc-new", "New Blank Circuit", new MyCommand("file", "newblankcircuit")));
        importFromLocalFileItem = menuItemWithShortcut("folder", "Open File...", Locale.LS(ctrlMetaKey + "O"),
                new MyCommand("file", "importfromlocalfile"));
        importFromLocalFileItem.setEnabled(LoadFile.isSupported());
        fileMenuBar.addItem(importFromLocalFileItem);
        importFromTextItem = iconMenuItem("doc-text", "Import From Text...", new MyCommand("file", "importfromtext"));
        fileMenuBar.addItem(importFromTextItem);
        importFromDropboxItem = iconMenuItem("dropbox", "Import From Dropbox...",
                new MyCommand("file", "importfromdropbox"));
        fileMenuBar.addItem(importFromDropboxItem);
        if (isElectron()) {
            saveFileItem = fileMenuBar.addItem(menuItemWithShortcut("floppy", "Save", Locale.LS(ctrlMetaKey + "S"),
                    new MyCommand("file", "save")));
            fileMenuBar.addItem(iconMenuItem("floppy", "Save As...", new MyCommand("file", "saveas")));
        } else {
            exportAsLocalFileItem = menuItemWithShortcut("floppy", "Save As...", Locale.LS(ctrlMetaKey + "S"),
                    new MyCommand("file", "exportaslocalfile"));
            exportAsLocalFileItem.setEnabled(ExportAsLocalFileDialog.downloadIsSupported());
            fileMenuBar.addItem(exportAsLocalFileItem);
        }
        exportAsUrlItem = iconMenuItem("export", "Export As Link...", new MyCommand("file", "exportasurl"));
        fileMenuBar.addItem(exportAsUrlItem);
        exportAsTextItem = iconMenuItem("export", "Export As Text...", new MyCommand("file", "exportastext"));
        fileMenuBar.addItem(exportAsTextItem);
        fileMenuBar.addItem(iconMenuItem("export", "Export As Image...", new MyCommand("file", "exportasimage")));
        fileMenuBar.addItem(iconMenuItem("export", "Export As SVG...", new MyCommand("file", "exportassvg")));
        fileMenuBar.addItem(iconMenuItem("microchip", "Create Subcircuit...", new MyCommand("file", "createsubcircuit")));
        fileMenuBar.addItem(iconMenuItem("magic", "Find DC Operating Point", new MyCommand("file", "dcanalysis")));
        recoverItem = iconMenuItem("back-in-time", "Recover Auto-Save", new MyCommand("file", "recover"));
        recoverItem.setEnabled(recovery != null);
        fileMenuBar.addItem(recoverItem);
        printItem = menuItemWithShortcut("print", "Print...", Locale.LS(ctrlMetaKey + "P"),
                new MyCommand("file", "print"));
        fileMenuBar.addItem(printItem);
        fileMenuBar.addSeparator();
        fileMenuBar.addItem(iconMenuItem("resize-full-alt", "Toggle Full Screen", new MyCommand("view", "fullscreen")));
        fileMenuBar.addSeparator();
        aboutItem = iconMenuItem("info-circled", "About...", (Command) null);
        fileMenuBar.addItem(aboutItem);
        aboutItem.setScheduledCommand(new MyCommand("file", "about"));

        int width = (int) RootLayoutPanel.get().getOffsetWidth();
/*        if (VERTICALPANELWIDTH > 166)
            VERTICALPANELWIDTH = 166;
        if (VERTICALPANELWIDTH < 128)
            VERTICALPANELWIDTH = 128;*/

        menuBar = new MenuBar();
        menuBar.addItem(Locale.LS("File"), fileMenuBar);

        verticalPanel = new VerticalPanel();
        verticalPanel.addStyleName("mainVertical");
        // make buttons side by side if there's room
        buttonPanel = (VERTICALPANELWIDTH >= 166) ? new HorizontalPanel() : new VerticalPanel();
        buttonPanel.addStyleName("buttonPanel");
        m = new MenuBar(true);
        m.addItem(undoItem = menuItemWithShortcut("ccw", "Undo", Locale.LS(ctrlMetaKey + "Z"),
                new MyCommand("edit", "undo")));
        m.addItem(redoItem = menuItemWithShortcut("cw", "Redo", Locale.LS(ctrlMetaKey + "Y"),
                new MyCommand("edit", "redo")));
        m.addSeparator();
        m.addItem(cutItem = menuItemWithShortcut("scissors", "Cut", Locale.LS(ctrlMetaKey + "X"),
                new MyCommand("edit", "cut")));
        m.addItem(copyItem = menuItemWithShortcut("copy", "Copy", Locale.LS(ctrlMetaKey + "C"),
                new MyCommand("edit", "copy")));
        m.addItem(pasteItem = menuItemWithShortcut("paste", "Paste", Locale.LS(ctrlMetaKey + "V"),
                new MyCommand("edit", "paste")));
        pasteItem.setEnabled(false);

        m.addItem(menuItemWithShortcut("clone", "Duplicate", Locale.LS(ctrlMetaKey + "D"),
                new MyCommand("edit", "duplicate")));

        m.addSeparator();
        m.addItem(selectAllItem = menuItemWithShortcut("select-all", "Select All", Locale.LS(ctrlMetaKey + "A"),
                new MyCommand("edit", "selectAll")));
        m.addSeparator();
        m.addItem(menuItemWithShortcut("search", "Find Component...", "/", new MyCommand("edit", "search")));
        m.addItem(iconMenuItem("target", weAreInUS(false) ? "Center Circuit" : "Centre Circuit",
                new MyCommand("edit", "centrecircuit")));
        m.addItem(menuItemWithShortcut("zoom-11", "Zoom 100%", "0", new MyCommand("zoom", "zoom100")));
        m.addItem(menuItemWithShortcut("zoom-in", "Zoom In", "+", new MyCommand("zoom", "zoomin")));
        m.addItem(menuItemWithShortcut("zoom-out", "Zoom Out", "-", new MyCommand("zoom", "zoomout")));
        menuBar.addItem(Locale.LS("Edit"), m);

        MenuBar drawMenuBar = new MenuBar(true);
        drawMenuBar.setAutoOpen(true);

        menuBar.addItem(Locale.LS("Draw"), drawMenuBar);


        optionsMenuBar = m = new MenuBar(true);
        menuBar.addItem(Locale.LS("Options"), optionsMenuBar);
        m.addItem(tempsCheckItem = new CheckboxMenuItem(Locale.LS("Show Temperatures in Graph"), new Command() {
            public void execute() {
                viewTempsInGraph = !viewTempsInGraph;
            }
        }));
        tempsCheckItem.setState(true);
        dotsCheckItem = new CheckboxMenuItem(Locale.LS("Show Current"));
        dotsCheckItem.setState(true);
        voltsCheckItem = new CheckboxMenuItem(Locale.LS("Show Voltage"), new Command() {
            public void execute() {
                if (voltsCheckItem.getState())
                    powerCheckItem.setState(false);
            }
        });
        voltsCheckItem.setState(true);
        powerCheckItem = new CheckboxMenuItem(Locale.LS("Show Power"), new Command() {
            public void execute() {
                if (powerCheckItem.getState())
                    voltsCheckItem.setState(false);
            }
        });
        showValuesCheckItem = new CheckboxMenuItem(Locale.LS("Show Values"));
        showValuesCheckItem.setState(true);
        // m.add(conductanceCheckItem = getCheckItem(LS("Show Conductance")));
        m.addItem(smallGridCheckItem = new CheckboxMenuItem(Locale.LS("Small Grid"), new Command() {
            public void execute() {
                setGrid();
            }
        }));
        m.addItem(crossHairCheckItem = new CheckboxMenuItem(Locale.LS("Show Cursor Cross Hairs"), new Command() {
            public void execute() {
                setOptionInStorage("crossHair", crossHairCheckItem.getState());
            }
        }));
        crossHairCheckItem.setState(getOptionFromStorage("crossHair", false));
        euroResistorCheckItem = new CheckboxMenuItem(Locale.LS("European Resistors"), new Command() {
            public void execute() {
                setOptionInStorage("euroResistors", euroResistorCheckItem.getState());
            }
        });
        euroResistorCheckItem.setState(euroSetting);
        euroGatesCheckItem = new CheckboxMenuItem(Locale.LS("IEC Gates"), new Command() {
            public void execute() {
                setOptionInStorage("euroGates", euroGatesCheckItem.getState());
                int i;
                for (i = 0; i != elmList.size(); i++)
                    getElm(i).setPoints();
            }
        });
        euroGatesCheckItem.setState(euroGates);
        m.addItem(printableCheckItem = new CheckboxMenuItem(Locale.LS("White Background"), new Command() {
            public void execute() {
                setOptionInStorage("whiteBackground", printableCheckItem.getState());
            }
        }));
        printableCheckItem.setState(printable);

        conventionCheckItem = new CheckboxMenuItem(Locale.LS("Conventional Current Motion"), new Command() {
            public void execute() {
                setOptionInStorage("conventionalCurrent", conventionCheckItem.getState());
                String cc = CircuitElm.currentColor.getHexValue();
                // change the current color if it hasn't changed from the default
                if (cc.equals("#ffff00") || cc.equals("#00ffff"))
                    CircuitElm.currentColor = conventionCheckItem.getState() ? Color.yellow : Color.cyan;
            }
        });
        conventionCheckItem.setState(convention);
        m.addItem(noEditCheckItem = new CheckboxMenuItem(Locale.LS("Disable Editing")));
        noEditCheckItem.setState(noEditing);

        m.addItem(mouseWheelEditCheckItem = new CheckboxMenuItem(Locale.LS("Edit Values With Mouse Wheel"),
                new Command() {
                    public void execute() {
                        setOptionInStorage("mouseWheelEdit", mouseWheelEditCheckItem.getState());
                    }
                }));
        mouseWheelEditCheckItem.setState(mouseWheelEdit);

        m.addItem(new CheckboxAlignedMenuItem(Locale.LS("Shortcuts..."), new MyCommand("options", "shortcuts")));
        m.addItem(optionsItem = new CheckboxAlignedMenuItem(Locale.LS("Other Options..."),
                new MyCommand("options", "other")));
        if (isElectron())
            m.addItem(new CheckboxAlignedMenuItem(Locale.LS("Toggle Dev Tools"), new MyCommand("options", "devtools")));

        mainMenuBar = new MenuBar(true);
        mainMenuBar.setAutoOpen(true);
        composeMainMenu(mainMenuBar, 0);
        composeMainMenu(drawMenuBar, 1);
        loadShortcuts();

        if (!hideMenu)
            layoutPanel.addNorth(menuBar, MENUBARHEIGHT);

        if (hideSidebar)
            VERTICALPANELWIDTH = 0;
        else
            layoutPanel.addEast(verticalPanel, VERTICALPANELWIDTH);
        RootLayoutPanel.get().add(layoutPanel);

        cv = Canvas.createIfSupported();
        if (cv == null) {
            RootPanel.get().add(new Label("Not working. You need a browser that supports the CANVAS element."));
            return;
        }

        Window.addResizeHandler(new ResizeHandler() {
            public void onResize(ResizeEvent event) {
                repaint();
            }
        });

        cvcontext = cv.getContext2d();
        setCanvasSize();
        layoutPanel.add(cv);
        verticalPanel.add(buttonPanel);
        //buttonPanel.add(resetButton = new Button(Locale.LS("Reset")));
        buttonPanel.add(resetButton = new Button(Locale.LS("Build TCC")));
        buttonPanel.add(quickResetButton = new Button(Locale.LS("Reset TCC")));
        testButton = new Button(Locale.LS("Test"));
        resetButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                new StartDialog(theSim).show();
            }
        });
        quickResetButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                resetAction();
            }
        });
        testButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                runTestSuite();
            }
        });


        buttonPanel.add(runStopButton = new Button(Locale.LSHTML("<Strong>RUN</Strong>&nbsp;/&nbsp;Stop")));
        runStopButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                setSimRunning(!simIsRunning());
            }
        });

        /*
         * dumpMatrixButton = new Button("Dump Matrix");
         * dumpMatrixButton.addClickHandler(new ClickHandler() { public void
         * onClick(ClickEvent event) { dumpMatrix = true; }});
         * verticalPanel.add(dumpMatrixButton);// IES for debugging
         */

        if (LoadFile.isSupported())
            verticalPanel.add(loadFileInput = new LoadFile(this));

        Label l;
        verticalPanel.add(l = new Label(Locale.LS("Simulation Speed")));
        l.addStyleName("topSpace");

        verticalPanel.add(speedBar = new Scrollbar(Scrollbar.HORIZONTAL, 3, 1, 0, 260));
        speedBar.addStyleName("topSpace");
        verticalPanel.add(l = new Label(Locale.LS("Scale")));
        l.addStyleName("topSpace");
        scale = new ListBox();
        scale.addItem("micrometer");
        scale.addItem("millimeter");
        scale.addItem("centimeter");
        scale.addItem("meter");
        scale.addStyleName("topSpace");
        scale.setSelectedIndex(1);
        verticalPanel.add(scale);
        verticalPanel.add(l = new Label(Locale.LS("Dimensionality")));
        l.addStyleName("topSpace");
        dimensionality = new ListBox();
        dimensionality.addItem("1D");
        dimensionality.addItem("2D");
        dimensionality.addStyleName("topSpace");
        verticalPanel.add(dimensionality);

        scale.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                switch (scale.getSelectedItemText()) {
                    case "micrometer":
                        selectedLengthUnit = CirSim.LengthUnit.MICROMETER;
                        break;
                    case "millimeter":
                        selectedLengthUnit = CirSim.LengthUnit.MILLIMETER;
                        break;
                    case "centimeter":
                        selectedLengthUnit = CirSim.LengthUnit.CENTIMETER;
                        break;
                    case "meter":
                        selectedLengthUnit = CirSim.LengthUnit.METER;
                        break;
                }
                for (Component c : simComponents) {
                    c.calculateLength();
                }
                if (simRunning)
                    resetAction();
            }
        });
        cyclicOperationLabel = new HTML();

        cyclicOperationLabel.addStyleName("topSpace");
        cyclicOperationLabel.setWidth("100%");
        verticalPanel.add(cyclicOperationLabel);


        // l.setFont(f);
        titleLabel = new Label("Label");
        // titleLabel.setFont(f);

        verticalPanel.add(iFrame = new Frame("iframe.html"));
        iFrame.setWidth(VERTICALPANELWIDTH + "px");
        iFrame.setHeight("100 px");
        iFrame.getElement().setAttribute("scrolling", "no");
        setGrid();
        elmList = new Vector<CircuitElm>();
        adjustables = new Vector<Adjustable>();
        // setupList = new Vector();
        undoStack = new Vector<UndoItem>();
        redoStack = new Vector<UndoItem>();
        trackedTemperatures = new ArrayList<Component>();

        awaitedResponses = new ArrayList<String>();
        random = new Random();
        // cv.setBackground(Color.black);
        // cv.setForeground(Color.lightGray);

        elmMenuBar = new MenuBar(true);
        elmMenuBar.setAutoOpen(true);


        elmMenuBar.addItem(elmEditMenuItem = new MenuItem(Locale.LS("Edit..."), new MyCommand("elm", "edit")));
        elmMenuBar.addItem(elmTempsMenuItem = new MenuItem(Locale.LS("Toggle in Temperature Display"),
                new MyCommand("elm", "viewTemps")));
        elmMenuBar.addItem(elmCutMenuItem = new MenuItem(Locale.LS("Cut"), new MyCommand("elm", "cut")));
        elmMenuBar.addItem(elmCopyMenuItem = new MenuItem(Locale.LS("Copy"), new MyCommand("elm", "copy")));
        elmMenuBar.addItem(elmDeleteMenuItem = new MenuItem(Locale.LS("Delete"), new MyCommand("elm", "delete")));
        elmMenuBar.addItem(new MenuItem(Locale.LS("Duplicate"), new MyCommand("elm", "duplicate")));
        elmMenuBar.addItem(elmFlipMenuItem = new MenuItem(Locale.LS("Swap Terminals"), new MyCommand("elm", "flip")));
        elmMenuBar.addItem(elmSplitMenuItem = menuItemWithShortcut("", "Split Wire", Locale.LS(ctrlMetaKey + "click"),
                new MyCommand("elm", "split")));
        elmMenuBar.addItem(elmSliderMenuItem = new MenuItem(Locale.LS("Sliders..."), new MyCommand("elm", "sliders")));


        setColors(positiveColor, negativeColor, neutralColor, selectColor, currentColor);

        if (startCircuitText != null) {
            getSetupList(false);
            readCircuit(startCircuitText);
            unsavedChanges = false;
        } else {
            if (stopMessage == null && startCircuitLink != null) {
                readCircuit("");
                getSetupList(false);
                ImportFromDropboxDialog.setSim(this);
                ImportFromDropboxDialog.doImportDropboxLink(startCircuitLink, false);
            } else {
                readCircuit("");
                if (stopMessage == null && startCircuit != null) {
                    getSetupList(false);
                    readSetupFile(startCircuit, startLabel);
                } else
                    getSetupList(true);
            }
        }

        if (mouseModeReq != null)
            menuPerformed("main", mouseModeReq);

        enableUndoRedo();
        enablePaste();
        setiFrameHeight();
        cv.addMouseDownHandler(this);
        cv.addMouseMoveHandler(this);
        cv.addMouseOutHandler(this);
        cv.addMouseUpHandler(this);
        cv.addClickHandler(this);
        cv.addDoubleClickHandler(this);
        doTouchHandlers(this, cv.getCanvasElement());
        cv.addDomHandler(this, ContextMenuEvent.getType());


        menuBar.addDomHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                doMainMenuChecks();
            }
        }, ClickEvent.getType());
        Event.addNativePreviewHandler(this);
        cv.addMouseWheelHandler(this);

        Window.addWindowClosingHandler(new Window.ClosingHandler() {
            public void onWindowClosing(ClosingEvent event) {
                // there is a bug in electron that makes it impossible to close the app if this
                // warning is given
                if (unsavedChanges && !isElectron())
                    event.setMessage(Locale.LS("Are you sure?  There are unsaved changes."));
            }
        });
        setupJSInterface();
        // ***************************** Katni **************************************
        initHeatSimulation();
        // ***************************************************************************
        setSimRunning(running);
    }

    // ***********************************Katni**********************************************
    void initHeatSimulation() {
        materialNames = new Vector<String>();
        materialHashMap = new HashMap<String, Material>();
        colorChoices = new Vector<String>();
        simComponents = new Vector<Component>();
        this.simTCEs = new Vector<TCE>();
        this.num_cvs = 0;// this.circuit.num_cvs;
        // this.special_boundaries = []
        this.left_boundary = 41;
        this.right_boundary = 42;
        this.h_left = 100000.0;
        this.h_right = 100000.0;
        this.temp_left = 291.0;
        this.temp_right = 290.0;
        this.qIn = 0.0;
        this.qOut = 0.0;
        this.startTemp = 290.0;
        this.ambient_temperature = 293.0;
        // this.start_temperatures = new double[this.num_cvs];
        this.times = new ArrayList<Double>();
        this.temperatures = new ArrayList<Double[]>();
        this.multipl = 1000;
        this.tt = 0;
        this.cpart_t = 0.0;
        this.dt = maxTimeStep * 1e3;
        this.total_time = timeStep;
        this.reach_steady = false;
        this.cyclic = false;
        this.time = 0.0;
        this.cycle = 0;
        this.printing_interval = 1;
        this.cycleParts = new Vector<CyclePart>();
        this.cyclePartTime = 0.0;
        this.cyclePartIndex = 0;

        // this.num_cpart_steps = new double[];
        // this.sim_file = open("results/simulation.txt", "w", encoding = "utf-8")
        // this.temperatures_file = open("results/temperatures.txt", "w", encoding = "utf-8")
        this.ud = 0;
        // this.user_defined_functions = new String[this.num_cvs];
        this.x_prev = new double[this.num_cvs];
        this.x_mod = new double[this.num_cvs];
        colorChoices.add("white");  // I will fix this later.
        colorChoices.add("lightGray");
        colorChoices.add("gray");
        colorChoices.add("darkGray");
        colorChoices.add("red");
        colorChoices.add("pink");
        colorChoices.add("orange");
        colorChoices.add("yellow");
        colorChoices.add("green");
        colorChoices.add("magenta");
        colorChoices.add("blue");

        readMaterialFlags();
        /*readMaterialNames(new AsyncCallback<String[]>() {
            @Override
            public void onSuccess(String[] materialNames) {
                RegExp pattern = RegExp.compile("<a\\s+[^>]*href\\s*=\\s*\"([^\"]*)\"[^>]*>");
                for (String m : materialNames) {
                    if (pattern.test(m)) {
                        MatchResult matcher = pattern.exec(m);
                        if (matcher != null) {
                            String hrefText = matcher.getGroup(1);
                            String decodedHrefText = URL.decodeQueryString(hrefText.substring(0, hrefText.length() - 1));
                            CirSim.this.materialNames.add(decodedHrefText);
                            materialHashMap.put(decodedHrefText, new Material(decodedHrefText, theSim));

                        }
                    }
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                Window.alert(throwable.getMessage());
            }
        });*/
    }

    private void readMaterialFlags() {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, GWT.getModuleBaseURL() + "material_data/materials_flags.csv");
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    //Window.alert(Locale.LS("Can't load data!"));
                    GWT.log("File Error Response", exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        materialFlagText = response.getText();
                        String[] lines = materialFlagText.split("\n");
                        for (String s : Arrays.copyOfRange(lines, 1, lines.length)) {
                            String name = s.split(",")[0];
                            materialHashMap.put(name, new Material(name, theSim));
                            materialNames.add(name);
                        }


                    } else {
                        //Window.alert(Locale.LS("Can't load data!"));
                        GWT.log("Bad file server response:" + response.getStatusText());
                    }
                }
            });
        } catch (RequestException e) {
            GWT.log("failed file reading", e);
        }
    }

    private void readMaterialNames(final AsyncCallback<String[]> callback) {
        final String folderPath = GWT.getModuleBaseURL() + "material_data/materials/";
        // Create a request to the server to get the names of the subfolders
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, folderPath);
        requestBuilder.setCallback(new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                if (response.getStatusCode() == 200) {
                    // Split the response text by newline characters to get the folder names
                    String[] folderNames = response.getText().split("\n");
                    // Invoke the callback with the folder names array
                    callback.onSuccess(folderNames);
                } else {
                    // Invoke the callback with the error message
                    callback.onFailure(new Throwable(response.getStatusCode() + " " + response.getStatusText()));
                }
            }

            @Override
            public void onError(Request request, Throwable exception) {
                // Invoke the callback with the error message
                callback.onFailure(new Throwable("Error: " + exception.getMessage()));
            }
        });

        try {
            // Send the request to the server
            requestBuilder.send();
        } catch (RequestException e) {
            // Invoke the callback with the error message
            callback.onFailure(new Throwable("Failed to get folder names: " + e.getMessage()));
        }
    }

    void makeTCC() {
        simTCEs.clear();
        simTCEs.add(new TCE("TCE1", 0, simComponents));
        heatCircuit = new TCC("Heat circuit", simTCEs);
        heatCircuit.left_boundary = left_boundary;
        heatCircuit.right_boundary = right_boundary;
        heatCircuit.h_left = h_left;
        heatCircuit.h_right = h_right;
        heatCircuit.temp_left = temp_left;
        heatCircuit.temp_right = temp_right;

        heatCircuit.build_TCC();
        heatCircuit.initializeMatrix();
        int n = heatCircuit.num_cvs;
        double[] temps = new double[n];
        Arrays.fill(temps, startTemp);
        heatCircuit.setTemperatures(temps);

        double maxValue = 0;
        for (Component c : simComponents) {
            if (c.material.magnetocaloric) {
                for (Vector<Double> dTcoolingVector : c.material.dTcooling) {
                    maxValue = Math.max(maxValue, Collections.max(dTcoolingVector));
                }

                for (Vector<Double> dTheatingVector : c.material.dTheating) {
                    maxValue = Math.max(maxValue, Collections.max(dTheatingVector));
                }
            }
        }
        if (maxValue == 0) {
            minTemp = Math.min(startTemp, Math.min(temp_left, temp_right));
            maxTemp = Math.max(startTemp, Math.max(temp_left, temp_right));
        } else {
            minTemp = startTemp - maxValue;
            maxTemp = startTemp + maxValue;
        }

    }

    void setHeatSim() {
        num_cvs = heatCircuit.num_cvs;
        underdiag = new double[this.num_cvs];
        diag = new double[this.num_cvs];
        upperdiag = new double[this.num_cvs];
        rhs = new double[this.num_cvs];
        left_boundary = heatCircuit.left_boundary;
        right_boundary = heatCircuit.right_boundary;
        start_temperatures = new double[this.num_cvs];
        numCycleParts = this.cycleParts.size();
        if (cycleParts.size() > 0)
            cyclePart = this.cycleParts.get(0);
        cyclePartTime = 0.0;
        printing_interval = 1;
        total_time = 1.0;
        reach_steady = false;
    }

    void resetHeatSim() {
        times.clear();
        temperatures.clear();
        numCycleParts = 0;
        cyclic = false;
        time = 0.0;
        cycleParts.clear();
    }

    public void append_new_temps() {
        Double[] ttemps = new Double[this.num_cvs];
        for (int i = 0; i < this.num_cvs; i++) {
            ttemps[i] = heatCircuit.cvs.get(i).temperature;
        }
        this.temperatures.add(ttemps);
        this.times.add(this.time);
    }

    public void heat_transfer_step() {

        for (int i = 0; i < this.num_cvs; i++) {
            this.x_prev[i] = heatCircuit.cvs.get(i).temperature_old;
        }
        //while (true) {
        for (int k = 0; k < 3; k++) {
            // heatCircuit.neighbours()
            heatCircuit.calc_conduct_lr();

            heatCircuit.makeMatrix(this.dt);
            ModelMethods.tdmaSolve(heatCircuit.cvs, heatCircuit.underdiag, heatCircuit.diag, heatCircuit.upperdiag,heatCircuit.rhs);
            for (int i = 0; i < this.num_cvs; i++) {
                this.x_mod[i] = heatCircuit.cvs.get(i).temperature;
            }
            // flag = hf.compare(x_mod, x_prev, pa.tolerance)
            boolean flag = true;
            this.x_prev = this.x_mod;
            for (int i = 0; i < simComponents.size(); i++) {
                if (simComponents.get(i).material.cpThysteresis == true)
                    simComponents.get(i).updateModes();
            }
            // if (flag) {
            //     break;
            // }
        }

        heatCircuit.calc_conduct_lr();
        heatCircuit.makeMatrix(this.dt);
        ModelMethods.tdmaSolve(heatCircuit.cvs, heatCircuit.underdiag, heatCircuit.diag, heatCircuit.upperdiag,
                heatCircuit.rhs);
        // if len(this.PCMs) > 0:
        // for i in range(0, len(this.PCMs)):
        // this.PCMs[i].update_temperatures()
        // this.PCMs[i].raise_latent_heat(pa.dt)
        heatCircuit.replace_old_new();
        this.append_new_temps();
        // hf.print_darray_row(this.temperatures[-1], this.temperatures_file, 4)
        // ModelMethods.printTemps(this.temperatures.get(this.temperatures.size()-1));
    }

    // **************************************************************************************

    void setColors(String positiveColor, String negativeColor, String neutralColor, String selectColor,
                   String currentColor) {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor != null) {
            if (positiveColor == null)
                positiveColor = stor.getItem("positiveColor");
            if (negativeColor == null)
                negativeColor = stor.getItem("negativeColor");
            if (neutralColor == null)
                neutralColor = stor.getItem("neutralColor");
            if (selectColor == null)
                selectColor = stor.getItem("selectColor");
            if (currentColor == null)
                currentColor = stor.getItem("currentColor");
        }

        if (positiveColor != null)
            CircuitElm.positiveColor = new Color(URL.decodeQueryString(positiveColor));
        else if (getOptionFromStorage("alternativeColor", false))
            CircuitElm.positiveColor = Color.blue;

        if (negativeColor != null)
            CircuitElm.negativeColor = new Color(URL.decodeQueryString(negativeColor));
        if (neutralColor != null)
            CircuitElm.neutralColor = new Color(URL.decodeQueryString(neutralColor));

        if (selectColor != null)
            CircuitElm.selectColor = new Color(URL.decodeQueryString(selectColor));
        else
            CircuitElm.selectColor = Color.cyan;

        if (currentColor != null)
            CircuitElm.currentColor = new Color(URL.decodeQueryString(currentColor));
        else
            CircuitElm.currentColor = conventionCheckItem.getState() ? Color.yellow : Color.cyan;

        CircuitElm.setColorScale();
    }

    MenuItem menuItemWithShortcut(String icon, String text, String shortcut, MyCommand cmd) {
        final String edithtml = "<div style=\"white-space:nowrap\"><div style=\"display:inline-block;width:100%;\"><i class=\"cirjsicon-";
        String nbsp = "&nbsp;";
        if (icon == "")
            nbsp = "";
        String sn = edithtml + icon + "\"></i>" + nbsp + Locale.LS(text) + "</div>" + shortcut + "</div>";
        return new MenuItem(SafeHtmlUtils.fromTrustedString(sn), cmd);
    }

    MenuItem iconMenuItem(String icon, String text, Command cmd) {
        String icoStr = "<i class=\"cirjsicon-" + icon + "\"></i>&nbsp;" + Locale.LS(text); // <i
        // class="cirjsicon-"></i>&nbsp;
        return new MenuItem(SafeHtmlUtils.fromTrustedString(icoStr), cmd);
    }

    boolean getOptionFromStorage(String key, boolean val) {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
            return val;
        String s = stor.getItem(key);
        if (s == null)
            return val;
        return s == "true";
    }

    void setOptionInStorage(String key, boolean val) {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
            return;
        stor.setItem(key, val ? "true" : "false");
    }

    // save shortcuts to local storage
    void saveShortcuts() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
            return;
        String str = "1";
        int i;
        // format: version;code1=ClassName;code2=ClassName;etc
        for (i = 0; i != shortcuts.length; i++) {
            String sh = shortcuts[i];
            if (sh == null)
                continue;
            str += ";" + i + "=" + sh;
        }
        stor.setItem("shortcuts", str);
    }

    // load shortcuts from local storage
    void loadShortcuts() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
            return;
        String str = stor.getItem("shortcuts");
        if (str == null)
            return;
        String keys[] = str.split(";");

        // clear existing shortcuts
        int i;
        for (i = 0; i != shortcuts.length; i++)
            shortcuts[i] = null;

        // clear shortcuts from menu
        for (i = 0; i != mainMenuItems.size(); i++) {
            CheckboxMenuItem item = mainMenuItems.get(i);
            // stop when we get to drag menu items
            if (item.getShortcut().length() > 1)
                break;
            item.setShortcut("");
        }

        // go through keys (skipping version at start)
        for (i = 1; i < keys.length; i++) {
            String arr[] = keys[i].split("=");
            if (arr.length != 2)
                continue;
            int c = Integer.parseInt(arr[0]);
            String className = arr[1];
            shortcuts[c] = className;

            // find menu item and fix it
            int j;
            for (j = 0; j != mainMenuItems.size(); j++) {
                if (mainMenuItemNames.get(j) == className) {
                    CheckboxMenuItem item = mainMenuItems.get(j);
                    item.setShortcut(Character.toString((char) c));
                    break;
                }
            }
        }
    }

    // install touch handlers
    // don't feel like rewriting this in java. Anyway, java doesn't let us create
    // mouse
    // events and dispatch them.
    native static void doTouchHandlers(CirSim sim, CanvasElement cv) /*-{
	// Set up touch events for mobile, etc
	var lastTap;
	var tmout;
	var lastScale;

	cv.addEventListener("touchstart", function (e) {
        	mousePos = getTouchPos(cv, e);
  		var touch = e.touches[0];
  		var etype = "mousedown";
  		lastScale = 1;
  		clearTimeout(tmout);
  		e.preventDefault();

  		if (e.timeStamp-lastTap < 300) {
     		    etype = "dblclick";
  		} else {
  		    tmout = setTimeout(function() {
  		        sim.@com.lushprojects.circuitjs1.client.CirSim::longPress()();
  		    }, 500);
  		}
  		lastTap = e.timeStamp;

  		var touch1 = e.touches[0];
  		var touch2 = e.touches[e.touches.length-1];
  		var mouseEvent = new MouseEvent(etype, {
    			clientX: .5*(touch1.clientX+touch2.clientX),
    			clientY: .5*(touch1.clientY+touch2.clientY)
  		});
  		cv.dispatchEvent(mouseEvent);
  		if (e.touches.length > 1)
  		    sim.@com.lushprojects.circuitjs1.client.CirSim::twoFingerTouch(II)(mouseEvent.clientX, mouseEvent.clientY - cv.getBoundingClientRect().y);
	}, false);
	cv.addEventListener("touchend", function (e) {
  		var mouseEvent = new MouseEvent("mouseup", {});
  		e.preventDefault();
  		clearTimeout(tmout);
  		cv.dispatchEvent(mouseEvent);
	}, false);
	cv.addEventListener("touchmove", function (e) {
  		e.preventDefault();
  		clearTimeout(tmout);
	        if (e.touches.length > 1) {
	            sim.@com.lushprojects.circuitjs1.client.CirSim::zoomCircuit(D)(40*(Math.log(e.scale)-Math.log(lastScale)));
	            lastScale = e.scale;
	        }
  		var touch1 = e.touches[0];
  		var touch2 = e.touches[e.touches.length-1];
  		var mouseEvent = new MouseEvent("mousemove", {
    			clientX: .5*(touch1.clientX+touch2.clientX),
    			clientY: .5*(touch1.clientY+touch2.clientY)
  		});
  		cv.dispatchEvent(mouseEvent);
	}, false);

	// Get the position of a touch relative to the canvas
	function getTouchPos(canvasDom, touchEvent) {
  		var rect = canvasDom.getBoundingClientRect();
  		return {
    			x: touchEvent.touches[0].clientX - rect.left,
    			y: touchEvent.touches[0].clientY - rect.top
  		};
	}

    }-*/;

    boolean shown = false;

    // this is called twice, once for the Draw menu, once for the right mouse popup
    // menu
    public void composeMainMenu(MenuBar mainMenuBar, int num) {
        mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Component"), "Component"));
        mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Conduit"), ""));
        mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Resistor"), ""));
        mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Switch"), ""));
        mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Diode"), ""));
        mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Regulator"), ""));
        mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Capacitor"), ""));
        mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Heat Source/Sink"), ""));


        MenuBar otherMenuBar = new MenuBar(true);
        CheckboxMenuItem mi;
        otherMenuBar.addItem(mi = getClassCheckItem(Locale.LS("Drag All"), "DragAll"));
        mi.setShortcut(Locale.LS("(Alt-drag)"));
        otherMenuBar.addItem(mi = getClassCheckItem(Locale.LS("Drag Row"), "DragRow"));
        mi.setShortcut(Locale.LS("(A-S-drag)"));
        otherMenuBar.addItem(mi = getClassCheckItem(Locale.LS("Drag Column"), "DragColumn"));
        mi.setShortcut(isMac ? Locale.LS("(A-Cmd-drag)") : Locale.LS("(A-M-drag)"));
        otherMenuBar.addItem(getClassCheckItem(Locale.LS("Drag Selected"), "DragSelected"));
        otherMenuBar.addItem(mi = getClassCheckItem(Locale.LS("Drag Post"), "DragPost"));
        mi.setShortcut("(" + ctrlMetaKey + "drag)");

        mainMenuBar.addItem(
                SafeHtmlUtils.fromTrustedString(CheckboxMenuItem.checkBoxHtml + Locale.LS("&nbsp;</div>Drag")),
                otherMenuBar);

        mainMenuBar.addItem(mi = getClassCheckItem(Locale.LS("Select/Drag Sel."), "Select"));
        mi.setShortcut(Locale.LS("(space/Shift-drag)"));
    }

    void composeSubcircuitMenu() {
        if (subcircuitMenuBar == null)
            return;
        int mi;

        // there are two menus to update: the one in the Draw menu, and the one in the
        // right mouse menu
        for (mi = 0; mi != 2; mi++) {
            MenuBar menu = subcircuitMenuBar[mi];
            menu.clearItems();
            Vector<CustomCompositeModel> list = CustomCompositeModel.getModelList();
            int i;
            for (i = 0; i != list.size(); i++) {
                String name = list.get(i).name;
                menu.addItem(getClassCheckItem(Locale.LS("Add ") + name, "CustomCompositeElm:" + name));
            }
        }
        lastSubcircuitMenuUpdate = CustomCompositeModel.sequenceNumber;
    }

    public void composeSelectScopeMenu(MenuBar sb) {
        sb.clearItems();

    }

    public void setiFrameHeight() {
        if (iFrame == null)
            return;
        int i;
        int cumheight = 0;
        for (i = 0; i < verticalPanel.getWidgetIndex(iFrame); i++) {
            if (verticalPanel.getWidget(i) != loadFileInput) {
                cumheight = cumheight + verticalPanel.getWidget(i).getOffsetHeight();
                if (verticalPanel.getWidget(i).getStyleName().contains("topSpace"))
                    cumheight += 12;
            }
        }
        int ih = RootLayoutPanel.get().getOffsetHeight() - MENUBARHEIGHT - cumheight;
        if (ih < 0)
            ih = 0;
        iFrame.setHeight(ih + "px");
    }

    CheckboxMenuItem getClassCheckItem(String s, String t) {
        // try {
        // Class c = Class.forName(t);
        String shortcut = "";
        CircuitElm elm = null;
        try {
            elm = constructElement(t, 0, 0);
        } catch (Exception e) {
        }
        CheckboxMenuItem mi;
        // register(c, elm);
        if (elm != null) {
            if (elm.needsShortcut()) {
                shortcut += (char) elm.getShortcut();
                if (shortcuts[elm.getShortcut()] != null && !shortcuts[elm.getShortcut()].equals(t))
                    console("already have shortcut for " + (char) elm.getShortcut() + " " + elm);
                shortcuts[elm.getShortcut()] = t;
            }
            elm.delete();
        }
        // else
        // GWT.log("Coudn't create class: "+t);
        // } catch (Exception ee) {
        // ee.printStackTrace();
        // }
        if (shortcut == "")
            mi = new CheckboxMenuItem(s);
        else
            mi = new CheckboxMenuItem(s, shortcut);
        mi.setScheduledCommand(new MyCommand("main", t));
        mainMenuItems.add(mi);
        mainMenuItemNames.add(t);
        return mi;
    }

    void centreCircuit() {
        if (elmList == null) // avoid exception if called during initialization
            return;

        Rectangle bounds = getCircuitBounds();
        setCircuitArea();

        double scale = 1;
        int cheight = circuitArea.height;


        if (bounds != null)
            // add some space on edges because bounds calculation is not perfect
            scale = Math.min(circuitArea.width / (double) (bounds.width + 140),
                    cheight / (double) (bounds.height + 100));
        scale = Math.min(scale, 1.5); // Limit scale so we don't create enormous circuits in big windows

        // calculate transform so circuit fills most of screen
        transform[0] = transform[3] = scale;
        transform[1] = transform[2] = transform[4] = transform[5] = 0;
        if (bounds != null) {
            transform[4] = (circuitArea.width - bounds.width * scale) / 2 - bounds.x * scale;
            transform[5] = (cheight - bounds.height * scale) / 2 - bounds.y * scale;
        }
    }

    // get circuit bounds. remember this doesn't use setBbox(). That is calculated
    // when we draw
    // the circuit, but this needs to be ready before we first draw it, so we use
    // this crude method
    Rectangle getCircuitBounds() {
        int i;
        int minx = 30000, maxx = -30000, miny = 30000, maxy = -30000;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            // centered text causes problems when trying to center the circuit,
            // so we special-case it here
            if (!ce.isCenteredText()) {
                minx = min(ce.x, min(ce.x2, minx));
                maxx = max(ce.x, max(ce.x2, maxx));
            }
            miny = min(ce.y, min(ce.y2, miny));
            maxy = max(ce.y, max(ce.y2, maxy));
        }
        if (minx > maxx)
            return null;
        return new Rectangle(minx, miny, maxx - minx, maxy - miny);
    }

    long lastTime = 0, lastFrameTime, lastIterTime, secTime = 0;
    int frames = 0;
    int steps = 0;
    int framerate = 0, steprate = 0;
    static CirSim theSim;

    public void setSimRunning(boolean s) {
        if (awaitedResponses.size() == 0)
            if (s) {
                if (stopMessage != null)
                    return;
                simRunning = true;
                runStopButton.setHTML(Locale.LSHTML("<strong>RUN</strong>&nbsp;/&nbsp;Stop"));
                runStopButton.removeStyleName("topButton-red");
                timer.scheduleRepeating(FASTTIMER);
            } else {
                simRunning = false;
                runStopButton.setHTML(Locale.LSHTML("Run&nbsp;/&nbsp;<strong>STOP</strong>"));
                runStopButton.addStyleName("topButton-red");
                timer.cancel();
                repaint();
            }
    }

    public boolean simIsRunning() {
        return simRunning;
    }

    boolean needsRepaint;

    void repaint() {
        if (!needsRepaint) {
            needsRepaint = true;
            Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                public boolean execute() {
                    updateCircuit();
                    needsRepaint = false;
                    return false;
                }
            }, FASTTIMER);
        }
    }

    // *****************************************************************
    // UPDATE CIRCUIT

    public void updateCircuit() {
        PerfMonitor perfmon = new PerfMonitor();
        perfmon.startContext("updateCircuit()");

        checkCanvasSize();

        // Analyze circuit
        boolean didAnalyze = analyzeFlag;
        if (analyzeFlag || dcAnalysisFlag) {
            perfmon.startContext("analyzeCircuit()");
            analyzeCircuit();
            analyzeFlag = false;
            perfmon.stopContext();
        }

        // Stamp circuit
        if (needsStamp && simRunning) {
            perfmon.startContext("stampCircuit()");
            try {
                stampCircuit();
            } catch (Exception e) {
                stop("Exception in stampCircuit()", null);
            }
            perfmon.stopContext();
        }

        if (stopElm != null && stopElm != mouseElm)
            stopElm.setMouseElm(true);


        Graphics g = new Graphics(cvcontext);

        if (printableCheckItem.getState()) {
            CircuitElm.whiteColor = Color.black;
            CircuitElm.lightGrayColor = Color.black;
            g.setColor(Color.white);
            cv.getElement().getStyle().setBackgroundColor(Color.white.getHexValue());
        } else {
            CircuitElm.whiteColor = Color.white;
            CircuitElm.lightGrayColor = Color.lightGray;
            g.setColor(Color.black);
            cv.getElement().getStyle().setBackgroundColor(Color.gray.getHexValue());
        }

        // Clear the frame
        g.fillRect(0, 0, canvasWidth, canvasHeight);

        // Run circuit
        if (simRunning) {
            if (needsStamp)
                console("needsStamp while simRunning?");

            perfmon.startContext("runCircuit()");
            try {
                if (awaitedResponses.size() == 0)
                    runCircuit(didAnalyze);
            } catch (Exception e) {
                console("exception in runCircuit " + e.toString());
                //debugger();
                e.printStackTrace();
            }
            perfmon.stopContext();
        }

        long sysTime = System.currentTimeMillis();
        if (simRunning) {
            if (lastTime != 0) {
                int inc = (int) (sysTime - lastTime);
                double c = 0;
                c = java.lang.Math.exp(c / 3.5 - 14.2);
                CircuitElm.currentMult = 1.7 * inc * c;
                if (!conventionCheckItem.getState())
                    CircuitElm.currentMult = -CircuitElm.currentMult;
            }
            lastTime = sysTime;
        } else {
            lastTime = 0;
        }

        if (sysTime - secTime >= 1000) {
            framerate = frames;
            steprate = steps;
            frames = 0;
            steps = 0;
            secTime = sysTime;
        }

        CircuitElm.powerMult = Math.exp(0 / 4.762 - 7);

        perfmon.startContext("graphics");

        g.setFont(CircuitElm.unitsFont);

        if (noEditCheckItem.getState())
            g.drawLock(20, 30);

        g.setColor(Color.white);

        // Set the graphics transform to deal with zoom and offset
        double scale = devicePixelRatio();
        cvcontext.setTransform(transform[0] * scale, 0, 0, transform[3] * scale, transform[4] * scale,
                transform[5] * scale);

        // Draw each element
        perfmon.startContext("elm.draw()");
        for (int i = 0; i != elmList.size(); i++) {
            if (powerCheckItem.getState())
                g.setColor(Color.gray);

            getElm(i).draw(g);
        }
        perfmon.stopContext();

        // Draw posts normally
        if (mouseMode != CirSim.MODE_DRAG_ROW && mouseMode != CirSim.MODE_DRAG_COLUMN) {
            for (int i = 0; i != postDrawList.size(); i++)
                CircuitElm.drawPost(g, postDrawList.get(i));
        }

        // for some mouse modes, what matters is not the posts but the endpoints (which
        // are only the same for 2-terminal elements). We draw those now if needed
        if (tempMouseMode == MODE_DRAG_ROW || tempMouseMode == MODE_DRAG_COLUMN || tempMouseMode == MODE_DRAG_POST
                || tempMouseMode == MODE_DRAG_SELECTED) {
            for (int i = 0; i != elmList.size(); i++) {

                CircuitElm ce = getElm(i);
                // ce.drawPost(g, ce.x , ce.y );
                // ce.drawPost(g, ce.x2, ce.y2);
                if (ce != mouseElm || tempMouseMode != MODE_DRAG_POST) {
                    g.setColor(Color.gray);
                    g.fillOval(ce.x - 3, ce.y - 3, 7, 7);
                    g.fillOval(ce.x2 - 3, ce.y2 - 3, 7, 7);
                } else {
                    ce.drawHandles(g, CircuitElm.selectColor);
                }
            }
        }

        // draw handles for elm we're creating
        if (tempMouseMode == MODE_SELECT && mouseElm != null) {
            mouseElm.drawHandles(g, CircuitElm.selectColor);
        }

        // draw handles for elm we're dragging
        if (dragElm != null && (dragElm.x != dragElm.x2 || dragElm.y != dragElm.y2)) {
            dragElm.draw(g);
            if (dragElm instanceof Component) {
                ((Component) dragElm).calculateLength();
            }


            dragElm.drawHandles(g, CircuitElm.selectColor);
        }

        // draw bad connections. do this last so they will not be overdrawn.
        for (int i = 0; i != badConnectionList.size(); i++) {
            Point cn = badConnectionList.get(i);
            g.setColor(Color.red);
            g.fillOval(cn.x - 3, cn.y - 3, 7, 7);
        }

        // draw the selection rect
        if (selectedArea != null) {
            g.setColor(CircuitElm.selectColor);
            g.drawRect(selectedArea.x, selectedArea.y, selectedArea.width, selectedArea.height);
        }

        // draw the crosshair cursor
        if (crossHairCheckItem.getState() && mouseCursorX >= 0 && mouseCursorX <= circuitArea.width
                && mouseCursorY <= circuitArea.height) {
            g.setColor(Color.gray);
            int x = snapGrid(inverseTransformX(mouseCursorX));
            int y = snapGrid(inverseTransformY(mouseCursorY));
            g.drawLine(x, inverseTransformY(0), x, inverseTransformY(circuitArea.height));
            g.drawLine(inverseTransformX(0), y, inverseTransformX(circuitArea.width), y);
        }

        // reset the graphics scale and translation
        cvcontext.setTransform(scale, 0, 0, scale, 0, 0);


        // draw the temperature display

        if (simComponents.size() > 0)
            if (viewTempsInGraph)
                drawTemperatureGraphs(g);
            else
                drawTemperatureDisplays(g);

        perfmon.startContext("drawBottomArea()");
        drawBottomArea(g);
        perfmon.stopContext();

        g.setColor(Color.white);

        perfmon.stopContext(); // graphics

        if (stopElm != null && stopElm != mouseElm)
            stopElm.setMouseElm(false);

        frames++;

        // if we did DC analysis, we need to re-analyze the circuit with that flag
        // cleared.
        if (dcAnalysisFlag) {
            dcAnalysisFlag = false;
            analyzeFlag = true;
        }

        lastFrameTime = lastTime;

        perfmon.stopContext(); // updateCircuit

        if (developerMode) {
            int height = 15;
            int increment = 15;
            g.drawString("Framerate: " + CircuitElm.showFormat.format(framerate), 10, height);
            g.drawString("Steprate: " + CircuitElm.showFormat.format(steprate), 10, height += increment);
            g.drawString("Steprate/iter: " + CircuitElm.showFormat.format(steprate / getIterCount()), 10,
                    height += increment);
            g.drawString("iterc: " + CircuitElm.showFormat.format(getIterCount()), 10, height += increment);
            g.drawString("Frames: " + frames, 10, height += increment);

            height += (increment * 2);

            String perfmonResult = PerfMonitor.buildString(perfmon).toString();
            String[] splits = perfmonResult.split("\n");
            for (int x = 0; x < splits.length; x++) {
                g.drawString(splits[x], 10, height + (increment * x));
            }
        }

        // This should always be the last
        // thing called by updateCircuit();
        callUpdateHook();
    }

    void drawTemperatureDisplays(Graphics g) {
        Context2d ctx = g.context;
        double maxElementHeight = 30.0;
        int h = (int) (trackedTemperatures.size() * maxElementHeight < 200
                ? trackedTemperatures.size() * maxElementHeight
                : 200);
        g.setColor(Color.black.getHexValue());
        g.fillRect(0, circuitArea.height - h, circuitArea.width, canvasHeight - circuitArea.height + h);

        // drawing the index and calculating the sizes of the elements
        double indexWidth = (circuitArea.width * 1.00) * .05;
        double elementHeight = h / trackedTemperatures.size();
        double YOffset = circuitArea.height - h;
        double XOffSet = 0;
        // drawing the displays
        for (Component component : trackedTemperatures) {
            try {
                XOffSet = 0;
                double[] temps = component.listTemps();
                double elementWidth = (circuitArea.width * 1.00 - indexWidth) / (temps.length);
                drawDisplaySegment(g, XOffSet, YOffset, indexWidth, elementHeight, component.index + "",
                        component.color.getHexValue());
                XOffSet += indexWidth;
                for (double temp : temps) {
                    drawDisplaySegment(g, XOffSet, YOffset, elementWidth, elementHeight, NumberFormat.getFormat("#.00").format(temp), component.color.getHexValue());
                    XOffSet += elementWidth;
                }
            } catch (Exception e) {
            }

            YOffset += elementHeight;

        }

    }

    void drawTemperatureGraphs(Graphics g) {
        double maxElementHeight = 30.0;
        int h = (int) (trackedTemperatures.size() * maxElementHeight < 200
                ? trackedTemperatures.size() * maxElementHeight
                : 200);
        h = 250;
        if (heatCircuit == null)
            return;

        double elementWidth = (double) (circuitArea.width) / trackedTemperatures.size();
        double elementHeight = h * .9;

        double YOffset = circuitArea.height - h;
        double XOffSet = 100;
        double prevX = 0;
        double prevY = 0;

        //drawing the graph's background
        Context2d ctx = g.context;
        ctx.setFillStyle(Color.white.getHexValue());
        //ctx.fillRect(XOffSet, YOffset, circuitArea.width, h);
        ctx.setStrokeStyle(Color.lightGray.getHexValue());
        ctx.setLineWidth(0.5);

        double tempDiff = Math.abs(maxTemp - minTemp);

        int numberOfLines = 10;
        for (int i = 0; i < numberOfLines + 1; i++) {
            ctx.beginPath();
            double y = YOffset + ((elementHeight / numberOfLines) * i);
            String text = NumberFormat.getFormat("#.00").format((maxTemp - (tempDiff / (numberOfLines)) * i));

            ctx.moveTo(XOffSet * .75, y);
            ctx.lineTo(circuitArea.width - XOffSet * .25, YOffset + ((elementHeight / numberOfLines) * i));
            ctx.stroke();

            double fontSize = Math.min(XOffSet / text.length(), (elementHeight / numberOfLines) / 2);
            ctx.setFont(fontSize + "px sans-serif");

            TextMetrics metrics = ctx.measureText(text);
            double textWidth = metrics.getWidth();

            double textX = 0 + (XOffSet - textWidth) / 2;
            double textY = y + (fontSize) / 2;
            ctx.fillText(text, textX, textY);
        }

        double tempWidth = ((double) (circuitArea.width - 1.5 * XOffSet) / ((num_cvs) - 1));
        double innerX = XOffSet;
        Color prevColor = Color.NONE;
        for (Component component : trackedTemperatures) {
            try {
                double[] temps = component.listTemps();
                double tempHeight = elementHeight;
                double innerY = YOffset;

/*                if (trackedTemperatures.size() > 1) {
                    drawRectangleWithText(g, innerX, YOffset - 25, tempWidth * temps.length, 25, String.valueOf(component.index), component.color.getHexValue(), false);
                }*/
                if (component == trackedTemperatures.get(0)) {
                    innerX -= tempWidth / 2;
                }

                for (int i = 0, tempsLength = temps.length; i < tempsLength; i++) {
                    double temp = temps[i];

                    double[] tmp;
                    if (i == 0) {
                        drawGraphLine(g, prevX, prevY, innerX, innerY, tempWidth, tempHeight, temp,
                                minTemp,
                                maxTemp,
                                prevColor.getHexValue());
                    } else {
                        drawGraphLine(g, prevX, prevY, innerX, innerY, tempWidth, tempHeight, temp,
                                minTemp,
                                maxTemp,
                                component.color.getHexValue());
                    }

                    tmp = drawGraphDot(g, prevX, prevY, innerX, innerY, tempWidth, tempHeight, temp,
                            minTemp,
                            maxTemp,
                            component.color.getHexValue());

                    prevX = tmp[0];
                    prevY = tmp[1];
                    innerX += tempWidth;
                }
                prevColor = component.color;

            } catch (Exception ignored) {

            }

            XOffSet += elementWidth;

        }

    }

    private double[] drawGraphDot(Graphics g, double prevX, double prevY, double x, double y, double width, double height, double value, double minValue, double maxValue, String colorDot) {
        Context2d ctx = g.context;

        // Calculate the position of the dot relative to the value and the min/max values
        double dotRadius = 5;
        double valueRange = maxValue - minValue;
        double relativeValue = (value - minValue) / valueRange;
        double dotY = (y + height) - (height * relativeValue);
        double dotX = x + width / 2.0;
        ctx.setFillStyle(colorDot);

        //g.drawLine(prevX,prevY,dotX,dotY);
        ctx.beginPath();
        ctx.arc(dotX, dotY, dotRadius, 0, 2 * Math.PI);
        ctx.fill();
        ctx.closePath();
        return new double[]{dotX, dotY};
    }

    private double[] drawGraphLine(Graphics g, double prevX, double prevY, double x, double y, double width, double height, double value, double minValue, double maxValue, String colorLine) {
        Context2d ctx = g.context;

        // Calculate the position of the dot relative to the value and the min/max values
        double dotRadius = 5;
        double valueRange = maxValue - minValue;
        double relativeValue = (value - minValue) / valueRange;
        double dotY = (y + height) - (height * relativeValue);
        double dotX = x + width / 2.0;

        ctx.setStrokeStyle(colorLine);
        ctx.setLineWidth(2.5);

        if (prevY != 0) {
            ctx.beginPath();
            ctx.moveTo(prevX, prevY);
            ctx.lineTo(dotX, dotY);
            ctx.stroke();
        }
        return new double[]{dotX, dotY};
    }


    private void drawDisplaySegment(Graphics g, double x, double y, double width, double height, String text,
                                    String color) {
        Context2d ctx = g.context;
        ctx.setFillStyle(color);
        ctx.setStrokeStyle(color);
        ctx.setLineWidth(1);
        // Draw the rectangle
        ctx.strokeRect(x, y, width, height);

        // Set the font size according to the rectangle size
        double fontSize = Math.min(width / text.length(), height / 2);
        ctx.setFont("bold " + fontSize + "px sans-serif");

        // Measure the text width
        TextMetrics metrics = ctx.measureText(text);
        double textWidth = metrics.getWidth();

        // Calculate the position to center the text
        double textX = x + (width - textWidth) / 2;
        double textY = y + (height + (fontSize * .85) / 2) / 2;

        // Draw the text
        ctx.fillText(text, textX, textY);
    }

    void drawBottomArea(Graphics g) {
        int leftX = 0;
        int h = 0;
        if (stopMessage != null && circuitArea.height > canvasHeight - 30)
            h = 30;
        g.setColor(printableCheckItem.getState() ? "#eee" : "#111");


        g.setFont(CircuitElm.unitsFont);
        g.setColor(CircuitElm.whiteColor);
        //GWT.log(dragElm.toString());
        if (stopMessage != null) {
            g.drawString(stopMessage, 10, canvasHeight - 10);
        } else if (!hideInfoBox) {
            // in JS it doesn't matter how big this is, there's no out-of-bounds exception
            String[] info = new String[10];
            if (dragElm != null) {
                dragElm.getInfo(info);
            } else if (mouseElm != null) {
                mouseElm.getInfo(info);
            } else {
                info[0] = Locale.LS("t = ") + (double) Math.round(this.time * 100) / 100 + " s";
                info[1] = Locale.LS("time step = ") + this.dt + " s";
                info[2] = Locale.LS("components = " + printComponents());

            }
            int i;
            if (hintType != -1) {
                i = 0;
                while (info[i] != null)
                    i++;
                String s = getHint();
                if (s == null)
                    hintType = -1;
                else
                    info[i] = s;
            }
            int x = leftX + 5;

            // x = max(x, canvasWidth*2/3);
            // x=cv.getCoordinateSpaceWidth()*2/3;

            // count lines of data
            for (i = 0; info[i] != null; i++)
                ;
            int badnodes = badConnectionList.size();
            if (badnodes > 0)
                info[i++] = badnodes + ((badnodes == 1) ? Locale.LS(" bad connection") : Locale.LS(" bad connections"));
            if (savedFlag)
                info[i++] = "(saved)";

            for (i = 0; info[i] != null; i++)
                g.drawString(info[i], x, 15 * (i + 1));
        }
    }

    Color getBackgroundColor() {
        if (printableCheckItem.getState())
            return Color.white;
        return Color.black;
    }


    String getHint() {
/*        CircuitElm c1 = getElm(hintItem1);
        CircuitElm c2 = getElm(hintItem2);
        if (c1 == null || c2 == null)
            return null;
        if (hintType == HINT_LC) {
            if (!(c1 instanceof InductorElm))
                return null;
            if (!(c2 instanceof CapacitorElm))
                return null;
            InductorElm ie = (InductorElm) c1;
            CapacitorElm ce = (CapacitorElm) c2;
            return Locale.LS("res.f = ")
                    + CircuitElm.getUnitText(1 / (2 * pi * Math.sqrt(ie.inductance * ce.capacitance)), "Hz");
        }
        if (hintType == HINT_RC) {
            if (!(c1 instanceof ResistorElm))
                return null;
            if (!(c2 instanceof CapacitorElm))
                return null;
            ResistorElm re = (ResistorElm) c1;
            CapacitorElm ce = (CapacitorElm) c2;
            return "RC = " + CircuitElm.getUnitText(re.resistance * ce.capacitance, "s");
        }
        if (hintType == HINT_3DB_C) {
            if (!(c1 instanceof ResistorElm))
                return null;
            if (!(c2 instanceof CapacitorElm))
                return null;
            ResistorElm re = (ResistorElm) c1;
            CapacitorElm ce = (CapacitorElm) c2;
            return Locale.LS("f.3db = ") + CircuitElm.getUnitText(1 / (2 * pi * re.resistance * ce.capacitance), "Hz");
        }
        if (hintType == HINT_3DB_L) {
            if (!(c1 instanceof ResistorElm))
                return null;
            if (!(c2 instanceof InductorElm))
                return null;
            ResistorElm re = (ResistorElm) c1;
            InductorElm ie = (InductorElm) c2;
            return Locale.LS("f.3db = ") + CircuitElm.getUnitText(re.resistance / (2 * pi * ie.inductance), "Hz");
        }
        if (hintType == HINT_TWINT) {
            if (!(c1 instanceof ResistorElm))
                return null;
            if (!(c2 instanceof CapacitorElm))
                return null;
            ResistorElm re = (ResistorElm) c1;
            CapacitorElm ce = (CapacitorElm) c2;
            return Locale.LS("fc = ") + CircuitElm.getUnitText(1 / (2 * pi * re.resistance * ce.capacitance), "Hz");
        }*/
        return null;
    }

    // public void toggleSwitch(int n) {
    // int i;
    // for (i = 0; i != elmList.size(); i++) {
    // CircuitElm ce = getElm(i);
    // if (ce instanceof SwitchElm) {
    // n--;
    // if (n == 0) {
    // ((SwitchElm) ce).toggle();
    // analyzeFlag = true;
    // cv.repaint();
    // return;
    // }
    // }
    // }
    // }

    void needAnalyze() {
        analyzeFlag = true;
        repaint();
    }

    Vector<CircuitNode> nodeList;
    Vector<Point> postDrawList = new Vector<Point>();
    Vector<Point> badConnectionList = new Vector<Point>();
    CircuitElm voltageSources[];

    public CircuitNode getCircuitNode(int n) {
        if (n >= nodeList.size())
            return null;
        return nodeList.elementAt(n);
    }

    public CircuitElm getElm(int n) {
        if (n >= elmList.size())
            return null;
        return elmList.elementAt(n);
    }

    public Adjustable findAdjustable(CircuitElm elm, int item) {
        int i;
        for (i = 0; i != adjustables.size(); i++) {
            Adjustable a = adjustables.get(i);
            if (a.elm == elm && a.editItem == item)
                return a;
        }
        return null;
    }

    public static native void console(String text)
	/*-{
	    console.log(text);
	}-*/;

    public static native void debugger() /*-{ debugger; }-*/;

    class NodeMapEntry {
        int node;

        NodeMapEntry() {
            node = -1;
        }

        NodeMapEntry(int n) {
            node = n;
        }
    }

    // map points to node numbers
    HashMap<Point, NodeMapEntry> nodeMap;
    HashMap<Point, Integer> postCountMap;

    class WireInfo {
        CircuitElm wire;
        Vector<CircuitElm> neighbors;
        int post;

        WireInfo(CircuitElm w) {
            wire = w;
        }
    }

    // info about each wire and its neighbors, used to calculate wire currents
    Vector<WireInfo> wireInfoList;

    // find groups of nodes connected by wire equivalents and map them to the same
    // node. this speeds things
    // up considerably by reducing the size of the matrix. We do this for wires,
    // labeled nodes, and ground.
    // The actual node we map to is not assigned yet. Instead we map to the same
    // NodeMapEntry.
    void calculateWireClosure() {
        int i;
        LabeledNodeElm.resetNodeList();
        GroundElm.resetNodeList();
        nodeMap = new HashMap<Point, NodeMapEntry>();
        // int mergeCount = 0;
        wireInfoList = new Vector<WireInfo>();
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (!ce.isRemovableWire())
                continue;
            ce.hasWireInfo = false;
            wireInfoList.add(new WireInfo(ce));
            Point p0 = ce.getPost(0);
            NodeMapEntry cn = nodeMap.get(p0);

            // what post are we connected to
            Point p1 = ce.getConnectedPost();
            if (p1 == null) {
                // no connected post (true for labeled node the first time it's encountered, or
                // ground)
                if (cn == null) {
                    cn = new NodeMapEntry();
                    nodeMap.put(p0, cn);
                }
                continue;
            }
            NodeMapEntry cn2 = nodeMap.get(p1);
            if (cn != null && cn2 != null) {
                // merge nodes; go through map and change all keys pointing to cn2 to point to
                // cn
                for (Map.Entry<Point, NodeMapEntry> entry : nodeMap.entrySet()) {
                    if (entry.getValue() == cn2)
                        entry.setValue(cn);
                }
                // mergeCount++;
                continue;
            }
            if (cn != null) {
                nodeMap.put(p1, cn);
                continue;
            }
            if (cn2 != null) {
                nodeMap.put(p0, cn2);
                continue;
            }
            // new entry
            cn = new NodeMapEntry();
            nodeMap.put(p0, cn);
            nodeMap.put(p1, cn);
        }

        // console("got " + (groupCount-mergeCount) + " groups with " + nodeMap.size() +
        // " nodes " + mergeCount);
    }

    // generate info we need to calculate wire currents. Most other elements
    // calculate currents using
    // the voltage on their terminal nodes. But wires have the same voltage at both
    // ends, so we need
    // to use the neighbors' currents instead. We used to treat wires as zero
    // voltage sources to make
    // this easier, but this is very inefficient, since it makes the matrix 2 rows
    // bigger for each wire.
    // We create a list of WireInfo objects instead to help us calculate the wire
    // currents instead,
    // so we make the matrix less complex, and we only calculate the wire currents
    // when we need them
    // (once per frame, not once per subiteration). We need the WireInfos arranged
    // in the correct order,
    // each one containing a list of neighbors and which end to use (since one end
    // may be ready before
    // the other)
    boolean calcWireInfo() {
        int i;
        int moved = 0;

        for (i = 0; i != wireInfoList.size(); i++) {
            WireInfo wi = wireInfoList.get(i);
            CircuitElm wire = wi.wire;
            CircuitNode cn1 = nodeList.get(wire.getNode(0)); // both ends of wire have same node #
            int j;

            Vector<CircuitElm> neighbors0 = new Vector<CircuitElm>();
            Vector<CircuitElm> neighbors1 = new Vector<CircuitElm>();

            // assume each end is ready (except ground nodes which have one end)
            // labeled nodes are treated as having 2 terminals, see below
            boolean isReady0 = true, isReady1 = !(wire instanceof GroundElm);

            // go through elements sharing a node with this wire (may be connected
            // indirectly
            // by other wires, but at least it's faster than going through all elements)
            for (j = 0; j != cn1.links.size(); j++) {
                CircuitNodeLink cnl = cn1.links.get(j);
                CircuitElm ce = cnl.elm;
                if (ce == wire)
                    continue;
                Point pt = ce.getPost(cnl.num);

                // is this a wire that doesn't have wire info yet? If so we can't use it yet.
                // That would create a circular dependency. So that side isn't ready.
                boolean notReady = (ce.isRemovableWire() && !ce.hasWireInfo);

                // which post does this element connect to, if any?
                if (pt.x == wire.x && pt.y == wire.y) {
                    neighbors0.add(ce);
                    if (notReady)
                        isReady0 = false;
                } else if (wire.getPostCount() > 1) {
                    Point p2 = wire.getConnectedPost();
                    if (pt.x == p2.x && pt.y == p2.y) {
                        neighbors1.add(ce);
                        if (notReady)
                            isReady1 = false;
                    }
                } else if (ce instanceof LabeledNodeElm && wire instanceof LabeledNodeElm
                        && ((LabeledNodeElm) ce).text == ((LabeledNodeElm) wire).text) {
                    // ce and wire are both labeled nodes with matching labels. treat them as
                    // neighbors
                    neighbors1.add(ce);
                    if (notReady)
                        isReady1 = false;
                }
            }

            // does one of the posts have all information necessary to calculate current?
            if (isReady0) {
                wi.neighbors = neighbors0;
                wi.post = 0;
                wire.hasWireInfo = true;
                moved = 0;
            } else if (isReady1) {
                wi.neighbors = neighbors1;
                wi.post = 1;
                wire.hasWireInfo = true;
                moved = 0;
            } else {
                // no, so move to the end of the list and try again later
                wireInfoList.add(wireInfoList.remove(i--));
                moved++;
                if (moved > wireInfoList.size() * 2) {
                    stop("wire loop detected", wire);
                    return false;
                }
            }
        }

        return true;
    }

    // find or allocate ground node
    void setGroundNode() {
        int i;
        boolean gotGround = false;
        boolean gotRail = false;
        CircuitElm volt = null;

        // System.out.println("ac1");
        // look for voltage or ground element
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce instanceof GroundElm) {
                gotGround = true;

                // set ground node to 0
                NodeMapEntry nme = nodeMap.get(ce.getPost(0));
                nme.node = 0;
                break;
            }

            if (volt == null && ce instanceof VoltageElm)
                volt = ce;
        }

        // if no ground, and no rails, then the voltage elm's first terminal
        // is ground
        if (!gotGround && volt != null && !gotRail) {
            CircuitNode cn = new CircuitNode();
            Point pt = volt.getPost(0);
            nodeList.addElement(cn);

            // update node map
            NodeMapEntry cln = nodeMap.get(pt);
            if (cln != null)
                cln.node = 0;
            else
                nodeMap.put(pt, new NodeMapEntry(0));
        } else {
            // otherwise allocate extra node for ground
            CircuitNode cn = new CircuitNode();
            nodeList.addElement(cn);
        }
    }

    // make list of nodes
    void makeNodeList() {
        int i, j;
        int vscount = 0;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            int inodes = ce.getInternalNodeCount();
            int ivs = ce.getVoltageSourceCount();
            int posts = ce.getPostCount();

            // allocate a node for each post and match posts to nodes
            for (j = 0; j != posts; j++) {
                Point pt = ce.getPost(j);
                Integer g = postCountMap.get(pt);
                postCountMap.put(pt, g == null ? 1 : g + 1);
                NodeMapEntry cln = nodeMap.get(pt);

                // is this node not in map yet? or is the node number unallocated?
                // (we don't allocate nodes before this because changing the allocation order
                // of nodes changes circuit behavior and breaks backward compatibility;
                // the code below to connect unconnected nodes may connect a different node to
                // ground)
                if (cln == null || cln.node == -1) {
                    CircuitNode cn = new CircuitNode();
                    CircuitNodeLink cnl = new CircuitNodeLink();
                    cnl.num = j;
                    cnl.elm = ce;
                    cn.links.addElement(cnl);
                    ce.setNode(j, nodeList.size());
                    if (cln != null)
                        cln.node = nodeList.size();
                    else
                        nodeMap.put(pt, new NodeMapEntry(nodeList.size()));
                    nodeList.addElement(cn);
                } else {
                    int n = cln.node;
                    CircuitNodeLink cnl = new CircuitNodeLink();
                    cnl.num = j;
                    cnl.elm = ce;
                    getCircuitNode(n).links.addElement(cnl);
                    ce.setNode(j, n);
                    // if it's the ground node, make sure the node voltage is 0,
                    // cause it may not get set later
                    if (n == 0)
                        ce.setNodeVoltage(j, 0);
                }
            }
            for (j = 0; j != inodes; j++) {
                CircuitNode cn = new CircuitNode();
                cn.internal = true;
                CircuitNodeLink cnl = new CircuitNodeLink();
                cnl.num = j + posts;
                cnl.elm = ce;
                cn.links.addElement(cnl);
                ce.setNode(cnl.num, nodeList.size());
                nodeList.addElement(cn);
            }

            // also count voltage sources so we can allocate array
            vscount += ivs;
        }

        voltageSources = new CircuitElm[vscount];
    }

    Vector<Integer> unconnectedNodes;
    Vector<CircuitElm> nodesWithGroundConnection;
    int nodesWithGroundConnectionCount;

    void findUnconnectedNodes() {
        int i, j;

        // determine nodes that are not connected indirectly to ground.
        // all nodes must be connected to ground somehow, or else we
        // will get a matrix error.
        boolean closure[] = new boolean[nodeList.size()];
        boolean changed = true;
        unconnectedNodes = new Vector<Integer>();
        nodesWithGroundConnection = new Vector<CircuitElm>();
        closure[0] = true;
        while (changed) {
            changed = false;
            for (i = 0; i != elmList.size(); i++) {
                CircuitElm ce = getElm(i);
                if (ce instanceof WireElm)
                    continue;
                // loop through all ce's nodes to see if they are connected
                // to other nodes not in closure
                boolean hasGround = false;
                for (j = 0; j < ce.getConnectionNodeCount(); j++) {
                    boolean hg = ce.hasGroundConnection(j);
                    if (hg)
                        hasGround = true;
                    if (!closure[ce.getConnectionNode(j)]) {
                        if (hg)
                            closure[ce.getConnectionNode(j)] = changed = true;
                        continue;
                    }
                    int k;
                    for (k = 0; k != ce.getConnectionNodeCount(); k++) {
                        if (j == k)
                            continue;
                        int kn = ce.getConnectionNode(k);
                        if (ce.getConnection(j, k) && !closure[kn]) {
                            closure[kn] = true;
                            changed = true;
                        }
                    }
                }
                if (hasGround)
                    nodesWithGroundConnection.add(ce);
            }
            if (changed)
                continue;

            // connect one of the unconnected nodes to ground with a big resistor, then try
            // again
            for (i = 0; i != nodeList.size(); i++)
                if (!closure[i] && !getCircuitNode(i).internal) {
                    unconnectedNodes.add(i);
                    //console("node " + i + " unconnected");
                    // stampResistor(0, i, 1e8); // do this later in connectUnconnectedNodes()
                    closure[i] = true;
                    changed = true;
                    break;
                }
        }
    }

    // take list of unconnected nodes, which we identifie222211d earlier, and connect them
    // to ground
    // with a big resistor. otherwise we will get matrix errors. The resistor has to
    // be big,
    // otherwise circuits like 555 Square Wave will break
    void connectUnconnectedNodes() {
        int i;
        if (unconnectedNodes.size() > 1)
            Window.alert("Nodes not properly connected!");
        for (i = 0; i != unconnectedNodes.size(); i++) {
            int n = unconnectedNodes.get(i);
            stampResistor(0, n, 1e8);
        }
    }

    boolean validateCircuit() {
        int i, j;

        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);

            // look for current sources with no current path
            if (ce instanceof CurrentElm) {
                CurrentElm cur = (CurrentElm) ce;
                FindPathInfo fpi = new FindPathInfo(FindPathInfo.INDUCT, ce, ce.getNode(1));
                cur.setBroken(!fpi.findPath(ce.getNode(0)));
            }


            // look for voltage source or wire loops. we do this for voltage sources
            if (ce.getPostCount() == 2) {
                if (ce instanceof VoltageElm) {
                    FindPathInfo fpi = new FindPathInfo(FindPathInfo.VOLTAGE, ce, ce.getNode(1));
                    if (fpi.findPath(ce.getNode(0))) {
                        stop("Voltage source/wire loop with no resistance!", ce);
                        return false;
                    }
                }
            }


        }
        return true;
    }

    // analyze the circuit when something changes, so it can be simulated
    void analyzeCircuit() {
        stopMessage = null;
        stopElm = null;
        if (elmList.isEmpty()) {
            postDrawList = new Vector<Point>();
            badConnectionList = new Vector<Point>();
            return;
        }
        int i, j;
        nodeList = new Vector<CircuitNode>();
        postCountMap = new HashMap<Point, Integer>();

        calculateWireClosure();
        setGroundNode();

        // allocate nodes and voltage sources
        makeNodeList();

        makePostDrawList();
        if (!calcWireInfo())
            return;
        nodeMap = null; // done with this

        int vscount = 0;
        circuitNonLinear = false;

        // determine if circuit is nonlinear. also set voltage sources
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.nonLinear())
                circuitNonLinear = true;
            int ivs = ce.getVoltageSourceCount();
            for (j = 0; j != ivs; j++) {
                voltageSources[vscount] = ce;
                ce.setVoltageSource(j, vscount++);
            }
        }
        voltageSourceCount = vscount;

        // show resistance in voltage sources if there's only one.
        // can't use voltageSourceCount here since that counts internal voltage sources,
        // like the one in GroundElm
        boolean gotVoltageSource = false;
        showResistanceInVoltageSources = true;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce instanceof VoltageElm) {
                if (gotVoltageSource)
                    showResistanceInVoltageSources = false;
                else
                    gotVoltageSource = true;
            }
        }

        findUnconnectedNodes();
        if (!validateCircuit())
            return;

        nodesWithGroundConnectionCount = nodesWithGroundConnection.size();
        // only need this for validation
        nodesWithGroundConnection = null;

        timeStep = maxTimeStep;
        needsStamp = true;

        callAnalyzeHook();
    }

    // stamp the matrix, meaning populate the matrix as required to simulate the
    // circuit (for all linear elements, at least)
    void stampCircuit() {
        int i;
        int matrixSize = nodeList.size() - 1 + voltageSourceCount;
        circuitMatrix = new double[matrixSize][matrixSize];
        circuitRightSide = new double[matrixSize];
        nodeVoltages = new double[nodeList.size() - 1];
        if (lastNodeVoltages == null || lastNodeVoltages.length != nodeVoltages.length)
            lastNodeVoltages = new double[nodeList.size() - 1];
        origMatrix = new double[matrixSize][matrixSize];
        origRightSide = new double[matrixSize];
        circuitMatrixSize = circuitMatrixFullSize = matrixSize;
        circuitRowInfo = new RowInfo[matrixSize];
        circuitPermute = new int[matrixSize];
        for (i = 0; i != matrixSize; i++)
            circuitRowInfo[i] = new RowInfo();
        circuitNeedsMap = false;

        connectUnconnectedNodes();

        // stamp linear circuit elements
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            ce.setParentList(elmList);
            ce.stamp();
        }

        if (!simplifyMatrix(matrixSize))
            return;

        // check if we called stop()
        if (circuitMatrix == null)
            return;

        // if a matrix is linear, we can do the lu_factor here instead of
        // needing to do it every frame
        if (!circuitNonLinear) {
            if (!lu_factor(circuitMatrix, circuitMatrixSize, circuitPermute)) {
                stop("Singular matrix!", null);
                return;
            }
        }


        // ******************************* Katni
        // **************************************************
        // ****************************************************************************************

        needsStamp = false;
    }

    // simplify the matrix; this speeds things up quite a bit, especially for
    // digital circuits.
    // or at least it did before we added wire removal
    boolean simplifyMatrix(int matrixSize) {
        int i, j;
        for (i = 0; i != matrixSize; i++) {
            int qp = -1;
            double qv = 0;
            RowInfo re = circuitRowInfo[i];
            /*
             * System.out.println("row " + i + " " + re.lsChanges + " " + re.rsChanges + " "
             * + re.dropRow);
             */

            // if (qp != -100) continue; // uncomment this line to disable matrix
            // simplification for debugging purposes

            if (re.lsChanges || re.dropRow || re.rsChanges)
                continue;
            double rsadd = 0;

            // see if this row can be removed
            for (j = 0; j != matrixSize; j++) {
                double q = circuitMatrix[i][j];
                if (circuitRowInfo[j].type == RowInfo.ROW_CONST) {
                    // keep a running total of const values that have been
                    // removed already
                    rsadd -= circuitRowInfo[j].value * q;
                    continue;
                }
                // ignore zeroes
                if (q == 0)
                    continue;
                // keep track of first nonzero element that is not ROW_CONST
                if (qp == -1) {
                    qp = j;
                    qv = q;
                    continue;
                }
                // more than one nonzero element? give up
                break;
            }
            if (j == matrixSize) {
                if (qp == -1) {
                    // probably a singular matrix, try disabling matrix simplification above to
                    // check this
                    stop("Matrix error", null);
                    return false;
                }
                RowInfo elt = circuitRowInfo[qp];
                // we found a row with only one nonzero nonconst entry; that value
                // is a constant
                if (elt.type != RowInfo.ROW_NORMAL) {
                    System.out.println("type already " + elt.type + " for " + qp + "!");
                    continue;
                }
                elt.type = RowInfo.ROW_CONST;
                // console("ROW_CONST " + i + " " + rsadd);
                elt.value = (circuitRightSide[i] + rsadd) / qv;
                circuitRowInfo[i].dropRow = true;
                // find first row that referenced the element we just deleted
                for (j = 0; j != i; j++)
                    if (circuitMatrix[j][qp] != 0)
                        break;
                // start over just before that
                i = j - 1;
            }
        }
        // System.out.println("ac7");

        // find size of new matrix
        int nn = 0;
        for (i = 0; i != matrixSize; i++) {
            RowInfo elt = circuitRowInfo[i];
            if (elt.type == RowInfo.ROW_NORMAL) {
                elt.mapCol = nn++;
                // System.out.println("col " + i + " maps to " + elt.mapCol);
                continue;
            }
            if (elt.type == RowInfo.ROW_CONST)
                elt.mapCol = -1;
        }

        // make the new, simplified matrix
        int newsize = nn;
        double newmatx[][] = new double[newsize][newsize];
        double newrs[] = new double[newsize];
        int ii = 0;
        for (i = 0; i != matrixSize; i++) {
            RowInfo rri = circuitRowInfo[i];
            if (rri.dropRow) {
                rri.mapRow = -1;
                continue;
            }
            newrs[ii] = circuitRightSide[i];
            rri.mapRow = ii;
            // System.out.println("Row " + i + " maps to " + ii);
            for (j = 0; j != matrixSize; j++) {
                RowInfo ri = circuitRowInfo[j];
                if (ri.type == RowInfo.ROW_CONST)
                    newrs[ii] -= ri.value * circuitMatrix[i][j];
                else
                    newmatx[ii][ri.mapCol] += circuitMatrix[i][j];
            }
            ii++;
        }

        // console("old size = " + matrixSize + " new size = " + newsize);

        circuitMatrix = newmatx;
        circuitRightSide = newrs;
        matrixSize = circuitMatrixSize = newsize;
        for (i = 0; i != matrixSize; i++)
            origRightSide[i] = circuitRightSide[i];
        for (i = 0; i != matrixSize; i++)
            for (j = 0; j != matrixSize; j++)
                origMatrix[i][j] = circuitMatrix[i][j];
        circuitNeedsMap = true;
        return true;
    }

    // make list of posts we need to draw. posts shared by 2 elements should be
    // hidden, all
    // others should be drawn. We can't use the node list for this purpose anymore
    // because wires
    // have the same node number at both ends.
    void makePostDrawList() {
        postDrawList = new Vector<Point>();
        badConnectionList = new Vector<Point>();
        for (Map.Entry<Point, Integer> entry : postCountMap.entrySet()) {
            if (entry.getValue() != 2)
                postDrawList.add(entry.getKey());

            // look for bad connections, posts not connected to other elements which
            // intersect
            // other elements' bounding boxes
            if (entry.getValue() == 1) {
                int j;
                boolean bad = false;
                Point cn = entry.getKey();
                for (j = 0; j != elmList.size() && !bad; j++) {
                    CircuitElm ce = getElm(j);
                    if (ce instanceof GraphicElm)
                        continue;
                    // does this post intersect elm's bounding box?
                    if (!ce.boundingBox.contains(cn.x, cn.y))
                        continue;
                    int k;
                    // does this post belong to the elm?
                    int pc = ce.getPostCount();
                    for (k = 0; k != pc; k++)
                        if (ce.getPost(k).equals(cn))
                            break;
                    if (k == pc)
                        bad = true;
                }
                if (bad)
                    badConnectionList.add(cn);
            }
        }
        postCountMap = null;
    }

    class FindPathInfo {
        static final int INDUCT = 1;
        static final int VOLTAGE = 2;
        static final int SHORT = 3;
        static final int CAP_V = 4;
        boolean visited[];
        int dest;
        CircuitElm firstElm;
        int type;

        // State object to help find loops in circuit subject to various conditions
        // (depending on type_)
        // elm_ = source and destination element. dest_ = destination node.
        FindPathInfo(int type_, CircuitElm elm_, int dest_) {
            dest = dest_;
            type = type_;
            firstElm = elm_;
            visited = new boolean[nodeList.size()];
        }

        // look through circuit for loop starting at node n1 of firstElm, for a path
        // back to
        // dest node of firstElm
        boolean findPath(int n1) {
            if (n1 == dest)
                return true;

            // depth first search, don't need to revisit already visited nodes!
            if (visited[n1])
                return false;

            visited[n1] = true;
            CircuitNode cn = getCircuitNode(n1);
            int i;
            if (cn == null)
                return false;
            for (i = 0; i != cn.links.size(); i++) {
                CircuitNodeLink cnl = cn.links.get(i);
                CircuitElm ce = cnl.elm;
                if (checkElm(n1, ce))
                    return true;
            }
            if (n1 == 0) {
                for (i = 0; i != nodesWithGroundConnection.size(); i++)
                    if (checkElm(0, nodesWithGroundConnection.get(i)))
                        return true;
            }
            return false;
        }

        boolean checkElm(int n1, CircuitElm ce) {
            if (ce == firstElm)
                return false;
            if (type == INDUCT) {
                // inductors need a path free of current sources
                if (ce instanceof CurrentElm)
                    return false;
            }
            if (type == VOLTAGE) {
                // when checking for voltage loops, we only care about voltage
                // sources/wires/ground
                if (!(ce.isWireEquivalent() || ce instanceof VoltageElm || ce instanceof GroundElm))
                    return false;
            }
            // when checking for shorts, just check wires
            if (type == SHORT && !ce.isWireEquivalent())
                return false;

            if (n1 == 0) {
                // look for posts which have a ground connection;
                // our path can go through ground
                int j;
                for (j = 0; j != ce.getConnectionNodeCount(); j++)
                    if (ce.hasGroundConnection(j) && findPath(ce.getConnectionNode(j)))
                        return true;
            }
            int j;
            for (j = 0; j != ce.getConnectionNodeCount(); j++) {
                if (ce.getConnectionNode(j) == n1) {
                    if (ce.hasGroundConnection(j) && findPath(0))
                        return true;
                    int k;
                    for (k = 0; k != ce.getConnectionNodeCount(); k++) {
                        if (j == k)
                            continue;
                        if (ce.getConnection(j, k) && findPath(ce.getConnectionNode(k))) {
                            // System.out.println("got findpath " + n1);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    void stop(String s, CircuitElm ce) {
        stopMessage = Locale.LS(s);
        circuitMatrix = null; // causes an exception
        stopElm = ce;
        setSimRunning(false);
        analyzeFlag = false;
        // cv.repaint();
    }

    // control voltage source vs with voltage from n1 to n2 (must
    // also call stampVoltageSource())
    void stampVCVS(int n1, int n2, double coef, int vs) {
        int vn = nodeList.size() + vs;
        stampMatrix(vn, n1, coef);
        stampMatrix(vn, n2, -coef);
    }

    // stamp independent voltage source #vs, from n1 to n2, amount v
    void stampVoltageSource(int n1, int n2, int vs, double v) {
        int vn = nodeList.size() + vs;
        stampMatrix(vn, n1, -1);
        stampMatrix(vn, n2, 1);
        stampRightSide(vn, v);
        stampMatrix(n1, vn, 1);
        stampMatrix(n2, vn, -1);
    }

    // use this if the amount of voltage is going to be updated in doStep(), by
    // updateVoltageSource()
    void stampVoltageSource(int n1, int n2, int vs) {
        int vn = nodeList.size() + vs;
        stampMatrix(vn, n1, -1);
        stampMatrix(vn, n2, 1);
        stampRightSide(vn);
        stampMatrix(n1, vn, 1);
        stampMatrix(n2, vn, -1);
    }

    // update voltage source in doStep()
    void updateVoltageSource(int n1, int n2, int vs, double v) {
        int vn = nodeList.size() + vs;
        stampRightSide(vn, v);
    }

    void stampResistor(int n1, int n2, double r) {
        double r0 = 1 / r;
        if (Double.isNaN(r0) || Double.isInfinite(r0)) {
            System.out.print("bad resistance " + r + " " + r0 + "\n");
            int a = 0;
            a /= a;
        }
        stampMatrix(n1, n1, r0);
        stampMatrix(n2, n2, r0);
        stampMatrix(n1, n2, -r0);
        stampMatrix(n2, n1, -r0);
    }

    void stampConductance(int n1, int n2, double r0) {
        stampMatrix(n1, n1, r0);
        stampMatrix(n2, n2, r0);
        stampMatrix(n1, n2, -r0);
        stampMatrix(n2, n1, -r0);
    }

    // specify that current from cn1 to cn2 is equal to voltage from vn1 to 2,
    // divided by g
    void stampVCCurrentSource(int cn1, int cn2, int vn1, int vn2, double g) {
        stampMatrix(cn1, vn1, g);
        stampMatrix(cn2, vn2, g);
        stampMatrix(cn1, vn2, -g);
        stampMatrix(cn2, vn1, -g);
    }

    void stampCurrentSource(int n1, int n2, double i) {
        stampRightSide(n1, -i);
        stampRightSide(n2, i);
    }

    // stamp a current source from n1 to n2 depending on current through vs
    void stampCCCS(int n1, int n2, int vs, double gain) {
        int vn = nodeList.size() + vs;
        stampMatrix(n1, vn, gain);
        stampMatrix(n2, vn, -gain);
    }

    // Katni
    // void buildTCC(){

    // }

    // ******************************* Katni
    // **************************************************
    void stampHeatMatrix() {

    }
    // ****************************************************************************************

    // stamp value x in row i, column j, meaning that a voltage change
    // of dv in node j will increase the current into node i by x dv.
    // (Unless i or j is a voltage source node.)
    void stampMatrix(int i, int j, double x) {
        if (Double.isInfinite(x))
            debugger();
        if (i > 0 && j > 0) {
            if (circuitNeedsMap) {
                i = circuitRowInfo[i - 1].mapRow;
                RowInfo ri = circuitRowInfo[j - 1];
                if (ri.type == RowInfo.ROW_CONST) {
                    // System.out.println("Stamping constant " + i + " " + j + " " + x);
                    circuitRightSide[i] -= x * ri.value;
                    return;
                }
                j = ri.mapCol;
                // System.out.println("stamping " + i + " " + j + " " + x);
            } else {
                i--;
                j--;
            }
            circuitMatrix[i][j] += x;
        }
    }

    // stamp value x on the right side of row i, representing an
    // independent current source flowing into node i
    void stampRightSide(int i, double x) {
        if (i > 0) {
            if (circuitNeedsMap) {
                i = circuitRowInfo[i - 1].mapRow;
                // System.out.println("stamping " + i + " " + x);
            } else
                i--;
            circuitRightSide[i] += x;
        }
    }

    // indicate that the value on the right side of row i changes in doStep()
    void stampRightSide(int i) {
        // System.out.println("rschanges true " + (i-1));
        if (i > 0)
            circuitRowInfo[i - 1].rsChanges = true;
    }

    // indicate that the values on the left side of row i change in doStep()
    void stampNonLinear(int i) {
        if (i > 0)
            circuitRowInfo[i - 1].lsChanges = true;
    }

    double getIterCount() {
        // IES - remove interaction
        if (speedBar.getValue() == 0)
            return 0;

        return .1 * Math.exp((speedBar.getValue() - 61) / 24.);

    }

    // we need to calculate wire currents for every iteration if someone is viewing
    // a wire in the
    // scope. Otherwise we can do it only once per frame.
    boolean canDelayWireProcessing() {

        return true;
    }

    boolean converged;
    int subIterations;

    void runCircuit(boolean didAnalyze) {

        // Katni commented lines bellow
        if (circuitMatrix == null || elmList.size() == 0) {
            circuitMatrix = null;
            return;
        }
        int iter;
        //int maxIter = getIterCount();
        boolean debugprint = dumpMatrix;
        dumpMatrix = false;
        long steprate = (long) (160 * getIterCount());
        long tm = System.currentTimeMillis();
        long lit = lastIterTime;
        if (lit == 0) {
            lastIterTime = tm;
            return;
        }
        // Check if we don't need to run simulation (for very slow simulation speeds).
        // If the circuit changed, do at least one iteration to make sure everything is consistent.
        if (1000 >= steprate * (tm - lastIterTime) && !didAnalyze)
            return;
        boolean delayWireProcessing = canDelayWireProcessing();
        int timeStepCountAtFrameStart = timeStepCount;

        stampCircuit();
        //Window.alert(String.valueOf(didAnalyze));

        for (iter = 1; ; iter++) {
            // *************************** Katni *******************************
            if (!this.cyclic) {
                heat_transfer_step();
                time += dt;
            } else {
                //CirSim.debugger();
                if (this.cycleParts.size() == 0) {
                    Window.alert("Sim set to cyclic but cycle parts undefined");
                }
                this.cyclePart.execute();

                if (this.cyclePart.duration > 0.0) {
                    time += dt;
                }
                this.cyclePartTime += dt;
                if (this.cyclePartTime >= this.cyclePart.duration) {
                    this.cyclePartTime = 0.0;
                    this.cyclePartIndex = (this.cyclePartIndex + 1) % this.numCycleParts;
                    this.cyclePart = this.cycleParts.get(this.cyclePartIndex);
                }
            }
            // *****************************************************************
            t += timeStep;
            timeStepAccum += timeStep;
            if (timeStepAccum >= maxTimeStep) {
                timeStepAccum -= maxTimeStep;
                timeStepCount++;
            }

            tm = System.currentTimeMillis();
            lit = tm;
            // Check whether enough time has elapsed to perform an *additional* iteration after
            // those we have already completed. But limit total computation time to 50ms (20fps)
            if ((timeStepCount - timeStepCountAtFrameStart) * 1000 >= steprate * (tm - lastIterTime) || (tm - lastFrameTime > 50))
                break;
            if (!simRunning)
                break;
        }

        lastIterTime = lit;
        if (delayWireProcessing)
            calcWireCurrents();


    }

    // set node voltages given right side found by solving matrix
    void applySolvedRightSide(double rs[]) {
        // console("setvoltages " + rs);
        int j;
        for (j = 0; j != circuitMatrixFullSize; j++) {
            RowInfo ri = circuitRowInfo[j];
            double res = 0;
            if (ri.type == RowInfo.ROW_CONST)
                res = ri.value;
            else
                res = rs[ri.mapCol];
            if (Double.isNaN(res)) {
                converged = false;
                break;
            }
            if (j < nodeList.size() - 1) {
                nodeVoltages[j] = res;
            } else {
                int ji = j - (nodeList.size() - 1);
                voltageSources[ji].setCurrent(ji, res);
            }
        }

        setNodeVoltages(nodeVoltages);
    }

    // set node voltages in each element given an array of node voltages
    void setNodeVoltages(double nv[]) {
        int j, k;
        for (j = 0; j != nv.length; j++) {
            double res = nv[j];
            CircuitNode cn = getCircuitNode(j + 1);
            for (k = 0; k != cn.links.size(); k++) {
                CircuitNodeLink cnl = cn.links.elementAt(k);
                cnl.elm.setNodeVoltage(cnl.num, res);
            }
        }
    }

    // we removed wires from the matrix to speed things up. in order to display wire
    // currents,
    // we need to calculate them now.
    void calcWireCurrents() {
        int i;

        // for debugging
        // for (i = 0; i != wireInfoList.size(); i++)
        // wireInfoList.get(i).wire.setCurrent(-1, 1.23);

        for (i = 0; i != wireInfoList.size(); i++) {
            WireInfo wi = wireInfoList.get(i);
            double cur = 0;
            int j;
            Point p = wi.wire.getPost(wi.post);
            for (j = 0; j != wi.neighbors.size(); j++) {
                CircuitElm ce = wi.neighbors.get(j);
                int n = ce.getNodeAtPoint(p.x, p.y);
                cur += ce.getCurrentIntoNode(n);
            }
            // get correct current polarity
            // (LabeledNodes may have wi.post == 1, in which case we flip the current sign)
            if (wi.post == 0 || (wi.wire instanceof LabeledNodeElm))
                wi.wire.setCurrent(-1, cur);
            else
                wi.wire.setCurrent(-1, -cur);
        }
    }

    int min(int a, int b) {
        return (a < b) ? a : b;
    }

    int max(int a, int b) {
        return (a > b) ? a : b;
    }

    public void resetAction() {
        int i;
        analyzeFlag = true;
        if (t == 0)
            setSimRunning(true);
        t = timeStepAccum = 0;
        timeStepCount = 0;
        for (i = 0; i != elmList.size(); i++)
            getElm(i).reset();
        repaint();
        makeTCC();
        setHeatSim();
    }

    static void electronSaveAsCallback(String s) {
        s = s.substring(s.lastIndexOf('/') + 1);
        s = s.substring(s.lastIndexOf('\\') + 1);
        theSim.setCircuitTitle(s);
        theSim.allowSave(true);
        theSim.savedFlag = true;
        theSim.repaint();
    }

    static void electronSaveCallback() {
        theSim.savedFlag = true;
        theSim.repaint();
    }

    static native void electronSaveAs(String dump) /*-{
        $wnd.showSaveDialog().then(function (file) {
            if (file.canceled)
            	return;
            $wnd.saveFile(file, dump);
            @com.lushprojects.circuitjs1.client.CirSim::electronSaveAsCallback(Ljava/lang/String;)(file.filePath.toString());
        });
    }-*/;

    static native void electronSave(String dump) /*-{
        $wnd.saveFile(null, dump);
        @com.lushprojects.circuitjs1.client.CirSim::electronSaveCallback()();
    }-*/;

    static void electronOpenFileCallback(String text, String name) {
        LoadFile.doLoadCallback(text, name);
        theSim.allowSave(true);
    }

    static native void electronOpenFile() /*-{
        $wnd.openFile(function (text, name) {
            @com.lushprojects.circuitjs1.client.CirSim::electronOpenFileCallback(Ljava/lang/String;Ljava/lang/String;)(text, name);
        });
    }-*/;

    static native void toggleDevTools() /*-{
        $wnd.toggleDevTools();
    }-*/;

    static native boolean isElectron() /*-{
        return ($wnd.openFile != undefined);
    }-*/;

    static native String getElectronStartCircuitText() /*-{
    	return $wnd.startCircuitText;
    }-*/;

    void allowSave(boolean b) {
        if (saveFileItem != null)
            saveFileItem.setEnabled(b);
    }

    public void menuPerformed(String menu, String item) {
        if ((menu == "edit" || menu == "main" || menu == "scopes") && noEditCheckItem.getState()) {
            Window.alert(Locale.LS("Editing disabled.  Re-enable from the Options menu."));
            return;
        }
        if (item == "about")
            aboutBox = new AboutBox(circuitjs1.versionString);
        if (item == "importfromlocalfile") {
            pushUndo();
            if (isElectron())
                electronOpenFile();
            else
                loadFileInput.click();
        }
        if (item == "newwindow") {
            Window.open(Document.get().getURL(), "_blank", "");
        }
        if (item == "save")
            electronSave(dumpCircuit());
        if (item == "saveas")
            electronSaveAs(dumpCircuit());
        if (item == "importfromtext") {
            dialogShowing = new ImportFromTextDialog(this);
        }
        if (item == "importfromdropbox") {
            dialogShowing = new ImportFromDropboxDialog(this);
        }
        if (item == "exportasurl") {
            doExportAsUrl();
            unsavedChanges = false;
        }
        if (item == "exportaslocalfile") {
            doExportAsLocalFile();
            unsavedChanges = false;
        }
        if (item == "exportastext") {
            doExportAsText();
            unsavedChanges = false;
        }
        if (item == "exportasimage")
            doExportAsImage();
        if (item == "exportassvg")
            doExportAsSVG();
        if (item == "createsubcircuit")
            doCreateSubcircuit();

        if (item == "print")
            doPrint();
        if (item == "recover")
            doRecover();

        if ((menu == "elm" || menu == "scopepop") && contextPanel != null)
            contextPanel.hide();
        if (menu == "options" && item == "shortcuts") {
            dialogShowing = new ShortcutsDialog(this);
            dialogShowing.show();
        }
        if (item == "search") {
            dialogShowing = new SearchDialog(this);
            dialogShowing.show();
        }
        if (menu == "options" && item == "other")
            doEdit(new EditOptions(this));
        if (item == "devtools")
            toggleDevTools();
        if (item == "undo")
            doUndo();
        if (item == "redo")
            doRedo();

        // if the mouse is hovering over an element, and a shortcut key is pressed,
        // operate on that element (treat it like a context menu item selection)
        if (menu == "key" && mouseElm != null) {
            menuElm = mouseElm;
            menu = "elm";
        }

        if (item == "cut") {
            if (menu != "elm")
                menuElm = null;
            doCut();
        }
        if (item == "copy") {
            if (menu != "elm")
                menuElm = null;
            doCopy();
        }
        if (item == "paste")
            doPaste(null);
        if (item == "duplicate") {
            if (menu != "elm")
                menuElm = null;
            doDuplicate();
        }
        if (item == "flip")
            doFlip();
        if (item == "split")
            doSplit(menuElm);
        if (item == "selectAll")
            doSelectAll();
        // if (e.getSource() == exitItem) {
        // destroyFrame();
        // return;
        // }

        if (item == "centrecircuit") {
            pushUndo();
            centreCircuit();
        }


        if (item == "zoomin")
            zoomCircuit(20, true);
        if (item == "zoomout")
            zoomCircuit(-20, true);
        if (item == "zoom100")
            setCircuitScale(1, true);
        if (menu == "elm" && item == "edit")
            doEdit(menuElm);
        if (item == "delete") {
            if (menu != "elm")
                menuElm = null;
            pushUndo();
            doDelete(true);
        }
        if (item == "sliders")
            doSliders(menuElm);


        if (item == "viewTemps" && menuElm != null) {
            Component component = (Component) menuElm;
            if (trackedTemperatures.contains(component)) {
                trackedTemperatures.remove(component);
            } else {
                trackedTemperatures.add((Component) component);
            }
        }


        if (menu == "circuits" && item.indexOf("setup ") == 0) {
            pushUndo();
            int sp = item.indexOf(' ', 6);
            readSetupFile(item.substring(6, sp), item.substring(sp + 1));
        }
        if (item == "newblankcircuit") {
            pushUndo();
            readSetupFile("blank.txt", "Blank Circuit");
        }

        // if (ac.indexOf("setup ") == 0) {
        // pushUndo();
        // readSetupFile(ac.substring(6),
        // ((MenuItem) e.getSource()).getLabel());
        // }

        // IES: Moved from itemStateChanged()
        if (menu == "main") {
            if (contextPanel != null)
                contextPanel.hide();
            // MenuItem mmi = (MenuItem) mi;
            // int prevMouseMode = mouseMode;
            setMouseMode(MODE_ADD_ELM);
            String s = item;
            if (s.length() > 0)
                mouseModeStr = s;
            if (s.compareTo("DragAll") == 0)
                setMouseMode(MODE_DRAG_ALL);
            else if (s.compareTo("DragRow") == 0)
                setMouseMode(MODE_DRAG_ROW);
            else if (s.compareTo("DragColumn") == 0)
                setMouseMode(MODE_DRAG_COLUMN);
            else if (s.compareTo("DragSelected") == 0)
                setMouseMode(MODE_DRAG_SELECTED);
            else if (s.compareTo("DragPost") == 0)
                setMouseMode(MODE_DRAG_POST);
            else if (s.compareTo("Select") == 0)
                setMouseMode(MODE_SELECT);
            // else if (s.length() > 0) {
            // try {
            // addingClass = Class.forName(s);
            // } catch (Exception ee) {
            // ee.printStackTrace();
            // }
            // }
            // else
            // setMouseMode(prevMouseMode);
            tempMouseMode = mouseMode;
        }
        if (item == "fullscreen") {
            if (!Graphics.isFullScreen)
                Graphics.viewFullScreen();
            else
                Graphics.exitFullScreen();
            centreCircuit();
        }

        repaint();
    }


    void doEdit(Editable eable) {
        clearSelection();
        pushUndo();
        if (editDialog != null) {
            // requestFocus();
            editDialog.setVisible(false);
            editDialog = null;
        }

        editDialog = new EditDialog(eable, this);
        editDialog.show();
    }

    void doSliders(CircuitElm ce) {
        clearSelection();
        pushUndo();
        dialogShowing = new SliderDialog(ce, this);
        dialogShowing.show();
    }

    void doExportAsUrl() {
        String dump = dumpCircuit();
        dialogShowing = new ExportAsUrlDialog(dump);
        dialogShowing.show();
    }

    void doExportAsText() {
        // String dump = dumpCircuit();
        String dump = dumpThermalCircuit(); // Katni
        dialogShowing = new ExportAsTextDialog(this, dump);
        dialogShowing.show();
    }

    void doExportAsImage() {
        dialogShowing = new ExportAsImageDialog(CAC_IMAGE);
        dialogShowing.show();
    }

    void doCreateSubcircuit() {
        EditCompositeModelDialog dlg = new EditCompositeModelDialog();
        if (!dlg.createModel())
            return;
        dlg.createDialog();
        dialogShowing = dlg;
        dialogShowing.show();
    }

    void doExportAsLocalFile() {
        // String dump = dumpCircuit();
        String dump = dumpThermalCircuit(); // Katni
        dialogShowing = new ExportAsLocalFileDialog(dump);
        dialogShowing.show();
    }

    public void importCircuitFromText(String circuitText, boolean subcircuitsOnly) {
        int flags = subcircuitsOnly ? (CirSim.RC_SUBCIRCUITS | CirSim.RC_RETAIN) : 0;
        if (circuitText != null) {
            readCircuit(circuitText, flags);
            allowSave(false);
        }
    }

    String dumpOptions() {
        int f = (dotsCheckItem.getState()) ? 1 : 0;
        f |= (smallGridCheckItem.getState()) ? 2 : 0;
        f |= (voltsCheckItem.getState()) ? 0 : 4;
        f |= (powerCheckItem.getState()) ? 8 : 0;
        f |= (showValuesCheckItem.getState()) ? 0 : 16;
        // 32 = linear scale in afilter
        f |= adjustTimeStep ? 64 : 0;
        String dump = "$ " + f + " " + maxTimeStep + " " + getIterCount() + " " + " " + minTimeStep + "\n";
        return dump;
    }

    String dumpCircuit() {
        int i;
        CustomLogicModel.clearDumpedFlags();
        CustomCompositeModel.clearDumpedFlags();
        DiodeModel.clearDumpedFlags();
        TransistorModel.clearDumpedFlags();

        String dump = dumpOptions();

        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            String m = ce.dumpModel();
            if (m != null && !m.isEmpty())
                dump += m + "\n";
            dump += ce.dump() + "\n";
        }

        for (i = 0; i != adjustables.size(); i++) {
            Adjustable adj = adjustables.get(i);
            dump += "38 " + adj.dump() + "\n";
        }
        if (hintType != -1)
            dump += "h " + hintType + " " + hintItem1 + " " + hintItem2 + "\n";
        return dump;
    }

    // ********************************************** Katni
    // ********************************************
    String dumpThermalCircuit() {
        String dump =
                "Data directory: " + "/materials\n" +
                        "Time step dt: " + theSim.dt + "\n" +
                        "Inner loop tolerance: " + "\n" +
                        "Boundary condition on the left: " + "\n" +
                        "Boundary condition on the right: " + "\n" +
                        "Temperature on the left: " + heatCircuit.temp_left + " K\n" +
                        "Convection coefficient on the left: " + " W/(m^2K)\n" +
                        "Temperature on the right: " + heatCircuit.temp_right + " K\n" +
                        "Convection coefficient on the right: " + " W/(m^2K)\n";

        dump += "\nThermal control elements: \n";
        for (TCE tce : simTCEs) {
            dump += "\nTCE: " + tce.name + "\n";
            dump += "Components: \n";
            for (Component component : tce.components) {
                dump += "Component name: " + component.name + "\n" +
                        "Component index: " + component.index + "\n" +
                        "Material: " + component.material.materialName + "\n" +
                        "Number of control volumes:  " + component.num_cvs + "\n" +
                        "Control volume length: " + component.cvs.get(0).dx + " m\n" +
                        "Constant density:" + "kg/m^3\n" +
                        "Constant specific heat:" + "J/(kgK)\n" +
                        "Constant thermal conductivity: " + " W/(mK)\n" +
                        "Left contact resistance: " + "mK/W\n" +
                        "Right contact resistance: " + "mK/W\n" +
                        "Generated heat: " + "W/m^2\n\n";
            }
        }


        for (int i = 0; i < temperatures.size(); i++) {
            dump += ModelMethods.printTemps(times.get(i), temperatures.get(i));
            dump += "\n";
        }
        dump += "\nFluxes:\n";
        heatCircuit.calculateHeatFluxes();
        for (int i = 0; i < heatCircuit.num_cvs; i++) {
            dump += String.valueOf(heatCircuit.fluxes[i]) + "\t";
        }
        return dump;
    }
    // *************************************************************************************************

    void getSetupList(final boolean openDefault) {

        String url;
        url = GWT.getModuleBaseURL() + "setuplist.txt" + "?v=" + random.nextInt();
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    Window.alert(Locale.LS("Can't load circuit list!"));
                    GWT.log("File Error Response", exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    // processing goes here
                    if (response.getStatusCode() == Response.SC_OK) {
                        String text = response.getText();
                        processSetupList(text.getBytes(), openDefault);
                        // end or processing
                    } else {
                        Window.alert(Locale.LS("Can't load circuit list!"));
                        GWT.log("Bad file server response:" + response.getStatusText());
                    }
                }
            });
        } catch (RequestException e) {
            GWT.log("failed file reading", e);
        }
    }

    void processSetupList(byte b[], final boolean openDefault) {
        int len = b.length;
        MenuBar currentMenuBar;
        MenuBar stack[] = new MenuBar[6];
        int stackptr = 0;
        currentMenuBar = new MenuBar(true);
        currentMenuBar.setAutoOpen(true);
        menuBar.addItem(Locale.LS("Circuits"), currentMenuBar);
        stack[stackptr++] = currentMenuBar;
        int p;
        for (p = 0; p < len; ) {
            int l;
            for (l = 0; l != len - p; l++)
                if (b[l + p] == '\n' || b[l + p] == '\r') {
                    l++;
                    break;
                }
            String line = new String(b, p, l - 1);
            if (line.charAt(0) == '#')
                ;
            else if (line.charAt(0) == '+') {
                // MenuBar n = new Menu(line.substring(1));
                MenuBar n = new MenuBar(true);
                n.setAutoOpen(true);
                currentMenuBar.addItem(Locale.LS(line.substring(1)), n);
                currentMenuBar = stack[stackptr++] = n;
            } else if (line.charAt(0) == '-') {
                currentMenuBar = stack[--stackptr - 1];
            } else {
                int i = line.indexOf(' ');
                if (i > 0) {
                    String title = Locale.LS(line.substring(i + 1));
                    boolean first = false;
                    if (line.charAt(0) == '>')
                        first = true;
                    String file = line.substring(first ? 1 : 0, i);
                    currentMenuBar.addItem(new MenuItem(title, new MyCommand("circuits", "setup " + file + " " + title)));
                    if (file.equals(startCircuit) && startLabel == null) {
                        startLabel = title;
                        titleLabel.setText(title);
                    }
                    if (first && startCircuit == null) {
                        startCircuit = file;
                        startLabel = title;
                        if (openDefault && stopMessage == null)
                            readSetupFile(startCircuit, startLabel);
                    }
                }
            }
            p += l;
        }
    }

    void readCircuit(String text, int flags) {
        readCircuit(text.getBytes(), flags);
        if ((flags & RC_KEEP_TITLE) == 0)
            titleLabel.setText(null);
    }

    void readCircuit(String text) {
        readCircuit(text.getBytes(), 0);
        titleLabel.setText(null);
    }

    void setCircuitTitle(String s) {
        if (s != null)
            titleLabel.setText(s);
    }

    void readSetupFile(String str, String title) {
        System.out.println(str);
        // TODO: Maybe think about some better approach to cache management!
        String url = GWT.getModuleBaseURL() + "circuits/" + str + "?v=" + random.nextInt();
        loadFileFromURL(url);
        if (title != null)
            titleLabel.setText(title);
        unsavedChanges = false;

    }

    void preloadMaterials() {

    }

    void runTestSuite() {
        if (testTimer == null) {
            testCounter = 0;
            setSimRunning(true);
            //materials to use
            for (Material m : materialHashMap.values()) {
                if (!m.isLoaded())
                    m.readFiles();
            }
            //FIXME: An error can occur if the timer starts before the files have been read!
            testTimer = new Timer() {
                public void run() {
                    switch (testCounter) {
                        //both sides same
                        case 0:
                            temp_left = 290;
                            temp_right = 290;
                            loadRandomComponents("100001-Inox", "100001-Inox", "100001-Inox");
                            testCounter++;
                            break;

                        //left side higher
                        case 1:
                            temp_left = 291;
                            temp_right = 290;
                            loadRandomComponents("100001-Inox", "100001-Inox", "100001-Inox");
                            testCounter++;
                            break;

                        //right side higher
                        case 2:
                            temp_left = 290;
                            temp_right = 295.124;
                            loadRandomComponents("100001-Inox", "100001-Inox", "100001-Inox");
                            testCounter++;
                            break;
                        case 3:
                            temp_left = 350;
                            temp_right = 290;
                            loadRandomComponents("100001-Inox", "500001-Si", "100001-Inox");
                            testCounter++;
                            break;
                        case 4:
                            temp_left = 291;
                            temp_right = 290;
                            loadRandomComponents("500001-Si", "100001-Inox", "500001-Si");
                            testCounter++;
                            break;
                        default:
                            testTimer.cancel();
                            testTimer = null;
                            testCounter = 0;
                            setSimRunning(false);
                            break;
                    }
                }


            };
            testTimer.scheduleRepeating(1000); // 5000 milliseconds = 5 seconds
        }
    }

    void loadRandomComponents(String material0, String material1, String material2) {
        ArrayList<Integer> points = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            points.add(snapGrid((int) (Math.random() * canvasWidth)));

        }
        Collections.sort(points);
        GWT.log(materialHashMap.get("100001-Inox").cp.get(0).get(0) + "");
        String text = "$ 1 5.0E-6 10 50 5.0\n" +
                "520 " + points.get(0) + " 192 " + points.get(1) + " 192 0 0 " + material0 + " 4 " + Math.abs(points.get(0) - points.get(1)) + "\n" +
                "520 " + points.get(1) + " 192 " + points.get(2) + " 192 0 1 " + material1 + "  8 " + Math.abs(points.get(1) - points.get(2)) + "\n" +
                "520 " + points.get(2) + " 192 " + points.get(3) + " 192 0 2 " + material2 + " 10 " + Math.abs(points.get(2) - points.get(3)) + "\n";
        readCircuit(text, RC_KEEP_TITLE);
        allowSave(false);
        unsavedChanges = false;
        resetAction();
    }

    void loadFileFromURL(String url) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);

        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    Window.alert(Locale.LS("Can't load circuit!"));
                    GWT.log("File Error Response", exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        String text = response.getText();
                        readCircuit(text, RC_KEEP_TITLE);
                        allowSave(false);
                        unsavedChanges = false;
                    } else {
                        Window.alert(Locale.LS("Can't load circuit!"));
                        GWT.log("Bad file server response:" + response.getStatusText());
                    }
                }
            });
        } catch (RequestException e) {
            GWT.log("failed file reading", e);
        }

    }

    // ************************ Katni *****************************
    // void readThermalFile(String str) {
    // System.out.println(str);
    // String url=GWT.getModuleBaseURL()+"circuits/inox_k.txt";// +
    // "?v="+random.nextInt(); ;
    // loadThermalFileFromURL(url);
    // }

    // void loadThermalFileFromURL(String url) {
    // RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);

    // try {
    // requestBuilder.sendRequest(null, new RequestCallback() {
    // public void onError(Request request, Throwable exception) {
    // Window.alert(Locale.LS("Can't load data!"));
    // GWT.log("File Error Response", exception);
    // }

    // public void onResponseReceived(Request request, Response response) {
    // if (response.getStatusCode()==Response.SC_OK) {
    // String text = response.getText();
    // readThermalData(text.getBytes(), RC_KEEP_TITLE);
    // allowSave(false);
    // unsavedChanges = false;
    // }
    // else {
    // Window.alert(Locale.LS("Can't load data!"));
    // GWT.log("Bad file server response:"+response.getStatusText() );
    // }
    // }
    // });
    // } catch (RequestException e) {
    // GWT.log("failed file reading", e);
    // }

    // }
    // ************************************************************

    static final int RC_RETAIN = 1;
    static final int RC_NO_CENTER = 2;
    static final int RC_SUBCIRCUITS = 4;
    static final int RC_KEEP_TITLE = 8;

    void readCircuit(byte b[], int flags) {
        int i;
        int len = b.length;
        if ((flags & RC_RETAIN) == 0) {
            clearMouseElm();
            for (i = 0; i != elmList.size(); i++) {
                CircuitElm ce = getElm(i);
                ce.delete();
            }
            t = timeStepAccum = 0;
            elmList.removeAllElements();
            simComponents = new Vector<Component>();
            trackedTemperatures = new ArrayList<Component>();
            hintType = -1;
            maxTimeStep = 5e-6;
            minTimeStep = 50e-12;
            dotsCheckItem.setState(false);
            smallGridCheckItem.setState(false);
            powerCheckItem.setState(false);
            voltsCheckItem.setState(true);
            showValuesCheckItem.setState(true);
            setGrid();
            speedBar.setValue(57); // 57
            CircuitElm.voltageRange = 5;
            lastIterTime = 0;


        }
        boolean subs = (flags & RC_SUBCIRCUITS) != 0;
        // cv.repaint();
        int p;
        for (p = 0; p < len; ) {
            int l;
            int linelen = len - p; // IES - changed to allow the last line to not end with a delim.
            for (l = 0; l != len - p; l++)
                if (b[l + p] == '\n' || b[l + p] == '\r') {
                    linelen = l++;
                    if (l + p < b.length && b[l + p] == '\n')
                        l++;
                    break;
                }
            String line = new String(b, p, linelen);
            StringTokenizer st = new StringTokenizer(line, " +\t\n\r\f");
            while (st.hasMoreTokens()) {
                String type = st.nextToken();
                int tint = type.charAt(0);
                try {
                    if (subs && tint != '.')
                        continue;
                    if (tint == 'h') {
                        readHint(st);
                        break;
                    }
                    if (tint == '$') {
                        readOptions(st, flags);
                        break;
                    }
                    if (tint == '!') {
                        CustomLogicModel.undumpModel(st);
                        break;
                    }
                    if (tint == '%' || tint == '?' || tint == 'B') {
                        // ignore afilter-specific stuff
                        break;
                    }
                    // do not add new symbols here without testing export as link

                    // if first character is a digit then parse the type as a number
                    if (tint >= '0' && tint <= '9')
                        tint = Integer.parseInt(type);

                    if (tint == 34) {
                        DiodeModel.undumpModel(st);
                        break;
                    }
                    if (tint == 32) {
                        TransistorModel.undumpModel(st);
                        break;
                    }
                    if (tint == 38) {
                        Adjustable adj = new Adjustable(st, this);
                        if (adj.elm != null)
                            adjustables.add(adj);
                        break;
                    }
                    if (tint == '.') {
                        CustomCompositeModel.undumpModel(st);
                        break;
                    }
                    int x1 = Integer.parseInt(st.nextToken());
                    int y1 = Integer.parseInt(st.nextToken());
                    int x2 = Integer.parseInt(st.nextToken());
                    int y2 = Integer.parseInt(st.nextToken());
                    int f = Integer.parseInt(st.nextToken());
                    CircuitElm newce = createCe(tint, x1, y1, x2, y2, f, st);

                    if (newce == null) {
                        System.out.println("unrecognized dump type: " + type);
                        break;
                    }
                    /*
                     * debug code to check if allocNodes() is called in constructor. It gets called
                     * in setPoints() but that doesn't get called for subcircuits. double vv[] =
                     * newce.volts; int vc = newce.getPostCount() + newce.getInternalNodeCount(); if
                     * (vv.length != vc) console("allocnodes not called! " + tint);
                     */
                    newce.setPoints();
                    elmList.addElement(newce);

                } catch (Exception ee) {
                    ee.printStackTrace();
                    console("exception while undumping " + ee);
                    break;
                }
                break;
            }
            p += l;

        }
        if ((flags & RC_RETAIN) == 0) {
            enableItems();
            // create sliders as needed
            for (i = 0; i < adjustables.size(); i++) {
                if (!adjustables.get(i).createSlider(this))
                    adjustables.remove(i--);
            }
        }
        // if (!retain)
        // handleResize(); // for scopes
        needAnalyze();
        if ((flags & RC_NO_CENTER) == 0)
            centreCircuit();
        if ((flags & RC_SUBCIRCUITS) != 0)
            updateModels();

    }

    // ***************************************************** Katni
    // ***********************************
    // void readThermalData(byte b[], int flags) {
    // int i;
    // int len = b.length;
    // boolean subs = (flags & RC_SUBCIRCUITS) != 0;
    // //cv.repaint();
    // int p;
    // int linePos = 0;
    // for (p = 0; p < len; ) {
    // int l;
    // int linelen = len-p; // IES - changed to allow the last line to not end with
    // a delim.
    // for (l = 0; l != len-p; l++)
    // if (b[l+p] == '\n' || b[l+p] == '\r') {
    // linelen = l++;
    // if (l+p < b.length && b[l+p] == '\n')
    // l++;
    // break;
    // }
    // String line = new String(b, p, linelen);
    // StringTokenizer st = new StringTokenizer(line, " +\t\n\r\f");
    // while (st.hasMoreTokens()) {
    // // String type = st.nextToken();
    // // int tint = type.charAt(0);
    // try {
    // double x1 = new Double(st.nextToken()).doubleValue();
    // double y1 = new Double(st.nextToken()).doubleValue();
    // tempsFromFile[linePos] = x1;
    // kValuesFromFile[linePos] = y1;
    // } catch (Exception ee) {
    // ee.printStackTrace();
    // console("exception while undumping " + ee);
    // break;
    // }
    // break;
    // }
    // p += l;
    // linePos++;

    // }
    // String T = "Temperatures: " + String.valueOf(tempsFromFile[0]);
    // T += ", " + String.valueOf(tempsFromFile[1]) + ", " +
    // String.valueOf(tempsFromFile[2]) + "\n";
    // T += "Conductivities: " + String.valueOf(kValuesFromFile[0]) + ", " +
    // String.valueOf(kValuesFromFile[1]) + ", " +
    // String.valueOf(kValuesFromFile[2]) + "\n";
    // //Window.alert(T);
    // }
    // ***********************************************************************************************

    // delete sliders for an element
    void deleteSliders(CircuitElm elm) {
        int i;
        if (adjustables == null)
            return;
        for (i = adjustables.size() - 1; i >= 0; i--) {
            Adjustable adj = adjustables.get(i);
            if (adj.elm == elm) {
                adj.deleteSlider(this);
                adjustables.remove(i);
            }
        }
    }

    void readHint(StringTokenizer st) {
        hintType = Integer.parseInt(st.nextToken());
        hintItem1 = Integer.parseInt(st.nextToken());
        hintItem2 = Integer.parseInt(st.nextToken());
    }

    void readOptions(StringTokenizer st, int importFlags) {
        int flags = Integer.parseInt(st.nextToken());

        if ((importFlags & RC_RETAIN) != 0) {
            // need to set small grid if pasted circuit uses it
            if ((flags & 2) != 0)
                smallGridCheckItem.setState(true);
            return;
        }

        dotsCheckItem.setState((flags & 1) != 0);
        smallGridCheckItem.setState((flags & 2) != 0);
        voltsCheckItem.setState((flags & 4) == 0);
        powerCheckItem.setState((flags & 8) == 8);
        showValuesCheckItem.setState((flags & 16) == 0);
        adjustTimeStep = (flags & 64) != 0;
        maxTimeStep = timeStep = Double.parseDouble(st.nextToken());
        double sp = Double.parseDouble(st.nextToken());
        int sp2 = (int) (Math.log(10 * sp) * 24 + 61.5);
        // int sp2 = (int) (Math.log(sp)*24+1.5);
        speedBar.setValue(57);
        CircuitElm.voltageRange = Double.parseDouble(st.nextToken());

        try {
            minTimeStep = Double.parseDouble(st.nextToken());
        } catch (Exception e) {
        }
        setGrid();
    }

    int snapGrid(int x) {
        return (x + gridRound) & gridMask;
    }

    boolean doSwitch(int x, int y) {
        return false;
        /*if (mouseElm == null || !(mouseElm instanceof SwitchElm))
            return false;
        SwitchElm se = (SwitchElm) mouseElm;
        if (!se.getSwitchRect().contains(x, y))
            return false;
        se.toggle();
        if (se.momentary)
            heldSwitchElm = se;

        return true;*/
    }

    int locateElm(CircuitElm elm) {
        int i;
        for (i = 0; i != elmList.size(); i++)
            if (elm == elmList.elementAt(i))
                return i;
        return -1;
    }

    public void mouseDragged(MouseMoveEvent e) {
        // ignore right mouse button with no modifiers (needed on PC)
        if (e.getNativeButton() == NativeEvent.BUTTON_RIGHT) {
            if (!(e.isMetaKeyDown() || e.isShiftKeyDown() || e.isControlKeyDown() || e.isAltKeyDown()))
                return;
        }

        if (tempMouseMode == MODE_DRAG_SPLITTER) {
            dragSplitter(e.getX(), e.getY());
            return;
        }
        int gx = inverseTransformX(e.getX());
        int gy = inverseTransformY(e.getY());
        if (!circuitArea.contains(e.getX(), e.getY()))
            return;
        boolean changed = false;
        if (dragElm != null)
            dragElm.drag(gx, gy);
        boolean success = true;
        switch (tempMouseMode) {
            case MODE_DRAG_ALL:
                dragAll(e.getX(), e.getY());
                break;
            case MODE_DRAG_ROW:
                dragRow(snapGrid(gx), snapGrid(gy));
                changed = true;
                break;
            case MODE_DRAG_COLUMN:
                dragColumn(snapGrid(gx), snapGrid(gy));
                changed = true;
                break;
            case MODE_DRAG_POST:
                if (mouseElm != null) {
                    dragPost(snapGrid(gx), snapGrid(gy), e.isShiftKeyDown());
                    if (mouseElm instanceof Component)
                        ((Component) mouseElm).calculateLength();
                    changed = true;
                }
                break;
            case MODE_SELECT:
                if (mouseElm == null)
                    selectArea(gx, gy, e.isShiftKeyDown());
                else if (!noEditCheckItem.getState()) {
                    // wait short delay before dragging. This is to fix problem where switches were
                    // accidentally getting
                    // dragged when tapped on mobile devices
                    if (System.currentTimeMillis() - mouseDownTime < 150)
                        return;

                    tempMouseMode = MODE_DRAG_SELECTED;
                    changed = success = dragSelected(gx, gy);
                }
                break;
            case MODE_DRAG_SELECTED:
                changed = success = dragSelected(gx, gy);
                break;

        }
        dragging = true;
        if (success) {
            dragScreenX = e.getX();
            dragScreenY = e.getY();
            // console("setting dragGridx in mousedragged");
            dragGridX = inverseTransformX(dragScreenX);
            dragGridY = inverseTransformY(dragScreenY);
            if (!(tempMouseMode == MODE_DRAG_SELECTED && onlyGraphicsElmsSelected())) {
                dragGridX = snapGrid(dragGridX);
                dragGridY = snapGrid(dragGridY);
            }
        }
        if (changed)
            writeRecoveryToStorage();
        repaint();
    }

    void dragSplitter(int x, int y) {
        double h = (double) canvasHeight;
        if (h < 1)
            h = 1;
        scopeHeightFraction = 1.0 - (((double) y) / h);
        if (scopeHeightFraction < 0.1)
            scopeHeightFraction = 0.1;
        if (scopeHeightFraction > 0.9)
            scopeHeightFraction = 0.9;
        setCircuitArea();
        repaint();
    }

    void dragAll(int x, int y) {
        int dx = x - dragScreenX;
        int dy = y - dragScreenY;
        if (dx == 0 && dy == 0)
            return;
        transform[4] += dx;
        transform[5] += dy;
        dragScreenX = x;
        dragScreenY = y;
    }

    void dragRow(int x, int y) {
        int dy = y - dragGridY;
        if (dy == 0)
            return;
        int i;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.y == dragGridY)
                ce.movePoint(0, 0, dy);
            if (ce.y2 == dragGridY)
                ce.movePoint(1, 0, dy);
        }
        removeZeroLengthElements();
    }

    void dragColumn(int x, int y) {
        int dx = x - dragGridX;
        if (dx == 0)
            return;
        int i;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.x == dragGridX)
                ce.movePoint(0, dx, 0);
            if (ce.x2 == dragGridX)
                ce.movePoint(1, dx, 0);
        }
        removeZeroLengthElements();
    }

    boolean onlyGraphicsElmsSelected() {
        if (mouseElm != null && !(mouseElm instanceof GraphicElm))
            return false;
        int i;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.isSelected() && !(ce instanceof GraphicElm))
                return false;
        }
        return true;
    }

    boolean dragSelected(int x, int y) {
        boolean me = false;
        int i;
        if (mouseElm != null && !mouseElm.isSelected())
            mouseElm.setSelected(me = true);

        if (!onlyGraphicsElmsSelected()) {
            // console("Snapping x and y");
            x = snapGrid(x);
            y = snapGrid(y);
        }

        int dx = x - dragGridX;
        // console("dx="+dx+"dragGridx="+dragGridX);
        int dy = y - dragGridY;
        if (dx == 0 && dy == 0) {
            // don't leave mouseElm selected if we selected it above
            if (me)
                mouseElm.setSelected(false);
            return false;
        }
        boolean allowed = true;

        // check if moves are allowed
        for (i = 0; allowed && i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.isSelected() && !ce.allowMove(dx, dy))
                allowed = false;
        }

        if (allowed) {
            for (i = 0; i != elmList.size(); i++) {
                CircuitElm ce = getElm(i);
                if (ce.isSelected())
                    ce.move(dx, dy);
            }
            needAnalyze();
        }

        // don't leave mouseElm selected if we selected it above
        if (me)
            mouseElm.setSelected(false);

        return allowed;
    }

    void dragPost(int x, int y, boolean all) {
        if (draggingPost == -1) {
            draggingPost = (Graphics.distanceSq(mouseElm.x, mouseElm.y, x, y) > Graphics.distanceSq(mouseElm.x2,
                    mouseElm.y2, x, y)) ? 1 : 0;
        }
        int dx = x - dragGridX;
        int dy = y - dragGridY;
        if (dx == 0 && dy == 0)
            return;

        if (all) {
            // go through all elms
            int i;
            for (i = 0; i != elmList.size(); i++) {
                CircuitElm e = elmList.get(i);

                // which post do we move?
                int p = 0;
                if (e.x == dragGridX && e.y == dragGridY)
                    p = 0;
                else if (e.x2 == dragGridX && e.y2 == dragGridY)
                    p = 1;
                else
                    continue;
                e.movePoint(p, dx, dy);
            }
        } else
            mouseElm.movePoint(draggingPost, dx, dy);
        needAnalyze();
    }

    void doFlip() {
        menuElm.flipPosts();
        needAnalyze();
    }

    void doSplit(CircuitElm ce) {
        int x = snapGrid(inverseTransformX(menuX));
        int y = snapGrid(inverseTransformY(menuY));
        if (ce == null || !(ce instanceof WireElm))
            return;
        if (ce.x == ce.x2)
            x = ce.x;
        else
            y = ce.y;

        // don't create zero-length wire
        if (x == ce.x && y == ce.y || x == ce.x2 && y == ce.y2)
            return;

        WireElm newWire = new WireElm(x, y);
        newWire.drag(ce.x2, ce.y2);
        ce.drag(x, y);
        elmList.addElement(newWire);
        needAnalyze();
    }

    void selectArea(int x, int y, boolean add) {
        int x1 = min(x, initDragGridX);
        int x2 = max(x, initDragGridX);
        int y1 = min(y, initDragGridY);
        int y2 = max(y, initDragGridY);
        selectedArea = new Rectangle(x1, y1, x2 - x1, y2 - y1);
        int i;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            ce.selectRect(selectedArea, add);
        }
    }

    // void setSelectedElm(CircuitElm cs) {
    // int i;
    // for (i = 0; i != elmList.size(); i++) {
    // CircuitElm ce = getElm(i);
    // ce.setSelected(ce == cs);
    // }
    // mouseElm = cs;
    // }

    void setMouseElm(CircuitElm ce) {
        if (ce != mouseElm) {
            if (mouseElm != null)
                mouseElm.setMouseElm(false);
            if (ce != null)
                ce.setMouseElm(true);
            mouseElm = ce;
            int i;
            for (i = 0; i < adjustables.size(); i++)
                adjustables.get(i).setMouseElm(ce);
        }
    }

    void removeZeroLengthElements() {
        int i;
        boolean changed = false;
        for (i = elmList.size() - 1; i >= 0; i--) {
            CircuitElm ce = getElm(i);
            if (ce.x == ce.x2 && ce.y == ce.y2) {
                elmList.remove(ce);
                simComponents.remove((Component) ce);
                trackedTemperatures.remove((Component) ce);
                Collections.sort(trackedTemperatures);
                ce.delete();
                changed = true;
            }
        }
        needAnalyze();
    }

    boolean mouseIsOverSplitter(int x, int y) {
        boolean isOverSplitter;

        isOverSplitter = ((x >= 0) && (x < circuitArea.width) && (y >= circuitArea.height - 5)
                && (y < circuitArea.height));
        if (isOverSplitter != mouseWasOverSplitter) {
            if (isOverSplitter)
                setCursorStyle("cursorSplitter");
            else
                setMouseMode(mouseMode);
        }
        mouseWasOverSplitter = isOverSplitter;
        return isOverSplitter;
    }

    public void onMouseMove(MouseMoveEvent e) {
        e.preventDefault();
        mouseCursorX = e.getX();
        mouseCursorY = e.getY();
        if (mouseDragging) {
            mouseDragged(e);
            return;
        }
        mouseSelect(e);
        scopeMenuSelected = -1;
    }

    // convert screen coordinates to grid coordinates by inverting circuit transform
    int inverseTransformX(double x) {
        return (int) ((x - transform[4]) / transform[0]);
    }

    int inverseTransformY(double y) {
        return (int) ((y - transform[5]) / transform[3]);
    }

    // convert grid coordinates to screen coordinates
    int transformX(double x) {
        return (int) ((x * transform[0]) + transform[4]);
    }

    int transformY(double y) {
        return (int) ((y * transform[3]) + transform[5]);
    }

    // need to break this out into a separate routine to handle selection,
    // since we don't get mouse move events on mobile
    public void mouseSelect(MouseEvent<?> e) {
        // The following is in the original, but seems not to work/be needed for GWT
        // if (e.getNativeButton()==NativeEvent.BUTTON_LEFT)
        // return;
        CircuitElm newMouseElm = null;
        mouseCursorX = e.getX();
        mouseCursorY = e.getY();
        int sx = e.getX();
        int sy = e.getY();
        int gx = inverseTransformX(sx);
        int gy = inverseTransformY(sy);
        // console("Settingd draggridx in mouseEvent");
        dragGridX = snapGrid(gx);
        dragGridY = snapGrid(gy);
        dragScreenX = sx;
        dragScreenY = sy;
        draggingPost = -1;
        int i;
        // CircuitElm origMouse = mouseElm;

        mousePost = -1;
        plotXElm = plotYElm = null;

        if (mouseIsOverSplitter(sx, sy)) {
            setMouseElm(null);
            return;
        }

        if (circuitArea.contains(sx, sy)) {
            if (mouseElm != null && (mouseElm.getHandleGrabbedClose(gx, gy, POSTGRABSQ, MINPOSTGRABSIZE) >= 0)) {
                newMouseElm = mouseElm;
            } else {
                int bestDist = 100000000;
                int bestArea = 100000000;
                for (i = 0; i != elmList.size(); i++) {
                    CircuitElm ce = getElm(i);
                    if (ce.boundingBox.contains(gx, gy)) {
                        int j;
                        int area = ce.boundingBox.width * ce.boundingBox.height;
                        int jn = ce.getPostCount();
                        if (jn > 2)
                            jn = 2;
                        for (j = 0; j != jn; j++) {
                            Point pt = ce.getPost(j);
                            int dist = Graphics.distanceSq(gx, gy, pt.x, pt.y);

                            // if multiple elements have overlapping bounding boxes,
                            // we prefer selecting elements that have posts close
                            // to the mouse pointer and that have a small bounding
                            // box area.
                            if (dist <= bestDist && area <= bestArea) {
                                bestDist = dist;
                                bestArea = area;
                                newMouseElm = ce;
                            }
                        }
                        // prefer selecting elements that have small bounding box area (for
                        // elements with no posts)
                        if (ce.getPostCount() == 0 && area <= bestArea) {
                            newMouseElm = ce;
                            bestArea = area;
                        }
                    }
                } // for
            }
        }
        scopeSelected = -1;
        if (newMouseElm == null) {
            // // the mouse pointer was not in any of the bounding boxes, but we
            // // might still be close to a post
            for (i = 0; i != elmList.size(); i++) {
                CircuitElm ce = getElm(i);
                if (mouseMode == MODE_DRAG_POST) {
                    if (ce.getHandleGrabbedClose(gx, gy, POSTGRABSQ, 0) > 0) {

                        newMouseElm = ce;
                        break;
                    }
                }
                int j;
                int jn = ce.getPostCount();
                for (j = 0; j != jn; j++) {
                    Point pt = ce.getPost(j);
                    // int dist = Graphics.distanceSq(x, y, pt.x, pt.y);
                    if (Graphics.distanceSq(pt.x, pt.y, gx, gy) < 26) {
                        newMouseElm = ce;
                        mousePost = j;
                        break;
                    }
                }
            }
        } else {
            mousePost = -1;
            // look for post close to the mouse pointer
            for (i = 0; i != newMouseElm.getPostCount(); i++) {
                Point pt = newMouseElm.getPost(i);
                if (Graphics.distanceSq(pt.x, pt.y, gx, gy) < 26)
                    mousePost = i;
            }
        }
        repaint();
        setMouseElm(newMouseElm);
    }

    public void onContextMenu(ContextMenuEvent e) {
        e.preventDefault();
        if (!dialogIsShowing()) {
            menuClientX = e.getNativeEvent().getClientX();
            menuClientY = e.getNativeEvent().getClientY();
            doPopupMenu();
        }
    }

    @SuppressWarnings("deprecation")
    void doPopupMenu() {
        if (noEditCheckItem.getState() || dialogIsShowing())
            return;
        menuElm = mouseElm;
        menuScope = -1;
        menuPlot = -1;
        int x, y;
        if (mouseElm != null) {
            elmTempsMenuItem.setEnabled(mouseElm.canViewInScope());

            elmEditMenuItem.setEnabled(mouseElm.getEditInfo(0) != null);
            elmFlipMenuItem.setEnabled(mouseElm.getPostCount() == 2);
            elmSplitMenuItem.setEnabled(canSplit(mouseElm));
            elmSliderMenuItem.setEnabled(sliderItemEnabled(mouseElm));
            contextPanel = new PopupPanel(true);
            contextPanel.add(elmMenuBar);
            contextPanel.setPopupPosition(menuClientX, menuClientY);
            contextPanel.show();

        } else {
            doMainMenuChecks();
            contextPanel = new PopupPanel(true);
            contextPanel.add(mainMenuBar);
            x = Math.max(0, Math.min(menuClientX, canvasWidth - 400));
            y = Math.max(0, Math.min(menuClientY, canvasHeight - 450));
            contextPanel.setPopupPosition(x, y);
            contextPanel.show();
        }
    }

    boolean canSplit(CircuitElm ce) {
        if (!(ce instanceof WireElm))
            return false;
        WireElm we = (WireElm) ce;
        if (we.x == we.x2 || we.y == we.y2)
            return true;
        return false;
    }

    // check if the user can create sliders for this element
    boolean sliderItemEnabled(CircuitElm elm) {
        int i;


        for (i = 0; ; i++) {
            EditInfo ei = elm.getEditInfo(i);
            if (ei == null)
                return false;
            if (ei.canCreateAdjustable())
                return true;
        }
    }

    void longPress() {
        doPopupMenu();
    }

    void twoFingerTouch(int x, int y) {
        tempMouseMode = MODE_DRAG_ALL;
        dragScreenX = x;
        dragScreenY = y;
    }

    // public void mouseClicked(MouseEvent e) {
    public void onClick(ClickEvent e) {
        e.preventDefault();
        // //IES - remove inteaction
        //// if ( e.getClickCount() == 2 && !didSwitch )
        //// doEditMenu(e);
        // if (e.getNativeButton() == NativeEvent.BUTTON_LEFT) {
        // if (mouseMode == MODE_SELECT || mouseMode == MODE_DRAG_SELECTED)
        // clearSelection();
        // }
        if ((e.getNativeButton() == NativeEvent.BUTTON_MIDDLE))
            scrollValues(e.getNativeEvent().getClientX(), e.getNativeEvent().getClientY(), 0);
    }

    public void onDoubleClick(DoubleClickEvent e) {
        e.preventDefault();
        // if (!didSwitch && mouseElm != null)
        if (mouseElm != null && !noEditCheckItem.getState())
            doEdit(mouseElm);
    }

    // public void mouseEntered(MouseEvent e) {
    // }

    public void onMouseOut(MouseOutEvent e) {
        mouseCursorX = -1;
    }

    void clearMouseElm() {
        scopeSelected = -1;
        setMouseElm(null);
        plotXElm = plotYElm = null;
    }

    int menuClientX, menuClientY;
    int menuX, menuY;

    public void onMouseDown(MouseDownEvent e) {
        // public void mousePressed(MouseEvent e) {
        e.preventDefault();

        // make sure canvas has focus, not stop button or something else, so all
        // shortcuts work
        cv.setFocus(true);

        stopElm = null; // if stopped, allow user to select other elements to fix circuit
        menuX = menuClientX = e.getX();
        menuY = menuClientY = e.getY();
        mouseDownTime = System.currentTimeMillis();

        // maybe someone did copy in another window? should really do this when
        // window receives focus
        enablePaste();

        if (e.getNativeButton() != NativeEvent.BUTTON_LEFT && e.getNativeButton() != NativeEvent.BUTTON_MIDDLE)
            return;

        // set mouseElm in case we are on mobile
        mouseSelect(e);

        mouseDragging = true;
        didSwitch = false;

        if (mouseWasOverSplitter) {
            tempMouseMode = MODE_DRAG_SPLITTER;
            return;
        }
        if (e.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            // // left mouse
            tempMouseMode = mouseMode;
            if (e.isAltKeyDown() && e.isMetaKeyDown())
                tempMouseMode = MODE_DRAG_COLUMN;
            else if (e.isAltKeyDown() && e.isShiftKeyDown())
                tempMouseMode = MODE_DRAG_ROW;
            else if (e.isShiftKeyDown())
                tempMouseMode = MODE_SELECT;
            else if (e.isAltKeyDown())
                tempMouseMode = MODE_DRAG_ALL;
            else if (e.isControlKeyDown() || e.isMetaKeyDown())
                tempMouseMode = MODE_DRAG_POST;
        } else
            tempMouseMode = MODE_DRAG_ALL;

        if (noEditCheckItem.getState())
            tempMouseMode = MODE_SELECT;


        int gx = inverseTransformX(e.getX());
        int gy = inverseTransformY(e.getY());
        if (doSwitch(gx, gy)) {
            // do this BEFORE we change the mouse mode to MODE_DRAG_POST! Or else logic
            // inputs
            // will add dots to the whole circuit when we click on them!
            didSwitch = true;
            return;
        }

        // IES - Grab resize handles in select mode if they are far enough apart and you
        // are on top of them
        if (tempMouseMode == MODE_SELECT && mouseElm != null && !noEditCheckItem.getState()
                && mouseElm.getHandleGrabbedClose(gx, gy, POSTGRABSQ, MINPOSTGRABSIZE) >= 0 && !anySelectedButMouse())
            tempMouseMode = MODE_DRAG_POST;

        if (tempMouseMode != MODE_SELECT && tempMouseMode != MODE_DRAG_SELECTED)
            clearSelection();

        pushUndo();
        initDragGridX = gx;
        initDragGridY = gy;
        dragging = true;
        if (tempMouseMode != MODE_ADD_ELM)
            return;
        //
        int x0 = snapGrid(gx);
        int y0 = snapGrid(gy);
        if (!circuitArea.contains(e.getX(), e.getY()))
            return;

        try {
            dragElm = constructElement(mouseModeStr, x0, y0);
        } catch (Exception ex) {
            debugger();
        }
    }

    static int lastSubcircuitMenuUpdate;

    // check/uncheck/enable/disable menu items as appropriate when menu bar clicked
    // on, or when
    // right mouse menu accessed. also displays shortcuts as a side effect
    void doMainMenuChecks() {
        int c = mainMenuItems.size();
        int i;
        for (i = 0; i < c; i++) {
            String s = mainMenuItemNames.get(i);
            mainMenuItems.get(i).setState(s == mouseModeStr);

            // Code to disable draw menu items when cct is not editable, but no used in this
            // version as it
            // puts up a dialog box instead (see menuPerformed).
            // if (s.length() > 3 && s.substring(s.length()-3)=="Elm")
            // mainMenuItems.get(i).setEnabled(!noEditCheckItem.getState());
        }


        // also update the subcircuit menu if necessary
        if (lastSubcircuitMenuUpdate != CustomCompositeModel.sequenceNumber)
            composeSubcircuitMenu();
    }

    public void onMouseUp(MouseUpEvent e) {
        e.preventDefault();
        mouseDragging = false;

        // click to clear selection
        if (tempMouseMode == MODE_SELECT && selectedArea == null)
            clearSelection();

        // cmd-click = split wire
        if (tempMouseMode == MODE_DRAG_POST && draggingPost == -1)
            doSplit(mouseElm);

        tempMouseMode = mouseMode;
        selectedArea = null;
        dragging = false;
        boolean circuitChanged = false;

        if (dragElm != null) {
            // if the element is zero size then don't create it
            // IES - and disable any previous selection
            if (dragElm.creationFailed()) {
                dragElm.delete();
                if (mouseMode == MODE_SELECT || mouseMode == MODE_DRAG_SELECTED)
                    clearSelection();
            } else {
                elmList.addElement(dragElm);
                dragElm.draggingDone();
                circuitChanged = true;
                writeRecoveryToStorage();
                unsavedChanges = true;
            }
            dragElm = null;
        }
        if (circuitChanged) {
            needAnalyze();
            pushUndo();
        }
        if (dragElm != null)
            dragElm.delete();
        dragElm = null;
        repaint();
    }

    public void onMouseWheel(MouseWheelEvent e) {
        e.preventDefault();

        // once we start zooming, don't allow other uses of mouse wheel for a while
        // so we don't accidentally edit a resistor value while zooming
        boolean zoomOnly = System.currentTimeMillis() < zoomTime + 1000;

        if (noEditCheckItem.getState() || !mouseWheelEditCheckItem.getState())
            zoomOnly = true;

        if (!zoomOnly)
            scrollValues(e.getNativeEvent().getClientX(), e.getNativeEvent().getClientY(), e.getDeltaY());

        if (mouseElm instanceof MouseWheelHandler && !zoomOnly)
            ((MouseWheelHandler) mouseElm).onMouseWheel(e);
        else if (!dialogIsShowing()) {
            mouseCursorX = e.getX();
            mouseCursorY = e.getY();
            zoomCircuit(-e.getDeltaY(), false);
            zoomTime = System.currentTimeMillis();
        }
        repaint();
    }

    void zoomCircuit(double dy) {
        zoomCircuit(dy, false);
    }

    void zoomCircuit(double dy, boolean menu) {
        double newScale;
        double oldScale = transform[0];
        double val = dy * .01;
        newScale = Math.max(oldScale + val, .2);
        newScale = Math.min(newScale, 2.5);
        setCircuitScale(newScale, menu);
    }

    void setCircuitScale(double newScale, boolean menu) {
        int constX = !menu ? mouseCursorX : circuitArea.width / 2;
        int constY = !menu ? mouseCursorY : circuitArea.height / 2;
        int cx = inverseTransformX(constX);
        int cy = inverseTransformY(constY);
        transform[0] = transform[3] = newScale;

        // adjust translation to keep center of screen constant
        // inverse transform = (x-t4)/t0
        transform[4] = constX - cx * newScale;
        transform[5] = constY - cy * newScale;
    }


    void scrollValues(int x, int y, int deltay) {

    }

    void enableItems() {
    }

    void setGrid() {
        gridSize = (smallGridCheckItem.getState()) ? 8 : 16;
        gridMask = ~(gridSize - 1);
        gridRound = gridSize / 2 - 1;
    }

    void pushUndo() {
        redoStack.removeAllElements();
        String s = dumpCircuit();
        if (undoStack.size() > 0 && s.compareTo(undoStack.lastElement().dump) == 0)
            return;
        undoStack.add(new UndoItem(s));
        enableUndoRedo();
        savedFlag = false;
    }

    void doUndo() {
        if (undoStack.size() == 0)
            return;
        redoStack.add(new UndoItem(dumpCircuit()));
        UndoItem ui = undoStack.remove(undoStack.size() - 1);
        loadUndoItem(ui);
        enableUndoRedo();
    }

    void doRedo() {
        if (redoStack.size() == 0)
            return;
        undoStack.add(new UndoItem(dumpCircuit()));
        UndoItem ui = redoStack.remove(redoStack.size() - 1);
        loadUndoItem(ui);
        enableUndoRedo();
    }

    void loadUndoItem(UndoItem ui) {
        readCircuit(ui.dump, RC_NO_CENTER);
        transform[0] = transform[3] = ui.scale;
        transform[4] = ui.transform4;
        transform[5] = ui.transform5;
    }

    void doRecover() {
        pushUndo();
        readCircuit(recovery);
        allowSave(false);
        recoverItem.setEnabled(false);
    }

    void enableUndoRedo() {
        redoItem.setEnabled(redoStack.size() > 0);
        undoItem.setEnabled(undoStack.size() > 0);
    }

    void setMouseMode(int mode) {
        mouseMode = mode;
        if (mode == MODE_ADD_ELM) {
            setCursorStyle("cursorCross");
        } else {
            setCursorStyle("cursorPointer");
        }
    }

    void setCursorStyle(String s) {
        if (lastCursorStyle != null)
            cv.removeStyleName(lastCursorStyle);
        cv.addStyleName(s);
        lastCursorStyle = s;
    }

    void setMenuSelection() {
        if (menuElm != null) {
            if (menuElm.selected)
                return;
            clearSelection();
            menuElm.setSelected(true);
        }
    }

    void doCut() {
        int i;
        pushUndo();
        setMenuSelection();
        clipboard = "";
        for (i = elmList.size() - 1; i >= 0; i--) {
            CircuitElm ce = getElm(i);
            // ScopeElms don't cut-paste well because their reference to a parent
            // elm by number get's messed up in the dump. For now we will just ignore them
            // until I can be bothered to come up with something better
            if (willDelete(ce)) {
                clipboard += ce.dump() + "\n";
            }
        }
        writeClipboardToStorage();
        doDelete(true);
        enablePaste();
    }

    void writeClipboardToStorage() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
            return;
        stor.setItem("circuitClipboard", clipboard);
    }

    void readClipboardFromStorage() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
            return;
        clipboard = stor.getItem("circuitClipboard");
    }

    void writeRecoveryToStorage() {
        //console("write recovery");
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
            return;
        String s = dumpCircuit();
        stor.setItem("circuitRecovery", s);
    }

    void readRecovery() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null)
            return;
        recovery = stor.getItem("circuitRecovery");
    }


    ArrayList<CircuitElm> tmp = new ArrayList<CircuitElm>();


    void doDelete(boolean pushUndoFlag) {
        int i;
        if (pushUndoFlag)
            pushUndo();
        boolean hasDeleted = false;

        for (i = elmList.size() - 1; i >= 0; i--) {
            CircuitElm ce = getElm(i);
            if (willDelete(ce)) {
                if (ce.isMouseElm())
                    setMouseElm(null);
                ce.delete();
                elmList.removeElementAt(i);
                // Katni *******************************************************************
                if (ce instanceof Component) {
                    simComponents.remove((Component) ce);
                    resetHeatSim();
                    trackedTemperatures.remove(ce);
                    Collections.sort(trackedTemperatures);
                }
                // *************************************************************************
                hasDeleted = true;
            }
        }
        if (hasDeleted) {
            needAnalyze();
            writeRecoveryToStorage();
        }
    }

    boolean willDelete(CircuitElm ce) {
        // Is this element in the list to be deleted.
        // This changes the logic from the previous version which would initially only
        // delete selected elements (which could include the mouseElm) and then delete
        // the
        // mouseElm if there were no selected elements. Not really sure this added
        // anything useful
        // to the user experience.
        //
        // BTW, the old logic could also leave mouseElm pointing to a deleted element.
        return ce.isSelected() || ce.isMouseElm();
    }

    String copyOfSelectedElms() {
        String r = dumpOptions();
        CustomLogicModel.clearDumpedFlags();
        CustomCompositeModel.clearDumpedFlags();
        DiodeModel.clearDumpedFlags();
        TransistorModel.clearDumpedFlags();
        for (int i = elmList.size() - 1; i >= 0; i--) {
            CircuitElm ce = getElm(i);
            String m = ce.dumpModel();
            if (m != null && !m.isEmpty())
                r += m + "\n";
            // See notes on do cut why we don't copy ScopeElms.
            if (ce.isSelected())
                r += ce.dump() + "\n";
        }
        return r;
    }

    void doCopy() {
        // clear selection when we're done if we're copying a single element using the
        // context menu
        boolean clearSel = (menuElm != null && !menuElm.selected);

        setMenuSelection();
        clipboard = copyOfSelectedElms();

        if (clearSel)
            clearSelection();

        writeClipboardToStorage();
        enablePaste();
    }

    void enablePaste() {
        if (clipboard == null || clipboard.length() == 0)
            readClipboardFromStorage();
        pasteItem.setEnabled(clipboard != null && clipboard.length() > 0);
    }

    void doDuplicate() {
        String s;
        setMenuSelection();
        s = copyOfSelectedElms();
        doPaste(s);
    }

    void doPaste(String dump) {
        pushUndo();
        clearSelection();
        int i;
        Rectangle oldbb = null;

        // get old bounding box
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            Rectangle bb = ce.getBoundingBox();
            if (oldbb != null)
                oldbb = oldbb.union(bb);
            else
                oldbb = bb;
        }

        // add new items
        int oldsz = elmList.size();
        int flags = RC_RETAIN;

        // don't recenter circuit if we're going to paste in place because that will
        // change the transform
        // if (mouseCursorX > 0 && circuitArea.contains(mouseCursorX, mouseCursorY))

        // in fact, don't ever recenter circuit, unless old circuit was empty
        if (oldsz > 0)
            flags |= RC_NO_CENTER;

        if (dump != null)
            readCircuit(dump, flags);
        else {
            readClipboardFromStorage();
            readCircuit(clipboard, flags);
        }

        // select new items and get their bounding box
        Rectangle newbb = null;
        for (i = oldsz; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            ce.setSelected(true);
            Rectangle bb = ce.getBoundingBox();
            if (newbb != null)
                newbb = newbb.union(bb);
            else
                newbb = bb;
        }

        if (oldbb != null && newbb != null /* && oldbb.intersects(newbb) */) {
            // find a place on the edge for new items
            int dx = 0, dy = 0;
            int spacew = circuitArea.width - oldbb.width - newbb.width;
            int spaceh = circuitArea.height - oldbb.height - newbb.height;

            if (!oldbb.intersects(newbb)) {
                // old coordinates may be really far away so move them to same origin as current
                // circuit
                dx = snapGrid(oldbb.x - newbb.x);
                dy = snapGrid(oldbb.y - newbb.y);
            }

            if (spacew > spaceh) {
                dx = snapGrid(oldbb.x + oldbb.width - newbb.x + gridSize);
            } else {
                dy = snapGrid(oldbb.y + oldbb.height - newbb.y + gridSize);
            }

            // move new items near the mouse if possible
            if (mouseCursorX > 0 && circuitArea.contains(mouseCursorX, mouseCursorY)) {
                int gx = inverseTransformX(mouseCursorX);
                int gy = inverseTransformY(mouseCursorY);
                int mdx = snapGrid(gx - (newbb.x + newbb.width / 2));
                int mdy = snapGrid(gy - (newbb.y + newbb.height / 2));
                for (i = oldsz; i != elmList.size(); i++) {
                    if (!getElm(i).allowMove(mdx, mdy))
                        break;
                }
                if (i == elmList.size()) {
                    dx = mdx;
                    dy = mdy;
                }
            }

            // move the new items
            for (i = oldsz; i != elmList.size(); i++) {
                CircuitElm ce = getElm(i);
                ce.move(dx, dy);
            }

            // center circuit
            // handleResize();
        }
        needAnalyze();
        writeRecoveryToStorage();
    }

    void clearSelection() {
        int i;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            ce.setSelected(false);
        }
    }

    void doSelectAll() {
        int i;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            ce.setSelected(true);
        }
    }

    boolean anySelectedButMouse() {
        for (int i = 0; i != elmList.size(); i++)
            if (getElm(i) != mouseElm && getElm(i).selected)
                return true;
        return false;
    }

    // public void keyPressed(KeyEvent e) {}
    // public void keyReleased(KeyEvent e) {}

    boolean dialogIsShowing() {
        if (editDialog != null && editDialog.isShowing())
            return true;
        if (customLogicEditDialog != null && customLogicEditDialog.isShowing())
            return true;
        if (diodeModelEditDialog != null && diodeModelEditDialog.isShowing())
            return true;
        if (dialogShowing != null && dialogShowing.isShowing())
            return true;
        if (contextPanel != null && contextPanel.isShowing())
            return true;
        if (scrollValuePopup != null && scrollValuePopup.isShowing())
            return true;
        if (aboutBox != null && aboutBox.isShowing())
            return true;
        return false;
    }

    public void onPreviewNativeEvent(NativePreviewEvent e) {
        int cc = e.getNativeEvent().getCharCode();
        int t = e.getTypeInt();
        int code = e.getNativeEvent().getKeyCode();
        if (dialogIsShowing()) {
            if (scrollValuePopup != null && scrollValuePopup.isShowing() && (t & Event.ONKEYDOWN) != 0) {
                if (code == KEY_ESCAPE || code == KEY_SPACE)
                    scrollValuePopup.close(false);
                if (code == KEY_ENTER)
                    scrollValuePopup.close(true);
            }

            // process escape/enter for dialogs
            // multiple edit dialogs could be displayed at once, pick the one in front
            Dialog dlg = editDialog;
            if (diodeModelEditDialog != null)
                dlg = diodeModelEditDialog;
            if (customLogicEditDialog != null)
                dlg = customLogicEditDialog;
            if (dialogShowing != null)
                dlg = dialogShowing;
            if (dlg != null && dlg.isShowing() && (t & Event.ONKEYDOWN) != 0) {
                if (code == KEY_ESCAPE)
                    dlg.closeDialog();
                if (code == KEY_ENTER)
                    dlg.enterPressed();
            }
            return;
        }

        if ((t & Event.ONKEYPRESS) != 0) {
            if (cc == '-') {
                menuPerformed("key", "zoomout");
                e.cancel();
            }
            if (cc == '+' || cc == '=') {
                menuPerformed("key", "zoomin");
                e.cancel();
            }
            if (cc == '0') {
                menuPerformed("key", "zoom100");
                e.cancel();
            }
            if (cc == '/' && shortcuts['/'] == null) {
                menuPerformed("key", "search");
                e.cancel();
            }
        }

        // all other shortcuts are ignored when editing disabled
        if (noEditCheckItem.getState())
            return;

        if ((t & Event.ONKEYDOWN) != 0) {
            if (code == KEY_BACKSPACE || code == KEY_DELETE) {
                menuElm = null;
                pushUndo();
                doDelete(true);
                e.cancel();

            }
            if (code == KEY_ESCAPE) {
                setMouseMode(MODE_SELECT);
                mouseModeStr = "Select";
                tempMouseMode = mouseMode;
                e.cancel();
            }

            if (e.getNativeEvent().getCtrlKey() || e.getNativeEvent().getMetaKey()) {
                if (code == KEY_C) {
                    menuPerformed("key", "copy");
                    e.cancel();
                }
                if (code == KEY_X) {
                    menuPerformed("key", "cut");
                    e.cancel();
                }
                if (code == KEY_V) {
                    menuPerformed("key", "paste");
                    e.cancel();
                }
                if (code == KEY_Z) {
                    menuPerformed("key", "undo");
                    e.cancel();
                }
                if (code == KEY_Y) {
                    menuPerformed("key", "redo");
                    e.cancel();
                }
                if (code == KEY_D) {
                    menuPerformed("key", "duplicate");
                    e.cancel();
                }
                if (code == KEY_A) {
                    menuPerformed("key", "selectAll");
                    e.cancel();
                }
                if (code == KEY_P) {
                    menuPerformed("key", "print");
                    e.cancel();
                }
                if (code == KEY_N && isElectron()) {
                    menuPerformed("key", "newwindow");
                    e.cancel();
                }
                if (code == KEY_S) {
                    String cmd = "exportaslocalfile";
                    if (isElectron())
                        cmd = saveFileItem.isEnabled() ? "save" : "saveas";
                    menuPerformed("key", cmd);
                    e.cancel();
                }
                if (code == KEY_O) {
                    menuPerformed("key", "importfromlocalfile");
                    e.cancel();
                }
            }
        }
        if ((t & Event.ONKEYPRESS) != 0) {
            if (cc > 32 && cc < 127) {
                String c = shortcuts[cc];
                e.cancel();
                if (c == null)
                    return;
                setMouseMode(MODE_ADD_ELM);
                mouseModeStr = c;
                tempMouseMode = mouseMode;
            }
            if (cc == 32) {
                setMouseMode(MODE_SELECT);
                mouseModeStr = "Select";
                tempMouseMode = mouseMode;
                e.cancel();
            }
        }
    }

    // factors a matrix into upper and lower triangular matrices by
    // gaussian elimination. On entry, a[0..n-1][0..n-1] is the
    // matrix to be factored. ipvt[] returns an integer vector of pivot
    // indices, used in the lu_solve() routine.
    static boolean lu_factor(double a[][], int n, int ipvt[]) {
        int i, j, k;

        // check for a possible singular matrix by scanning for rows that
        // are all zeroes
        for (i = 0; i != n; i++) {
            boolean row_all_zeros = true;
            for (j = 0; j != n; j++) {
                if (a[i][j] != 0) {
                    row_all_zeros = false;
                    break;
                }
            }
            // if all zeros, it's a singular matrix
            if (row_all_zeros)
                return false;
        }

        // use Crout's method; loop through the columns
        for (j = 0; j != n; j++) {

            // calculate upper triangular elements for this column
            for (i = 0; i != j; i++) {
                double q = a[i][j];
                for (k = 0; k != i; k++)
                    q -= a[i][k] * a[k][j];
                a[i][j] = q;
            }

            // calculate lower triangular elements for this column
            double largest = 0;
            int largestRow = -1;
            for (i = j; i != n; i++) {
                double q = a[i][j];
                for (k = 0; k != j; k++)
                    q -= a[i][k] * a[k][j];
                a[i][j] = q;
                double x = Math.abs(q);
                if (x >= largest) {
                    largest = x;
                    largestRow = i;
                }
            }

            // pivoting
            if (j != largestRow) {
                if (largestRow == -1) {
                    console("largestRow == -1");
                    return false;
                }
                double x;
                for (k = 0; k != n; k++) {
                    x = a[largestRow][k];
                    a[largestRow][k] = a[j][k];
                    a[j][k] = x;
                }
            }

            // keep track of row interchanges
            ipvt[j] = largestRow;

            // check for zeroes; if we find one, it's a singular matrix.
            // we used to avoid them, but that caused weird bugs. For example,
            // two inverters with outputs connected together should be flagged
            // as a singular matrix, but it was allowed (with weird currents)
            if (a[j][j] == 0.0) {
                console("didn't avoid zero");
                // a[j][j]=1e-18;
                return false;
            }

            if (j != n - 1) {
                double mult = 1.0 / a[j][j];
                for (i = j + 1; i != n; i++)
                    a[i][j] *= mult;
            }
        }
        return true;
    }

    // Solves the set of n linear equations using a LU factorization
    // previously performed by lu_factor. On input, b[0..n-1] is the right
    // hand side of the equations, and on output, contains the solution.
    static void lu_solve(double a[][], int n, int ipvt[], double b[]) {
        int i;

        // find first nonzero b element
        for (i = 0; i != n; i++) {
            int row = ipvt[i];

            double swap = b[row];
            b[row] = b[i];
            b[i] = swap;
            if (swap != 0)
                break;
        }

        int bi = i++;
        for (; i < n; i++) {
            int row = ipvt[i];
            int j;
            double tot = b[row];

            b[row] = b[i];
            // forward substitution using the lower triangular matrix
            for (j = bi; j < i; j++)
                tot -= a[i][j] * b[j];
            b[i] = tot;
        }
        for (i = n - 1; i >= 0; i--) {
            double tot = b[i];

            // back-substitution using the upper triangular matrix
            int j;
            for (j = i + 1; j != n; j++)
                tot -= a[i][j] * b[j];
            b[i] = tot / a[i][i];

        }
    }

    void createNewLoadFile() {
        // This is a hack to fix what IMHO is a bug in the <INPUT FILE element
        // reloading the same file doesn't create a change event so importing the same
        // file twice
        // doesn't work unless you destroy the original input element and replace it
        // with a new one
        int idx = verticalPanel.getWidgetIndex(loadFileInput);
        LoadFile newlf = new LoadFile(this);
        verticalPanel.insert(newlf, idx);
        verticalPanel.remove(idx + 1);
        loadFileInput = newlf;
    }

    void addWidgetToVerticalPanel(Widget w) {
        if (iFrame != null) {
            int i = verticalPanel.getWidgetIndex(iFrame);
            verticalPanel.insert(w, i);
            setiFrameHeight();
        } else
            verticalPanel.add(w);
    }

    void removeWidgetFromVerticalPanel(Widget w) {
        verticalPanel.remove(w);
        if (iFrame != null)
            setiFrameHeight();
    }

    public static CircuitElm createCe(int tint, int x1, int y1, int x2, int y2, int f, StringTokenizer st) {
        switch (tint) {


            case 207:
                return new LabeledNodeElm(x1, y1, x2, y2, f, st);


            case 'b':
                return new BoxElm(x1, y1, x2, y2, f, st);
            case 'g':
                return new GroundElm(x1, y1, x2, y2, f, st);
            case 'i':
                return new CurrentElm(x1, y1, x2, y2, f, st);
            case 'r':
                return new ResistorElm(x1, y1, x2, y2, f, st);
            case 'v':
                return new VoltageElm(x1, y1, x2, y2, f, st);
            case 'w':
                return new WireElm(x1, y1, x2, y2, f, st);
            case 'x':
                return new TextElm(x1, y1, x2, y2, f, st);
            case 520:
                return new Component(x1, y1, x2, y2, f, st);
        }
        return null;
    }

    public static CircuitElm constructElement(String n, int x1, int y1) {


        //TODO: figure out if LabeledNodeElm is needed
        if (n.equals("LabeledNodeElm"))
            return (CircuitElm) new LabeledNodeElm(x1, y1);


        if (n.equals("BoxElm"))
            return (CircuitElm) new BoxElm(x1, y1);
        if (n.equals("GroundElm"))
            return (CircuitElm) new GroundElm(x1, y1);
        if (n.equals("CurrentElm"))
            return (CircuitElm) new CurrentElm(x1, y1);
        if (n.equals("ResistorElm"))
            return (CircuitElm) new ResistorElm(x1, y1);
        if (n.equals("DCVoltageElm") || n.equals("VoltageElm"))
            return (CircuitElm) new DCVoltageElm(x1, y1);
        if (n.equals("WireElm"))
            return (CircuitElm) new WireElm(x1, y1);
        if (n.equals("TextElm"))
            return (CircuitElm) new TextElm(x1, y1);
        if (n.equals("Component"))
            return (CircuitElm) new Component(x1, y1);
        if (n.equals("MC Component"))
            return (CircuitElm) new MCComponent(x1, y1);

        return null;
    }

    public void updateModels() {
        int i;
        for (i = 0; i != elmList.size(); i++)
            elmList.get(i).updateModels();
    }

    native boolean weAreInUS(boolean orCanada) /*-{
    try {
	l = navigator.languages ? navigator.languages[0] : (navigator.language || navigator.userLanguage) ;
    	if (l.length > 2) {
    		l = l.slice(-2).toUpperCase();
    		return (l == "US" || (l=="CA" && orCanada));
    	} else {
    		return 0;
    	}

    } catch (e) { return 0;
    }
    }-*/;

    native boolean weAreInGermany() /*-{
    try {
	l = navigator.languages ? navigator.languages[0] : (navigator.language || navigator.userLanguage) ;
	return (l.toUpperCase().startsWith("DE"));
    } catch (e) { return 0;
    }
    }-*/;

    // For debugging
    void dumpNodelist() {

        CircuitNode nd;
        CircuitElm e;
        int i, j;
        String s;
        String cs;
        //
        // for(i=0; i<nodeList.size(); i++) {
        // s="Node "+i;
        // nd=nodeList.get(i);
        // for(j=0; j<nd.links.size();j++) {
        // s=s+" " + nd.links.get(j).num + " " +nd.links.get(j).elm.getDumpType();
        // }
        // console(s);
        // }
        console("Elm list Dump");
        for (i = 0; i < elmList.size(); i++) {
            e = elmList.get(i);
            cs = e.getDumpClass().toString();
            int p = cs.lastIndexOf('.');
            cs = cs.substring(p + 1);
            if (cs == "WireElm")
                continue;
            if (cs == "LabeledNodeElm")
                cs = cs + " " + ((LabeledNodeElm) e).text;
            if (cs == "TransistorElm") {

                cs = "NTransistorElm";
            }
            s = cs;
            for (j = 0; j < e.getPostCount(); j++) {
                s = s + " " + e.nodes[j];
            }
            console(s);
        }
    }

    native void printCanvas(CanvasElement cv) /*-{
	    var img    = cv.toDataURL("image/png");
	    var win = window.open("", "print", "height=500,width=500,status=yes,location=no");
	    win.document.title = "Print Circuit";
	    win.document.open();
	    win.document.write('<img src="'+img+'"/>');
	    win.document.close();
	    setTimeout(function(){win.print();},1000);
	}-*/;


    void doPrint() {
        Canvas cv = getCircuitAsCanvas(CAC_PRINT);
        printCanvas(cv.getCanvasElement());
    }

    boolean loadedCanvas2SVG = false;

    boolean initializeSVGScriptIfNecessary(final String followupAction) {
        // load canvas2svg if we haven't already
        if (!loadedCanvas2SVG) {
            ScriptInjector.fromUrl("canvas2svg.js").setCallback(new Callback<Void, Exception>() {
                public void onFailure(Exception reason) {
                    Window.alert("Can't load canvas2svg.js.");
                }

                public void onSuccess(Void result) {
                    loadedCanvas2SVG = true;
                    if (followupAction.equals("doExportAsSVG")) {
                        doExportAsSVG();
                    } else if (followupAction.equals("doExportAsSVGFromAPI")) {
                        doExportAsSVGFromAPI();
                    }
                }
            }).inject();
            return false;
        }
        return true;
    }

    void doExportAsSVG() {
        if (!initializeSVGScriptIfNecessary("doExportAsSVG")) {
            return;
        }
        dialogShowing = new ExportAsImageDialog(CAC_SVG);
        dialogShowing.show();
    }

    public void doExportAsSVGFromAPI() {
        if (!initializeSVGScriptIfNecessary("doExportAsSVGFromAPI")) {
            return;
        }
        String svg = getCircuitAsSVG();
        callSVGRenderedHook(svg);
    }

    static final int CAC_PRINT = 0;
    static final int CAC_IMAGE = 1;
    static final int CAC_SVG = 2;

    public Canvas getCircuitAsCanvas(int type) {
        // create canvas to draw circuit into
        Canvas cv = Canvas.createIfSupported();
        Rectangle bounds = getCircuitBounds();

        // add some space on edges because bounds calculation is not perfect
        int wmargin = 140;
        int hmargin = 100;
        int w = (bounds.width * 2 + wmargin);
        int h = (bounds.height * 2 + hmargin);
        cv.setCoordinateSpaceWidth(w);
        cv.setCoordinateSpaceHeight(h);

        Context2d context = cv.getContext2d();
        drawCircuitInContext(context, type, bounds, w, h);
        return cv;
    }

    // create SVG context using canvas2svg
    native static Context2d createSVGContext(int w, int h) /*-{
	    return new C2S(w, h);
	}-*/;

    native static String getSerializedSVG(Context2d context) /*-{
	    return context.getSerializedSvg();
	}-*/;

    public String getCircuitAsSVG() {
        Rectangle bounds = getCircuitBounds();

        // add some space on edges because bounds calculation is not perfect
        int wmargin = 140;
        int hmargin = 100;
        int w = (bounds.width + wmargin);
        int h = (bounds.height + hmargin);
        Context2d context = createSVGContext(w, h);
        drawCircuitInContext(context, CAC_SVG, bounds, w, h);
        return getSerializedSVG(context);
    }

    void drawCircuitInContext(Context2d context, int type, Rectangle bounds, int w, int h) {
        Graphics g = new Graphics(context);
        context.setTransform(1, 0, 0, 1, 0, 0);
        double oldTransform[] = Arrays.copyOf(transform, 6);

        double scale = 1;

        // turn on white background, turn off current display
        boolean p = printableCheckItem.getState();
        boolean c = dotsCheckItem.getState();
        boolean print = (type == CAC_PRINT);
        if (print)
            printableCheckItem.setState(true);
        if (printableCheckItem.getState()) {
            CircuitElm.whiteColor = Color.black;
            CircuitElm.lightGrayColor = Color.black;
            g.setColor(Color.white);
        } else {
            CircuitElm.whiteColor = Color.white;
            CircuitElm.lightGrayColor = Color.lightGray;
            g.setColor(Color.black);
            g.fillRect(0, 0, w, h);
        }
        dotsCheckItem.setState(false);

        int wmargin = 140;
        int hmargin = 100;
        if (bounds != null)
            scale = Math.min(w / (double) (bounds.width + wmargin), h / (double) (bounds.height + hmargin));

        // ScopeElms need the transform array to be updated
        transform[0] = transform[3] = scale;
        transform[4] = -(bounds.x - wmargin / 2);
        transform[5] = -(bounds.y - hmargin / 2);
        context.scale(scale, scale);
        context.translate(transform[4], transform[5]);
        context.setLineCap(Context2d.LineCap.ROUND);

        // draw elements
        int i;
        for (i = 0; i != elmList.size(); i++) {
            getElm(i).draw(g);
        }
        for (i = 0; i != postDrawList.size(); i++) {
            CircuitElm.drawPost(g, postDrawList.get(i));
        }

        // restore everything
        printableCheckItem.setState(p);
        dotsCheckItem.setState(c);
        transform = oldTransform;
    }

    boolean isSelection() {
        for (int i = 0; i != elmList.size(); i++)
            if (getElm(i).isSelected())
                return true;
        return false;
    }

    public CustomCompositeModel getCircuitAsComposite() {
        int i;
        String nodeDump = "";
        String dump = "";
        // String models = "";
        CustomLogicModel.clearDumpedFlags();
        DiodeModel.clearDumpedFlags();
        TransistorModel.clearDumpedFlags();
        Vector<LabeledNodeElm> sideLabels[] = new Vector[]{new Vector<LabeledNodeElm>(), new Vector<LabeledNodeElm>(),
                new Vector<LabeledNodeElm>(), new Vector<LabeledNodeElm>()};
        Vector<ExtListEntry> extList = new Vector<ExtListEntry>();
        boolean sel = isSelection();

        boolean used[] = new boolean[nodeList.size()];
        boolean extnodes[] = new boolean[nodeList.size()];

        // find all the labeled nodes, get a list of them, and create a node number map
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (sel && !ce.isSelected())
                continue;
            if (ce instanceof LabeledNodeElm) {
                LabeledNodeElm lne = (LabeledNodeElm) ce;
                String label = lne.text;
                if (lne.isInternal())
                    continue;

                // already added to list?
                if (extnodes[ce.getNode(0)])
                    continue;

                int side = ChipElm.SIDE_W;
                if (Math.abs(ce.dx) >= Math.abs(ce.dy) && ce.dx > 0)
                    side = ChipElm.SIDE_E;
                if (Math.abs(ce.dx) <= Math.abs(ce.dy) && ce.dy < 0)
                    side = ChipElm.SIDE_N;
                if (Math.abs(ce.dx) <= Math.abs(ce.dy) && ce.dy > 0)
                    side = ChipElm.SIDE_S;

                // create ext list entry for external nodes
                sideLabels[side].add(lne);
                extnodes[ce.getNode(0)] = true;
            }
        }

        Collections.sort(sideLabels[ChipElm.SIDE_W], (LabeledNodeElm a, LabeledNodeElm b) -> Integer.signum(a.y - b.y));
        Collections.sort(sideLabels[ChipElm.SIDE_E], (LabeledNodeElm a, LabeledNodeElm b) -> Integer.signum(a.y - b.y));
        Collections.sort(sideLabels[ChipElm.SIDE_N], (LabeledNodeElm a, LabeledNodeElm b) -> Integer.signum(a.x - b.x));
        Collections.sort(sideLabels[ChipElm.SIDE_S], (LabeledNodeElm a, LabeledNodeElm b) -> Integer.signum(a.x - b.x));

        for (int side = 0; side < sideLabels.length; side++) {
            for (int pos = 0; pos < sideLabels[side].size(); pos++) {
                LabeledNodeElm lne = sideLabels[side].get(pos);
                ExtListEntry ent = new ExtListEntry(lne.text, lne.getNode(0), pos, side);
                extList.add(ent);
            }
        }

        // output all the elements
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (sel && !ce.isSelected())
                continue;
            // don't need these elements dumped
            if (ce instanceof WireElm || ce instanceof LabeledNodeElm)
                continue;
            if (ce instanceof GraphicElm || ce instanceof GroundElm)
                continue;
            int j;
            if (nodeDump.length() > 0)
                nodeDump += "\r";
            nodeDump += ce.getClass().getSimpleName();
            for (j = 0; j != ce.getPostCount(); j++) {
                int n = ce.getNode(j);
                used[n] = true;
                nodeDump += " " + n;
            }

            // save positions
            int x1 = ce.x;
            int y1 = ce.y;
            int x2 = ce.x2;
            int y2 = ce.y2;

            // set them to 0 so they're easy to remove
            ce.x = ce.y = ce.x2 = ce.y2 = 0;

            String tstring = ce.dump();
            tstring = tstring.replaceFirst("[A-Za-z0-9]+ 0 0 0 0 ", ""); // remove unused tint_x1 y1 x2 y2 coords for
            // internal components

            // restore positions
            ce.x = x1;
            ce.y = y1;
            ce.x2 = x2;
            ce.y2 = y2;
            if (dump.length() > 0)
                dump += " ";
            dump += CustomLogicModel.escape(tstring);
        }

        for (i = 0; i != extList.size(); i++) {
            ExtListEntry ent = extList.get(i);
            if (!used[ent.node]) {
                Window.alert("Node \"" + ent.name + "\" is not used!");
                return null;
            }
        }

        boolean first = true;
        for (i = 0; i != unconnectedNodes.size(); i++) {
            int q = unconnectedNodes.get(i);
            if (!extnodes[q] && used[q]) {
                if (nodesWithGroundConnectionCount == 0 && first) {
                    first = false;
                    continue;
                }
                Window.alert("Some nodes are unconnected!");
                return null;
            }
        }

        CustomCompositeModel ccm = new CustomCompositeModel();
        ccm.nodeList = nodeDump;
        ccm.elmDump = dump;
        ccm.extList = extList;
        return ccm;
    }

    static void invertMatrix(double a[][], int n) {
        int ipvt[] = new int[n];
        lu_factor(a, n, ipvt);
        int i, j;
        double b[] = new double[n];
        double inva[][] = new double[n][n];

        // solve for each column of identity matrix
        for (i = 0; i != n; i++) {
            for (j = 0; j != n; j++)
                b[j] = 0;
            b[i] = 1;
            lu_solve(a, n, ipvt, b);
            for (j = 0; j != n; j++)
                inva[j][i] = b[j];
        }

        // return in original matrix
        for (i = 0; i != n; i++)
            for (j = 0; j != n; j++)
                a[i][j] = inva[i][j];
    }

    double getLabeledNodeVoltage(String name) {
        Integer node = LabeledNodeElm.getByName(name);
        if (node == null || node == 0)
            return 0;
        // subtract one because ground is not included in nodeVoltages[]
        return nodeVoltages[node.intValue() - 1];
    }


    native JsArray<JavaScriptObject> getJSArray() /*-{ return []; }-*/;

    JsArray<JavaScriptObject> getJSElements() {
        int i;
        JsArray<JavaScriptObject> arr = getJSArray();
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            ce.addJSMethods();
            arr.push(ce.getJavaScriptObject());
        }
        return arr;
    }

    native void setupJSInterface() /*-{
	    var that = this;
	    $wnd.CircuitJS1 = {
	        setSimRunning: $entry(function(run) { that.@com.lushprojects.circuitjs1.client.CirSim::setSimRunning(Z)(run); } ),
	        getTime: $entry(function() { return that.@com.lushprojects.circuitjs1.client.CirSim::t; } ),
	        getTimeStep: $entry(function() { return that.@com.lushprojects.circuitjs1.client.CirSim::timeStep; } ),
	        setTimeStep: $entry(function(ts) { that.@com.lushprojects.circuitjs1.client.CirSim::timeStep = ts; } ),
	        isRunning: $entry(function() { return that.@com.lushprojects.circuitjs1.client.CirSim::simIsRunning()(); } ),
	        getNodeVoltage: $entry(function(n) { return that.@com.lushprojects.circuitjs1.client.CirSim::getLabeledNodeVoltage(Ljava/lang/String;)(n); } ),
	        getElements: $entry(function() { return that.@com.lushprojects.circuitjs1.client.CirSim::getJSElements()(); } ),
	        getCircuitAsSVG: $entry(function() { return that.@com.lushprojects.circuitjs1.client.CirSim::doExportAsSVGFromAPI()(); } ),
	        exportCircuit: $entry(function() { return that.@com.lushprojects.circuitjs1.client.CirSim::dumpCircuit()(); } ),
	        importCircuit: $entry(function(circuit, subcircuitsOnly) { return that.@com.lushprojects.circuitjs1.client.CirSim::importCircuitFromText(Ljava/lang/String;Z)(circuit, subcircuitsOnly); })
	    };
	    var hook = $wnd.oncircuitjsloaded;
	    if (hook)
	    	hook($wnd.CircuitJS1);
	}-*/;

    native void callUpdateHook() /*-{
	    var hook = $wnd.CircuitJS1.onupdate;
	    if (hook)
	    	hook($wnd.CircuitJS1);
	}-*/;

    native void callAnalyzeHook() /*-{
            var hook = $wnd.CircuitJS1.onanalyze;
            if (hook)
                hook($wnd.CircuitJS1);
    	}-*/;

    native void callTimeStepHook() /*-{
	    var hook = $wnd.CircuitJS1.ontimestep;
	    if (hook)
	    	hook($wnd.CircuitJS1);
	}-*/;

    native void callSVGRenderedHook(String svgData) /*-{
		var hook = $wnd.CircuitJS1.onsvgrendered;
		if (hook)
			hook($wnd.CircuitJS1, svgData);
	}-*/;

    class UndoItem {
        public String dump;
        public double scale, transform4, transform5;

        UndoItem(String d) {
            dump = d;
            scale = transform[0];
            transform4 = transform[4];
            transform5 = transform[5];
        }
    }

}