package com.example.clothesstore.product.dto;

import com.example.clothesstore.product.Product;
import com.example.clothesstore.product.ProductTextile;
import com.example.clothesstore.product.ProductVariant;
import com.example.clothesstore.color.Color;
import com.example.clothesstore.color.ColorDto;
import com.example.clothesstore.size.Size;
import com.example.clothesstore.size.SizeDto;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    // ===== RESPONSE =====

    ProductDto toDto(Product product);
    ProductShortDto toShortDto(Product product);
    ProductVariantDto toDto(ProductVariant variant);

    @Mapping(source = "textile.id", target = "textileId")
    @Mapping(source = "textile.name", target = "textileName")
    ProductTextileDto toDto(ProductTextile productTextile);

    ColorDto toDto(Color color);

    SizeDto toDto(Size size);

    // ===== CREATE: request → entity =====

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "textiles", ignore = true)
    Product toEntity(ProductCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "color", ignore = true)   // заполним вручную по colorId
    @Mapping(target = "size", ignore = true)    // заполним вручную по sizeId
    ProductVariant toEntity(ProductVariantCreateRequest request);

    // ===== UPDATE: patch → entity =====

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "textiles", ignore = true)
    void updateEntity(ProductPatchRequest request, @MappingTarget Product product);
}