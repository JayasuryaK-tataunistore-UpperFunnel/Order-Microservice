package com.surya.orderservice.service;

import com.surya.orderservice.client.InventoryClient;
import com.surya.orderservice.dto.OrderRequest;
import com.surya.orderservice.event.OrderPlacedEvent;
import com.surya.orderservice.model.Order;
import com.surya.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
//import org.springframework.cloud.contract
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;


@RequiredArgsConstructor
@Service
@Slf4j
public class  OrderService {

    private final OrderRepository orderRepository;

    private final KafkaTemplate<String , OrderPlacedEvent> kafkaTemplate;

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
            // Send the message to Kafka Topic
            OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent();
            orderPlacedEvent.setOrderNumber(order.getOrderNumber());
            orderPlacedEvent.setEmail(orderRequest.userDetails().email());
            orderPlacedEvent.setFirstName(orderRequest.userDetails().firstName());
            orderPlacedEvent.setLastName(orderRequest.userDetails().lastName());

            log.info("Start sending order-placed event {} to kafka topic order-placed" , orderPlacedEvent);
            kafkaTemplate.send("order-placed" , orderPlacedEvent);
            log.info("End sending order-placed event {} to kafka topic order-placed" , orderPlacedEvent);



        }else {
            throw new RuntimeException("Product with skuCode "+ orderRequest.skuCode() + " ");
        }

    }

}
