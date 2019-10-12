package krsuppliers.models;

public class Particular {
    private int _id;
    private String particular;
    private int balance_qty;

    public Particular(int _id, String particular, int balance_qty){
        this._id = _id;
        this.particular = particular;
        this.balance_qty = balance_qty;
    }

    public Particular(String particular){
        this.particular = particular;
    }
}
