package com.surya.orderservice.service;

import com.surya.orderservice.client.InventoryClient;
import com.surya.orderservice.dto.OrderRequest;
import com.surya.orderservice.model.Order;
import com.surya.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
//import org.springframework.cloud.contract
import org.springframework.stereotype.Service;

import java.util.UUID;


@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final InventoryClient inventoryClient;

    public void placeOrder(OrderRequest orderRequest){
        var isProductInStock  = inventoryClient.isInStock(orderRequest.skuCode() , orderRequest.quantity());

        if(isProductInStock){
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID().toString());
            order.setPrice(orderRequest.price());
            order.setSkuCode(orderRequest.skuCode());
            order.setQuantity(orderRequest.quantity());
            orderRepository.save(order);
        }else {
            throw new RuntimeException("Product with skuCode "+ orderRequest.skuCode() + " ");
        }

    }

}
