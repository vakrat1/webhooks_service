package com.tufin.webhook.nats;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

public class NatsPublisher {

    private Connection nc = null;

    public NatsPublisher() throws IOException, InterruptedException {
        Options o = new Options.Builder()
                .server("nats://localhost:4222")
                .maxReconnects(-1).build();
        nc = Nats.connect(o);
    }

    public void sendMessages()  {
        try {
            int counts = 0;
            while (counts < 10) {
                nc.publish("yanivnats", "hello world".getBytes(StandardCharsets.UTF_8));
                nc.flush(Duration.ZERO);
                System.out.println("Msg send to NATS");

                counts++;
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } finally {
            try {
                if (nc != null) {
                    nc.close();
                    nc = null;
                }
            }catch (InterruptedException e){
                e.printStackTrace();
                nc = null;
            }
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new NatsPublisher().sendMessages();
    }
}
