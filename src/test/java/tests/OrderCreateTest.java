package tests;


import helpers.Order;
import helpers.OrderApi;
import helpers.TestData;
import io.restassured.response.Response;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import java.util.Collection;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.hamcrest.Matchers.*;


@RunWith(Parameterized.class)
public class OrderCreateTest {

    private OrderApi orderApi = new OrderApi();
    private String colorParam;
    private boolean expectTrack;



    public OrderCreateTest(String colorParam, boolean expectTrack) {
        this.colorParam = colorParam;
        this.expectTrack = expectTrack;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{

                {"BLACK", true},

                {"GREY", true},

                {"BLACK,GREY", true},

                {"", true}
        });
    }

    @Test
    public void testOrderCreateWithColor() {

        java.util.List<String> colors = colorParam.isEmpty()
                ? java.util.Collections.emptyList()
                : Arrays.asList(colorParam.split(","));


        Order order = new Order();
        order.setFirstName("Test");
        order.setLastName("User");
        order.setAddress("Test address");
        order.setMetroStation("4");
        order.setPhone("+79991234567");
        order.setRentTime(1);
        order.setDeliveryDate(TestData.getFutureDate());
        order.setComment("Test comment");
        order.setColor(colors);

        Response response = orderApi.createOrder(order);


        response.then()
                .statusCode(SC_CREATED)
                .body("track", expectTrack ? notNullValue() : nullValue());
    }
}
