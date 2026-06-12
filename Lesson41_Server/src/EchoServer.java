import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;

public class EchoServer {
    private final int port;


    private final Map<String, Function<String, String>> commands = new HashMap<>();

    private EchoServer(int port) {
        this.port = port;
        initCommands();
    }

    private void initCommands() {
        commands.put("date", msg -> LocalDate.now().toString());
        commands.put("time", msg -> LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        commands.put("reverse", msg -> {
            var text = msg.substring("reverse".length()).strip();
            return new StringBuilder(text).reverse().toString();
        });
        commands.put("upper", msg -> msg.substring("upper".length()).strip().toUpperCase());
    }

    public static EchoServer bindToPort(int port) {
        return new EchoServer(port);
    }

    public void run() {
        try (var server = new ServerSocket(port)) {
            try (var clientSocket = server.accept()) {
                handle(clientSocket);
            }
        } catch (IOException e) {
            var formatMsg = "Вероятнее всего порт %s занят.%n";
            System.out.printf(formatMsg, port);
            e.printStackTrace();
        }
    }

    private void handle(Socket socket) throws IOException {
        var input = socket.getInputStream();
        var isr = new InputStreamReader(input, "UTF-8");
        var output = socket.getOutputStream();
        var writer = new PrintWriter(output, false);

        try (var sc = new Scanner(isr); writer) {
            while (true) {
                var message = sc.nextLine().strip();
                System.out.printf("Got: %s%n", message);


                if (message.toLowerCase().equals("bye")) {
                    System.out.println("Bye bye");
                    return;
                }


                var keyword = message.split(" ")[0].toLowerCase();

                var handler = commands.getOrDefault(keyword, msg -> msg);
                var response = handler.apply(message);

                writer.write(response);
                writer.write(System.lineSeparator());
                writer.flush();
            }
        } catch (NoSuchElementException ex) {
            System.out.println("Client dropped connection");
        }
    }
}

