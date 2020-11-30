package guru.sfg.beer.order.service.statemachine.actions;

import guru.sfg.beer.brewery.model.BeerOrderEventEnum;
import guru.sfg.beer.brewery.model.BeerOrderStatusEnum;
import guru.sfg.beer.order.service.services.BeerOrderManagerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

/**
 * Created by aminebechraoui, on 30/11/2020, in guru.sfg.beer.order.service.statemachine.actions
 */
@Component
@Slf4j
public class ValidationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    public static final String VALIDATION_ERROR = "Compensating Transaction ... Validation Failed : ";

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> stateContext) {
        String beerOrderId = stateContext.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER).toString();
        log.error(VALIDATION_ERROR + beerOrderId);
    }
}
