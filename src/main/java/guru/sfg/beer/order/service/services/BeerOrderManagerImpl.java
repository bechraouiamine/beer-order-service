package guru.sfg.beer.order.service.services;

import guru.sfg.beer.brewery.model.BeerOrderDto;
import guru.sfg.beer.brewery.model.BeerOrderEventEnum;
import guru.sfg.beer.brewery.model.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.statemachine.BeerOrderStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Created by BECHRAOUI, 26/10/2020
 */
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setBeerOrderStatus(BeerOrderStatusEnum.NEW);

        BeerOrder savedbeerORder = beerOrderRepository.save(beerOrder);
        sendBeerOrderEvent(savedbeerORder, BeerOrderEventEnum.VALIDATE_ORDER);

        return savedbeerORder;
    }

    @Override
    public void processValidationResult(UUID beerOrderId, Boolean isValid) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderId);

        if (isValid) {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

            BeerOrder validatedBeerOrder = beerOrderRepository.findOneById(beerOrder.getId());

            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
        } else {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILED);
        }
    }

    @Override
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);

        updateAllocatedQty(beerOrderDto, beerOrder);
    }

    @Override
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);

        updateAllocatedQty(beerOrderDto, beerOrder);
    }

    @Override
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED);
    }


    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrder);

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

    private void updateAllocatedQty(BeerOrderDto beerOrderDto, BeerOrder beerOrder) {
        BeerOrder allocatedOrder = beerOrderRepository.getOne(beerOrderDto.getId());

        allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
            beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
                if (beerOrderLine.getId().equals(beerOrderLineDto.getId())) {
                    beerOrderLine.setQuantityAllocated(beerOrderLineDto.getOrderQuantity());
                }
            });
        });

        beerOrderRepository.saveAndFlush(beerOrder);
    }
}
