import org.apache.commons.codec.digest.Crypt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.stream.Stream;

public class Crack {
    private final User[] users;
    private final String dictionary;

    public Crack(String shadowFile, String dictionary) throws FileNotFoundException {
        this.dictionary = dictionary;
        this.users = Crack.parseShadow(shadowFile);
    }

    public void crack() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(dictionary);
        Scanner scanner = new Scanner(inputStream);

        while (scanner.hasNextLine()) {
            String word = scanner.nextLine();
            for (User user : users) {
                String passHash = user.getPassHash();
                if (passHash.contains("$")) {
                    String hash = Crypt.crypt(word, passHash);
                    if (hash.equals(passHash)) {
                        System.out.println("Found password " + word + " for user " + user.getUserName() + ".");
                    }
                }
            }
        }
    }


    public static int getLineCount(String path) {
        int lineCount = 0;
        try (Stream<String> stream = Files.lines(Path.of(path), StandardCharsets.UTF_8)) {
            lineCount = (int)stream.count();
        } catch(IOException ignored) {}
        return lineCount;
    }

    public static User[] parseShadow(String shadowFile) throws FileNotFoundException {
        int lineCount = getLineCount(shadowFile);
        User[] users = new User[lineCount];

        InputStream inputStream = new FileInputStream(shadowFile);
        Scanner scanner = new Scanner(inputStream);

        int index = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] part = line.split(":");
            String username = part[0];
            String passHash = part[1];
            User user = new User(username, passHash);
            users[index++] = user;
        }

        return users;
    }

    public static void main(String[] args) throws FileNotFoundException {
        Scanner sc = new Scanner(System.in);
        System.out.print("Type the path to your shadow file: ");
        String shadowPath = sc.nextLine();
        System.out.print("Type the path to your dictionary file: ");
        String dictPath = sc.nextLine();

        Crack c = new Crack(shadowPath, dictPath);
        c.crack();
    }
}
