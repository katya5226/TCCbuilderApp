package lahde.tccbuilder.client.ejmlsparselu;

import lahde.tccbuilder.client.ejmlsparselu.LinearSolverFactory_DSCC;
import lahde.tccbuilder.client.ejmlsparselu.LinearSolverSparse;
import lahde.tccbuilder.client.ejmlsparselu.LinearSolver;


public class CommonOps_DSCC {
    /**
     * <p>
     * Solves for x in the following equation:<br>
     * <br>
     * A*x = b
     * </p>
     *
     * <p>
     * If the system could not be solved then false is returned. If it returns true
     * that just means the algorithm finished operating, but the results could still be bad
     * because 'A' is singular or nearly singular.
     * </p>
     *
     * <p>
     * If repeat calls to solve are being made then one should consider using {@link LinearSolverFactory_DSCC}
     * instead.
     * </p>
     *
     * <p>
     * It is ok for 'b' and 'x' to be the same matrix.
     * </p>
     *
     * @param a (Input) A matrix that is m by n. Not modified.
     * @param b (Input) A matrix that is n by k. Not modified.
     * @param x (Output) A matrix that is m by k. Modified.
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean solve( DMatrixSparseCSC a,
                                 DMatrixRMaj b,
                                 DMatrixRMaj x ) {
        x.reshape(a.numCols, b.numCols);
        LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver;
        solver = LinearSolverFactory_DSCC.lu(FillReducing.NONE);  // Katni
//        if (a.numRows > a.numCols) {
//            solver = LinearSolverFactory_DSCC.qr(FillReducing.NONE);// todo specify a filling that makes sense
//        } else {
//            solver = LinearSolverFactory_DSCC.lu(FillReducing.NONE);
//        }

        // Ensure that the input isn't modified
        if (solver.modifiesA())
            a = a.copy();

        if (solver.modifiesB())
            b = b.copy();

        // decompose then solve the matrix
        if (!solver.setA(a))
            return false;

        solver.solve(b, x);
        return true;
    }

    /**
     * <p>
     * Solves for x in the following equation:<br>
     * <br>
     * A*x = b
     * </p>
     *
     * <p>
     * If the system could not be solved then false is returned. If it returns true
     * that just means the algorithm finished operating, but the results could still be bad
     * because 'A' is singular or nearly singular.
     * </p>
     *
     * <p>
     * If repeat calls to solve are being made then one should consider using {@link LinearSolverFactory_DSCC}
     * instead.
     * </p>
     *
     * <p>
     * It is ok for 'b' and 'x' to be the same matrix.
     * </p>
     *
     * @param a (Input) A matrix that is m by n. Not modified.
     * @param b (Input) A matrix that is n by k. Not modified.
     * @param x (Output) A matrix that is m by k. Modified.
     * @return true if it could invert the matrix false if it could not.
     */
    public static boolean solve( DMatrixSparseCSC a,
                                 DMatrixSparseCSC b,
                                 DMatrixSparseCSC x ) {
        x.reshape(a.numCols, b.numCols);
        LinearSolverSparse<DMatrixSparseCSC, DMatrixRMaj> solver;
        solver = LinearSolverFactory_DSCC.lu(FillReducing.NONE);  // Katni
//        if (a.numRows > a.numCols) {
//            solver = LinearSolverFactory_DSCC.qr(FillReducing.NONE);// todo specify a filling that makes sense
//        } else {
//            solver = LinearSolverFactory_DSCC.lu(FillReducing.NONE);
//        }

        // Ensure that the input isn't modified
        if (solver.modifiesA())
            a = a.copy();

        if (solver.modifiesB())
            b = b.copy();

        // decompose then solve the matrix
        if (!solver.setA(a))
            return false;

        solver.solveSparse(b, x);
        return true;
    }

    /**
     * Perform matrix transpose
     *
     * @param A Input matrix. Not modified
     * @param A_t Storage for transpose of 'a'. Must be correct shape. data length might be adjusted.
     * @param gw (Optional) Storage for internal workspace. Can be null.
     * @return The transposed matrix
     */
    public static DMatrixSparseCSC transpose( DMatrixSparseCSC A, @Nullable DMatrixSparseCSC A_t, @Nullable IGrowArray gw ) {
        A_t = UtilEjml.reshapeOrDeclare(A_t, A.numCols, A.numRows, A.nz_length);
        ImplCommonOps_DSCC.transpose(A, A_t, gw);
        return A_t;
    }

