package lahde.tccbuilder.client.ejmlsparselu;

// import lombok.Data;

/**
 * <p>
 * Represents a complex number using 64-bit floating point numbers. A complex number is composed of
 * real and imaginary components.
 * </p>
 */
// @Data
public class Complex_F64 {
    public double real;
    public double imaginary;

    public Complex_F64( double real, double imaginary ) {
        this.real = real;
        this.imaginary = imaginary;
    }

    public Complex_F64() {}

    public double getMagnitude() {
        return Math.sqrt(real*real + imaginary*imaginary);
    }

    public double getMagnitude2() {
        return real*real + imaginary*imaginary;
    }

    public void setTo( double real, double imaginary ) {
        this.real = real;
        this.imaginary = imaginary;
    }

    public void setTo( Complex_F64 src ) {
        this.real = src.real;
        this.imaginary = src.imaginary;
    }

    public boolean isReal() {
        return imaginary == 0.0;
    }

    public Complex_F64 plus( Complex_F64 a ) {
        Complex_F64 ret = new Complex_F64();
        ComplexMath_F64.plus(this, a, ret);
        return ret;
    }

    public Complex_F64 minus( Complex_F64 a ) {
        Complex_F64 ret = new Complex_F64();
        ComplexMath_F64.minus(this, a, ret);
        return ret;
    }

    public Complex_F64 times( Complex_F64 a ) {
        Complex_F64 ret = new Complex_F64();
        ComplexMath_F64.multiply(this, a, ret);
        return ret;
    }

    public Complex_F64 divide( Complex_F64 a ) {
        Complex_F64 ret = new Complex_F64();
        ComplexMath_F64.divide(this, a, ret);
        return ret;
    }

    @Override
    public String toString() {
        if (imaginary == 0) {
            return "" + real;
        } else {
            return real + " " + imaginary + "i";
        }
    }
}
