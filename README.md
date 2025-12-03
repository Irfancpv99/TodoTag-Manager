# TodoTag-Manager

A  desktop todo management application built with Java Swing,  practices including Test-Driven Development (TDD), dual database support, and  CI/CD integration.


##Build & Coverage

[![Coverage Status](https://coveralls.io/repos/github/Irfancpv99/TodoTag-Manager/badge.svg?branch=main)](https://coveralls.io/github/Irfancpv99/TodoTag-Manager?branch=main)  [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Irfancpv99_TodoTag-Manager&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=Irfancpv99_TodoTag-Manager)   

##Code Quality

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Irfancpv99_TodoTag-Manager&metric=coverage)](https://sonarcloud.io/summary/new_code?id=Irfancpv99_TodoTag-Manager)   [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=Irfancpv99_TodoTag-Manager&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=Irfancpv99_TodoTag-Manager)   [![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=Irfancpv99_TodoTag-Manager&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=Irfancpv99_TodoTag-Manager)

##Security & Reliability

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Irfancpv99_TodoTag-Manager&metric=bugs)](https://sonarcloud.io/summary/new_code?id=Irfancpv99_TodoTag-Manager)   [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Irfancpv99_TodoTag-Manager&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=Irfancpv99_TodoTag-Manager)   [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Irfancpv99_TodoTag-Manager&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=Irfancpv99_TodoTag-Manager)  [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Irfancpv99_TodoTag-Manager&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=Irfancpv99_TodoTag-Manager)   [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Irfancpv99_TodoTag-Manager&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=Irfancpv99_TodoTag-Manager)



##  Overview

TodoTag-Manager is a desktop application developed as part of an Advanced Software Development course project, showcasing professional-grade software engineering practices. The application provides a complete todo management system with tagging functionality, dual database support (MongoDB and MySQL), and a testing suite achieving 100% code coverage.

This project was built following strict Test-Driven Development (TDD) methodology, with every feature being test-first developed to ensure reliability and maintainability.

##  Key Features

### Core Functionality
- **Todo Management**: Create, read, update, and delete todo items
- **Task Status**: Mark tasks as complete or incomplete with visual feedback
- **Tag System**: Organize todos with custom tags
- **Tag Management**: Create, assign, and remove tags from todos
- **Search & Filter**: Find todos by description or filter by completion status
- **Double-click Toggle**: Quick status changes via double-click on table rows

### Technical Features
- **Dual Database Support**: Seamlessly switch between MongoDB and MySQL
- **Transaction Management**: Proper ACID compliance for MySQL operations
- **Repository Pattern**: Clean separation of data access logic
- **Service Layer**: Business logic isolation for better maintainability
- **MVC Architecture**: Clear separation of concerns in GUI layer
- **Comprehensive Testing**: Unit, integration, and end-to-end tests

##  Technology Stack
### Core Technologies
- **Java 17**: Modern Java features and performance
- **Maven**: Dependency management and build automation
- **Swing**: Cross-platform GUI framework

### Databases
- **MongoDB 6.0**: NoSQL document database for flexible schema
- **MySQL 8.0**: Relational database with JPA/Hibernate integration

### Persistence Layer
- **Hibernate 6.2.2**: ORM framework for MySQL
- **Jakarta Persistence API 3.1**: JPA specification
- **MongoDB Java Driver 4.9.1**: Native MongoDB support

### Testing Frameworks
- **JUnit 5.9.3**: Unit testing framework
- **Mockito 4.11.0**: Mocking framework for unit tests
- **AssertJ Swing 3.17.1**: GUI testing framework
- **Testcontainers 1.18.0**: Integration testing with real databases

### Quality Assurance
- **JaCoCo 0.8.8**: Code coverage analysis
- **PITest 1.14.1**: Mutation testing
- **SonarCloud**: Code quality and security analysis
- **Coveralls**: Coverage reporting and tracking

### CI/CD
- **GitHub Actions**: Automated build, test, and analysis pipeline
- **Docker Compose**: Local development environment setup

##  Architecture

The application follows a layered architecture pattern:

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│  (MainFrame, MainFrameController)   │
├─────────────────────────────────────┤
│          Service Layer              │
│         (TodoService)               │
├─────────────────────────────────────┤
│        Repository Layer             │
│  (TodoRepository, TagRepository)    │
├─────────────────────────────────────┤
│       Persistence Layer             │
│    (MongoDB / MySQL with JPA)       │
└─────────────────────────────────────┘
```

### Design Patterns Used
- **Repository Pattern**: Data access abstraction
- **Factory Pattern**: Database-specific repository creation
- **MVC Pattern**: GUI structure and separation of concerns
- **Strategy Pattern**: Swappable database implementations
- **Transaction Script**: Service layer operations

##  Getting Started

### Prerequisites

- **Java Development Kit (JDK) 17** or higher
- **Maven 3.8+** for building the project
- **Docker & Docker Compose** (optional, for local database setup)
- **Git** for version control

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Irfancpv99/TodoTag-Manager.git
   cd TodoTag-Manager
   ```

2. **Set up databases** (choose one option)

   **Option A: Using Docker Compose** (Recommended)
   ```bash
   docker-compose up -d
   ```

   **Option B: Manual Setup**
   - Install MongoDB locally (port 27017)
   - Install MySQL locally (port 3307)
   - Create database: `todoapp`

3. **Configure the application**
   
   Edit `src/main/resources/application.properties`:
   ```properties
   # Choose database type: MONGODB or MYSQL
   database.type=MONGODB

   # MongoDB settings (if using MongoDB)
   mongodb.host=localhost
   mongodb.port=27017
   mongodb.database=todoapp

   # MySQL settings (if using MySQL)
   mysql.url=jdbc:mysql://localhost:3307/todoapp
   mysql.username=todouser
   mysql.password=todopassword
   ```

4. **Build the project**
   ```bash
   mvn clean install
   mvn clean verify
   ```

5. **Run the application**
   ```bash
   mvn exec:java
   ```

##  Usage Guide

### Basic Operations

#### Creating a Todo
1. Enter a description in the "Add Todo" field
2. Click "Add Todo" button or press Enter
3. The todo appears in the table with status "false"

#### Managing Todo Status
- **Mark as Complete/Incomplete**: Select a todo and click "Toggle Done"
- **Quick Toggle**: Double-click on any todo in the table

#### Editing a Todo
1. Select a todo from the table
2. Click "Edit Todo" button
3. Modify the description in the dialog
4. Click OK to save changes

#### Deleting a Todo
1. Select a todo from the table
2. Click "Delete Todo" button
3. The todo is permanently removed

### Working with Tags

#### Creating Tags
1. Enter a tag name in the "Add Tag" field
2. Click "Add Tag" button or press Enter
3. Tag appears in the "All Available Tags" list

#### Assigning Tags to Todos
1. Select a todo from the table
2. Select a tag from the "All Available Tags" list
3. Click "Add Tag to Todo" button
4. Tag appears in the "Todo Tags" list for that todo

#### Removing Tags from Todos
1. Select a todo from the table
2. Select a tag from the "Todo Tags" list
3. Click "Remove Tag from Todo" button

#### Deleting Tags
1. Select a tag from the "All Available Tags" list
2. Click "Delete Selected Tag" button
3. Tag is removed from all todos and deleted

### Search and Filter

#### Searching Todos
1. Enter keywords in the "Search" field
2. Click "Search" button or press Enter
3. Table displays matching todos

#### Showing All Todos
- Click "Show All" button to reset search and display all todos

##  Testing

The project maintains **100% code coverage** with zero surviving mutants in mutation testing.

### Running Tests

**Run all tests:**
```bash
mvn clean test
```

**Run with coverage report:**
```bash
mvn clean test jacoco:report
```
Coverage report: `target/site/jacoco/index.html`

**Run mutation testing:**
```bash
mvn clean test org.pitest:pitest-maven:mutationCoverage
```
Mutation report: `target/pit-reports/index.html`

### Test Categories

#### Unit Tests
- **Model Tests**: `Todo` and `Tag` entity validation
- **Repository Tests**: Database operations with mocks
- **Service Tests**: Business logic validation
- **Controller Tests**: GUI controller behavior
- **Configuration Tests**: Application configuration handling

#### Integration Tests
- **Repository Integration**: Real database operations using Testcontainers
- **Service Integration**: End-to-end service flows with both databases

#### End-to-End Tests
- **GUI Tests**: Complete user workflows using AssertJ Swing
- **Headless Testing**: CI-compatible GUI tests with Xvfb

### Test Coverage Requirements

Per project requirements:
- Line Coverage: **100%**
- Branch Coverage: **100%**
- Mutation Coverage: **100%** (0 surviving mutants)

##  CI/CD Pipeline

The project uses GitHub Actions for continuous integration and deployment.

### Pipeline Stages

1. **Test & Coverage**
   - Runs all tests with MongoDB and MySQL services
   - Generates JaCoCo coverage reports
   - Uploads results to Coveralls
   - Performs SonarCloud analysis

2. **Mutation Testing** (main branch only)
   - Runs PITest mutation analysis
   - Uploads mutation reports

3. **Build**
   - Creates application JAR
   - Uploads build artifacts

### Quality Gates

- All tests must pass
- Code coverage must be 100%
- No new code smells or bugs (SonarCloud)
- Mutation testing score must be 100%

### Running CI Locally

You can simulate the CI environment locally:

```bash
# Start database services
docker-compose up -d

# Run tests with coverage
mvn clean test jacoco:report

# Run mutation tests
mvn test org.pitest:pitest-maven:mutationCoverage

# Build the application
mvn clean package -DskipTests
```

##  Database Configuration

### MongoDB Configuration

**Connection String Format:**
```
mongodb://host:port/database
```

**Default Configuration:**
```properties
mongodb.host=localhost
mongodb.port=27017
mongodb.database=todoapp
```

**Collections Created:**
- `todos`: Todo documents
- `tags`: Tag documents

### MySQL Configuration

**JDBC URL Format:**
```
jdbc:mysql://host:port/database?options
```

**Default Configuration:**
```properties
mysql.url=jdbc:mysql://localhost:3307/todoapp?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
mysql.username=todouser
mysql.password=todopassword
```

**Tables Created** (via Hibernate auto-DDL):
- `todos`: Todo entities
- `tags`: Tag entities
- `todo_tags`: Join table for many-to-many relationship

### Switching Databases

Simply change the `database.type` property in `application.properties`:

```properties
# For MongoDB
database.type=MONGODB

# For MySQL
database.type=MYSQL
```

##  Project Structure

```
TodoTag-Manager/
├── src/
│   ├── main/
│   │   ├── java/com/todoapp/
│   │   │   ├── TodoApplication.java          # Application entry point
│   │   │   ├── config/                       # Configuration classes
│   │   │   │   
│   │   │   ├── gui/                          # GUI layer
│   │   │   │   
│   │   │   ├── model/                        # Domain models
│   │   │   │   
│   │   │   ├── repository/                   # Data access layer
│   │   │   │   ├── mongo/                   # MongoDB implementations
│   │   │   │   └── mysql/                   # MySQL implementations
│   │   │   └── service/                      # Business logic layer
│   │   └── resources/
│   │       ├── application.properties       # Application configuration
│   │       └── META-INF/
│   │           └── persistence.xml          # JPA configuration
│   └── test/
│       └── java/com/todoapp/               # Test classes mirror main structure
│           ├── config/                     # Configuration tests
│           ├── gui/                        # GUI tests (unit + E2E)
│           ├── model/                      # Model tests
│           ├── repository/                 # Repository tests
│           └── service/                    # Service tests
├── .github/
│   └── workflows/
│       └── ci.yml                          # CI/CD pipeline configuration
├── docker-compose.yml                       # Local development databases
├── pom.xml                                  # Maven configuration
└── README.md                                # This file
```

##  Development Practices

### Test-Driven Development (TDD)

This project strictly follows the **Red-Green-Refactor** cycle:

1. **Red**: Write a failing test first
2. **Green**: Write minimal code to make it pass
3. **Refactor**: Improve code while keeping tests green