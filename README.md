# 📡 Smart Home Telemetry Platform

Smart Home Telemetry Platform — это распределённая микросервисная система для сбора, обработки и анализа данных «умного дома» и управления сценариями автоматизации.
Система принимает данные от датчиков, агрегирует состояния, проверяет пользовательские сценарии и выполняет действия через Kafka и gRPC.
Дополнительно реализован коммерческий домен (Commerce Subsystem) — заказ, оплата и доставка, построенные на тех же архитектурных принципах.

⸻

## ⚙️ Используемые технологии и фреймворки

Проект построен на современном Java-стеке, объединяющем микросервисную архитектуру, событийное взаимодействие и надёжную сериализацию данных.

### Основные технологии:
	•	☕ Java 17 — основной язык разработки
	•	🧱 Maven (multi-module) — управление зависимостями и структурой проекта
	•	🌱 Spring Boot 3.x — каркас микросервисов
	•	🌐 Spring Web / Spring MVC — реализация REST API
	•	📨 Apache Kafka — обмен телеметрией между сервисами
	•	🧬 Avro и Protocol Buffers (Protobuf) — бинарная сериализация данных
	•	🔗 gRPC — двоичное взаимодействие между сервисами (Analyzer ↔ HubRouterController)
	•	🗄️ PostgreSQL — хранение сценариев, заказов и оплат
	•	🧮 Spring Data JPA / Hibernate — ORM-уровень для работы с базой данных
	•	🧭 Netflix Eureka — Service Discovery и балансировка
	•	⚙️ Spring Cloud Config — централизованная конфигурация микросервисов
	•	🚪 Spring Cloud Gateway — маршрутизация и проксирование REST-запросов
	•	🤝 OpenFeign — интеграция между сервисами commerce-домена
	•	🐳 Docker / Docker Compose — контейнеризация и инфраструктура
	•	📜 OpenAPI / Swagger — описание REST-интерфейсов
	•	🧪 JUnit 5 / Spring Test — модульное и интеграционное тестирование
	•	🪵 SLF4J / Logback — логирование

⸻

### 💡 Основная идея проекта

Система объединяет все уровни работы с умными устройствами:
она принимает телеметрию от датчиков и хабов, сериализует данные в Avro и публикует их в Kafka, агрегирует состояние устройств в снапшоты, анализирует сценарии и при необходимости выполняет действия через gRPC.
Отдельная commerce-подсистема демонстрирует расширяемость архитектуры и применение тех же технологий к бизнес-домену заказов и логистики.

⸻

### 🧱 Архитектура и структура проекта

Проект организован как многомодульное Maven-приложение, включающее два основных домена: Telemetry Core и Commerce Subsystem.

⸻

### 🛰️ Telemetry Core

Назначение: обработка телеметрии, агрегация данных и анализ сценариев «умного дома».

Модули:
	•	📥 telemetry/collector — REST API для приёма JSON, сериализация в Avro и публикация в Kafka
	•	⚙️ telemetry/aggregator — консьюмер Kafka, собирающий состояния датчиков в снапшоты
	•	🧠 telemetry/analyzer — анализ сценариев, хранение их в PostgreSQL и выполнение действий через gRPC
	•	🧾 serialization/avro-schemas — набор Avro-схем событий и показаний
	•	🧩 serialization/proto-schemas — Protobuf-схемы для gRPC между Analyzer и HubRouterController
	•	🛠️ infra — общие конфигурации Kafka, PostgreSQL и Docker

⸻

### 🛒 Commerce Subsystem

Назначение: демонстрация применения микросервисной архитектуры к бизнес-домену e-commerce.

