package com.TIES4520.onto.demo.model;

public class Cottage {
    private String iri;
    private String cottageID;
    private String address;
    private String imageURL;
    private Integer capacity;
    private Integer numberOfBedrooms;
    private Integer distanceToLake;
    private String cityName;
    private Integer distanceToCity;

    public Cottage() {}

    public Cottage(String iri, String cottageID, String address, String imageURL,
                   Integer capacity, Integer numberOfBedrooms, Integer distanceToLake,
                   String cityName, Integer distanceToCity) {
        this.iri = iri;
        this.cottageID = cottageID;
        this.address = address;
        this.imageURL = imageURL;
        this.capacity = capacity;
        this.numberOfBedrooms = numberOfBedrooms;
        this.distanceToLake = distanceToLake;
        this.cityName = cityName;
        this.distanceToCity = distanceToCity;
    }

    public String getIri() { return iri; }
    public void setIri(String iri) { this.iri = iri; }
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
}

