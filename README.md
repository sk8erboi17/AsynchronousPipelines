
# Pipelines Overview

The **pipeline** is designed to organize and manage asynchronous data flows in network applications, providing flexibility and modularity in handling HTTP and Socket communications.

This methodology facilitates the efficient processing of data streams and asynchronous operations, ensuring applications can scale and adapt to various networking tasks effortlessly. By encapsulating the complexities of asynchronous communication into a structured pipeline, developers can concentrate on the application's core logic while utilizing a robust mechanism for data transmission and processing.

## Building Our First Pipeline

### Setting Up Our First Output/Input Socket

#### Sending an Output

These channels cater to distinct purposes, as suggested by their names: one for HTTP communication and the other for generic socket communication. Let's delve into the code for a better understanding:

**`AsyncChannelSocket.createChannel(...)`**: This method establishes and opens an `AsynchronousSocketChannel`. It connects the channel to a specific remote address, in this instance, `localhost` with designated ports.

``` java
private final static AsynchronousSocketChannel socketChannel = AsyncChannelSocket.createChannel(new InetSocketAddress("localhost", 8082));
``` 
#### Receiving an Input

The code snippet below illustrates initializing an `AsynchronousServerSocketChannel`, used to listen for incoming socket connections on a specified port for an Embedded Server.

``` java
private static AsynchronousServerSocketChannel server = AsyncInputSocket.createInput(new InetSocketAddress(8082));` 
``` 

**`AsyncInputSocket.createInput(...)`**: This method simplifies the creation and initialization of an `AsynchronousServerSocketChannel`, aiming to ease the setup process of the server socket channel.

```java
private static void setupIncomingClients() {
   Listener.getInstance().startConnectionListen(server, client -> {
       buildPipelinesIn(client);
       setupOutputForClients(client);
   });
}
``` 

-   **`setupIncomingClients`**: This function is tasked with initializing the server's capability to accept and manage incoming client connections.

-   **`Listener.getInstance()`**: Utilizes a singleton pattern for the `Listener` class, which is presumably responsible for network communication, specifically monitoring incoming connections on a server socket.

-   **`startConnectionListen(server, client -> {...})`**: Initiates the listening process on the specified `server`. The `server` object, while not explicitly defined, is assumed to represent the server's socket or a similar networking entity. The method employs a lambda function as its second argument, executed for each incoming client connection. This function receives a `client` parameter, denoting the connected client.

    -   **`client`**: Represents an individual client connection. This parameter is likely an instance of `AsynchronousSocketChannel` or a similar class enabling asynchronous network socket communication.

    -   **`buildPipelinesIn(client)`**: Constructs an input pipeline for each connected client, configured to asynchronously receive and process data from the client. The specific configuration and processing logic are encapsulated within the `buildPipelinesIn` method.

    -   **`setupOutputForClients(client)`**: Beyond configuring the input pipeline, it's also essential to establish an output pipeline or a comparable mechanism for sending data back to the client. The `setupOutputForClients` method handles this configuration.


We will explore the last two methods in subsequent sections.

## Constructing Input Pipelines with `PipelineBuilderIn`

Our application necessitates an input pipeline to manage incoming packets from external sources. This pipeline monitors all incoming packets and processes them accordingly. The subsequent section delineates constructing such an input pipeline using the `PipelineBuilderIn` class.

This method configures an input pipeline for asynchronous communication with external entities.

```java
public static void buildPipelinesIn(AsynchronousSocketChannel client) {
    PipelineIn pipelineIn = new PipelineInBuilder(client)
            .configureAggregateCallback(
                    new CallbackBuilder()
                            .onComplete(object -> System.out.println("Response:" + object))
                            .onException(Throwable::printStackTrace)
                            .build())
            .setBufferSize(4096 * 128)
            .build();
}
``` 

#### `PipelineInBuilder` Parameters:

-   **`.configureAggregateCallback(...)`**: This parameter is pivotal in defining how the pipeline processes incoming data, along with handling responses or exceptions. It employs a `CallbackBuilder` to set up callbacks for various events:
    -   **`.onComplete(...)`**: Specifies a callback to execute upon successfully receiving a response. The lambda expression `object -> System.out.println("Response :" + object )` showcases a straightforward case where the response is echoed to the console.
    -   **`.onException(...)`**: Establishes a callback for managing exceptions during pipeline operation. The lambda `Throwable::printStackTrace` prints the exception's stack trace, aiding debugging.
-   **`.setBufferSize(...)`**: Determines the input buffer's size.

## Constructing Output Pipelines

Before delving into `PipelineBuilderOut` for building output pipelines, it's essential to understand how requests are crafted, either via Socket or HTTP. The `GetUsersFromWebServer` and `SayHelloToEmbeddedServer` classes exemplify two approaches for request creation within an application.

### HTTP Request: `GetUsersFromWebServer`

```java
public class GetUsersFromWebServer implements Http {
    @Override
    public HttpRequest request() {
        return new HttpBuilder().GET().uri("http://136.243.124.131").build();
    }