Модули и микросервисы:
	•	🌉 gateway — Spring Cloud Gateway (роутинг /api/v1/**, балансировка)
	•	📦 order-service — управление жизненным циклом заказов и их статусами
	•	💳 payment-service — расчёт стоимости, учёт НДС, статусы оплат и возвраты
	•	🚚 delivery-service — расчёт стоимости и статусов доставки по весу, объёму и адресам
	•	🏭 warehouse-service — сборка товаров и передача их в доставку
	•	🔄 interaction-api — общий модуль DTO для Feign-клиентов
	•	🔍 discovery — Eureka Server (порт 8761)
	•	🧭 config-server — Spring Cloud Config Server (централизованная конфигурация)

⸻

### 🔗 Взаимодействие сервисов

Обмен телеметрией между Collector, Aggregator и Analyzer осуществляется через Apache Kafka.
Analyzer вызывает действия на устройствах по gRPC через HubRouterController.
Commerce-сервисы (order, payment, delivery) взаимодействуют между собой через REST и Feign-клиентов.
Eureka используется для регистрации и поиска сервисов, а Config Server — для централизованного управления настройками.
Все внешние HTTP-запросы проходят через Spring Cloud Gateway, который выполняет маршрутизацию и балансировку нагрузки.

⸻

### 📨 Kafka-топики

В системе используются три основных топика:
	•	telemetry.sensors.v1 — показания датчиков
	•	telemetry.hubs.v1 — события хаба и сценариев
	•	telemetry.snapshots.v1 — агрегированные состояния устройств (снапшоты)

⸻

### 🧠 Логика работы Telemetry Core

Collector
Принимает JSON-запросы от хабов, сериализует данные в Avro и публикует их в Kafka в топики telemetry.sensors.v1 и telemetry.hubs.v1.
Использует низкоуровневый KafkaProducer с ручной конфигурацией параметров.

Aggregator
Слушает Kafka-топик telemetry.sensors.v1, объединяет показания в агрегированные состояния (снапшоты) и публикует их в telemetry.snapshots.v1.

Analyzer
Слушает снапшоты, анализирует их, хранит сценарии и условия в PostgreSQL, проверяет выполнение условий и при их срабатывании вызывает действия устройств через gRPC.
Для вызова используется HubRouterControllerBlockingStub и метод handleDeviceAction.

⸻

### 🧬 Avro-схемы

Пространство имён: ru.yandex.practicum.kafka.telemetry.event

Реализованы следующие типы:

Перечисления:
DeviceTypeAvro, ConditionTypeAvro, ConditionOperationAvro, ActionTypeAvro

События:
DeviceAddedEventAvro, DeviceRemovedEventAvro, ScenarioAddedEventAvro, ScenarioRemovedEventAvro, HubEventAvro

Датчики:
ClimateSensorAvro, LightSensorAvro, MotionSensorAvro, SwitchSensorAvro, TemperatureSensorAvro

Обёртка:
SensorEventAvro (поля id, hubId, timestamp, payload — объединение всех типов датчиков)

⸻

### 🌐 API сервисов

#### Collector
	•	POST /api/v1/collector/sensors
	•	POST /api/v1/collector/hub-events

#### Order
	•	GET /api/v1/order?username={string}
	•	PUT /api/v1/order
	•	POST /api/v1/order/payment
	•	POST /api/v1/order/payment/failed
	•	POST /api/v1/order/delivery
	•	POST /api/v1/order/delivery/failed
	•	POST /api/v1/order/completed
	•	POST /api/v1/order/calculate/total
	•	POST /api/v1/order/calculate/delivery
	•	POST /api/v1/order/assembly
	•	POST /api/v1/order/assembly/failed
	•	POST /api/v1/order/return

#### Payment
	•	POST /api/v1/payment
	•	POST /api/v1/payment/productCost
	•	POST /api/v1/payment/totalCost
	•	POST /api/v1/payment/refund
	•	POST /api/v1/payment/failed

#### Delivery
	•	POST /api/v1/delivery
	•	POST /api/v1/delivery/failed
	•	GET /api/v1/delivery/{id}

⸻

#### 🧭 Архитектурный поток данных

🔹 Collector → Kafka → Aggregator → Kafka → Analyzer → gRPC → HubRouterController
(данные от устройств проходят все уровни обработки и анализа)

🔹 Order → Payment → Delivery → Warehouse
(цепочка бизнес-процессов в commerce-домене)

🔹 Gateway ↔ Eureka ↔ Config Server
(маршрутизация, регистрация и конфигурация сервисов)
