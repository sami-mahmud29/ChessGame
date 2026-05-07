package com.example.chessgame.controller;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

public class MenuController {

    @FXML
    private void handleVsAI(ActionEvent event) throws Exception {
        Integer seconds = askTimeSelection(true);
        if (seconds == null) {
            return;
        }
        openGame(event, true, seconds);
    }

    @FXML
    private void handleTwoPlayer(ActionEvent event) throws Exception {
        Integer seconds = askTimeSelection(false);
        if (seconds == null) {
            return;
        }
        openGame(event, false, seconds);
    }

    @FXML
    private void handleLanMultiplayer(ActionEvent event) throws Exception {
        ChoiceDialog<String> modeDialog = new ChoiceDialog<>("Host", "Host", "Join");
        modeDialog.setTitle("LAN Multiplayer");
        modeDialog.setHeaderText("Choose LAN mode");
        modeDialog.setContentText("Mode:");

        Optional<String> modeResult = modeDialog.showAndWait();
        if (modeResult.isEmpty()) {
            return;
        }

        TextInputDialog portDialog = new TextInputDialog("5000");
        portDialog.setTitle("LAN Port");
        portDialog.setHeaderText("Enter port");
        portDialog.setContentText("Port:");

        Optional<String> portResult = portDialog.showAndWait();
        if (portResult.isEmpty()) {
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portResult.get().trim());
        } catch (NumberFormatException ex) {
            showError("Invalid port number.");
            return;
        }

        if (port < 1024 || port > 65535) {
            showError("Port must be between 1024 and 65535.");
            return;
        }

        boolean isHost = modeResult.get().equals("Host");
        String hostAddress = "127.0.0.1";

        if (isHost) {
            String selectedIp = chooseHostIpAddress();
            if (selectedIp == null) {
                return;
            }
            Alert hostInfo = new Alert(Alert.AlertType.INFORMATION);
            hostInfo.setTitle("Host Details");
            hostInfo.setHeaderText("Share this with the joining player");
            hostInfo.setContentText("Host IP: " + selectedIp + "\nPort: " + port +
                    "\n\nThen press Start in the game screen.");
            hostInfo.showAndWait();
        }

        if (!isHost) {
            TextInputDialog hostDialog = new TextInputDialog("127.0.0.1");
            hostDialog.setTitle("Host Address");
            hostDialog.setHeaderText("Enter host IP");
            hostDialog.setContentText("IP:");

            Optional<String> hostResult = hostDialog.showAndWait();
            if (hostResult.isEmpty() || hostResult.get().trim().isEmpty()) {
                return;
            }
            hostAddress = hostResult.get().trim();
        }

        openLanGame(event, isHost, hostAddress, port);
    }

    private Integer askTimeSelection(boolean vsAI) {
        if (vsAI) {
            ChoiceDialog<String> dialog = new ChoiceDialog<>("5 minutes", "5 minutes", "10 minutes");
            dialog.setTitle("Choose Time Control");
            dialog.setHeaderText("Play vs AI: choose clock length");
            dialog.setContentText("Time:");

            Optional<String> result = dialog.showAndWait();
            return result.map(choice -> choice.equals("5 minutes") ? 300 : 600).orElse(null);
        } else {
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Current (10 minutes)", "Current (10 minutes)", "5 minutes");
            dialog.setTitle("Choose Time Control");
            dialog.setHeaderText("2 Player: choose clock length");
            dialog.setContentText("Time:");

            Optional<String> result = dialog.showAndWait();
            return result.map(choice -> choice.equals("5 minutes") ? 300 : 600).orElse(null);
        }
    }

    private void openGame(ActionEvent event, boolean vsAI, int timeSeconds) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/chessgame/hello-view.fxml")
        );

        Scene scene = new Scene(loader.load());

// 🔥 THIS LINE IS CRITICAL
        HelloController controller = loader.getController();
        controller.setVsAI(vsAI);
        controller.setGameDurationSeconds(timeSeconds);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    private void openLanGame(ActionEvent event, boolean isHost, String hostAddress, int port) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/chessgame/hello-view.fxml")
        );

        Scene scene = new Scene(loader.load());

        HelloController controller = loader.getController();
        controller.setVsAI(false);
        controller.enableLanMode(isHost, hostAddress, port);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("LAN setup failed");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            return "Unable to detect (use ipconfig)";
        }
    }

    private String chooseHostIpAddress() {
        List<String> addresses = getAllLocalIpv4Addresses();
        if (addresses.isEmpty()) {
            return getLocalIpAddress();
        }

        ChoiceDialog<String> ipDialog = new ChoiceDialog<>(addresses.get(0), addresses);
        ipDialog.setTitle("Choose Host IP");
        ipDialog.setHeaderText("Select the IP on your LAN/Wi-Fi adapter");
        ipDialog.setContentText("Host IP:");
        Optional<String> selected = ipDialog.showAndWait();
        return selected.orElse(null);
    }

    private List<String> getAllLocalIpv4Addresses() {
        List<String> ips = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        ips.add(address.getHostAddress());
                    }
                }
            }
        } catch (SocketException ignored) {
            // Fallback to getLocalIpAddress.
        }
        return ips;
    }
}