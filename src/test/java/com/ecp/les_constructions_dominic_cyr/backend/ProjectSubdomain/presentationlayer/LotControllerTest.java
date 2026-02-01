//package com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.presentationlayer;
//
//import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.BusinessLayer.Lot.LotService;
//import com.ecp.les_constructions_dominic_cyr.backend.ProjectSubdomain.PresentationLayer.Lot.LotController;
//import com.ecp.les_constructions_dominic_cyr.backend.utils.Exception.InvalidInputException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import java.util.List;
//import java.util.UUID;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.doNothing;
//import static org.mockito.Mockito.verify;
//import static org.springframework.test.web.reactive.server.WebTestClient.bindToController;
//
//@org.springframework.web.bind.annotation.ControllerAdvice
//class TestControllerAdvice {
//    @ExceptionHandler(InvalidInputException.class)
//    public ResponseEntity<String> handleInvalidInputException(InvalidInputException e) {
//        return ResponseEntity.badRequest().body(e.getMessage());
//    }
//}
//
//
//@WebFluxTest(LotController.class)
//class LotControllerTest {
//
//    private WebTestClient webTestClient;
//
//    @MockBean
//    private LotService lotService;
//
//    private static final int UUID_LENGTH = 36;
//    private static final String VALID_LOT_ID = UUID.randomUUID().toString();
//    private static final String INVALID_SHORT_LOT_ID = "123";
//    private static final String INVALID_FORMAT_LOT_ID = "not-a-uuid-string-of-length-36";
//
//    private final LotRequestModel mockRequest = new LotRequestModel();
//    private final LotResponseModel mockResponse = new LotResponseModel(VALID_LOT_ID, "Location A", 1000f, "1000", "92.9");
//    private final List<LotResponseModel> mockResponseList = List.of(
//            new LotResponseModel(UUID.randomUUID().toString(), "Location B", 2000f, "2000", "185.8"),
//            new LotResponseModel(UUID.randomUUID().toString(), "Location C", 3000f, "3000", "278.7")
//    );
//
//
//    @BeforeEach
//    void setup() {
//        // Initialize WebTestClient, binding to the controller and the TestControllerAdvice
//        webTestClient = bindToController(new LotController(lotService))
//                .controllerAdvice(new TestControllerAdvice())
//                .build();
//    }
//
//
//    @Test
//    @DisplayName("GET /api/v1/projects/{projectIdentifier}/lots should return a list of all lots with 200 OK")
//    void getAllLots_ReturnsAllLots() {
//        String projectIdentifier = "proj-001-test";
//        webTestClient.get().uri("/api/v1/projects/{projectIdentifier}/lots", projectIdentifier)
//                .accept(MediaType.APPLICATION_JSON)
//                .exchange()
//                .expectStatus().isOk()
//                .expectBodyList(LotResponseModel.class);
//
//    }
//
//
//    @Test
//    @DisplayName("GET /api/v1/projects/{projectIdentifier}/lots/{lotId} should return 400 Bad Request for incorrect ID length")
//    void getLotById_InvalidLength_Returns400() {
//        String projectIdentifier = "proj-001-test";
//        // ACT & ASSERT
//        webTestClient.get().uri("/api/v1/projects/{projectIdentifier}/lots/{lotId}", projectIdentifier, INVALID_SHORT_LOT_ID)
//                .exchange()
//                .expectStatus().isBadRequest()
//                .expectBody(String.class)
//                .value(response -> {
//                    assert (response.contains("Invalid lot ID"));
//                });
//    }
//
//
//    @Test
//    @DisplayName("DELETE /api/v1/projects/{projectIdentifier}/lots/{lotId} should return 204 No Content for valid ID")
//    void deleteLot_ValidId_Returns204() {
//        String projectIdentifier = "proj-001-test";
//        // ARRANGE
//        doNothing().when(lotService).deleteLot(eq(VALID_LOT_ID));
//
//        // ACT & ASSERT
//        webTestClient.delete().uri("/api/v1/projects/{projectIdentifier}/lots/{lotId}", projectIdentifier, VALID_LOT_ID)
//                .exchange()
//                .expectStatus().isNoContent()
//                .expectBody().isEmpty();
//
//        verify(lotService).deleteLot(eq(VALID_LOT_ID));
//    }
//
//
//    @Test
//    @DisplayName("DELETE /api/v1/projects/{projectIdentifier}/lots/{lotId} should return 400 Bad Request for incorrect delete ID length")
//    void deleteLot_InvalidLength_Returns400() {
//        String projectIdentifier = "proj-001-test";
//        // ACT & ASSERT
//        webTestClient.delete().uri("/api/v1/projects/{projectIdentifier}/lots/{lotId}", projectIdentifier, INVALID_SHORT_LOT_ID)
//                .exchange()
//                .expectStatus().isBadRequest()
//                .expectBody(String.class)
//                .value(response -> {
//                    assert (response.contains("Invalid lot ID"));
//                });
//    }
//
//
//    static class LotRequestModel {
//    }
//
//    static class LotResponseModel {
//        public String id;
//        public String civicAddress;
//        public Float price;
//        public String dimensionsSquareFeet;
//        public String dimensionsSquareMeters;
//
//        public LotResponseModel(String id, String civicAddress, Float price, String dimensionsSquareFeet, String dimensionsSquareMeters) {
//            this.id = id;
//            this.civicAddress = civicAddress;
//            this.price = price;
//            this.dimensionsSquareFeet = dimensionsSquareFeet;
//            this.dimensionsSquareMeters = dimensionsSquareMeters;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//            LotResponseModel that = (LotResponseModel) o;
//            return id.equals(that.id) && civicAddress.equals(that.civicAddress);
//        }
//
//        @Override
//        public int hashCode() {
//            return java.util.Objects.hash(id, civicAddress);
//        }
//    }
//}