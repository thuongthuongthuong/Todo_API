# Todo API

A RESTful API for managing a to-do list application with task dependencies, caching, notifications, and containerized deployment using Docker. This project showcases a robust backend solution with performance optimization and modern development practices.

---

## Features

### 1. Task Management
- Create tasks with `title`, `description`, `due date`, and `priority`.
- Retrieve a paginated list of tasks with optional filtering by status.
- Update task details and status.
- Delete tasks.

### 2. Task Dependencies
- Define dependencies between tasks (e.g., Task B depends on Task A).
- Add and remove dependencies via dedicated endpoints.
- Retrieve all dependencies (direct and indirect) for a given task using recursive traversal.

### 3. Dependency Validation
- Detect circular dependencies using a Depth-First Search (DFS) algorithm.
- Prevent creation of circular dependencies with appropriate error responses.

### 4. Caching
- Implement Redis-based caching for frequently accessed data (task lists and dependencies).
- Cache eviction on task creation, updates, and deletions to ensure data consistency.

### 5. Notification System
- Background scheduler checks for upcoming (within 24 hours) and overdue tasks every 60 seconds.
- Notifications are logged to the console (extensible to email or other channels).

### 6. Deployment
- Containerized application with Docker and Docker Compose.
- Includes PostgreSQL for persistent storage and Redis for caching.

---

## Prerequisites

- **Docker** and **Docker Compose** (recommended for deployment).
- **Java 17** and **Maven** (for local development without Docker).
- **Git** (to clone the repository).

---

## Setup Instructions

### Option 1: Using Docker Compose (Recommended)
Run the application, PostgreSQL, and Redis in containers.

1. **Clone the repository**:
   ```bash
   git clone https://github.com/<your-username>/todo-api.git
   cd todo-api
   ```

2. **Build and run:**
   ```bash
   docker-compose up --build
   ```

   - Application: http://localhost:8080
   - PostgreSQL: localhost:5432 (exposed for debugging)
   - Redis: localhost:6380 (mapped from container port 6379 to avoid conflicts)

3. **Stop the application:**
   ```bash
   docker-compose down
   ```
   Use `docker-compose down -v` to remove volumes and reset data.

### Option 2: Local Development (Without Docker)
Run the application locally with PostgreSQL and Redis installed on your machine.

1. **Set up dependencies:**
   - PostgreSQL: Install and run on `localhost:5432`.
     - Database: `todo_db`
     - User: `postgres`
     - Password: `291003`
   - Redis: Install and run on `localhost:6379`.
   - Update `src/main/resources/application.properties` if ports differ.

2. **Build and run:**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

---

## API Endpoints

| Method | Endpoint | Description | Request Body / Query Params |
|--------|---------|-------------|-----------------------------|
| POST | `/api/tasks` | Create a new task | `{"title": "string", "description": "string", "dueDate": "yyyy-MM-ddTHH:mm:ss", "priority": "LOW/MEDIUM/HIGH", "status": "PENDING/IN_PROGRESS/COMPLETED"}` |
| GET | `/api/tasks?page={page}&size={size}&status={status}` | List tasks (paginated, filtered) | `page` (int), `size` (int), `status` (optional: PENDING/IN_PROGRESS/COMPLETED) |
| PUT | `/api/tasks/{id}` | Update a task | Same as POST body |
| DELETE | `/api/tasks/{id}` | Delete a task | - |
| POST | `/api/tasks/{taskId}/dependencies` | Add a dependency | Query param: `dependsOnId={id}` |
| GET | `/api/tasks/{taskId}/dependencies` | Get all dependencies of a task | - |

---

## Example Requests

### Create a Task
```bash
curl -X POST http://localhost:8080/api/tasks \
-H "Content-Type: application/json" \
-d '{"title":"Test Task","description":"A test","dueDate":"2025-03-25T09:00:00","priority":"HIGH","status":"PENDING"}'
```

### List Tasks
```bash
curl http://localhost:8080/api/tasks?page=0&size=10
```

### Add a Dependency
```bash
curl -X POST http://localhost:8080/api/tasks/1/dependencies?dependsOnId=2
```

### Get Dependencies
```bash
curl http://localhost:8080/api/tasks/1/dependencies
```

---

## Notes

- **Caching**: Task lists and dependencies are cached in Redis. Cache is cleared on task creation, updates, deletions, or dependency changes.
- **Notifications**: Upcoming and overdue tasks are logged every 60 seconds to the console. Extensible to email or WebSocket.
- **Circular Dependencies**: Returns a `400 Bad Request` with message *"Circular dependency detected"* if a circular reference is attempted.
- **Database**: PostgreSQL stores tasks and dependencies with automatic schema generation (`spring.jpa.hibernate.ddl-auto=update`).

---

## Technologies Used

- **Backend**: Spring Boot 3.2.x
- **Database**: PostgreSQL 15
- **Caching**: Redis 7
- **Containerization**: Docker, Docker Compose
- **Build Tool**: Maven

---

## Project Structure
```
todo-api/
├── src/
│   ├── main/
│   │   ├── java/org/example/todoapi/
│   │   │   ├── config/         # Configuration (Redis, Scheduler)
│   │   │   ├── entity/         # JPA entities (Task, TaskDependency)
│   │   │   ├── exception/      # Custom exceptions
│   │   │   ├── repository/     # JPA repositories
│   │   │   ├── service/        # Business logic (TaskService, NotificationService)
│   │   └── resources/
│   │       └── application.properties  # Configuration file
├── Dockerfile                  # Docker configuration for the app
├── docker-compose.yml          # Docker Compose setup
├── pom.xml                     # Maven dependencies
└── README.md                   # Project documentation
```

---

## Performance Optimization

- **Caching**: Redis reduces database load for frequent queries (`getTasks`, `getAllDependencies`).
- **Pagination**: Task listing uses Spring Data's `Pageable` for efficient handling of large datasets.

### Future Improvements:
- Add database indexes on `Task.dueDate` and `Task.status` for scalability.
- Optimize recursive dependency queries with batch fetching for millions of records.

---

## Challenges and Solutions

- **Serialization Issues**: Fixed errors serializing `Page<Task>` with Redis by using `JdkSerializationRedisSerializer` instead of Jackson JSON.
- **Docker Networking**: Resolved *"Connection refused"* errors by using service names (`db`, `redis`) instead of `localhost`.
- **Port Conflicts**: Mapped Redis to `6380` on the host to avoid conflicts with local Redis instances.

