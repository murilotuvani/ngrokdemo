package br.com.autogeral.ngrokdemo;

import com.ngrok.Ngrok;
import com.ngrok.definitions.AgentIngressList;
import com.ngrok.definitions.CredentialList;
import com.ngrok.definitions.Page;
import com.ngrok.definitions.TunnelList;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Example {

    private static String apiKey = null;

    public static void main(final String[] args) {
        File file = new File("ngrok.pro");
        if (!file.exists()) {
            System.out.println("File " + file.getAbsolutePath() + " is not present");
        } else {
            Properties prop = new Properties();
            try ( FileInputStream ios = new FileInputStream(file)) {
                prop.load(ios);
                apiKey = prop.getProperty("API_KEY");
//      final var ngrok = Ngrok.createDefault();
                final var ngrok = Ngrok.createDefault(apiKey);

//        // Create an IP Policy that allows traffic from some subnets
//        ngrok.ipPolicies().create().call().thenCompose(policy
//                -> CompletableFuture.allOf(
//                        Stream.of("24.0.0.0/8", "12.0.0.0/8")
//                                .map(cidr
//                                        -> ngrok.ipPolicyRules()
//                                        .create(cidr, policy.getId(), "allow")
//                                        .call()
//                                        .toCompletableFuture()
//                                )
//                                .toArray(CompletableFuture[]::new)
//                )
//        ).toCompletableFuture().join();
                System.out.println("Tunnels:");
                ngrok.tunnels().list().call()
                        .thenCompose(tunnelPage -> printTunnelsRecursively(ngrok, tunnelPage))
                        .toCompletableFuture().join();

                ngrok.credentials().list().call()
                        .thenCompose(credentialPage -> printCredentialsRecursively(ngrok, credentialPage))
                        .toCompletableFuture().join();

                ngrok.agentIngresses().list().call()
                        .thenCompose(ingressesPage -> printIngressesRecursively(ngrok, ingressesPage))
                        .toCompletableFuture().join();
            } catch (IOException ex) {
                Logger.getLogger(Example.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private static CompletionStage<Void> printTunnelsRecursively(final Ngrok ngrok, final Page<TunnelList> currentPage) {
        currentPage.getPage().getTunnels().forEach(System.out::println);
        return currentPage.next().thenCompose(maybeTunnelPage -> maybeTunnelPage
                .map(tunnelPage -> printTunnelsRecursively(ngrok, tunnelPage))
                .orElseGet(() -> CompletableFuture.<Void>completedFuture(null))
        );
    }

    private static CompletionStage<Void> printCredentialsRecursively(final Ngrok ngrok, final Page<CredentialList> currentPage) {
        currentPage.getPage().getCredentials().forEach(System.out::println);
        return currentPage.next().thenCompose(maybeCredentialPage -> maybeCredentialPage
                .map(credentialPage -> printCredentialsRecursively(ngrok, credentialPage))
                .orElseGet(() -> CompletableFuture.<Void>completedFuture(null))
        );
    }

    private static CompletionStage<Void> printIngressesRecursively(final Ngrok ngrok, final Page<AgentIngressList> currentPage) {
        currentPage.getPage().getIngresses().forEach(System.out::println);
        return currentPage.next().thenCompose(maybeCredentialPage -> maybeCredentialPage
                .map(credentialPage -> printIngressesRecursively(ngrok, credentialPage))
                .orElseGet(() -> CompletableFuture.<Void>completedFuture(null))
        );
    }
}
