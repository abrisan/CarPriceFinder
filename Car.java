public class Car {

    private final int id;
    private final int price;
    private final int mileage;
    private final int year;

    public Car(int id, int price, int mileage, int year){
        this.id = id;
        this.price = price;
        this.mileage = mileage;
        this.year = year;
    }

    public int getId() {return this.id;}
    public int getPrice() {return this.price;}
    public int getMileage() {return this.mileage;}
    public double getYear() {return this.year;}

    @Override
    public String toString(){
        return String.format("ID: %d, Price %d, Mileage: %d, Year: %d\n", this.id, this.price,
                this.mileage, this.year);
    }


}
