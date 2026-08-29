package com.velocitymotors.carbooking.model.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings", uniqueConstraints = {
    @jakarta.persistence.UniqueConstraint(name = "uk_bookings_payment_reference", columnNames = "payment_reference")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    @Column(name = "payment_reference", nullable = false)
    private String paymentReference;

    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;

    @Version
    private Long version;

    @Builder
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

    public void confirm() {
        this.bookingStatus = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        this.bookingStatus = BookingStatus.CANCELLED;
    }
}