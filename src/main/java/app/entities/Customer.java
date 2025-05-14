package app.entities;

public class Customer {
    private String name;
    private String address;
    private int zip;
    private String city;
    private int phoneNumber;
    private String email;

    public Customer(String name, String address, int zip, String city, int phoneNumber, String email) {
        this.name = name;
        this.address = address;
        this.zip = zip;
        this.city = city;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getZip() {
        return zip;
    }

    public String getCity() {
        return city;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }


    @Override
    public String toString() {
        return "Customer{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", zip=" + zip +
                ", city='" + city + '\'' +
                ", phoneNumber=" + phoneNumber +
                ", email='" + email + '\'' +
                '}';
    }
}
