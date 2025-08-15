package com.xreatlabs.xdiscordultimate.utils.imagemessage;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImageMessage {

    private final static char TRANSPARENT_CHAR = ' ';

    private final Color[] colors = {
            new Color(0, 0, 0),
            new Color(0, 0, 170),
            new Color(0, 170, 0),
            new Color(0, 170, 170),
            new Color(170, 0, 0),
            new Color(170, 0, 170),
            new Color(255, 170, 0),
            new Color(170, 170, 170),
            new Color(85, 85, 85),
            new Color(85, 85, 255),
            new Color(85, 255, 85),
            new Color(85, 255, 255),
            new Color(255, 85, 85),
            new Color(255, 85, 255),
            new Color(255, 255, 85),
            new Color(255, 255, 255),
    };

    private List<String> lines;

    public ImageMessage(BufferedImage image, int height, char imgChar) {
        this.lines = toChatLines(image, height, imgChar);
    }

    public ImageMessage(List<String> lines) {
        this.lines = lines;
    }

    public ImageMessage appendText(String... text) {
        for (int y = 0; y < lines.size(); y++) {
            if (y < text.length) {
                lines.set(y, lines.get(y) + " " + text[y]);
            }
        }
        return this;
    }

    public ImageMessage appendCenteredText(String... text) {
        for (int y = 0; y < lines.size(); y++) {
            if (y < text.length) {
                int len = ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH - lines.get(y).length();
                lines.set(y, lines.get(y) + center(text[y], len));
            }
        }
        return this;
    }

    public void sendToPlayer(Player player) {
        for (String line : lines) {
            player.sendMessage(line);
        }
    }

    private List<String> toChatLines(BufferedImage image, int height, char imgChar) {
        List<String> result = new ArrayList<>();
        BufferedImage resized = resizeImage(image, (int) (image.getWidth() * ((double) height / image.getHeight())), height);

        for (int y = 0; y < resized.getHeight(); y++) {
            StringBuilder builder = new StringBuilder();
            for (int x = 0; x < resized.getWidth(); x++) {
                int rgb = resized.getRGB(x, y);
                if (rgb >> 24 == 0x00) {
                    builder.append(TRANSPARENT_CHAR);
                    continue;
                }
                ChatColor closest = getClosestChatColor(new Color(rgb));
                builder.append(closest.toString()).append(imgChar);
            }
            result.add(builder.toString());
        }
        return result;
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        AffineTransform af = new AffineTransform();
        af.scale(
                width / (double) originalImage.getWidth(),
                height / (double) originalImage.getHeight());

        AffineTransformOp operation = new AffineTransformOp(af, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return operation.filter(originalImage, null);
    }

    private ChatColor getClosestChatColor(Color color) {
        double minDistance = Double.MAX_VALUE;
        int bestColor = 0;
        for (int i = 0; i < colors.length; i++) {
            double r = colors[i].getRed() - color.getRed();
            double g = colors[i].getGreen() - color.getGreen();
            double b = colors[i].getBlue() - color.getBlue();
            double distance = r * r + g * g + b * b;
            if (distance < minDistance) {
                minDistance = distance;
                bestColor = i;
            }
        }
        return ChatColor.values()[bestColor];
    }

    private String center(String s, int length) {
        if (s.length() > length) {
            return s.substring(0, length);
        } else if (s.length() == length) {
            return s;
        } else {
            int leftPadding = (length - s.length()) / 2;
            StringBuilder leftBuilder = new StringBuilder();
            for (int i = 0; i < leftPadding; i++) {
                leftBuilder.append(" ");
            }
            return leftBuilder.toString() + s;
        }
    }
}
