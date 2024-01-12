package lahde.tccbuilder.client.ejmlsparselu;

public interface MatrixSparse extends ReshapeMatrix {
    /**
     * Prints to standard out the non-zero elements only.
     */
    void printNonZero();

    /**
     * Reshapes the matrix so that it can store a matrix with the specified dimensions and the number of
     * non-zero elements. The reshaped matrix will be empty.
     *
     * @param numRows number of rows
     * @param numCols number of columns
     * @param arrayLength Array length for storing non-zero elements.
     */
    void reshape( int numRows , int numCols , int arrayLength );

    /**
     * Changes the number of rows and columns in the matrix. The graph structure is flushed and the matrix will
     * be empty/all zeros. Similar to {@link #reshape(int, int, int)}, but the storage for non-zero elements is
     * not changed
     *
     * @param numRows number of rows
     * @param numCols number of columns
     */
    @Override
    void reshape( int numRows , int numCols );

    /**
     * Reduces the size of internal data structures to their minimal size. No information is lost but the arrays will
     * change
     */
    void shrinkArrays();

    /**
     * If the specified element is non-zero it is removed from the structure
     * @param row the row
     * @param col the column
     */
    void remove( int row , int col );

    /**
     * Is the specified element explicitly assigned a value
     * @param row the row
     * @param col the column
     * @return true if it has been assigned a value or false if not
     */
    boolean isAssigned( int row , int col );

    /**
     * Sets all elements to zero by removing the sparse graph
     */
    @Override
    void zero();

    /**
     * Returns the number of non-zero elements.
     */
    int getNonZeroLength();
}

