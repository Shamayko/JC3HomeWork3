

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client extends JFrame {
    //Объявляем переменные для графики
    private JTextArea chatArea;
    private JTextField inputField;

    //Создаем потоки
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    //Открываем соединение, инициируем Гуи
    public Client() {
        try {
            openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initGUI();
    }

    //Запускаем графику
    public void initGUI() {
        setBounds(600, 300, 500, 500);
        setTitle("Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
//Добавляем панельки с вводом текста, кнопочкой отправки сообщения и т.п.
        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        JPanel panel = new JPanel(new BorderLayout());
        JButton sendButton = new JButton("Send");
        panel.add(sendButton, BorderLayout.EAST);
        inputField = new JTextField();
        panel.add(inputField, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);
//Добавляем слушателей на кнопочки отправки сообщения
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());
//Создаем слушателя на закрытие окошка
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    outputStream.writeUTF(ChatConstants.STOP_WORD);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
//Делаем все видимым
        setVisible(true);
    }

    //Открываем соединение, сокеты, порты
    private void openConnection() throws IOException {
        Socket socket = new Socket(ChatConstants.HOST, ChatConstants.PORT);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());
//Создаем новый поток
        new Thread(() -> {
            //Создаем трай, который ждет пока не пройдет авторизация
            try {
                while (true) {
                    String strFromServer = inputStream.readUTF();
                    if (strFromServer.equals(ChatConstants.AUTH_OK)) {
                        break;
                    }
                    chatArea.append(strFromServer);
                    chatArea.append("\n");
                }
//Создаем бесконечный цикл, который читает строку до тех пор пока не будет стоп слова.
                while (true) {
                    String strFromServer = inputStream.readUTF();
                    if (strFromServer.equals(ChatConstants.STOP_WORD)) {
                        break;
                    }
                    chatArea.append(strFromServer);
                    chatArea.append("\n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    //Пишем метод отправки сообщений, ловим исключения
    private void sendMessage() {
        if (!inputField.getText().trim().isEmpty()) {
            try {
                outputStream.writeUTF(inputField.getText());
                inputField.setText("");
                inputField.grabFocus();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Send error occured");
            }
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}
