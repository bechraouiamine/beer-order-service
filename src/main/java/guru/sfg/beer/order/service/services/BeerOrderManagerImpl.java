package guru.sfg.beer.order.service.services;

import guru.sfg.beer.brewery.model.BeerOrderDto;
import guru.sfg.beer.brewery.model.BeerOrderEventEnum;
import guru.sfg.beer.brewery.model.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.statemachine.BeerOrderStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by BECHRAOUI, 26/10/2020
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        log.debug("BeerOrder newBeerOrder : " + beerOrder.toString());

        beerOrder.setId(null);
        beerOrder.setBeerOrderStatus(BeerOrderStatusEnum.NEW);

        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);

        log.debug("newBeerOrder, beerOrder saved : " + savedBeerOrder.getId());

        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATE_ORDER);

        return savedBeerOrder;
    }

    @Transactional
    @Override
    public void processValidationResult(UUID beerOrderId, Boolean isValid) {
        log.debug("Pocessing validation result : " + beerOrderId.toString());

        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderId);

        if (isValid) {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

            log.debug("Beer order event VALIDATION_PASSED sent : beerOrder id : " + beerOrder.getId());

            // Await for order to be validated
            awaitForStatus(beerOrderId, BeerOrderStatusEnum.VALIDATED);

            BeerOrder validatedBeerOrder = beerOrderRepository.findOneById(beerOrder.getId());

            log.debug("Order id found on the database : status : " + validatedBeerOrder.getBeerOrderStatus().toString());

            sendBeerOrderEvent(validatedBeerOrder, BeerOrderEventEnum.ALLOCATE_ORDER);

            log.debug("Beer order event ALLOCATE_ORDER sent : beerOrder id : " + beerOrder.getId());
        } else {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
        }
    }

    @Transactional
    @Override
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
        log.debug("Allocation passed beer ID : " + beerOrderDto.getId());
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());

        log.debug("Beerorder found on the database : " + beerOrder.getId());

        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);

        awaitForStatus(beerOrderDto.getId(), BeerOrderStatusEnum.ALLOCATED);

        log.debug("Sending Allocation success order");

        updateAllocatedQty(beerOrderDto);
    }

    @Transactional
    @Override
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);

        awaitForStatus(beerOrderDto.getId(), BeerOrderStatusEnum.PENDING_INVENTORY);

        updateAllocatedQty(beerOrderDto);
    }

    @Transactional
    @Override
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
    }

    @Override
    public void beerOrderPickedUp(UUID id) {
        Optional<BeerOrder> beerOrder = beerOrderRepository.findById(id);

        beerOrder.ifPresentOrElse(beerOrder1 -> {
            sendBeerOrderEvent(beerOrder1, BeerOrderEventEnum.BEERORDER_PICKED_UP);
        }, () -> log.error("Order not found, id :  " + id));
    }

    @Override
    public void cancelOrder(UUID id) {
        Optional<BeerOrder> beerOrder = beerOrderRepository.findById(id);

        beerOrder.ifPresentOrElse(beerOrder1 -> {
            sendBeerOrderEvent(beerOrder1, BeerOrderEventEnum.CANCEL_ORDER);
        }, () -> log.error("Order not found : " + id));
    }


    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrder);

        log.info("beerOrder.getId().toString() : " + beerOrder.getId().toString());

        Message msg = MessageBuilder.withPayload(eventEnum)
                .setHeader(ORDER_ID_HEADER, beerOrder.getId().toString())
                .build();

        sm.sendEvent(msg);
    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = stateMachineFactory.getStateMachine(beerOrder.getId());

        sm.stop();

        sm.getStateMachineAccessor().doWithRegion(sma -> {
            sma.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
            sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getBeerOrderStatus(), null, null, null));
        });

        sm.start();

        return sm;
    }

    private void updateAllocatedQty(BeerOrderDto beerOrderDto) {
        BeerOrder allocatedOrder = beerOrderRepository.getOne(beerOrderDto.getId());

        Optional<BeerOrder> beerOrderOptional = beerOrderRepository.findById(beerOrderDto.getId());

        beerOrderOptional.ifPresentOrElse(allocatedOrder1 -> {
            allocatedOrder1.getBeerOrderLines().forEach(beerOrderLine -> {
                beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
                    if (beerOrderLineDto.getId().equals(beerOrderLine.getId())) {
                        beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
                    }
                });
            });
            beerOrderRepository.saveAndFlush(allocatedOrder);

        }, () -> log.error("Order not found id : " + beerOrderDto.getId()));
    }

    private void awaitForStatus(UUID beerOrderId, BeerOrderStatusEnum statusEnum) {
        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);

        while (!found.get()) {
            if (loopCount.incrementAndGet() > 10) {
                found.set(true);
                log.debug("Loop retries exceed 10 times !!");
            }

            beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> {
                if (statusEnum.equals(beerOrder.getBeerOrderStatus())) {
                    found.set(true);
                    log.debug("Order found " + beerOrderId);
                }
            }, () -> {
                log.debug("Order not found : " + beerOrderId);
            });

            if (!found.get()) {
                log.debug("Thread sleep for retry");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.debug(e.getMessage());
                }
            }
        }


    }
}
