package guru.sfg.beer.order.service.statemachine.actions;

import guru.sfg.beer.brewery.model.BeerOrderEventEnum;
import guru.sfg.beer.brewery.model.BeerOrderStatusEnum;
import guru.sfg.beer.brewery.model.events.DeallocateOrderRequest;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.repositories.BeerOrderRepository;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import guru.sfg.beer.order.service.services.BeerOrderService;
import guru.sfg.beer.order.service.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by aminebechraoui, on 13/12/2020, in guru.sfg.beer.order.service.statemachine.actions
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeallocationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        UUID beerOrderId = UUID.fromString(stateContext.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER).toString());

        beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> {
            jmsTemplate.convertAndSend(
                    JmsConfig.DEALLOCATE_ORDER_QUEUE,
                    DeallocateOrderRequest.builder()
                            .beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder))
                            .build());
        }, () -> {
            log.debug("Beer order not found : " + beerOrderId);
        });


    }
}
