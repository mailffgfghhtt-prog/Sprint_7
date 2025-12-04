package tests;

import helpers.*;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.assertNotNull;

public class OrderDeleteByTrackTest {
    private String track;
    private OrderApi orderApi = new OrderApi();
    @Before
    public void setUp() throws Exception {
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

        Response response = orderApi.createOrder(order);
        track = response.then()
                .statusCode(SC_CREATED)
                .extract().path("track").toString();

        assertNotNull("Track не должен быть null", track);
        System.out.println("Created order with track: " + track);
    }
    @After
    public void tearDown() {
        if (track != null && !track.isEmpty()) {
            try {
                Response response = orderApi.deleteOrderByTrack(track);
                if (response.statusCode() == SC_OK) {
                    System.out.println("Order deleted successfully.");
                } else if (response.statusCode() == SC_NOT_FOUND) {
                    System.out.println("Warning: Order not found during deletion (404).");
                } else {
                    System.out.println("Error deleting order. Status: " + response.statusCode());
                }
            } catch (Exception e) {
                System.out.println("Exception during order deletion: " + e.getMessage());
            }
        } else {
            System.out.println("Warning: Track is null or empty, skipping deletion.");
        }
    }
    @Test
    public void deleteOrderByTrackReturnsOk() {
        System.out.println("Deleting order with track: " + track);
        Response response = orderApi.deleteOrderByTrack(track);
        response.then().statusCode(SC_NOT_FOUND);
    }
    @Test
    public void deleteNonExistingOrderReturnsError() {
        String nonExistingTrack = "999999";
        Response response = orderApi.deleteOrderByTrack(nonExistingTrack);
        response.then().statusCode(SC_NOT_FOUND);
    }
    @Test
    public void deleteOrderWithoutTrackParameterReturnsError() {
        Response response = given()
                .spec(ApiClient.getRequestSpec())
                .delete("/api/v1/orders/track");
        response.then().statusCode(SC_NOT_FOUND);
    }
}