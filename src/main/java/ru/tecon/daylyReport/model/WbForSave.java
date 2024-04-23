package ru.tecon.daylyReport.model;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.Serializable;
import java.util.StringJoiner;

public class WbForSave implements Serializable {

    private SXSSFWorkbook w;
    private String crash;

    public WbForSave(SXSSFWorkbook w, String crash) {
        this.w = w;
        this.crash = crash;
    }

    public SXSSFWorkbook getW() {
        return w;
    }

    public void setW(SXSSFWorkbook w) {
        this.w = w;
    }

    public String getCrash() {
        return crash;
    }

    public void setCrash(String crash) {
        this.crash = crash;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", WbForSave.class.getSimpleName() + "[", "]")
                .add("w=" + w)
                .add("crash='" + crash + "'")
                .toString();
    }
}
