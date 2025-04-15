import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Scanner;

public class UserAgent extends Agent {
    protected void setup() {
        System.out.println(getLocalName() + " started.");

        addBehaviour(new OneShotBehaviour() {
            public void action() {
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter your query: ");
                String query = scanner.nextLine();
                scanner.close();

                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.setContent(query);
                msg.addReceiver(getAID("ExecutionAgent"));
                send(msg);

                System.out.println(getLocalName() + ": Sent query to ExecutionAgent.");
            }
        });
    }
}
