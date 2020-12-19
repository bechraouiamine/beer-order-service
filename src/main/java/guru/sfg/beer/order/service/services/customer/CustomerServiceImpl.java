package guru.sfg.beer.order.service.services.customer;

import guru.sfg.beer.brewery.model.CustomerDto;
import guru.sfg.beer.order.service.repositories.CustomerRepository;
import guru.sfg.beer.order.service.web.mappers.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by aminebechraoui, on 19/12/2020, in guru.sfg.beer.order.service.services.customer
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final CustomerMapper customerMapper;

    @Override
    public List<CustomerDto> listCustomers() {

        return customerRepository.findAll().stream().map(customerMapper::customerObjectToDto).collect(Collectors.toList());
    }

    @Override
    public List<CustomerDto> getByCustomerName(String name) {
        return customerRepository.findAllByCustomerNameLike(name).stream().map(customerMapper::customerObjectToDto).collect(Collectors.toList());
    }

    @Override
    public CustomerDto getById(UUID customerId) {
        if (customerRepository.findById(customerId).isPresent()) {
            return customerMapper.customerObjectToDto(customerRepository.findById(customerId).get());
        } else {
            return null;
        }
    }

    @Override
    public CustomerDto newCustomer(CustomerDto customerDto) {
        return customerMapper.customerObjectToDto(customerRepository.save(customerMapper.customerDtoToObject(customerDto)));
    }

    @Override
    public CustomerDto updateCustomer(CustomerDto customerDto) {
        return customerMapper.customerObjectToDto(customerRepository.saveAndFlush(customerMapper.customerDtoToObject(customerDto)));
    }
}
