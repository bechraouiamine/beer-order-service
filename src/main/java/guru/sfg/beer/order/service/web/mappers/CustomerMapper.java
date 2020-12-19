package guru.sfg.beer.order.service.web.mappers;

import guru.sfg.beer.brewery.model.CustomerDto;
import guru.sfg.beer.order.service.domain.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Created by aminebechraoui, on 19/12/2020, in guru.sfg.beer.order.service.web.mappers
 */
@Mapper(uses = {DateMapper.class})
public interface CustomerMapper {

    @Mapping(target = "name", source = "customerName")
    CustomerDto customerObjectToDto(Customer customer);

    @Mapping(target = "customerName", source = "name")
    Customer customerDtoToObject(CustomerDto customerDto);
}
