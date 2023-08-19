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

package lahde.tccbuilder.client;

import lahde.tccbuilder.client.math3.linear.LUDecomposition;
import lahde.tccbuilder.client.math3.linear.RealVector;

// GWT conversion (c) 2015 by Iain Sharp

// For information about the theory behind this, see Electronic Circuit & System Simulation Methods by Pillage
// or https://github.com/sharpie7/circuitjs1/blob/master/INTERNALS.md

import java.util.Vector;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

import lahde.tccbuilder.client.util.Locale;
import lahde.tccbuilder.client.util.PerfMonitor;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.canvas.dom.client.TextMetrics;

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
    CheckboxMenuItem showTemperaturesCheckItem;
    CheckboxMenuItem showOverlayCheckItem;
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

    String lastCursorStyle;
    boolean mouseWasOverSplitter = false;

    // Class addingClass;
    PopupPanel contextPanel = null;
    int mouseMode = MODE_SELECT;
    int tempMouseMode = MODE_SELECT;
    String mouseModeStr = "Select";
    static final int MODE_ADD_ELM = 0;
    static final int MODE_DRAG_ALL = 1;
    static final int MODE_DRAG_ROW = 2;
    static final int MODE_DRAG_COLUMN = 3;
    static final int MODE_DRAG_SELECTED = 4;
    static final int MODE_DRAG_POST = 5;
    static final int MODE_SELECT = 6;
    static final int MODE_DRAG_SPLITTER = 7;
    int dragGridX, dragGridY, dragScreenX, dragScreenY, initDragGridX, initDragGridY;
    long mouseDownTime;
    long zoomTime;
    int mouseCursorX = -1;
    int mouseCursorY = -1;
    Rectangle selectedArea;
    int gridSize, gridMask, gridRound;
    boolean dragging;
    boolean dcAnalysisFlag;
    // boolean useBufferedImage;
    boolean isMac;
    String ctrlMetaKey;
    double t;
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
    Vector<CircuitElm> elmList;
    Vector<Adjustable> adjustables;
    CircuitElm dragElm, menuElm, stopElm;

    private CircuitElm mouseElm = null;
    boolean didSwitch = false;
    int mousePost = -1;
    CircuitElm plotXElm, plotYElm;
    int draggingPost;
    boolean simRunning;
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

    public int printing_interval;
    // public double sim_file;
    // public double temperatures_file;
    public int ud;
    double[] x_prev;
    double[] x_mod;

    boolean viewTempsInGraph = true;
    boolean viewTempsOverlay = true;
    double minTemp, maxTemp;
    public String materialFlagText;
    ArrayList<String> awaitedResponses;
    int testCounter = 0;
    Timer testTimer;

    // Two-dimensional choice
    int simDimensionality = 1;
    Vector<TwoDimComponent> simTwoDimComponents;
    TwoDimTCE twoDimTCE;
    TwoDimEqSys twoDimES;
    TwoDimBC twoDimBC;

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
        //fileMenuBar.addItem(iconMenuItem("microchip", "Create Subcircuit...", new MyCommand("file", "createsubcircuit")));
        //fileMenuBar.addItem(iconMenuItem("magic", "Find DC Operating Point", new MyCommand("file", "dcanalysis")));
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
        m.addItem(showTemperaturesCheckItem = new CheckboxMenuItem(Locale.LS("Show Temperatures in Graph"), new Command() {
            public void execute() {
                viewTempsInGraph = !viewTempsInGraph;
            }
        }));
        showTemperaturesCheckItem.setState(true);
