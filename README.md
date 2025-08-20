# Tic Tac Toe API

## Description
The service provides a REST API implementation to play the well known game Tic Tac Toe, where:

1. There are 2 players
2. Every player is represented by a unique symbol, usually it's X and O
3. The board consists of a 3x3 matrix. A player wins if they can align 3 of their markers in a vertical, horizontal or diagonal line 
4. If no more moves are possible, the game should finish

## Implementation
- The game is modeled with Game entity that contains all the needed information to play and to fetch 
the game at any time (by gameId). It contains the board, last player marker (e.g. useful for the caller to derive nextPlayer), 
winner and game state. Just for convenience, it contains also the player's data.
- I choose to save the board as a json to map it to an arraylist of arraylist, because the board size is pretty small. This approach also made operation on the board very easy to implement.
Another approach would have been to save the board in a separate table in a more relational way with fields like
(board_id, row_index, col_index, value). This approach would lead to a more complex code to load the whole board in memory.

The API accept a configurable board size to play with different matrix size. Current size is set to 3.

### Technologies 
The app uses Spring Data Jpa for the persistence and Flyway for the PostgresSQL database control version. 
It provides a docker-compose file to create the database locally. The controller and service tests uses a Docker PostgresSQL test container.
Documentation is provided via springdoc openapi and shown in the Swagger UI.

### How to play
1. Create a game using start endpoint:
```
curl --location 'http://localhost:8082/tic-tac-toe/api/v1/start' \
  --header 'Content-Type: application/json' \
  --data '[
  {
  "name":"Daniela",
  "marker":"X"
  },
  {
  "name":"Player 2",
  "marker":"O"
  }
  ]
  '
  ```
2. Play using play endpoint (e.g. use gameId received from previous call):
```
curl --location 'http://localhost:8082/tic-tac-toe/api/v1/play' \
--header 'Content-Type: application/json' \
--data '{
   "gameId":"7e383752-8bc2-47de-be36-9360e320a7f1",
   "rowIndex":2,
   "colIndex":0,
   "player":{
      "name":"Daniela",
      "marker":"X"
   }
}
'
```
  
When receiving a play request the service validates it and check if it results in a win or a draw. 
Check winner algorithm looks in the row, column and diagonals where the marker is placed.

3. Get the game by game id to know the board configuration, game state, winner and last player marker:
```
curl --location 'http://localhost:8082/tic-tac-toe/api/v1/game/7e383752-8bc2-47de-be36-9360e320a7f1' \
--data ''
```

## Possible improvements
1. Extract player's data in a separate table.
2. Add some more test cases (e.g. more on input validation, configuration, etc.).
3. Provide logs.
4. Complete Swagger documentation and cleanup unneeded endpoints.
5. Containerize the app with docker to possibly deploy it.
    
## How to run it

- Download and install Docker
- Create DB 
```
   docker run --name postgres-db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=tic_tac_toe_db \
  -p 5432:5432 \
  -d postgres:15
  ```
- Start Spring Boot application (e.g. `./gradlew clean build`, `./gradlew bootRun`)
- Check if app is run at http://localhost:8082/actuator/health
- Try to create and play a game via Swagger `http://localhost:8082/swagger-ui/index.html` or through provided postman collection