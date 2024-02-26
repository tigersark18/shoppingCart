package com.shopingcart.controllers;

import com.shopingcart.models.Product;
import com.shopingcart.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<Map<String, String>> createCart(){
        return cartService.createCart();
    }

    @GetMapping("{cartId}")
    public ResponseEntity<Map<String, Object>> getCart(@PathVariable String cartId){
        return cartService.getCart(cartId);
    }

    @PostMapping("/{cartId}/products")
    public ResponseEntity<Map<String, String>> addProducts(@PathVariable String cartId, @RequestBody List<Product> products){
        return cartService.addProducts(cartId, products);
    }

    @DeleteMapping("{cartId}")
    public ResponseEntity<Map<String, String>> deleteCart(@PathVariable String cartId){
        return cartService.deleteCart(cartId);
    }
}
