package guru.sfg.beer.brewery.model.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Created by BECHRAOUI, 27/10/2020
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateOrderResult {

    private UUID orderId;
    private Boolean isValid;
}
