import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class BrokerAgent extends Agent {
    // Map to store services and providers
    private Map<String, List<AID>> serviceDirectory = new HashMap<>();

    @Override
    protected void setup() {
        System.out.println("✅ Broker agent " + getLocalName() + " is ready.");

        // Register the broker service in the yellow pages (DF)
        registerService();

        // Add the behavior to handle service registrations
        addBehaviour(new ServiceRegistrationServer());

        // Add the behavior to handle service requests
        addBehaviour(new ServiceRequestServer());

        // ✅ ADD THIS: Behavior to handle direct user queries
        addBehaviour(new HandleUserQuery());
    }

    @Override
    protected void takeDown() {
        // Deregister from the DF
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        System.out.println("Broker agent " + getLocalName() + " terminating.");
    }

    /**
     * Register the broker service in the DF (Directory Facilitator)
     */
    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("broker");
        sd.setName("JADE-broker");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Behavior to handle service registrations from provider agents
     */
    private class ServiceRegistrationServer extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                // Process the service registration message
                String serviceType = msg.getContent();
                AID provider = msg.getSender();

                // Add the service provider to the directory
                if (!serviceDirectory.containsKey(serviceType)) {
                    serviceDirectory.put(serviceType, new ArrayList<>());
                }

                List<AID> providers = serviceDirectory.get(serviceType);
                if (!providers.contains(provider)) {
                    providers.add(provider);
                    System.out.println("Service " + serviceType + " registered by " + provider.getName());
                }

                // Confirm registration
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.CONFIRM);
                reply.setContent("Service " + serviceType + " registered successfully");
                send(reply);
            } else {
                block();
            }
        }
    }

    /**
     * Behavior to handle service requests from consumer agents
     */
    private class ServiceRequestServer extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
            ACLMessage msg = myAgent.receive(mt);

            if (msg != null) {
                // Process the service request
                String serviceType = msg.getContent();
                AID consumer = msg.getSender();

                ACLMessage reply = msg.createReply();

                // Check if the requested service is available
                if (serviceDirectory.containsKey(serviceType) && !serviceDirectory.get(serviceType).isEmpty()) {
                    // Find best provider (simple implementation: choose the first one)
                    AID provider = serviceDirectory.get(serviceType).get(0);

                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(provider.getName());
                    System.out.println("Service request for " + serviceType + " from " + consumer.getName() +
                            " matched with provider " + provider.getName());
                } else {
                    // Service not available
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("Service " + serviceType + " not available");
                    System.out.println("Service " + serviceType + " requested by " + consumer.getName() +
                            " is not available");
                }

                send(reply);
            } else {
                block();
            }
        }
    }

    /**
     * ✅ New Behavior to handle direct user queries
     */
    private class HandleUserQuery extends CyclicBehaviour {
        @Override
        public void action() {
            // Listen for REQUEST messages from UserAgent
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchSender(new AID("userAgent", AID.ISLOCALNAME))
            );

            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String query = msg.getContent();
                System.out.println("✅ Received query from UserAgent: " + query);

                // Simulate processing the query
                String responseContent = "Response to query: " + query;

                // Send a response back to the UserAgent
                ACLMessage reply = msg.createReply();
                reply.setPerformative(ACLMessage.INFORM);
                reply.setContent(responseContent);
                send(reply);

                System.out.println("✅ Sent response to UserAgent: " + responseContent);
            } else {
                block();
            }
        }
    }
}