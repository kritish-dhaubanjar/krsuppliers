package krsuppliers.models;

import java.sql.Date;

public class Sale extends Transaction{
    public Sale(int _id, Date date, int bill, int particular_id, String particular, int qty, float rate, float discount, float amount){
        super(_id, date, bill, particular_id, particular, qty, rate, discount, amount);
        super.setCategory(Category.SALES);
    }
}
