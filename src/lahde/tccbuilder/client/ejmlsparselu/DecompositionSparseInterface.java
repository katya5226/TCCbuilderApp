package lahde.tccbuilder.client.ejmlsparselu;

public interface DecompositionSparseInterface<T extends Matrix> extends
        DecompositionInterface<T>
{
    /**
     * <p>Save results from structural analysis step. This can reduce computations if a matrix with the exactly same
     * non-zero pattern is decomposed in the future. If a matrix has yet to be processed then the structure of
     * the next matrix is saved. If a matrix has already been processed then the structure of the most recently
     * processed matrix will be saved.</p>
     */
    void setStructureLocked( boolean lock );

    /**
     * Checks to see if the structure is locked.
     * @return true if locked or false if not locked.
     */
    boolean isStructureLocked();
}
