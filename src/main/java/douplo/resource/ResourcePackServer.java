package douplo.resource;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import douplo.item.ServerOnlyItem;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ResourcePackServer {

    public static final Logger LOGGER = LogManager.getLogger("ResourcePackServer");

    public static void initializeResourcePackServer(String address, int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(address, port), 0);
            server.createContext("/test", new MyHttpHandler());
            HttpContext context = server.createContext("/resourcepack", new ResourcePackHandler());
            server.setExecutor(null);
            server.start();

        } catch (IOException e) {
            LOGGER.error(e);
        }

    }

    private static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    private static long appendFileToArchive(ZipOutputStream arch, File file) {

        try {
            arch.putNextEntry(new ZipEntry(file.getName()));
            InputStream stream = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(stream);
            long fileSize = Files.size(file.toPath());
            char[] data = new char[(int) fileSize];
            reader.read(data, 0, (int) fileSize);
            arch.write(toBytes(data), 0, (int) fileSize);
            arch.closeEntry();
            return fileSize;
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error(e);
            e.printStackTrace();
        }

        return 0;

    }

    private static void appendFileToArchive(ZipOutputStream arch, String filename, InputStream stream) {
        try {
            arch.putNextEntry(new ZipEntry(filename));
            InputStreamReader reader = new InputStreamReader(stream);

            int byteRead = -1;
            while ((byteRead = stream.read()) != -1) {
                arch.write(byteRead);
            }

            arch.closeEntry();
        } catch (FileNotFoundException e) {
            LOGGER.error(e);
            e.printStackTrace();
        } catch (IOException e) {
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    private static void appendJsonToArchive(ZipOutputStream arch, String filename, JsonObject json) {
        try {
            arch.putNextEntry(new ZipEntry(filename));
            arch.write(json.toString().getBytes(StandardCharsets.UTF_8));
            arch.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String modelIdToPath(Identifier modelId) {
        return "assets/" + modelId.getNamespace() + "/models/" + modelId.getPath() + ".json";
    }

    private static String identifierToPath(Identifier id) {
        return "assets/" + id.getNamespace() + "/" + id.getPath();
    }

    private static void writeItemModelsToArchive(ZipOutputStream arch) {

        Map<Identifier, JsonObject> models = ServerOnlyItem.generateModelOverrides();
        for (Map.Entry<Identifier, JsonObject> model : models.entrySet()) {

            appendJsonToArchive(arch, modelIdToPath(model.getKey()), model.getValue());

        }

    }

    private static byte[] cachedPackData;
    private static String cachedPackHash;

    public static String getPackHash() {
        return new String(cachedPackHash);
    }

    public static String getPackAddress() {
        return "http://localhost:8000/resourcepack/" + getPackHash() + ".zip";
    }

    public static void createResourcePack(ResourceManager manager) {
        LOGGER.info("Generating resource pack");
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(os);

        try {
            out.putNextEntry(new ZipEntry("pack.mcmeta"));
            String packDesc = "{\n" +
                    "\"pack\": {\n" +
                    "   \"pack_format\": 8,\n" +
                    "   \"description\": \"Auto-generated resource pack\"\n" +
                    " }\n" +
                    "}";
            out.write(packDesc.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }

        writeItemModelsToArchive(out);
        writeItemResourcesToArchive(out, manager);

        try {
            out.close();
        } catch (IOException e) {
            LOGGER.error(e);
            e.printStackTrace();
        }
        cachedPackData = os.toByteArray();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(cachedPackData);
            StringBuilder builder = new StringBuilder();
            for (byte b : digest.digest()) {
                builder.append(String.format("%02x", b));
            }
            cachedPackHash = builder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        LOGGER.info("Generated resource pack. SHA-1: " + new String(cachedPackHash));

    }

    private static void writeItemResourcesToArchive(ZipOutputStream out, ResourceManager manager) {

        List<ServerOnlyItem.ResourceIdentifier> resources = ServerOnlyItem.getExtraResources();
        for (ServerOnlyItem.ResourceIdentifier res : resources) {

            LOGGER.info("Writing resource " + res.getFilePath());

            Identifier serverPath = res.getFilePath();
            Identifier clientPath = res.getDestinationPath();

            try {
                InputStream stream = manager.getResource(serverPath).getInputStream();
                appendFileToArchive(out, identifierToPath(clientPath), stream);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    static class ResourcePackHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            LOGGER.info("Getting resource pack " + cachedPackData.length);

            OutputStream out = httpExchange.getResponseBody();
            httpExchange.sendResponseHeaders(200, cachedPackData.length);
            out.write(cachedPackData, 0, cachedPackData.length);
            out.close();
            httpExchange.close();
        }

    }

    static class MyHttpHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String response = "Hello World";
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }

}
