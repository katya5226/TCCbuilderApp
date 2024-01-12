import java.util.Random;
import java.util.Arrays;

public class UtilEjml {
    public static final int[] ZERO_LENGTH_I32 = new int[0];
    public static final double[] ZERO_LENGTH_F64 = new double[0];
    public static double PI = Math.PI;

    public static void checkTooLarge( int rows, int cols ) {
        if ((rows*cols) != ((long)rows*cols))
            throw new IllegalArgumentException("Matrix size exceeds the size of an integer");
    }

    public static void checkValidMatrixShape( int rows, int cols ) {
        if (rows < 0)
            throw new IllegalArgumentException("Rows are negative: value=" + rows);
        if (cols < 0)
            throw new IllegalArgumentException("Cols are negative: value=" + cols);
        checkTooLarge(rows, cols);
    }

    public static void shuffle( int[] list, int N, int start, int end, Random rand ) {
        int range = end - start;
        for (int i = 0; i < range; i++) {
            int selected = rand.nextInt(N - i) + i + start;
            int v = list[i];
            list[i] = list[selected];
            list[selected] = v;
        }
    }

    public static int[] adjust( @Nullable IGrowArray gwork, int desired ) {
        if (gwork == null) gwork = new IGrowArray();
        gwork.reshape(desired);
        return gwork.data;
    }
    public static int[] adjust( @Nullable IGrowArray gwork, int desired, int zeroToM ) {
        int[] w = adjust(gwork, desired);
        Arrays.fill(w, 0, zeroToM, 0);
        return w;
    }

    /**
     * Resizes the array to ensure that it is at least of length desired and returns its internal array
     */
    public static double[] adjust( @Nullable DGrowArray gwork, int desired ) {
        if (gwork == null) gwork = new DGrowArray();
        gwork.reshape(desired);
        return gwork.data;
    }

    public static int[] adjustFill( @Nullable IGrowArray gwork, int desired, int value ) {
        int[] w = adjust(gwork, desired);
        Arrays.fill(w, 0, desired, value);
        return w;
    }

    /**
     * If the input matrix is null a new matrix is created and returned. If it exists it will be reshaped and returned.
     *
     * @param a (Input/Output) matrix which is to be checked. Can be null.
     * @param rows Desired number of rows
     * @param cols Desired number of cols
     * @return modified matrix or new matrix
     */
    public static DMatrixRMaj reshapeOrDeclare( @Nullable DMatrixRMaj a, int rows, int cols ) {
        if (a == null)
            return new DMatrixRMaj(rows, cols);
        else if (a.numRows != rows || a.numCols != cols)
            a.reshape(rows, cols);
        return a;
    }

    /**
     * If the input matrix is null a new matrix is created and returned. If it exists it will be reshaped and returned.
     *
     * @param a (Input/Output) matrix which is to be checked. Can be null.
     * @param rows Desired number of rows
     * @param cols Desired number of cols
     * @return modified matrix or new matrix
     */

    public static DMatrixSparseCSC reshapeOrDeclare( @Nullable DMatrixSparseCSC target, int rows, int cols, int nz_length ) {
        if (target == null)
            return new DMatrixSparseCSC(rows, cols, nz_length);
        else
            target.reshape(rows, cols, nz_length);
        return target;
    }

    public static int[] adjustClear( @Nullable IGrowArray gwork, int desired ) {
        return adjust(gwork, desired, desired);
    }

    public static String stringShapes( Matrix A, Matrix B ) {
        return "( " + A.getNumRows() + "x" + A.getNumCols() + " ) " +
                "( " + B.getNumRows() + "x" + B.getNumCols() + " )";
    }

    public static int permutationSign( int[] p, int N, int[] work ) {
        System.arraycopy(p, 0, work, 0, N);
        p = work;
        int cnt = 0;
        for (int i = 0; i < N; ++i) {
            while (i != p[i]) {
                ++cnt;
                int tmp = p[i];
                p[i] = p[p[i]];
                p[tmp] = tmp;
            }
        }
        return cnt%2 == 0 ? 1 : -1;
    }

    public static int[] pivotVector( int[] pivots, int length, @Nullable IGrowArray storage ) {
        if (storage == null) storage = new IGrowArray();
        storage.reshape(length);
        System.arraycopy(pivots, 0, storage.data, 0, length);
        return storage.data;
    }

    /**
     * Checks the size of inputs to the standard size function. Throws exception if B is incorrect. Reshapes X.
     *
     * @param numRowsA Number of rows in A matrix
     * @param numColsA Number of columns in A matrix
     */
    public static void checkReshapeSolve( int numRowsA, int numColsA, ReshapeMatrix B, ReshapeMatrix X ) {
        if (B.getNumRows() != numRowsA)
            throw new IllegalArgumentException("Unexpected number of rows in B based on shape of A. Found=" +
                    B.getNumRows() + " Expected=" + numRowsA);
        X.reshape(numColsA, B.getNumCols());
    }


}
