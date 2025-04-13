package com.example;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Main {
    public static void main(String[] args) {
        // Start the JADE runtime
        Runtime runtime = Runtime.instance();

        // Create a default profile for the main container
        Profile profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, "localhost");
        profile.setParameter(Profile.MAIN_PORT, "1099");
        profile.setParameter(Profile.GUI, "true");

        // Create the main container
        AgentContainer mainContainer = runtime.createMainContainer(profile);

        try {
            // Launch BrokerAgent
            AgentController brokerAgent = mainContainer.createNewAgent(
                    "brokerAgent",
                    "BrokerAgent",
                    null
            );
            brokerAgent.start();

            // Launch ExecutionAgent
            AgentController executionAgent = mainContainer.createNewAgent(
                    "executionAgent",
                    "ExecutionAgent",
                    null
            );
            executionAgent.start();

            // Launch InternalResourceAgent
            AgentController internalResourceAgent = mainContainer.createNewAgent(
                    "internalResourceAgent",
                    "InternalResourceAgent",
                    null
            );
            internalResourceAgent.start();

            // Launch ExternalResourceAgent
            AgentController externalResourceAgent = mainContainer.createNewAgent(
                    "externalResourceAgent",
                    "ExternalResourceAgent",
                    null
            );
            externalResourceAgent.start();

            // Launch UserAgent
            AgentController userAgent = mainContainer.createNewAgent(
                    "userAgent",
                    "UserAgent",
                    null
            );
            userAgent.start();

        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }
}