package fr.ycraft.jump.injection;

import com.google.common.base.Charsets;
import com.google.inject.AbstractModule;
import com.google.mu.util.stream.BiStream;
import fr.ycraft.jump.enums.Patterns;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.nowtryz.mcutils.templating.Pattern;
import net.nowtryz.mcutils.templating.PatternFactory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.EnumMap;

import static fr.ycraft.jump.enums.Patterns.FOLDER_NAME;
import static org.bukkit.configuration.file.YamlConfiguration.loadConfiguration;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TemplatesModule extends AbstractModule {
    EnumMap<Patterns, Pattern> patterns;
    private final File guiFolder;

    public TemplatesModule(JavaPlugin plugin) {
        this.guiFolder = new File(plugin.getDataFolder(), FOLDER_NAME);
        this.patterns = new EnumMap<>(BiStream.biStream(Arrays.asList(Patterns.values()))
                .mapValues(Patterns::getFileName)
                .mapValues(file -> loadPattern(file, plugin))
                .toMap());
    }

    private Pattern loadPattern(String patternName, JavaPlugin plugin) {
        // TODO error if file is not present or invalid
        try {
            return PatternFactory.compile(new File(guiFolder, patternName));
        } catch (Exception exception) {
            plugin.getLogger().severe("Unable to load " + patternName + " template: " + exception.getMessage());
            plugin.getLogger().severe("Falling back to default pattern");

            try (InputStream resource = plugin.getResource(FOLDER_NAME + "/" + patternName)) {
                return PatternFactory.compile(loadConfiguration(new InputStreamReader(resource, Charsets.UTF_8)));
            } catch (IOException e) {
                plugin.getLogger().severe("Unable to load " + patternName + " default template");
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    protected void configure() {
        this.patterns.forEach((patterns, pattern) ->
                bind(Pattern.class).annotatedWith(patterns.annotation()).toInstance(pattern));
    }
}
