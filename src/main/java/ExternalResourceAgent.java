import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class ExternalResourceAgent extends Agent {
    @Override
    protected void setup() {
        System.out.println("ExternalResourceAgent " + getLocalName() + " is ready.");

        // Register the external-resource service in the DF
        registerService();

        // Add behavior to handle requests
        addBehaviour(new RequestHandler());
    }

    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("external-resource");
        sd.setName("ExternalResourceAgent");
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
                System.out.println("ExternalResourceAgent received request: " + msg.getContent());

                // Simulate processing the request
                String responseContent = "Processed externally: " + msg.getContent();

                // Send response
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(responseContent);
                send(reply);

                System.out.println("ExternalResourceAgent sent response.");
            } else {
                block();
            }
        }
    }
}
