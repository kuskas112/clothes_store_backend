package com.example.clothesstore.product.dto;

import com.example.clothesstore.image.ImageMapper;
import com.example.clothesstore.product.jpa.Product;
import com.example.clothesstore.product.jpa.ProductTextile;
import com.example.clothesstore.product.jpa.ProductVariant;
import com.example.clothesstore.color.Color;
import com.example.clothesstore.color.ColorDto;
import com.example.clothesstore.product.dto.request.ProductCreateRequest;
import com.example.clothesstore.product.dto.request.ProductPatchRequest;
import com.example.clothesstore.product.dto.request.ProductVariantCreateRequest;
import com.example.clothesstore.size.Size;
import com.example.clothesstore.size.SizeDto;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = ImageMapper.class)
public abstract class ProductMapper {

    @Autowired
    private ImageMapper imageMapper;

    // ===== RESPONSE =====

    public abstract ProductDto toDto(Product product);
    public abstract ProductVariantDto toDto(ProductVariant variant);

    @Mapping(source = "textile.id", target = "textileId")
    @Mapping(source = "textile.name", target = "textileName")
    public abstract ProductTextileDto toDto(ProductTextile productTextile);

    public abstract ColorDto toDto(Color color);

    public abstract SizeDto toDto(Size size);

    // ===== CREATE: request → entity =====

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "textiles", ignore = true)
    public abstract Product toEntity(ProductCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "color", ignore = true)   // заполним вручную по colorId
    @Mapping(target = "size", ignore = true)    // заполним вручную по sizeId
    public abstract ProductVariant toEntity(ProductVariantCreateRequest request);

    // ===== UPDATE: patch → entity =====

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "variants", ignore = true)
    @Mapping(target = "textiles", ignore = true)
    public abstract void updateEntity(ProductPatchRequest request, @MappingTarget Product product);

    public ProductShortDto toShortDto(Product product){
        String url = null;
        for(var img : product.getImages()){
            if(img.getIsMain()){
                url = imageMapper.toResponse(img.getImage()).url();
                break;
            }
        }

        return new ProductShortDto(
                product.getId(),
                product.getName(),
                product.getGender(),
                product.getSeason(),
                product.getBasePrice(),
                url
        );
    }



}