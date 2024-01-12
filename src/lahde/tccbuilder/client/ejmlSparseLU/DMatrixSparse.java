import java.util.Iterator;

/**
 * High level interface for sparse matrices double types.
 *
 * @author Peter Abeles
 */
public interface DMatrixSparse extends DMatrix, MatrixSparse {

    /**
     * Returns the value of value of the specified matrix element.
     *
     * @param row           Matrix element's row index..
     * @param col           Matrix element's column index.
     * @param fallBackValue Value to return, if the matrix element is not assigned
     * @return The specified element's value.
     */
    double get(int row, int col, double fallBackValue);

    /**
     * Same as {@link #get} but does not perform bounds check on input parameters. This results in about a 25%
     * speed increase but potentially sacrifices stability and makes it more difficult to track down simple errors.
     * It is not recommended that this function be used, except in highly optimized code where the bounds are
     * implicitly being checked.
     *
     * @param row           Matrix element's row index..
     * @param col           Matrix element's column index.
     * @param fallBackValue Value to return, if the matrix element is not assigned
     * @return The specified element's value or the fallBackValue, if the element is not assigned.
     */
    double unsafe_get(int row, int col, double fallBackValue);

    /**
     * Creates an iterator which will go through each non-zero value in the sparse matrix. Order is not defined
     * and is implementation specific
     */
    Iterator<CoordinateRealValue> createCoordinateIterator();

    /**
     * Value of an element in a sparse matrix
     */
    class CoordinateRealValue {
        /** The coordinate */
        public int row,col;
        /** The value of the coordinate */
        public double value;
    }
}

