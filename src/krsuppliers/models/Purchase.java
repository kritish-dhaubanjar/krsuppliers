package krsuppliers.models;

import java.sql.Date;

public class Purchase extends Transaction{

    public Purchase(int _id, Date date, int bill, int particular_id, String particular, float qty, float rate, float discount, float amount){
        super(_id, date, bill, particular_id, particular, qty, rate, discount, amount);
        super.setCategory(Category.PURCHASE);
    }

}
