package helpers;

import io.restassured.response.Response;
import java.util.Map;
import static io.restassured.RestAssured.given;
public class OrderApi {
    private static final String BASE_PATH = "/api/v1/orders";
    public Response createOrder(Order order) {
        try {
            return given()
                    .spec(ApiClient.getRequestSpec())
                    .body(order)
                    .post(BASE_PATH);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create order: " + e.getMessage(), e);
        }
    }
    public Response deleteOrderByTrack(String track) {
        System.out.println("DELETE request for track: " + track);
        Response response = given()
                .spec(ApiClient.getRequestSpec())
                .queryParam("track", track)
                .log().all()
                .delete(BASE_PATH + "/track");

        System.out.println("DELETE response: " + response.statusCode() + " | " + response.asString());
        return response;
    }
    public Response orderCreate(Order order) {
        return given()
                .spec(ApiClient.getRequestSpec())
                .body(order)
                .post("/api/v1/orders");
    }
    public Response getOrdersList() {
        return given()
                .spec(ApiClient.getRequestSpec())
                .get(BASE_PATH);
    }
    public Response cancelOrder(String track) {
        return given()
                .spec(ApiClient.getRequestSpec())
                .pathParam("track", track)
                .delete("/api/v1/orders/{track}");
    }
    public Response getOrdersByCourierId(String courierId) {
        Map<String, String> params = Map.of("courierId", courierId);
        return given()
                .spec(ApiClient.getRequestSpec())
                .queryParams(params)
                .get(BASE_PATH);
    }
    public Response getOrdersByNearestStation(String stations) {
        Map<String, String> params = Map.of("nearStations", stations);
        return given()
                .spec(ApiClient.getRequestSpec())
                .queryParams(params)
                .get(BASE_PATH);
    }
}