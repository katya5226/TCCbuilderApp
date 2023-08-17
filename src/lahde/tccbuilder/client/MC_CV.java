package lahde.tccbuilder.client;

public class MC_CV extends ControlVolume {

    public MC_CV(int index) {
        super(index);
    }

    @Override
    double cp(){
        double cp = 0.0;
        if (this.parent instanceof MCComponent) {
            MCComponent p = (MCComponent)this.parent;
            if (!p.field)
                cp = this.temperature * ModelMethods.linInterp(this.temperature, p.temperaturesST, p.derSTlow);
            if (p.field)
            cp = this.temperature * ModelMethods.linInterp(this.temperature, p.temperaturesST, p.derSThigh);
        }
        return cp;
    }

    public void magnetize() {
        //Window.alert("Field: " + String.valueOf(this.parent.getClass()));
        double s = 0.0;
        double T = 0.0;
        if (this.parent instanceof MCComponent) {
            MCComponent p = (MCComponent)this.parent;
            if (!p.field) {
                s = ModelMethods.linInterp(this.temperature, p.temperaturesST, p.lowFieldEntropies);
                T = ModelMethods.linInterp(s, p.highFieldEntropies, p.temperaturesST);
                this.temperature = T; this.temperature_old = T;
            }
            if (p.field) {
                s = ModelMethods.linInterp(this.temperature, p.temperaturesST, p.highFieldEntropies);
                T = ModelMethods.linInterp(s, p.lowFieldEntropies, p.temperaturesST);
                this.temperature = T; this.temperature_old = T;
            }
        }
    }
}