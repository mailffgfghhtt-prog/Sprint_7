package steps;


import io.qameta.allure.Step;

public class StepAnnotations {

    @Step("Создание курьера с логином {login}")
    public static void createCourier(String login) {}

    @Step("Авторизация курьера {login}")
    public static void loginCourier(String login) {}

    @Step("Создание заказа с треком {track}")
    public static void createOrder(String track) {}

    @Step("Удаление заказа по треку {track}")
    public static void deleteOrder(String track) {}

    @Step("Получение списка заказов")
    public static void getOrdersList() {}
}
