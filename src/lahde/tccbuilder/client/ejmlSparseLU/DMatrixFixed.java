public interface DMatrixFixed extends DMatrix {
    @Override
    default <T extends Matrix> T create(int numRows, int numCols) {
        if( numRows == getNumRows() && numCols == getNumCols() )
            return createLike();
        throw new RuntimeException("Fixed sized matrices can't be used to create matrices of arbitrary shape");
    }
}
