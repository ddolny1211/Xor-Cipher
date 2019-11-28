import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Xor {
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            switch (args[0]) {
                case "-p":
                    createPlainFromOrig();
                    System.out.println("I'm creating plain.txt...");
                    break;
                case "-e":
                    encrypt();
                    System.out.println("I'm encoding text. Nobody will crack it now ha ha !");
                    break;
                case "-k":
                    cryptanalysis();
                    decrypt();
                    System.out.println("Crap... nevermind...");
                    break;
            }
        } else {
            System.out.println("Choose one of -p -e -k options!");
        }
    }

    private static void createPlainFromOrig() throws Exception {
        FileWriter fileWriter = new FileWriter("plain.txt");
        String origText = readFile("orig.txt");
        if (!origText.equals("")) {
            char[] origTextArray = origText.toCharArray();
            String line;
            int letterIterator = 0;
            for (char character : origTextArray) {
                line = String.valueOf(character);
                if (line.matches("[a-z ]")) {
                    fileWriter.write(character);
                    letterIterator++;
                    if (letterIterator == 64) {
                        fileWriter.write("\n");
                        letterIterator = 0;
                    }
                }
            }
        }
        fileWriter.close();
    }

    private static void encrypt() throws Exception {
        FileWriter fileWriter = new FileWriter("crypto.txt");
        String plainText = readFile("plain.txt");
        char[] inputArray = plainText.toCharArray();
        String currentLine;
        FileReader fileReader = new FileReader("plain.txt");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        char[][] inputArrayTable = new char[(inputArray.length / 64)][64];
        int index = 0;
        int lenght = 0;
        while ((currentLine = bufferedReader.readLine()) != null) {
            inputArrayTable[index] = currentLine.toCharArray();
            lenght = currentLine.length();
            index++;
        }

        String key = readFile("key.txt");
        char[] keyArray = key.toCharArray();
        if (!plainText.equals("") && !key.equals("")) {
            for (int i = 0; i < inputArray.length / 64; i++) {
                StringBuilder keyLetters;
                StringBuilder textChar;
                int indexOfEncrypt;
                for (int j = 0; j < 64; j++) {
                    if (i == ((inputArray.length / 64) - 2) && j == (lenght)) {
                        while (j < 64) {
                            fileWriter.write("11111111");
                            fileWriter.write("\n");
                            j++;
                        }
                        break;
                    }
                    keyLetters = new StringBuilder(Integer
                            .toBinaryString(keyArray[j]));

                    textChar = new StringBuilder(Integer.toBinaryString(inputArrayTable[i][j]));
                    while (textChar.length() < 8) {
                        textChar.insert(0, "0");
                    }
                    while (keyLetters.length() < 8) {
                        keyLetters.insert(0, "0");
                    }
                    for (int c = 0; c < 8; c++) {
                        indexOfEncrypt =
                                Integer.parseInt(
                                        keyLetters.charAt(c) + ""
                                )
                                        ^
                                        Integer.parseInt(
                                                textChar.charAt(c) + ""
                                        );
                        fileWriter.write(indexOfEncrypt + "");
                    }
                    fileWriter.write("\n");
                }
            }
            fileWriter.close();
        }
    }

    private static void cryptanalysis() throws Exception {
        String encryptedText = readFile("crypto.txt");
        String thisLine;
        FileReader fileReader = new FileReader("crypto.txt");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        if (!encryptedText.equals("")) {
            char[] foundKeyCrypto = new char[64];
            for (int u = 0; u < 64; u++) {
                foundKeyCrypto[u] = ' ';
            }
            StringBuilder decodedString = new StringBuilder();
            int i = 0;
            while ((thisLine = bufferedReader.readLine()) != null) {
                if ((thisLine.charAt(0) == '0') && (thisLine.charAt(1) == '0')) {
                    if (thisLine.indexOf('1') > 0) {
                        if (foundKeyCrypto[i % 64] == ' ') {
                            foundKeyCrypto[i % 64] = '.';
                        }
                    }
                } else if ((thisLine.charAt(0) == '0') && (thisLine.charAt(1) == '1')) {
                    if (foundKeyCrypto[i % 64] == '.') {
                        foundKeyCrypto[i % 64] = decrypt(thisLine);
                    }
                }
                i++;
            }
            for (int s = 0; s < 64; s++) {
                if (foundKeyCrypto[s] == '.') {
                    foundKeyCrypto[s] = ' ';
                }
                decodedString.append(foundKeyCrypto[s]);
            }
            createFile("key-crypto.txt", decodedString.toString().toLowerCase());
        }
    }

    private static char decrypt(String byteDigit) {
        String spaceBinary = "00100000";
        int indexOfDecrypt;
        StringBuilder decryptText = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            indexOfDecrypt = Integer.parseInt(
                    spaceBinary.charAt(i) + "")
                    ^
                    Integer.parseInt(
                            byteDigit.charAt(i) + ""
                    );
            decryptText.append(indexOfDecrypt);
        }
        int curParsedInt = Integer.parseInt(decryptText.toString(), 2);
        return (char) curParsedInt;
    }

    private static void decrypt() throws Exception {
        FileWriter fileWriter = new FileWriter("decrypt.txt");
        String cryptoText = readFile("crypto.txt");
        String key = readFile("key-crypto.txt");
        String thisLine;
        FileReader fileReader = new FileReader("crypto.txt");
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        if (!cryptoText.equals("")) {
            StringBuilder decryptString = new StringBuilder();
            int decryptChar;
            int x = 0;
            StringBuilder keyChar;

            while ((thisLine = bufferedReader.readLine()) != null) {
                if (thisLine.charAt(0) != '0') {
                    break;
                }
                if ((x % 64 == 0) && (x > 0)) {
                    fileWriter.write("\n");
                }
                StringBuilder digitDecryptString = new StringBuilder();
                keyChar = new StringBuilder(Integer.toBinaryString(key.charAt(x % 64)));
                while (keyChar.length() < 8) {
                    keyChar.insert(0, "0");
                }
                for (int i = 0; i < 8; i++) {
                    decryptChar = (
                            Integer.parseInt(thisLine.charAt(i) + "")
                                    ^
                                    Integer.parseInt(keyChar.charAt(i) + "")
                    );
                    digitDecryptString.append(decryptChar);
                }
                decryptString.append((char) Integer.parseInt(digitDecryptString.toString(), 2));
                fileWriter.write((char) Integer.parseInt(digitDecryptString.toString(), 2));
                x++;
            }
            fileWriter.close();
        }
    }

    private static String readFile(String fileName) throws Exception {
        File file = new File(fileName);
        String loadedText = "";
        if (file.canRead()) {
            loadedText = new String(Files.readAllBytes(Paths.get(fileName)));
        } else {
            System.out.println("Error with reading " + fileName);
        }

        return loadedText;
    }

    private static void createFile(String fileName, String input) throws Exception {
        File file = new File(fileName);
        FileWriter writer = new FileWriter(file);
        writer.write(input);
        writer.close();
    }
}
