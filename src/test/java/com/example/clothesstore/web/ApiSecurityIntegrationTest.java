package com.example.clothesstore.web;

import com.example.clothesstore.category.CategoryRepository;
import com.example.clothesstore.color.ColorRepository;
import com.example.clothesstore.image.ImageRepository;
import com.example.clothesstore.product.ProductCategoryRepository;
import com.example.clothesstore.product.ProductRepository;
import com.example.clothesstore.product.jpa.Product;
import com.example.clothesstore.support.TestFixtures;
import com.example.clothesstore.textile.TextileRepository;
import com.example.clothesstore.user.Role;
import com.example.clothesstore.user.UserRepository;
import com.example.clothesstore.size.SizeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Сквозные тесты HTTP-слоя на реальном Tomcat (random port) и H2:
 * права доступа, авторизация, валидация, коды ошибок, CORS и лимит страницы.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiSecurityIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private TextileRepository textileRepository;

    @Autowired
    private ColorRepository colorRepository;

    @Autowired
    private SizeRepository sizeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private HttpClient client;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        client = HttpClient.newBuilder()
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    // ===== публичные vs защищённые эндпоинты =====

    @Test
    void productsArePublic() throws Exception {
        HttpResponse<String> response = get("/api/products");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"content\"");
    }

    @Test
    void anonymousCannotListUsers() throws Exception {
        HttpResponse<String> response = get("/api/users");

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.headers().firstValue("Content-Type")).hasValueSatisfying(
                value -> assertThat(value).startsWith("application/json"));
        assertThat(response.body()).contains("message");
    }

    @Test
    void anonymousCannotCreateProduct() throws Exception {
        HttpResponse<String> response = postJson("/api/admin/products", """
                {"name":"Hack","basePrice":100}
                """);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void meRequiresAuthentication() throws Exception {
        assertThat(get("/api/auth/me").statusCode()).isEqualTo(401);
    }

    // ===== аутентификация =====

    @Test
    void loginWithUnknownEmailReturns401() throws Exception {
        HttpResponse<String> response = login("nobody-" + UUID.randomUUID() + "@test.local",
                TestFixtures.DEFAULT_PASSWORD);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        String email = uniqueEmail();
        TestFixtures.user(userRepository, passwordEncoder, email, Role.USER);

        assertThat(login(email, "definitely-wrong").statusCode()).isEqualTo(401);
    }

    @Test
    void regularUserGets403OnAdminEndpoint() throws Exception {
        String email = uniqueEmail();
        TestFixtures.user(userRepository, passwordEncoder, email, Role.USER);

        assertThat(login(email, TestFixtures.DEFAULT_PASSWORD).statusCode()).isEqualTo(200);
        assertThat(get("/api/users").statusCode()).isEqualTo(403);
    }

    @Test
    void adminCanListUsers() throws Exception {
        String email = uniqueEmail();
        TestFixtures.user(userRepository, passwordEncoder, email, Role.ADMIN);

        assertThat(login(email, TestFixtures.DEFAULT_PASSWORD).statusCode()).isEqualTo(200);
        assertThat(get("/api/users").statusCode()).isEqualTo(200);
    }

    // ===== регистрация =====

    @Test
    void registerRejectsInvalidEmailAndShortPassword() throws Exception {
        HttpResponse<String> response = postJson("/api/auth/register", """
                {"email":"not-an-email","password":"123"}
                """);

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat(response.body()).contains("email");
    }

    @Test
    void registerLogsUserInAndHidesPassword() throws Exception {
        String email = uniqueEmail();

        HttpResponse<String> register = postJson("/api/auth/register", """
                {"email":"%s","password":"%s","firstName":"Иван"}
                """.formatted(email, TestFixtures.DEFAULT_PASSWORD));

        assertThat(register.statusCode()).isEqualTo(201);
        assertThat(register.body()).contains(email).doesNotContain("password");

        HttpResponse<String> me = get("/api/auth/me");
        assertThat(me.statusCode()).isEqualTo(200);
        assertThat(me.body()).contains(email);
    }

    @Test
    void registeredUserIsNotAdmin() throws Exception {
        String email = uniqueEmail();
        postJson("/api/auth/register", """
                {"email":"%s","password":"%s"}
                """.formatted(email, TestFixtures.DEFAULT_PASSWORD));

        assertThat(get("/api/users").statusCode()).isEqualTo(403);
    }

    // ===== ошибки =====

    @Test
    void unknownProductReturns404Not500() throws Exception {
        HttpResponse<String> response = get("/api/products/999999");

        assertThat(response.statusCode()).isEqualTo(404);
        assertThat(response.body()).contains("not found");
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        loginAsAdmin();

        HttpRequest request = HttpRequest.newBuilder(uri("/api/admin/products/1"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString("{oops"))
                .build();

        assertThat(send(request).statusCode()).isEqualTo(400);
    }

    // ===== админский CRUD и скидки =====

    @Test
    void adminCreatesProductWithDiscountAndItAppearsOnStorefront() throws Exception {
        loginAsAdmin();
        String name = "Тестовый пуховик " + UUID.randomUUID();

        HttpResponse<String> create = postJson("/api/admin/products", """
                {"name":"%s","gender":"UNISEX","season":"WINTER","basePrice":1000,"discountedPrice":800}
                """.formatted(name));

        assertThat(create.statusCode()).isEqualTo(201);
        assertThat(create.body()).contains(name).contains("discountedPrice");

        HttpResponse<String> list = get("/api/products");
        assertThat(list.statusCode()).isEqualTo(200);
        assertThat(list.body()).contains(name).contains("discountedPrice");
    }

    @Test
    void pageSizeIsCappedAtConfiguredMaximum() throws Exception {
        List<Product> products = IntStream.range(0, 150)
                .mapToObj(i -> TestFixtures.product("Товар " + i + " " + UUID.randomUUID()))
                .toList();
        productRepository.saveAll(products);

        HttpResponse<String> response = get("/api/products?size=1000");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"size\":100");
        assertThat(response.body()).contains("\"totalElements\":150");
    }

    // ===== CORS =====

    @Test
    void corsPreflightAllowsConfiguredSpaOrigin() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri("/api/products"))
                .timeout(Duration.ofSeconds(10))
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "GET")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = send(request);

        assertThat(response.statusCode()).isIn(200, 204);
        assertThat(response.headers().firstValue("Access-Control-Allow-Origin"))
                .contains("http://localhost:3000");
    }

    // ===== helpers =====

    private void loginAsAdmin() throws Exception {
        String email = uniqueEmail();
        TestFixtures.user(userRepository, passwordEncoder, email, Role.ADMIN);
        assertThat(login(email, TestFixtures.DEFAULT_PASSWORD).statusCode()).isEqualTo(200);
    }

    private String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@test.local";
    }

    private HttpResponse<String> login(String email, String password) throws Exception {
        return postJson("/api/auth/login", """
                {"email":"%s","password":"%s"}
                """.formatted(email, password));
    }

    private HttpResponse<String> get(String path) throws Exception {
        return send(HttpRequest.newBuilder(uri(path))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build());
    }

    private HttpResponse<String> postJson(String path, String json) throws Exception {
        return send(HttpRequest.newBuilder(uri(path))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build());
    }

    private HttpResponse<String> send(HttpRequest request) throws Exception {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private void cleanDatabase() {
        productCategoryRepository.deleteAll();
        productRepository.deleteAll();
        imageRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        textileRepository.deleteAll();
        colorRepository.deleteAll();
        sizeRepository.deleteAll();
    }
}
