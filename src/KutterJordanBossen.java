import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class KutterJordanBossen {

    private static final double LAMBDA = 0.1;

    public static void embedText(BufferedImage image, String text) {
        String binaryMessage = textToBinary(text);
        embedMessage(image, binaryMessage);
    }


    public static String extractText(BufferedImage image, int messageLengthInBits) {
        String binaryMessage = extractMessage(image, messageLengthInBits);
        return binaryToText(binaryMessage);
    }

    // Метод для встраивания бинарного сообщения в изображение
    private static void embedMessage(BufferedImage image, String binaryMessage) {
        int width = image.getWidth();
        int height = image.getHeight();
        int messageLength = binaryMessage.length();

//        if (messageLength > width * height) {
//            throw new IllegalArgumentException("Сообщение слишком длинное для данного изображения.");
//        }

        int messageIndex = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (messageIndex >= messageLength) {
                    return;
                }

                // Получаем цвет пикселя
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Вычисляем яркость пикселя
                double Y = 0.3 * r + 0.59 * g + 0.11 * b;

                char bit = binaryMessage.charAt(messageIndex);
                int m = (bit == '1') ? 1 : -1;

                int newB = (int) (b + LAMBDA * m * Y);
                newB = Math.max(0, Math.min(255, newB));

                int newRGB = (r << 16) | (g << 8) | newB;
                image.setRGB(x, y, newRGB);

                messageIndex++;
            }
        }
    }

    // Метод для извлечения бинарного сообщения из изображения
    private static String extractMessage(BufferedImage image, int messageLengthInBits) {
        int width = image.getWidth();
        int height = image.getHeight();
        StringBuilder binaryMessage = new StringBuilder();

        int messageIndex = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (messageIndex >= messageLengthInBits) {
                    return binaryMessage.toString();
                }

                // Получаем цвет пикселя
                int rgb = image.getRGB(x, y);
                int b = rgb & 0xFF;

                // Прогнозируем значение яркости синего канала
                double predictedB = predictBlueValue(image, x, y);

                int m = (b > predictedB) ? 1 : 0;
                binaryMessage.append(m);

                messageIndex++;
            }
        }

        return binaryMessage.toString();
    }

    // Метод для прогнозирования значения яркости синего канала
    private static double predictBlueValue(BufferedImage image, int x, int y) {
        int width = image.getWidth();
        int height = image.getHeight();
        double sum = 0;
        int count = 0;

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;

                int nx = x + dx;
                int ny = y + dy;

                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    int rgb = image.getRGB(nx, ny);
                    int b = rgb & 0xFF;
                    sum += b;
                    count++;
                }
            }
        }

        return sum / count;
    }


    private static String textToBinary(String text) {
        StringBuilder binary = new StringBuilder();
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            String binaryByte = Integer.toBinaryString(b & 0xFF);

            while (binaryByte.length() < 8) {
                binaryByte = "0" + binaryByte;
            }
            binary.append(binaryByte);
        }
        return binary.toString();
    }

    private static String binaryToText(String binary) {

        byte[] bytes = new byte[binary.length() / 8];
        for (int i = 0; i < bytes.length; i++) {
            String byteString = binary.substring(i * 8, (i + 1) * 8);
            bytes[i] = (byte) Integer.parseInt(byteString, 2);
        }

        return new String(bytes, StandardCharsets.UTF_8);
    }


    public static void main(String[] args) {
        try {

            BufferedImage image = ImageIO.read(new File("input.jpg"));StringBuilder content = new StringBuilder();
            try {
                Scanner scanner = new Scanner(new File("a.txt"));
                while (scanner.hasNextLine()) {
                    content.append(scanner.nextLine()).append("\n");
                }
                scanner.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    
            String text = content.toString();

            //String text = "Это сообщение скрыто";
            //System.out.println("Исходное сообщение: " + text);
            embedText(image, text);
            ImageIO.write(image, "png", new File("output_KJB.png"));
            int messageLengthInBits = text.getBytes(StandardCharsets.UTF_8).length * 8; // Длина в битах
            String extractedText = extractText(image, messageLengthInBits);
            System.out.println("Извлеченное сообщение: " + extractedText);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}