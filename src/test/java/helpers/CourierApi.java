package helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;
import static io.restassured.RestAssured.given;
public class CourierApi {
    private static final String BASE_PATH = "/api/v1/courier";
    private ObjectMapper objectMapper = new ObjectMapper();
    public Response createCourier(Courier courier) {
        try {
            String body = objectMapper.writeValueAsString(courier);
            return given()
                    .spec(ApiClient.getRequestSpec())
                    .body(body)
                    .post(BASE_PATH);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create courier: " + e.getMessage(), e);
        }
    }
    public Response loginCourier(String login, String password) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("login", login);
        credentials.put("password", password);
        return given()
                .spec(ApiClient.getRequestSpec())
                .body(credentials)
                .log().all()
                .post("/api/v1/courier/login");
    }
    public Response deleteCourier(Integer id) {
        return given()
                .spec(ApiClient.getRequestSpec())
                .delete(BASE_PATH + "/" + id);
    }
}