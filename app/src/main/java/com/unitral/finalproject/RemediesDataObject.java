package com.unitral.finalproject;


public class RemediesDataObject {
    private String Disease, Causes, Remedies;

    public RemediesDataObject(String disease, String causes, String remedies) {
        Disease = disease;
        Causes = causes;
        Remedies = remedies;
    }

    public RemediesDataObject() {
    }

    public String getDisease() {
        return Disease;
    }

    public void setDisease(String disease) {
        Disease = disease;
    }

    public String getCauses() {
        return Causes;
    }

    public void setCauses(String causes) {
        Causes = causes;
    }

    public String getRemedies() {
        return Remedies;
    }

    public void setRemedies(String remedies) {
        Remedies = remedies;
    }
}
