
# AsynchronousServerClientLib - Overview
The **AsynchronousServerClientLib** is a powerful library for managing asynchronous communication between clients and servers. With this library, developers can easily create applications that are able to handle multiple client connections and provide fast, asynchronous communication between clients and servers.

 **Getting Start with Server**


`Listener` is a class that serves as a starting point for setting up server-client communication in the network. It is responsible for managing the creation and management of the server socket channel and the incoming client socket channels.

    // Get the instance of the Listener Class
     Listener class Listener listener = Listener.getInstance();

The `startListen` method in the `Listener` class is responsible for starting the listening process on a specific socket channel.

The method takes two parameters: a `serverSocketChannel` and a `callback` function.
The `serverSocketChannel` is an asynchronous socket channel that is used as a server-side socket to listen for incoming connections.
The `callback` function is executed whenever a new client connects to the server. This function is called for each connected client and is passed the `socketChannel` of the newly connected client.
In the `startListen` method, the server socket channel is set up to continuously listen for incoming connections. When a new client connects, the server socket channel returns the client's socket channel and the `callback` function is executed.

    listener.startConnectionListen(serverSocketChannel, socketChanne...

The `ReadingDataListener` class is an abstract class that provides a framework for reading data from an asynchronous server socket channel. It has a single public method `readData()` which initiates a read operation on the specified asynchronous server socket channel. The method takes two parameters:

 -  `AsynchronousSocketChannel asyncServerSocket`: This is the asynchronous socket channel from which data is to be read.
    
 -  `ResponseCallback<T> callback`: This is the response callback that is executed once data is read from the socket channel. It takes as a parameter the data that was read and is of the generic type `T`.
   
   
```sh
// Create an instance of the ReaderStringInputEvent class 
// which is an implementation of the ReadingDataListener class
 ReadingDataListener dataListener = new ReaderStringInputEvent();
```
 List of ReadingDataListener:
 
 - ReaderStringInputEvent
 - ReadCharInputEvent
 - ReadDoubleInputEvent
 - ReaderIntInputEvent
 - ReadFloatInputEvent

The `WritingDataListener` class is used to handle writing data to a server using an asynchronous socket channel. The class contains methods to handle writing of data, error handling, and checking if the server socket channel is open. The class has an abstract method `writeDataToServer` that should be implemented by concrete implementations of this class to initiate a write operation on the specified asynchronous socket channel. The `handleWrite` method writes the data to the server if the server socket channel is open and not currently writing data.
 ```sh
// Create an instance of the WriteStringOutputEvent class 
    WriteStringOutputEvent writeStringOutputEvent = new WriteStringOutputEvent();
 ```
 
 List of WritingDataListener:
 - WriteCharOutputEvent
 - WriteDoubleOutputEvent
 - WriteFloatOutputEvent
 - WriteIntOutputEvent
 -  WriteStringOutputEvent
 
   The `InputEventHandler` interface provides a `handle` method that is used to process incoming data from a connected client, listen for incoming data, and send a response back to the connected client. The `handle` method takes three parameters: an instance of `AsynchronousSocketChannel` representing the server socket for incoming connection requests, an instance of `ReadingDataListener` to listen for incoming data from the connected clients, and an instance of `ResponseCallback` to send the response back to the connected clients. This interface is implemented by the developers to handle incoming events in the server.
  ```sh
 // Create an instance of the InputListener class and handle input data 
    new InputListener<String>().handle(socketChannel, dataListener, System.out::println);
 ```

The `OutputEventHandler` is an interface that defines a contract for handling output events in a server. It provides a way for writing data to a specified asynchronous socket channel and handling any errors that may occur during the writing process. The `handle` method of this interface takes three parameters:

1.  `asyncServerSocket`: The asynchronous socket channel on which the data is going to be written.
2.  `type`: The type of data that is going to be written.
   
3.  `dataListener`: The writing data listener that is used to manage the writing process.
    

In other words, this interface provides a way to write data to the specified asynchronous socket channel and handle any errors that might occur during the writing process. By implementing this interface, you can create classes that are responsible for writing data to an asynchronous socket channel and handling any errors that might occur during the writing process.
 ```sh
// Create an instance of the OutputListener which is an implementation of OutputEventHandler class and handle output 
new OutputListener<String>().handle(socketChannel, "hello", writeStringOutputEvent);
 ```
**Code example complete**
 ```sh
// Get the instance of the Listener class
Listener listener = Listener.getInstance();

// Start the connection listen method with server socket channel and a lambda expression as a parameter
   AsynchronousServerSocketChannel serverSocketChannel;
        try {
            serverSocketChannel = AsyncServerSocket.createServer(new InetSocketAddress(8080));
            //Start Listen Clients
    Listener.getInstance().startConnectionListen(serverSocketChannel, socketChannel -> {
    System.out.println("Client Connected"); // print message indicating a client has connected

    // Create an instance of the ReaderStringInputEvent class
    ReaderStringInputEvent dataListener = new ReaderStringInputEvent();

    // Create an instance of the WriteStringOutputEvent class
    WriteStringOutputEvent writeStringOutputEvent = new WriteStringOutputEvent();

    // Create an instance of the InputListener class and handle input data
    new InputListener<String>().handle(socketChannel, dataListener, System.out::println);

    // Create an instance of the OutputListener class and handle output data
    new OutputListener<String>().handle(socketChannel, "hello", writeStringOutputEvent);
});
```

**Getting Start with Client**

`AsyncSocket` is a class that provides a static method for creating a client connection using an AsynchronousSocketChannel. The main purpose of this class is to simplify the process of creating a client connection for a client application that wants to communicate with a server.

The `createClient` method is the main method of this class. It takes an `InetSocketAddress` as an input, which represents the address and port of the server that the client wants to connect to. The method opens an AsynchronousSocketChannel and connects to the specified server. The method returns the connected AsynchronousSocketChannel.

The method also throws IOException, ExecutionException, and InterruptedException if an I/O error occurs or if the current thread was interrupted while waiting for the connection to complete.

This method provides a convenient way for clients to connect to a server, without having to worry about the underlying details of opening and connecting a socket channel.
```sh
AsynchronousServerSocketChannel serverSocketChannel;  
try {  
  serverSocketChannel = AsyncServerSocket.createServer(new InetSocketAddress(8080));  
} catch (IOException e) {  
  e.printStackTrace();  
}
```

The `SocketThreadIO` class serves as a base for reading and writing data to a server using an `AsynchronousSocketChannel` and a `ByteBuffer`. This class provides the basic structure for implementing a reading or writing thread that utilizes asynchronous socket channels for communication.

An asynchronous socket channel is a type of network socket that allows for asynchronous communication, meaning that the socket can handle multiple incoming or outgoing connections at the same time without blocking the main thread. This is useful for implementing high-performance servers that can handle multiple connections simultaneously.

The `SocketThreadIO` class contains a single abstract method, `startThread()`, which must be implemented by subclasses. This method is called to start the reading or writing thread, and it is up to the subclass to determine the logic that will be executed in the thread.

```sh
// Creating a client asynchronous socket channel that connects to the server at the specified host and port  
    AsynchronousSocketChannel socketChannel = AsyncSocket.createClient(new  InetSocketAddress("localhost",8080));
    
 // Creating a SocketReadThread object that reads data from the socket channel 
    SocketThreadIO socketReadThread= new SocketReadThread(socketChannel,1024); 
    
 // Creating a SocketWriteThread object that writes data to the socket channel  
    SocketThreadIO socketWriteThread = new SocketWriteThread(socketChannel,1024); 
    
 // Starting the write thread to begin writing data to the socket channel 
    socketWriteThread.startThread(); 
    
 // Starting the read thread to begin reading data from the socket channel 
    socketReadThread.startThread();
```

 TODO:
 - Read and Write Custom Object
