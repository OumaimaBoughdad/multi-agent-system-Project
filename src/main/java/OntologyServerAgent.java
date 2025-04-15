import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class OntologyServerAgent extends Agent {
    protected void setup() {
        System.out.println(getLocalName() + " started.");

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String query = msg.getContent().toLowerCase();
                    String destination;

                    // Very basic routing logic
                    if (query.contains("database") || query.contains("internal")) {
                        destination = "RessourceInterneAgent";
                    } else {
                        destination = "RessourceExterneAgent";
                    }

                    ACLMessage out = new ACLMessage(ACLMessage.REQUEST);
                    out.setContent(query);
                    out.addReceiver(getAID("BrokerAgent"));
                    out.setUserDefinedParameter("target", destination);
                    send(out);

                    System.out.println(getLocalName() + ": Mapped query to " + destination);
                } else {
                    block();
                }
            }
        });
    }
}
