package net.jeebiss.spazz.uclassify.results;

public class GenderResult extends ClassifierResult {

    protected Payload cls1;

    public double getFemale() {
        return cls1.female;
    }

    public double getMale() {
        return cls1.male;
    }

    private class Payload {
        protected double female;
        protected double male;
    }
}
