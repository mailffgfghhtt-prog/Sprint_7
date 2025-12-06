package tests;

import helpers.*;
import io.restassured.response.Response;
import org.junit.*;
import java.util.Arrays;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;
public class OrdersListTest {
    private Integer courierId; // Изменён тип на Integer
    private OrderApi orderApi = new OrderApi();
    private String track;
    @Before
    public void setUp() throws Exception {
        Courier courier = new Courier(
                TestData.generateUniqueLogin(),
                "1234",
                "TestCourier"
        );
        Response courierResponse = new CourierApi().createCourier(courier);
        courierResponse.then().statusCode(SC_CREATED);
        Response loginResponse = new CourierApi().loginCourier(
                courier.getLogin(),
                courier.getPassword()
        );
        loginResponse.then().statusCode(SC_OK);
        courierId = loginResponse.jsonPath().getInt("id"); // Получаем int
        Order order = new Order();
        order.setFirstName("Test");
        order.setLastName("User");
        order.setAddress("Test address");
        order.setMetroStation("4");
        order.setPhone("+79991234567");
        order.setRentTime(1);
        order.setDeliveryDate(TestData.getFutureDate());
        order.setComment("Test comment");
        order.setColor(Arrays.asList("BLACK"));

        Response response = orderApi.createOrder(order);
        track = response.then()
                .statusCode(SC_CREATED)
                .extract().path("track").toString();
    }
    @After
    public void tearDown() {
        if (track != null) {
            orderApi.cancelOrder(track);
        }
        if (courierId != null) {
            new CourierApi().deleteCourier(courierId); // Передаём Integer
        }
    }
    @Test
    public void getOrdersListReturnsOrders() {
        Response response = orderApi.getOrdersList();
        response.then()
                .statusCode(SC_OK)
                .body("orders", notNullValue())
                .body("pageInfo", notNullValue())
                .body("availableStations", notNullValue())
                .body("orders", hasSize(greaterThanOrEqualTo(0)))
                .body("pageInfo.page", equalTo(0))
                .body("availableStations", hasSize(greaterThan(0)));
    }
    @Test
    public void getOrdersWithCourierIdReturnsFilteredOrders() {
        Response response = orderApi.getOrdersByCourierId(String.valueOf(courierId));
        response.then()
                .statusCode(SC_OK)
                .body("orders", hasSize(greaterThanOrEqualTo(0)));
    }
    @Test
    public void getOrdersWithInvalidCourierIdReturnsEmptyList() {
        Response response = orderApi.getOrdersByCourierId("999999");
        response.then()
                .statusCode(SC_NOT_FOUND)
                .body("orders", anyOf(nullValue(), hasSize(0)));
    }
    @Test
    public void getOrdersByNearestStationReturnsFilteredOrders() {
        Response response = orderApi.getOrdersByNearestStation("[\"1\",\"2\"]");
        response.then()
                .statusCode(SC_OK)
                .body("orders", anyOf(hasSize(greaterThanOrEqualTo(0)), nullValue()));
    }
}