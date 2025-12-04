package steps;

import io.qameta.allure.Step;
import helpers.Order;
public class StepAnnotations {
    @Step("Создание курьера с логином {login}")
    public static void createCourier(String login) {}
    @Step("Авторизация курьера {login}")
    public static void loginCourier(String login) {}
    @Step("Создание заказа: {order}")
    public static void createOrder(Order order) {}
    @Step("Удаление заказа по треку {track}")
    public static void deleteOrder(String track) {}
    @Step("Получение списка заказов")
    public static void getOrdersList() {}
    @Step("Получение заказов курьера {courierId}")
    public static void getOrdersByCourierId(String courierId) {}
    @Step("Получение заказов по станции {stations}")
    public static void getOrdersByNearestStation(String stations) {}
}