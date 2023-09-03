package lahde.tccbuilder.client;
@Deprecated
//moved to Simulation abstract class
public class HeatSimProps {

    public enum Property {  // TODO: implement in code
        DENSITY,
        HEATCAPACITY,
        THCONDUCTIVITY,
        EMISSIVITY
    }

    public enum BC {
        ADIABATIC,
        CONSTHEATFLUX,
        CONSTTEMP,
        CONVECTIVE
    }
}