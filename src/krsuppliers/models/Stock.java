package krsuppliers.models;

import java.util.Date;

public class Stock implements Comparable<Stock>{

    private int _id;
    private Date date;
    private int bill;
    private int particular_id;
    private String particular;
    private float qty;
    private float rate, discount, amount;
    private float balance;

    @Override
    public int compareTo(Stock o) {
        if(this.date.after(o.date))
            return  1;
        else if (this.date.before(o.date))
            return -1;
        else
            return 0;
    }

    public Stock(int _id, Date date, int bill, int particular_id, String particular, float qty, float rate, float discount, float amount) {
        this._id = _id;
        this.date = date;
        this.bill = bill;
        this.particular_id = particular_id;
        this.particular = particular;
        this.qty = qty;
        this.rate = rate;
        this.discount = discount;
        this.amount = amount;
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

    public float getQty() {
        return qty;
    }

    public float getRate() {
        return rate;
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

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public int getBill() {
        return bill;
    }

    public void setBill(int bill) {
        this.bill = bill;
    }

    public Double getSelling_rate(){
        return Double.parseDouble(String.format("%.2f", this.rate + this.rate * 0.2));
    }
}