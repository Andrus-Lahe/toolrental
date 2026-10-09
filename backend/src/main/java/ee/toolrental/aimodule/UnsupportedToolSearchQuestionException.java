package ee.toolrental.aimodule;

public class UnsupportedToolSearchQuestionException extends RuntimeException {

    public UnsupportedToolSearchQuestionException() {
        super("Saan vastata ainult saadaval tööriistade otsimise küsimustele.");
    }
}
