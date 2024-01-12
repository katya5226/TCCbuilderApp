package lahde.tccbuilder.client.ejmlsparselu;

import java.io.Serializable;

/**
 * Base interface for all rectangular matrices
 *
 * @author Peter Abeles
 */
public interface Matrix extends Serializable {

    /**
     * Returns the number of rows in this matrix.
     *
     * @return Number of rows.
     */
    int getNumRows();

    /**
     * Returns the number of columns in this matrix.
     *
     * @return Number of columns.
     */
    int getNumCols();

    /**
     * Sets all values inside the matrix to zero
     */
    void zero();

    /**
     * Creates an exact copy of the matrix
     */
    <T extends Matrix> T copy();

    /**
     * Creates a new matrix with the same shape as this matrix
     */
    <T extends Matrix> T createLike();

    /**
     * Creates a new matrix of the same type with the specified shape
     */
    <T extends Matrix> T create( int numRows , int numCols );

    /**
     * Sets this matrix to be identical to the 'original' matrix passed in.
     */
    void setTo( Matrix original );

    /**
     * Prints the matrix to standard out using standard formatting. This is the same as calling print("%e")
     */
    void print();

    /**
     * Prints the matrix to standard out with the specified formatting.
     *
     * @see java.util.Formatter
     * @param format printf style formatting for a float. E.g. "%f"
     */
    void print( String format );

    /**
     * Returns the type of matrix
     */
    MatrixType getType();
}
