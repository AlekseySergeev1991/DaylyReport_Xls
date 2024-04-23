package ru.tecon.daylyReport.model;

import java.io.Serializable;
import java.util.StringJoiner;

public class ObjectParams implements Serializable {

    private String objName;
    private String objAddress;
    private String crashTime;
    private String tnv;
    private String tnvColor;
    private String tnvTime;
    private String t1;
    private String t1Color;
    private String t1Time;
    private String t2;
    private String t2Color;
    private String t2Time;
    private String p1;
    private String p1Color;
    private String p1Time;
    private String p2;
    private String p2Color;
    private String p2Time;
    private String t3;
    private String t3Color;
    private String t3Time;
    private String t4;
    private String t4Color;
    private String t4Time;
    private String p3;
    private String p3Color;
    private String p3Time;
    private String p4;
    private String p4Color;
    private String p4Time;
    private String t7;
    private String t7Color;
    private String t7Time;
    private String t7_2;
    private String t7_2Color;
    private String t7_2Time;
    private String p7;
    private String p7Color;
    private String p7Time;
    private String p7_2;
    private String p7_2Color;
    private String p7_2Time;
    private String t13;
    private String t13Color;
    private String t13Time;
    private String t13_2;
    private String t13_2Color;
    private String t13_2Time;
    private String p13;
    private String p13Color;
    private String p13Time;
    private String p13_2;
    private String p13_2Color;
    private String p13_2Time;
    private String pg;
    private String pgColor;
    private String pgTime;
    private String ph;
    private String phColor;
    private String phTime;
    private String ph_2;
    private String ph_2Color;
    private String ph_2Time;
    private String qts;
    private String qtsColor;
    private String qtsTime;
    private String gp;
    private String gpColor;
    private String gpTime;
    private String gee;
    private String geeColor;
    private String geeTime;
    private String geh;
    private String gehColor;
    private String gehTime;
    private String v8;
    private String v8Color;
    private String v8Time;
    private String comment;
    private String fileName;
    private String file;

    public ObjectParams() {
    }

    public ObjectParams(String objName, String objAddress, String crashTime, String tnv, String tnvColor, String tnvTime, String t1, String t1Color, String t1Time, String t2, String t2Color, String t2Time, String p1, String p1Color, String p1Time, String p2, String p2Color, String p2Time, String t3, String t3Color, String t3Time, String t4, String t4Color, String t4Time, String p3, String p3Color, String p3Time, String p4, String p4Color, String p4Time, String t7, String t7Color, String t7Time, String t7_2, String t7_2Color, String t7_2Time, String p7, String p7Color, String p7Time, String p7_2, String p7_2Color, String p7_2Time, String t13, String t13Color, String t13Time, String t13_2, String t13_2Color, String t13_2Time, String p13, String p13Color, String p13Time, String p13_2, String p13_2Color, String p13_2Time, String pg, String pgColor, String pgTime, String ph, String phColor, String phTime, String ph_2, String ph_2Color, String ph_2Time, String qts, String qtsColor, String qtsTime, String gp, String gpColor, String gpTime, String gee, String geeColor, String geeTime, String geh, String gehColor, String gehTime, String v8, String v8Color, String v8Time) {
        this.objName = objName;
        this.objAddress = objAddress;
        this.crashTime = crashTime;
        this.tnv = tnv;
        this.tnvColor = tnvColor;
        this.tnvTime = tnvTime;
        this.t1 = t1;
        this.t1Color = t1Color;
        this.t1Time = t1Time;
        this.t2 = t2;
        this.t2Color = t2Color;
        this.t2Time = t2Time;
        this.p1 = p1;
        this.p1Color = p1Color;
        this.p1Time = p1Time;
        this.p2 = p2;
        this.p2Color = p2Color;
        this.p2Time = p2Time;
        this.t3 = t3;
        this.t3Color = t3Color;
        this.t3Time = t3Time;
        this.t4 = t4;
        this.t4Color = t4Color;
        this.t4Time = t4Time;
        this.p3 = p3;
        this.p3Color = p3Color;
        this.p3Time = p3Time;
        this.p4 = p4;
        this.p4Color = p4Color;
        this.p4Time = p4Time;
        this.t7 = t7;
        this.t7Color = t7Color;
        this.t7Time = t7Time;
        this.t7_2 = t7_2;
        this.t7_2Color = t7_2Color;
        this.t7_2Time = t7_2Time;
        this.p7 = p7;
        this.p7Color = p7Color;
        this.p7Time = p7Time;
        this.p7_2 = p7_2;
        this.p7_2Color = p7_2Color;
        this.p7_2Time = p7_2Time;
        this.t13 = t13;
        this.t13Color = t13Color;
        this.t13Time = t13Time;
        this.t13_2 = t13_2;
        this.t13_2Color = t13_2Color;
        this.t13_2Time = t13_2Time;
        this.p13 = p13;
        this.p13Color = p13Color;
        this.p13Time = p13Time;
        this.p13_2 = p13_2;
        this.p13_2Color = p13_2Color;
        this.p13_2Time = p13_2Time;
        this.pg = pg;
        this.pgColor = pgColor;
        this.pgTime = pgTime;
        this.ph = ph;
        this.phColor = phColor;
        this.phTime = phTime;
        this.ph_2 = ph_2;
        this.ph_2Color = ph_2Color;
        this.ph_2Time = ph_2Time;
        this.qts = qts;
        this.qtsColor = qtsColor;
        this.qtsTime = qtsTime;
        this.gp = gp;
        this.gpColor = gpColor;
        this.gpTime = gpTime;
        this.gee = gee;
        this.geeColor = geeColor;
        this.geeTime = geeTime;
        this.geh = geh;
        this.gehColor = gehColor;
        this.gehTime = gehTime;
        this.v8 = v8;
        this.v8Color = v8Color;
        this.v8Time = v8Time;
    }

