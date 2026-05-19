# Meeting Room MCP Server

## Overview

This project implements a Model Context Protocol (MCP) server that wraps the existing internal meeting room reservation system (`espora-min-ps-ofc`). The MCP server enables Claude Desktop users to interact with the meeting room reservation system using natural language commands.

## Project Status

### Phase 1 Complete
- ✅ Basic project structure and Maven configuration
- ✅ Spring Boot 3.3.5 + Spring AI 1.1.6 setup
- ✅ Basic MCP server configuration with Streamable HTTP transport
- ✅ Backend API client with referer header handling
- ✅ Office DTO and API response wrappers
- ✅ ThreadLocal-based employee number context management
- ✅ Basic Spring MVC configuration with interceptor

### Current Implementation
The project now includes:
1. Complete Maven project structure
2. Spring Boot application with proper configuration
3. Backend API client for communicating with legacy tablet API
4. DTO classes for API responses
5. Context management for employee numbers via HTTP headers
6. Basic MCP tool infrastructure

## Architecture

```
[Claude Desktop]
    │ HTTP POST /mcp (Streamable HTTP)
    │ Headers:
    │   x-emp-no: 12345  ← Employee Number
    ▼
[meeting-room-mcp (This Project)]
    │ 1. HandlerInterceptor extracts x-emp-no → ThreadLocal storage
    │ 2. Spring AI MCP starter dispatches tools
    │ 3. Tool methods execute (Phase 2 implementation)
    │ 4. BackendApiClient calls existing system
    │    (Automatically attaches Referer header, passes employee number)
    ▼
[espora-min-ps-ofc (Legacy System)]
    │ MtgrmTabletController (referer validation)
    │   → MtgrmResveController (actual business logic)
    ▼
[MariaDB]
```

## Key Features

- **Natural Language Interface**: Users can query and manage meeting rooms using natural language
- **MCP Integration**: Built with Spring AI MCP Server for seamless Claude Desktop integration
- **Streamable HTTP**: Uses Streamable HTTP protocol for real-time communication
- **Secure Wrapper**: Wraps the existing tablet API with proper authentication headers and validation
- **ThreadSafe Context**: Uses ThreadLocal for safe employee number management

## Implementation Status

### Completed
- ✅ Maven project structure with all required dependencies
- ✅ Basic Spring Boot application with proper configuration
- ✅ Backend API client configuration
- ✅ DTO classes for API responses
- ✅ ThreadLocal-based employee number context management
- ✅ MVC configuration with interceptors
- ✅ Configuration Properties with proper validation

### In Progress
- ✅ Implementation of `list_offices` tool (Phase 1 completion)

### Planned for Future Phases
- ✅ Implementation of other read operations (`list_meeting_rooms`, `check_availability`, `list_reservations`, `search_employees`)
- ✅ Implementation of write operations (`create_reservation`, `cancel_reservation`)
- ✅ Full MCP tool implementation with proper descriptions and validations

## Requirements

- Java 17+
- Spring Boot 3.x
- Spring AI 1.1.6
- Existing `espora-min-ps-ofc` tablet API system

## Setup Instructions

### Environment Variables

Set the following environment variables:
- `BACKEND_BASE_URL` - Base URL of the existing tablet API
- `BACKEND_REFERER` - Referer header for tablet API validation
- `TZ` - Timezone setting (e.g., Asia/Seoul)

### Docker Deployment

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/meeting-room-mcp-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Configuration

Configure in `application.yml`:
```yaml
spring:
  application:
    name: meeting-room-mcp
  ai:
    mcp:
      server:
        name: meeting-room
        version: 1.0.0
        type: SYNC
        protocol: STREAMABLE
        instructions: |
          These tools provide access to the corporate meeting room reservation system.
          Functions include checking room availability, viewing reservations, and creating/cancelling bookings.
          Meeting room names typically follow the format "18th floor 3rd conference room".
          Creating and cancelling reservations are destructive operations that require user confirmation.
        streamable-http:
          mcp-endpoint: /mcp
          keep-alive-interval: 30s

backend:
  api:
    base-url: ${BACKEND_BASE_URL:http://localhost:8081}
    referer: ${BACKEND_REFERER:http://localhost:8081/com/smartofc/mtgTablet_list.do}
    connect-timeout: 5s
    read-timeout: 15s

server:
  port: 8080
```

## Usage with Claude Desktop

Configure Claude Desktop connector settings:

```json
{
  "mcpServers": {
    "meeting-room": {
      "type": "http",
      "url": "http://[MCP_SERVER_ADDRESS]:8080/mcp",
      "headers": {
        "x-emp-no": "[YOUR_EMPLOYEE_NUMBER]"
      }
    }
  }
}
```

## Development Phases

### Phase 1: Basic Setup (Complete)
- Create project structure
- Implement basic MCP server with `list_offices` tool
- Set up backend API client with referer header handling
- Create DTO classes for API responses

### Phase 2: Read Operations (In Progress)
- Implement `list_meeting_rooms`, `check_availability`, `list_reservations`, `search_employees`
- Handle fallback scenarios for date conflicts in tablet API

### Phase 3: Write Operations (Planned)
- Implement `create_reservation` and `cancel_reservation` tools
- Add proper validation and confirmation requirements

### Phase 4: Production Deployment (Planned)
- Containerize with Docker
- Configure nginx reverse proxy
- Apply security hardening

## Security Considerations

This system operates in an internal network environment with the following considerations:
- No authentication is performed at the MCP layer
- Employee number is passed via HTTP header `x-emp-no`
- All communication is over secure channels
- Referer header validation is maintained for compatibility with legacy systems

## Building and Running

```bash
# Build the project
mvn clean package

# Run locally
mvn spring-boot:run

# Run tests
mvn test
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is proprietary to the company and not licensed for public use.