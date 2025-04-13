import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class UserAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("✅ User Agent " + getLocalName() + " started.");

        // Add the behavior to send a query
        addBehaviour(new SendQueryBehaviour());

        // Add the behavior to receive responses
        addBehaviour(new ReceiveResponseBehaviour());
    }

    // ---------------------
    // Sending Query Behavior
    // ---------------------
    private class SendQueryBehaviour extends Behaviour {
        private boolean done = false;

        @Override
        public void action() {
            String query = "Find articles about AI";

            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
            msg.addReceiver(new AID("brokerAgent", AID.ISLOCALNAME));
            msg.setContent(query);
            send(msg);

            System.out.println("📤 Sent query to broker: " + query);
            done = true;
        }

        @Override
        public boolean done() {
            return done;
        }
    }

    // ---------------------
    // Receiving Response Behavior
    // ---------------------
    private class ReceiveResponseBehaviour extends Behaviour {

        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                System.out.println("📥 Received response: " + msg.getContent());
            } else {
                block();  // Wait for the message
            }
        }

        @Override
        public boolean done() {
            return false;  // Keep running
        }
    }
}