    @Override
    public Callback response() {
        return new CallbackBuilder()
                .onComplete(response -> HttpFormatter.formatHttpResponse(String.valueOf(response)))
                .onException(exception -> {
                    throw new RuntimeException("An error occurred while receiving the response " + exception.getMessage(), exception);
                }).build();
    }

}
``` 
-   **`Http` Interface**: By adhering to this interface, `GetUsersFromWebServer` mandates defining a `request()` method, returning an `HttpRequest`.

-   **`request()`**: Employs `HttpBuilder`, a builder pattern for assembling an HTTP request. The builder is configured for a GET operation to the URI `http://localhost:8080/api/users`, aimed at retrieving user data from a local web server on port `8080`.

-   **`HttpBuilder`**: Aims to simplify HTTP request creation. The builder pattern enhances code readability and fluency.

-   **`response()`**: Uses `CallbackBuilder` to construct a callback, managing successful and failed responses.


### Socket Request: `SayHelloToEmbeddedServer`

``` java
public class SayHelloToEmbeddedServer implements Request {
    private final String message;

    public SayHelloToEmbeddedServer(String message) {
        this.message = message;
    }

    @Override
    public Object getMessage() {
        return message;
    }

    @Override
    public Callback getCallback() {
        return new CallbackBuilder()
                .onComplete(null)
                .onException(Throwable::printStackTrace).build();
    }
}
``` 
-   **Constructor**: Accepts a `String message` as input, representing the message intended for the embedded server.

-   **`CallbackBuilder`**: Utilizes a builder pattern for callback configuration, addressing completion and exception scenarios of the request. However, the `onComplete` callback is set to `null`, denoting no specific action upon completion. The `onException` callback is designed to print any exceptions' stack trace, facilitating debugging.

-   **`getMessage`**: Retrieves the message for transmission through the pipeline.

-   **Purpose**: `SayHelloToEmbeddedServer` is crafted to transmit a simple message (likely over a socket) to an embedded server, based on its context and naming convention.


### Constructing Output Pipelines with `PipelineBuilderOut`

#### HTTP Request Pipeline: `buildPipelinesHttpOut`

This method assembles an output pipeline tailored for HTTP communication, intended for dispatching an HTTP request to a web server.

```java
public static void buildPipelinesHttpOut(AsynchronousSocketChannel client) {
    PipelineOut pipelineOut = new PipelineOutBuilder().buildHTTP();
    Http request = new GetUsersFromWebServer();
    pipelineOut.registerRequest(request);
} 
``` 

-   **`PipelineOutBuilder` Configuration**:
    -   `.buildHTTP()`: Completes the pipeline setup, generating the `PipelineOut` instance.
-   **Registering and Dispatching an HTTP Request**: `pipelineOut.registerRequest(new GetUsersFromWebServer());` enlists an HTTP GET request to retrieve user data from a web server. The `GetUsersFromWebServer` class, as previously discussed, crafts this request.

### Socket Request Pipeline: `buildPipelinesSocketOut`

This method establishes an output pipeline for dispatching a message to an embedded server via socket communication.

```java
public static void buildPipelinesSocketOut(AsynchronousSocketChannel client) {
    PipelineOut pipelineOut = new PipelineOutBuilder(client).allocateDirect(true).setBufferSize(4096).buildSocket();
    pipelineOut.registerRequest(new SayHelloToEmbeddedServer("Message from Client: Hi Embedded Server!"));
}
``` 

1.  **`PipelineOut` Creation**: Utilizes `PipelineOutBuilder` for generating a `PipelineOut` instance. The configuration entails:
    -   **`allocateDirect`**: When set to `true`, leverages direct memory for the output buffer, potentially boosting performance.
    -   **`setBufferSize`**: Adjusts the output buffer size to 4096 bytes.
2.  **Request Registration**: Enlists a `SayHelloToEmbeddedServer` object within the pipeline, encapsulating the message "Message from Client: Hi Embedded Server!" for the server.

For a comprehensive example, refer to the src package.