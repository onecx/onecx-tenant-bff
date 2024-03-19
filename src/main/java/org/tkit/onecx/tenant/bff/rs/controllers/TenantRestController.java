package org.tkit.onecx.tenant.bff.rs.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.tenant.bff.rs.mappers.ExceptionMapper;
import org.tkit.onecx.tenant.bff.rs.mappers.ProblemDetailMapper;
import org.tkit.onecx.tenant.bff.rs.mappers.TenantMapper;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.tenant.bff.rs.internal.TenantApiService;
import gen.org.tkit.onecx.tenant.bff.rs.internal.model.*;
import gen.org.tkit.onecx.tenant.client.api.TenantInternalApi;
import gen.org.tkit.onecx.tenant.client.model.ProblemDetailResponse;
import gen.org.tkit.onecx.tenant.client.model.Tenant;
import gen.org.tkit.onecx.tenant.client.model.TenantPageResult;

@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
@LogService
public class TenantRestController implements TenantApiService {

    @Inject
    @RestClient
    TenantInternalApi tenantClient;
    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    TenantMapper tenantMapper;

    @Inject
    ProblemDetailMapper problemDetailMapper;

    @Override
    public Response createTenant(CreateTenantRequestDTO createTenantRequestDTO) {

        try (Response response = tenantClient.createTenant(tenantMapper.map(createTenantRequestDTO))) {
            return Response.status(response.getStatus()).build();
        } catch (WebApplicationException ex) {
            return Response.status(ex.getResponse().getStatus())
                    .entity(problemDetailMapper.map(ex.getResponse().readEntity(ProblemDetailResponse.class))).build();
        }
    }

    @Override
    public Response getTenant(String id) {

        try (Response response = tenantClient.getTenant(id)) {
            TenantDTO tenantDTO = tenantMapper.map(response.readEntity(Tenant.class));
            return Response.status(response.getStatus()).entity(tenantDTO).build();
        }
    }

    @Override
    public Response searchTenants(TenantSearchCriteriaDTO tenantSearchCriteriaDTO) {

        try (Response response = tenantClient.searchTenants(tenantMapper.map(tenantSearchCriteriaDTO))) {
            TenantPageResultDTO tenantPageResult = tenantMapper.map(response.readEntity(TenantPageResult.class));
            return Response.status(response.getStatus()).entity(tenantPageResult).build();
        } catch (WebApplicationException ex) {
            return Response.status(ex.getResponse().getStatus())
                    .entity(problemDetailMapper.map(ex.getResponse().readEntity(ProblemDetailResponse.class))).build();
        }
    }

    @Override
    public Response updateTenant(String id, UpdateTenantRequestDTO updateTenantRequestDTO) {
        try (Response response = tenantClient.updateTenant(id, tenantMapper.map(updateTenantRequestDTO))) {
            TenantDTO tenantDTO = tenantMapper.map(response.readEntity(Tenant.class));
            return Response.status(response.getStatus()).entity(tenantDTO).build();
        }
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }

    @ServerExceptionMapper
    public Response restException(WebApplicationException ex) {
        return Response.status(ex.getResponse().getStatus()).build();
    }
}
