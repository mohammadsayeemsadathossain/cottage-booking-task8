package com.TIES4520.onto.demo.model;

public class Booking {
    private String bookingNumber;   // UUID string
    private String bookerName;
    private String cottageID;       // for convenience in API
    private String cottageIri;      // resolved from cottageID
    private String startDate;       // yyyy-MM-dd
    private String endDate;         // yyyy-MM-dd
    public int numberOfDays;

    public String getBookingNumber() { return bookingNumber; }
    public void setBookingNumber(String bookingNumber) { this.bookingNumber = bookingNumber; }
    public String getBookerName() { return bookerName; }
    public void setBookerName(String bookerName) { this.bookerName = bookerName; }
    public String getCottageID() { return cottageID; }
    public void setCottageID(String cottageID) { this.cottageID = cottageID; }
    public String getCottageIri() { return cottageIri; }
    public void setCottageIri(String cottageIri) { this.cottageIri = cottageIri; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public int getNumberOfDays() { return numberOfDays; }
    public void setNumberOfDays(int numberOfDays) {
    	this.numberOfDays = numberOfDays;
    }
}

