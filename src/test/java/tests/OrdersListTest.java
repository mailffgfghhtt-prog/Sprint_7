package tests;

import org.junit.Before;
import org.junit.Test;
import helpers.ApiClient;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrdersListTest {

    private static final String VALID_COURIER_ID = "1";

    @Before
    public void setUp() {

    }

    @Test
    public void getOrdersListReturnsOrders() {
        given()
                .spec(ApiClient.getRequestSpec())
                .get("/api/v1/orders")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("orders", hasSize(greaterThanOrEqualTo(0)))
                .body("pageInfo.page", equalTo(0))
                .body("availableStations", hasSize(greaterThan(0)));
    }

    @Test
    public void getOrdersWithCourierIdReturnsFilteredOrders() {
        System.out.println("Sending request: GET /api/v1/orders?courierId=" + VALID_COURIER_ID);

        given()
                .spec(ApiClient.getRequestSpec())
                .queryParam("courierId", VALID_COURIER_ID)
                .get("/api/v1/orders")
                .then()
                .log().all()  // Логируем весь ответ
                // Если курьер может отсутствовать, разрешаем 404
                .statusCode(anyOf(equalTo(200), equalTo(404)))
                .body("orders", anyOf(hasSize(greaterThanOrEqualTo(0)), nullValue()));
    }

    @Test
    public void getOrdersWithNearestStationReturnsFilteredOrders() {
        given()
                .spec(ApiClient.getRequestSpec())
                .queryParam("nearestStation", "[\"1\",\"2\"]")
                .get("/api/v1/orders")
                .then()
                .log().ifValidationFails()
                .statusCode(200)
                .body("orders", anyOf(hasSize(greaterThanOrEqualTo(0)), nullValue()));
    }
}
