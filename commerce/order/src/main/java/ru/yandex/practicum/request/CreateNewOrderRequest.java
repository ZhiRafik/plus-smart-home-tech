package ru.yandex.practicum.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.dto.AddressDto;
import ru.yandex.practicum.dto.ShoppingCartDto;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateNewOrderRequest {

    ShoppingCartDto shoppingCart;

    AddressDto deliveryAddress;
}
