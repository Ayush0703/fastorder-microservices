package com.fastorder.client;

import com.fastorder.dto.InventoryReservationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", url = "${inventory.service.url}")
public interface InventoryClient {

    @PostMapping("/api/inventory/reservations")
    void reserveInventory(@RequestBody InventoryReservationRequest request);
}
