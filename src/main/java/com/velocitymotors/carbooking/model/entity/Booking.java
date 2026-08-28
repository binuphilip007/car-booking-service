package com.velocitymotors.carbooking.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    private String bookingId;
    private String customerName;
    private String vehicleId;
    private LocalDateTime rentalStartDate;
    private LocalDateTime rentalEndDate;

    @Enumerated(EnumType.STRING)
    private VehicleCategory vehicleCategory;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    private String paymentReference;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @Version
    private Long version;

    protected Booking() {
    }

    public Booking(String bookingId, String customerName, String vehicleId,
                   LocalDateTime rentalStartDate, LocalDateTime rentalEndDate,
                   VehicleCategory vehicleCategory, PaymentMode paymentMode,
                   String paymentReference, BookingStatus bookingStatus) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.vehicleId = vehicleId;
        this.rentalStartDate = rentalStartDate;
        this.rentalEndDate = rentalEndDate;
        this.vehicleCategory = vehicleCategory;
        this.paymentMode = paymentMode;
        this.paymentReference = paymentReference;
        this.bookingStatus = bookingStatus;
    }

    public String getBookingId() { return bookingId; }
    public String getCustomerName() { return customerName; }
    public String getVehicleId() { return vehicleId; }
    public LocalDateTime getRentalStartDate() { return rentalStartDate; }
    public LocalDateTime getRentalEndDate() { return rentalEndDate; }
    public VehicleCategory getVehicleCategory() { return vehicleCategory; }
    public PaymentMode getPaymentMode() { return paymentMode; }
    public String getPaymentReference() { return paymentReference; }
    public BookingStatus getBookingStatus() { return bookingStatus; }

    public void confirm() {
        this.bookingStatus = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        this.bookingStatus = BookingStatus.CANCELLED;
    }
}