import java.util.Random;

/**
 * @author Peter Abeles
 */
public class FillReductionFactory_DSCC {
    public static final Random rand = new Random(234234);

    /**
     * Returns a method for computing the fill reduce permutations. If null is returned that means no permutations
     * should be done
     * @param type The method
     * @return ComputePermutation or null if no permutations should be applied
     */
    public static @Nullable ComputePermutation<DMatrixSparseCSC> create(FillReducing type ) {
        switch( type ) {
            case NONE:
                return null;

            case RANDOM:
                return new ComputePermutation<>(true, true) {
                    @Override
                    @SuppressWarnings("NullAway") // constructor parameters ensures these are not null
                    public void process(DMatrixSparseCSC m) {
                        prow.reshape(m.numRows);
                        pcol.reshape(m.numCols);
                        fillSequence(prow);
                        fillSequence(pcol);
                        Random _rand;
                        synchronized (rand) {
                            _rand = new Random(rand.nextInt());
                        }
                        UtilEjml.shuffle(prow.data, prow.length, 0, prow.length, _rand);
                        UtilEjml.shuffle(pcol.data, pcol.length, 0, pcol.length, _rand);
                    }
                };

            case IDENTITY:
                return new ComputePermutation<>(true,true) {
                    @Override
                    @SuppressWarnings("NullAway") // constructor parameters ensures these are not null
                    public void process(DMatrixSparseCSC m) {
                        prow.reshape(m.numRows);
                        pcol.reshape(m.numCols);
                        fillSequence(prow);
                        fillSequence(pcol);
                    }
                };

            default:
                throw new RuntimeException("Unknown "+type);
        }
    }

    private static void fillSequence(IGrowArray perm) {
        for (int i = 0; i <perm.length; i++) {
            perm.data[i] = i;
        }
    }
}

