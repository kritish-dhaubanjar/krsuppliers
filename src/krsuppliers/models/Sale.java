package krsuppliers.models;

import java.sql.Date;

public class Sale extends Transaction{
    public Sale(int _id, Date date, int particular_id, String particular, int qty, int rate, int discount, int amount){
        super(_id, date, particular_id, particular, qty, rate, discount, amount);
    }
}
