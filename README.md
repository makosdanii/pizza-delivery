# Pizza delivery
A **full-stack web application** for operating a pizza delivery network. 
## Framework
It is implemented by using the **Java Spring** framework which provides an API through HTTP connection for authenticated clients. 
The authentication is done with **JWT** since the server is stateless. Based on the authorization, the client can do **CRUD operations** with the ORM based entities.
## Shortest path navigation
Starting the server also stores a weighted graph in the memory, which is read at specific server-client interactions for calculating the shortest path. Entities resembling delivery vehicles will get mapped to different vertices, which shows their change of location.
One's location is changed if able to deliver and it's distance by shortest path is the smallest to the customer, amongst all the other available vehicles. The shortest of the existing paths are determined by **Dijkstra's algorithm**.
### Concurrent websockets
Websocket connection is also made when a delivery process has begun to keep the client of customer up-to-date about the vehicle's location. With the dimention of time, concurrent programming is introduced to make the simulation more realistic and suitable for interacting with multiple client and handling their orders parallelly if multiple vehicle are used as well.

