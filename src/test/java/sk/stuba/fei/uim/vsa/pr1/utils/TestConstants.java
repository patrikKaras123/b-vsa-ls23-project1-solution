package sk.stuba.fei.uim.vsa.pr1.utils;

import java.io.*;
import java.util.Map;
import java.util.function.Function;

public class TestConstants {

//    public static final String DB = getEnvOrDefault("VSA_DB", "vsa_pr1");
    public static final String DB = getValueFromPersistenceProperty("javax.persistence.jdbc.url", v -> v.substring(v.lastIndexOf('/') + 1));
    //    public static final String USERNAME = getEnvOrDefault("VSA_DB_USERNAME", "vsa");
    public static final String USERNAME = getValueFromPersistenceProperty("javax.persistence.jdbc.user", null);
    //    public static final String PASSWORD = getEnvOrDefault("VSA_DB_PASSWORD", "vsa");
    public static final String PASSWORD = getValueFromPersistenceProperty("javax.persistence.jdbc.password", null);
    //public static final String[] ID_FIELDS = getEnvOrDefault("VSA_ID_FIELDS", "id,aisId,ais,aisid,ID").split(",");

    public static final Map<String, String> ENV = System.getenv();

    public static String getEnvOrDefault(String key, String defaultValue) {
        return System.getenv(key) == null ? defaultValue : System.getenv(key);
    }

    private static String getValueFromPersistenceProperty(String property, Function<String, String> convertor) {
        try {
            File persistenceFile = new File(String.join(File.separator, "src", "main", "resources", "META-INF", "persistence.xml"));
            if (!persistenceFile.exists() || !persistenceFile.isFile()) return "";
            String content = readFromInputStream(new FileInputStream(persistenceFile));
            int idx = content.indexOf(property);
            if (idx == -1) return "";
            String valueConst = "value=\"";
            int valueIndex = content.indexOf(valueConst, idx + property.length());
            if (valueIndex == -1) return "";
            int endIndex = content.indexOf("\"", valueIndex + valueConst.length());
            if (endIndex == -1) return "";
            String value = content.substring(valueIndex + valueConst.length(), endIndex);
            value = value.replace("\"", "").trim();
            if (convertor != null) {
                value = convertor.apply(value);
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

}
