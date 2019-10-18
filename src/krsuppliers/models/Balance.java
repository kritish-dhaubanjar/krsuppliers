package krsuppliers.models;

import java.sql.Date;

public class Balance extends Transaction {
    public Balance(int _id, Date date, int bill, int particular_id, String particular, float qty, float rate, float discount, float amount){
        super(_id, date, bill, particular_id, particular, qty, rate, discount, amount);
        super.setCategory(Category.BALANCE);
    }

}
