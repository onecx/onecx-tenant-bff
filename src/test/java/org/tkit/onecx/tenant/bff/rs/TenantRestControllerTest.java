package org.tkit.onecx.tenant.bff.rs;

import static io.quarkus.qute.Variant.APPLICATION_JSON;
import static io.restassured.RestAssured.given;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.core.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.tkit.onecx.tenant.bff.rs.controllers.TenantRestController;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.tenant.bff.rs.internal.model.*;
import gen.org.tkit.onecx.tenant.client.model.*;
import io.quarkiverse.mockserver.test.InjectMockServerClient;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;

@QuarkusTest
@LogService
@TestHTTPEndpoint(TenantRestController.class)
class TenantRestControllerTest extends AbstractTest {

    KeycloakTestClient keycloakClient = new KeycloakTestClient();
    @InjectMockServerClient
    MockServerClient mockServerClient;

    static final String MOCK_ID = "MOCK";

    @BeforeEach
    void resetExpectation() {
        try {
            mockServerClient.clear(MOCK_ID);
        } catch (Exception ex) {
            //  mockId not existing
        }
    }

    @Test
    void getTenantTest_shouldReturnTenant() {
        Tenant data = new Tenant();
        data.setId("test-id-1");
        data.setDescription("test-description");
        data.setTenantId("test-tenantId-1");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/tenants/" + data.getId()).withMethod(HttpMethod.GET))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(data)));

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", data.getId())
                .get("/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(TenantDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(data.getId(), output.getId());
        Assertions.assertEquals(data.getDescription(), output.getDescription());
        Assertions.assertEquals(data.getOrgId(), output.getOrgId());
    }

    @Test
    void getTenantTest_shouldReturnNotFound() {
        Tenant data = new Tenant();
        data.setId("test-id-1");
        data.setDescription("test-description");
        data.setTenantId("test-tenantId-1");

        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode("CONSTRAINT_VIOLATIONS");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/tenants/" + data.getId()).withMethod(HttpMethod.GET))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode()));
        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .pathParam("id", data.getId())
                .get("/{id}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        Assertions.assertNotNull(output);
    }

    @Test
    void searchTenantsTest_shouldReturnTenantPageResult() {
        Tenant tenant1 = new Tenant();
        tenant1.setId("test-id-1");
        tenant1.setDescription("test-description");
        tenant1.setTenantId("test-tenantId-1");

        Tenant tenant2 = new Tenant();
        tenant2.setId("test-id-1");
        tenant2.setDescription("test-description");
        tenant2.setTenantId("test-tenantId-1");

        List<Tenant> tenantList = new ArrayList<>();
        tenantList.add(tenant1);
        tenantList.add(tenant2);

        TenantPageResult tenantPageResult = new TenantPageResult();
        tenantPageResult.setNumber(0);
        tenantPageResult.setSize(100);
        tenantPageResult.setStream(tenantList);

        TenantSearchCriteria tenantSearchCriteria = new TenantSearchCriteria();
        tenantSearchCriteria.setPageSize(100);
        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/tenants/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(tenantSearchCriteria)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(tenantPageResult)));

        TenantSearchCriteriaDTO tenantSearchCriteriaDTO = new TenantSearchCriteriaDTO();
        tenantSearchCriteriaDTO.setPageSize(100);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(tenantSearchCriteriaDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(TenantPageResultDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(tenantPageResult.getNumber(), output.getNumber());
        Assertions.assertEquals(tenantPageResult.getTotalPages(), output.getTotalPages());
        Assertions.assertEquals(2, output.getStream().toArray().length);
    }

    @Test
    void searchTenantsTest_shouldReturnBadRequest() {
        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(Response.Status.BAD_REQUEST.toString());

        TenantSearchCriteria tenantSearchCriteria = new TenantSearchCriteria();
        tenantSearchCriteria.setOrgId("test-or");
        tenantSearchCriteria.setPageNumber(0);
        tenantSearchCriteria.setPageSize(0);

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/tenants/search").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(tenantSearchCriteria)))
                .withPriority(100)
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        TenantSearchCriteriaDTO tenantSearchCriteriaDTO = new TenantSearchCriteriaDTO();
        tenantSearchCriteriaDTO.setOrgId("test-or");
        tenantSearchCriteriaDTO.setPageNumber(0);
        tenantSearchCriteriaDTO.setPageSize(0);

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(tenantSearchCriteriaDTO)
                .post("/search")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(output);
    }

    @Test
    void createTenantTest_shouldReturnCreated() {

        CreateTenantRequest createTenantRequest = new CreateTenantRequest();
        createTenantRequest.setTenantId("test-id-1");
        createTenantRequest.setDescription("test-description");
        createTenantRequest.setOrgId("test-or");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/tenants").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(createTenantRequest)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.CREATED.getStatusCode()));

        CreateTenantRequestDTO createTenantRequestDTO = new CreateTenantRequestDTO();
        createTenantRequestDTO.setTenantId("test-id-1");
        createTenantRequestDTO.setDescription("test-description");
        createTenantRequestDTO.setOrgId("test-or");

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(createTenantRequestDTO)
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode());

        Assertions.assertNotNull(output);
    }

    @Test
    void createTenantTest_shouldReturnBadRequest() {
        ProblemDetailResponse problemDetailResponse = new ProblemDetailResponse();
        problemDetailResponse.setErrorCode(Response.Status.BAD_REQUEST.toString());

        CreateTenantRequest createTenantRequest = new CreateTenantRequest();
        createTenantRequest.setTenantId("test-id-1");
        createTenantRequest.setDescription("test-description");
        createTenantRequest.setOrgId("test-or");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/tenants").withMethod(HttpMethod.POST)
                .withBody(JsonBody.json(createTenantRequest)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.BAD_REQUEST.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(problemDetailResponse)));

        CreateTenantRequestDTO createTenantRequestDTO = new CreateTenantRequestDTO();
        createTenantRequestDTO.setTenantId("test-id-1");
        createTenantRequestDTO.setDescription("test-description");
        createTenantRequestDTO.setOrgId("test-or");

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(createTenantRequestDTO)
                .post()
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(ProblemDetailResponseDTO.class);

        Assertions.assertNotNull(output);
    }

