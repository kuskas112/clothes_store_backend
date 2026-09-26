package com.example.clothesstore.textile;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TextileMapper {

    TextileDto toDto(Textile textile);

    List<TextileDto> toDto(List<Textile> textiles);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    Textile toEntity(TextileDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(TextilePatchDto patch, @MappingTarget Textile textile);
}