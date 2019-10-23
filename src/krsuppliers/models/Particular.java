package krsuppliers.models;

import java.sql.Date;

public class Particular implements Comparable<Particular> {
    private int _id;
    private Date date;
    private int bill;
    private String particular;
    private float qty;
    private float rate;
    private float discount;
    private float amount;

    @Override
    public int compareTo(Particular o) {
        return this.particular.compareTo(o.particular);
    }

    public Particular(int _id, String particular){
        this._id = _id;
        this.particular = particular;
    }

    public Particular(int _id, Date date, int bill, String particular, float qty, float rate, float discount, float amount) {
        this._id = _id;
        this.date = date;
        this.bill = bill;
        this.particular = particular;
        this.qty = qty;
        this.rate = rate;
        this.discount = discount;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return this.particular;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getBill() {
        return bill;
    }

    public void setBill(int bill) {
        this.bill = bill;
    }

    public String getParticular() {
        return particular;
    }

    public void setParticular(String particular) {
        this.particular = particular;
    }

    public float getQty() {
        return qty;
    }

    public void setQty(float qty) {
        this.qty = qty;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public float getDiscount() {
        return discount;
    }

    public void setDiscount(float discount) {
        this.discount = discount;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
