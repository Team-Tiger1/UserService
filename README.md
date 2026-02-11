# User Service

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

This service handles:
- Users
- Vendors
- Authentication

The User Service handles all user and vendor details and generates tokens for clients, so they can access endpoints on all the services. It manages logins and registering and accessing general vendor data. It receives messages from the RabbitMQ instance when a User streak needs to be updated.

## Documentation

[![Swagger Docs](https://img.shields.io/badge/Swagger-OpenAPI%20Docs-85EA2D?style=for-the-badge&logo=openapi-initiative&logoColor=black)](https://thelastfork.shop/api/userservice/docs)

## Tech Stack

### Core & Build

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Maven](https://img.shields.io/badge/Apache%20Maven-%23C71A36.svg?style=for-the-badge&logo=Apache%20Maven&logoColor=white)

### Database
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)

### Deployment
![Kubernetes](https://img.shields.io/badge/kubernetes-%23326ce5.svg?style=for-the-badge&logo=kubernetes&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

### Messaging
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-%23FF6600.svg?style=for-the-badge&logo=rabbitmq&logoColor=white)

### Testing
![JUnit 5](https://img.shields.io/badge/Junit5-%2325A162.svg?style=for-the-badge&logo=junit5&logoColor=white)


## How to Run Tests
> Intructions for **all microservices** can be found on the [**LocalDeployment**](https://github.com/Team-Tiger1/LocalDeployment) repo but the below instructions are for running just the User service tests
### Requirements for running tests
- **Git**
- **Java JDK 17+**

## Run Tests

### Run Tests (Windows)

1. Open Terminal, Clone and open this repository
```Bash
  git clone https://github.com/Team-Tiger1/userservice

  cd userservice
```

2. run the following command to run tests
```Bash
    ./mvnw.cmd test
```
3. If successful you should see somthing similar to
```Bash
[INFO] Results:
[INFO]
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:33 min
[INFO] Finished at: 2026-02-11T21:48:58Z
[INFO] ------------------------------------------------------------------------
```

###  Run Tests (Linux/MacOs)
1. Clone this repository
``` Bash
    git clone https://github.com/Team-Tiger1/userservice

  cd userservice
```

2. run the following commands to give access for maven to be executable and to run tests
```Bash
    chmod +x mvnw
    ./mvnw test
```

3. If successful you should see somthing similar to
```Bash
[INFO] Results:
[INFO]
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:33 min
[INFO] Finished at: 2026-02-11T21:48:58Z
[INFO] ------------------------------------------------------------------------
```



## Contribution

**Author: Daniel Jackson**
- Setup Authentication Components:
    - Created Refresh and Access Token Generation using Secret Key
    - Created methods to extract user type and Id from access token
    - Created endpoint for users and vendors to refresh their access token
- Setup User Components:
    - Created User Endpoints and specified the data required in the request
    - Validated incoming request data at the DTO layer
    - Defined the User and Streak database tables and linked them using Spring Boot JPA
    - Wrote custom SQL queries using JPARepository
    - Wrote business logic in the service layer that accesses the database
    - Defined Custom Exceptions to improve visibility in the logs
    - Setup a RabbitMQ Listener that receives updates to the streak
    - Created a Component that generates a unique username using random adjectives and nouns
- Setup Vendor Components:
    - Created Vendor Endpoints and specified the data required in the request
    - Validated incoming request data at the DTO layer
    - Defined the Vendor database table using Spring Boot JPA
    - Wrote business logic in the Vendor Service layer that accesses the database
- Added OpenAPI documentation to improve visibility of the backend for the front-end developers
- Enforced Controller-Service-Repository model to improve consistency across services for developers
- Setup Spring Boot Profiles to manage configurations for production, development and testing environments
- Used Maven Licensing Plugin to check permissions of dependency licenses (Software Inventory)
- Created README file to show details about the repository

<br>

**Author: Jed Leas**

- Setting up all CI/CD workflows to handle
  1. Automatic testing on push of main branch on the user service repo
  2. Automatic Deployment onto k3s with zero downtime on compleation of automatic testing so broken code won't make it to deployment
- Set up the connection to the Postgres database, and RabbitMq



**Author: Ivy Figari**
