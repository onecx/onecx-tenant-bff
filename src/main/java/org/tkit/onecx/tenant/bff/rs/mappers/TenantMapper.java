package org.tkit.onecx.tenant.bff.rs.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gen.org.tkit.onecx.tenant.bff.rs.internal.model.*;
import gen.org.tkit.onecx.tenant.client.model.*;

@Mapper
public interface TenantMapper {

    TenantSearchCriteria map(TenantSearchCriteriaDTO tenantSearchCriteriaDTO);

    @Mapping(target = "removeStreamItem", ignore = true)
    TenantPageResultDTO map(TenantPageResult pageResult);

    TenantDTO map(Tenant tenant);

    CreateTenantRequest map(CreateTenantRequestDTO createTenantRequestDTO);

    UpdateTenantRequest map(UpdateTenantRequestDTO updateTenantRequestDTO);
}
