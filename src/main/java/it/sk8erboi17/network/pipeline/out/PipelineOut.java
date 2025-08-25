package it.sk8erboi17.network.pipeline.out;

import it.sk8erboi17.exception.ProtocolIncompleteException;
import it.sk8erboi17.listeners.response.Callback;
import it.sk8erboi17.network.pipeline.out.content.Request;
import it.sk8erboi17.network.transformers.encoder.DataEncoder;
import it.sk8erboi17.network.transformers.encoder.op.FrameEncoder;
import it.sk8erboi17.utils.FailWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * The PipelineOut class is responsible for managing the output pipeline for
 * a single client connection. It wires together the necessary components and
 * delegates data serialization and transmission.
 */
public class PipelineOut {
    private static final Logger log = LoggerFactory.getLogger(PipelineOut.class);
    private AsynchronousSocketChannel client;
    private FrameEncoder frameEncoder;

    /**
     * Constructs a PipelineOut for a specific client, wiring up all necessary components.
     * @param client The non-null channel to write data to.
     */
    public PipelineOut(AsynchronousSocketChannel client) {
        if (client == null) {
            throw new IllegalArgumentException("Client channel cannot be null.");
        }
        this.client = client;

        DataEncoder dataEncoder = new DataEncoder(this.client);
        this.frameEncoder = new FrameEncoder(dataEncoder);
    }

    public void handleRequest(Request request) {
        if (frameEncoder == null) {
            log.warn("Attempting to handle request on a non-existent encoder.");
            return;
        }

        final Object message = request.getMessage();
        final Callback callback = request.getCallback();

        if (message == null) {
            frameEncoder.sendHeartbeat(callback);
            return;
        }

        if (message instanceof String s) {
            frameEncoder.sendString(s, callback);
            return;
        }

        if (message instanceof Integer i) {
            frameEncoder.sendInt(i, callback);
            return;
        }

        if (message instanceof Float v) {
            frameEncoder.sendFloat(v, callback);
            return;
        }

        if (message instanceof Double v) {
            frameEncoder.sendDouble(v, callback);
            return;
        }

        if (message instanceof Character c) {
            frameEncoder.sendChar(c, callback);
            return;
        }

        if (message instanceof byte[] bytes) {
            frameEncoder.sendByteArray(bytes, callback);
            return;
        }

        log.error("Unsupported message type: {}", message.getClass().getName());
        FailWriter.writeFile("Error with type: ",  new ProtocolIncompleteException("Unsupported message type: " + message.getClass().getName()));
    }

    public AsynchronousSocketChannel getClient() {
        return client;
    }

    public void setClient(AsynchronousSocketChannel newClient) {
        if (this.client != null && this.client.isOpen() && this.client != newClient) {
            try {
                this.client.close(); // old connection
            } catch (IOException e) {
                log.error("Error with close {}", e.getMessage(), e);
                FailWriter.writeFile("Error with close ", e);
            }
        }
        this.client = newClient;
        if (this.client != null && this.client.isOpen()) {
            DataEncoder dataEncoder = new DataEncoder(this.client);
            this.frameEncoder = new FrameEncoder(dataEncoder);
        } else {
            this.frameEncoder = null;
        }
    }
}
