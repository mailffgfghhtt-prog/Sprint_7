package tests;

import helpers.Courier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import helpers.ApiClient;
import helpers.TestData;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static steps.StepAnnotations.createCourier;

public class CourierCreateTest {
    private String uniqueLogin;
    private Courier validCourier;
    private Courier courierWithoutFirstName;
    private static final String BASE_PATH = "/api/v1/courier";

    @Before
    public void setUp() {

        uniqueLogin = TestData.generateUniqueLogin();
        validCourier = new Courier();
        validCourier.setLogin(uniqueLogin);
        validCourier.setPassword("1234");
        validCourier.setFirstName("TestName");

        courierWithoutFirstName = new Courier();
        courierWithoutFirstName.setLogin(uniqueLogin + "_no_name");
        courierWithoutFirstName.setPassword("1234");
    }

    @After
    public void tearDown() {
        System.out.println("Starting cleanup for login: " + uniqueLogin);

        Response loginResponse = given()
                .spec(ApiClient.getRequestSpec())
                .body("{\"login\": \"" + uniqueLogin + "\", \"password\": \"1234\"}")
                .post(BASE_PATH + "/login");

        if (loginResponse.statusCode() == 200) {
            int courierId = loginResponse.then().extract().path("id");
            System.out.println("Courier ID found: " + courierId);

            Response deleteResponse = given()
                    .spec(ApiClient.getRequestSpec())
                    .delete(BASE_PATH + "/" + courierId);


            if (deleteResponse.statusCode() == 200) {
                System.out.println("Courier deleted successfully.");
            } else if (deleteResponse.statusCode() == 404) {
                System.out.println("Warning: Courier not found during deletion (404).");
            } else {
                System.out.println("Error deleting courier. Status: " + deleteResponse.statusCode());
            }
        } else if (loginResponse.statusCode() == 404) {
            System.out.println("Warning: Courier not found during authorization (404). Skipping deletion.");
        } else {
            System.out.println("Unexpected status during authorization: " + loginResponse.statusCode());
        }
    }

    @Test
    public void successfulCourierCreationReturnsOk() {
        createCourier(uniqueLogin);

        given()
                .spec(ApiClient.getRequestSpec())
                .body(validCourier)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Test
    public void cannotCreateCourierWithDuplicateLogin() {
        // Сначала создаём курьера
        given()
                .spec(ApiClient.getRequestSpec())
                .body(validCourier)
                .post(BASE_PATH)
                .then()
                .statusCode(201);

        // Пытаемся создать курьера с тем же логином
        given()
                .spec(ApiClient.getRequestSpec())
                .body(validCourier)
                .post(BASE_PATH)
                .then()
                .statusCode(409)
                .body("message", containsString("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    public void createCourierWithoutFirstNameIsAllowed() {
        createCourier(courierWithoutFirstName.getLogin());

        given()
                .spec(ApiClient.getRequestSpec())
                .body(courierWithoutFirstName)
                .post(BASE_PATH)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Test
    public void creationWithoutLoginReturnsError() {
        Courier courierNoLogin = new Courier();
        courierNoLogin.setPassword("1234");
        courierNoLogin.setFirstName("Test");

        given()
                .spec(ApiClient.getRequestSpec())
                .body(courierNoLogin)
                .post(BASE_PATH)
                .then()
                .statusCode(400)
                .body("message", containsString("Недостаточно данных для создания учетной записи"));
    }

    @Test
    public void creationWithoutPasswordReturnsError() {
        Courier courierNoPassword = new Courier();
        courierNoPassword.setLogin("testlogin");
        courierNoPassword.setFirstName("Test");

        given()
                .spec(ApiClient.getRequestSpec())
                .body(courierNoPassword)
                .post(BASE_PATH)
                .then()
                .statusCode(400)
                .body("message", containsString("Недостаточно данных для создания учетной записи"));
    }
}
