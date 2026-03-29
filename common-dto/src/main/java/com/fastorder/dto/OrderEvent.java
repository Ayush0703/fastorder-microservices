package com.fastorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;



public class OrderEvent {
    private String productId;
    private int quantity;
    private double totalPrice;

    // MANUALLY ADD THESE (Right-click > Generate > Getter and Setter)
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    // Add a Default Constructor (Crucial for Kafka/JSON)
    public OrderEvent() {}

    public OrderEvent(String productId, int quantity, double totalPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
    }
}
