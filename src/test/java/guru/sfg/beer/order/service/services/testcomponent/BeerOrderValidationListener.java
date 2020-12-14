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

    public static final String RUNNING_VALIDATION_FROM_TEST_COMPONENT_ORDER_ID = "Running Validation from test component !!! Order id : ";
    public static final String VALIDATION_EXCEPTION = "ValidationException";
    public static final String DONT_VALIDATE = "dont-validate";
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void listen(Message msg) {
        boolean isValid = true;
        boolean sendResponse = true;

        ValidateOrderRequest request = (ValidateOrderRequest) msg.getPayload();

        if (request.getBeerOrderDto().getCustomerRef() != null) {
            if (request.getBeerOrderDto().getCustomerRef().equals(VALIDATION_EXCEPTION)) {
                isValid = false;
            } else if (request.getBeerOrderDto().getCustomerRef().equals(DONT_VALIDATE)) {
                sendResponse = false;
            }

        }


        System.out.println(RUNNING_VALIDATION_FROM_TEST_COMPONENT_ORDER_ID + request.getBeerOrderDto().getId());

        if (sendResponse) {
            jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE,
                    ValidateOrderResult.builder()
                            .isValid(isValid)
                            .orderId(request.getBeerOrderDto().getId())
                            .build()
            );
        }

    }
}
