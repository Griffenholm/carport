package app.entities;

public class City {
    private int zip;
    private String cityName;

    public City(int zip, String cityName) {
        this.zip = zip;
        this.cityName = cityName;
    }

    public int getZip() {
        return zip;
    }

    public void setZip(int zip) {
        this.zip = zip;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Override
    public String toString() {
        return "City{" +
                "zip=" + zip +
                ", cityName='" + cityName + '\'' +
                '}';
    }
}

