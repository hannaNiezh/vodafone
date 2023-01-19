import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

public class SearchTest {
        private final String SEARCH_URL_AUTOCOMPLETE = "https://www.vodafone.ua/api/search/autocomplete/";
        private final String SEARCH_URL = "https://www.vodafone.ua/api/search/";
        @DataProvider(name="validSearchData")
        public static Object[][] getValidSearchData() {
                return new Object[][]{
                        {"tel", "test fingerprint"},
                        {"телефон", "Телефонуйте"},
                        {"допомога", "Допомога Збройним Силам України та клієнтам"}
                };
        }
        @Test(dataProvider = "validSearchData")
        public void verifyValidSearchResultForAutoComplete(String data, String expectedResult) {
                Response response = RestAssured.get(SEARCH_URL_AUTOCOMPLETE +  data);
                response.then()
                        .statusCode(HttpStatus.SC_OK)
                        .and()
                        .body("data", isA(List.class));
                response.jsonPath().get("data[0]").toString().equals(expectedResult);
        }
        @DataProvider(name="invalidSearchData")
        public static Object[][] getInvalidSearchData() {
                return new Object[][]{
                        {"new", Collections.EMPTY_LIST},
                        {"но", Collections.EMPTY_LIST},
                        {"фон", Collections.EMPTY_LIST}
                };
        }
        @Test(dataProvider = "invalidSearchData")
        public void verifyInvalidSearchResultForAutoComplete(String data, List expectedResult) {
                Response response = RestAssured.get(SEARCH_URL_AUTOCOMPLETE +  data);
                response.then()
                        .statusCode(HttpStatus.SC_OK)
                        .and()
                        .body("data", isA(List.class));
                assertThat(response.jsonPath().get("data"), equalTo(expectedResult));
        }

        @DataProvider(name="searchData")
        public static Object[][] getSearchData() {
                return new Object[][]{
                        {"дзвінки", 10},
                        {"телефон", 10},
                        {"допомог", 10}
                };
        }
        @Test(dataProvider = "searchData")
        public void verifySearchResultOnPage(String data, Integer expectedCount) {
                Response response =  RestAssured.given().queryParams(new HashMap(){{
                                put("page", 1);}})
                        .get(SEARCH_URL +  data);
                response.then()
                        .statusCode(HttpStatus.SC_OK);
                Long actualCount =  response.jsonPath().getList("data").stream()
                        .filter(searchResult -> ((Map) searchResult).get("short_description").toString().toLowerCase().contains(data) ||
                                ((Map) searchResult).get("title").toString().toLowerCase().contains(data)).count();
                assertThat(actualCount.intValue(), equalTo(expectedCount));
        }
}
