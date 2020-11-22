package guru.sfg.beer.order.service.services.testcomponent;

import guru.sfg.beer.brewery.model.events.ValidateOrderRequest;
import guru.sfg.beer.brewery.model.events.ValidateOrderResult;
import guru.sfg.beer.order.service.config.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * AMINE created on 02/11/2020 inside the package - guru.sfg.beer.order.service.services.testcomponent
 **/
@Slf4j
@RequiredArgsConstructor
@Component
public class BeerOrderValidationListener {

    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void listen(Message msg) {
        boolean isValid = true;

        ValidateOrderRequest request = (ValidateOrderRequest) msg.getPayload();

        if (request.getBeerOrderDto().getCustomerRef() != null && request.getBeerOrderDto().getCustomerRef().equals("ValidationException"))
            isValid = false;

        System.out.println("Running Validation from test component !!! Order id : " + request.getBeerOrderDto().getId());

        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
                ValidateOrderResult.builder()
                        .isValid(isValid)
                        .orderId(request.getBeerOrderDto().getId())
                        .build()
        );
    }
}
