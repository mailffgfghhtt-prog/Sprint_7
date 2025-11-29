package helpers;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class CourierApi {

    private static final String BASE_PATH = "/api/v1/courier";

    @Step("Создание курьера: {courier}")
    public Response createCourier(Courier courier) {
        return given()
                .spec(ApiClient.getRequestSpec())
                .body(courier)
                .post(BASE_PATH);
    }

    @Step("Авторизация курьера: логин {login}, пароль {password}")
    public Response loginCourier(String login, String password) {
        String body = String.format("{\"login\": \"%s\", \"password\": \"%s\"}", login, password);
        return given()
                .spec(ApiClient.getRequestSpec())
                .body(body)
                .post(BASE_PATH + "/login");
    }

    @Step("Удаление курьера по ID: {courierId}")
    public Response deleteCourier(int courierId) {
        return given()
                .spec(ApiClient.getRequestSpec())
                .delete(BASE_PATH + "/" + courierId);
    }
}
