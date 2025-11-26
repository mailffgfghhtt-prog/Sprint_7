package tests;

import helpers.Order;
import helpers.ApiClient;
import helpers.TestData;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.anyOf;
import static org.junit.Assert.assertNotNull;


public class OrderDeleteByTrackTest {
    private String track;

    @Before
    public void setUp() {
        Order order = new Order();
        order.setFirstName("Delete");
        order.setLastName("Test");
        order.setAddress("Test address");
        order.setMetroStation("4");
        order.setPhone("+79991234567");
        order.setRentTime(1);
        order.setDeliveryDate(TestData.getFutureDate());
        order.setComment("Delete test");
        order.setColor(Arrays.asList("BLACK"));


        Object rawTrack = given()
                .spec(ApiClient.getRequestSpec())
                .body(order)
                .post("/api/v1/orders")
                .then()
                .log().ifValidationFails()
                .statusCode(201)
                .extract().path("track");

        track = (rawTrack != null) ? rawTrack.toString() : null;

        assertNotNull("Track не должен быть null", track);
        System.out.println("Created order with track: " + track);
    }

    @After
    public void tearDown() {
        if (track != null) {
            given()
                    .spec(ApiClient.getRequestSpec())
                    .queryParam("t", track)
                    .log().all()
                    .delete("/api/v1/orders/track")
                    .then()
                    .log().ifError()
                    .statusCode(anyOf(equalTo(200), equalTo(404))); // Исправлено
        }
    }

    @Test
    public void deleteOrderByTrackReturnsOk() {
        System.out.println("Deleting order with track: " + track);

        given()
                .spec(ApiClient.getRequestSpec())
                .queryParam("t", track)
                .log().all()
                .delete("/api/v1/orders/track")
                .then()
                .log().ifError()
                .statusCode(200)
                .body("ok", equalTo(true));
    }

    @Test
    public void deleteNonExistingOrderReturnsError() {
        String nonExistingTrack = "999999";
        System.out.println("Attempting to delete non-existing order with track: " + nonExistingTrack);

        given()
                .spec(ApiClient.getRequestSpec())
                .queryParam("t", nonExistingTrack)
                .log().all()
                .delete("/api/v1/orders/track")
                .then()
                .log().ifError()
                .statusCode(404)
                .body("message", containsString("Недостаточно данных для поиска"));
    }

    @Test
    public void deleteOrderWithoutTrackParameterReturnsError() {
        System.out.println("Attempting to delete order without track parameter");

        given()
                .spec(ApiClient.getRequestSpec())
                .log().all()
                .delete("/api/v1/orders/track")
                .then()
                .log().ifError()
                .statusCode(404)
                .body("message", containsString("Недостаточно данных для поиска"));
    }
}

