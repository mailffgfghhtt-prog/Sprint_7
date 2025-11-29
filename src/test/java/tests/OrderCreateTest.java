package tests;

import helpers.Order;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import helpers.ApiClient;
import helpers.TestData;

import java.util.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class OrderCreateTest {

    private Order order;
    private Integer track;
    private List<String> colors;  // Параметр теста: список цветов или null

    public OrderCreateTest(List<String> colors) {
        this.colors = colors;  // Сохраняем параметр в поле
    }

    @Before
    public void setUp() {
        order = new Order();
        order.setFirstName("Test");
        order.setLastName("User");
        order.setAddress("Test address");
        order.setMetroStation("4");
        order.setPhone("+79991234567");
        order.setRentTime(5);
        order.setDeliveryDate(TestData.getFutureDate());
        order.setComment("Test comment");
    }

    @After
    public void tearDown() {
        if (track != null) {
            given()
                    .spec(ApiClient.getRequestSpec())
                    .queryParam("t", track.toString())
                    .delete("/api/v1/orders/track")
                    .then()
                    .statusCode(org.hamcrest.Matchers.anyOf(
                            org.hamcrest.Matchers.equalTo(200),
                            org.hamcrest.Matchers.equalTo(404)
                    ));
        }
    }

    // Поставщик тестовых данных
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {Arrays.asList("BLACK")},
                {Arrays.asList("GREY")},
                {Arrays.asList("BLACK", "GREY")},
                {null}  // без цвета
        });
    }

    @Test
    public void createOrderWithColorsReturnsTrack() {
        // Устанавливаем цвета, если они заданы
        if (colors != null) {
            order.setColor(colors);
        } else {
            order.setColor(null);
        }

        track = given()
                .spec(ApiClient.getRequestSpec())
                .body(order)
                .post("/api/v1/orders")
                .then()
                .statusCode(201)
                .extract().path("track");

        assertThat(track, notNullValue());
    }
}
