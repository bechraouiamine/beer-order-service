package guru.sfg.beer.order.service.services.customer;

import guru.sfg.beer.brewery.model.CustomerDto;
import guru.sfg.beer.order.service.domain.Customer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Created by aminebechraoui, on 19/12/2020, in guru.sfg.beer.order.service.services.customer
 */
@Service
public interface CustomerService {

    List<CustomerDto> listCustomers();

    List<CustomerDto> getByCustomerName(String name);

    CustomerDto getById(UUID customerId);

    CustomerDto newCustomer(CustomerDto customerDto);

    CustomerDto updateCustomer(CustomerDto customerDto);

}
