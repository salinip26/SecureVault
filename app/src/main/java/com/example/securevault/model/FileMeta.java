package com.example.securevault.model;

public class FileMeta {
    private String filePath;

    private String extension;

    private String fileName;
    private String type;
    private double latVal;
    private double longVal;
    private String fromTime;
    private String toTime;

    public FileMeta(String filePath,String fileName,String extension, String type, double latVal, double longVal, String fromTime, String toTime) {
        this.filePath = filePath;
        this.type = type;
        this.latVal = latVal;
        this.longVal = longVal;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.fileName = fileName;
        this.extension = extension;

    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLatVal() {
        return latVal;
    }

    public void setLatVal(double latVal) {
        this.latVal = latVal;
    }

    public double getLongVal() {
        return longVal;
    }

    public void setLongVal(double longVal) {
        this.longVal = longVal;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
