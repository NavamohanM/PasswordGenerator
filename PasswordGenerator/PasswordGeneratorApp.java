import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PasswordGeneratorApp extends JFrame {
    // Character pools
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUM = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{}|;:,.<>?";
    private static final String SIMILAR = "il1Lo0O";

    // UI Components
    private JSlider lengthSlider;
    private JCheckBox upperCaseBox, lowerCaseBox, numberBox, symbolBox, excludeSimilarBox, pronounceableBox;
    private JButton generateBtn, copyBtn, toggleViewBtn;
    private JPasswordField passwordField;
    private JProgressBar strengthBar;
    private JLabel strengthLabel;

    private boolean isPasswordVisible = false;

    public PasswordGeneratorApp() {
        setTitle("Secure Password Generator");
        setSize(600, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Options Panel
        JPanel optionsPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

        lengthSlider = new JSlider(8, 32, 16);
        lengthSlider.setMajorTickSpacing(4);
        lengthSlider.setPaintTicks(true);
        lengthSlider.setPaintLabels(true);

        upperCaseBox = new JCheckBox("Include Uppercase", true);
        lowerCaseBox = new JCheckBox("Include Lowercase", true);
        numberBox = new JCheckBox("Include Numbers", true);
        symbolBox = new JCheckBox("Include Symbols", true);
        excludeSimilarBox = new JCheckBox("Exclude Similar Characters");
        pronounceableBox = new JCheckBox("Pronounceable Mode");

        optionsPanel.add(new JLabel("Password Length:"));
        optionsPanel.add(lengthSlider);
        optionsPanel.add(upperCaseBox);
        optionsPanel.add(lowerCaseBox);
        optionsPanel.add(numberBox);
        optionsPanel.add(symbolBox);
        optionsPanel.add(excludeSimilarBox);
        optionsPanel.add(pronounceableBox);

        // Password Panel
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 5));
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Monospaced", Font.BOLD, 18));
        passwordField.setEditable(false);

        toggleViewBtn = new JButton("Show");
        toggleViewBtn.addActionListener(e -> togglePasswordVisibility());
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        passwordPanel.add(toggleViewBtn, BorderLayout.EAST);
        passwordPanel.setBorder(BorderFactory.createTitledBorder("Generated Password"));

        // Strength Meter
        strengthBar = new JProgressBar(0, 100);
        strengthBar.setStringPainted(true);
        strengthLabel = new JLabel("Strength: ");

        JPanel strengthPanel = new JPanel(new BorderLayout());
        strengthPanel.add(strengthLabel, BorderLayout.WEST);
        strengthPanel.add(strengthBar, BorderLayout.CENTER);

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        generateBtn = new JButton("Generate");
        copyBtn = new JButton("Copy");
        buttonPanel.add(generateBtn);
        buttonPanel.add(copyBtn);

        // Add to frame
        add(optionsPanel, BorderLayout.NORTH);
        add(passwordPanel, BorderLayout.CENTER);
        add(strengthPanel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.PAGE_END);

        // Button actions
        generateBtn.addActionListener(e -> generatePassword());
        copyBtn.addActionListener(e -> copyPassword());

        setVisible(true);
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordField.setEchoChar('*');
            toggleViewBtn.setText("Show");
        } else {
            passwordField.setEchoChar((char) 0);
            toggleViewBtn.setText("Hide");
        }
        isPasswordVisible = !isPasswordVisible;
    }

    private void generatePassword() {
        int length = lengthSlider.getValue();
        boolean useUpper = upperCaseBox.isSelected();
        boolean useLower = lowerCaseBox.isSelected();
        boolean useNum = numberBox.isSelected();
        boolean useSymbol = symbolBox.isSelected();
        boolean excludeSimilar = excludeSimilarBox.isSelected();
        boolean pronounceable = pronounceableBox.isSelected();

        try {
            String password;
            if (pronounceable) {
                password = generatePronounceablePassword(length);
            } else {
                password = generateSecurePassword(length, useUpper, useLower, useNum, useSymbol, excludeSimilar);
            }
            passwordField.setText(password);
            int entropyPercent = calculateEntropyPercent(password);
            strengthLabel.setText("Strength: " + entropyPercent + "%");
            strengthBar.setValue(entropyPercent);
            savePasswordToFile(password);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copyPassword() {
        String password = new String(passwordField.getPassword());
        if (!password.isEmpty()) {
            StringSelection selection = new StringSelection(password);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            JOptionPane.showMessageDialog(this, "Password copied to clipboard!");
        }
    }

    private String generateSecurePassword(int length, boolean useUpper, boolean useLower, boolean useNum, boolean useSymbols, boolean excludeSimilar) {
        if (length < 12) throw new IllegalArgumentException("Password length must be at least 12 characters.");

        StringBuilder charPool = new StringBuilder();
        List<Character> mandatoryChars = new ArrayList<>();
        SecureRandom rand = new SecureRandom();

        if (useUpper) charPool.append(UPPER);
        if (useLower) charPool.append(LOWER);
        if (useNum) charPool.append(NUM);
        if (useSymbols) charPool.append(SYMBOLS);

        if (excludeSimilar) {
            for (char c : SIMILAR.toCharArray()) {
                int idx;
                while ((idx = charPool.indexOf(String.valueOf(c))) != -1) {
                    charPool.deleteCharAt(idx);
                }
            }
        }

        if (charPool.length() == 0) throw new IllegalArgumentException("Select at least one character type.");

        if (useUpper) mandatoryChars.add(randomChar(UPPER, rand, excludeSimilar));
        if (useLower) mandatoryChars.add(randomChar(LOWER, rand, excludeSimilar));
        if (useNum) mandatoryChars.add(randomChar(NUM, rand, excludeSimilar));
        if (useSymbols) mandatoryChars.add(randomChar(SYMBOLS, rand, excludeSimilar));

        List<Character> pwdChars = new ArrayList<>(mandatoryChars);
        while (pwdChars.size() < length) {
            char next = charPool.charAt(rand.nextInt(charPool.length()));
            if (pwdChars.size() > 0 && pwdChars.get(pwdChars.size() - 1) == next) continue;
            pwdChars.add(next);
        }
        Collections.shuffle(pwdChars, rand);
        String password = listToString(pwdChars);

        if (containsSimplePattern(password)) {
            return generateSecurePassword(length, useUpper, useLower, useNum, useSymbols, excludeSimilar);
        }
        return password;
    }

    private char randomChar(String pool, SecureRandom rand, boolean excludeSimilar) {
        char c;
        do {
            c = pool.charAt(rand.nextInt(pool.length()));
        } while (excludeSimilar && SIMILAR.indexOf(c) >= 0);
        return c;
    }

    private boolean containsSimplePattern(String pwd) {
        String lower = pwd.toLowerCase();
        for (int i = 0; i < lower.length() - 2; i++) {
            int a = lower.charAt(i), b = lower.charAt(i + 1), c = lower.charAt(i + 2);
            if ((b == a + 1 && c == b + 1) || (b == a - 1 && c == b - 1))
                return true;
        }
        return false;
    }

    private String listToString(List<Character> chars) {
        StringBuilder sb = new StringBuilder(chars.size());
        for (char c : chars) sb.append(c);
        return sb.toString();
    }

    private String generatePronounceablePassword(int length) {
        String[] consonants = {"b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "r", "s", "t", "v", "w", "x", "z"};
        String[] vowels = {"a", "e", "i", "o", "u"};
        SecureRandom rand = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length / 2; i++) {
            sb.append(consonants[rand.nextInt(consonants.length)]);
            sb.append(vowels[rand.nextInt(vowels.length)]);
        }
        if (sb.length() > length) sb.setLength(length);
        return sb.toString();
    }

    private int calculateEntropyPercent(String pwd) {
        int space = 0;
        if (pwd.matches(".*[A-Z].*")) space += 26;
        if (pwd.matches(".*[a-z].*")) space += 26;
        if (pwd.matches(".*[0-9].*")) space += 10;
        if (pwd.matches(".*[!@#$%^&*()\\-_=+\\[\\]{}|;:,.<>?].*")) space += 25;
        double entropy = pwd.length() * (Math.log(space) / Math.log(2));
        double maxEntropy = 128.0;
        int percent = (int) Math.min(100, (entropy / maxEntropy) * 100);
        return percent;
    }

    private void savePasswordToFile(String password) {
        try (FileWriter writer = new FileWriter("password_history.txt", true)) {
            writer.write(password + System.lineSeparator());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving password: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PasswordGeneratorApp::new);
    }
}
