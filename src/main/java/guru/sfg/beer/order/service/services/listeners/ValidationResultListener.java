package guru.sfg.beer.order.service.services.listeners;

import guru.sfg.beer.brewery.model.events.ValidateOrderResult;
import guru.sfg.beer.order.service.config.JmsConfig;
import guru.sfg.beer.order.service.services.BeerOrderManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Created by BECHRAOUI, 27/10/2020
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ValidationResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
    public void listen(ValidateOrderResult validateOrderResult) {
        final UUID beerOrderId = validateOrderResult.getOrderId();

        log.info("Validation Result for order id : " + beerOrderId);

        Boolean isValid = validateOrderResult.getIsValid();

        beerOrderManager.processValidationResult(
                beerOrderId,
                isValid);
    }
}
