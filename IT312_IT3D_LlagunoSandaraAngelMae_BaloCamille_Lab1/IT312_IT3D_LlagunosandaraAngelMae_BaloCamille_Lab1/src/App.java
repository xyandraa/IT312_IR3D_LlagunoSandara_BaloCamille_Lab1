import java.util.Arrays;
import java.util.Scanner;

public class App {
     private static String promptPlaintext(Scanner sc) {
        while (true) {
            System.out.print("Enter plaintext (letters only; spaces allowed): ");
            String raw = sc.nextLine();
            if (raw == null || raw.trim().isEmpty()) {
                System.out.println("Error: Plaintext cannot be empty.");
                continue;
            }
            if (raw.matches(".*\\d.*")) {
                System.out.println("Error: Plaintext must not contain digits. Please enter letters only.");
                continue;
            }
    
            String cleaned = raw.replaceAll("[^a-zA-Z]", "");
            if (cleaned.isEmpty()) {
                System.out.println("Error: Plaintext must contain letters (A-Z).");
                continue;
            }
            return cleaned.toUpperCase();
        }
    }

    
    private static int[] promptKey(Scanner sc) {
        while (true) {
            System.out.print("Enter numeric key (digits only, e.g. 31425): ");
            String keyStr = sc.nextLine().trim();
            if (!keyStr.matches("\\d+")) {
                System.out.println("Error: Key must contain digits only (no letters or symbols).");
                continue;
            }
            int n = keyStr.length();
            int[] key = new int[n];
            boolean[] seen = new boolean[n + 1]; 
            boolean ok = true;
            for (int i = 0; i < n; i++) {
                int d = Character.getNumericValue(keyStr.charAt(i));
                key[i] = d;
                if (d < 1 || d > n) {
                    System.out.println("Error: Each key digit must be in the range 1.." + n + ".");
                    ok = false;
                    break;
                }
                if (seen[d]) {
                    System.out.println("Error: Duplicate digit '" + d + "' found in key. Key digits must be unique.");
                    ok = false;
                    break;
                }
                seen[d] = true;
            }
            if (!ok) continue;
        
            for (int i = 1; i <= n; i++) {
                if (!seen[i]) {
                    System.out.println("Error: Key must be a permutation of 1.." + n + ". Missing digit: " + i);
                    ok = false;
                    break;
                }
            }
            if (!ok) continue;
            return key;
        }
    }

    
    private static int getKeyIndex(int[] key, int colNumber) {
        for (int i = 0; i < key.length; i++) if (key[i] == colNumber) return i;
        return -1;
    }

    
    private static void printTable(int[] key, char[][] table) {
        int rows = table.length;
        int cols = key.length;
        int rowNumWidth = Integer.toString(rows).length(); 

        
        StringBuilder border = new StringBuilder("+");
        for (int j = 0; j < cols; j++) border.append("---+");
        String pad = String.format("%" + (rowNumWidth + 1) + "s", ""); 

        
        System.out.println(pad + border.toString());

    
        System.out.print(pad + "|");
        for (int j = 0; j < cols; j++) {
            System.out.printf(" %d |", key[j]);
        }
        System.out.println();

        
        System.out.println(pad + border.toString());

        
        for (int r = 0; r < rows; r++) {
            System.out.printf("%" + rowNumWidth + "d ", r + 1); 
            System.out.print("|");
            for (int c = 0; c < cols; c++) {
                char ch = table[r][c];
                
                if (ch == '\0') ch = ' ';
                System.out.printf(" %c |", ch);
            }
            System.out.println();
            System.out.println(pad + border.toString());
        }
    }


    private static String encryptAndPrint(String plaintext, int[] key) {
        String pt = plaintext.replaceAll("[^A-Z]", "");
        int cols = key.length;
        int rows = (int) Math.ceil((double) pt.length() / cols);

        
        char[][] table = new char[rows][cols];
        int idx = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (idx < pt.length()) table[r][c] = pt.charAt(idx++);
                else table[r][c] = 'X';
            }
        }

        
        System.out.println("\n=== ENCRYPTION ===");
        System.out.println("PT = " + pt);
        System.out.println("K  = " + Arrays.toString(key) + "  -> " + cols + " columns");
        System.out.println("Rows = " + rows);
        printTable(key, table);


        StringBuilder ct = new StringBuilder();
        for (int order = 1; order <= cols; order++) {
            int colIdx = getKeyIndex(key, order);
            for (int r = 0; r < rows; r++) {
                ct.append(table[r][colIdx]);
            }
        }
        System.out.println("CT = " + ct.toString());
        return ct.toString();
    }

    
    private static String decryptAndPrint(String ciphertext, int[] key) {
        String ct = ciphertext.replaceAll("[^A-Z]", ""); 
        int cols = key.length;
        int rows = (int) Math.ceil((double) ct.length() / cols);

        System.out.println("\n=== DECRYPTION ===");
        System.out.println("CT = " + ct);
        System.out.println("K  = " + Arrays.toString(key) + "  -> " + cols + " columns");
        System.out.println("Rows = " + rows);

        
        String[] groupsInKeyOrder = new String[cols];
        int idx = 0;
        for (int order = 1; order <= cols; order++) {
            int take = Math.min(rows, ct.length() - idx); 
            if (take < 0) take = 0;
            String g = ct.substring(idx, idx + take);
            groupsInKeyOrder[order - 1] = g;
            idx += take;
        }

        
        System.out.print("CT Division: ");
        for (int i = 0; i < cols; i++) {
            System.out.print(groupsInKeyOrder[i]);
            if (i < cols - 1) System.out.print(" | ");
        }
        System.out.println();

        char[][] table = new char[rows][cols];
        for (int order = 1; order <= cols; order++) {
            int colIdx = getKeyIndex(key, order); 
            String group = groupsInKeyOrder[order - 1];
            for (int r = 0; r < rows; r++) {
                if (r < group.length()) table[r][colIdx] = group.charAt(r);
                else table[r][colIdx] = 'X';
            }
        }

        
        printTable(key, table);

    
        StringBuilder pt = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                pt.append(table[r][c]);
            }
        }
        
        String recovered = pt.toString().replaceAll("X+$", "");
        System.out.println("PT = " + recovered);
        return recovered;
    }

       public static void main(String[] args) throws Exception {
           Scanner sc = new Scanner(System.in);


        String plaintext = promptPlaintext(sc);
        int[] key = promptKey(sc);

        
        String ciphertext = encryptAndPrint(plaintext, key);

    
        decryptAndPrint(ciphertext, key);

        sc.close();
    }
}

