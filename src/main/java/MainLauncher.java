import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.core.Runtime;

public class MainLauncher {
    public static void main(String[] args) throws Exception {
        Runtime rt = Runtime.instance();
        Profile profile = new ProfileImpl();
        AgentContainer mainContainer = rt.createMainContainer(profile);

        mainContainer.createNewAgent("UserAgent", "UserAgent", null).start();
        mainContainer.createNewAgent("ExecutionAgent", "ExecutionAgent", null).start();
        mainContainer.createNewAgent("BrokerAgent", "BrokerAgent", null).start();
        mainContainer.createNewAgent("ExternalResourceAgent", "ExternalResourceAgent", null).start();
    }
}
