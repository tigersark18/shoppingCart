package com.shopingcart.services;

import com.shopingcart.models.Cart;
import com.shopingcart.models.Product;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.swing.plaf.PanelUI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Getter
@Setter
public class CartService {

    private final int TTL = 10*60*1000;
    private ConcurrentHashMap<String, Cart> carts;
    private int cartTtl;
    private int cleanInterval;
    private ScheduledExecutorService cleanThread;

    public CartService() {
        this.carts = new ConcurrentHashMap<>();
        this.cartTtl = TTL;
        this.cleanInterval = 1000;
        this.cleanThread = Executors.newScheduledThreadPool(1);
        this.cleanThread.scheduleAtFixedRate(this::cleanUp, 0, cleanInterval, TimeUnit.MILLISECONDS);
    }

    public ResponseEntity<Map<String, String>> createCart(){
        String cartId = UUID.randomUUID().toString();
        this.carts.put(cartId, new Cart(cartId, new ArrayList<>()));
        HashMap<String, String> map = new HashMap<>();
        map.put("CartId", cartId);
        return ResponseEntity.ok().body(map);
    }

    public ResponseEntity<Map<String, Object>> getCart(String cartId){
        Cart cart = this.carts.get(cartId);
        if (cart == null) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("CartId", cartId);
            map.put("Description", "Cart not found");
            return ResponseEntity.ok().body(map);
        }
        Map<String, Object> cartInfo = new HashMap<>();
        cartInfo.put("id", cart.getId());
        cartInfo.put("creationTime", cart.getCreationTime());
        cartInfo.put("products", cart.getProducts());
        return ResponseEntity.ok(cartInfo);
    }

    public ResponseEntity<Map<String, String>> addProducts(String cartId, List<Product> products){
        Cart cart = this.carts.get(cartId);
        HashMap<String, String> map = new HashMap<>();
        map.put("CartId", cartId);
        if (cart == null) {
            map.put("Description", "Cart not found");
            return ResponseEntity.ok().body(map);
        }
        for (Product product : products) {
            Product existingProduct = cart.getProducts().stream()
                    .filter(p -> p.getId() == product.getId())
                    .findFirst()
                    .orElse(null);
            if (existingProduct != null) {
                existingProduct.setAmount(existingProduct.getAmount() + product.getAmount());
            } else {
                cart.getProducts().add(product);
            }
        }
        map.put("CartId", cartId);
        map.put("Description", "Products added");
        return ResponseEntity.ok().body(map);
    }

    public ResponseEntity<Map<String, String>> deleteCart(String cartId){
        HashMap<String, String> map = new HashMap<>();
        map.put("CartId", cartId);
        if (this.carts.remove(cartId) != null) {
            map.put("Description", "Cart deleted");
            return ResponseEntity.ok().body(map);
        }
        else{
            map.put("Description", "Cart not found");
            return ResponseEntity.ok().body(map);
        }
    }

    public void cleanUp(){
        long currentTime = System.currentTimeMillis();
        this.carts.entrySet().removeIf(entry -> currentTime - entry.getValue().getCreationTime().toEpochMilli() > this.cartTtl);
    }
}
