package com.pine.emeter.model;

/**
 * Created by PinesucceedAndroid on 6/21/2018.
 */

public class HistoryModel {
    String receipt_no;
    String receipt_date_time;
    String customer_name;
    String customer_phone;
    String customer_address;
    String customer_zone;
    String meter_no;
    String Previous_Reading;
    String Current_Reading;
    String Actual_cost;
    String Reading_Cost;
    String meter_image;

    public String getMeter_image() {
        return meter_image;
    }

    public void setMeter_image(String meter_image) {
        this.meter_image = meter_image;
    }

    public String getReceipt_no() {
        return receipt_no;
    }

    public void setReceipt_no(String receipt_no) {
        this.receipt_no = receipt_no;
    }

    public String getReceipt_date_time() {
        return receipt_date_time;
    }

    public void setReceipt_date_time(String receipt_date_time) {
        this.receipt_date_time = receipt_date_time;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getCustomer_phone() {
        return customer_phone;
    }

    public void setCustomer_phone(String customer_phone) {
        this.customer_phone = customer_phone;
    }

    public String getCustomer_address() {
        return customer_address;
    }

    public void setCustomer_address(String customer_address) {
        this.customer_address = customer_address;
    }

    public String getCustomer_zone() {
        return customer_zone;
    }

    public void setCustomer_zone(String customer_zone) {
        this.customer_zone = customer_zone;
    }

    public String getMeter_no() {
        return meter_no;
    }

    public void setMeter_no(String meter_no) {
        this.meter_no = meter_no;
    }

    public String getPrevious_Reading() {
        return Previous_Reading;
    }

    public void setPrevious_Reading(String previous_Reading) {
        Previous_Reading = previous_Reading;
    }

    public String getCurrent_Reading() {
        return Current_Reading;
    }

    public void setCurrent_Reading(String current_Reading) {
        Current_Reading = current_Reading;
    }

    public String getActual_cost() {
        return Actual_cost;
    }

    public void setActual_cost(String actual_cost) {
        Actual_cost = actual_cost;
    }

    public String getReading_Cost() {
        return Reading_Cost;
    }

    public void setReading_Cost(String reading_Cost) {
        Reading_Cost = reading_Cost;
    }
}
