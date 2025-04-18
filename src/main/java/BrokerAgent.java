import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class BrokerAgent extends Agent {
    protected void setup() {
        System.out.println(getLocalName() + " started.");

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String destination = msg.getUserDefinedParameter("target");

                    ACLMessage redirect = new ACLMessage(ACLMessage.REQUEST);
                    redirect.setContent(msg.getContent());
                    redirect.addReceiver(getAID(destination));
                    send(redirect);

                    System.out.println(getLocalName() + ": Routed to " + destination);
                } else {
                    block();
                }
            }
        });
    }
}
