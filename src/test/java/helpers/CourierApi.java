package helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import org.junit.Test;
import java.util.HashMap;
import java.util.Map;
import static io.restassured.RestAssured.given;
import io.qameta.allure.Step;

public class CourierApi {

    private static final String BASE_PATH = "/api/v1/courier";
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Создаёт нового курьера через API.
     */
    @Step("Создание курьера: логин={login}, пароль={password}")
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

    /**
     * Выполняет вход курьера в систему.
     */
    @Step("Вход курьера: логин={login}, пароль={password}")
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

    /**
     * Удаляет курьера по ID.
     */
    @Step("Удаление курьера с ID={id}")
    public Response deleteCourier(Integer id) {
        return given()
                .spec(ApiClient.getRequestSpec())
                .delete(BASE_PATH + "/" + id);
    }
}
