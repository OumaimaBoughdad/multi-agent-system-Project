import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import utils.HttpHelper;

public class RessourceExterneAgent extends Agent {
    protected void setup() {
        System.out.println(getLocalName() + " started.");

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String query = msg.getContent();
                    String result = HttpHelper.searchExternalSource("wikipedia", query)
                            + "\n\nDuckDuckGo:\n" + HttpHelper.searchExternalSource("duckduckgo", query);

                    System.out.println(getLocalName() + ": External data fetched:\n" + result);
                } else {
                    block();
                }
            }
        });
    }
}
