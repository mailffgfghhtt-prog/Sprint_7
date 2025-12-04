package tests;

import helpers.*;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;
public class CourierLoginTest {
    private String courierLogin;
    private Integer courierId;
    private CourierApi courierApi = new CourierApi();
    private OrderApi orderApi = new OrderApi();
    private String track;
    @Before
    public void setUp() {
        try {
            courierLogin = TestData.generateUniqueLogin();
            Courier courier = new Courier(courierLogin, "1234", "TestLogin");
            Response createResponse = courierApi.createCourier(courier);
            createResponse.then()
                    .statusCode(SC_CREATED)
                    .body("ok", equalTo(true));
            Response loginResponse = courierApi.loginCourier(courierLogin, "1234");
            loginResponse.then().statusCode(SC_OK);
            courierId = loginResponse.then().extract().path("id");
            System.out.println("Setup completed. Courier ID: " + courierId);
        } catch (Exception e) {
            System.err.println("Error in setUp: " + e.getMessage());
            throw e;
        }
    }
    @After
    public void tearDown() {
        try {
            if (courierId != null) {
                System.out.println("Starting cleanup for courier ID: " + courierId);
                Response deleteResponse = courierApi.deleteCourier(courierId);
                switch (deleteResponse.statusCode()) {
                    case SC_OK:
                        System.out.println("Courier (ID: " + courierId + ") deleted successfully.");
                        break;
                    case SC_NOT_FOUND:
                        System.out.println("Warning: Courier (ID: " + courierId + ") not found during deletion (404).");
                        break;
                    default:
                        System.out.println("Error deleting courier (ID: " + courierId +
                                "). Status: " + deleteResponse.statusCode());
                }
            } else {
                System.out.println("No courier ID to delete (createdCourierId is null).");
            }

            if (track != null && !track.isEmpty()) {
                System.out.println("Deleting order with track: " + track);
                Response orderDeleteResponse = orderApi.deleteOrderByTrack(track);

                switch (orderDeleteResponse.statusCode()) {
                    case SC_OK:
                        System.out.println("Order (track: " + track + ") deleted successfully.");
                        break;
                    case SC_NOT_FOUND:
                        System.out.println("Warning: Order (track: " + track +
                                ") not found during deletion (404).");
                        break;
                    default:
                        System.out.println("Error deleting order (track: " + track +
                                "). Status: " + orderDeleteResponse.statusCode());
                }
            }
        } catch (Exception e) {
            System.err.println("Exception during cleanup: " + e.getMessage());
        }
    }
    @Test
    public void loginWithValidCredentialsReturnsOk() {
        try {
            Response response = courierApi.loginCourier(courierLogin, "1234");
            response.then()
                    .statusCode(SC_OK)
                    .body("id", notNullValue());
            System.out.println("Login successful. Response: " + response.asString());
        } catch (Exception e) {
            System.err.println("Test loginWithValidCredentialsReturnsOk failed: " + e.getMessage());
            throw e;
        }
    }
    @Test
    public void loginWithInvalidPasswordReturnsError() {
        try {
            Response response = courierApi.loginCourier(courierLogin, "invalid_password");
            response.then()
                    .statusCode(SC_NOT_FOUND)
                    .body("message", containsString(ApiMessages.ERROR_ACCOUNT_NOT_FOUND));
            System.out.println("Invalid password test passed. Response: " + response.asString());
        } catch (Exception e) {
            System.err.println("Test loginWithInvalidPasswordReturnsError failed: " + e.getMessage());
            throw e;
        }
    }
    @Test
    public void loginWithNonExistingCourierReturnsError() {
        try {
            String nonExistingLogin = "non_existing_login_" + System.currentTimeMillis();
            Response response = courierApi.loginCourier(nonExistingLogin, "1234");
            response.then()
                    .statusCode(SC_NOT_FOUND)
                    .body("message", containsString(ApiMessages.ERROR_ACCOUNT_NOT_FOUND));
            System.out.println("Non-existing courier test passed. Response: " + response.asString());
        } catch (Exception e) {
            System.err.println("Test loginWithNonExistingCourierReturnsError failed: " + e.getMessage());
            throw e;
        }
    }
    @Test
    public void loginWithNullLoginReturnsError() {
        Response response = courierApi.loginCourier(null, "password");
        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", containsString("Недостаточно данных для входа"));
    }
    @Test
    public void loginWithNullPasswordReturnsError() {
        Response response = courierApi.loginCourier("login", null);
        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", containsString("Недостаточно данных для входа"));
    }
    @Test
    public void loginWithBothNullReturnsError() {
        Response response = courierApi.loginCourier(null, null);
        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", containsString("Недостаточно данных для входа"));
    }
}