/*        m.addItem(showOverlayCheckItem = new CheckboxMenuItem(Locale.LS("Show Temperature Overlay (2D)"), new Command() {
            public void execute() {
                viewTempsOverlay = !viewTempsOverlay;
            }
        }));
        showOverlayCheckItem.setState(true);*/
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
            layoutPanel.addEast(verticalPanel, VERTICALPANELWIDTH + 32);
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
        resetButton = new Button(Locale.LS("Build TCC"));
        quickResetButton = new Button(Locale.LS("Reset TCC"));
        testButton = new Button(Locale.LS("Test TCC"));
        runStopButton = new Button(Locale.LSHTML("<Strong>RUN</Strong>&nbsp;/&nbsp;Stop"));
        buttonPanel.add(resetButton);
        buttonPanel.add(quickResetButton);
        //buttonPanel.add(testButton);
        buttonPanel.add(runStopButton);
        resetButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (simDimensionality == 1)
                    new StartDialog(theSim).show();
                else if (simDimensionality == 2)
                    new StartDialog2D(theSim).show();
            }
        });
        quickResetButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                resetAction();
            }
        });
        testButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                setupForTest();
            }
        });


        runStopButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                setSimRunning(!simIsRunning());
                //TwoDimTCCmanager.printTemps(twoDimTCE.cvs);
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
        //dimensionality.addItem("2D");
        dimensionality.addStyleName("topSpace");
        verticalPanel.add(dimensionality);

        dimensionality.setSelectedIndex(0);
        dimensionality.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                switch (dimensionality.getSelectedItemText()) {
                    case "1D":
                        simDimensionality = 1;
                        break;
                    case "2D":
                        simDimensionality = 2;
                        break;
                }


                drawMenuBar.clearItems();
                composeMainMenu(drawMenuBar, 1);
                mainMenuBar.clearItems();
                composeMainMenu(mainMenuBar, 1);
                loadShortcuts();
                simTwoDimComponents = new Vector<TwoDimComponent>();
                simComponents = new Vector<Component>();
            }
        });

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
                for (Component c : simComponents)
                    c.calculateLength();

                for (TwoDimComponent c : simTwoDimComponents)
                    c.calculateLengthHeight();

                if (simRunning)
                    resetAction();
            }
        });
        cyclicOperationLabel = new HTML();
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
        elmMenuBar.addItem(elmTempsMenuItem = new MenuItem(Locale.LS("Toggle in Temperature Display"), new MyCommand("elm", "viewTemps")));
        elmMenuBar.addItem(elmCutMenuItem = new MenuItem(Locale.LS("Cut"), new MyCommand("elm", "cut")));
        elmMenuBar.addItem(elmCopyMenuItem = new MenuItem(Locale.LS("Copy"), new MyCommand("elm", "copy")));
        elmMenuBar.addItem(elmDeleteMenuItem = new MenuItem(Locale.LS("Delete"), new MyCommand("elm", "delete")));
        elmMenuBar.addItem(new MenuItem(Locale.LS("Duplicate"), new MyCommand("elm", "duplicate")));
        elmMenuBar.addItem(elmFlipMenuItem = new MenuItem(Locale.LS("Swap Terminals"), new MyCommand("elm", "flip")));
        elmMenuBar.addItem(elmSplitMenuItem = menuItemWithShortcut("", "Split Wire", Locale.LS(ctrlMetaKey + "click"), new MyCommand("elm", "split")));
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

        // 2D
        simTwoDimComponents = new Vector<TwoDimComponent>();
        HeatSimProps.BC bc = HeatSimProps.BC.CONVECTIVE;  // No way I am passing this to a constructor 4x.
        twoDimBC = new TwoDimBC(new HeatSimProps.BC[]{bc, bc, bc, bc});

        this.ud = 0;
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


        String CORSproxy = "https://corsproxy.io/?";
        String baseURL = CORSproxy + "http://materials.tccbuilder.org/";
