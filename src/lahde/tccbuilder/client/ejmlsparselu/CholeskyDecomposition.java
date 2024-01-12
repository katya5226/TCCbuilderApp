package lahde.tccbuilder.client.ejmlsparselu;

public interface CholeskyDecomposition<MatrixType extends Matrix>
        extends DecompositionInterface<MatrixType> {

    /**
     * If true the decomposition was for a lower triangular matrix.
     * If false it was for an upper triangular matrix.
     *
     * @return True if lower, false if upper.
     */
    boolean isLower();

    /**
     * <p>
     * Returns the triangular matrix from the decomposition.
     * </p>
     *
     * <p>
     * If an input is provided that matrix is used to write the results to.
     * Otherwise a new matrix is created and the results written to it.
     * </p>
     *
     * @param T If not null then the decomposed matrix is written here.
     * @return A lower or upper triangular matrix.
     */
    MatrixType getT(@Nullable MatrixType T);
}
