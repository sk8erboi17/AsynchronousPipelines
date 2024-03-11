
# Pipelines Overview!


The **pipeline** serves to organize and manage asynchronous data flows in network applications, offering flexibility and modularity in the management of HTTP and Socket communications.

This approach allows for the efficient processing of data streams and asynchronous operations, ensuring that applications can scale and adapt to various networking tasks with ease. By encapsulating the complexities of asynchronous communication into a structured pipeline, developers can focus on the core logic of their applications while leveraging a robust mechanism for data transmission and processing.

## Let's build our first Pipeline
### Setup our first output/input socket
#### Send an Output
These channels are intended for different purposes, as indicated by their names: one for HTTP communication and the other for generic socket communication. Let's break down the code for a clearer understanding:

**`AsyncOutputSocket.createOutput(...)`**: This method creates and opens an `AsynchronousSocketChannel`. It connects the channel to a specified remote address, in this case, `localhost` with specific ports. The exact implementation details of `createOutput` would depend on the library or framework being used.

 ```java  
private final static AsynchronousSocketChannel socketChannel = AsyncOutputSocket.createOutput(new InetSocketAddress("localhost", 8082));  
 ```
**`httpChannel`**: This variable holds an `AsynchronousSocketChannel` intended for HTTP communication. This channel is connected to a server running on `localhost` (the same machine as the client) and listening on port `8080`. This is a common setup for local development and testing of web applications, where the application server listens for HTTP requests on port `8080`.
 ```java  
 private final static AsynchronousSocketChannel httpChannel = AsyncOutputSocket.createOutput(new InetSocketAddress("localhost", 8080));  
 ```
**`socketChannel`**: Similar to `httpChannel`, this static variable holds an `AsynchronousSocketChannel`, but it's intended for generic socket communication rather than HTTP. This channel connects to a service listening on port `8082` on `localhost`. The specific use of port `8082` that this channel is used for a different type of service or application running on the same machine, possibly for internal communication or a specific functionality that does not involve HTTP.
#### Receive an Input
This setup is particularly useful for applications that require handling asynchronous communication with multiple clients, such as chat servers, multiplayer game servers, or any server that needs to efficiently manage client requests and responses.

The provided code snippet demonstrates how to initialize an `AsynchronousServerSocketChannel` in Java, which is used for listening for incoming socket connections on a specified port an Embeeded Server.
 ```java  
 private static AsynchronousServerSocketChannel server = AsyncInputSocket.createInput(new InetSocketAddress(8082));  
 ```
**`AsyncInputSocket.createInput(...)`**: This method abstracts the creation and initialization of an `AsynchronousServerSocketChannel` and the  purpose is to simplify the setup process of the server socket channel.
 ```java  
private static void setupIncomeClients() {  
 Listener.getInstance().startConnectionListen(server, client -> {
     PipeslineIO.buildPipelinesIn(client);
     setupOutputForClients(client);
    });
}  
 ```
-   **`setupIncomeClients`**: This method is responsible for initializing the server's ability to accept and handle incoming client connections.

-   **`Listener.getInstance()`**: This call use  a singleton for a `Listener` class. The `Listener` class is likely responsible for network communication, specifically listening for incoming connections on a server socket.

-   **`startConnectionListen(server, client -> {...})`**: This method starts the listening process on the specified `server` object. The server object is not explicitly defined but is assumed to be an instance that represents the server's socket or a similar networking entity. The method takes a lambda function as its second argument, which is executed for each incoming client connection. The function receives a `client` parameter, representing the connected client.

    -   **`client`**:  refers to an individual client connection. This object is likely an instance of `AsynchronousSocketChannel` or a similar class that facilitates asynchronous communication over network sockets.

    -   **`PipeslineIO.buildPipelinesIn(client)`**: For each connected client, this line constructs an input pipeline using the client connection. The input pipeline is configured to asynchronously receive and process data sent by the client. The exact configuration and processing logic would be defined within the `buildPipelinesIn` method
    - **`setupOutputForClients(client)`**: After setting up the input pipeline, this line is also a need to configure an output pipeline or similar mechanism for sending data back to the client. The method `setupOutputForClients` is responsible for this configuration.

We will see the last two methods in another topic.

## Building Input Pipelines with `PipelineBuilderIn`

To handle incoming packets from external sources, our application requires an input pipeline. This pipeline listens for all incoming packets and processes them accordingly. The following section explains how to construct such an input pipeline using the `PipelineBuilderIn` class.

This method sets up an input pipeline for asynchronous communication with external entities. It's designed to be used with an `AsynchronousSocketChannel`, which facilitates non-blocking I/O operations over socket channels.

```java  
public static void buildPipelinesIn(AsynchronousSocketChannel client) {    
    PipelineIn pipelineIn = new PipelineInBuilder()    
            .configureAggregateCallback(  
             new CallbackBuilder()
                .onComplete(object -> System.out.println("Response:" + object ))    
                .onException(Throwable::printStackTrace)    
                .build())    
            .client(client)    
            .build();    
closePipeline(pipelineIn); 
}  
```  
#### Parameters of `PipelineInBuilder`:

-   **`.configureAggregateCallback(...)`**: This parameter is crucial for defining how the pipeline handles incoming data and any associated responses or exceptions. It uses a `CallbackBuilder` to configure callbacks for different events:
    -   **`.onComplete(...)`**: Specifies a callback function to be executed when a response is successfully received. The lambda expression `object -> System.out.println("Response :" + object )` demonstrates a simple use case where the received response is printed to the console.
    -   **`.onException(...)`**: Defines a callback for handling exceptions that may occur during the pipeline operation. The provided lambda expression `Throwable::printStackTrace` prints the stack trace of the exception, aiding in debugging.
