# Reactive and non-blocking REST API with Spring WebFlux
This project for implement Reactive and Non-blocking applications with Spring WebFlux framework.

### What you will learn
- Use Spring Security to **protect** your Spring WebFlux application
- Implement user login (**authentication**)
- Generate and use **JSON Web Tokens** (JWT) for secure communication
- **Validate JWT** and read token claims
- Use **method-level security** annotations to perform authorization
- Write and read from a database in reactive/non-blocking way
- Write Spring Security Expressions (SpEL)

### How to open H2 database console
Open this command
```
http://localhost:8082
```
Using username and password from application.properties file

### onError operators
- **onErrorReturn()** - Replaces an error with a single, predefined value.
- **onErrorResume()** - Replaces an error with an alternative publisher.
- **onErrorComplete()** - Transforms an error into a completion signal.
- **onErrorContinue()** - Recovers from errors by dropping the erroneous element and continuing the sequence.
- **onErrorMap()** - Transforms the error into a different error.
- **onErrorStop()** - Tells the stream to stop processing immediately.

### Key Features of WebClient in Spring WebFlux
- Non-Blocking Asynchronous Communication
- Support HTTP/2
- Reactive Streaming with Flux and Mono
- Automatic Service Discovery with Spring Cloud
- Built-in Support for Custom Headers and Parameters
- Error Handling with onErrorResume and onStatus
- Load Balancing with Spring Cloud
- Customizable Request and Response Filters
- Built-in JSON and XML Support
- Timeout and Retry Mechanisms