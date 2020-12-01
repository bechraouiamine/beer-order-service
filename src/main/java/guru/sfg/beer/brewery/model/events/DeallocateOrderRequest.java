package guru.sfg.beer.brewery.model.events;

import guru.sfg.beer.brewery.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by aminebechraoui, on 01/12/2020, in guru.sfg.beer.brewery.model.events
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeallocateOrderRequest {
    private BeerOrderDto beerOrderDto;
}
