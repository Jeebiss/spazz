package net.jeebiss.spazz.uclassify.results;

public abstract class ClassifierResult {

    protected String version;
    protected boolean success;
    protected int statusCode;
    protected String errorMessage;
    protected double textCoverage;

    public String getVersion() {
        return version;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public double getTextCoverage() {
        return textCoverage;
    }
}
