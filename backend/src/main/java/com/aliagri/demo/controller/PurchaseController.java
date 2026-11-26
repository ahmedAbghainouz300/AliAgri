package com.aliagri.demo.controller;

import com.aliagri.demo.dto.purchasedto.PurchaseRequest;
import com.aliagri.demo.dto.purchasedto.PurchaseResponse;
import com.aliagri.demo.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping
    public List<PurchaseResponse> getAll() {
        return purchaseService.getAllPurchases();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseResponse create(@RequestBody PurchaseRequest request) {
        return purchaseService.createPurchase(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        purchaseService.deletePurchase(id);
    }
}