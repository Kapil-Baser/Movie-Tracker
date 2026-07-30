## Movie Tracker
A Spring Boot application to track movies, keep a watch history, make a movie collection and subscribe to a movie to be notified when it will be out for streaming.

## Features
- User authentication with Spring Security and Google OAuth2
- Browse trending, upcoming and now playing movies
- Organize movies into custom collections
- Search movies by title using PostgreSQL Full-Text Search
- Mark movies as watched
- Track watch history with timestamps
- Subscribe to a movie and get notified via email or Google Calendar

# Key Technical Features
- PostgreSQL Full-Text Search
- Quartz Scheduler for automatic movie collection updates
- OAuth2 authentication with Google
- Flyway database versioning and migrations
- Java Virtual Threads for asynchronous background processing using CompletableFuture
- HTMX-powered dynamic UI without a JavaScript framework
- JPA entity relationships for users, collections, subscriptions, and watch history
- Server-side rendering with Thymeleaf, enhanced using HTMX and HyperScript

## Tech Stack
### Backend
- Java 25+
- Spring Boot
- Spring MVC
- Spring Security (OAuth2, Form Login)
- Spring Data JPA / Hibernate
- Hibernate

### Frontend
- Thymeleaf
- HTMX
- HyperScript
- Tailwind CSS

### Database
- PostgreSQL
- PostgreSQL Full-Text Search (tsvector/tsquery)
- Flyway

### Build & Tools
- Maven
- Git
  
## Screenshots
### Homepage
![homepage](https://github.com/Kapil-Baser/movieapi/blob/main/screenshots/homepage.png)
### Login
![login](https://github.com/Kapil-Baser/movieapi/blob/main/screenshots/login.png)
### Watch History
![Watch-history](https://github.com/Kapil-Baser/movieapi/blob/main/screenshots/watch-history.png)
### Collection
![collection](https://github.com/Kapil-Baser/movieapi/blob/main/screenshots/collection.png)
