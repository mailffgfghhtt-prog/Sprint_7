package tests;

import helpers.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import helpers.ApiClient;
import helpers.TestData;
import java.util.Arrays;
import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class OrderCreateTest {
    private Order order;
    private Integer track;
    @Before
    public void setUp() {
        order = new Order();
        order.setFirstName("Test");
        order.setLastName("User");
        order.setAddress("Test address");
        order.setMetroStation("4");
        order.setPhone("+79991234567");
        order.setRentTime(5);
        order.setDeliveryDate(TestData.getFutureDate());
        order.setComment("Test comment");
    }

    @After
    public void tearDown() {
        if (track != null) {
            given()
                    .spec(ApiClient.getRequestSpec())
                    .queryParam("t", track.toString())
                    .delete("/api/v1/orders/track")
                    .then()
                    .statusCode(anyOf(equalTo(200), equalTo(404)));
        }
    }

    @Test
    public void createOrderWithBlackColorReturnsTrack() {
        order.setColor(Arrays.asList("BLACK"));

        track = given()
                .spec(ApiClient.getRequestSpec())
                .body(order)
                .post("/api/v1/orders")
                .then()
                .statusCode(201)
                .extract().path("track");

        assertThat(track, notNullValue());
    }

    @Test
    public void createOrderWithGreyColorReturnsTrack() {
        order.setColor(Arrays.asList("GREY"));

        track = given()
                .spec(ApiClient.getRequestSpec())
                .body(order)
                .post("/api/v1/orders")
                .then()
                .statusCode(201)
                .extract().path("track");

        assertThat(track, notNullValue());
    }

    @Test
    public void createOrderWithBothColorsReturnsTrack() {
        order.setColor(Arrays.asList("BLACK", "GREY"));

        track = given()
                .spec(ApiClient.getRequestSpec())
                .body(order)
                .post("/api/v1/orders")
                .then()
                .statusCode(201)
                .extract().path("track");

        assertThat(track, notNullValue());
    }

    @Test
    public void createOrderWithoutColorReturnsTrack() {
        track = given()
                .spec(ApiClient.getRequestSpec())
                .body(order)
                .post("/api/v1/orders")
                .then()
                .statusCode(201)
                .extract().path("track");

        assertThat(track, notNullValue());
    }
}
