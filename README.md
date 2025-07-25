# Evaluation Management System

## Overview

The Evaluation Management System is designed for colleges and companies to streamline the process of evaluating students or candidates. It enables teachers and interviewers to create subjects, define competencies, assign marks, and manage evaluation rules efficiently.

---

## Problem Statement

Teachers/interviewers should be able to:

- Create subjects (e.g., Cloud Computing, Data Structures, Networking, Signal Processing).
- Add evaluation rules for each subject (e.g., LinkedLists in DS = 8 marks, Arrays in DS = 7 marks).
- Assign multiple competencies to each subject and set marks for each.
- Edit or delete subjects and competencies. Deleting a subject removes its competencies.

---

## Features

- Modular backend with Java, Spring Boot, and Spring Data.
- Custom exception handling and input validation (e.g., marks must be 0-10).
- Uses MySQL for persistent storage.
- Utilizes Lombok and MapStruct for cleaner code.
- Logging via Log4j or SLF4J.
- Handles null pointer exceptions gracefully.
- Service layer encapsulates all business logic.
- Readable, maintainable, and well-documented code.

---

## Backend Architecture

- **Language & Frameworks:** Java, Spring Boot, Spring Data
- **Database:** MySQL
- **Libraries:** Lombok, MapStruct, Log4j/SLF4J
- **Validation:** Annotation-based (e.g., @Min, @Max)
- **Exception Handling:** Custom exceptions for business logic and validation errors
- **Logging:** All major actions and errors are logged
- **Service Layer:** All requests are processed through the service layer
- **Code Quality:** Modular functions, readable code, and comprehensive documentation

---

## Frontend Architecture

- **Frameworks:** React.js
- **Design:** Modernized UI following Industrial Standards
- **Design Rationale:** Each design is explained for usability and aesthetics
- **Event Instrumentation:** All user actions are tracked for analytics and debugging
- **Debugging:** Tools and practices for effective debugging during interviews
- **Industry Standards:** Follows best practices.

---

## Getting Started

### Backend Setup

1. Clone the repository.
2. Configure MySQL database in `src/main/resources/application.properties`.
3. Build and run the Spring Boot application:
   ```shell
   ./mvnw spring-boot:run
   ```
4. API endpoints are available for managing subjects and competencies.

### Frontend Setup

1. Navigate to the [frontend directory](https://github.com/KOWSIK-M/Grade-Management-System-Frontend) .
2. Install dependencies:
   ```shell
   npm install
   ```
3. Start the development server:
   ```shell
   npm run dev
   ```

---

## API Endpoints (Backend)

- `POST /subjects` - Create a new subject
- `GET /subjects` - List all subjects
- `PUT /subjects/{id}` - Edit a subject
- `DELETE /subjects/{id}` - Delete a subject and its competencies
- `POST /competencies` - Add a competency to a subject
- `PUT /competencies/{id}` - Edit a competency
- `DELETE /competencies/{id}` - Delete a competency

---

## Validation Rules

- Marks must be between 0 and 10
- Subject and competency names must not be empty
- All input is validated using annotations

---

## Exception Handling

- Custom exceptions for invalid input, not found, and business logic errors
- All exceptions are logged

---

## Logging

- Uses Log4j or SLF4J for logging all major actions and errors

---

## Code Quality

- Modular, small functions
- Readable and well-documented code
- Follows high industry standards

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Open a pull request

---

## License

This project is licensed under the MIT License.

---

## Contact

For questions or support, please open an issue or contact the maintainer.
