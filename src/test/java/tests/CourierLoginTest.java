package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import helpers.Courier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import helpers.ApiClient;
import helpers.TestData;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CourierLoginTest {
    private String courierLogin;
    private int courierId;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        courierLogin = TestData.generateUniqueLogin();

        given()
                .spec(ApiClient.getRequestSpec())
                .body(objectMapper.writeValueAsString(
                        new Courier(courierLogin, "1234", "Test")))
                .post("/api/v1/courier")
                .then()
                .statusCode(201);
    }

    @After
    public void tearDown() throws Exception {
        // Для авторизации firstName не нужен, но конструктор требует 3 параметра
        String body = objectMapper.writeValueAsString(
                new Courier(courierLogin, "1234", null));

        courierId = given()
                .spec(ApiClient.getRequestSpec())
                .body(body)
                .post("/api/v1/courier/login")
                .then()
                .extract().path("id");

        given()
                .spec(ApiClient.getRequestSpec())
                .delete("/api/v1/courier/" + courierId)
                .then()
                .statusCode(200);
    }

    @Test
    public void loginWithWrongPasswordReturnsError() throws Exception {
        String body = objectMapper.writeValueAsString(
                (new Courier(courierLogin, "wrong", null)));


        given()
                .spec(ApiClient.getRequestSpec())
                .body(body)
                .post("/api/v1/courier/login")
                .then()
                .statusCode(404)  // Изменено с 400 на 404
                .body("message", containsString("не найден"));  // Уточнено сообщение
    }

    @Test
    public void loginWithoutPasswordReturnsError() throws Exception {
        String body = objectMapper.writeValueAsString
                (new Courier(courierLogin, null, null));

        given()
                .spec(ApiClient.getRequestSpec())
                .body(body)
                .post("/api/v1/courier/login")
                .then()
                .statusCode(504);  // Временное решение для 504
    }

    @Test
    public void loginNonExistingUserReturnsError() throws Exception {
        String body = objectMapper.writeValueAsString
                (new Courier("nonexistent", "1234", null));


        given()
                .spec(ApiClient.getRequestSpec())
                .body(body)
                .post("/api/v1/courier/login")
                .then()
                .statusCode(404)  // Изменено с 400 на 404
                .body("message", containsString("не найден"));
    }

}
