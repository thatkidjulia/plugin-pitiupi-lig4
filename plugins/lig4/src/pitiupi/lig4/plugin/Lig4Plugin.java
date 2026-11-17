package pitiupi.lig4.plugin;

import pitiupi.api.plugin.PluginWorkspace;
import pitiupi.application.messaging.MessageDispatcher;
import pitiupi.application.messaging.SendTarget;
import pitiupi.application.messaging.TransportMode;
import pitiupi.application.peeractions.PeerAction;
import pitiupi.application.peeractions.PeerActionTarget;
import pitiupi.application.peeractions.PeerTarget;
import pitiupi.control.ConnectionManager;
import pitiupi.lig4.net.Lig4Message;
import pitiupi.lig4.ui.Lig4Panel;
import pitiupi.models.Messages.Message;
import pitiupi.models.Plugin;
import pitiupi.presentation.shell.LocalNetworkInfo;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

public final class Lig4Plugin extends Plugin {
    private ConnectionManager connectionManager;
    private PluginWorkspace workspace;
    private Lig4Panel panel;
    private InetAddress opponentAddress;
    private String opponentName;
    private String matchId;
    private int moveNumber;

    @Override public void createPlugin(ConnectionManager manager, PluginWorkspace pluginWorkspace) {
        connectionManager = manager;
        workspace = pluginWorkspace;
        panel = new Lig4Panel();
        panel.setMoveHandler(this::playLocalMove);
        panel.setRematchHandler(this::requestRematch);
        bindShellWorkspaceContent(panel);
        workspace.peerActions().register(PeerAction.of(
                "lig4.challenge", "Desafiar para Lig4", PeerActionTarget.SINGLE_PEER, this::challengePeer));
    }

    private void challengePeer(PeerTarget target) {
        if (!(target instanceof PeerTarget.Single single)) return;
        if (opponentAddress != null) {
            JOptionPane.showMessageDialog(panel, "Você já está em uma partida. Termine ou aguarde a conexão atual.");
            return;
        }
        opponentAddress = single.peer().getInetAddress().orElse(null);
        if (opponentAddress == null) {
            JOptionPane.showMessageDialog(panel, "Não consegui encontrar o endereço dessa pessoa. Verifiquem se ambos estão online.");
            return;
        }
        opponentName = single.peer().getDisplayName();
        matchId = UUID.randomUUID().toString();
        moveNumber = 0;
        workspace.openOrFocusTab(getIdentifier(), getDisplayName());
        panel.endMatch("Convite enviado para " + opponentName + ". Aguardando resposta...");
        send(Lig4Message.INVITE, -1);
    }

    private void playLocalMove(int column) {
        if (opponentAddress == null || panel.game().isFinished()) return;
        int player = panel.localPlayer();
        int row = panel.play(column);
        if (row < 0) return;
        moveNumber++;
        send(Lig4Message.MOVE, column);
    }

    private void requestRematch() {
        if (opponentAddress == null) return;
        send(Lig4Message.REMATCH, -1);
        panel.endMatch("Pedido de revanche enviado para " + opponentName + ".");
    }

    private void send(String type, int column) {
        Lig4Message message = new Lig4Message(type, matchId, connectionManager.getUserName(),
                localIp(), column, moveNumber);
        boolean sent = MessageDispatcher.send(connectionManager, message,
                SendTarget.singlePeer(opponentAddress), TransportMode.TCP) > 0;
        if (!sent && type.equals(Lig4Message.INVITE)) {
            JOptionPane.showMessageDialog(panel, "Não foi possível enviar o convite. Confira a conexão e tente novamente.");
            clearMatch();
        }
    }

    private String localIp() {
        String ip = LocalNetworkInfo.bestLocalIpv4();
        return ip == null ? "" : ip;
    }

    @Override public List<Class<? extends Message>> getMessages() { return List.of(Lig4Message.class); }

    @Override public void receiveMessage(Message message) {
        if (!(message instanceof Lig4Message msg)) return;
        SwingUtilities.invokeLater(() -> handle(msg));
    }

    private void handle(Lig4Message msg) {
        if (Lig4Message.INVITE.equals(msg.getType())) {
            receiveInvite(msg);
            return;
        }
        if (matchId == null || !matchId.equals(msg.getMatchId())) return;
        switch (msg.getType()) {
            case Lig4Message.ACCEPT -> panel.beginMatch(panel.localPlayer() == 0 ? 1 : panel.localPlayer(), opponentName);
            case Lig4Message.REJECT -> { JOptionPane.showMessageDialog(panel, opponentName + " recusou o convite."); clearMatch(); }
            case Lig4Message.MOVE -> {
                if (msg.getMoveNumber() != moveNumber + 1 || !panel.applyRemoteMove(msg.getColumn(), 3 - panel.localPlayer())) return;
                moveNumber++;
            }
            case Lig4Message.REMATCH -> {
                int answer = JOptionPane.showConfirmDialog(panel, opponentName + " pediu uma revanche. Aceitar?", "Revanche", JOptionPane.YES_NO_OPTION);
                if (answer == JOptionPane.YES_OPTION) {
                    moveNumber = 0;
                    panel.beginMatch(panel.localPlayer(), opponentName);
                    send(Lig4Message.ACCEPT, -1);
                } else send(Lig4Message.REJECT, -1);
            }
        }
    }

    private void receiveInvite(Lig4Message invite) {
        if (opponentAddress != null) {
            JOptionPane.showMessageDialog(panel, "Você já está em uma partida. Termine antes de aceitar outro convite.");
            return;
        }
        InetAddress address;
        try { address = InetAddress.getByName(invite.getSenderIp()); }
        catch (Exception ex) { return; }
        String name = invite.getSenderName() == null ? "Outro jogador" : invite.getSenderName();
        int answer = JOptionPane.showConfirmDialog(panel, name + " desafiou você para uma partida de Lig4. Aceitar?", "Convite para Lig4", JOptionPane.YES_NO_OPTION);
        opponentAddress = address;
        opponentName = name;
        matchId = invite.getMatchId();
        moveNumber = 0;
        workspace.openOrFocusTab(getIdentifier(), getDisplayName());
        if (answer == JOptionPane.YES_OPTION) {
            panel.beginMatch(2, opponentName);
            send(Lig4Message.ACCEPT, -1);
        } else {
            send(Lig4Message.REJECT, -1);
            clearMatch();
        }
    }

    private void clearMatch() {
        opponentAddress = null;
        opponentName = null;
        matchId = null;
        moveNumber = 0;
        panel.endMatch("Desafie alguém na lista de usuários para jogar.");
    }

    @Override public String getName() { return "Lig4"; }
    @Override public String getDisplayName() { return "Lig4"; }
    @Override public String getAuthor() { return "Pitiupi"; }
    @Override public String getVersion() { return "0.2"; }
    @Override public String getIdentifier() { return "pitiupi.lig4"; }
}