/*        baseURL = GWT.getModuleBaseURL() + "material_data/materials_library/";
        baseURL = "http://127.0.0.1:8888/";*/
        //TODO: Add a callback to setSimRunning()
        readMaterialFlags(baseURL + "materials_flags.csv");
    }

    private void readMaterialFlags(String url) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
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
                            GWT.log(name);
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
        heatCircuit.q_in = qIn;
        heatCircuit.q_out = qOut;

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

    void makeTwoDimTCE() {
        GWT.log("MAKING TCE");
        GWT.log(String.valueOf(simTwoDimComponents.size()));
        twoDimTCE = new TwoDimTCE("2D TCE", 0, simTwoDimComponents);
        twoDimTCE.buildTCE();
        TwoDimTCCmanager.setTemperatures(twoDimTCE.cvs, 300.0, true);
        minTemp = Math.min(twoDimBC.T[0], twoDimBC.T[1]);
        maxTemp = Math.max(twoDimBC.T[0], twoDimBC.T[1]);
        GWT.log(minTemp + " - " + maxTemp);
    }

    void setTwoDimSim() {
        twoDimES = new TwoDimEqSys(twoDimTCE, twoDimBC);
        //TwoDimTCCmanager.printTemps(twoDimTCE.cvs);
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
        if (!cycleParts.isEmpty())
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
            ModelMethods.tdmaSolve(heatCircuit.cvs, heatCircuit.underdiag, heatCircuit.diag, heatCircuit.upperdiag, heatCircuit.rhs);
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

    public void twoDimHeatTransferStep() {
        // GWT.log(String.valueOf(minTemp));
        // GWT.log(String.valueOf(maxTemp));
        twoDimES.conductionMatrix();
        LUDecomposition solver = new LUDecomposition(twoDimES.matrix);
        RealVector solutionVector = solver.getSolver().solve(twoDimES.rhs);
        for (int i = 0; i < twoDimTCE.cvs.size(); i++) {
            twoDimTCE.cvs.get(i).temperature = solutionVector.getEntry(i);
        }
        // TwoDimTCCmanager.printTemps(twoDimTCE.cvs);
        // update modes
        // calculate again
        // set new temperatures
        // replace old with new
        TwoDimTCCmanager.replaceOldNew(twoDimTCE.cvs);
        this.append_new_temps();

    }

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


        if (selectColor != null)
            CircuitElm.selectColor = new Color(URL.decodeQueryString(selectColor));
        else
            CircuitElm.selectColor = Color.cyan;


    }

    MenuItem menuItemWithShortcut(String icon, String text, String shortcut, MyCommand cmd) {
        final String html =
                "<div style=\"display : flex; align-items:center; justify-content:space-between; \">" +
                        "<div style=\"text-align:left;\">" +
                        "<i class=\"cirjsicon-" + icon + "\"></i>" +
                        Locale.LS(text) +
                        "</div>" +
                        "<div style=\"text-align:right;\">" +
                        shortcut +
                        "</div>" +
                        "</div>";

        return new MenuItem(SafeHtmlUtils.fromTrustedString(html), cmd);
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

            if (e.timeStamp - lastTap < 300) {
                etype = "dblclick";
            } else {
                tmout = setTimeout(function () {
                    sim.@lahde.tccbuilder.client.CirSim::longPress()();
                }, 500);
            }
            lastTap = e.timeStamp;

            var touch1 = e.touches[0];
            var touch2 = e.touches[e.touches.length - 1];
            var mouseEvent = new MouseEvent(etype, {
                clientX: .5 * (touch1.clientX + touch2.clientX),
                clientY: .5 * (touch1.clientY + touch2.clientY)
            });
            cv.dispatchEvent(mouseEvent);
            if (e.touches.length > 1)
                sim.@lahde.tccbuilder.client.CirSim::twoFingerTouch(II)(mouseEvent.clientX, mouseEvent.clientY - cv.getBoundingClientRect().y);
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
                sim.@lahde.tccbuilder.client.CirSim::zoomCircuit(D)(40 * (Math.log(e.scale) - Math.log(lastScale)));
                lastScale = e.scale;
            }
            var touch1 = e.touches[0];
            var touch2 = e.touches[e.touches.length - 1];
            var mouseEvent = new MouseEvent("mousemove", {
                clientX: .5 * (touch1.clientX + touch2.clientX),
                clientY: .5 * (touch1.clientY + touch2.clientY)
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
        //TODO: update this when mergind with elements branchs
        if (simDimensionality == 2) {
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add 2DComponent"), "2DComponent"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add ZigZagInterface"), "ZigZagInterface"));
        } else {
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Component"), "Component"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Conduit"), "GroundElm"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Resistor"), "ResistorElm"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Switch"), "SwitchElm"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Diode"), "DiodeElm"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Regulator"), "RegulatorElm"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Capacitor"), "CapacitorElm"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Heat Source/Sink"), ""));
        }


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

        if (stopElm != null && stopElm != mouseElm)
            stopElm.setMouseElm(true);


        Graphics g = new Graphics(cvcontext);


        g.setColor(Color.black);
        cv.getElement().getStyle().setBackgroundColor(Color.gray.getHexValue());


        // Clear the frame
        g.fillRect(0, 0, canvasWidth, canvasHeight);

        // Run circuit
        if (simRunning) {

            perfmon.startContext("runCircuit()");
            try {
                if (awaitedResponses.isEmpty())
                    runCircuit();
            } catch (Exception e) {
                console("exception in runCircuit " + e.toString());
                //debugger();
                e.printStackTrace();
            }
            perfmon.stopContext();
        }

        long sysTime = System.currentTimeMillis();
        if (simRunning) {

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


        perfmon.startContext("graphics");


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
            for (CircuitElm elm : elmList) {
                if (elm.needsHighlight())
                    elm.drawPosts(g);
            }
            for (CircuitElm elm : elmList) {
                if (!elm.needsHighlight())
                    elm.drawPosts(g);
            }

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
            if (dragElm instanceof ZigZagInterface)
                ((TwoDimComponent) dragElm).calculateLengthHeight();
            if (dragElm instanceof TwoDimComponent)
                ((TwoDimComponent) dragElm).calculateLengthHeight();
            if (dragElm instanceof Component)
                ((Component) dragElm).calculateLength();


            dragElm.drawHandles(g, CircuitElm.selectColor);
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
            ctx.setFont(fontSize + "px Roboto");

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
        ctx.setFont("bold " + fontSize + "px Roboto");

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
        g.setColor(Color.white);

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
        for (int i = 0; info[i] != null; i++)
            g.drawString(info[i], 8, 8 + 16 * (i + 1));

    }

    void needAnalyze() {
        repaint();
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

    public static native void debugger() /*-{
        debugger;
    }-*/;


    public static String formatLength(double value) {
        if (value < 1e-3) { // less than 1 millimeter
            value *= 1e6; // convert to micrometers
            return (Math.round(value * 1000) / 1000.0) + " µm";
        } else if (value < 1) { // less than 1 meter
            value *= 1e3; // convert to millimeters
            return (Math.round(value * 1000) / 1000.0) + " mm";
        } else {
            return (Math.round(value * 1000) / 1000.0) + " m";
        }
    }


    double getIterCount() {
        // IES - remove interaction
        if (speedBar.getValue() == 0)
            return 0;

        return .1 * Math.exp((speedBar.getValue() - 61) / 24.);

    }

    boolean converged;

    void runCircuit() {

        //int maxIter = getIterCount();
        long steprate = (long) (160 * getIterCount());
        long tm = System.currentTimeMillis();
        long lit = lastIterTime;
        if (lit == 0) {
            lastIterTime = tm;
            return;
        }
        // Check if we don't need to run simulation (for very slow simulation speeds).
        // If the circuit changed, do at least one iteration to make sure everything is consistent.
        if (1000 >= steprate * (tm - lastIterTime))
            return;
        int timeStepCountAtFrameStart = timeStepCount;

        //stampCircuit();
        int iter;
        for (iter = 1; ; iter++) {
            // *************************** Katni *******************************
            if (simDimensionality == 1) {
                if (!this.cyclic) {
                    heat_transfer_step();
                    time += dt;
                } else {
                    if (this.cycleParts.isEmpty())
                        Window.alert("Sim set to cyclic but cycle parts undefined");

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
            }
            if (simDimensionality == 2) {
                twoDimHeatTransferStep();
                time += dt;
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
        //calcWireCurrents();


    }


    int min(int a, int b) {
        return (a < b) ? a : b;
    }

    int max(int a, int b) {
        return (a > b) ? a : b;
    }

    public void resetAction() {
        int i;
        if (t == 0)
            setSimRunning(true);
        t = timeStepAccum = 0;
        timeStepCount = 0;
        for (i = 0; i != elmList.size(); i++)
            getElm(i).reset();
        repaint();
        if (simDimensionality == 1) {
            makeTCC();
            setHeatSim();
        } else if (simDimensionality == 2) {
            makeTwoDimTCE();
            setTwoDimSim();
        }
    }

    static void electronSaveAsCallback(String s) {
        s = s.substring(s.lastIndexOf('/') + 1);
        s = s.substring(s.lastIndexOf('\\') + 1);
        theSim.setCircuitTitle(s);
        theSim.allowSave(true);
        theSim.repaint();
    }

    static void electronSaveCallback() {
        theSim.repaint();
    }

    static native void electronSaveAs(String dump) /*-{
        $wnd.showSaveDialog().then(function (file) {
            if (file.canceled)
                return;
            $wnd.saveFile(file, dump);
            @lahde.tccbuilder.client.CirSim::electronSaveAsCallback(Ljava/lang/String;)(file.filePath.toString());
        });
    }-*/;

    static native void electronSave(String dump) /*-{
        $wnd.saveFile(null, dump);
        @lahde.tccbuilder.client.CirSim::electronSaveCallback()();
    }-*/;

    static void electronOpenFileCallback(String text, String name) {
        LoadFile.doLoadCallback(text, name);
        theSim.allowSave(true);
    }

    static native void electronOpenFile() /*-{
        $wnd.openFile(function (text, name) {
            @lahde.tccbuilder.client.CirSim::electronOpenFileCallback(Ljava/lang/String;Ljava/lang/String;)(text, name);
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
            aboutBox = new AboutBox(TCCBuilder.versionString);
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

    String dumpThermalCircuit() {
        String dump = "";


        if (simDimensionality == 1) {
            dump =
                    "Data directory: " + "/materials\n" +
                            "Time step dt: " + dt + "\n" +
                            "Dimensionality: 1D\n" +
                            "Boundary condition on the left: " + ModelMethods.return_bc_name(heatCircuit.left_boundary) + "\n" +
                            "Boundary condition on the right: " + ModelMethods.return_bc_name(heatCircuit.right_boundary) + "\n" +
                            "Temperature on the left: " + heatCircuit.temp_left + " K\n" +
                            "Convection coefficient on the left: " + heatCircuit.h_left + " W/(m²K)\n" +
                            "Temperature on the right: " + heatCircuit.temp_right + " K\n" +
                            "Convection coefficient on the right: " + heatCircuit.h_right + " W/(m²K)\n";

            dump += "\nThermal control elements: \n";
            for (TCE tce : simTCEs) {
                dump += "\nTCE: " + tce.name + "\n";
                dump += "Components: \n";
                for (Component component : tce.components) {
                    dump += "Component name: " + component.name + "\n" +
                            "Component index: " + component.index + "\n" +
                            "Material: " + component.material.materialName + "\n" +
                            "Number of control volumes:  " + component.num_cvs + "\n" +
                            "Control volume length: " + formatLength(component.cvs.get(0).dx) + "\n" +
                            "Constant density: " + ((component.constRho == -1) ? "not set" : component.constRho + " kg/m³") + "\n" +
                            "Constant specific heat: " + ((component.constCp == -1) ? "not set" : component.constCp + " J/(kgK)") + "\n" +
                            "Constant thermal conductivity: " + ((component.constK == -1) ? "not set" : component.constK + " W/(mK)") + "\n" +
                            "Left contact resistance: " + component.left_resistance + " mK/W\n" +
                            "Right contact resistance: " + component.right_resistance + " mK/W\n" +
                            "Generated heat: " + 0.0 + " W/m²\n\n";
                }
            }
            dump += "\nTemperatures:\n";
            dump += "Time\t";
            for (int i = 0; i < num_cvs; i++) {
                dump += "CV# " + i + "\t";
            }
            dump += "\n";
            for (int i = 0; i < temperatures.size(); i++) {
                Double[] temp = temperatures.get(i);
                Double time = times.get(i);
                dump += NumberFormat.getFormat("0.000").format(time) + "\t";
                for (double CVTemp : temp) {
                    dump += NumberFormat.getFormat("0.00").format(CVTemp) + "\t";
                }
                dump += "\n";
            }
            dump += "\nFluxes:\n";
            heatCircuit.calculateHeatFluxes();
            for (Double f : heatCircuit.fluxes)
                dump += f + "\t";

        } else if (simDimensionality == 2) {
            dump =
                    "Data directory: " + "/materials\n" +
                            "Time step dt: " + theSim.dt + "\n" +
                            "Dimensionality: 2D\n" +
                            "Boundary condition on the left: " + heatCircuit.left_boundary + "\n" +
                            "Boundary condition on the right: " + heatCircuit.right_boundary + "\n" +
                            "Temperature on the left: " + " K\n" +
                            "Convection coefficient on the left: " + " W/(m²K)\n" +
                            "Temperature on the right: " + " K\n" +
                            "Convection coefficient on the right: " + " W/(m²K)\n";

            dump += "\nThermal control elements: \n";
            dump += "\nTCE: " + twoDimTCE.name + "\n";
            dump += "Components: \n";
            for (TwoDimComponent component : twoDimTCE.components) {
                dump += "Component name: " + component.name + "\n" +
                        "Component index: " + component.index + "\n" +
                        "Material: " + component.material.materialName + "\n" +
                        "X-discretizaton number:  " + component.n + "\n" +
                        "Y-discretizaton number:  " + component.m + "\n" +
                        "Control volume length: " + formatLength(component.cvs.get(0).dx) + "\n" +
                        "Control volume height: " + formatLength(component.cvs.get(0).dy) + "\n" +
                        "Constant density: " + (component.cvs.get(0).constRho == -1 ? "not set" : component.cvs.get(0).constRho) + "kg/m³\n" +
                        "Constant specific heat: " + (component.cvs.get(0).constCp == -1 ? "not set" : component.cvs.get(0).constCp) + "J/(kgK)\n" +
                        "Constant thermal conductivity: " + (component.cvs.get(0).constK == -1 ? "not set" : component.cvs.get(0).constK) + " W/(mK)\n" +
                        "Left contact resistance: " + component.resistances[0] + "mK/W\n" +
                        "Right contact resistance: " + component.resistances[1] + "mK/W\n" +
                        "Bottom contact resistance: " + component.resistances[2] + "mK/W\n" +
                        "Top contact resistance: " + component.resistances[3] + "mK/W\n" +
                        "Generated heat: " + 0.0 + "W/m²\n\n";
            }
            dump += "\nTemperatures at " + NumberFormat.getFormat("0.00").format(time) + "s\n";
            Vector<TwoDimCV> cvs = twoDimTCE.cvs;
            for (int i = 0; i < cvs.size(); i++) {
                TwoDimCV cv = cvs.get(i);
                if (i % twoDimTCE.n == 0)
                    dump += "\n";
                dump += NumberFormat.getFormat("0.00").format(cv.temperature) + "\t";
            }
            dump += "\nfw";
            for (int i = 0; i < twoDimTCE.n - 1; i++) {
                double flow = 0.0;
                for (int j = 0; j < twoDimTCE.m; j++) {
                    TwoDimCV cv = cvs.get(j * twoDimTCE.n + i);
                    TwoDimCV cve = cvs.get(j * twoDimTCE.n + i + 1);
                    flow += (cv.kd[1] * (cv.temperature - cve.temperature) / cv.dx) / twoDimTCE.m;
                }
                dump += "\nflow(" + i + ")=" + NumberFormat.getFormat("0.0000").format(flow);

            }
            dump += "\nfe\n";
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

    public void setupForTest() {
        cyclic = true;
        temp_left = 291;
        h_left = 100000.0;
        h_right = 100000.0;
        temp_right = 290;
        qIn = 0;
        qOut = 0;
        dt = 0.005;
        startTemp = 290;
        left_boundary = 41;
        right_boundary = 42;


        CyclePart cyclePartHeatTransfer = new CyclePart(cycleParts.size(), this);
        cyclePartHeatTransfer.partType = CyclePart.PartType.HEAT_TRANSFER;
        cyclePartHeatTransfer.duration = 1.0;
        cycleParts.add(cyclePartHeatTransfer);

        CyclePart cyclePartMagneticFieldChange = new CyclePart(cycleParts.size(), this);
        cyclePartMagneticFieldChange.partType = CyclePart.PartType.MAGNETIC_FIELD_CHANGE;
        cyclePartMagneticFieldChange.components.add(simComponents.get(2));
        simComponents.get(2).fieldIndex = 2;
        cycleParts.add(cyclePartMagneticFieldChange);

        // CyclePart cyclePartPropertiesChange = new CyclePart(cycleParts.size(), this);
        // cyclePartPropertiesChange.partType = CyclePart.PartType.PROPERTIES_CHANGE;
        // cyclePartPropertiesChange.components.add(simComponents.get(1));
        // cyclePartPropertiesChange.components.add(simComponents.get(3));

//        tukaj neki smrdi :{ ( ne dela )
/*        cyclePartPropertiesChange.newProperties.add(new Vector<Double>());
        cyclePartPropertiesChange.newProperties.lastElement().add(-1.0);
        cyclePartPropertiesChange.newProperties.lastElement().add(-1.0);
        cyclePartPropertiesChange.newProperties.lastElement().add(-1.0);
        */
//        cycleParts.add(cyclePartPropertiesChange);

        for (CyclePart cp : cycleParts) {
            CyclicDialog cd = new CyclicDialog(this);
            cd.printCyclePart(cp, cyclicOperationLabel);
            cd.closeDialog();
        }
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

                    if (tint == '%' || tint == '?' || tint == 'B') {
                        // ignore afilter-specific stuff
                        break;
                    }
                    // do not add new symbols here without testing export as link

                    // if first character is a digit then parse the type as a number
                    if (tint >= '0' && tint <= '9')
                        tint = Integer.parseInt(type);


                    if (tint == 38) {
                        Adjustable adj = new Adjustable(st, this);
                        if (adj.elm != null)
                            adjustables.add(adj);
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

        try {
            minTimeStep = Double.parseDouble(st.nextToken());
        } catch (Exception e) {
        }
        setGrid();
    }

    int snapGrid(int n) {
        return (n + gridRound) & gridMask;
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
                    if (mouseElm instanceof TwoDimComponent)
                        ((TwoDimComponent) mouseElm).calculateLengthHeight();
                    else if (mouseElm instanceof Component)
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
        if (mouseElm != null)
            return false;
        int i;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.isSelected())
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
        int x, y;
        if (mouseElm != null) {
            elmTempsMenuItem.setEnabled(false);

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
            GWT.log(ex.toString());
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


            case 'g':
                return new GroundElm(x1, y1, x2, y2, f, st);
            case 'r':
                return new ResistorElm(x1, y1, x2, y2, f, st);
            case 'w':
                return new WireElm(x1, y1, x2, y2, f, st);
            case 520:
                return new Component(x1, y1, x2, y2, f, st);
            case 521:
                return new TwoDimComponent(x1, y1, x2, y2, f, st);
            case 522:
                return new ZigZagInterface(x1, y1, x2, y2, f, st);
        }
        return null;
    }

    public static CircuitElm constructElement(String n, int x1, int y1) {
        switch (n) {
            case "DiodeElm":
                return new DiodeElm(x1, y1);
            case "SwitchElm":
                return new SwitchElm(x1, y1);
            case "CapacitorElm":
                return new CapacitorElm(x1, y1);
            case "RegulatorElm":
                return new RegulatorElm(x1, y1);
            case "GroundElm":
                return new GroundElm(x1, y1);
            case "ResistorElm":
                return new ResistorElm(x1, y1);
            case "WireElm":
                return new WireElm(x1, y1);
            case "Component":
                return new Component(x1, y1);
            case "2DComponent":
                return new TwoDimComponent(x1, y1);
            case "ZigZagInterface":
                return new ZigZagInterface(x1, y1);
            default:
                return null;
        }
    }


    native boolean weAreInUS(boolean orCanada) /*-{
        try {
            l = navigator.languages ? navigator.languages[0] : (navigator.language || navigator.userLanguage);
            if (l.length > 2) {
                l = l.slice(-2).toUpperCase();
                return (l == "US" || (l == "CA" && orCanada));
            } else {
                return 0;
            }

        } catch (e) {
            return 0;
        }
    }-*/;

    native boolean weAreInGermany() /*-{
        try {
            l = navigator.languages ? navigator.languages[0] : (navigator.language || navigator.userLanguage);
            return (l.toUpperCase().startsWith("DE"));
        } catch (e) {
            return 0;
        }
    }-*/;


    native void printCanvas(CanvasElement cv) /*-{
        var img = cv.toDataURL("image/png");
        var win = window.open("", "print", "height=500,width=500,status=yes,location=no");
        win.document.title = "Print Circuit";
        win.document.open();
        win.document.write('<img src="' + img + '"/>');
        win.document.close();
        setTimeout(function () {
            win.print();
        }, 1000);
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
            g.setColor(Color.white);
        } else {
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
        for (CircuitElm elm : elmList) {
            elm.draw(g);
            if (elm.needsHighlight())
                elm.drawPosts(g);
        }
        for (CircuitElm elm : elmList) {
            if (!elm.needsHighlight())
                elm.drawPosts(g);
        }

        // restore everything
        printableCheckItem.setState(p);
        dotsCheckItem.setState(c);
        transform = oldTransform;
    }


    native JsArray<JavaScriptObject> getJSArray() /*-{
        return [];
    }-*/;

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
            setSimRunning: $entry(function (run) {
                that.@lahde.tccbuilder.client.CirSim::setSimRunning(Z)(run);
            }),
            getTime: $entry(function () {
                return that.@lahde.tccbuilder.client.CirSim::t;
            }),
            getTimeStep: $entry(function () {
                return that.@lahde.tccbuilder.client.CirSim::timeStep;
            }),
            setTimeStep: $entry(function (ts) {
                that.@lahde.tccbuilder.client.CirSim::timeStep = ts;
            }),
            isRunning: $entry(function () {
                return that.@lahde.tccbuilder.client.CirSim::simIsRunning()();
            }),
            getElements: $entry(function () {
                return that.@lahde.tccbuilder.client.CirSim::getJSElements()();
            }),
            getCircuitAsSVG: $entry(function () {
                return that.@lahde.tccbuilder.client.CirSim::doExportAsSVGFromAPI()();
            }),
            exportCircuit: $entry(function () {
                return that.@lahde.tccbuilder.client.CirSim::dumpCircuit()();
            }),
            importCircuit: $entry(function (circuit, subcircuitsOnly) {
                return that.@lahde.tccbuilder.client.CirSim::importCircuitFromText(Ljava/lang/String;Z)(circuit, subcircuitsOnly);
            })
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