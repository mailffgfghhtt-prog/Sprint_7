package helpers;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.given;

public class ApiClient {
    public static final String BASE_URL = "http://qa-scooter.praktikum-services.ru";
    public static RequestSpecification getRequestSpec() {
        RestAssuredConfig config = RestAssured.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 10000)
                        .setParam("http.socket.timeout", 10000));
        return given()
                .baseUri(BASE_URL)
                .header("Content-Type", "application/json")
                .config(config);
    }
}