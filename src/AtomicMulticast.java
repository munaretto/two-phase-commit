import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Random;

public class AtomicMulticast {

	private static ArrayList<String> votes = new ArrayList();
	public static void main(String[] args) throws IOException {

		if (args.length != 4) {
			System.out.println("Uso: AtomicMulticast <iniciador> <grupo> <porta> <nickname>");
			return;
		}

		boolean iniciador = Boolean.parseBoolean(args[0]);
		String grupoAddress = args[1];
		int porta = Integer.parseInt(args[2]);
		String nick = args[3];

		MulticastSocket socket = new MulticastSocket(porta);
		InetAddress grupo = InetAddress.getByName(grupoAddress);

		socket.joinGroup(grupo);

		if (iniciador) {
			byte[] message = (nick + " " + "VOTE_REQUEST").getBytes();
			DatagramPacket pacoteInicial = new DatagramPacket(message, message.length, grupo, porta);
			socket.send(pacoteInicial);
			System.out.println("Iniciador diz: VOTE_REQUEST");
		}

		while (true) {
			try {
				
				// Verifica as respostas de todos
				if (iniciador) {
					try {
						int count = 0;
						
						while(count < 5) {
							byte[] received 				= new byte[1024];
							DatagramPacket pacote 	= new DatagramPacket(received,received.length);
							socket.receive(pacote);
							String recebido 				= new String(pacote.getData(),0,pacote.getLength());
							String message 					= recebido.split("\\s")[1];

							if (!message.equals("VOTE_REQUEST")) {
								votes.add(message);
							}
							Thread.sleep(1000);
							count++;
						}
						
						String response = votes.stream().allMatch(a -> a.equals("VOTE_COMMIT"))
							? "GLOBAL_COMMIT"
							: "GLOBAL_ABORT";

						byte[] responseBytes = (nick + " " + response).getBytes();
						DatagramPacket pacoteResponse = new DatagramPacket(responseBytes, responseBytes.length, grupo, porta);
						socket.send(pacoteResponse);
						System.out.println("Iniciador diz: "+response);
						break;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				else {
					byte[] entrada 				= new byte[1024];
					DatagramPacket pacote = new DatagramPacket(entrada,entrada.length);
					socket.setSoTimeout(100);
					socket.receive(pacote);
					String recebido 			= new String(pacote.getData(),0,pacote.getLength());
	
					String sender 	= recebido.split("\\s")[0];
					String message 	= recebido.split("\\s")[1];
	
					if (message.equals("VOTE_REQUEST")) {
						Random random = new Random();
						String vote 	= random.nextBoolean()
							? "VOTE_COMMIT"
							:	"VOTE_ABORT";
						byte[] voteBytes = (nick + " " + vote).getBytes();
						DatagramPacket votePacote = new DatagramPacket(voteBytes, voteBytes.length, grupo, porta);
						socket.send(votePacote);
						System.out.println(sender+" diz: "+message);
					}

					if (message.equals("GLOBAL_COMMIT") || message.equals("GLOBAL_ABORT")) {
						System.out.println("Resposta final: "+message);
						break;
					}
				}
			} 
			catch (IOException e){}
		}

		socket.leaveGroup(grupo);
		socket.close();
	}

	public static void setTimeout(Runnable runnable, int delay){
    new Thread(() -> {
        try {
            Thread.sleep(delay);
            runnable.run();
        }
        catch (Exception e){
            System.err.println(e);
        }
    }).start();
}
}