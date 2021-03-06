package guru.sfg.beer.order.service.services;

import guru.sfg.beer.brewery.model.BeerOrderDto;
import guru.sfg.beer.order.service.domain.BeerOrder;

import java.util.UUID;

/**
 * Created by BECHRAOUI, 26/10/2020
 */
public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder beerOrder);

    void processValidationResult(UUID beerORderId, Boolean isValid);

    void beerOrderAllocationPassed(BeerOrderDto beerOrderDto);

    void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto);

    void beerOrderAllocationFailed(BeerOrderDto beerOrderDto);

    void beerOrderPickedUp(UUID id);

    void cancelOrder(UUID id);
}
