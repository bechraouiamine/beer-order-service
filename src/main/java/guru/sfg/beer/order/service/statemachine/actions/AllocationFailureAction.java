package guru.sfg.beer.order.service.statemachine.actions;

import guru.sfg.beer.brewery.model.BeerOrderEventEnum;
import guru.sfg.beer.brewery.model.BeerOrderStatusEnum;
import guru.sfg.beer.brewery.model.events.AllocationFailureEvent;
import guru.sfg.beer.brewery.model.events.ValidateOrderRequest;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.domain.BeerOrder;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by aminebechraoui, on 30/11/2020, in guru.sfg.beer.order.service.statemachine.actions
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AllocationFailureAction  implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    public static final String SENT_ALLOCATION_FAILURE_EVENT_ORDER_ID = "Sent allocation failure event order id : ";
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {

        String beerOrderId = stateContext.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER).toString();

        jmsTemplate.convertAndSend(JmsConfig.ALLOCATION_FAILURE_QUEUE, AllocationFailureEvent.builder().orderId(UUID.fromString(beerOrderId)).build());

        log.info(SENT_ALLOCATION_FAILURE_EVENT_ORDER_ID + beerOrderId);
    }
}