    /**
     * Computes the inverse permutation vector
     *
     * @param original Original permutation vector
     * @param inverse It's inverse
     */
    public static void permutationInverse( int[] original, int[] inverse, int length ) {
        for (int i = 0; i < length; i++) {
            inverse[original[i]] = i;
        }
    }

    /**
     * Permutes a vector in the inverse. output[perm[k]] = input[k]
     *
     * @param perm (Input) permutation vector
     * @param input (Input) Vector which is to be permuted
     * @param output (Output) Where the permuted vector is stored.
     * @param N Number of elements in the vector.
     */
    public static void permuteInv( int[] perm, double[] input, double[] output, int N ) {
        for (int k = 0; k < N; k++) {
            output[perm[k]] = input[k];
        }
    }

    /**
     * Applies the permutation to upper triangular symmetric matrices. Typically a symmetric matrix only stores the
     * upper triangular part, so normal permutation will have undesirable results, e.g. the zeros will get mixed
     * in and will no longer be symmetric. This algorithm will handle the implicit lower triangular and construct
     * new upper triangular matrix.
     *
     * <p>See page cs_symperm() on Page 22 of "Direct Methods for Sparse Linear Systems"</p>
     *
     * @param input (Input) Upper triangular symmetric matrix which is to be permuted.
     * Entries below the diagonal are ignored.
     * @param permInv (Input) Inverse permutation vector. Specifies new order of the rows and columns.
     * @param output (Output) Upper triangular symmetric matrix which has the permutation stored in it. Reshaped.
     * @param gw (Optional) Storage for internal workspace. Can be null.
     */
    public static void permuteSymmetric( DMatrixSparseCSC input, int[] permInv, DMatrixSparseCSC output,
                                         @Nullable IGrowArray gw ) {
        if (input.numRows != input.numCols)
            throw new MatrixDimensionException("Input must be a square matrix. " + UtilEjml.stringShapes(input, output));
        if (input.numRows != permInv.length)
            throw new MatrixDimensionException("Number of column in input must match length of permInv");

        int N = input.numCols;

        int[] w = UtilEjml.adjustClear(gw, N); // histogram with column counts

        output.reshape(N, N, 0);
        output.indicesSorted = false;
        output.col_idx[0] = 0;

        // determine column counts for output
        for (int j = 0; j < N; j++) {
            int j2 = permInv[j];
            int idx0 = input.col_idx[j];
            int idx1 = input.col_idx[j + 1];

            for (int p = idx0; p < idx1; p++) {
                int i = input.nz_rows[p];
                if (i > j) // ignore the lower triangular portion
                    continue;
                int i2 = permInv[i];

                w[i2 > j2 ? i2 : j2]++;
            }
        }

        // update structure of output
        output.histogramToStructure(w);
        System.arraycopy(output.col_idx, 0, w, 0, output.numCols);

        for (int j = 0; j < N; j++) {
            // column j of Input is row j2 of Output
            int j2 = permInv[j];
            int idx0 = input.col_idx[j];
            int idx1 = input.col_idx[j + 1];

            for (int p = idx0; p < idx1; p++) {
                int i = input.nz_rows[p];
                if (i > j) // ignore the lower triangular portion
                    continue;

                int i2 = permInv[i];
                // row i of Input is row i2 of Output
                int q = w[i2 > j2 ? i2 : j2]++;
                output.nz_rows[q] = i2 < j2 ? i2 : j2;
                output.nz_values[q] = input.nz_values[p];
            }
        }
    }

