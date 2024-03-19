package org.tkit.onecx.tenant.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.tkit.quarkus.rs.mappers.OffsetDateTimeMapper;

import gen.org.tkit.onecx.tenant.bff.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.tenant.client.model.ProblemDetailResponse;

@Mapper(uses = { OffsetDateTimeMapper.class })
public interface ProblemDetailMapper {
    @Mapping(target = "removeParamsItem", ignore = true)
    @Mapping(target = "removeInvalidParamsItem", ignore = true)
    ProblemDetailResponseDTO map(ProblemDetailResponse problemDetailResponse);
}
