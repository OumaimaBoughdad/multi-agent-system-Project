

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.BasicOntology;

import java.util.HashMap;
import java.util.Map;

/**
 * ExecutionAgent for NetSA architecture
 * Processes tasks from Broker and coordinates with resource agents
 */
public class ExecutionAgent extends Agent {
    // References to resource agents
    private AID internalResourceAgent = null;
    private AID externalResourceAgent = null;
    private AID brokerAgent = null;
    private AID ontologyServer = null;

    // Communication language and ontology
    private Codec codec = new SLCodec();
    private Ontology ontology = BasicOntology.getInstance();

    @Override
    protected void setup() {
        System.out.println("Execution Agent " + getLocalName() + " started.");

        // Register language and ontology
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontology);

        // Register the execution service in the DF
        registerService();

        // Find necessary agents
        addBehaviour(new FindAgentsBehaviour());

        // Add behavior to handle requests from Broker
        addBehaviour(new RequestProcessor());

        System.out.println("Execution Agent initialized and waiting for messages.");
    }

    @Override
    protected void takeDown() {
        // Deregister from the DF
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        System.out.println("Execution Agent " + getLocalName() + " terminating.");
    }

    /**
     * Register the execution service in the DF
     */
    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("execution");
        sd.setName("NetSA-execution");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    /**
     * Behavior to find required agents (broker, resource agents, etc.)
     */
    private class FindAgentsBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            // Find Broker Agent
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("broker");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result.length > 0) {
                    brokerAgent = result[0].getName();
                    System.out.println("Found Broker Agent: " + brokerAgent.getName());

                    // Register with the broker
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.addReceiver(brokerAgent);
                    msg.setContent("execution-agent");
                    msg.setConversationId("execution-registration");
                    send(msg);
                } else {
                    System.out.println("No Broker Agent found. Retrying in 10 seconds...");
                    addBehaviour(new WakerBehaviour(myAgent, 10000) {
                        @Override
                        protected void onWake() {
                            addBehaviour(new FindAgentsBehaviour());
                        }
                    });
                    return;
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
                return;
            }

            // Find Ontology Server
            template = new DFAgentDescription();
            sd = new ServiceDescription();
            sd.setType("ontology-server");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result.length > 0) {
                    ontologyServer = result[0].getName();
                    System.out.println("Found Ontology Server: " + ontologyServer.getName());
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

            // Find Internal Resource Agent
            template = new DFAgentDescription();
            sd = new ServiceDescription();
            sd.setType("internal-resource");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result.length > 0) {
                    internalResourceAgent = result[0].getName();
                    System.out.println("Found Internal Resource Agent: " + internalResourceAgent.getName());
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

            // Find External Resource Agent
            template = new DFAgentDescription();
            sd = new ServiceDescription();
            sd.setType("external-resource");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                if (result.length > 0) {
                    externalResourceAgent = result[0].getName();
                    System.out.println("Found External Resource Agent: " + externalResourceAgent.getName());
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

            // Schedule periodic search for missing agents
            if (internalResourceAgent == null || externalResourceAgent == null || ontologyServer == null) {
                addBehaviour(new WakerBehaviour(myAgent, 10000) {
                    @Override
                    protected void onWake() {
                        addBehaviour(new FindMissingAgentsBehaviour());
                    }
                });
            }
        }
    }

    /**
     * Behavior to find any agents that weren't available initially
     */
    private class FindMissingAgentsBehaviour extends OneShotBehaviour {
        @Override
        public void action() {
            boolean stillMissing = false;

            // Find Internal Resource Agent if not found yet
            if (internalResourceAgent == null) {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("internal-resource");
                template.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    if (result.length > 0) {
                        internalResourceAgent = result[0].getName();
                        System.out.println("Found Internal Resource Agent: " + internalResourceAgent.getName());
                    } else {
                        stillMissing = true;
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                    stillMissing = true;
                }
            }

            // Find External Resource Agent if not found yet
            if (externalResourceAgent == null) {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("external-resource");
                template.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    if (result.length > 0) {
                        externalResourceAgent = result[0].getName();
                        System.out.println("Found External Resource Agent: " + externalResourceAgent.getName());
                    } else {
                        stillMissing = true;
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                    stillMissing = true;
                }
            }

            // Find Ontology Server if not found yet
            if (ontologyServer == null) {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("ontology-server");
                template.addServices(sd);

                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    if (result.length > 0) {
                        ontologyServer = result[0].getName();
                        System.out.println("Found Ontology Server: " + ontologyServer.getName());
                    } else {
                        stillMissing = true;
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                    stillMissing = true;
                }
            }

            // Schedule another search if still missing agents
            if (stillMissing) {
                addBehaviour(new WakerBehaviour(myAgent, 10000) {
                    @Override
                    protected void onWake() {
                        addBehaviour(new FindMissingAgentsBehaviour());
                    }
                });
            }
        }
    }

    /**
     * Behavior to process execution requests from Broker
     */
    private class RequestProcessor extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
                    MessageTemplate.MatchConversationId("execution-request")
            );

            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                String request = msg.getContent();
                String originalSender = msg.getUserDefinedParameter("original-sender");

                System.out.println("Received execution request: " + request);

                // Determine which resource agent should handle this
                AID targetAgent = determineTargetAgent(request);

                if (targetAgent != null) {
                    // Forward request to appropriate resource agent
                    ACLMessage forward = new ACLMessage(ACLMessage.REQUEST);
                    forward.addReceiver(targetAgent);
                    forward.setContent(request);
                    forward.setConversationId("resource-request");
                    forward.setReplyWith("res" + System.currentTimeMillis()); // Unique ID

                    // Keep track of the conversation
                    forward.addUserDefinedParameter("original-sender", originalSender);
                    forward.addUserDefinedParameter("broker-id", msg.getSender().getName());
                    forward.addUserDefinedParameter("request-id", msg.getReplyWith());

                    send(forward);
                    System.out.println("Forwarded request to " +
                            (targetAgent.equals(internalResourceAgent) ? "Internal" : "External") +
                            " Resource Agent");

                    // Register a behavior to handle the response
                    addBehaviour(new ResourceResponseHandler(forward.getReplyWith(), msg));
                } else {
                    // No suitable resource agent
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("No suitable resource agent available");
                    reply.setConversationId("execution-response");
                    send(reply);
                }
            } else {
                block();
            }
        }

        // Determine which resource agent should handle the request
        private AID determineTargetAgent(String request) {
            // Simple logic - in real system would consult ontology server
            if (request.toLowerCase().contains("database") && internalResourceAgent != null) {
                return internalResourceAgent;
            } else if (request.toLowerCase().contains("web") && externalResourceAgent != null) {
                return externalResourceAgent;
            } else if (internalResourceAgent != null) {
                return internalResourceAgent; // Default to internal
            } else if (externalResourceAgent != null) {
                return externalResourceAgent; // Fallback to external
            }
            return null;
        }
    }

    /**
     * Behavior to handle responses from resource agents
     */
    private class ResourceResponseHandler extends Behaviour {
        private String conversationId;
        private ACLMessage originalRequest;
        private boolean done = false;

        public ResourceResponseHandler(String conversationId, ACLMessage originalRequest) {
            this.conversationId = conversationId;
            this.originalRequest = originalRequest;
        }

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.or(
                            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                            MessageTemplate.MatchPerformative(ACLMessage.FAILURE)
                    ),
                    MessageTemplate.MatchInReplyTo(conversationId)
            );

            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                // Forward the result back to the broker
                ACLMessage reply = originalRequest.createReply();
                reply.setPerformative(msg.getPerformative());
                reply.setContent(msg.getContent());
                reply.setConversationId("execution-response");

                // Pass along the original sender info
                reply.addUserDefinedParameter("original-sender",
                        originalRequest.getUserDefinedParameter("original-sender"));

                send(reply);
                System.out.println("Forwarded resource result to Broker");

                done = true;
            } else {
                block();
            }
        }

        @Override
        public boolean done() {
            return done;
        }
    }
}