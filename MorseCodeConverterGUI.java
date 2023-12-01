import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class MorseCodeConverterGUI extends JFrame {

    private JTextField inputTextField;
    private JTextArea outputTextArea;

    private static final Map<Character, String> textToMorseMap = new HashMap<>();
    private static final Map<String, Character> morseToTextMap = new HashMap<>();

    private final AudioFormat audioFormat = new AudioFormat(8000.0f, 16, 1, true, true);
    private SourceDataLine sourceDataLine;

    static {
        // Define the mapping for text to Morse code
        textToMorseMap.put('A', ".-");
        textToMorseMap.put('B', "-...");
        textToMorseMap.put('C', "-.-.");
        textToMorseMap.put('D', "-..");
        textToMorseMap.put('E', ".");
        textToMorseMap.put('F', "..-.");
        textToMorseMap.put('G', "--.");
        textToMorseMap.put('H', "....");
        textToMorseMap.put('I', "..");
        textToMorseMap.put('J', ".---");
        textToMorseMap.put('K', "-.-");
        textToMorseMap.put('L', ".-..");
        textToMorseMap.put('M', "--");
        textToMorseMap.put('N', "-.");
        textToMorseMap.put('O', "---");
        textToMorseMap.put('P', ".--.");
        textToMorseMap.put('Q', "--.-");
        textToMorseMap.put('R', ".-.");
        textToMorseMap.put('S', "...");
        textToMorseMap.put('T', "-");
        textToMorseMap.put('U', "..-");
        textToMorseMap.put('V', "...-");
        textToMorseMap.put('W', ".--");
        textToMorseMap.put('X', "-..-");
        textToMorseMap.put('Y', "-.--");
        textToMorseMap.put('Z', "--..");
        textToMorseMap.put('0', "-----");
        textToMorseMap.put('1', ".----");
        textToMorseMap.put('2', "..---");
        textToMorseMap.put('3', "...--");
        textToMorseMap.put('4', "....-");
        textToMorseMap.put('5', ".....");
        textToMorseMap.put('6', "-....");
        textToMorseMap.put('7', "--...");
        textToMorseMap.put('8', "---..");
        textToMorseMap.put('9', "----.");
        // Add more characters as needed

        // Populate the mapping for Morse code to text
        for (Map.Entry<Character, String> entry : textToMorseMap.entrySet()) {
            morseToTextMap.put(entry.getValue(), entry.getKey());
        }
    }

    public MorseCodeConverterGUI() {
        super("Morse Code Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);

        JPanel panel = new JPanel();
        getContentPane().add(panel);
        placeComponents(panel);

        setVisible(true);
    }

    private void playMorseCode(String morseCode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (char c : morseCode.toCharArray()) {
                        if (c == '.') {
                            playSound(100); // dot
                        } else if (c == '-') {
                            playSound(300); // dash
                        } else if (c == ' ') {
                            pause(500); // space between symbols
                        }
                    }
                } finally {
                    // Close the SourceDataLine when sound playback is finished
                    if (sourceDataLine != null) {
                        sourceDataLine.drain();
                        sourceDataLine.stop();
                        sourceDataLine.close();
                    }
                }
            }
        }).start();
    }

    private void playSound(int duration) {
        try {
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();

            byte[] buffer = new byte[duration * 8]; // 8 bytes per millisecond
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = (byte) (Math.sin(i / (audioFormat.getSampleRate() / 440.0) * 2.0 * Math.PI) * 127.0);
            }
            sourceDataLine.write(buffer, 0, buffer.length);
            pause(100); // pause between dots and dashes
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void pause(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void placeComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel titleLabel = new JLabel("Morse Code Converter");
        titleLabel.setBounds(140, 10, 200, 25);
        panel.add(titleLabel);

        inputTextField = new JTextField(20);
        inputTextField.setBounds(30, 50, 200, 25);
        panel.add(inputTextField);

        JButton toMorseButton = new JButton("Text to Morse");
        toMorseButton.setBounds(250, 50, 120, 25);
        panel.add(toMorseButton);

        JButton toTextButton = new JButton("Morse to Text");
        toTextButton.setBounds(250, 80, 120, 25);
        panel.add(toTextButton);

        outputTextArea = new JTextArea();
        outputTextArea.setBounds(30, 120, 340, 100);
        panel.add(outputTextArea);

        JButton clearButton = new JButton("Clear");
        clearButton.setBounds(30, 230, 80, 25);
        panel.add(clearButton);

        JButton copyButton = new JButton("Copy");
        copyButton.setBounds(110, 230, 80, 25);
        panel.add(copyButton);

        JButton playButton = new JButton("Play Morse");
        playButton.setBounds(190, 230, 100, 25);
        panel.add(playButton);

        JButton exitButton = new JButton("Exit");
        exitButton.setBounds(300, 230, 80, 25);
        panel.add(exitButton);

        toMorseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputTextField.getText().toUpperCase(); // Convert to uppercase for consistency
                String morseCode = textToMorse(text);
                outputTextArea.setText(morseCode);
            }
        });

        toTextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String morseCode = inputTextField.getText();
                String text = morseToText(morseCode);
                outputTextArea.setText(text);
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                inputTextField.setText("");
                outputTextArea.setText("");
            }
        });

        copyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringSelection selection = new StringSelection(outputTextArea.getText());
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, null);
            }
        });

        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String morseCode = outputTextArea.getText();
                playMorseCode(morseCode);
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Exit the application
                System.exit(0);
            }
        });
    }

    private String textToMorse(String text) {
        StringBuilder morseCode = new StringBuilder();
        for (char ch : text.toCharArray()) {
            if (ch == ' ') {
                morseCode.append(" "); // Add space for word separation
            } else if (textToMorseMap.containsKey(ch)) {
                String code = textToMorseMap.get(ch);
                morseCode.append(code).append(" ");
            } else {
                // Handle unknown characters more gracefully
                morseCode.append("[?]");
            }
        }
        return morseCode.toString().trim();
    }

    private String morseToText(String morseCode) {
        StringBuilder text = new StringBuilder();
        String[] words = morseCode.split("   "); // Three spaces indicate word separation
        for (String word : words) {
            String[] letters = word.split(" ");
            for (String letter : letters) {
                if (morseToTextMap.containsKey(letter)) {
                    text.append(morseToTextMap.get(letter));
                } else {
                    // Handle unknown Morse code more gracefully
                    text.append("[?]");
                }
            }
            text.append(" ");
        }
        return text.toString().trim();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MorseCodeConverterGUI();
            }
        });
    }
}
