public abstract class ComputePermutation<T extends Matrix> {

    protected @Nullable IGrowArray prow;
    protected @Nullable IGrowArray pcol;

    protected ComputePermutation( boolean hasRow, boolean hasCol ) {
        if (hasRow)
            prow = new IGrowArray();
        if (hasCol)
            pcol = new IGrowArray();
    }

    public abstract void process( T m );

    /**
     * Returns row permutation
     */
    public @Nullable IGrowArray getRow() {
        return prow;
    }

    /**
     * Returns column permutation
     */
    public @Nullable IGrowArray getColumn() {
        return pcol;
    }

    public boolean hasRowPermutation() {
        return prow != null;
    }

    public boolean hasColumnPermutation() {
        return pcol != null;
    }
}