    @Test
    void updateTenantTest_shouldReturnOK() {

        Tenant tenant = new Tenant();
        tenant.setId("test-id-1");
        tenant.setDescription("test-description");
        tenant.setTenantId("test-tenantId-1");
        tenant.setOrgId("test-org");

        String updateId = "test-id-1";
        UpdateTenantRequest updateTenantRequest = new UpdateTenantRequest();
        updateTenantRequest.setDescription("test-description");
        updateTenantRequest.setOrgId("test-org");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/tenants/" + updateId).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(updateTenantRequest)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(tenant)));

        UpdateTenantRequestDTO updateTenantRequestDTO = new UpdateTenantRequestDTO();
        updateTenantRequestDTO.setDescription("test-description");
        updateTenantRequestDTO.setOrgId("test-org");

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(updateTenantRequestDTO)
                .pathParam("id", updateId)
                .put("/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(APPLICATION_JSON)
                .extract().as(TenantDTO.class);

        Assertions.assertNotNull(output);
        Assertions.assertEquals(tenant.getDescription(), output.getDescription());
        Assertions.assertEquals(tenant.getOrgId(), output.getOrgId());
    }

    @Test
    void updateTenantTest_shouldReturnBadRequest() {

        Tenant tenant = new Tenant();
        tenant.setId("test-id-1");
        tenant.setDescription("test-description");
        tenant.setTenantId("test-tenantId-1");

        String updateId = "test-id-1";
        UpdateTenantRequest updateTenantRequest = new UpdateTenantRequest();
        updateTenantRequest.setDescription("test-description");
        updateTenantRequest.setOrgId("test-or");

        // create mock rest endpoint
        mockServerClient.when(request().withPath("/internal/tenants/" + updateId).withMethod(HttpMethod.PUT)
                .withBody(JsonBody.json(updateTenantRequest)))
                .withId(MOCK_ID)
                .respond(httpRequest -> response().withStatusCode(Response.Status.OK.getStatusCode())
                        .withContentType(MediaType.APPLICATION_JSON)
                        .withBody(JsonBody.json(tenant)));

        UpdateTenantRequestDTO updateTenantRequestDTO = new UpdateTenantRequestDTO();

        var output = given()
                .when()
                .auth().oauth2(keycloakClient.getAccessToken(ADMIN))
                .header(APM_HEADER_PARAM, ADMIN)
                .contentType(APPLICATION_JSON)
                .body(updateTenantRequestDTO)
                .pathParam("id", updateId)
                .put("/{id}")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        Assertions.assertNotNull(output);

    }

}
