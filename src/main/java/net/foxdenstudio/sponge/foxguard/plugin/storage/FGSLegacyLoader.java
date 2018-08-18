package net.foxdenstudio.sponge.foxguard.plugin.storage;

import net.foxdenstudio.sponge.foxguard.plugin.FGManager;
import net.foxdenstudio.sponge.foxguard.plugin.config.FGConfigManager;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by fox on 12/6/17.
 */
public class FGSLegacyLoader {

    public static String LOGGER_NAME = FGStorageManagerNew.LOGGER_NAME + ".legacy";

    private static final int VERSION = 1;

    private static FGSLegacyLoader instance = new FGSLegacyLoader();
    private final Logger logger = LoggerFactory.getLogger(LOGGER_NAME);

    public static FGSLegacyLoader getInstance() {
        return instance;
    }

    private FGSLegacyLoader() {
    }

    public Optional<FGStorageManagerNew.IndexConfig> getLegacyIndex(Path file) {
        if (Files.notExists(file) || Files.isDirectory(file)) return Optional.empty();

        logger.info("Loading legacy index: " + file);

        try (DB mainDB = openFoxDB(file)) {
            List<FGSObjectIndex> list = new ArrayList<>();

            Map<String, String> mainMap = mainDB.hashMap("main", Serializer.STRING, Serializer.STRING).createOrOpen();
            Map<String, String> typeMap = mainDB.hashMap("types", Serializer.STRING, Serializer.STRING).createOrOpen();
            Map<String, Boolean> enabledMap = mainDB.hashMap("enabled", Serializer.STRING, Serializer.BOOLEAN).createOrOpen();
            Map<String, Integer> priorityMap = mainDB.exists("priority") ?
                    mainDB.hashMap("priority", Serializer.STRING, Serializer.INTEGER).createOrOpen() :
                    null;
            Map<String, String> linksMap = mainDB.exists("links") ?
                    mainDB.hashMap("links", Serializer.STRING, Serializer.STRING).createOrOpen() :
                    null;

            mainMap.forEach((name, category) -> {
                if (!typeMap.containsKey(name)) return;
                String type = typeMap.get(name);
                Boolean enabled = enabledMap.getOrDefault(name, true);
                Integer priority = priorityMap != null ? priorityMap.getOrDefault(name, 0) : null;
                List<FGSObjectPath> links = null;
                if (linksMap != null) {
                    String handlerString = linksMap.getOrDefault(name, "");
                    if (!handlerString.isEmpty()) {
                        links = new ArrayList<>();
                        String[] parts = handlerString.split(",");
                        for (String part : parts) {
                            if (!part.isEmpty()) {
                                links.add(new FGSObjectPath(part, FGManager.SERVER_OWNER));
                            }
                        }
                    }
                }
                FGSObjectIndex index = new FGSObjectIndex(name, null, category, type, enabled, priority, links);
                list.add(index);
            });
            if (list.isEmpty()) return Optional.empty();
            else return Optional.of(new FGStorageManagerNew.IndexConfig(list, VERSION));
        }
    }

    public String getIndexDBName(String catFileName) {
        return catFileName + ".foxdb";
    }

    @SuppressWarnings("Duplicates")
    public static DB openFoxDB(Path path) {
        FGConfigManager c = FGConfigManager.getInstance();
        DBMaker.Maker maker = DBMaker.fileDB(path.normalize().toFile());
        if (!c.lockDatabaseFiles()) maker.fileLockDisable();
        if (c.useMMappedFiles()) maker.fileMmapEnableIfSupported();
        if (c.gcCleanerHack()) maker.cleanerHackEnable();
        return maker.make();
    }
}
