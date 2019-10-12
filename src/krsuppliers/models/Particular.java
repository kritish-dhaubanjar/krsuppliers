package krsuppliers.models;

public class Particular {
    private int _id;
    private String particular;

    public Particular(int _id, String particular){
        this._id = _id;
        this.particular = particular;
    }

    @Override
    public String toString() {
        return this.particular;
    }

    public int get_id() {
        return _id;
    }

    public String getParticular() {
        return particular;
    }

}
