package com.TIES4520.onto.demo.model;

/**
 * Model class to hold the input parameters for the Cottage Search API,
 * as extracted from the incoming SSWAP Request RDF Graph (RRG).
 */
public class SearchRequestModel {

    private String bookerName;
    private int requiredPlaces;
    private int requiredBedrooms;
    private int maxLakeDistanceMeters;
    private String city;
    private int maxCityDistanceMeters;
    private String startDay;          // Expected format: dd.MM.yyyy for CottageService
    private int requiredDays;
    private int maxStartShiftDays;

    // Default Constructor
    public SearchRequestModel() {
    }

    // Constructor (Optional, but useful)
    public SearchRequestModel(String bookerName, int requiredPlaces, int requiredBedrooms, int maxLakeDistanceMeters,
                              String city, int maxCityDistanceMeters, String startDay, int requiredDays, int maxStartShiftDays) {
        this.bookerName = bookerName;
        this.requiredPlaces = requiredPlaces;
        this.requiredBedrooms = requiredBedrooms;
        this.maxLakeDistanceMeters = maxLakeDistanceMeters;
        this.city = city;
        this.maxCityDistanceMeters = maxCityDistanceMeters;
        this.startDay = startDay;
        this.requiredDays = requiredDays;
        this.maxStartShiftDays = maxStartShiftDays;
    }

    // --- Getters and Setters ---

    public String getBookerName() {
        return bookerName;
    }

    public void setBookerName(String bookerName) {
        this.bookerName = bookerName;
    }

    public int getRequiredPlaces() {
        return requiredPlaces;
    }

    public void setRequiredPlaces(int requiredPlaces) {
        this.requiredPlaces = requiredPlaces;
    }

    public int getRequiredBedrooms() {
        return requiredBedrooms;
    }

    public void setRequiredBedrooms(int requiredBedrooms) {
        this.requiredBedrooms = requiredBedrooms;
    }

    public int getMaxLakeDistanceMeters() {
        return maxLakeDistanceMeters;
    }

    public void setMaxLakeDistanceMeters(int maxLakeDistanceMeters) {
        this.maxLakeDistanceMeters = maxLakeDistanceMeters;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getMaxCityDistanceMeters() {
        return maxCityDistanceMeters;
    }

    public void setMaxCityDistanceMeters(int maxCityDistanceMeters) {
        this.maxCityDistanceMeters = maxCityDistanceMeters;
    }

    public String getStartDay() {
        return startDay;
    }

    public void setStartDay(String startDay) {
        this.startDay = startDay;
    }

    public int getRequiredDays() {
        return requiredDays;
    }

    public void setRequiredDays(int requiredDays) {
        this.requiredDays = requiredDays;
    }

    public int getMaxStartShiftDays() {
        return maxStartShiftDays;
    }

    public void setMaxStartShiftDays(int maxStartShiftDays) {
        this.maxStartShiftDays = maxStartShiftDays;
    }
}