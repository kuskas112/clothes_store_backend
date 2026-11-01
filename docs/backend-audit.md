# Аудит бэкенда clothes_store

Ветка: `fix/backend-audit` (от `master` @ `c202967`).

Контекст сборки взят из `D:\JavaProjs`: `docker-compose.yml` + `clothes_store/Dockerfile`
(бэкенд Spring Boot 4.1.1 / Java 26, PostgreSQL 16, Nuxt-фронтенд отдельным контейнером).
Docker-демон на машине не запущен, поэтому образ не собирался; проверены `mvn compile`
и `mvn test` (9 тестов, все зелёные).

Ниже — найденные проблемы: сначала критичные баги, затем N+1 и производительность,
целостность данных, безопасность, архитектурные антипаттерны и эксплуатация.
Для каждой помечено, что уже исправлено в ветке, а что требует отдельного решения.

---

## 1. Критичные баги (исправлены)

| # | Проблема | Почему это баг | Решение |
|---|----------|----------------|---------|
| 1 | `GlobalExceptionHandler.handleGeneric` ловил **все** исключения, включая `ResponseStatusException` | `ExceptionHandlerExceptionResolver` приоритетнее встроенного `ResponseStatusExceptionResolver`, поэтому любой `throw new ResponseStatusException(NOT_FOUND)` уходил клиенту как **500** | Добавлен отдельный `@ExceptionHandler(ResponseStatusException.class)`, сохраняющий статус |
| 2 | `UserDetailsService` бросал `UserNotFoundException` | `DaoAuthenticationProvider` превращает в `BadCredentials` только `UsernameNotFoundException`, остальные `RuntimeException` летят наружу → логин с несуществующим email отдавал **500** | Бросаем `UsernameNotFoundException`; `AuthenticationException` → 401 |
| 3 | `/api/users/**` был полностью публичным (`anyRequest().permitAll()`) | Любой мог получить список всех пользователей, email’ы, удалить или переписать профиль | `/api/users/**` → `hasRole("ADMIN")`, остальное по умолчанию `authenticated()` |
| 4 | `POST /api/images` и `/api/images/batch` были публичными | Анонимная заливка произвольных файлов на диск (заполнение диска, хостинг контента) | Запись/удаление изображений → `ADMIN`, чтение осталось публичным |
| 5 | `ColorController.create/patch` принимали/возвращали JPA-сущность | Mass assignment: `POST {"id": 1, ...}` делал `merge` и перезаписывал чужой цвет | DTO `ColorRequest`/`ColorPatchRequest` + валидация, `id` больше не принимается |
| 6 | Загрузка проверяла только заголовок `Content-Type` от клиента | Можно было сохранить HTML/SVG/произвольные байты как `.jpg` и отдать их с того же origin → stored XSS | Проверка реального формата через `ImageIO`, whitelist JPEG/PNG/GIF/BMP, лимит 40 МП, имя файла санитизируется |
| 7 | `GET /api/auth/me` для анонима бросал `UserNotFoundException` | Клиент получал 404 вместо 401 и не понимал, что нужно логиниться | `UnauthorizedUserException` → 401; `me()` дополнительно отсекает `AnonymousAuthenticationToken` |
| 8 | `ProductShortDto.previewImageUrl` заполнялся только по флагу `isMain` | Если флаг ни у кого не выставлен, витрина получала `null` вместо картинки | Fallback на первую картинку по `sortOrder` |
| 9 | `discountedPrice` нельзя было выставить через API | Поля не было в `ProductCreateRequest`/`ProductPatchRequest`, хотя в БД и `ProductDto` оно есть | Поле добавлено в оба request-DTO |
| 10 | `removeVariant` при несуществующем `variantId` молча возвращал 200 | Клиент считал, что удалил вариант | 404, как у `removeTextile` |
| 11 | `handleGeneric` не логировал исключение | 500-е ошибки было невозможно диагностировать | `log.error("Unhandled exception", ex)` |
| 12 | Дедупликация изображений: `findByChecksum` → `store` → `save` | При конкурентной загрузке одного файла — нарушение unique → **500**, плюс осиротевший файл | `saveAndFlush` + обработка `DataIntegrityViolationException`: вернуть существующую запись и удалить лишний файл |
| 13 | `sortOrder` новой картинки = `images.size()` | После удаления картинки появлялись дубли `sortOrder` | `max(sortOrder) + 1` |
| 14 | `product.addTextile` не поддерживал инвариант «одна основная ткань» | Можно было получить несколько `primaryFabric = true` | При установке флага остальные сбрасываются |
| 15 | Удаление основной категории товара оставляло товар без основной | Витрина/фильтры ломались | После удаления основная назначается первой оставшейся |

