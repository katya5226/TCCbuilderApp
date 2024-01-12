package lahde.tccbuilder.client.ejmlsparselu;

@FunctionalInterface
public interface IPredicateBinary {
    // TODO: add a version, where the value is also relevant f.i. for selecting non-zero entries
    boolean apply( int row, int col );
}
