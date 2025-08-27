package tests;

import utils.ConfigReader; 
import org.junit.jupiter.api.*;

import io.restassured.RestAssured;
import io.restassured.http.ContentType; 
import io.restassured.response.Response;

import java.nio.file.Files;
import java.nio.file.Paths; 
import static io.restassured.RestAssured.*; 
import static org.hamcrest.Matchers.*; 
import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookingTest {

    private static String token;
    private static int bookingId;

    @BeforeAll
    static void setup() {

        baseURI = ConfigReader.get("baseUrl");
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }



        @Test
        @Order(1)
        void testCreateToken() {

        Response response = given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"" + ConfigReader.get("username") + "\", \"password\":\"" + ConfigReader.get("password") + "\"}")
        .when()
                .post("/auth")
        .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract().response();

        token = response.jsonPath().getString("token");
        assertNotNull(token, "Token não foi gerado!");
        System.out.println("✅ Token gerado com sucesso:");
    }

    @Test
    @Order(2)
    void testGetBookingIds() {

        given()
                .log().all()
        .when()
                .get("/booking")
        .then()
                .log().all()
                .statusCode(200)
                .body("bookingid", not(empty()))
                .body("bookingid", everyItem(greaterThan(0)));

        System.out.println("✅ Lista de bookings consultada com sucesso!");
    }

    @Test
    @Order(3)
    void testCreateBookingFromJSON() throws Exception {

        String jsonBody = Files.readString(Paths.get("src/test/resources/json/create_booking.json"));


        Response response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", "token=" + " " + token)
               .body(jsonBody)
        .when()
                .post("/booking")
        .then()
                .log().all()
                .statusCode(200)
                .extract().response();

        bookingId = response.jsonPath().getInt("bookingid");
        assertTrue(bookingId > 0, "Booking ID inválido");

        System.out.println("✅ Booking criado com ID: " + bookingId + " (token: " + token + ")");
    }

    @Test
    @Order(4)
    void testGetBooking() {
    given()
            .log().all()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
    .when()
            .get("/booking/" + bookingId)
    .then()
            .log().all()
            .statusCode(200)
            .body("firstname", is("Josep"))
            .body("lastname", is("Silva"))
            .body("totalprice", is(123))
            .body("depositpaid", is(true))
            .body("bookingdates", hasKey("checkin"))
            .body("bookingdates", hasKey("checkout"))
            .body("additionalneeds", is("Breakfast"));
            
    System.out.println("✅ Booking consultado com sucesso!");
}
    @Test
    @Order(5)   
    void testUpdateBookingFromJSON() throws Exception {

        String jsonBody = Files.readString(Paths.get("src/test/resources/json/update_booking.json"));

        given()
                .log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", "token=" + " " + token)
                .body(jsonBody)
        .when()
                .put("/booking/" + bookingId)
        .then()
                .log().all()
                .statusCode(200)
                .body("firstname", is("Manoel"))
                .body("lastname", is("Souza"))
                .body("totalprice", is(200))
                .body("depositpaid", is(false))
                .body("bookingdates", hasKey("checkin"))
                .body("bookingdates", hasKey("checkout"));

        System.out.println("✅ Booking atualizado ID: " + bookingId + " com os dados: " + jsonBody);
    }

    @Test
    @Order(6)
       void testDeleteBooking() {

        given()
                .log().all()
                .header("Cookie", "token=" + " " + token)
        .when()
                .delete("/booking/" + bookingId)
        .then()
                .log().all()
                .statusCode(201);

        // Verifica que foi deletado
        given()
                .log().all()
                .header("Cookie", "token=" + " " + token)
        .when()
                .get("/booking/" + bookingId)
        .then()
                .log().all()
                .statusCode(404);
        System.out.println("✅ Booking deletado com sucesso! (ID: " + bookingId + " | token: " + token + ")");
    }
}