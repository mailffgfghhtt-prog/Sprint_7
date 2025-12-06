package tests;

import helpers.*;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class CourierCreateTest {
    private String uniqueLogin;
    private Courier validCourier;
    private Courier courierWithoutFirstName;
    private CourierApi courierApi = new CourierApi();
    private Integer createdCourierId;

    @Before
    public void setUp() {
        uniqueLogin = TestData.generateUniqueLogin();
        validCourier = new Courier(uniqueLogin, "1234", "TestName");
        courierWithoutFirstName = new Courier(uniqueLogin + "_no_name", "1234", null);
    }

    @After
    public void tearDown() {
        try {
            if (createdCourierId != null) {
                System.out.println("Starting cleanup for courier ID: " + createdCourierId);

                Response deleteResponse = courierApi.deleteCourier(createdCourierId);

                if (deleteResponse.statusCode() == SC_OK) {
                    System.out.println("Courier (ID: " + createdCourierId + ") deleted successfully.");
                } else if (deleteResponse.statusCode() == SC_NOT_FOUND) {
                    System.out.println("Warning: Courier (ID: " + createdCourierId + ") not found during deletion (404).");
                } else {
                    System.out.println("Error deleting courier (ID: " + createdCourierId + "). Status: " + deleteResponse.statusCode());
                }
            } else {
                System.out.println("No courier ID to delete (createdCourierId is null).");
            }
        } catch (Exception e) {
            System.err.println("Exception during courier cleanup: " + e.getMessage());
        }
    }
    @Test
    public void successfulCourierCreationReturnsCreated() throws Exception {
        Response response = courierApi.createCourier(validCourier);
        response.then()
                .statusCode(SC_CREATED)
                .body("ok", equalTo(true));

        createdCourierId = response.then().extract().path("id");
    }
    @Test
    public void cannotCreateCourierWithDuplicateLogin() throws Exception {

        Response firstResponse = courierApi.createCourier(validCourier);
        firstResponse.then().statusCode(SC_CREATED);
        createdCourierId = firstResponse.then().extract().path("id");

        Response secondResponse = courierApi.createCourier(validCourier);
        secondResponse.then()
                .statusCode(SC_CONFLICT)
                .body("message", containsString(ApiMessages.ERROR_LOGIN_DUPLICATE));
    }
    @Test
    public void createCourierWithoutFirstNameIsAllowed() throws Exception {
        Response response = courierApi.createCourier(courierWithoutFirstName);
        response.then()
                .statusCode(SC_CREATED)
                .body("ok", equalTo(true));

        createdCourierId = response.then().extract().path("id");
    }
    @Test
    public void creationWithoutLoginReturnsError() throws Exception {
        Courier courierNoLogin = new Courier(null, "1234", "Test");
        Response response = courierApi.createCourier(courierNoLogin);
        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", containsString(ApiMessages.ERROR_MISSING_DATA));
    }
    @Test
    public void creationWithoutPasswordReturnsError() throws Exception {
        Courier courierNoPassword = new Courier("testlogin", null, "Test");
        Response response = courierApi.createCourier(courierNoPassword);
        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("message", containsString(ApiMessages.ERROR_MISSING_DATA));
    }
}