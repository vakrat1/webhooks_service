package com.tufin.webhook.client.ws;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

public class GraphQLSubscriptionWSTestClient {

    static CountDownLatch latch;

    public static void main(String[] args) {
        try {
            latch = new CountDownLatch(1);

            // open websocket
            final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("ws://localhost:9000/subscriptions"));

            // add listener
            clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
                public void handleMessage(String message) {
                    System.out.println(message);
                }
            });

            // send message to websocket
//            String query = "{\"query\":\"subscription StockCodeSubscription { \\n stockQuotes { dateTime\\n stockCode\\n stockPrice\\n stockPriceChange\\n }}\",\"variables\":{}}";
            String query = "{\"query\":\"subscription StockCodeSubscription { \\n ticketUpdates { ticketId\\n dateTime\\n eventType\\n payload\\n }}\",\"variables\":{}}";
            clientEndPoint.sendMessage(query);
//            clientEndPoint.sendMessage("{'event':'addChannel','channel':'ok_btccny_ticker'}");

            // wait 5 seconds for messages from websocket
//            Thread.sleep(500000);
            latch.await();
        } catch (InterruptedException ex) {
            System.err.println("InterruptedException exception: " + ex.getMessage());
        } catch (URISyntaxException ex) {
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        }
    }
}
