import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class ExecutionAgent extends Agent {
    protected void setup() {
        System.out.println(getLocalName() + " started.");

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    ACLMessage fwd = new ACLMessage(ACLMessage.REQUEST);
                    fwd.setContent(msg.getContent());
                    fwd.addReceiver(getAID("OntologyServerAgent"));
                    send(fwd);
                    System.out.println(getLocalName() + ": Forwarded to OntologyServerAgent.");
                } else {
                    block();
                }
            }
        });
    }
}
