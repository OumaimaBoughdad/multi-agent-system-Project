

import jade.core.Agent;

public class ControllerAgent extends Agent {
    protected void setup() {
        System.out.println(getLocalName() + " started.");

        String[] agents = {
                "UserAgent",
                "ExecutionAgent",
                "BrokerAgent",
                "OntologyServerAgent",
                "RessourceExterneAgent",
                "RessourceInterneAgent"
        };

        for (String name : agents) {
            try {
                getContainerController().createNewAgent(name, "agents." + name, null).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
