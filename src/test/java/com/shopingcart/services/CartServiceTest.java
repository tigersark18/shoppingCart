package com.shopingcart.services;

import com.shopingcart.models.Cart;
import com.shopingcart.models.Product;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;


@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    @InjectMocks
    private CartService cartService;

    @BeforeEach
    public void setUp() {
        this.scheduledExecutorService = mock(ScheduledExecutorService.class);
        this.cartService = new CartService();
        this.cartService.setCleanThread(this.scheduledExecutorService);
    }

    @Test
    public void testCreateCart() {
        ResponseEntity<Map<String, String>> response = this.cartService.createCart();
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertNotNull(body.get("CartId"));
        assertEquals(1, this.cartService.getCarts().size());
    }

    @Test
    public void testGetCartNotFound() {
        String cartId = "cart1";
        ResponseEntity<Map<String, Object>> response = this.cartService.getCart(cartId);
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Cart not found", body.get("Description"));
    }

    @Test
    public void testAddProductsCartNotFound() {
        String cartId = "cart1";
        List<Product> products = Arrays.asList(new Product(1, "Product 1", 2), new Product(2, "Product 2", 1));
        ResponseEntity<Map<String, String>> response = this.cartService.addProducts(cartId, products);
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Cart not found", body.get("Description"));
    }

    @Test
    public void testAddProductsCartFound() {
        String cartId = "cart1";
        Cart cart = new Cart(cartId, new ArrayList<>());
        this.cartService.getCarts().put(cartId, cart);
        List<Product> products = Arrays.asList(new Product(1, "Product 1", 2), new Product(2, "Product 2", 1));
        ResponseEntity<Map<String, String>> response = this.cartService.addProducts(cartId, products);
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Products added", body.get("Description"));

        List<Product> productsInCart = cart.getProducts();
        assertEquals(2, productsInCart.size());
        Product product1 = productsInCart.get(0);
        assertEquals(1, product1.getId());
        assertEquals("Product 1", product1.getDescription());
        assertEquals(2, product1.getAmount());
        Product product2 = productsInCart.get(1);
        assertEquals(2, product2.getId());
        assertEquals("Product 2", product2.getDescription());
        assertEquals(1, product2.getAmount());
    }

    @Test
    public void testDeleteCartNotFound() {
        String cartId = "cart1";
        ResponseEntity<Map<String, String>> response = this.cartService.deleteCart(cartId);
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Cart not found", body.get("Description"));
    }

    @Test
    public void testDeleteCartFound() {
        String cartId = "cart1";
        Cart cart = new Cart(cartId, new ArrayList<>());
        this.cartService.getCarts().put(cartId, cart);
        ResponseEntity<Map<String, String>> response = this.cartService.deleteCart(cartId);
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Cart deleted", body.get("Description"));
        assertEquals(0, this.cartService.getCarts().size());
    }

    @Test
    public void testCleanUp() {
        String cartId1 = "cart1";
        Cart cart1 = new Cart(cartId1, new ArrayList<>());
        String cartId2 = "cart2";
        Cart cart2 = new Cart(cartId2, new ArrayList<>());
        this.cartService.getCarts().put(cartId1, cart1);
        this.cartService.getCarts().put(cartId2, cart2);
        this.cartService.cleanUp();
        assertEquals(2, this.cartService.getCarts().size());

        this.cartService.getCarts().put(cartId1, cart1);
        this.cartService.getCarts().put(cartId2, cart2);

        Instant creationTime1 = cart1.getCreationTime();
        Instant creationTime2 = cart2.getCreationTime();
        try {
            Thread.sleep(this.cartService.getCartTtl() + 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.cartService.cleanUp();
        assertEquals(0, this.cartService.getCarts().size());

        this.cartService.getCarts().put(cartId1, cart1);
        this.cartService.getCarts().put(cartId2, cart2);

        cart1.setCreationTime(Instant.now().minusMillis(this.cartService.getCartTtl() - 2000));
        this.cartService.cleanUp();
        assertEquals(1, this.cartService.getCarts().size());
        assertEquals(cartId1, this.cartService.getCarts().keySet().iterator().next());

        cart1.setCreationTime(Instant.now().minusMillis(this.cartService.getCartTtl() + 1000));
        cart2.setCreationTime(Instant.now().minusMillis(this.cartService.getCartTtl() + 1000));
        this.cartService.cleanUp();
        assertEquals(0, this.cartService.getCarts().size());
    }
}
