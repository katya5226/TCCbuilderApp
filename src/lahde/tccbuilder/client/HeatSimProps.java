package lahde.tccbuilder.client;
@Deprecated
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