    public String getObjName() {
        return objName;
    }

    public void setObjName(String objName) {
        this.objName = objName;
    }

    public String getObjAddress() {
        return objAddress;
    }

    public void setObjAddress(String objAddress) {
        this.objAddress = objAddress;
    }

    public String getCrashTime() {
        return crashTime;
    }

    public void setCrashTime(String crashTime) {
        this.crashTime = crashTime;
    }

    public String getTnv() {
        return tnv;
    }

    public void setTnv(String tnv) {
        this.tnv = tnv;
    }

    public String getTnvColor() {
        return tnvColor;
    }

    public void setTnvColor(String tnvColor) {
        this.tnvColor = tnvColor;
    }

    public String getTnvTime() {
        return tnvTime;
    }

    public void setTnvTime(String tnvTime) {
        this.tnvTime = tnvTime;
    }

    public String getT1() {
        return t1;
    }

    public void setT1(String t1) {
        this.t1 = t1;
    }

    public String getT1Color() {
        return t1Color;
    }

    public void setT1Color(String t1Color) {
        this.t1Color = t1Color;
    }

    public String getT1Time() {
        return t1Time;
    }

    public void setT1Time(String t1Time) {
        this.t1Time = t1Time;
    }

    public String getT2() {
        return t2;
    }

    public void setT2(String t2) {
        this.t2 = t2;
    }

    public String getT2Color() {
        return t2Color;
    }

    public void setT2Color(String t2Color) {
        this.t2Color = t2Color;
    }

    public String getT2Time() {
        return t2Time;
    }

    public void setT2Time(String t2Time) {
        this.t2Time = t2Time;
    }

    public String getP1() {
        return p1;
    }

    public void setP1(String p1) {
        this.p1 = p1;
    }

    public String getP1Color() {
        return p1Color;
    }

    public void setP1Color(String p1Color) {
        this.p1Color = p1Color;
    }

    public String getP1Time() {
        return p1Time;
    }

    public void setP1Time(String p1Time) {
        this.p1Time = p1Time;
    }

    public String getP2() {
        return p2;
    }

    public void setP2(String p2) {
        this.p2 = p2;
    }

    public String getP2Color() {
        return p2Color;
    }

    public void setP2Color(String p2Color) {
        this.p2Color = p2Color;
    }

    public String getP2Time() {
        return p2Time;
    }

    public void setP2Time(String p2Time) {
        this.p2Time = p2Time;
    }

