package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import helpers.*;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CourierCreateTest {
    private String uniqueLogin;
    private Courier validCourier;
    private Courier courierWithoutFirstName;
    private CourierApi courierApi = new CourierApi();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
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

        Response loginResponse = courierApi.loginCourier(uniqueLogin, "1234");

        if (loginResponse.statusCode() == 200) {
            int courierId = loginResponse.then().extract().path("id");
            System.out.println("Courier ID found: " + courierId);

            Response deleteResponse = courierApi.deleteCourier(courierId);

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
    public void successfulCourierCreationReturnsOk() throws Exception {
        Response response = courierApi.createCourier(validCourier);

        response.then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Test
    public void cannotCreateCourierWithDuplicateLogin() throws Exception {
        // Создаём курьера
        Response firstResponse = courierApi.createCourier(validCourier);
        firstResponse.then().statusCode(201);

        // Пытаемся создать с тем же логином
        Response secondResponse = courierApi.createCourier(validCourier);
        secondResponse.then()
                .statusCode(409)
                .body("message", containsString("Этот логин уже используется. Попробуйте другой."));
    }

    @Test
    public void createCourierWithoutFirstNameIsAllowed() throws Exception {
        Response response = courierApi.createCourier(courierWithoutFirstName);

        response.then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Test
    public void creationWithoutLoginReturnsError() throws Exception {
        Courier courierNoLogin = new Courier();
        courierNoLogin.setPassword("1234");
        courierNoLogin.setFirstName("Test");

        Response response = courierApi.createCourier(courierNoLogin);

        response.then()
                .statusCode(400)
                .body("message", containsString("Недостаточно данных для создания учетной записи"));
    }

    @Test
    public void creationWithoutPasswordReturnsError() throws Exception {
        Courier courierNoPassword = new Courier();
        courierNoPassword.setLogin("testlogin");
        courierNoPassword.setFirstName("Test");

        Response response = courierApi.createCourier(courierNoPassword);

        response.then()
                .statusCode(400)
                .body("message", containsString("Недостаточно данных для создания учетной записи"));
    }
}
