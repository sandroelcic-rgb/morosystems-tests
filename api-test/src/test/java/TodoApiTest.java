import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;

class TodoApiTest {

    @BeforeAll
    static void configureRestAssured() {

        /*
        ** Connecting to a port exposed by the todo-api container.
        */
        RestAssured.baseURI = "http://todo-api:8080";
    }


    @Test
    void api_rest_task() {

        /*
        ** First retrieve the list of tasks - there should be 3.
        */
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/tasks")
                .then()
                .statusCode(200)
                .body("$", hasSize(3));

        /*
        ** Then create a new task and store the ID.
        */
        String id = given()
                .contentType(ContentType.JSON)
                .body("{\"text\":\"assignment-create\"}")
                .when()
                .post("/tasks")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("text", equalTo("assignment-create"))
                .body("completed", equalTo(false))
                .extract()
                .path("id");

        /*
        ** Then try to update the task with some checks.
        */
        try {
            given()
                    .contentType(ContentType.JSON)
                    .body("{\"text\":\"assignment-update\"}")
                    .when()
                    .post("/tasks/" + id)
                    .then()
                    .statusCode(200)
                    .body("id", equalTo(id))
                    .body("text", equalTo("assignment-update"));
        } finally {

        /*
        ** Finally block to be sure that we attempt to delete even if the update fails.
        */

            given()
                    .when()
                    .delete("/tasks/" + id)
                    .then()
                    .statusCode(200);

        /*
        ** In the end, check that the task is deleted.
        */

            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/tasks")
                    .then()
                    .statusCode(200)
                    .body("$", hasSize(3))
                    .body("findAll { it.id == '" + id + "' }", hasSize(0));
        }
    }
}
