package krsuppliers.models;

import java.sql.Date;

public class Sale {
    private int _id;
    private Date date;
    private int particular_id;
    private String particular;
    private int qty;
    private int rate;
    private int discount;
    private int amount;

    /*Download*/
    public Sale(int _id, Date date, int particular_id, String particular, int qty, int rate, int discount, int amount){
        this._id = _id;
        this.date = date;
        this.particular_id = particular_id;
        this.particular = particular;
        this.qty = qty;
        this.rate = rate;
        this.discount = discount;
        this.amount = amount;
    }

    /*Upload*/
    public Sale(int particular_id, int qty, int rate, int discount){
        this.particular_id = particular_id;
        this.qty = qty;
        this.rate = rate;
        this.discount = discount;
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

    public int getDiscount() {
        return discount;
    }

    public int getAmount() {
        return amount;
    }
}