    public String getT3() {
        return t3;
    }

    public void setT3(String t3) {
        this.t3 = t3;
    }

    public String getT3Color() {
        return t3Color;
    }

    public void setT3Color(String t3Color) {
        this.t3Color = t3Color;
    }

    public String getT3Time() {
        return t3Time;
    }

    public void setT3Time(String t3Time) {
        this.t3Time = t3Time;
    }

    public String getT4() {
        return t4;
    }

    public void setT4(String t4) {
        this.t4 = t4;
    }

    public String getT4Color() {
        return t4Color;
    }

    public void setT4Color(String t4Color) {
        this.t4Color = t4Color;
    }

    public String getT4Time() {
        return t4Time;
    }

    public void setT4Time(String t4Time) {
        this.t4Time = t4Time;
    }

    public String getP3() {
        return p3;
    }

    public void setP3(String p3) {
        this.p3 = p3;
    }

    public String getP3Color() {
        return p3Color;
    }

    public void setP3Color(String p3Color) {
        this.p3Color = p3Color;
    }

    public String getP3Time() {
        return p3Time;
    }

    public void setP3Time(String p3Time) {
        this.p3Time = p3Time;
    }

    public String getP4() {
        return p4;
    }

    public void setP4(String p4) {
        this.p4 = p4;
    }

    public String getP4Color() {
        return p4Color;
    }

    public void setP4Color(String p4Color) {
        this.p4Color = p4Color;
    }

    public String getP4Time() {
        return p4Time;
    }

    public void setP4Time(String p4Time) {
        this.p4Time = p4Time;
    }

    public String getT7() {
        return t7;
    }

    public void setT7(String t7) {
        this.t7 = t7;
    }

    public String getT7Color() {
        return t7Color;
    }

    public void setT7Color(String t7Color) {
        this.t7Color = t7Color;
    }

    public String getT7Time() {
        return t7Time;
    }

    public void setT7Time(String t7Time) {
        this.t7Time = t7Time;
    }

    public String getT7_2() {
        return t7_2;
    }

    public void setT7_2(String t7_2) {
        this.t7_2 = t7_2;
    }

    public String getT7_2Color() {
        return t7_2Color;
    }

    public void setT7_2Color(String t7_2Color) {
        this.t7_2Color = t7_2Color;
    }

    public String getT7_2Time() {
        return t7_2Time;
    }

    public void setT7_2Time(String t7_2Time) {
        this.t7_2Time = t7_2Time;
    }

    public String getP7() {
        return p7;
    }

    public void setP7(String p7) {
        this.p7 = p7;
    }

    public String getP7Color() {
        return p7Color;
    }

    public void setP7Color(String p7Color) {
        this.p7Color = p7Color;
    }

    public String getP7Time() {
        return p7Time;
    }

    public void setP7Time(String p7Time) {
        this.p7Time = p7Time;
    }

    public String getP7_2() {
        return p7_2;
    }

    public void setP7_2(String p7_2) {
        this.p7_2 = p7_2;
    }

    public String getP7_2Color() {
        return p7_2Color;
    }

    public void setP7_2Color(String p7_2Color) {
        this.p7_2Color = p7_2Color;
    }

    public String getP7_2Time() {
        return p7_2Time;
    }

    public void setP7_2Time(String p7_2Time) {
        this.p7_2Time = p7_2Time;
    }

    public String getT13() {
        return t13;
    }

    public void setT13(String t13) {
        this.t13 = t13;
    }

    public String getT13Color() {
        return t13Color;
    }

    public void setT13Color(String t13Color) {
        this.t13Color = t13Color;
    }

    public String getT13Time() {
        return t13Time;
    }

    public void setT13Time(String t13Time) {
        this.t13Time = t13Time;
    }

    public String getT13_2() {
        return t13_2;
    }

    public void setT13_2(String t13_2) {
        this.t13_2 = t13_2;
    }

    public String getT13_2Color() {
        return t13_2Color;
    }

    public void setT13_2Color(String t13_2Color) {
        this.t13_2Color = t13_2Color;
    }

    public String getT13_2Time() {
        return t13_2Time;
    }

