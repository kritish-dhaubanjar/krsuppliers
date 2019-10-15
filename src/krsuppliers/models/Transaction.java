package krsuppliers.models;

import java.sql.Date;

public class Transaction implements Comparable<Transaction>{

    private int _id;
    private Date date;
    private int particular_id;
    private String particular;
    private int qty;
    private float rate;
    private float discount;
    private float amount;

    private Category category;

    @Override
    public int compareTo(Transaction o) {
        if(this.date.after(o.date))
            return  1;
        else if (this.date.before(o.date))
            return -1;
        else
            return 0;
    }

    Transaction(int _id, Date date, int particular_id, String particular, int qty, float rate, float discount, float amount){
        this._id = _id;
        this.date = date;
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

    public int getQty() {
        return qty;
    }

    public float getRate() {
        return rate;
    }

    public float getDiscount() {
        return discount;
    }

    public float getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
