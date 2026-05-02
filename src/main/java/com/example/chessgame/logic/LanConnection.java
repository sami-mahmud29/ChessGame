package com.example.chessgame.logic;

import com.example.chessgame.model.Move;
import com.example.chessgame.model.Position;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class LanConnection {

    private final boolean host;
    private final String remoteHost;
    private final int port;

    private ServerSocket serverSocket;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public LanConnection(boolean host, String remoteHost, int port) {
        this.host = host;
        this.remoteHost = remoteHost;
        this.port = port;
    }

    public void connect() throws IOException {
        if (host) {
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
        } else {
            socket = connectWithRetry(remoteHost, port, 60, 1000);
        }

        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    private Socket connectWithRetry(String hostAddress, int targetPort, int attempts, long delayMs) throws IOException {
        IOException lastError = null;

        for (int i = 0; i < attempts; i++) {
            try {
                Socket retrySocket = new Socket();
                retrySocket.connect(new InetSocketAddress(hostAddress, targetPort), 1500);
                return retrySocket;
            } catch (IOException ex) {
                lastError = ex;
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Connection retry interrupted", interruptedException);
                }
            }
        }

        if (lastError != null) {
            throw lastError;
        }
        throw new IOException("Unable to connect to host");
    }

    public synchronized void sendMove(Move move) throws IOException {
        writer.write("MOVE " + move.from.row + " " + move.from.col + " " + move.to.row + " " + move.to.col);
        writer.newLine();
        writer.flush();
    }

    public void listen(Consumer<Move> onMove, Runnable onDisconnect) {
        Thread listener = new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    Move move = parseMove(line);
                    if (move != null) {
                        onMove.accept(move);
                    }
                }
            } catch (IOException ignored) {
                // Connection is closed or interrupted.
            } finally {
                onDisconnect.run();
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    private Move parseMove(String line) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length != 5 || !"MOVE".equals(parts[0])) {
            return null;
        }
        try {
            int fromRow = Integer.parseInt(parts[1]);
            int fromCol = Integer.parseInt(parts[2]);
            int toRow = Integer.parseInt(parts[3]);
            int toCol = Integer.parseInt(parts[4]);
            return new Move(new Position(fromRow, fromCol), new Position(toRow, toCol));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public void close() {
        try {
            if (reader != null) reader.close();
        } catch (IOException ignored) {
        }
        try {
            if (writer != null) writer.close();
        } catch (IOException ignored) {
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {
        }
    }
}
