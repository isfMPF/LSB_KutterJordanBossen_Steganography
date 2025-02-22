import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class Main {

    // Метод для кодирования сообщения в изображение
    public static void encodeMessage(String inputImagePath, String outputImagePath, String message) {
        try {

            BufferedImage image = ImageIO.read(new File(inputImagePath));

            // Добавляем специальный символ для обозначения конца сообщения
            message += "#";


            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            int messageLength = messageBytes.length * 8;
            int[] messageBits = new int[messageLength];
            for (int i = 0; i < messageBytes.length; i++) {
                for (int j = 0; j < 8; j++) {
                    messageBits[i * 8 + j] = (messageBytes[i] >> (7 - j)) & 1;
                }
            }

            // Встраиваем сообщение в изображение
            int bitIndex = 0;
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    if (bitIndex >= messageLength) {
                        break;
                    }
                    
                    
                    // Получаем цвет пикселя
                    int rgb = image.getRGB(x, y);


                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;


                    if (bitIndex < messageLength) {
                        red = (red & 0xFE) | messageBits[bitIndex++];
                    }
                    if (bitIndex < messageLength) {
                        green = (green & 0xFE) | messageBits[bitIndex++];
                    }
                    if (bitIndex < messageLength) {
                        blue = (blue & 0xFE) | messageBits[bitIndex++];
                    }

                    int newRgb = (red << 16) | (green << 8) | blue;
                    image.setRGB(x, y, newRgb);
                }
            }

            // Сохраняем измененное изображение
            ImageIO.write(image, "png", new File(outputImagePath));
            System.out.println("Сообщение успешно скрыто в изображении.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для декодирования сообщения из изображения
    public static String decodeMessage(String imagePath) {
        try {

            BufferedImage image = ImageIO.read(new File(imagePath));
            StringBuilder bitBuffer = new StringBuilder();

            // Проходим по каждому пикселю изображения
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgb = image.getRGB(x, y);

                    int red = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue = rgb & 0xFF;

                    bitBuffer.append(red & 1);
                    bitBuffer.append(green & 1);
                    bitBuffer.append(blue & 1);
                }
            }

            // Преобразуем биты в байты
            byte[] messageBytes = new byte[bitBuffer.length() / 8];
            for (int i = 0; i < messageBytes.length; i++) {
                String byteString = bitBuffer.substring(i * 8, (i + 1) * 8);
                messageBytes[i] = (byte) Integer.parseInt(byteString, 2);
            }

            String message = new String(messageBytes, StandardCharsets.UTF_8);


            int endIndex = message.indexOf('#');
            if (endIndex != -1) {
                message = message.substring(0, endIndex);
            }

            return message;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        String inputImagePath = "input1.png";
        String outputImagePath = "output_LSB.png";
        StringBuilder content = new StringBuilder();
        try {
            Scanner scanner = new Scanner(new File("a.txt"));
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String message = content.toString();
        System.out.println("Размер текста: " + message.length());
       //String message = "Привет";


        encodeMessage(inputImagePath, outputImagePath, message);
        System.out.println("Сообщение скрыто в изображении: " + outputImagePath);

        String extractedMessage = decodeMessage(outputImagePath);
        System.out.println("Извлеченное сообщение: " + extractedMessage.length());
    }
}