    public void setT13_2Time(String t13_2Time) {
        this.t13_2Time = t13_2Time;
    }

    public String getP13() {
        return p13;
    }

    public void setP13(String p13) {
        this.p13 = p13;
    }

    public String getP13Color() {
        return p13Color;
    }

    public void setP13Color(String p13Color) {
        this.p13Color = p13Color;
    }

    public String getP13Time() {
        return p13Time;
    }

    public void setP13Time(String p13Time) {
        this.p13Time = p13Time;
    }

    public String getP13_2() {
        return p13_2;
    }

    public void setP13_2(String p13_2) {
        this.p13_2 = p13_2;
    }

    public String getP13_2Color() {
        return p13_2Color;
    }

    public void setP13_2Color(String p13_2Color) {
        this.p13_2Color = p13_2Color;
    }

    public String getP13_2Time() {
        return p13_2Time;
    }

    public void setP13_2Time(String p13_2Time) {
        this.p13_2Time = p13_2Time;
    }

    public String getPg() {
        return pg;
    }

    public void setPg(String pg) {
        this.pg = pg;
    }

    public String getPgColor() {
        return pgColor;
    }

    public void setPgColor(String pgColor) {
        this.pgColor = pgColor;
    }

    public String getPgTime() {
        return pgTime;
    }

    public void setPgTime(String pgTime) {
        this.pgTime = pgTime;
    }

    public String getPh() {
        return ph;
    }

    public void setPh(String ph) {
        this.ph = ph;
    }

    public String getPhColor() {
        return phColor;
    }

    public void setPhColor(String phColor) {
        this.phColor = phColor;
    }

    public String getPhTime() {
        return phTime;
    }

    public void setPhTime(String phTime) {
        this.phTime = phTime;
    }

    public String getPh_2() {
        return ph_2;
    }

    public void setPh_2(String ph_2) {
        this.ph_2 = ph_2;
    }

    public String getPh_2Color() {
        return ph_2Color;
    }

    public void setPh_2Color(String ph_2Color) {
        this.ph_2Color = ph_2Color;
    }

    public String getPh_2Time() {
        return ph_2Time;
    }

    public void setPh_2Time(String ph_2Time) {
        this.ph_2Time = ph_2Time;
    }

    public String getQts() {
        return qts;
    }

    public void setQts(String qts) {
        this.qts = qts;
    }

    public String getQtsColor() {
        return qtsColor;
    }

    public void setQtsColor(String qtsColor) {
        this.qtsColor = qtsColor;
    }

    public String getQtsTime() {
        return qtsTime;
    }

    public void setQtsTime(String qtsTime) {
        this.qtsTime = qtsTime;
    }

    public String getGp() {
        return gp;
    }

    public void setGp(String gp) {
        this.gp = gp;
    }

    public String getGpColor() {
        return gpColor;
    }

    public void setGpColor(String gpColor) {
        this.gpColor = gpColor;
    }

    public String getGpTime() {
        return gpTime;
    }

    public void setGpTime(String gpTime) {
        this.gpTime = gpTime;
    }

    public String getGee() {
        return gee;
    }

    public void setGee(String gee) {
        this.gee = gee;
    }

    public String getGeeColor() {
        return geeColor;
    }

    public void setGeeColor(String geeColor) {
        this.geeColor = geeColor;
    }

    public String getGeeTime() {
        return geeTime;
    }

    public void setGeeTime(String geeTime) {
        this.geeTime = geeTime;
    }

    public String getGeh() {
        return geh;
    }

    public void setGeh(String geh) {
        this.geh = geh;
    }

    public String getGehColor() {
        return gehColor;
    }

    public void setGehColor(String gehColor) {
        this.gehColor = gehColor;
    }

    public String getGehTime() {
        return gehTime;
    }

    public void setGehTime(String gehTime) {
        this.gehTime = gehTime;
    }

    public String getV8() {
        return v8;
    }

    public void setV8(String v8) {
        this.v8 = v8;
    }

    public String getV8Color() {
        return v8Color;
    }

    public void setV8Color(String v8Color) {
        this.v8Color = v8Color;
    }

    public String getV8Time() {
        return v8Time;
    }