Дополнительно: **в проекте не было CORS**, хотя SPA живёт на другом origin (`localhost:3000`),
а compose отдаёт фронтенду `NUXT_PUBLIC_API_BASE` на бэкенд. Браузерные запросы получали
CORS-ошибку, а cookie-сессия не работала бы и при `*`. Добавлен настраиваемый
`CorsConfigurationSource` (`APP_CORS_ALLOWED_ORIGINS`, по умолчанию `http://localhost:3000`,
`allowCredentials = true`).

## 2. N+1 запросы и производительность (исправлены)

| # | Место | Что было | Что стало |
|---|-------|----------|-----------|
| 16 | `ProductService.getAll` (витрина и админка) | `findAll(pageable)` + `toShortDto` по каждому товару: ленивая загрузка `images`, затем `image` → **1 + N + M** запросов | 2 запроса: страница товаров + `findWithImagesByIdIn` с `@EntityGraph` |
| 17 | `ProductService.getMany` | `findAllById` + тот же N+1, порядок ids не сохранялся | Батч-загрузка + сохранение порядка запроса, дубликаты отбрасываются |
| 18 | `ImageController.uploadMany` | На каждый файл `findByChecksum` + `save` → 2N запросов | Один `findByChecksumIn` + дедупликация внутри батча |
| 19 | `ProductCategory.category` был `FetchType.EAGER` | EAGER на `@ManyToOne` — скрытые join’ы в каждом запросе связей | `LAZY` + `@EntityGraph` в `findByProductIdOrderBySortOrderAscIdAsc` |
| 20 | `spring.jpa.open-in-view` (по умолчанию `true`) | Ленивые загрузки «в вью», соединение занято до конца сериализации, N+1 замаскирован | `false`; все DTO собираются в транзакции сервиса |
| 21 | Индексы по FK | PostgreSQL не индексирует FK: seq scan при удалении цвета/размера/ткани/картинки, выборке товаров категории | Миграция `V6__indexes_and_variant_uniqueness.sql` |
| 22 | `Pageable` без ограничений | `?size=1000000` — выкачивание всей таблицы | `spring.data.web.pageable.max-page-size=100`, `@PageableDefault(size = 20, sort = "id")` |
| 23 | `by-ids` без лимита | Огромный список id в query string | `@Size(max = 100)` на параметре |
| 24 | `products` без сортировки по умолчанию | Нестабильная пагинация (порядок строк в PG не гарантирован) | Сортировка по `id` по умолчанию |
| 25 | `spring.jpa.show-sql=true` и `DEBUG` security в базовом конфиге | Шумные логи и утечка деталей аутентификации в проде | `application-dev.properties` / `application-prod.properties` |

## 3. Целостность данных и миграции (исправлены)

| # | Проблема | Решение |
|---|----------|---------|
| 26 | `uk_product_variant_product_color_size (product_id, color_id, size_id)`: в PostgreSQL `NULL` считается разным, поэтому варианты без цвета/размера дублировались | `UNIQUE NULLS NOT DISTINCT` (PG 15+) в `V6` |
| 27 | Нарушения unique/foreign key уходили как 500 | `DataIntegrityViolationException` → 409 с понятным сообщением |
| 28 | `IllegalArgumentException` (например, «категория уже привязана») → 500 | 400/409 через `ResponseStatusException` + общий обработчик |
| 29 | Нет валидации `RegisterRequest`, `LoginRequest`, `CreateUserRequest`, `UpdateUserRequest` | `@Valid`, `@Email`, `@Size`, длина пароля 8–72 (BCrypt обрезает после 72 байт) |
| 30 | `Color.hex` произвольная строка | `@Pattern("^#[0-9A-Fa-f]{6}$")` |
| 31 | `Product.name` / `ProductVariant.sku` / категории: дубликаты → 500 | См. #27 (409) |

