package guru.sfg.beer.brewery.model.events;

import guru.sfg.beer.brewery.model.BeerOrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by BECHRAOUI, 28/10/2020
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocateOrderResult {
    private BeerOrderDto beerOrderDto;
    private Boolean allocationError = false;
    private Boolean pendingInventory = false;
}
