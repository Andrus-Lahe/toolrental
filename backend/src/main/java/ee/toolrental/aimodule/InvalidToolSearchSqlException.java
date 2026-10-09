package ee.toolrental.aimodule;

public class InvalidToolSearchSqlException extends RuntimeException {

    public InvalidToolSearchSqlException() {
        super("Generated SQL is outside the allowed tool search query contract");
    }
}
