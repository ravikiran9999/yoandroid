package com.yo.android.model.dialer;

public class CallLogsResult {
//    {
//        "stime": "2014­11­28  14:04:33",
//            "billsec": "0",
//            "salerate": "0.210000",
//            "salecost": "0.00000",
//            "dialnumber": "7921 181533",
//            "destination_name": "SIP2SIP Call",
//            "dialedstatus": "NOT  ANSWER"
//    }
    private String salerate;

    private String salecost;

    private String dialnumber;

    private String dialedstatus;

    private String stime;

    private String billsec;

    private String destination_name;

    public String getSalerate() {
        return salerate;
    }

    public void setSalerate(String salerate) {
        this.salerate = salerate;
    }

    public String getSalecost() {
        return salecost;
    }

    public void setSalecost(String salecost) {
        this.salecost = salecost;
    }

    public String getDialnumber() {
        return dialnumber;
    }

    public void setDialnumber(String dialnumber) {
        this.dialnumber = dialnumber;
    }

    public String getDialedstatus() {
        return dialedstatus;
    }

    public void setDialedstatus(String dialedstatus) {
        this.dialedstatus = dialedstatus;
    }

    public String getStime() {
        return stime;
    }

    public void setStime(String stime) {
        this.stime = stime;
    }

    public String getBillsec() {
        return billsec;
    }

    public void setBillsec(String billsec) {
        this.billsec = billsec;
    }

    public String getDestination_name() {
        return destination_name;
    }

    public void setDestination_name(String destination_name) {
        this.destination_name = destination_name;
    }

    @Override
    public String toString() {
        return "ClassPojo [salerate = " + salerate + ", salecost = " + salecost + ", dialnumber = " + dialnumber + ", dialedstatus = " + dialedstatus + ", stime = " + stime + ", billsec = " + billsec + ", destination_name = " + destination_name + "]";
    }
}