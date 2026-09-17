package com.example.clothesstore.shopitem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShopItemController {
    private final ShopItemService shopItemService;

    @Autowired
    public ShopItemController(ShopItemService shopItemService){
        this.shopItemService = shopItemService;
    }

    @GetMapping
    public String getShopItemById() {
        return shopItemService.getShopItemById();
    }
}
