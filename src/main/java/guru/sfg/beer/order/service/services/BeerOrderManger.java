package guru.sfg.beer.order.service.services;

import guru.sfg.beer.order.service.domain.BeerOrder;

/**
 * Created by BECHRAOUI, 26/10/2020
 */
public interface BeerOrderManger {

    BeerOrder newBeerOrder(BeerOrder beerOrder);
}
