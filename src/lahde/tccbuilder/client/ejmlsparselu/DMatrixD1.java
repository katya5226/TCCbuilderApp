public abstract class DMatrixD1 implements ReshapeMatrix, DMatrix {
    /**
     * Where the raw data for the matrix is stored. The format is type dependent.
     */
    public double[] data = UtilEjml.ZERO_LENGTH_F64;

    /**
     * Number of rows in the matrix.
     */
    public int numRows;
    /**
     * Number of columns in the matrix.
     */
    public int numCols;

    /** Sets the matrix's shape while checking that it's valid */
    protected void assignShape(int rows, int cols) {
        UtilEjml.checkValidMatrixShape(rows, cols);
        this.numRows = rows;
        this.numCols = cols;
    }

    /**
     * Used to get a reference to the internal data.
     *
     * @return Reference to the matrix's data.
     */
    public double[] getData() {
        return data;
    }

    /**
     * Changes the internal array reference.
     */
    public void setData( double[] data ) {
        this.data = data;
    }

    /**
     * Returns the internal array index for the specified row and column.
     *
     * @param row Row index.
     * @param col Column index.
     * @return Internal array index.
     */
    public abstract int getIndex( int row, int col );

    /**
     * Sets the value of this matrix to be the same as the value of the provided matrix. Both
     * matrices must have the same shape:<br>
     * <br>
     * a<sub>ij</sub> = b<sub>ij</sub><br>
     * <br>
     *
     * @param b The matrix that this matrix is to be set equal to.
     */
    public void setTo( DMatrixD1 b ) {
        this.reshape(b.numRows, b.numCols);

        int dataLength = b.getNumElements();

        System.arraycopy(b.data, 0, this.data, 0, dataLength);
    }

    /**
     * Returns the value of the matrix at the specified internal array index. The element at which row and column
     * returned by this function depends upon the matrix's internal structure, e.g. row-major, column-major, or block.
     *
     * @param index Internal array index.
     * @return Value at the specified index.
     */
    public double get( int index ) {
        return data[index];
    }

    /**
     * Sets the element's value at the specified index. The element at which row and column
     * modified by this function depends upon the matrix's internal structure, e.g. row-major, column-major, or block.
     *
     * @param index Index of element that is to be set.
     * @param val The new value of the index.
     */
    public double set( int index, double val ) {
        // See benchmarkFunctionReturn. Pointless return does not degrade performance. Tested on JDK 1.6.0_21
        return data[index] = val;
    }

    /**
     * <p>
     * Adds the specified value to the internal data array at the specified index.<br>
     * <br>
     * Equivalent to: this.data[index] += val;
     * </p>
     *
     * <p>
     * Intended for use in highly optimized code. The  row/column coordinate of the modified element is
     * dependent upon the matrix's internal structure.
     * </p>
     *
     * @param index The index which is being modified.
     * @param val The value that is being added.
     */
    public double plus( int index, double val ) {
        // See benchmarkFunctionReturn. Pointless return does not degrade performance. Tested on JDK 1.6.0_21
        return data[index] += val;
    }

    /**
     * <p>
     * Subtracts the specified value to the internal data array at the specified index.<br>
     * <br>
     * Equivalent to: this.data[index] -= val;
     * </p>
     *
     * <p>
     * Intended for use in highly optimized code. The  row/column coordinate of the modified element is
     * dependent upon the matrix's internal structure.
     * </p>
     *
     * @param index The index which is being modified.
     * @param val The value that is being subtracted.
     */
    public double minus( int index, double val ) {
        // See benchmarkFunctionReturn. Pointless return does not degrade performance. Tested on JDK 1.6.0_21
        return data[index] -= val;
    }

    /**
     * <p>
     * Multiplies the specified value to the internal data array at the specified index.<br>
     * <br>
     * Equivalent to: this.data[index] *= val;
     * </p>
     *
     * <p>
     * Intended for use in highly optimized code. The  row/column coordinate of the modified element is
     * dependent upon the matrix's internal structure.
     * </p>
     *
     * @param index The index which is being modified.
     * @param val The value that is being multiplied.
     */
    public double times( int index, double val ) {
        // See benchmarkFunctionReturn. Pointless return does not degrade performance. Tested on JDK 1.6.0_21
        return data[index] *= val;
    }

    /**
     * <p>
     * Divides the specified value to the internal data array at the specified index.<br>
     * <br>
     * Equivalent to: this.data[index] /= val;
     * </p>
     *
     * <p>
     * Intended for use in highly optimized code. The  row/column coordinate of the modified element is
     * dependent upon the matrix's internal structure.
     * </p>
     *
     * @param index The index which is being modified.
     * @param val The value that is being divided.
     */
    public double div( int index, double val ) {
        // See benchmarkFunctionReturn. Pointless return does not degrade performance. Tested on JDK 1.6.0_21
        return data[index] /= val;
    }

    /**
     * <p>
     * Changes the number of rows and columns in the matrix, allowing its size to grow or shrink.
     * If the saveValues flag is set to true, then the previous values will be maintained, but
     * reassigned to new elements in a row-major ordering. If saveValues is false values will only
     * be maintained when the requested size is less than or equal to the internal array size.
     * The primary use for this function is to encourage data reuse and avoid unnecessarily declaring
     * and initialization of new memory.
     * </p>
     *
     * <p>
     * Examples:<br>
     * [ 1 2 ; 3 4 ] &rarr; reshape( 2 , 3 , true ) = [ 1 2 3 ; 4 0 0 ]<br>
     * [ 1 2 ; 3 4 ] &rarr; reshape( 1 , 2 , true ) = [ 1 2 ]<br>
     * [ 1 2 ; 3 4 ] &rarr; reshape( 1 , 2 , false ) = [ 1 2 ]<br>
     * [ 1 2 ; 3 4 ] &rarr; reshape( 2 , 3 , false ) = [ 0 0 0 ; 0 0 0 ]
     * </p>
     *
     * @param numRows The new number of rows in the matrix.
     * @param numCols The new number of columns in the matrix.
     * @param saveValues If true then the value of each element will be save using a row-major reordering. Typically this should be false.
     */
    public abstract void reshape( int numRows, int numCols, boolean saveValues );

    /**
     * Equivalent to invoking reshape(numRows,numCols,false);
     *
     * @param numRows The new number of rows in the matrix.
     * @param numCols The new number of columns in the matrix.
     */
    @Override
    public void reshape( int numRows, int numCols ) {
        reshape(numRows, numCols, false);
    }

    /**
     * Creates a new iterator for traversing through a submatrix inside this matrix. It can be traversed
     * by row or by column. Range of elements is inclusive, e.g. minRow = 0 and maxRow = 1 will include rows
     * 0 and 1. The iteration starts at (minRow,minCol) and ends at (maxRow,maxCol)
     *
     * @param rowMajor true means it will traverse through the submatrix by row first, false by columns.
     * @param minRow first row it will start at.
     * @param minCol first column it will start at.
     * @param maxRow last row it will stop at.
     * @param maxCol last column it will stop at.
     * @return A new MatrixIterator
     */
    public DMatrixIterator iterator( boolean rowMajor, int minRow, int minCol, int maxRow, int maxCol ) {
        return new DMatrixIterator(this, rowMajor, minRow, minCol, maxRow, maxCol);
    }

    @Override
    public void print() {
        // MatrixIO.printFancy(System.out, this, MatrixIO.DEFAULT_LENGTH);
    }

    @Override
    public void print( String format ) {
        // MatrixIO.print(System.out, this, format);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumRows() {
        return numRows;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumCols() {
        return numCols;
    }

    /**
     * Sets the number of rows.
     *
     * @param numRows Number of rows
     */
    public void setNumRows( int numRows ) {
        this.numRows = numRows;
    }

    /**
     * Sets the number of columns.
     *
     * @param numCols Number of columns
     */
    public void setNumCols( int numCols ) {
        this.numCols = numCols;
    }
}
