# API-справочник

Базовый URL: `http://localhost:8080` (в docker-compose — `http://backend:8080` внутри сети).

- Формат — JSON, `UTF-8`. Исключение: загрузка изображений — `multipart/form-data`.
- Аутентификация — cookie-сессия `JSESSIONID`. Куки ставятся при `/api/auth/login` и
  `/api/auth/register`, дальше браузер/клиент передаёт их автоматически.
- Все даты в ответах отсутствуют; денежные значения — числа (`numeric(10,2)`).

Оглавление:

- [Аутентификация и права](#аутентификация-и-права)
- [Общие соглашения](#общие-соглашения)
- [Модели ответов](#модели-ответов)
- [Эндпоинты](#эндпоинты)
- [Перечисления](#перечисления)
- [Полный пример](#полный-пример)

---

## Аутентификация и права

| Роль | Как получить |
|---|---|
| аноним | без входа |
| `USER` | `POST /api/auth/register` |
| `ADMIN` | роль выставляется в БД (поле `users.role`) |

Логин:

```http
POST /api/auth/login
Content-Type: application/json

{"email":"admin@example.com","password":"password123"}
```

Ответ `200` + `Set-Cookie: JSESSIONID=...`, тело — `UserDto`.

Выход: `POST /api/auth/logout` → `204`, сессия инвалидируется.

Матрица доступа:

| Путь | Доступ |
|---|---|
| `POST /api/auth/register`, `POST /api/auth/login` | публично |
| `GET /api/auth/me`, `POST /api/auth/logout` | любой авторизованный |
| `/api/users/**` | `ADMIN` |
| `/api/admin/**` | `ADMIN` |
| `POST /api/images`, `POST /api/images/batch` | `ADMIN` |
| `GET /api/images/**` | публично |
| `GET /api/products/**`, `GET /api/categories/**` | публично |
| `GET /files/**`, `GET /actuator/health` | публично |
| всё остальное | любой авторизованный |

Неавторизованный запрос к защищённому эндпоинту → `401`, недостаточная роль → `403`.
Оба ответа в JSON:

```json
{"message": "Требуется авторизация"}
```

CORS: разрешены origin'ы из `APP_CORS_ALLOWED_ORIGINS` (по умолчанию
`http://localhost:3000`), `allowCredentials=true`, методы `GET/POST/PUT/PATCH/DELETE/OPTIONS`.

---

## Общие соглашения

### Пагинация

Эндпоинты, принимающие `Pageable`, отдают `PagedModel`:

```json
{
  "content": [ ... ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 57,
    "totalPages": 3
  }
}
```

Параметры: `page` (с 0), `size` (по умолчанию 20, **максимум 100**), `sort=поле,asc|desc`.

### Списки по id

`GET .../by-ids?ids=1,2,3` — максимум **100** идентификаторов. Несуществующие id
молча пропускаются, порядок ответа повторяет порядок запроса (для товаров).

### PATCH

`PATCH` — частичное обновление: `null`/отсутствующее поле означает «не менять».
Снять скидку (`discountedPrice = null`) через PATCH нельзя — только перезаписать значением.

### Ошибки

Единый формат:

```json
{"message": "Product with id `42` not found"}
```

| Код | Когда |
|---|---|
| `400` | ошибка валидации, битый JSON, неверный тип параметра |
| `401` | нет сессии, неверный email/пароль |
| `403` | недостаточно прав |
| `404` | сущность не найдена |
| `409` | дубликат (email, slug, name, sku, комбинация цвет+размер, повторная привязка) |
| `413` | превышен размер загружаемого файла |
| `500` | непредвиденная ошибка (логируется на сервере) |

Пример ошибки валидации:

```json
{"message": "email: must be a well-formed email address; password: size must be between 8 and 72"}
```

---

## Модели ответов

### UserDto

```json
{
  "id": 1,
  "email": "user@example.com",
  "role": "USER",
  "firstName": "Иван",
  "lastName": "Петров",
  "phone": "+79990000000"
}
```

Пароль и `createdAt/updatedAt` наружу не отдаются.

### CategoryDto / CategoryRefDto

```json
{"id":1,"name":"Одежда","slug":"clothing","description":"Вся одежда","parentId":null,"sortOrder":0,"active":true}
```

```json
{"id":7,"name":"Худи и свитшоты","slug":"hoodies"}
```

### ProductShortDto (витрина)

```json
{
  "id": 11,
  "name": "Худи oversize с начёсом",
  "gender": "UNISEX",
  "season": "ALL_SEASON",
  "basePrice": 4990.00,
  "discountedPrice": 3990.00,
  "previewImageUrl": "/files/2026-10-13/abc.jpg"
}
```

`previewImageUrl` — картинка с `isMain = true`, либо первая по `sortOrder`, либо `null`.

### ProductDto (карточка товара)

```json
{
  "id": 1,
  "name": "Пуховик зимний оверсайз",
  "gender": "UNISEX",
  "season": "WINTER",
  "basePrice": 12990.00,
  "discountedPrice": 10390.00,
  "description": "Тёплый пуховик...",
  "variants": [
    {
      "id": 10,
      "sku": "COAT-BLACK-M",
      "color": {"id": 1, "code": "BLACK", "name": "Чёрный", "hex": "#000000"},
      "size": {"id": 3, "code": "M", "label": "Medium", "system": "LETTER", "sortOrder": 3},
      "stockQuantity": 5,
      "price": 10390.00
    }
  ],
  "textiles": [
    {"id": 1, "textileId": 6, "textileName": "Полиэстер", "percentage": 100, "primaryFabric": true}
  ],
  "images": [
    {
      "id": 1,
      "sortOrder": 0,
      "isMain": true,
      "image": {"id": 9, "url": "/files/2026-10-13/abc.jpg", "altText": null, "width": 364, "height": 549}
    }
  ]
}
```

### TextileDto

```json
{
  "id": 6,
  "name": "Полиэстер",
  "description": "Прочная синтетическая ткань",
  "composition": "100% полиэстер",
  "densityGsm": 180,
  "weaveType": "WOVEN",
  "stretch": "LOW",
  "manufacturer": "CHINA",
  "careInstructions": "Машинная стирка 40°C",
  "priceModifier": 0.80,
  "active": true
}
```

### ColorDto

```json
{"id":1,"code":"BLACK","name":"Чёрный","hex":"#000000"}
```

### SizeDto

```json
{"id":3,"code":"M","label":"Medium","system":"LETTER","sortOrder":3}
```

### ImageResponse

```json
{"id":9,"url":"/files/2026-10-13/abc.jpg","altText":null,"width":364,"height":549}
```

---

## Эндпоинты

### Аутентификация — `/api/auth`

| Метод | Путь | Доступ | Ответ |
|---|---|---|---|
| `POST` | `/api/auth/register` | публично | `201 UserDto` + сессия |
| `POST` | `/api/auth/login` | публично | `200 UserDto` + сессия |
| `POST` | `/api/auth/logout` | auth | `204` |
| `GET` | `/api/auth/me` | auth | `200 UserDto` / `401` |

`POST /api/auth/register`:

```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "Иван",
  "lastName": "Петров"
}
```

Ограничения: `email` — валидный и ≤100 символов, `password` — 8..72 символа,
`firstName`/`lastName` — ≤50. Дубликат email → `409`, после регистрации пользователь
сразу авторизован.

### Пользователи — `/api/users` (только `ADMIN`)

| Метод | Путь | Ответ |
|---|---|---|
| `GET` | `/api/users` | `200 List<UserDto>` |
| `GET` | `/api/users/{id}` | `200 UserDto` |
| `GET` | `/api/users/by-email/{email}` | `200 UserDto` |
| `POST` | `/api/users` | `201 UserDto` |
| `PUT` | `/api/users/{id}` | `200 UserDto` |
| `DELETE` | `/api/users/{id}` | `204` |

`POST` — `{"email":"...","password":"..."}`.
`PUT` — `{"email":"...","phone":"...","firstName":"...","lastName":"..."}`
(поля профиля перезаписываются целиком, роль не меняется).

### Витрина товаров — `/api/products` (публично)

| Метод | Путь | Ответ |
|---|---|---|
| `GET` | `/api/products?page=&size=&sort=` | `200 PagedModel<ProductShortDto>` |
| `GET` | `/api/products/{id}` | `200 ProductDto` |
| `GET` | `/api/products/by-ids?ids=1,2` | `200 List<ProductShortDto>` |
| `GET` | `/api/products/{productId}/categories` | `200 List<CategoryRefDto>` |

Сортировка по умолчанию — `id asc`.

### Админка товаров — `/api/admin/products` (только `ADMIN`)

| Метод | Путь | Ответ |
|---|---|---|
| `GET` | `/api/admin/products` | `200 PagedModel<ProductShortDto>` |
| `GET` | `/api/admin/products/{id}` | `200 ProductDto` |
| `GET` | `/api/admin/products/by-ids?ids=` | `200 List<ProductShortDto>` |
| `POST` | `/api/admin/products` | `201 ProductDto` |
| `PATCH` | `/api/admin/products/{id}` | `200 ProductDto` |
| `DELETE` | `/api/admin/products/{id}` | `204` |
| `DELETE` | `/api/admin/products?ids=` | `204` |
| `POST` | `/api/admin/products/{productId}/variants` | `201 ProductDto` |
| `DELETE` | `/api/admin/products/{productId}/variants/{variantId}` | `200 ProductDto` |
| `POST` | `/api/admin/products/{productId}/images` | `201 ProductDto` |
| `PATCH` | `/api/admin/products/{productId}/images/{productImageId}` | `200 ProductDto` |
| `DELETE` | `/api/admin/products/{productId}/images/{productImageId}` | `200 ProductDto` |
| `GET` | `/api/admin/products/{productId}/categories` | `200 List<CategoryRefDto>` |
| `POST` | `/api/admin/products/{productId}/categories` | `201 List<CategoryRefDto>` |
| `PATCH` | `/api/admin/products/{productId}/categories/{productCategoryId}` | `200 List<CategoryRefDto>` |
| `DELETE` | `/api/admin/products/{productId}/categories/{productCategoryId}` | `204` |

Создание товара:

```json
{
  "name": "Пуховик зимний оверсайз",
  "gender": "UNISEX",
  "season": "WINTER",
  "basePrice": 12990.00,
  "discountedPrice": 10390.00,
  "description": "Тёплый пуховик..."
}
```

`name` обязателен (≤255, уникален), цены ≥ 0, `description` ≤ 10 000.
Варианты, картинки и ткани добавляются отдельными запросами.

Вариант:

```json
{"sku":"COAT-BLACK-M","colorId":1,"sizeId":3,"stockQuantity":5,"price":10390.00}
```

`sku` обязателен и уникален, `stockQuantity` ≥ 0, `colorId`/`sizeId` — опциональны.

Прикрепление картинки:

```json
{"imageId":9,"altText":"Пуховик, вид спереди","sortOrder":0,"isMain":true}
```

`imageId` обязателен. `isMain: true` снимает флаг с остальных картинок товара.

Обновление привязки картинки:

```json
{"altText":"новый alt","sortOrder":2,"isMain":false}
```

Прикрепление ткани:

```json
{"textileId":6,"percentage":100,"primaryFabric":true}
```

`percentage` — 0..100; `primaryFabric: true` снимает флаг с остальных тканей.

Привязка категории:

```json
{"categoryId":16,"sortOrder":0,"isPrimary":true}
```

`categoryId` обязателен; `isPrimary: true` снимает флаг с остальных категорий.
Ответ — актуальный список категорий товара.

### Витрина категорий — `/api/categories` (публично)

| Метод | Путь | Ответ |
|---|---|---|
| `GET` | `/api/categories` | `200 List<CategoryDto>` — только `active`, по `sortOrder` |
| `GET` | `/api/categories/{slug}` | `200 CategoryDto` / `404` |

### Админка категорий — `/api/admin/categories` (только `ADMIN`)

| Метод | Путь | Ответ |
|---|---|---|
| `GET` | `/api/admin/categories` | `200 List<CategoryDto>` — все, включая неактивные |
| `GET` | `/api/admin/categories/{id}` | `200 CategoryDto` |
| `POST` | `/api/admin/categories` | `201 CategoryDto` |
| `PATCH` | `/api/admin/categories/{id}` | `200 CategoryDto` |
| `DELETE` | `/api/admin/categories/{id}` | `204` |

Создание/обновление:

```json
{
  "name": "Худи и свитшоты",
  "slug": "hoodies",
  "description": "Худи, свитшоты, толстовки",
  "parentId": 1,
  "sortOrder": 0
}
```

`name` и `slug` уникальны. `PATCH` дополнительно принимает `active`.

### Ткани — `/api/admin/textiles` (только `ADMIN`)

| Метод | Путь | Ответ |
|---|---|---|
| `GET` | `/api/admin/textiles?page=&size=` | `200 PagedModel<TextileDto>` |
| `GET` | `/api/admin/textiles/{id}` | `200 TextileDto` |
| `GET` | `/api/admin/textiles/by-ids?ids=` | `200 List<TextileDto>` |
| `POST` | `/api/admin/textiles` | `201 TextileDto` |
| `PATCH` | `/api/admin/textiles/{id}` | `200 TextileDto` |
| `PATCH` | `/api/admin/textiles?ids=1,2` | `200 List<Integer>` |
| `DELETE` | `/api/admin/textiles/{id}` | `204` |
| `DELETE` | `/api/admin/textiles?ids=` | `204` |

Тело создания/частичного обновления — поля `TextileDto` (кроме `id`).
Bean-validation на ткани пока не настроена.

### Цвета — `/api/admin/colors` (только `ADMIN`)

| Метод | Путь | Ответ |
|---|---|---|
| `GET` | `/api/admin/colors` | `200 List<ColorDto>` |
| `GET` | `/api/admin/colors/{id}` | `200 ColorDto` |
| `GET` | `/api/admin/colors/by-ids?ids=` | `200 List<ColorDto>` |
| `POST` | `/api/admin/colors` | `201 ColorDto` |
| `PATCH` | `/api/admin/colors/{id}` | `200 ColorDto` |
| `DELETE` | `/api/admin/colors/{id}` | `204` |
| `DELETE` | `/api/admin/colors?ids=` | `204` |

Создание: `{"code":"BLACK","name":"Чёрный","hex":"#000000"}`.
`code` ≤20 и уникален, `name` ≤50 и уникален, `hex` — `#RRGGBB`.

### Изображения — `/api/images`

| Метод | Путь | Доступ | Ответ |
|---|---|---|---|
| `POST` | `/api/images` | `ADMIN` | `201 ImageResponse` (новый) / `200` (файл уже был) |
| `POST` | `/api/images/batch` | `ADMIN` | `201 List<ImageResponse>` |
| `GET` | `/api/images?page=&size=` | публично | `200 PagedModel<ImageResponse>` |

Загрузка одного файла (`multipart/form-data`, поле `file`):

```bash
curl -b cookies.txt -X POST http://localhost:8080/api/images -F "file=@coat.jpg"
```

Батч (поле `files`, до 20 файлов, до 50 МБ на запрос):

```bash
curl -b cookies.txt -X POST http://localhost:8080/api/images/batch \
  -F "files=@1.jpg" -F "files=@2.jpg"
```

Дубликаты определяются по SHA-256: одинаковый файл не сохраняется второй раз.
Разрешены JPEG/PNG/GIF/BMP, проверка — по содержимому. Файл >10 МБ → `413`.

### Статика и служебное

| Метод | Путь | Доступ | Описание |
|---|---|---|---|
| `GET` | `/files/{storageKey}` | публично | отдача загруженного файла, `Cache-Control: public, max-age=30d` |
| `GET` | `/actuator/health` | публично | healthcheck (используется Docker) |

---

## Перечисления

| Enum | Значения |
|---|---|
| `Gender` | `MAN`, `WOMAN`, `UNISEX` |
| `Season` | `SUMMER`, `WINTER`, `DEMI_SEASON`, `ALL_SEASON` |
| `Role` | `USER`, `ADMIN` |
| `SizeSystem` | `LETTER`, `EU`, `US`, `UK`, `KIDS`, `SHOES` |
| `WeaveType` | `WOVEN`, `KNITTED`, `NON_WOVEN`, `LACE`, `FELT` |
| `Stretch` | `NONE`, `LOW`, `MEDIUM`, `HIGH` |
| `Manufacturer` | `RUSSIA`, `CHINA`, `TURKEY`, `BELARUS` |

---

## Полный пример

Сценарий: админ логинится, загружает картинку, создаёт товар со скидкой,
прикрепляет картинку и категорию, затем смотрит витрину.

```bash
# 1. Логин (сохраняем cookie)
curl -s -c cookies.txt -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"password123"}'

# 2. Загрузка картинки
curl -s -b cookies.txt -X POST http://localhost:8080/api/images \
  -F 'file=@coat.jpg'
# → {"id":9,"url":"/files/2026-11-01/....jpg","altText":null,"width":364,"height":549}

# 3. Создание товара
curl -s -b cookies.txt -X POST http://localhost:8080/api/admin/products \
  -H 'Content-Type: application/json' \
  -d '{"name":"Пуховик тестовый","gender":"UNISEX","season":"WINTER",
       "basePrice":12990,"discountedPrice":10390,"description":"Тёплый"}'
# → 201, {"id":57, ... "discountedPrice":10390}

# 4. Прикрепление картинки (главной)
curl -s -b cookies.txt -X POST http://localhost:8080/api/admin/products/57/images \
  -H 'Content-Type: application/json' \
  -d '{"imageId":9,"isMain":true}'

# 5. Привязка к категории "Верхняя одежда" (id=2)
curl -s -b cookies.txt -X POST http://localhost:8080/api/admin/products/57/categories \
  -H 'Content-Type: application/json' \
  -d '{"categoryId":2,"isPrimary":true}'

# 6. Витрина
curl -s 'http://localhost:8080/api/products?size=20'
# → {"content":[{"id":57,"name":"Пуховик тестовый","previewImageUrl":"/files/...","discountedPrice":10390}],"page":{...}}
```

Проверка прав:

```bash
# без сессии
curl -i http://localhost:8080/api/users
# HTTP/1.1 401, {"message":"Требуется авторизация"}

# обычный пользователь
curl -i -b user-cookies.txt http://localhost:8080/api/users
# HTTP/1.1 403, {"message":"Недостаточно прав"}
```
