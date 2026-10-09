package ee.toolrental.aimodule;

public class InvalidToolSearchSqlException extends RuntimeException {

    private static final String SAFE_FEEDBACK =
            "QUERY_SHAPE_INVALID: use only the documented single SELECT query shape and allowed filters.";

    public InvalidToolSearchSqlException() {
        super("Generated SQL is outside the allowed tool search query contract");
    }

    public String safeFeedback() {
        return SAFE_FEEDBACK;
    }
}