## 4. Безопасность: что осознанно **не** исправлено в этой ветке

Это требует согласованных изменений с фронтендом или отдельного решения.

1. **CSRF отключён при аутентификации cookie-сессией.**
   `csrf.disable()` + `JSESSIONID` — классический CSRF-вектор. Включать нужно вместе с
   фронтендом: `CookieCsrfTokenRepository.withHttpOnlyFalse()`, отправка `X-XSRF-TOKEN`,
   `ignoringRequestMatchers("/api/auth/login")`.
2. **Нет rate limiting / lockout** на `/api/auth/login` и `/api/auth/register` — brute force.
   Варианты: Bucket4j в фильтре, `spring-boot-starter-cache` + Caffeine, или nginx `limit_req`.
3. **Аутентификация: сессии в памяти.**
   `JWT_SECRET` в `docker-compose.yml` нигде не используется. Сейчас:
   - при нескольких инстансах бэкенда сессии не шарятся (нужен sticky session);
   - рестарт контейнера разлогинивает всех.
   Варианты: Spring Session JDBC/Redis (проще, cookie остаётся) либо переход на stateless JWT.
4. **Cookie `JSESSIONID`** не имеет явных `SameSite`/`Secure`. За HTTPS в проде:
   `server.servlet.session.cookie.secure=true`, `same-site=lax` (или `strict`).
5. **Нет ограничения попыток/капчи на регистрацию** (спам-аккаунты). Валидация email добавлена,
   но подтверждения email нет.
6. **Медиа с того же origin.** Даже после проверки формата безопаснее отдавать файлы
   с отдельного домена/бакета (S3/MinIO) и `Content-Disposition: inline` + `X-Content-Type-Options: nosniff`.

## 5. Архитектурные антипаттерны (частично исправлены)

| # | Антипаттерн | Статус |
|---|-------------|--------|
| 32 | Контроллер работал с репозиторием напрямую (`ImageController`) | Исправлено: логика в `ImageService` |
| 33 | Сервис принимает маппер параметром (`TextileService.patch(id, dto, mapper)`) | Оставлено; рекомендуется инжектить `TextileMapper` в сервис и не тащить MapStruct в сигнатуры |
| 34 | Три способа сигналить об ошибке: кастомные исключения, `ResponseStatusException`, голый `RuntimeException` | Унифицировано на кастомные/`ResponseStatusException`; `GlobalExceptionHandler` больше не глотает статусы |
| 35 | `ImageSyncRunner` — мёртвый код (нет `@Component`), дублирует `ImageStorageService` и при включении читает все файлы в память в одной транзакции | Оставлено без изменений (может быть чьей-то заготовкой). Если нужен — включать под `@Profile("dev")` и стримить по файлу |
| 36 | `ProductController` и `AdminProductController` дублируют read-эндпоинты | Оставлено; можно оставить один `/api/products` для чтения |
| 37 | Витрина тянет категории отдельным запросом на каждый товар (`/{id}/categories` в цикле) | Есть батч-метод `findByProductIdIn`, но нет эндпоинта; рекомендуется отдавать категории в `ProductShortDto`/`ProductDto` или добавить `GET /api/products/categories?ids=` |
| 38 | Нет публичных справочников цветов/размеров — только `/api/admin/colors` | Рекомендуется `GET /api/colors`, `GET /api/sizes` для фильтров витрины |
| 39 | Нет `@Version` у `ProductVariant.stockQuantity` | При появлении заказов/списаний нужна оптимистичная блокировка, иначе lost update |
| 40 | MapStruct предупреждает об unmapped properties (`images`, `products`, `barcode`) | Можно включить `unmappedTargetPolicy = ERROR` и явно игнорировать поля — меньше сюрпризов при рефакторинге |
| 41 | Тестов практически не было (`contextLoads`, требующий живую БД) | Добавлены H2-профиль + 8 тестов: обработчик ошибок и список товаров/превью/порядок |

## 6. Docker / эксплуатация

В репозитории исправлено (`Dockerfile`):

