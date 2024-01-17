package lahde.tccbuilder.client.ejmlsparselu;

import java.util.Arrays;

@SuppressWarnings("NullAway.Init")
public class ColumnCounts_DSCC {

    // See constructor comments
    private final boolean ata;

    // transpose of input matrix
    private final DMatrixSparseCSC At = new DMatrixSparseCSC(1, 1, 1);

    // workspace array
    IGrowArray gw = new IGrowArray();
    int[] w;

    // shape of input matrix
    int m, n; // (row,col)

    //--------------indices in workspace
    int ancestor;
    int maxfirst;  // maxfirst[i] is the largest first[j] seen so far for nonzero a[i,j]
    int prevleaf;  // prevleaf[i] the previously found leaf of subtree i
    int first;     // first[j] is the first descendant of node j in elimination tree.
    //          j = ElimT index. first[j] = post-order index
    //----- Used when ata is true
    int head, next;

    // output from isLeaf()
    int jleaf;

    /**
     * Configures column count algorithm.
     *
     * @param ata flag used to indicate if the cholesky factor of A or A<sup>T</sup>A is to be computed.
     */
    public ColumnCounts_DSCC( boolean ata ) {
        this.ata = ata;
    }

    /**
     * Initializes class data structures and parameters
     */
    void initialize( DMatrixSparseCSC A ) {
        m = A.numRows;
        n = A.numCols;
        int s = 4*n + (ata ? (n + m + 1) : 0);

        gw.reshape(s);
        w = gw.data;

        // compute the transpose of A
        At.reshape(A.numCols, A.numRows, A.nz_length);
        CommonOps_DSCC.transpose(A, At, gw);

        // initialize w
        Arrays.fill(w, 0, s, -1); // assign all values in workspace to -1

        ancestor = 0;
        maxfirst = n;
        prevleaf = 2*n;
        first = 3*n;
    }

    /**
     * Processes and computes column counts of A
     *
     * @param A (Input) Upper triangular matrix
     * @param parent (Input) Elimination tree.
     * @param post (Input) Post order permutation of elimination tree. See {@link TriangularSolver_DSCC#postorder}
     * @param counts (Output) Storage for column counts.
     */
    public void process( DMatrixSparseCSC A, int[] parent, int[] post, int[] counts ) {
        if (counts.length < A.numCols)
            throw new IllegalArgumentException("counts must be at least of length A.numCols");

        initialize(A);

        int[] delta = counts;
        findFirstDescendant(parent, post, delta);

        if (ata) {
            init_ata(post);
        }
        for (int i = 0; i < n; i++)
            w[ancestor + i] = i;

        int[] ATp = At.col_idx;
        int[] ATi = At.nz_rows;

        for (int k = 0; k < n; k++) {
            int j = post[k];
            if (parent[j] != -1)
                delta[parent[j]]--; // j is not a root
            for (int J = HEAD(k, j); J != -1; J = NEXT(J)) {
                for (int p = ATp[J]; p < ATp[J + 1]; p++) {
                    int i = ATi[p];
                    int q = isLeaf(i, j);
                    if (jleaf >= 1)
                        delta[j]++;
                    if (jleaf == 2)
                        delta[q]--;
                }
            }
            if (parent[j] != -1)
                w[ancestor + j] = parent[j];
        }

        // sum up delta's of each child
        for (int j = 0; j < n; j++) {
            if (parent[j] != -1)
                counts[parent[j]] += counts[j];
        }
    }

    void findFirstDescendant( int[] parent, int[] post, int[] delta ) {
        for (int k = 0; k < n; k++) {
            int j = post[k];

            // if j is a leaf, delta[j] = 1
            delta[j] = (w[first + j] == -1) ? 1 : 0;

            // loop while not at the root and the first descendant for the node has not been set
            for (; j != -1 && w[first + j] == -1; j = parent[j]) {
                w[first + j] = k; // remember k is index in post ordered tree
            }
        }
    }

    private int HEAD( int k, int j ) {
        return ata ? w[head + k] : j;
    }

    private int NEXT( int J ) {
        return ata ? w[next + J] : -1;
    }

    private void init_ata( int[] post ) {
        int[] ATp = At.col_idx;
        int[] ATi = At.nz_rows;

        head = 4*n;
        next = 5*n + 1;

        // invert post
        for (int k = 0; k < n; k++) {
            w[post[k]] = k;
        }
        for (int i = 0; i < m; i++) {
            int k, p;
            for (k = n, p = ATp[i]; p < ATp[i + 1]; p++) {
                k = Math.min(k, w[ATi[p]]);
            }
            w[next + i] = w[head + k];
            w[head + k] = i;
        }
    }

    /**
     * <p>Determines if j is a leaf in the ith row subtree of T^t. If it is then it finds the least-common-ancestor
     * of the previously found leaf in T^i (jprev) and node j.</p>
     *
     * <ul>
     * <li>jleaf == 0 then j is not a leaf
     * <li>jleaf == 1 then 1st leaf. returned value = root of ith subtree
     * <li>jleaf == 2 then j is a subsequent leaf. returned value = (jprev,j)
     * </ul>
     *
     * <p>See cs_leaf on page 51</p>
     *
     * @param i Specifies which row subtree in T.
     * @param j node in subtree.
     * @return Depends on jleaf. See above.
     */
    int isLeaf( int i, int j ) {
        jleaf = 0;

        // see j is not a leaf
        if (i <= j || w[first + j] <= w[maxfirst + i])
            return -1;

        w[maxfirst + i] = w[first + j]; // update the max first[j] seen so far
        int jprev = w[prevleaf + i];
        w[prevleaf + i] = j;

        if (jprev == -1) { // j is the first leaf
            jleaf = 1;
            return i;
        } else {           // j is a subsequent leaf
            jleaf = 2;
        }

        // find the common ancestor with jprev
        int q;
        for (q = jprev; q != w[ancestor + q]; q = w[ancestor + q]) {
        }
        // path compression
        for (int s = jprev, sparent; s != q; s = sparent) {
            sparent = w[ancestor + s];
            w[ancestor + s] = q;
        }
        return q;
    }

    int[] getW() {
        return w;
    }
}
