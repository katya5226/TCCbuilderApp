public interface LinearSolverSparse<S extends Matrix, D extends Matrix> extends LinearSolver<S, D> {

    /**
     * Solve against sparse matrices. A*X=B. In most situations its more desirable to solve against
     * a dense matrix because of fill in.
     *
     * @param B Input. Never modified.
     * @param X Output. Never modified.
     */
    void solveSparse( S B, S X );

    /**
     * <p>Save results from structural analysis step. This can reduce computations of a matrix with the exactly same
     * non-zero pattern is decomposed in the future. If a matrix has yet to be processed then the structure of
     * the next matrix is saved. If a matrix has already been processed then the structure of the most recently
     * processed matrix will be saved.</p>
     */
    void setStructureLocked( boolean locked );

    /**
     * Checks to see if the structure is locked.
     *
     * @return true if locked or false if not locked.
     */
    boolean isStructureLocked();
}
