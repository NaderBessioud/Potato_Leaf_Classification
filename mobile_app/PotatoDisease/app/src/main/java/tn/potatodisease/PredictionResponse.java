package tn.potatodisease;

public class PredictionResponse {

    private String class_predicted;
    private String confidence;


    public String getClass_predicted() {
        return class_predicted;
    }

    public void setClass_predicted(String class_predicted) {
        this.class_predicted = class_predicted;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }
}

