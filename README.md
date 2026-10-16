# Secure REST API на Java

Лабораторная работа №1 по дисциплине «Информационная безопасность». Проект выполнен на Java 17, Spring Boot, Spring Security, Spring Data JPA и H2.

## API

| Метод | Путь | Доступ | Назначение |
|---|---|---|---|
| `POST` | `/auth/login` | открытый | Проверка логина/пароля и выдача JWT |
| `GET` | `/api/data` | Bearer JWT | Список заметок текущего пользователя |
| `POST` | `/api/notes` | Bearer JWT | Создание заметки |

## Запуск

```bash
export JWT_SECRET='replace-with-at-least-32-random-characters'
export INITIAL_ADMIN_USERNAME='student'
export INITIAL_ADMIN_PASSWORD='StrongPassword!2026'
mvn spring-boot:run
```

База H2 будет создана в `data/`. Пароль и JWT-секрет не хранятся в репозитории.

## Примеры curl

```bash
curl -s -X POST http://127.0.0.1:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"student","password":"StrongPassword!2026"}'

curl -s http://127.0.0.1:8080/api/data \
  -H 'Authorization: Bearer <JWT>'

curl -s -X POST http://127.0.0.1:8080/api/notes \
  -H 'Authorization: Bearer <JWT>' \
  -H 'Content-Type: application/json' \
  -d '{"title":"Первая заметка","content":"Данные API"}'
```

## Меры защиты

- **SQL Injection:** Spring Data JPA использует параметризованные запросы; SQL не собирается конкатенацией строк.
- **XSS:** заголовок и текст заметки экранируются методом `HtmlUtils.htmlEscape` перед возвратом.
- **Broken Authentication:** пароль хэшируется `BCryptPasswordEncoder` с cost 12. JWT подписывается HS256, содержит `iss`, `sub`, `iat`, `exp` и действует 30 минут.
- **Access Control:** Spring Security запрещает анонимный доступ ко всем маршрутам, кроме `/auth/login`.
- **Validation:** DTO ограничивают пустые значения и длину полей.

## Проверки

```bash
mvn test
mvn compile spotbugs:spotbugs spotbugs:check
mvn dependency-check:check
```

GitHub Actions запускает тесты, SpotBugs (SAST) и OWASP Dependency-Check (SCA) при каждом push и pull request. Отчёты сохраняются в артефакте `security-reports`.
