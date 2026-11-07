
package com.foodorbit.application.payment;

public class CashPaymentService {

    public boolean processPayment(double totalAmount, double paidAmount) {

        if (!Double.isFinite(totalAmount) || totalAmount <= 0) {
            throw new IllegalArgumentException(
                    "Total amount must be greater than zero"
            );
        }

        if (!Double.isFinite(paidAmount) || paidAmount < 0) {
            throw new IllegalArgumentException(
                    "Paid amount cannot be negative"
            );
        }

        return paidAmount >= totalAmount;
    }
}
