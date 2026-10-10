package ee.toolrental.aimodule;

public class UnsupportedToolSearchQuestionException extends RuntimeException {

    public UnsupportedToolSearchQuestionException() {
        super("Saan aidata vaid tööriistade otsinguga.");
    }
}
