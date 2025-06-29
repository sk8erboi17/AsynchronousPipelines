package example.client;


import example.client.request.SayHelloToEmbeddedServer;
import example.client.response.ResponseManager;
import it.sk8erboi17.network.pipeline.in.PipelineInBuilder;
import it.sk8erboi17.network.pipeline.out.PipelineOut;
import it.sk8erboi17.network.pipeline.out.PipelineOutBuilder;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class PipeslineIO {

    ///EMBEDDED SERVER REQUEST HERE
    public static void buildPipelinesSocketOut(AsynchronousSocketChannel client) {
        PipelineOut pipelineOut = new PipelineOutBuilder(client).buildSocket();
       pipelineOut.handleRequest(new SayHelloToEmbeddedServer("Message from Client: Hi Embedded Server!\n"));

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            pipelineOut.handleRequest(new SayHelloToEmbeddedServer("Personal Data:[2025-06-25T18:08:36.058542984Z] E' stato creato un nuovo utente UserInfoModel{userInfoId=800b65ad-ee45-44e7-b773-6f94e6aa460e, name='MICHELE', surname='TEST', genderType=MALE, birthdate=Tue Jan 01 01:00:00 CET 1980, birthCity='F839', taxCode='TSTMHL80A01F839Y', workPreferences=PRESENCE, seniorityType=JUNIOR, active=true, createdAt=null, updatedAt=null}\n"));
        }, 1, 1, TimeUnit.MILLISECONDS);

    }


    public static void buildPipelinesIn(AsynchronousSocketChannel client) {
        new PipelineInBuilder(client)
                .setBufferSize(4096)
                .configureAggregateCallback(Collections.singletonList(ResponseManager.responseToServer))
                .build();
    }


}
