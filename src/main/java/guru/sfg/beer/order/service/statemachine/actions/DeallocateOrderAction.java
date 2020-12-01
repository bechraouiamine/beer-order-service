package guru.sfg.beer.order.service.statemachine.actions;

import guru.sfg.beer.brewery.model.BeerOrderEventEnum;
import guru.sfg.beer.brewery.model.BeerOrderStatusEnum;
import guru.sfg.beer.brewery.model.events.AllocateOrderRequest;
import guru.sfg.beer.brewery.model.events.DeallocateOrderRequest;
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
 * Created by aminebechraoui, on 01/12/2020, in guru.sfg.beer.order.service.statemachine.actions
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeallocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;
    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        String beerOrderId = stateContext.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER).toString();

        log.debug("Deallocation order action received beerorderid : " + beerOrderId);

        BeerOrder beerOrder = beerOrderRepository.getOne(UUID.fromString(beerOrderId));

        log.debug("beer order found on the database : " + beerOrder.getId());

        jmsTemplate.convertAndSend(JmsConfig.DEALLOCATE_ORDER_QUEUE,
                DeallocateOrderRequest.builder()
                        .beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder))
                        .build());

        log.debug("Deallocate beer order request, beerOrderId : " + beerOrderId);
    }
}