    public void setV8Time(String v8Time) {
        this.v8Time = v8Time;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String getFile) {
        this.file = getFile;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ObjectParams.class.getSimpleName() + "[", "]")
                .add("objName='" + objName + "'")
                .add("objAddress='" + objAddress + "'")
                .add("crashTime='" + crashTime + "'")
                .add("tnv='" + tnv + "'")
                .add("tnvColor='" + tnvColor + "'")
                .add("tnvTime='" + tnvTime + "'")
                .add("t1='" + t1 + "'")
                .add("t1Color='" + t1Color + "'")
                .add("t1Time='" + t1Time + "'")
                .add("t2='" + t2 + "'")
                .add("t2Color='" + t2Color + "'")
                .add("t2Time='" + t2Time + "'")
                .add("p1='" + p1 + "'")
                .add("p1Color='" + p1Color + "'")
                .add("p1Time='" + p1Time + "'")
                .add("p2='" + p2 + "'")
                .add("p2Color='" + p2Color + "'")
                .add("p2Time='" + p2Time + "'")
                .add("t3='" + t3 + "'")
                .add("t3Color='" + t3Color + "'")
                .add("t3Time='" + t3Time + "'")
                .add("t4='" + t4 + "'")
                .add("t4Color='" + t4Color + "'")
                .add("t4Time='" + t4Time + "'")
                .add("p3='" + p3 + "'")
                .add("p3Color='" + p3Color + "'")
                .add("p3Time='" + p3Time + "'")
                .add("p4='" + p4 + "'")
                .add("p4Color='" + p4Color + "'")
                .add("p4Time='" + p4Time + "'")
                .add("t7='" + t7 + "'")
                .add("t7Color='" + t7Color + "'")
                .add("t7Time='" + t7Time + "'")
                .add("t7_2='" + t7_2 + "'")
                .add("t7_2Color='" + t7_2Color + "'")
                .add("t7_2Time='" + t7_2Time + "'")
                .add("p7='" + p7 + "'")
                .add("p7Color='" + p7Color + "'")
                .add("p7Time='" + p7Time + "'")
                .add("p7_2='" + p7_2 + "'")
                .add("p7_2Color='" + p7_2Color + "'")
                .add("p7_2Time='" + p7_2Time + "'")
                .add("t13='" + t13 + "'")
                .add("t13Color='" + t13Color + "'")
                .add("t13Time='" + t13Time + "'")
                .add("t13_2='" + t13_2 + "'")
                .add("t13_2Color='" + t13_2Color + "'")
                .add("t13_2Time='" + t13_2Time + "'")
                .add("p13='" + p13 + "'")
                .add("p13Color='" + p13Color + "'")
                .add("p13Time='" + p13Time + "'")
                .add("p13_2='" + p13_2 + "'")
                .add("p13_2Color='" + p13_2Color + "'")
                .add("p13_2Time='" + p13_2Time + "'")
                .add("pg='" + pg + "'")
                .add("pgColor='" + pgColor + "'")
                .add("pgTime='" + pgTime + "'")
                .add("ph='" + ph + "'")
                .add("phColor='" + phColor + "'")
                .add("phTime='" + phTime + "'")
                .add("ph_2='" + ph_2 + "'")
                .add("ph_2Color='" + ph_2Color + "'")
                .add("ph_2Time='" + ph_2Time + "'")
                .add("qts='" + qts + "'")
                .add("qtsColor='" + qtsColor + "'")
                .add("qtsTime='" + qtsTime + "'")
                .add("gp='" + gp + "'")
                .add("gpColor='" + gpColor + "'")
                .add("gpTime='" + gpTime + "'")
                .add("gee='" + gee + "'")
                .add("geeColor='" + geeColor + "'")
                .add("geeTime='" + geeTime + "'")
                .add("geh='" + geh + "'")
                .add("gehColor='" + gehColor + "'")
                .add("gehTime='" + gehTime + "'")
                .add("v8='" + v8 + "'")
                .add("v8Color='" + v8Color + "'")
                .add("v8Time='" + v8Time + "'")
                .add("comment='" + comment + "'")
                .add("fileName='" + fileName + "'")
                .add("getFile='" + file + "'")
                .toString();
    }
}
