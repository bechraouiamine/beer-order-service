package guru.sfg.beer.order.service.services;

import guru.sfg.beer.brewery.model.BeerOrderEventEnum;
import guru.sfg.beer.brewery.model.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by BECHRAOUI, 26/10/2020
 */
@RequiredArgsConstructor
@Service
public class BeerOrderMangerImpl implements BeerOrderManger {

    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;

    @Transactional
    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setBeerOrderStatusEnum(BeerOrderStatusEnum.NEW);

        BeerOrder savedbeerORder = beerOrderRepository.save(beerOrder);
        sendBeerOrderEvent(savedbeerORder, BeerOrderEventEnum.VALIDATE_ORDER);

        return savedbeerORder;
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEventEnum eventEnum) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = build(beerOrder);

        Message msg = MessageBuilder.withPayload(eventEnum).build();

        sm.sendEvent(msg);
    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> sm = stateMachineFactory.getStateMachine(beerOrder.getId());

        sm.stop();

        sm.getStateMachineAccessor().doWithRegion(sma -> {
            sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getBeerOrderStatusEnum(), null, null, null));
        });

        sm.start();

        return sm;
    }
}
