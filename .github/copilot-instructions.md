# MediConnect AI Coding Guidelines

## Architecture Overview
- **Full-stack application**: React frontend (Vite + Tailwind) in `client/mediconnect-react/`, Spring Boot backend in `server/`
- **API communication**: Frontend calls backend REST APIs on port 8081
- **Data persistence**: MySQL database (`mediconnect` schema), JPA with Hibernate
- **Package structure**: Backend follows `com.cts.mediconnect.{controller,model,repository,service,utils}`

## Development Workflows
- **Client startup**: `cd client/mediconnect-react && npm run dev` (runs on port 5173 by default)
- **Server startup**: `cd server && ./mvnw spring-boot:run` (runs on port 8081)
- **Database**: Ensure MySQL is running with `mediconnect` database, user `root`/`root`
- **Build client**: `npm run build` in client directory
- **Build server**: `./mvnw clean package` in server directory
- **Linting**: `npm run lint` for client, no specific server linting configured

## Coding Conventions
- **Backend**: Use Lombok for boilerplate (e.g., `@Data`, `@AllArgsConstructor` on entities)
- **Entities**: Place in `model/` package, use JPA annotations like `@Entity`, `@Id`, `@GeneratedValue`
- **Repositories**: Extend `JpaRepository<Entity, ID>` in `repository/` package
- **Services**: Business logic in `service/` package, inject repositories with `@Autowired`
- **Controllers**: REST endpoints in `controller/` package, use `@RestController`, `@RequestMapping`
- **Frontend**: Standard React hooks, Tailwind CSS classes for styling
- **Imports**: Use relative imports within modules, absolute for cross-module

## Configuration
- **Database config**: In `server/src/main/resources/application.properties` - update credentials as needed
- **JPA**: `ddl-auto=update` for development (auto-creates tables), `show-sql=true` for debugging
- **CORS**: May need to configure for frontend-backend communication (not yet implemented)

## Testing
- **Backend tests**: JUnit 5 with Spring Boot Test, place in `src/test/java/`
- **Client tests**: No testing framework configured yet

## Deployment
- **Client**: Static build served from backend's `static/` or separate web server
- **Server**: JAR deployment with `./mvnw spring-boot:run` or `java -jar target/server-0.0.1-SNAPSHOT.jar`

Reference key files: `server/pom.xml`, `client/mediconnect-react/package.json`, `server/src/main/resources/application.properties`</content>
<parameter name="filePath">c:\Users\2480003\Documents\mediconnect\.github\copilot-instructions.md