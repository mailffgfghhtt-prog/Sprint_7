package tests;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import helpers.*;
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


    private static class RetryFilter implements Filter {
        private final int maxRetries;
        private final long retryDelayMillis;

        public RetryFilter(int maxRetries, long retryDelayMillis) {
            this.maxRetries = maxRetries;
            this.retryDelayMillis = retryDelayMillis;
        }

        @Override
        public Response filter(FilterableRequestSpecification requestSpec,
                               FilterableResponseSpecification responseSpec,
                               FilterContext ctx) {
            Response response = null;
            Exception lastException = null;


            for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
                try {
                    response = ctx.next(requestSpec, responseSpec);
                    if (response.statusCode() < 500) {
                        return response;
                    }
                } catch (Exception e) {
                    lastException = e;
                    if (attempt <= maxRetries) {
                        System.out.println("Attempt " + attempt + " failed: " + e.getMessage() + ". Retrying in " + retryDelayMillis + " ms...");
                        try {
                            Thread.sleep(retryDelayMillis);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Retry interrupted", ie);
                        }
                    } else {
                        throw e;
                    }
                }
            }
            return response;
        }
    }

    @Before
    public void setUp() {
        try {

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(300000)  // 5 мин на подключение
                    .setSocketTimeout(300000)     // 5 мин на чтение
                    .build();


            CloseableHttpClient httpClient = HttpClientBuilder.create()
                    .setDefaultRequestConfig(requestConfig)
                    .setConnectionManager(new PoolingHttpClientConnectionManager())
                    .build();


            RestAssured.config = RestAssuredConfig.config()
                    .httpClient(HttpClientConfig.httpClientConfig()
                            .httpClientFactory(() -> httpClient));



            RestAssured.filters(new RetryFilter(3, 1000));
            RestAssured.filters(
                    new io.restassured.filter.log.RequestLoggingFilter(),
                    new io.restassured.filter.log.ResponseLoggingFilter());



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
        try {
            Response response = courierApi.loginCourier(null, "password");
            response.then()
                    .statusCode(SC_BAD_REQUEST)
                    .body("message", containsString("Недостаточно данных для входа"));
            System.out.println("Null login test passed. Response: " + response.asString());
        } catch (Exception e) {
            System.err.println("Test loginWithNullLoginReturnsError failed: " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void loginWithNullPasswordReturnsError() {
        try {
            Response response = courierApi.loginCourier("login", null);
            response.then()
                    .statusCode(SC_BAD_REQUEST)
                    .body("message", containsString("Недостаточно данных для входа"));
            System.out.println("Null password test passed. Response: " + response.asString());
        } catch (Exception e) {
            System.err.println("Test loginWithNullPasswordReturnsError failed: " + e.getMessage());
            throw e;
        }
    }

    @Test
    public void loginWithBothNullReturnsError() {
        try {
            Response response = courierApi.loginCourier(null, null);
            response.then()
                    .statusCode(SC_BAD_REQUEST)
                    .body("message", containsString("Недостаточно данных для входа"));
            System.out.println("Both null test passed. Response: " + response.asString());
        } catch (Exception e) {
            System.err.println("Test loginWithBothNullReturnsError failed: " + e.getMessage());
            throw e;
        }
    }
}
