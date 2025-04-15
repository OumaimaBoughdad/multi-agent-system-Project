import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;

public class AggregatorAgent extends Agent {
    private int expectedResults;
    private List<String> results = new ArrayList<>();

    protected void setup() {
        Object[] args = getArguments();
        expectedResults = (args != null && args.length > 0) ? (int) args[0] : 0;

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    results.add(msg.getContent());
                    System.out.println("Received: " + msg.getContent());

                    if (results.size() == expectedResults) {
                        System.out.println("\n--- Aggregated Results ---");
                        results.forEach(System.out::println);
                        doDelete();
                    }
                } else {
                    block();
                }
            }
        });
    }
}
