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

import java.math.BigDecimal;
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

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Version
    private Long version;

    @Builder
    public Booking(String bookingId, String customerName, String vehicleId,
                   LocalDateTime rentalStartDate, LocalDateTime rentalEndDate,
                   VehicleCategory vehicleCategory, PaymentMode paymentMode,
                   String paymentReference, BookingStatus bookingStatus,
                   BigDecimal totalAmount, BigDecimal amountPaid) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.vehicleId = vehicleId;
        this.rentalStartDate = rentalStartDate;
        this.rentalEndDate = rentalEndDate;
        this.vehicleCategory = vehicleCategory;
        this.paymentMode = paymentMode;
        this.paymentReference = paymentReference;
        this.bookingStatus = bookingStatus;
        this.totalAmount = totalAmount;
        this.amountPaid = amountPaid != null
                ? amountPaid
                : (bookingStatus == BookingStatus.CONFIRMED ? totalAmount : BigDecimal.ZERO);
    }

    public Booking(String bookingId, String customerName, String vehicleId,
                   LocalDateTime rentalStartDate, LocalDateTime rentalEndDate,
                   VehicleCategory vehicleCategory, PaymentMode paymentMode,
                   String paymentReference, BookingStatus bookingStatus,
                   BigDecimal totalAmount) {
        this(bookingId, customerName, vehicleId, rentalStartDate, rentalEndDate,
             vehicleCategory, paymentMode, paymentReference, bookingStatus, totalAmount, null);
    }

    /** Accumulates an instalment and confirms the booking once the full amount is covered. */
    public boolean registerPayment(BigDecimal paymentAmount) {
        this.amountPaid = this.amountPaid.add(paymentAmount);
        if (isFullyPaid()) {
            confirm();
            return true;
        }
        return false;
    }

    public boolean isFullyPaid() {
        return amountPaid.compareTo(totalAmount) >= 0;
    }

    public BigDecimal outstandingAmount() {
        return totalAmount.subtract(amountPaid).max(BigDecimal.ZERO);
    }

    public void confirm() {
        this.bookingStatus = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        this.bookingStatus = BookingStatus.CANCELLED;
    }
}