package tests;

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

    @Before
    public void setUp() {

        courierLogin = TestData.generateUniqueLogin();
        given()
                .spec(ApiClient.getRequestSpec())
                .body("{\"login\": \"" + courierLogin + "\", \"password\": \"1234\", \"firstName\": \"Test\"}")
                .post("/api/v1/courier")
                .then()
                .statusCode(201);
    }

    @After
    public void tearDown() {

        given()
                .spec(ApiClient.getRequestSpec())
                .body("{\"login\": \"" + courierLogin + "\", \"password\": \"1234\"}")
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
    public void successfulLoginReturnsId() {
        int id = given()
                .spec(ApiClient.getRequestSpec())
                .body("{\"login\": \"" + courierLogin + "\", \"password\": \"1234\"}")
                .post("/api/v1/courier/login")
                .then()
                .statusCode(200)
                .extract().path("id");

        assertThat(id, greaterThan(0));
    }

    @Test
    public void loginWithoutPasswordReturnsError() {
        given()
                .spec(ApiClient.getRequestSpec())
                .body("{\"login\": \"" + courierLogin + "\"}")
                .post("/api/v1/courier/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    public void loginWithWrongPasswordReturnsError() {
        given()
                .spec(ApiClient.getRequestSpec())
                .body("{\"login\": \"" + courierLogin + "\", \"password\": \"wrong\"}")
                .post("/api/v1/courier/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    public void loginNonExistingUserReturnsError() {
        given()
                .spec(ApiClient.getRequestSpec())
                .body("{\"login\": \"nonexistent\", \"password\": \"1234\"}")
                .post("/api/v1/courier/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }
}
