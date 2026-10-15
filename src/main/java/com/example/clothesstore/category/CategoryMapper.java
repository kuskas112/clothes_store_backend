package com.example.clothesstore.category;

import com.example.clothesstore.category.dto.request.CategoryCreateRequest;
import com.example.clothesstore.category.dto.request.CategoryPatchRequest;
import com.example.clothesstore.category.dto.CategoryDto;
import com.example.clothesstore.category.dto.CategoryRefDto;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public abstract class CategoryMapper {

    // ===== RESPONSE =====

    public CategoryDto toDto(Category category) {
        if (category == null) return null;
        return new CategoryDto(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getSortOrder(),
                category.getActive()
        );
    }

    public CategoryRefDto toRefDto(Category category) {
        if (category == null) return null;
        return new CategoryRefDto(
                category.getId(),
                category.getName(),
                category.getSlug()
        );
    }

    // ===== CREATE: request → entity (MapStruct сгенерирует) =====

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)   // parent заполним вручную в сервисе по parentId
    @Mapping(target = "active", ignore = true)   // ставим true в сервисе
    public abstract Category toEntity(CategoryCreateRequest request);

    // ===== UPDATE: patch → entity =====

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)   // parent меняем вручную в сервисе
    public abstract void updateEntity(CategoryPatchRequest request, @MappingTarget Category category);
}