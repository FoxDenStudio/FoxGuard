/*
 * This file is part of FoxGuard, licensed under the MIT License (MIT).
 *
 * Copyright (c) gravityfox - https://gravityfox.net/
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.foxdenstudio.foxsuite.foxguard.sponge.pluginold.config;

import net.foxdenstudio.foxsuite.foxguard.sponge.pluginold.FoxGuardMain;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

public final class FGConfigManager {

    private static FGConfigManager instance;

    private boolean cleanupFiles = true;
    private boolean saveInWorldFolder = true;
    private boolean saveWorldRegionsInWorldFolders = true;
    private boolean useConfigFolder = false;
    private int nameLengthLimit = 24;

    private Map<ListenerModule, String> modules = new EnumMap<>(ListenerModule.class);

    private FGConfigManager() {
    }

    public static FGConfigManager getInstance() {
        if (instance == null) {
            instance = new FGConfigManager();
            instance.load();
        }
        return instance;
    }

    public void save() {
        Path configFile =
                FoxGuardMain.instance().getConfigDirectory().resolve("foxguard.cfg");
        CommentedConfigurationNode root;
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(configFile).build();
        if (Files.exists(configFile)) {
            try {
                root = loader.load();
            } catch (IOException e) {
                root = loader.createEmptyNode(ConfigurationOptions.defaults());
            }
        } else {
            root = loader.createEmptyNode(ConfigurationOptions.defaults());
        }
        //--------------------------------------------------------------------------------------------------------------

        root.getNode("storage", "cleanupFiles").setComment("Sets whether to aggressively delete files that are no longer used. Default: true\n" +
                "This is meant to keep the file store clean and free of clutter. It also improves load times.\n" +
                "The caveat is that objects that fail to load are deleted without warning. This normally isn't an issue, even in server crashes.\n" +
                "However, modifying databases and moving the files around can trigger the cleanup.\n" +
                "If force loading is off or the plugin simply fails to load the database, it would just be discarded.\n" +
                "Setting this option to false will prevent databases from being deleted.\n" +
                "However, they will still be overwritten if a new database is made with the same name.")
                .setValue(cleanupFiles);

        root.getNode("storage", "saveInWorldFolder").setComment("Whether or not FoxGuard should save object information in the world folder.\n" +
                "This includes super-regions, handlers, and controllers, but does not include world-regions.")
                .setValue(saveInWorldFolder);

        root.getNode("storage", "saveWorldRegionsInWorldFolders").setComment("Whether or not FoxGuard should save world-region information in the world folder.\n" +
                "In this case, the files are kept with their corresponding world/dimension.\n" +
                "This makes it easier to copy and paste world data without causing de-synchronization between the world data and FoxGuard data.")
                .setValue(saveWorldRegionsInWorldFolders);
        root.getNode("storage", "useConfigFolder").setComment("Whether or not to place the foxguard folder inside the config folder.\n" +
                "Only applies if files are not kept inside the world folder.")
                .setValue(useConfigFolder);
        root.getNode("general", "nameLengthLimit").setComment("The length limit for object names. Use 0 or lower for no limit.\n" +
                "Extremely long names can cause a variety of unfixable issues. You have been warned.")
                .setValue(nameLengthLimit);

        for (ListenerModule m : ListenerModule.values()) {
            CommentedConfigurationNode node = root.getNode("module", m.getName()).setValue(this.modules.get(m));
            String comment = m.getComment();
            if(comment != null) node.setComment(comment);
        }


        //--------------------------------------------------------------------------------------------------------------
        try {
            loader.save(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        Path configFile =
                FoxGuardMain.instance().getConfigDirectory().resolve("foxguard.cfg");
        CommentedConfigurationNode root;
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(configFile).build();
        if (Files.exists(configFile)) {
            try {
                root = loader.load();
            } catch (IOException e) {
                root = loader.createEmptyNode(ConfigurationOptions.defaults());
            }
        } else {
            root = loader.createEmptyNode(ConfigurationOptions.defaults());
        }
        //--------------------------------------------------------------------------------------------------------------

        cleanupFiles = root.getNode("storage", "cleanupFiles").getBoolean(true);
        saveInWorldFolder = root.getNode("storage", "saveInWorldFolder").getBoolean(true);
        saveWorldRegionsInWorldFolders = root.getNode("storage", "saveWorldRegionsInWorldFolders").getBoolean(true);
        useConfigFolder = root.getNode("storage", "useConfigFolder").getBoolean(false);
        nameLengthLimit = root.getNode("general", "nameLengthLimit").getInt(24);
        for (ListenerModule m : ListenerModule.values()) {
            this.modules.put(m, root.getNode("module", m.getName()).getString(m.getDefaultValue()));
        }

        //--------------------------------------------------------------------------------------------------------------
    }


    public boolean cleanupFiles() {
        return cleanupFiles;
    }

    public boolean saveInWorldFolder() {
        return saveInWorldFolder;
    }

    public boolean saveWorldRegionsInWorldFolders() {
        return saveWorldRegionsInWorldFolders;
    }

    public boolean useConfigFolder() {
        return useConfigFolder;
    }

    public int getNameLengthLimit() {
        return nameLengthLimit;
    }

    public Map<ListenerModule, String> getModules() {
        return this.modules;
    }

    public void setupModule(ListenerModule module){
        module.setup(this.modules.get(module));
    }


}
