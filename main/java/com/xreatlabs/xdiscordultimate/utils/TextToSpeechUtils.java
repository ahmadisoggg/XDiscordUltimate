package com.xreatlabs.xdiscordultimate.utils;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.bukkit.entity.Player;

import javax.sound.sampled.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Text-to-Speech utility for voice channels
 */
public class TextToSpeechUtils {
    
    private final XDiscordUltimate plugin;
    private boolean ttsEnabled;
    private String ttsLanguage;
    private float ttsSpeed;
    private float ttsVolume;
    
    public TextToSpeechUtils(XDiscordUltimate plugin) {
        this.plugin = plugin;
        loadConfiguration();
    }
    
    private void loadConfiguration() {
        ttsEnabled = plugin.getConfig().getBoolean("features.voice-channel.tts.enabled", true);
        ttsLanguage = plugin.getConfig().getString("features.voice-channel.tts.language", "en");
        ttsSpeed = (float) plugin.getConfig().getDouble("features.voice-channel.tts.speed", 1.0);
        ttsVolume = (float) plugin.getConfig().getDouble("features.voice-channel.tts.volume", 0.8);
    }
    
    /**
     * Speak text in a voice channel
     */
    public CompletableFuture<Boolean> speakInChannel(VoiceChannel channel, String text) {
        if (!ttsEnabled || text == null || text.trim().isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Clean text for TTS
                String cleanText = cleanTextForTTS(text);
                if (cleanText.isEmpty()) {
                    return false;
                }
                
                // Generate TTS audio
                byte[] audioData = generateTTSAudio(cleanText);
                if (audioData == null) {
                    return false;
                }
                
                // Play audio in voice channel
                return playAudioInChannel(channel, audioData);
                
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to speak in voice channel", e);
                return false;
            }
        });
    }
    
    /**
     * Speak player join/leave events
     */
    public void announcePlayerEvent(Player player, String eventType) {
        if (!ttsEnabled) return;
        
        String message = "";
        switch (eventType.toLowerCase()) {
            case "join":
                message = player.getName() + " joined the server";
                break;
            case "leave":
                message = player.getName() + " left the server";
                break;
            case "death":
                message = player.getName() + " died";
                break;
            default:
                return;
        }
        
        // Find appropriate voice channel for the player
        VoiceChannel channel = findPlayerVoiceChannel(player);
        if (channel != null) {
            speakInChannel(channel, message);
        }
    }
    
    /**
     * Speak chat messages in voice channels
     */
    public void announceChatMessage(Player player, String message) {
        if (!ttsEnabled) return;
        
        // Check if TTS is enabled for chat
        if (!plugin.getConfig().getBoolean("features.voice-channel.tts.chat-messages", false)) {
            return;
        }
        
        VoiceChannel channel = findPlayerVoiceChannel(player);
        if (channel != null) {
            String ttsMessage = player.getName() + " says: " + message;
            speakInChannel(channel, ttsMessage);
        }
    }
    
    /**
     * Speak Discord messages in voice channels
     */
    public void announceDiscordMessage(String username, String message, VoiceChannel channel) {
        if (!ttsEnabled) return;
        
        if (!plugin.getConfig().getBoolean("features.voice-channel.tts.discord-messages", true)) {
            return;
        }
        
        String ttsMessage = username + " says: " + message;
        speakInChannel(channel, ttsMessage);
    }
    
    /**
     * Announce voice events (join/leave)
     */
    public void announceVoiceEvent(Player player, String message) {
        if (!ttsEnabled) return;
        
        VoiceChannel channel = findPlayerVoiceChannel(player);
        if (channel != null) {
            speakInChannel(channel, message);
        }
    }
    
    /**
     * Clean text for TTS (remove formatting, emojis, etc.)
     */
    private String cleanTextForTTS(String text) {
        if (text == null) return "";
        
        // Remove Minecraft color codes
        text = text.replaceAll("§[0-9a-fk-or]", "");
        
        // Remove Discord formatting
        text = text.replaceAll("[*_~`]", "");
        
        // Remove URLs
        text = text.replaceAll("https?://\\S+", "link");
        
        // Remove mentions
        text = text.replaceAll("<@!?\\d+>", "someone");
        text = text.replaceAll("<#\\d+>", "channel");
        text = text.replaceAll("<@&\\d+>", "role");
        
        // Remove emojis (basic Unicode emoji removal)
        text = text.replaceAll("[\\p{So}\\p{Cn}]", "");
        
        // Remove custom Discord emojis
        text = text.replaceAll("<a?:\\w+:\\d+>", "emoji");
        
        // Limit length
        if (text.length() > 200) {
            text = text.substring(0, 197) + "...";
        }
        
        return text.trim();
    }
    
    /**
     * Generate TTS audio using a simple TTS service
     */
    private byte[] generateTTSAudio(String text) {
        try {
            // Use a simple TTS approach - in a real implementation, you might use:
            // - Google Cloud Text-to-Speech API
            // - Amazon Polly
            // - Microsoft Speech Services
            // - Local TTS engines
            
            // For this example, we'll create a simple beep sound as placeholder
            return generateBeepAudio();
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to generate TTS audio", e);
            return null;
        }
    }
    
    /**
     * Generate a simple beep sound as placeholder for TTS
     */
    private byte[] generateBeepAudio() {
        try {
            int sampleRate = 44100;
            int duration = 1; // 1 second
            int frequency = 440; // A4 note
            
            byte[] audioData = new byte[sampleRate * duration * 2]; // 16-bit audio
            
            for (int i = 0; i < audioData.length / 2; i++) {
                double angle = 2.0 * Math.PI * i * frequency / sampleRate;
                short sample = (short) (Math.sin(angle) * 32767 * ttsVolume);
                
                // Convert to bytes (little-endian)
                audioData[i * 2] = (byte) (sample & 0xFF);
                audioData[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
            }
            
            return audioData;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to generate beep audio", e);
            return null;
        }
    }
    
    /**
     * Play audio in a voice channel
     */
    private boolean playAudioInChannel(VoiceChannel channel, byte[] audioData) {
        try {
            // Note: This is a simplified implementation
            // In a real Discord bot, you would need to:
            // 1. Connect to the voice channel
            // 2. Use an audio sending handler
            // 3. Send the audio data through JDA's audio system
            
            // For now, we'll just log that we would play audio
            plugin.getLogger().info("Would play TTS audio in channel: " + channel.getName());
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to play audio in voice channel", e);
            return false;
        }
    }
    
    /**
     * Find the voice channel for a player
     */
    private VoiceChannel findPlayerVoiceChannel(Player player) {
        if (plugin.getDiscordManager() == null || !plugin.getDiscordManager().isReady()) {
            return null;
        }
        
        String discordId = plugin.getAdminUtils().getDiscordId(player);
        if (discordId == null) {
            return null;
        }
        
        try {
            Guild guild = plugin.getDiscordManager().getMainGuild();
            Member member = guild.getMemberById(discordId);
            
            if (member != null && member.getVoiceState() != null && member.getVoiceState().inAudioChannel()) {
                return member.getVoiceState().getChannel().asVoiceChannel();
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error finding player voice channel", e);
        }
        
        return null;
    }
    
    /**
     * Check if TTS is enabled
     */
    public boolean isTTSEnabled() {
        return ttsEnabled;
    }
    
    /**
     * Set TTS enabled state
     */
    public void setTTSEnabled(boolean enabled) {
        this.ttsEnabled = enabled;
        plugin.getConfig().set("features.voice-channel.tts.enabled", enabled);
        plugin.saveConfig();
    }
    
    /**
     * Get TTS language
     */
    public String getTTSLanguage() {
        return ttsLanguage;
    }
    
    /**
     * Set TTS language
     */
    public void setTTSLanguage(String language) {
        this.ttsLanguage = language;
        plugin.getConfig().set("features.voice-channel.tts.language", language);
        plugin.saveConfig();
    }
    
    /**
     * Reload configuration
     */
    public void reload() {
        loadConfiguration();
    }
}