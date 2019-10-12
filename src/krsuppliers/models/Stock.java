package krsuppliers.models;

import java.util.Date;

public class Stock implements Comparable<Stock>{
    private int _id;
    private Date date;
    private int particular_id;
    private String particular;
    private int qty;
    private int rate;

    @Override
    public int compareTo(Stock o) {
        if(this.date.after(o.date))
            return  1;
        else if (this.date.before(o.date))
            return -1;
        else
            return 0;
    }

    /*Download*/
    public Stock(int _id, Date date, int particular_id, String particular, int qty, int rate) {
        this._id = _id;
        this.date = date;
        this.particular_id = particular_id;
        this.particular = particular;
        this.qty = qty;
        this.rate = rate;
    }

    public int get_id() {
        return _id;
    }

    public Date getDate() {
        return date;
    }

    public int getParticular_id() {
        return particular_id;
    }

    public String getParticular() {
        return particular;
    }

    public int getQty() {
        return qty;
    }

    public int getRate() {
        return rate;
    }
}