import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class InternalResourceAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println("InternalResourceAgent " + getLocalName() + " is ready.");

        // Register the internal-resource service in the DF
        registerService();

        // Add behavior to handle requests
        addBehaviour(new RequestHandler());
    }

    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("internal-resource");
        sd.setName("InternalResourceAgent");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class RequestHandler extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg != null) {
                System.out.println("InternalResourceAgent received request: " + msg.getContent());

                // Simulate processing the request
                String responseContent = "Processed internally: " + msg.getContent();

                // Send response
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(responseContent);
                send(reply);

                System.out.println("InternalResourceAgent sent response.");
            } else {
                block();
            }
        }
    }
}