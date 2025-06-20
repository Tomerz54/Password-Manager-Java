import java.io.*;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class PasswordManager {
    private static final String FILE_NAME = "passwords.dat";
    private static final String KEY_FILE_NAME = "aeskey.dat";
    private static SecretKey secretKey;
    private String encryptedMasterPassword;
    private static List<PasswordEntry> entries = new ArrayList<>();

    public static void main(String[] args) {
        PasswordManager manager = new PasswordManager();
        loadOrGenerateKey();

        if (manager.initialize()) {
            manager.run();
            manager.saveEntries();
        }
    }

    private static void loadOrGenerateKey() {
        File keyFile = new File(KEY_FILE_NAME);
        if (keyFile.exists()) {
            try (FileInputStream fis = new FileInputStream(keyFile)) {
                byte[] keyBytes = fis.readAllBytes();
                secretKey = new SecretKeySpec(keyBytes, "AES");
            } catch (IOException e) {
                throw new RuntimeException("Error loading encryption key", e);
            }
        } else {
            try {
                KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                keyGen.init(128);
                secretKey = keyGen.generateKey();
                try (FileOutputStream fos = new FileOutputStream(KEY_FILE_NAME)) {
                    fos.write(secretKey.getEncoded());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error generating encryption key", e);
            }
        }
    }

    private boolean initialize() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("No master password found. Setting up a new master password.");
            setMasterPassword();
        } else {
            this.loadEntries();
            if (!authenticate()) {
                return false;
            }
        }
        return true;
    }

    private void setMasterPassword() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Set your Master Password: ");
        String masterPassword = scanner.nextLine();
        this.encryptedMasterPassword = encrypt(masterPassword); // COMPLETE THIS PART: Encrypt the master password
                saveEntries();
    }

    private boolean authenticate() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Master Password: ");
        String enteredPassword = scanner.nextLine();
        String encryptedEnteredPassword = encrypt(enteredPassword);// COMPLETE THIS PART: Encrypt the entered password
        return encryptedMasterPassword.equals(encryptedEnteredPassword);
    }
    public static void addPassword(Scanner scanner){
        System.out.print("Enter Account Name: ");
        String accountName = scanner.nextLine();
        System.out.print("Enter Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();
        String encryptedPassword = encrypt(password);

        PasswordEntry entry = new PasswordEntry(accountName,username,encryptedPassword);
        entries.add(entry);

        System.out.println("Password added successfully");
    }

    private static void viewPasswords(){
        for(PasswordEntry pe : entries){
            String decryptedPassword = decrypt(pe.getEncryptedPassword());
            System.out.println(pe.getAccountName());
            System.out.println(pe.getEncryptedPassword());
            System.out.println(pe.getUsername());
            System.out.println();
        }
    }
    private void removePassword(Scanner scanner) {
        System.out.print("Enter Account Name to remove: ");
        String accountName = scanner.nextLine();

        entries.removeIf(entry -> entry.getAccountName().equalsIgnoreCase(accountName));
        System.out.println("Password removed successfully.");
    }

    private static String encrypt(String enteredPassword){
        try{
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipher.ENCRYPT_MODE,secretKey);
            byte[] encryptedBytes = cipher.doFinal(enteredPassword.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }catch (Exception e){
            throw new RuntimeException("Error encrypting data "+e);
        }
    }

    private static String decrypt(String EncryptedPassword){
        try{
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipher.DECRYPT_MODE,secretKey);
            byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(EncryptedPassword));
            return new String(decryptedData);
        }catch (Exception e){
            throw new RuntimeException("Error decrypting data "+e);
        }
    }
    private void loadEntries(){
        try(ObjectInputStream ois = new ObjectInputStream
                (new FileInputStream(FILE_NAME))){
            this.encryptedMasterPassword = (String) ois.readObject();
            entries = (List<PasswordEntry>)ois.readObject();

        }catch (IOException | ClassNotFoundException io){
            System.out.println("no saved passwords found. starting fresh!");
        }
        catch (Exception e){
            throw new RuntimeException("Error loading entries"+e);
        }
    }
    private void saveEntries(){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))){
            oos.writeObject(encryptedMasterPassword);
            oos.writeObject(entries);
        }catch (IOException io){
            System.out.println("Error saving passwords : "+io.getMessage());
        }
    }

    private void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\nPassword Manager");
            System.out.println("1. Add Password");
            System.out.println("2. View Passwords");
            System.out.println("3. Remove Password");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1:
                    // COMPLETE THIS PART: Implement addPassword method
                    addPassword(scanner);
                    break;
                case 2:
                    // COMPLETE THIS PART: Implement viewPasswords method
                    viewPasswords();
                    break;
                case 3:
                    // COMPLETE THIS PART: Implement removePassword method
                    removePassword(scanner);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
