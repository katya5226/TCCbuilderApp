import lombok.Data;

/**
 * <p>
 * {@link Complex_F64} number in polar notation.<br>
 * z = r*(cos(&theta;) + i*sin(&theta;))<br>
 * where r and &theta; are polar coordinate parameters
 * </p>
 *
 * @author Peter Abeles
 */
@Data
public class ComplexPolar_F64 {
    public double r;
    public double theta;

    public ComplexPolar_F64( double r, double theta ) {
        this.r = r;
        this.theta = theta;
    }

    public ComplexPolar_F64( Complex_F64 n ) {
        ComplexMath_F64.convert(n, this);
    }

    public ComplexPolar_F64() {}

    public Complex_F64 toStandard() {
        Complex_F64 ret = new Complex_F64();
        ComplexMath_F64.convert(this, ret);
        return ret;
    }

    public void setTo( double r, double theta ) {
        this.r = r;
        this.theta = theta;
    }

    public void setTo( ComplexPolar_F64 src ) {
        this.r = src.r;
        this.theta = src.theta;
    }

    @Override
    public String toString() {
        return "( r = " + r + " theta = " + theta + " )";
    }
}