    /**
     * Applies the row permutation specified by the vector to the input matrix and save the results
     * in the output matrix. output[perm[j],:] = input[j,:]
     *
     * @param permInv (Input) Inverse permutation vector. Specifies new order of the rows.
     * @param input (Input) Matrix which is to be permuted
     * @param output (Output) Matrix which has the permutation stored in it. Is reshaped.
     */
    public static void permuteRowInv( int[] permInv, DMatrixSparseCSC input, DMatrixSparseCSC output ) {
        if (input.numRows > permInv.length)
            throw new IllegalArgumentException("permutation vector must have at least as many elements as input has rows");

        output.reshape(input.numRows, input.numCols, input.nz_length);
        output.nz_length = input.nz_length;
        output.indicesSorted = false;

        System.arraycopy(input.nz_values, 0, output.nz_values, 0, input.nz_length);
        System.arraycopy(input.col_idx, 0, output.col_idx, 0, input.numCols + 1);

        int idx0 = 0;
        for (int i = 0; i < input.numCols; i++) {
            int idx1 = output.col_idx[i + 1];

            for (int j = idx0; j < idx1; j++) {
                output.nz_rows[j] = permInv[input.nz_rows[j]];
            }
            idx0 = idx1;
        }
    }

    /**
     * Converts the permutation vector into a matrix. B = P*A. B[p[i],:] = A[i,:]
     *
     * @param p (Input) Permutation vector
     * @param inverse (Input) If it is the inverse. B[i,:] = A[p[i],:)
     * @param P (Output) Permutation matrix
     */
    public static DMatrixSparseCSC permutationMatrix( int[] p, boolean inverse, int N,
                                                      @Nullable DMatrixSparseCSC P ) {

        if (P == null)
            P = new DMatrixSparseCSC(N, N, N);
        else
            P.reshape(N, N, N);
        P.indicesSorted = true;
        P.nz_length = N;

        // each column should have one element inside of it
        if (!inverse) {
            for (int i = 0; i < N; i++) {
                P.col_idx[i + 1] = i + 1;
                P.nz_rows[p[i]] = i;
                P.nz_values[i] = 1;
            }
        } else {
            for (int i = 0; i < N; i++) {
                P.col_idx[i + 1] = i + 1;
                P.nz_rows[i] = p[i];
                P.nz_values[i] = 1;
            }
        }

        return P;
    }

    /**
     * Applies the forward column and inverse row permutation specified by the two vector to the input matrix
     * and save the results in the output matrix. output[permRow[j],permCol[i]] = input[j,i]
     *
     * @param permRowInv (Input) Inverse row permutation vector. Null is the same as passing in identity.
     * @param input (Input) Matrix which is to be permuted
     * @param permCol (Input) Column permutation vector. Null is the same as passing in identity.
     * @param output (Output) Matrix which has the permutation stored in it. Is reshaped.
     */
    public static void permute( @Nullable int[] permRowInv, DMatrixSparseCSC input, @Nullable int[] permCol,
                                DMatrixSparseCSC output ) {
        if (permRowInv != null && input.numRows > permRowInv.length)
            throw new IllegalArgumentException("rowInv permutation vector must have at least as many elements as input has columns");
        if (permCol != null && input.numCols > permCol.length)
            throw new IllegalArgumentException("permCol permutation vector must have at least as many elements as input has rows");

        output.reshape(input.numRows, input.numCols, input.nz_length);
        output.indicesSorted = false;
        output.nz_length = input.nz_length;

        int N = input.numCols;

        // traverse through in order for the output columns
        int outputNZ = 0;
        for (int i = 0; i < N; i++) {
            int inputCol = permCol != null ? permCol[i] : i; // column of input to source from
            int inputNZ = input.col_idx[inputCol];
            int total = input.col_idx[inputCol + 1] - inputNZ; // total nz in this column

            output.col_idx[i + 1] = output.col_idx[i] + total;

            for (int j = 0; j < total; j++) {
                int row = input.nz_rows[inputNZ];
                output.nz_rows[outputNZ] = permRowInv != null ? permRowInv[row] : row;
                output.nz_values[outputNZ++] = input.nz_values[inputNZ++];
            }
        }
    }

    /**
     * Permutes a vector. output[i] = input[perm[i]]
     *
     * @param perm (Input) permutation vector
     * @param input (Input) Vector which is to be permuted
     * @param output (Output) Where the permuted vector is stored.
     * @param N Number of elements in the vector.
     */
    public static void permute( int[] perm, double[] input, double[] output, int N ) {
        for (int k = 0; k < N; k++) {
            output[k] = input[perm[k]];
        }
    }


}
