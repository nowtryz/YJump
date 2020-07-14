package fr.ycraft.jump.manager;

import com.google.common.base.Charsets;
import fr.ycraft.jump.JumpPlugin;
import fr.ycraft.jump.entity.Jump;
import fr.ycraft.jump.util.LocationUtil;
import fr.ycraft.jump.util.MapCollector;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class JumpManager extends AbstractManager {
    private final File jumpsFolder;
    private final Configuration defaults;
    private final Map<String, Jump> jumps;
    private Map<Location, Jump> jumpStarts;
    private List<Location> protectedLocations;
    private List<World> protectedWorlds;

    public JumpManager(JumpPlugin plugin) {
        super(plugin);
        Configuration defaults = null;

        this.jumpsFolder = new File(plugin.getDataFolder(), "jumps");
        if (!this.jumpsFolder.mkdirs()) {
            try (
                final InputStream defConfigStream = this.plugin.getResource("jump.defaults.yml");
                final InputStreamReader reader = new InputStreamReader(defConfigStream, Charsets.UTF_8)
            ) {
                Validate.notNull(defConfigStream, "How the hell, the default jump file can be missing in the jar");
                defaults = YamlConfiguration.loadConfiguration(reader);

            } catch (IOException e) {
                this.plugin.getLogger().severe("Unable to load default jump data");
            } catch (IllegalStateException e) {
                this.plugin.getLogger().severe(() -> "Unable to load jump data: " + e.getMessage());
                this.plugin.getLogger().severe("Did you manually edit jump files?");
            }
        }

        this.defaults = defaults;
        this.jumps = new ConcurrentHashMap<>(Arrays
                .stream(this.jumpsFolder.listFiles((dir, name) -> (name.endsWith(".yml") || name.endsWith(".yaml"))))
                .filter(File::isFile)
                .map(YamlConfiguration::loadConfiguration)
                .peek(conf -> conf.setDefaults(this.defaults))
                .peek(conf -> conf.options().copyDefaults(true))
                .map(conf -> conf.get("jump"))
                .filter(Jump.class::isInstance)
                .map(Jump.class::cast)
                .collect(MapCollector.toMap(
                        name -> this.plugin.getLogger().severe(
                                "Jump \"" + name + "\" is duplicated, please verify your jump files"),
                        Jump::getName,
                        Function.identity()
                )));

        this.updateJumpList();
    }

    private class JumpCollector implements Collector<Jump, HashMap<String, Jump>, Map<String, Jump>> {

        @Override
        public Supplier<HashMap<String, Jump>> supplier() {
            return HashMap::new;
        }

        @Override
        public BiConsumer<HashMap<String, Jump>, Jump> accumulator() {
            return (map, jump) -> {
                if (map.putIfAbsent(jump.getName(), jump) != null) this.duplicateKeyException(jump.getName());
            };
        }

        @Override
        public BinaryOperator<HashMap<String, Jump>> combiner() {
            return (m1, m2) -> {
                for (Map.Entry<String,Jump> e : m2.entrySet()) {
                    String k = e.getKey();
                    Jump v = Objects.requireNonNull(e.getValue());
                    Jump u = m1.putIfAbsent(k, v);
                    if (u != null) this.duplicateKeyException(k);
                }
                return m1;
            };
        }

        private void duplicateKeyException(String name) {
            JumpManager.this.plugin.getLogger().severe(
                    () -> "Jump " + name + " is duplicated, please verify jump files"
            );
        }

        @Override
        public Function<HashMap<String, Jump>, Map<String, Jump>> finisher() {
            return map -> map;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return new HashSet<>(Arrays.asList(
                Characteristics.IDENTITY_FINISH,
                Characteristics.UNORDERED
            ));
        }
    }

    public void updateJumpList() {
        this.jumpStarts = this.jumps.values()
                .stream().parallel()
                .filter(jump -> jump.getStart().isPresent())
                .filter(jump -> jump.getEnd().isPresent())
                .collect(Collectors.toMap(jump -> jump.getStart().get(), Function.identity()));

        List<Location> protectedLocations = new ArrayList<>();
        protectedLocations.addAll(this.jumpStarts.keySet());
        protectedLocations.addAll(this.jumps.values().stream().parallel()
                .map(Jump::getEnd)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList())
        );
        protectedLocations.addAll(this.jumps.values().stream().parallel()
                .map(Jump::getCheckpoints)
                .flatMap(List::stream)
                .collect(Collectors.toList())
        );

        this.protectedLocations = protectedLocations.stream().parallel()
                .map(LocationUtil::toBlock)
                .collect(Collectors.toList());
        this.protectedWorlds = this.protectedLocations.stream().parallel()
                .map(Location::getWorld)
                .distinct()
                .collect(Collectors.toList());
    }

    // Getters

    public Map<String, Jump> getJumps() {
        return this.jumps;
    }

    public Map<Location, Jump> getJumpStarts() {
        return jumpStarts;
    }

    public List<Location> getProtectedLocations() {
        return protectedLocations;
    }

    public List<World> getProtectedWorlds() {
        return protectedWorlds;
    }

    public Optional<Jump> getJump(String name) {
        return Optional.ofNullable(this.jumps.get(name));
    }

    public File getFile(Jump jump) {
        return new File(this.jumpsFolder, jump.getName() + ".yml");
    }

    // Utilities

    public void persist(Jump jump) {
        this.jumps.put(jump.getName(), jump);
        File file = new File(this.jumpsFolder, jump.getName() + ".yml");

        try {
            if ( !file.exists() && file.createNewFile()) this.plugin.getLogger().info(String.format("Created %s", file.getName()));

            YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
            conf.setDefaults(this.defaults);
            conf.options().copyDefaults(true);
            conf.set("jump", jump);
            conf.save(file);
        } catch (IOException exception) {
            this.plugin.getLogger().severe(String.format("Unable to save %s: %s", file.getName(), exception.getMessage()));
        }

        if (!this.plugin.isDisabling()) this.updateJumpList();
    }

    public void updateName(Jump jump, String name) {
        File file = new File(this.jumpsFolder, jump.getName() + ".yml");
        if (!file.delete()) this.plugin.getLogger().severe(() -> "Unable to delete " + file.getName());
        this.jumps.remove(jump.getName(), jump);

        jump.setName(name);
        this.jumps.put(name, jump);
        this.persist(jump);
    }

    public void delete(Jump jump) {
        this.jumps.remove(jump.getName());
        this.updateJumpList();
        File file = this.getFile(jump);
        if (!file.delete()) this.plugin.getLogger().severe(() -> "Unable to delete " + file.getName());
    }

    public void save() {
        this.jumps.values().forEach(this::persist);
    }
}
