package net.modconfig;

import java.io.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import net.CoroUtil.forge.CULog;
import net.CoroUtil.OldUtil;
import net.modconfig.ConfigMod;

public class ModConfigData {
    public String configID;
    public Class configClass;
    public IConfigCategory configInstance;

    public HashMap<String, String> valsString = new HashMap<>();
    public HashMap<String, Integer> valsInteger = new HashMap<>();
    public HashMap<String, Double> valsDouble = new HashMap<>();
    public HashMap<String, Boolean> valsBoolean = new HashMap<>();


    public List<ConfigEntryInfo> configData = new ArrayList<>();

    public File saveFilePath;

    public ModConfigData(File savePath, String parStr, Class parClass, IConfigCategory parConfig) {
        configID = parStr;
        configClass = parClass;
        configInstance = parConfig;
        saveFilePath = savePath;
    }

    public void updateHashMaps() {
        Field[] fields = configClass.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String name = field.getName();
            processField(name);
        }
    }

    public void initData() {
        valsString.clear();
        valsInteger.clear();
        valsDouble.clear();
        valsBoolean.clear();

        updateHashMaps();
    }

    public boolean updateField(String name, Object obj) {
        if (setFieldBasedOnType(name, obj)) {
            writeConfigFile(true);
            return true;
        }
        return false;
    }

    public boolean setFieldBasedOnType(String name, Object obj) {
        try {
            if (valsString.containsKey(name)) {
                OldUtil.setPrivateValue(configClass, configInstance, name, (String)obj);
                inputField(name, (String)obj);
            } else if (valsInteger.containsKey(name)) {
                OldUtil.setPrivateValue(configClass, configInstance, name, Integer.valueOf(obj.toString()));
                inputField(name, Integer.valueOf(obj.toString()));
            } else if (valsDouble.containsKey(name)) {
                OldUtil.setPrivateValue(configClass, configInstance, name, Double.valueOf(obj.toString()));
                inputField(name, Double.valueOf(obj.toString()));
            } else if (valsBoolean.containsKey(name)) {
                OldUtil.setPrivateValue(configClass, configInstance, name, Boolean.valueOf(obj.toString()));
                inputField(name, Boolean.valueOf(obj.toString()));
            } else {
                return false;
            }

            configInstance.hookUpdatedValues();

            return true;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void processField(String fieldName) {
        try {
            Object obj = ConfigMod.getField(configID, fieldName);
            if (obj instanceof String) {
                valsString.put(fieldName, (String)obj);
            } else if (obj instanceof Integer) {
                valsInteger.put(fieldName, (Integer)obj);
            } else if (obj instanceof Double) {
                valsDouble.put(fieldName, (Double)obj);
            } else if (obj instanceof Boolean) {
                valsBoolean.put(fieldName, (Boolean)obj);
            } else {

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void inputField(String fieldName, Object obj) {
        if (obj instanceof String) {
            valsString.put(fieldName, (String)obj);
        } else if (obj instanceof Integer) {
            valsInteger.put(fieldName, (Integer)obj);
        } else if (obj instanceof Double) {
            valsDouble.put(fieldName, (Double)obj);
        } else if (obj instanceof Boolean) {
            valsBoolean.put(fieldName, (Boolean)obj);
        }
    }

    public void writeConfigFile(boolean resetConfig) {
        if (resetConfig && saveFilePath.exists()) {
            saveFilePath.delete();
        }


        if (!saveFilePath.getParentFile().exists()) {
            saveFilePath.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFilePath))) {
            writer.write("# Configuration file for " + configID);
            writer.newLine();
            writer.newLine();

            Field[] fields = configClass.getDeclaredFields();

            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                String name = field.getName();


                String comment = null;
                ConfigComment anno_comment = field.getAnnotation(ConfigComment.class);
                if (anno_comment != null) {
                    comment = anno_comment.value()[0];
                }


                if (comment != null && !comment.isEmpty()) {
                    writer.write("# " + comment);
                    writer.newLine();
                }


                Object obj = ConfigMod.getField(configID, name);


                if (obj != null) {
                    String type = "";
                    if (obj instanceof String) {
                        type = "S";
                    } else if (obj instanceof Integer) {
                        type = "I";
                    } else if (obj instanceof Double) {
                        type = "D";
                    } else if (obj instanceof Boolean) {
                        type = "B";
                    }

                    writer.write(type + ":" + name + "=" + obj.toString());
                    writer.newLine();
                }

                writer.newLine();
            }

            CULog.dbg("writeConfigFile invoked for " + this.configID + ", resetConfig: " + resetConfig);

        } catch (IOException e) {
            CULog.err("Failed to write config file: " + saveFilePath);
            e.printStackTrace();
        }


        if (!resetConfig) {
            readConfigFile();
        }
    }

    public void readConfigFile() {
        if (!saveFilePath.exists()) {
            CULog.dbg("Config file doesn't exist, creating default: " + saveFilePath);
            writeConfigFile(true);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(saveFilePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();


                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }


                if (line.contains(":") && line.contains("=")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length == 2) {
                        String type = parts[0];
                        String[] nameValue = parts[1].split("=", 2);

                        if (nameValue.length == 2) {
                            String name = nameValue[0];
                            String value = nameValue[1];


                            try {
                                switch (type) {
                                    case "S":
                                        setFieldBasedOnType(name, value);
                                        break;
                                    case "I":
                                        setFieldBasedOnType(name, Integer.parseInt(value));
                                        break;
                                    case "D":
                                        setFieldBasedOnType(name, Double.parseDouble(value));
                                        break;
                                    case "B":
                                        setFieldBasedOnType(name, Boolean.parseBoolean(value));
                                        break;
                                }
                            } catch (NumberFormatException e) {
                                CULog.err("Failed to parse config value: " + line);
                            }
                        }
                    }
                }
            }

            CULog.dbg("Config file loaded: " + saveFilePath);

        } catch (IOException e) {
            CULog.err("Failed to read config file: " + saveFilePath);
            e.printStackTrace();
        }
    }
}