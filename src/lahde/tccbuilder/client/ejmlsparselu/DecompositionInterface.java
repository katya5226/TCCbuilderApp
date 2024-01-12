public interface DecompositionInterface <T extends Matrix> {

    /**
     * Computes the decomposition of the input matrix. Depending on the implementation
     * the input matrix might be stored internally or modified. If it is modified then
     * the function {@link #inputModified()} will return true and the matrix should not be
     * modified until the decomposition is no longer needed.
     *
     * @param orig The matrix which is being decomposed. Modification is implementation dependent.
     * @return Returns if it was able to decompose the matrix.
     */
    boolean decompose( T orig );

    /**
     * Checks if the input matrix to {#decompose(org.ejml.data.Matrix)} is modified during
     * the decomposition process.
     *
     * @return true if the input matrix to decompose() is modified.
     */
    boolean inputModified();
}