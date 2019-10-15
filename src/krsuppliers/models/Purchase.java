package krsuppliers.models;

import java.sql.Date;

public class Purchase extends Transaction{
    private float selling_rate;

    public Purchase(int _id, Date date, int particular_id, String particular, int qty, float rate, float selling_rate, float discount, float amount){
        super(_id, date, particular_id, particular, qty, rate, discount, amount);
        this.selling_rate = selling_rate;
        super.setCategory(Category.PURCHASE);
    }

    public float getSelling_rate(){
        return selling_rate;
    }

}
