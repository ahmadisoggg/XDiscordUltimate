package com.xreatlabs.xdiscordultimate.modules.minigames;

import com.xreatlabs.xdiscordultimate.XDiscordUltimate;
import com.xreatlabs.xdiscordultimate.modules.Module;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MiniGamesModule extends Module implements Listener {
    
    private MiniGamesListener discordListener;
    private final Map<String, Poll> activePolls = new ConcurrentHashMap<>();
    private final Map<String, MiniGame> activeGames = new ConcurrentHashMap<>();
    private final Map<UUID, Long> gameCooldowns = new ConcurrentHashMap<>();
    
    // Configuration
    private String gamesChannelName;
    private int maxPollDuration;
    private boolean allowMultipleVotes;
    private int gameCooldownMinutes;
    
    // Emoji for polls
    private static final String[] POLL_EMOJIS = {"1️⃣", "2️⃣", "3️⃣", "4️⃣", "5️⃣", "6️⃣", "7️⃣", "8️⃣", "9️⃣", "🔟"};
    
    public MiniGamesModule(XDiscordUltimate plugin) {
        super(plugin);
    }
    
    @Override
    public String getName() {
        return "MiniGames";
    }
    
    @Override
    public String getDescription() {
        return "Interactive mini-games and polls system";
    }
    
    @Override
    protected void onEnable() {
        loadConfiguration();
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Register Discord listener
        if (plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            discordListener = new MiniGamesListener();
            plugin.getDiscordManager().getJDA().addEventListener(discordListener);
        }
        
        info("Mini-games module enabled");
    }
    
    @Override
    protected void onDisable() {
        // Unregister Discord listener
        if (discordListener != null && plugin.getDiscordManager() != null && plugin.getDiscordManager().isReady()) {
            plugin.getDiscordManager().getJDA().removeEventListener(discordListener);
        }
        
        // End all active games and polls
        activePolls.values().forEach(poll -> endPoll(poll.id, true));
        activeGames.values().forEach(game -> endGame(game.id, true));
        
        info("Mini-games module disabled");
    }
    
    private void loadConfiguration() {
        gamesChannelName = getConfig().getString("games-channel", "mini-games");
        maxPollDuration = getConfig().getInt("max-poll-duration-minutes", 60);
        allowMultipleVotes = getConfig().getBoolean("allow-multiple-votes", false);
        gameCooldownMinutes = getConfig().getInt("game-cooldown-minutes", 5);
    }
    
    /**
     * Create a new poll
     */
    public void createPoll(Player creator, String question, List<String> options, int durationMinutes) {
        if (options.size() < 2 || options.size() > 10) {
            plugin.getMessageManager().sendError(creator, "Polls must have between 2 and 10 options!");
            return;
        }
        
        if (durationMinutes > maxPollDuration) {
            plugin.getMessageManager().sendError(creator, "Poll duration cannot exceed " + maxPollDuration + " minutes!");
            return;
        }
        
        String pollId = UUID.randomUUID().toString().substring(0, 8);
        Poll poll = new Poll(pollId, creator.getName(), question, options, durationMinutes);
        activePolls.put(pollId, poll);
        
        // Send to Discord
        sendPollToDiscord(poll);
        
        // Announce in Minecraft
        Bukkit.broadcastMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "📊 New Poll by " + creator.getName() + "!");
        Bukkit.broadcastMessage(ChatColor.WHITE + question);
        Bukkit.broadcastMessage("");
        for (int i = 0; i < options.size(); i++) {
            Bukkit.broadcastMessage(ChatColor.GRAY.toString() + (i + 1) + ". " + ChatColor.WHITE + options.get(i));
        }
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.AQUA + "Vote with: /poll vote " + pollId + " <option>");
        Bukkit.broadcastMessage(ChatColor.GRAY + "Duration: " + durationMinutes + " minutes");
        Bukkit.broadcastMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // Schedule poll end
        new BukkitRunnable() {
            @Override
            public void run() {
                endPoll(pollId, false);
            }
        }.runTaskLater(plugin, 20L * 60 * durationMinutes);
    }
    
    /**
     * Start a mini-game
     */
    public void startMiniGame(Player initiator, String gameType) {
        UUID uuid = initiator.getUniqueId();
        
        // Check cooldown
        if (gameCooldowns.containsKey(uuid)) {
            long cooldownEnd = gameCooldowns.get(uuid);
            if (System.currentTimeMillis() < cooldownEnd) {
                long remainingSeconds = (cooldownEnd - System.currentTimeMillis()) / 1000;
                plugin.getMessageManager().sendError(initiator, 
                    "Please wait " + remainingSeconds + " seconds before starting another game!");
                return;
            }
        }
        
        String gameId = UUID.randomUUID().toString().substring(0, 8);
        MiniGame game;
        
        switch (gameType.toLowerCase()) {
            case "trivia":
                game = createTriviaGame(gameId, initiator.getName());
                break;
            case "math":
                game = createMathGame(gameId, initiator.getName());
                break;
            case "scramble":
                game = createWordScrambleGame(gameId, initiator.getName());
                break;
            case "reaction":
                game = createReactionGame(gameId, initiator.getName());
                break;
            default:
                plugin.getMessageManager().sendError(initiator, "Unknown game type: " + gameType);
                return;
        }
        
        activeGames.put(gameId, game);
        gameCooldowns.put(uuid, System.currentTimeMillis() + (gameCooldownMinutes * 60 * 1000));
        
        // Send to Discord
        sendGameToDiscord(game);
        
        // Announce in Minecraft
        announceGame(game);
        
        // Set timeout
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeGames.containsKey(gameId)) {
                    endGame(gameId, false);
                }
            }
        }.runTaskLater(plugin, 20L * 60); // 1 minute timeout
    }
    
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");
        String command = args[0].toLowerCase();
        
        if (command.equals("/poll")) {
            event.setCancelled(true);
            handlePollCommand(event.getPlayer(), args);
        } else if (command.equals("/game")) {
            event.setCancelled(true);
            handleGameCommand(event.getPlayer(), args);
        }
    }
    
    private void handlePollCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /poll <create|vote|results>");
            return;
        }
        
        switch (args[1].toLowerCase()) {
            case "create":
                openPollCreator(player);
                break;
            case "vote":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Usage: /poll vote <poll-id> <option>");
                    return;
                }
                votePoll(player, args[2], args[3]);
                break;
            case "results":
                if (args.length < 3) {
                    showActivePolls(player);
                } else {
                    showPollResults(player, args[2]);
                }
                break;
        }
    }
    
    private void handleGameCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /game <answer|start> [type]");
            return;
        }
        
        if (args[1].equalsIgnoreCase("answer") && args.length >= 3) {
            String answer = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            checkGameAnswer(player, answer);
        } else if (args[1].equalsIgnoreCase("start") && args.length >= 3) {
            startMiniGame(player, args[2]);
        } else {
            player.sendMessage(ChatColor.YELLOW + "Available games: trivia, math, scramble, reaction");
        }
    }
    
    private void openPollCreator(Player player) {
        // TODO: Implement GUI poll creator
        plugin.getMessageManager().sendInfo(player, "GUI poll creator coming soon! Use Discord to create polls for now.");
    }
    
    private void votePoll(Player player, String pollId, String optionStr) {
        Poll poll = activePolls.get(pollId);
        if (poll == null) {
            plugin.getMessageManager().sendError(player, "Poll not found or has ended!");
            return;
        }
        
        try {
            int option = Integer.parseInt(optionStr) - 1;
            if (option < 0 || option >= poll.options.size()) {
                plugin.getMessageManager().sendError(player, "Invalid option number!");
                return;
            }
            
            String voterId = player.getUniqueId().toString();
            
            if (!allowMultipleVotes && poll.hasVoted(voterId)) {
                plugin.getMessageManager().sendError(player, "You have already voted in this poll!");
                return;
            }
            
            poll.addVote(voterId, option);
            plugin.getMessageManager().sendSuccess(player, "Vote recorded for: " + poll.options.get(option));
            
        } catch (NumberFormatException e) {
            plugin.getMessageManager().sendError(player, "Please enter a valid option number!");
        }
    }
    
    private void showActivePolls(Player player) {
        if (activePolls.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No active polls at the moment.");
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage(ChatColor.YELLOW + "Active Polls:");
        activePolls.values().forEach(poll -> {
            player.sendMessage(ChatColor.WHITE + "• " + poll.question + 
                ChatColor.GRAY + " (ID: " + poll.id + ")");
        });
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    private void showPollResults(Player player, String pollId) {
        Poll poll = activePolls.get(pollId);
        if (poll == null) {
            plugin.getMessageManager().sendError(player, "Poll not found!");
            return;
        }
        
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage(ChatColor.YELLOW + "Poll Results: " + ChatColor.WHITE + poll.question);
        player.sendMessage("");
        
        int totalVotes = poll.getTotalVotes();
        for (int i = 0; i < poll.options.size(); i++) {
            int votes = poll.getVotesForOption(i);
            double percentage = totalVotes > 0 ? (votes * 100.0 / totalVotes) : 0;
            
            player.sendMessage(ChatColor.GRAY.toString() + (i + 1) + ". " + ChatColor.WHITE + poll.options.get(i));
            player.sendMessage(ChatColor.AQUA + "   " + votes + " votes (" + 
                String.format("%.1f", percentage) + "%)");
        }
        
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "Total votes: " + totalVotes);
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    private void checkGameAnswer(Player player, String answer) {
        for (MiniGame game : activeGames.values()) {
            if (game.checkAnswer(answer)) {
                // Correct answer!
                game.winner = player.getName();
                endGame(game.id, false);
                
                // Reward player
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                    "eco give " + player.getName() + " 100");
                
                return;
            }
        }
        
        plugin.getMessageManager().sendError(player, "Incorrect answer!");
    }
    
    private void sendPollToDiscord(Poll poll) {
        TextChannel channel = getGamesChannel();
        if (channel == null) return;
        
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(new Color(255, 215, 0))
            .setTitle("📊 " + poll.question)
            .setDescription("React to vote! Poll ends in " + poll.durationMinutes + " minutes.")
            .setFooter("Poll ID: " + poll.id + " | Created by " + poll.creator, null)
            .setTimestamp(Instant.now());
        
        for (int i = 0; i < poll.options.size(); i++) {
            embed.addField(POLL_EMOJIS[i] + " Option " + (i + 1), poll.options.get(i), false);
        }
        
        channel.sendMessageEmbeds(embed.build()).queue(message -> {
            poll.discordMessageId = message.getId();
            
            // Add reactions
            for (int i = 0; i < poll.options.size(); i++) {
                message.addReaction(Emoji.fromUnicode(POLL_EMOJIS[i])).queue();
            }
        });
    }
    
    private void sendGameToDiscord(MiniGame game) {
        TextChannel channel = getGamesChannel();
        if (channel == null) return;
        
        EmbedBuilder embed = new EmbedBuilder()
            .setColor(new Color(0, 255, 0))
            .setTitle("🎮 " + game.type + " Game!")
            .setDescription(game.question)
            .addField("How to Play", game.instructions, false)
            .addField("Reward", "💰 100 coins", true)
            .setFooter("Started by " + game.initiator, null)
            .setTimestamp(Instant.now());
        
        channel.sendMessageEmbeds(embed.build())
            .setActionRow(
                Button.primary("game_answer_" + game.id, "Submit Answer")
                    .withEmoji(Emoji.fromUnicode("✏️"))
            )
            .queue(message -> game.discordMessageId = message.getId());
    }
    
    private void announceGame(MiniGame game) {
        Bukkit.broadcastMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "🎮 " + game.type + " Game Started!");
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.WHITE + game.question);
        Bukkit.broadcastMessage("");
        Bukkit.broadcastMessage(ChatColor.AQUA + game.instructions);
        Bukkit.broadcastMessage(ChatColor.GREEN + "Answer with: /game answer <your answer>");
        Bukkit.broadcastMessage(ChatColor.GRAY + "Reward: 100 coins");
        Bukkit.broadcastMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
    
    private void endPoll(String pollId, boolean cancelled) {
        Poll poll = activePolls.remove(pollId);
        if (poll == null) return;
        
        if (!cancelled) {
            // Announce results
            int totalVotes = poll.getTotalVotes();
            int winningOption = poll.getWinningOption();
            
            String results = ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                ChatColor.YELLOW + "📊 Poll Results: " + ChatColor.WHITE + poll.question + "\n\n";
            
            for (int i = 0; i < poll.options.size(); i++) {
                int votes = poll.getVotesForOption(i);
                double percentage = totalVotes > 0 ? (votes * 100.0 / totalVotes) : 0;
                
                String prefix = (i == winningOption) ? ChatColor.GREEN + "➤ " : ChatColor.GRAY + "  ";
                results += prefix + poll.options.get(i) + "\n";
                results += ChatColor.AQUA + "   " + votes + " votes (" + 
                    String.format("%.1f", percentage) + "%)\n";
            }
            
            results += "\n" + ChatColor.GRAY + "Total votes: " + totalVotes + "\n";
            results += ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";
            
            Bukkit.broadcastMessage(results);
            
            // Update Discord message
            updatePollDiscordMessage(poll, true);
        }
    }
    
    private void endGame(String gameId, boolean cancelled) {
        MiniGame game = activeGames.remove(gameId);
        if (game == null) return;
        
        if (!cancelled) {
            if (game.winner != null) {
                Bukkit.broadcastMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                Bukkit.broadcastMessage(ChatColor.YELLOW + "🎉 Game Winner: " + 
                    ChatColor.GREEN + game.winner + "!");
                Bukkit.broadcastMessage(ChatColor.WHITE + "Answer: " + game.answer);
                Bukkit.broadcastMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            } else {
                Bukkit.broadcastMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                Bukkit.broadcastMessage(ChatColor.YELLOW + "⏰ Game Timed Out!");
                Bukkit.broadcastMessage(ChatColor.WHITE + "Answer was: " + game.answer);
                Bukkit.broadcastMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }
            
            // Update Discord message
            updateGameDiscordMessage(game);
        }
    }
    
    private void updatePollDiscordMessage(Poll poll, boolean ended) {
        if (poll.discordMessageId == null) return;
        
        TextChannel channel = getGamesChannel();
        if (channel == null) return;
        
        channel.retrieveMessageById(poll.discordMessageId).queue(message -> {
            EmbedBuilder embed = new EmbedBuilder()
                .setColor(ended ? new Color(128, 128, 128) : new Color(255, 215, 0))
                .setTitle("📊 " + poll.question + (ended ? " [ENDED]" : ""))
                .setFooter("Poll ID: " + poll.id + " | Created by " + poll.creator, null)
                .setTimestamp(Instant.now());
            
            int totalVotes = poll.getTotalVotes();
            int winningOption = poll.getWinningOption();
            
            for (int i = 0; i < poll.options.size(); i++) {
                int votes = poll.getVotesForOption(i);
                double percentage = totalVotes > 0 ? (votes * 100.0 / totalVotes) : 0;
                
                String fieldName = POLL_EMOJIS[i] + " " + poll.options.get(i);
                if (ended && i == winningOption) {
                    fieldName = "🏆 " + fieldName;
                }
                
                embed.addField(fieldName, 
                    votes + " votes (" + String.format("%.1f", percentage) + "%)", 
                    false);
            }
            
            message.editMessageEmbeds(embed.build()).queue();
        });
    }
    
    private void updateGameDiscordMessage(MiniGame game) {
        if (game.discordMessageId == null) return;
        
        TextChannel channel = getGamesChannel();
        if (channel == null) return;
        
        channel.retrieveMessageById(game.discordMessageId).queue(message -> {
            EmbedBuilder embed = new EmbedBuilder()
                .setColor(game.winner != null ? new Color(0, 255, 0) : new Color(255, 0, 0))
                .setTitle("🎮 " + game.type + " Game! [ENDED]")
                .setDescription(game.question)
                .addField("Answer", game.answer, false)
                .addField("Winner", game.winner != null ? game.winner : "No winner", false)
                .setFooter("Started by " + game.initiator, null)
                .setTimestamp(Instant.now());
            
            message.editMessageEmbeds(embed.build()).queue();
            message.editMessageComponents().queue(); // Remove buttons
        });
    }
    
    private MiniGame createTriviaGame(String id, String initiator) {
        String[] questions = {
            "What year was Minecraft officially released?",
            "What is the rarest ore in Minecraft?",
            "How many blocks high can you build in Minecraft?",
            "What mob drops Ender Pearls?",
            "What dimension is the Wither found in?"
        };
        
        String[] answers = {
            "2011", "emerald", "256", "enderman", "nether"
        };
        
        int index = ThreadLocalRandom.current().nextInt(questions.length);
        
        return new MiniGame(id, "Trivia", initiator, questions[index], answers[index],
            "Type the correct answer in chat!");
    }
    
    private MiniGame createMathGame(String id, String initiator) {
        int a = ThreadLocalRandom.current().nextInt(10, 100);
        int b = ThreadLocalRandom.current().nextInt(10, 100);
        int operation = ThreadLocalRandom.current().nextInt(3);
        
        String question;
        int answer;
        
        switch (operation) {
            case 0:
                question = a + " + " + b + " = ?";
                answer = a + b;
                break;
            case 1:
                question = a + " - " + b + " = ?";
                answer = a - b;
                break;
            default:
                question = a + " × " + b + " = ?";
                answer = a * b;
                break;
        }
        
        return new MiniGame(id, "Math", initiator, question, String.valueOf(answer),
            "Solve the math problem!");
    }
    
    private MiniGame createWordScrambleGame(String id, String initiator) {
        String[] words = {
            "diamond", "creeper", "enderman", "nether", "redstone",
            "enchantment", "villager", "minecraft", "pickaxe", "crafting"
        };
        
        String word = words[ThreadLocalRandom.current().nextInt(words.length)];
        String scrambled = scrambleWord(word);
        
        return new MiniGame(id, "Word Scramble", initiator, 
            "Unscramble: " + scrambled.toUpperCase(), word,
            "Unscramble the word!");
    }
    
    private MiniGame createReactionGame(String id, String initiator) {
        String[] words = {
            "QUICK", "FAST", "SPEED", "REACT", "TYPE", "WIN"
        };
        
        String word = words[ThreadLocalRandom.current().nextInt(words.length)];
        
        // Delay announcement
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage(ChatColor.RED + "⚡ TYPE THIS: " + 
                    ChatColor.YELLOW + ChatColor.BOLD + word);
            }
        }.runTaskLater(plugin, 20L * ThreadLocalRandom.current().nextInt(3, 8));
        
        return new MiniGame(id, "Reaction", initiator,
            "Get ready to type the word that appears!", word.toLowerCase(),
            "Be the first to type the word when it appears!");
    }
    
    private String scrambleWord(String word) {
        List<Character> chars = word.chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.toList());
        Collections.shuffle(chars);
        return chars.stream()
            .map(String::valueOf)
            .collect(Collectors.joining());
    }
    
    private TextChannel getGamesChannel() {
        if (plugin.getDiscordManager() == null || !plugin.getDiscordManager().isReady()) {
            return null;
        }
        
        return plugin.getDiscordManager().getMainGuild()
            .getTextChannelsByName(gamesChannelName, true)
            .stream()
            .findFirst()
            .orElse(null);
    }
    
    private class MiniGamesListener extends ListenerAdapter {
        @Override
        public void onMessageReactionAdd(MessageReactionAddEvent event) {
            if (event.getUser().isBot()) return;
            
            // Find poll by Discord message ID
            activePolls.values().stream()
                .filter(poll -> event.getMessageId().equals(poll.discordMessageId))
                .findFirst()
                .ifPresent(poll -> {
                    String emoji = event.getReaction().getEmoji().getName();
                    for (int i = 0; i < POLL_EMOJIS.length && i < poll.options.size(); i++) {
                        if (POLL_EMOJIS[i].equals(emoji)) {
                            String voterId = "discord_" + event.getUserId();
                            if (allowMultipleVotes || !poll.hasVoted(voterId)) {
                                poll.addVote(voterId, i);
                                updatePollDiscordMessage(poll, false);
                            }
                            break;
                        }
                    }
                });
        }
        
        @Override
        public void onButtonInteraction(ButtonInteractionEvent event) {
            String buttonId = event.getComponentId();
            
            if (buttonId.startsWith("game_answer_")) {
                String gameId = buttonId.substring(12);
                MiniGame game = activeGames.get(gameId);
                
                if (game != null) {
                    event.reply("Please type your answer in this channel!").setEphemeral(true).queue();
                } else {
                    event.reply("This game has already ended!").setEphemeral(true).queue();
                }
            }
        }
    }
    
    /**
     * Poll data class
     */
    private static class Poll {
        final String id;
        final String creator;
        final String question;
        final List<String> options;
        final int durationMinutes;
        final long createdAt;
        String discordMessageId;
        
        private final Map<String, Integer> votes = new ConcurrentHashMap<>();
        private final int[] voteCounts;
        
        Poll(String id, String creator, String question, List<String> options, int durationMinutes) {
            this.id = id;
            this.creator = creator;
            this.question = question;
            this.options = new ArrayList<>(options);
            this.durationMinutes = durationMinutes;
            this.createdAt = System.currentTimeMillis();
            this.voteCounts = new int[options.size()];
        }
        
        void addVote(String voterId, int option) {
            Integer previousVote = votes.put(voterId, option);
            if (previousVote != null) {
                voteCounts[previousVote]--;
            }
            voteCounts[option]++;
        }
        
        boolean hasVoted(String voterId) {
            return votes.containsKey(voterId);
        }
        
        int getTotalVotes() {
            return Arrays.stream(voteCounts).sum();
        }
        
        int getVotesForOption(int option) {
            return voteCounts[option];
        }
        
        int getWinningOption() {
            int maxVotes = -1;
            int winner = 0;
            for (int i = 0; i < voteCounts.length; i++) {
                if (voteCounts[i] > maxVotes) {
                    maxVotes = voteCounts[i];
                    winner = i;
                }
            }
            return winner;
        }
    }
    
    /**
     * Mini-game data class
     */
    private static class MiniGame {
        final String id;
        final String type;
        final String initiator;
        final String question;
        final String answer;
        final String instructions;
        final long createdAt;
        String discordMessageId;
        String winner;
        
        MiniGame(String id, String type, String initiator, String question, 
                String answer, String instructions) {
            this.id = id;
            this.type = type;
            this.initiator = initiator;
            this.question = question;
            this.answer = answer;
            this.instructions = instructions;
            this.createdAt = System.currentTimeMillis();
        }
        
        boolean checkAnswer(String attempt) {
            return answer.equalsIgnoreCase(attempt.trim());
        }
    }
}