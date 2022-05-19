// TODO:
/*
package me.drex.itsours.command.bulk.util;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.drex.itsours.util.Pair;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class CriteriumParser implements SuggestionProvider<ServerCommandSource> {
    private static CriteriumParser instance = new CriteriumParser();
    private Set<String> criteria;
    private HashMap<String, Suggestor> criteriumSuggestors = new HashMap<>();

    private CriteriumParser() {
        criteriumSuggestors.put("owner", new Suggestor(StringArgumentType.greedyString()));
        criteriumSuggestors.put("dimension", new Suggestor(DimensionArgumentType.dimension()));
        criteriumSuggestors.put("area", new Suggestor(StringArgumentType.string()));
        criteriumSuggestors.put("x", new Suggestor(StringArgumentType.string()));
        criteriumSuggestors.put("y", new Suggestor(StringArgumentType.string()));
        criteriumSuggestors.put("z", new Suggestor(StringArgumentType.string()));
        this.criteria = criteriumSuggestors.keySet();
    }

    public static CriteriumParser getInstance() {
        return instance;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        String input = builder.getInput();
        int lastSpaceIndex = input.lastIndexOf(' ');
        char[] inputArr = input.toCharArray();
        int lastColonIndex = -1;
        for (int i = inputArr.length - 1; i >= 0; i--) {
            char c = inputArr[i];
            if (c == ':') { // encountered a colon
                lastColonIndex = i;
            } else if (lastColonIndex != -1 && c == ' ') { // we have encountered a space after our colon
                break;
            }
        }
        if (lastColonIndex == -1) { // no colon, just suggest criteria
            SuggestionsBuilder offsetBuilder = builder.createOffset(lastSpaceIndex + 1);
            builder.add(suggestCriteria(offsetBuilder));
        } else { // take last colon
            String[] spaceSplit = input.substring(0, lastColonIndex).split(" ");
            String criterium = spaceSplit[spaceSplit.length - 1];
            String criteriumArg = input.substring(lastColonIndex + 1);

            if (!criteriumSuggestors.containsKey(criterium)) {
                return builder.buildFuture();
            } else { // check if suggestor consumes the rest
                Suggestor suggestor = criteriumSuggestors.get(criterium);

                int remaining = suggestor.getRemaining(criteriumArg);
                if (remaining > 0) { // suggest new criterium
                    SuggestionsBuilder offsetBuilder = builder.createOffset(input.length() - remaining + 1);
                    return suggestCriteria(offsetBuilder).buildFuture();
                } else {
                    SuggestionsBuilder offsetBuilder = builder.createOffset(lastColonIndex + 1);
                    return suggestor.listSuggestions(context, offsetBuilder);
                }
            }
        }

        return builder.buildFuture();
    }

    private SuggestionsBuilder suggestCriteria(SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();
        for (String criterium : criteria) {
            if (criterium.startsWith(input)) {
                builder.suggest(criterium + ":");
            }
        }
        return builder;
    }

    public HashMap<String, Object> rawProperties(String s) throws CommandSyntaxException {
        StringReader reader = new StringReader(s);
        HashMap<String, Object> result = new HashMap<>();
        while (reader.canRead()) {
            String propertyName = reader.readStringUntil(':').trim();
            Suggestor suggestor = this.criteriumSuggestors.get(propertyName);
            if (suggestor == null) {
                throw new SimpleCommandExceptionType(new LiteralMessage("Unknown property value: " + propertyName))
                        .create();
            }
            result.put(propertyName, suggestor.parse(reader));
        }
        return result;
    }

    public List<Pair<String, Object>> rawEntries(String s) throws CommandSyntaxException {
        StringReader reader = new StringReader(s);
        List<Pair<String, Object>> result = new ArrayList<>();
        while (reader.canRead()) {
            String propertyName = reader.readStringUntil(':').trim();
            Suggestor suggestor = this.criteriumSuggestors.get(propertyName);
            if (suggestor == null) {
                throw new SimpleCommandExceptionType(new LiteralMessage("Unknown property value: " + propertyName))
                        .create();
            }
            result.add(new Pair<>(propertyName, suggestor.parse(reader)));
        }
        return result;
    }

    private static class Suggestor {
        boolean useSuggestionProvider = false;
        private SuggestionProvider<ServerCommandSource> suggestionProvider;
        private ArgumentType argumentType;

        public Suggestor(SuggestionProvider<ServerCommandSource> suggestionProvider) {
            this.suggestionProvider = suggestionProvider;
            this.useSuggestionProvider = true;
        }

        public Suggestor(ArgumentType argumentType) {
            this.argumentType = argumentType;
        }

        public CompletableFuture<Suggestions> listSuggestions(CommandContext<ServerCommandSource> context,
                                                              SuggestionsBuilder builder) {
            if (this.useSuggestionProvider) {
                try {
                    return this.suggestionProvider.getSuggestions(context, builder);
                } catch (CommandSyntaxException e) {
                    return builder.buildFuture();
                }
            } else {
                return this.argumentType.listSuggestions(context, builder);
            }
        }

        public int getRemaining(String s) {
            if (this.useSuggestionProvider) {
                int spaceIndex = s.lastIndexOf(' ');
                if (spaceIndex == -1)
                    return -1;
                return s.length() - s.lastIndexOf(' ');
            }
            try {
                StringReader reader = new StringReader(s);
                this.argumentType.parse(reader);
                return reader.getRemainingLength();
            } catch (CommandSyntaxException e) {
                return -1;
            }
        }

        public Object parse(StringReader reader) throws CommandSyntaxException {
            if (this.useSuggestionProvider) {
                int startPos = reader.getCursor();
                try {
                    return reader.readStringUntil(' ');
                } catch (CommandSyntaxException e) {
                    return reader.getString().substring(startPos);
                }
            } else {
                return this.argumentType.parse(reader);
            }
        }
    }
}
*/
