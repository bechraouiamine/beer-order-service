package guru.sfg.beer.order.service.web.controllers;

import guru.sfg.beer.brewery.model.CustomerDto;
import guru.sfg.beer.order.service.services.customer.CustomerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by aminebechraoui, on 19/12/2020, in guru.sfg.beer.order.service.web.controllers
 */
@RequestMapping("/api/v1/")
@RestController
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("customers")
    public List<CustomerDto> listCustomer() {
        return customerService.listCustomers();
    }
}
