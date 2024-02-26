package com.shopingcart.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    private String id;
    private Instant creationTime;
    private List<Product> products;

    public Cart(String id, List<Product> products) {
        this.id = id;
        this.creationTime = Instant.now();
        this.products = products;
    }

    public void addProducts(List<Product> products){
        this.products.addAll(products);
    }
}