- кэш Maven-зависимостей (`COPY pom.xml` → `dependency:go-offline` → `COPY src`);
- запуск не от `root` (`USER app`);
- `HEALTHCHECK` на `/actuator/health` (+ зависимость `spring-boot-starter-actuator`);
- `JAVA_OPTS` с `MaxRAMPercentage` и `ExitOnOutOfMemoryError`.

`docker-compose.yml` лежит **вне** репозитория (`D:\JavaProjs\docker-compose.yml`), его нужно
поправить руками:

1. `healthcheck.interval: 1000s` — почти наверняка опечатка (17 минут). Из-за
   `depends_on: condition: service_healthy` бэкенд будет ждать первый healthcheck очень долго.
   → `10s`.
2. `APP_STORAGE_BASE_URL: http://localhost:8080/files` — URL, который уходит в браузер.
   Для клиентов не с localhost он неверный. → `/files` (теперь это дефолт) или публичный домен.
3. `NUXT_PUBLIC_API_BASE: http://backend:8080` — имя `backend` резолвится только внутри
   docker-сети. Если Nuxt ходит в API из браузера, нужен `http://localhost:8080` или
   проксирование через `server/api`/nginx.
4. `JWT_SECRET` не используется приложением — удалить или реализовать JWT.
5. Пароли БД захардкожены в compose — вынести в `.env`.
6. `volumes: ./clothes_store/uploads:/app/uploads` + `USER app` в образе: на Linux bind mount
   будет принадлежать root, и приложение не сможет писать. → именованный volume
   `uploads_data:/app/uploads` или `chown` в entrypoint.
7. Нет healthcheck у `frontend`, нет `restart` политики для зависимостей, нет лимитов памяти.

## 7. Что изменилось в API (важно для фронтенда)

| Эндпоинт | Было | Стало |
|----------|------|-------|
| `/api/users/**` | публично | только `ADMIN` |
| `POST`/`DELETE /api/images/**` | публично | только `ADMIN` |
| `GET /api/auth/me` без сессии | 404 | 401 |
| `POST /api/auth/login` с неверным email | 500 | 401 |
| `POST /api/admin/colors` | принимал `id` | `id` игнорируется/не принимается |
| `DELETE /api/admin/colors/{id}` | 200 + тело | 204 |
| `POST /api/admin/products`, `PATCH` | без `discountedPrice` | поле добавлено (аддитивно) |
| `POST /api/images` | любой `image/*` | только JPEG/PNG/GIF/BMP (проверка содержимого) |
| `?size=` | без ограничения | максимум 100 |
| `GET /api/products` | порядок не определён | сортировка по `id` по умолчанию |

## 8. Как проверить

```bash
# из корня репозитория
mvn test          # 9 тестов, H2, без внешней БД
mvn -DskipTests package
```

Локальный запуск без профиля использует базовый `application.properties`
(`DB_URL`/`DB_USER`/`DB_PASSWORD` из `.env`). Для подробных логов:
`SPRING_PROFILES_ACTIVE=dev`.

## 9. Файлы, изменённые в ветке

```
pom.xml
Dockerfile
src/main/resources/application.properties
src/main/resources/application-dev.properties          (new)
src/main/resources/application-prod.properties         (new)
src/main/resources/db/migration/V6__indexes_and_variant_uniqueness.sql (new)

config/SecurityConfig.java
auth/AuthController.java
auth/AuthService.java
user/UserController.java
user/UserService.java
exception/GlobalExceptionHandler.java
image/ImageController.java
image/ImageService.java                                (new)
image/ImageRepository.java
image/ImageMapper.java
image/LocalImageStorageService.java
color/ColorController.java
color/ColorRequest.java                                (new)
color/ColorPatchRequest.java                           (new)
product/ProductController.java
product/AdminProductController.java
product/ProductService.java
product/ProductRepository.java
product/ProductCategoryService.java
product/ProductCategoryRepository.java
product/jpa/ProductCategory.java
product/dto/ProductMapper.java
product/dto/request/ProductCreateRequest.java
product/dto/request/ProductPatchRequest.java
textile/TextileController.java
config/StaticResourceConfig.java

src/test/resources/application.properties             (new)
src/test/java/.../exception/GlobalExceptionHandlerTest.java       (new)
src/test/java/.../product/ProductServiceIntegrationTest.java      (new)
```
