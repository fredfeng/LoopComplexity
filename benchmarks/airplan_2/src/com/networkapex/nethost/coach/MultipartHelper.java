package com.networkapex.nethost.coach;

import com.sun.net.httpserver.HttpExchange;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides ability to parse multipart/form-data content from a POST.
 */
public class MultipartHelper {

    // allowing urls longer than this causes a problem in the MultipartHelper class
    // If you ask the FileUpload class to get the parameter map out of a url bigger than 10K,
    // it will create a temporary file on disk. It is possible to trigger a vulnerability by posting
    // too much information
    private static final int MAX_POST_LENGTH = 32 * 1024;

    /**
     * Parses the multipart content of the <code>HttpExchange</code> and returns
     * the content associated with the specified field name as a String. If the
     * exchange is not multipart or is missing properties required for parsing,
     * a <code>RuntimeException</code> is raised. If no field matches,
     * <code>null</code> is returned.
     *
     * @param httpExchange holding the multipart request to be parsed
     * @param fieldName    of the form to grab the content
     * @return String representing the content of multipart field associated
     * with the specified field name; may be <code>null</code> if there
     * is no matching field
     * @throws IllegalArgumentException if the exchange is not a POST, is missing necessary
     *                                  properties, or has trouble being parsed
     */
    public static String grabMultipartFieldContent(HttpExchange httpExchange, String fieldName) {
        if (httpExchange == null) {
            return grabMultipartFieldContentHelp();
        }

        if (StringUtils.isBlank(fieldName)) {
            throw new IllegalArgumentException("Field name may not be blank or null");
        }

        HttpExchangeRequestContext context = new HttpExchangeRequestContext(httpExchange);

        String result = null;

        try {
            FileUpload fileUpload = new FileUpload();
            FileItemIterator iterator = fileUpload.getItemIterator(context);

            while (iterator.hasNext()) {
                FileItemStream fileItemStream = iterator.next();
                String name = fileItemStream.getFieldName();

                if (name.equals(fieldName)) {
                    result = IOUtils.toString(fileItemStream.openStream(), "UTF-8");
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing multipart message: " + e.getMessage(), e);
        }

        return result;
    }

    private static String grabMultipartFieldContentHelp() {
        throw new IllegalArgumentException("HttpExchange may not be null");
    }


    /**
     * Get the parameters from the multi part field. If there are duplicates, the last one wins.
     * @param httpExchange
     * @return
     */
    public static Map<String, String> getMultipartFieldContent(HttpExchange httpExchange) {
        if (httpExchange == null) {
            throw new IllegalArgumentException("HttpExchange may not be null");
        }

        HttpExchangeRequestContext context = new HttpExchangeRequestContext(httpExchange);

        String result = null;

        Map<String, String> postFields = new HashMap<>();

        try {
            FileUpload fileUpload = new FileUpload();
            FileItemIterator iterator = fileUpload.getItemIterator(context);

            while (iterator.hasNext()) {
                FileItemStream fileItemStream = iterator.next();
                String name = fileItemStream.getFieldName();
                String value = IOUtils.toString(fileItemStream.openStream(), "UTF-8");
                postFields.put(name, value);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing multipart message: " + e.getMessage(), e);
        }

        return postFields;
    }

    public static Map<String, List<String>> fetchMultipartFieldContentDuplicates(HttpExchange httpExchange) {
        if (httpExchange == null) {
            throw new IllegalArgumentException("HttpExchange may not be null");
        }

        HttpExchangeRequestContext context = new HttpExchangeRequestContext(httpExchange);

        try {
            FileUpload fileUpload = new FileUpload();
            FileItemFactory fileItemFactory = new DiskFileItemFactory();
            fileUpload.setFileItemFactory(fileItemFactory);

            Map<String, List<FileItem>> fieldItems = fileUpload.parseParameterMap(context);
          //  System.out.println("field items : " + Arrays.toString(fieldItems.keySet().toArray()));
            // TODO: this may cause an unintentional vulnerability
             return extractFieldsFromParameterMap(fieldItems, fieldItems.keySet());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing multipart message: " + e.getMessage(), e);
        }


    }

    /**
     * Parses the multipart content of the HttpExchange and returns all
     * the information associated with the specified field names.
     *
     * @param httpExchange holding POST data
     * @param fieldNames   of interest to the method caller
     * @return Map of the field names to a List of their values;
     * may be empty but guaranteed to not be <code>null</code>
     * @throws IllegalArgumentException if either argument is <code>null</code> or
     *                                  there is trouble parsing the POST content
     */
    public static Map<String, List<String>> getMultipartValues(HttpExchange httpExchange,
                                                               Set<String> fieldNames) {
        if (httpExchange == null) {
            return fetchMultipartValuesAssist();
        }

        if (fieldNames == null) {
            throw new IllegalArgumentException("Field Names may not be null");
        }

        HttpExchangeRequestContext context = new HttpExchangeRequestContext(httpExchange);

        FileUpload fileUpload = new FileUpload();
        FileItemFactory fileItemFactory = new DiskFileItemFactory();
        fileUpload.setFileItemFactory(fileItemFactory);

        Map<String, List<String>> fieldNameValues = new HashMap<>();

        try {
            // create map of all given field names and their associated item as a string
            Map<String, List<FileItem>> parameterMap = fileUpload.parseParameterMap(context);

            for (String fieldName : fieldNames) {
                List<FileItem> items = parameterMap.get(fieldName);

                if ((items != null) && !items.isEmpty()) {
                    List<String> values = fieldNameValues.get(fieldName);

                    if (values == null) {
                        values = new ArrayList<>();
                        fieldNameValues.put(fieldName, values);
                    }

                    for (int a = 0; a < items.size(); a++) {
                        fetchMultipartValuesManager(items, values, a);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing multipart message: " + e.getMessage(), e);
        }

        return fieldNameValues;
    }

    private static void fetchMultipartValuesManager(List<FileItem> items, List<String> values, int c) {
        FileItem item = items.get(c);
        values.add(item.getString());
    }

    private static Map<String, List<String>> fetchMultipartValuesAssist() {
        throw new IllegalArgumentException("HttpExchange may not be null");
    }

    private static Map<String, List<String>> extractFieldsFromParameterMap(Map<String, List<FileItem>> parameterMap,
                                                                           Set<String> fields) {
        Map<String, List<String>> fieldNameValues = new HashMap<>();

        for (String fieldName : fields) {
            List<FileItem> items = parameterMap.get(fieldName);

            if ((items != null) && !items.isEmpty()) {
                List<String> values = fieldNameValues.get(fieldName);

                if (values == null) {
                    values = new ArrayList<>();
                    fieldNameValues.put(fieldName, values);
                }

                for (int k = 0; k < items.size(); k++) {
                    extractFieldsFromParameterMapAdviser(items, values, k);
                }
            }
        }
        return fieldNameValues;

    }

    private static void extractFieldsFromParameterMapAdviser(List<FileItem> items, List<String> values, int a) {
        FileItem item = items.get(a);
        values.add(item.getString());
    }

    /**
     * Parses the multipart content of the HttpExchange. Copies the file image
     * to the image destination directory given and returns all the information
     * necessary to create a new photo using the image.
     *
     * @param httpExchange   holding POST data
     * @param allFieldNames  of the form to grab the content necessary to create the photo;
     *                       should include the image field name
     * @param fileFieldName used to distinguish the image from other content
     * @param fileDestDir   path to local destination directory
     * @return Map of the field names to their file item as a string
     * @throws IllegalArgumentException if the exchange does not
     *                                  contain an image or has trouble being parsed
     */

    public static Map<String, String> takeMultipartFile(HttpExchange httpExchange,
                                                        Set<String> allFieldNames,
                                                        String fileFieldName,
                                                        Path fileDestDir,
                                                        String fileName) {
        if (httpExchange == null) {
            return takeMultipartFileService();
        }

        if (allFieldNames == null) {
            return obtainMultipartFileHelper();
        }

        if (StringUtils.isBlank(fileFieldName)) {
            return pullMultipartFileTarget();
        }

        if (fileDestDir == null) {
            throw new IllegalArgumentException("File Destination Directory may not be null");
        }

        HttpExchangeRequestContext context = new HttpExchangeRequestContext(httpExchange);

        FileUpload fileUpload = new FileUpload();
        FileItemFactory fileItemFactory = new DiskFileItemFactory();
        fileUpload.setFileItemFactory(fileItemFactory);

        Map<String, String> fieldNameItems = new HashMap<>();
        InputStream fileIn = null;

        // Make sure fileFieldName is part of allFieldNames
        if (!allFieldNames.contains(fileFieldName)) {
            Set<String> newFieldNames = new HashSet<>(allFieldNames);
            newFieldNames.add(fileFieldName);
            allFieldNames = newFieldNames;
        }

        try {
            // create map of all given field names and their associated item as a string
            Map<String, List<FileItem>> parameterMap = fileUpload.parseParameterMap(context);

            for (String fieldName : allFieldNames) {
                List<FileItem> items = parameterMap.get(fieldName);
                if (items != null) {
                    if (items.size() == 1) { // there should only be one FileItem per fieldName
                        // if we have the file field name, we need to capture
                        // the input stream containing the file and the file name
                        FileItem item = items.get(0);
                        String fileItem;
                        if (fieldName.equals(fileFieldName)) {
                            fileIn = item.getInputStream();
                            fileItem = item.getName();
                        } else {
                            fileItem = item.getString();
                        }
                        fieldNameItems.put(fieldName, fileItem);
                        fieldNameItems.put("MIME", item.getContentType());
                    } else {
                        return grabMultipartFileFunction();
                    }
                }
            }

            if ((fileIn == null) || StringUtils.isBlank(fieldNameItems.get(fileFieldName))) {
                throw new IllegalArgumentException("Missing required POST file file associated with field name " + fileFieldName);
            }

            File newfile = fileDestDir.toFile();

            if (!newfile.exists()) {
                obtainMultipartFileGuide(newfile);
            }
            Path trail;
            if (fileName != null) {
                trail = Paths.get(fileDestDir.toString(), fileName);
            } else {
                trail = Paths.get(fileDestDir.toString(), fieldNameItems.get(fileFieldName));
            }
            Files.copy(fileIn, trail, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing multipart message: " + e.getMessage(), e);
        }

        return fieldNameItems;
    }

    private static void obtainMultipartFileGuide(File newfile) {
        newfile.mkdirs();
    }

    private static Map<String, String> grabMultipartFileFunction() {
        throw new IllegalArgumentException("Cannot handle more than one File Item for each Field Name");
    }

    private static Map<String, String> pullMultipartFileTarget() {
        throw new IllegalArgumentException("File Field Name many not be empty or null");
    }

    private static Map<String, String> obtainMultipartFileHelper() {
        throw new IllegalArgumentException("Field Names may not be null");
    }

    private static Map<String, String> takeMultipartFileService() {
        throw new IllegalArgumentException("HttpExchange may not be null");
    }

    /**
     * Parses the multipart content of the HttpExchange and returns a List of
     * the file items, as Strings, associated with the specified field name. If
     * there are no file items associated with the field name, or the field name
     * is not found, an empty list is returned.
     *
     * @param httpExchange holding the multipart request to be parsed
     * @param fieldName    of the form to grab the content
     * @return List of Strings representing the content of the multipart field
     * associated with the specified field name; may be empty
     * @throws IllegalArgumentException if the exchange is not a POST, is missing necessary
     *                                  properties, or has trouble being parsed
     */
    public static List<String> getMultipartFieldItems(HttpExchange httpExchange, String fieldName) {
        if (httpExchange == null) {
            return obtainMultipartFieldItemsManager();
        }

        if (StringUtils.isBlank(fieldName)) {
            throw new IllegalArgumentException("Field name may not be blank or null");
        }

        HttpExchangeRequestContext context = new HttpExchangeRequestContext(httpExchange);

        FileUpload fileUpload = new FileUpload();
        FileItemFactory fileItemFactory = new DiskFileItemFactory();
        fileUpload.setFileItemFactory(fileItemFactory);
        List<String> itemStrings = new ArrayList<>();

        try {
            // get items associated with the field name
            List<FileItem> items = fileUpload.parseParameterMap(context).get(fieldName);

            if (items != null && !items.isEmpty()) {
                for (int j = 0; j < items.size(); j++) {
                    FileItem item = items.get(j);
                    itemStrings.add(item.getString());
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing multipart message: " + e.getMessage(), e);
        }

        return itemStrings;
    }

    private static List<String> obtainMultipartFieldItemsManager() {
        throw new IllegalArgumentException("HttpExchange may not be null");
    }

    private static class HttpExchangeRequestContext implements RequestContext {
        private static final String CONTENT_TYPE = "Content-Type";
        private static final String MULTIPART_FORM_DATA = "multipart/form-data";
        private static final String BOUNDARY_EQUALS = "boundary=";
        private static final String CONTENT_LENGTH = "Content-Length";
        private static final String CONTENT_ENCODING = "Content-Encoding";

        private final String contentType;
        private final String contentEncoding;
        private final int contentLength;
        private final InputStream inputStream;

        public HttpExchangeRequestContext(HttpExchange httpExchange) {
            if (!"POST".equals(httpExchange.getRequestMethod())) {
                HttpExchangeRequestContextHome();
            }

            contentType = httpExchange.getRequestHeaders().getFirst(CONTENT_TYPE);

            if (contentType == null) {
                HttpExchangeRequestContextCoordinator();
            }

            if (!contentType.startsWith(MULTIPART_FORM_DATA)) {
                throw new IllegalArgumentException("Content type must be " + MULTIPART_FORM_DATA);
            }

            int index = contentType.indexOf(BOUNDARY_EQUALS, MULTIPART_FORM_DATA.length());

            if (index == -1) {
                throw new IllegalArgumentException("Content type must contain a boundary mapping");
            }

            String contentLengthHeader = httpExchange.getRequestHeaders().getFirst(CONTENT_LENGTH);

            if (contentLengthHeader == null) {
                HttpExchangeRequestContextService();
            }

            try {
                contentLength = Integer.parseInt(contentLengthHeader);
                if (contentLength > MAX_POST_LENGTH) {
                    throw new IllegalArgumentException("Content length is too long: " + contentLength + ". It must be smaller than " + MAX_POST_LENGTH + " characters");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(CONTENT_LENGTH + " must be a number: " + e.getMessage(), e);
            }

            contentEncoding = httpExchange.getRequestHeaders().getFirst(CONTENT_ENCODING);

            inputStream = httpExchange.getRequestBody();
        }

        private void HttpExchangeRequestContextService() {
            new HttpExchangeRequestContextManager().invoke();
        }

        private void HttpExchangeRequestContextCoordinator() {
            throw new IllegalArgumentException("The " + CONTENT_TYPE + " request header must exist");
        }

        private void HttpExchangeRequestContextHome() {
            throw new IllegalArgumentException("Only POST method is permitted");
        }

        @Override
        public String getCharacterEncoding() {
            return contentEncoding;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        @Deprecated
        public int getContentLength() {
            return contentLength;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return inputStream;
        }

        private class HttpExchangeRequestContextManager {
            public void invoke() {
                throw new IllegalArgumentException("The " + CONTENT_LENGTH + " request header must exist");
            }
        }
    }
}
