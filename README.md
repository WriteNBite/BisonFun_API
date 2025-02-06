# BisonFun API

![GitHub Release](https://img.shields.io/github/v/release/WriteNBite/BisonFun_API)


A brave new version of [BisonFun](https://github.com/WriteNBite/bisonfun) is now as GraphQL API! Track your watch in a 🏅better, 💨faster, and 💪stronger way. Discover trends and information about your favorite video content and share watchlists with your friends.

## Tech Stack

![Java](https://img.shields.io/badge/Java%2021-black?style=for-the-badge&logo=openjdk)
![MySQL](https://img.shields.io/badge/MySQL%208.0-white?style=for-the-badge&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-white?style=for-the-badge&logo=redis)  
![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203-white?style=for-the-badge&logo=springboot)
![Unirest](https://img.shields.io/badge/Unirest-navy?style=for-the-badge&logo=kong)

## Features

### Cross-Category Video Content 📼
The APIs search function supports multi-format queries, allowing users to find movies, TV shows, music videos, and specials in a single request. This enables seamless discovery across different content categories, including mainstream and anime.

Additionally, the API provides a trending content query, allowing users to retrieve popular video content filtered by format and category. This helps users stay updated on what’s currently trending across different media types.

### Manage and Track Personal Watchlist 📃
The API enables users to manage their personal watchlist efficiently. Users can track their watch status (Planned, Watching, Completed, Paused, etc.), monitor the number of watched episodes, and assign personal ratings to each entry. This feature helps users stay organized and keep track of their viewing progress across different types of content.

### "What To Watch" 🤔
For authenticated users with an established watchlist, this functionality assists in deciding what to watch next. Rather than using an informal **jar method** _(a casual term implying the random drawing of a movie title from a hat)_ the system leverages a random selection process. It filters the user’s watchlist based on their specific preferences (such as category, format, etc.) and then randomly selects a video content item that meets those criteria. This approach provides a spontaneous yet personalized recommendation experience.

## v.1 Roadmap

- [ ] Asian Drama Support
- [ ] User Activity History
- [ ] News Feed Related To Video Content
- [ ] Video Content Collections
- [ ] Achievements
- [ ] Favourites
- [ ] Extended User Reviews
- [ ] Extended User Page


## Run Locally

### Prerequisites

- Java: JDK 21 or later (verify with `java -version`)
- Maven: 3.6+
- MySQL: Either installed locally or running via Docker
- Redis: Either installed locally or running via Docker

### Environment Variables

The API uses several environment variables. Set them in your environment with the following keys:

- `MYSQL_USER` & `MYSQL_PASS` - Username and password for the MySQL database.
- `REDIS_PASS` - Password for the Redis database.
- `TMDB_KEY` - Read-access key from TheMovieDatabase [API](https://www.themoviedb.org/settings/api).
- `JWT_SECRET` - A secret key for generating [JWT](https://jwt.io/introduction).

### Build

Build project with Maven:

```
mvn clean install
```

And run the API:

```
mvn spring-boot:run
```

Once the application starts, the GraphQL API will be available at:
- GraphQL Endpoint: http://localhost:8080/graphql
- GraphiQL UI (Interactive Playground): http://localhost:8080/graphiql