-   **`.client(client)`**: Associates the pipeline with a specific `AsynchronousSocketChannel`. This channel is used for the actual I/O operations, enabling the pipeline to listen for and process incoming data. The `client` parameter passed to this method represents the asynchronous channel that will be used for communication.

#### Closing the Pipeline:

After setting up and utilizing the pipeline, it is important to properly close it to ensure that resources are released and to prevent potential memory leaks. The method `closePipeline(pipelineIn);` is called to handle this cleanup process efficiently.

## Building Output Pipelines
Before to see how to build a PipelineBuilderOut, we need to know how request are made in via Socket or via HTTP  
The `GetUsersFromWebServer` and `SayHelloToEmbeededServer` classes demonstrate two approaches to creating requests in an application.

### HTTP Request: `GetUsersFromWebServer`
```java  
public class GetUsersFromWebServer implements Http {  
 @Override 
 public HttpRequest request() { 
     return new HttpBuilder().GET().uri("http://localhost:8080/api/users").build();
 }
}  
```  

-   **`Http` Interface**: By implementing this interface, `GetUsersFromWebServer` requires a method `request()` to be defined, which returns an `HttpRequest`.

-   **`request()`**: This method utilizes `HttpBuilder`, a builder pattern to construct an HTTP request. The builder is configured to perform a GET operation to the URI `http://localhost:8080/api/users`. This URI is for the request is intended to fetch user data from a web server running locally on port `8080`.

-   **`HttpBuilder`**: This class is designed to simplify the creation of HTTP requests. The builder pattern used here allows for fluent and readable code.

### Socket Request: `SayHelloToEmbeededServer`
```java  
  
public class SayHelloToEmbeededServer extends Request {  
 public SayHelloToEmbeededServer(String message) {
     super(new CallbackBuilder()
             .onComplete(null)
             .onException(Throwable::printStackTrace).build(), message); 
 }
}  
```  

-   **Constructor**: The constructor takes a `String message` as an argument, which is the payload or message to be sent to the embedded server.

-   **`CallbackBuilder`**: Similar to `HttpBuilder`, this uses a builder pattern but for configuring callbacks. It's used here to handle the completion and exception scenarios of the request. However, the `onComplete` callback is set to `null`, indicating no specific action on completion. The `onException` callback is configured to print the stack trace of any thrown exceptions, aiding in debugging.

-   **Purpose**: Given the context and the naming, `SayHelloToEmbeededServer` seems designed to send a simple message (likely over a socket) to an embedded server. The embedded server is expected to be listening on a different port or service, distinct from the HTTP server mentioned earlier.

### Building Output Pipelines with `PipelineBuilderOut`
#### HTTP Request Pipeline: `buildPipelinesHttpOut`

This method constructs an output pipeline configured for HTTP communication. It is designed to send an HTTP request to a web server.
```java  
public static void buildPipelinesHttpOut(AsynchronousSocketChannel client) {
    PipelineOut pipelineOut = new PipelineOutBuilder().client(client).allocateDirect(true).setHttpEnabled(true).initBuffer(4096).build();
    pipelineOut.registerRequest(new GetUsersFromWebServer().request());
    closePipeline(pipelineOut);
}
```
-   **`PipelineOutBuilder` Configuration**:

    -   `.client(client)`: Associates the pipeline with an `AsynchronousSocketChannel` for communication.
    -   `.allocateDirect(true)`: Specifies that the buffer used by the pipeline should be a direct buffer, which can potentially offer performance benefits by reducing the overhead of copying data between Java and native memory.
    -   `.setHttpEnabled(true)`: Enables HTTP functionality for the pipeline, indicating that it will be used for sending HTTP requests.
    -   `.initBuffer(4096)`: Initializes a buffer with a capacity of 4096 bytes for the pipeline's I/O operations.
    -   `.build()`: Finalizes the pipeline configuration and creates the `PipelineOut` instance.
-   **Registering and Sending an HTTP Request**: The method `pipelineOut.registerRequest(new GetUsersFromWebServer().request());` registers an HTTP GET request to fetch users from a web server. The request is created by the `GetUsersFromWebServer` class, as explained previously.

-   **Closing the Pipeline**: Ensures that resources are properly released and the pipeline is closed after the pipeline is out.

### Socket Request Pipeline: `buildPipelinesSocketOut`

This method sets up an output pipeline for sending a message to an embedded server using socket communication.
```java  
public static void buildPipelinesSocketOut(AsynchronousSocketChannel client) {
    PipelineOut pipelineOut = new PipelineOutBuilder().client(client).allocateDirect(true).initBuffer(4096).setHttpEnabled(false).build();
    pipelineOut.registerRequest(new SayHelloToEmbeededServer("Message from Client: Hi Embedded Server!"));
    closePipeline(pipelineOut);
}
```
-   **`PipelineOutBuilder` Configuration**:
    -   Similar to the HTTP pipeline configuration, but with `.setHttpEnabled(false)` to indicate that this pipeline is not intended for HTTP communication but rather for socket communication.
-   **Registering and Sending a Socket Request**: Registers a custom request, `new SayHelloToEmbeededServer("Message from Client: Hi Embedded Server!")`, to send a message to an embedded server. This demonstrates how to use the pipeline for non-HTTP, socket-based communication.

-   **Closing the Pipeline**: As with the HTTP pipeline, it's important to close the pipeline if it is out.

A complete example is visible in src package.
