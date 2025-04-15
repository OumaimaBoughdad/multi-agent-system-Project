import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import utils.HttpHelper;

public class ExternalResourceAgent extends Agent {
    protected void setup() {
        System.out.println(getLocalName() + " started.");

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String query = msg.getContent();
                    System.out.println(getLocalName() + ": Searching for " + query);

                    String wikipediaResult = HttpHelper.searchExternalSource("wikipedia", query);
                    String duckResult = HttpHelper.searchExternalSource("duckduckgo", query);

                    System.out.println("--- Results for: " + query + " ---");
                    System.out.println("Wikipedia: " + wikipediaResult);
                    System.out.println("DuckDuckGo: " + duckResult);
                } else {
                    block();
                }
            }
        });
    }
}
