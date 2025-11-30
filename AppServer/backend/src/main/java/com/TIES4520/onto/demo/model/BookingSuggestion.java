package com.TIES4520.onto.demo.model;

public class BookingSuggestion {
    private String bookerName;
    private String cottageID;
    private String address;
    private String imageURL;
    private Integer capacity;
    private Integer numberOfBedrooms;
    private Integer distanceToLake;
    private String cityName;
    private Integer distanceToCity;
    private String startDate;       // yyyy-MM-dd
    private String endDate;         // yyyy-MM-dd

    public String getBookerName() { return bookerName; }
    public void setBookerName(String bookerName) { this.bookerName = bookerName; }
    public String getCottageID() { return cottageID; }
    public void setCottageID(String cottageID) { this.cottageID = cottageID; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getNumberOfBedrooms() { return numberOfBedrooms; }
    public void setNumberOfBedrooms(Integer numberOfBedrooms) { this.numberOfBedrooms = numberOfBedrooms; }
    public Integer getDistanceToLake() { return distanceToLake; }
    public void setDistanceToLake(Integer distanceToLake) { this.distanceToLake = distanceToLake; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public Integer getDistanceToCity() { return distanceToCity; }
    public void setDistanceToCity(Integer distanceToCity) { this.distanceToCity = distanceToCity; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
}
