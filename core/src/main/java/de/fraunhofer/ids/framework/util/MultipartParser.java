package de.fraunhofer.ids.framework.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.UploadContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

/**
 * Utility Class for parsing Multipart Maps from String responses.
 */
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class MultipartParser implements UploadContext {
    String postBody;
    String boundary;

    @Getter
    Map<String, String> parameters = new ConcurrentHashMap<>();

    /**
     * Constructor for the MultipartStringParser used internally to parse a multipart response to a Map<Partname, MessagePart>.
     *
     * @param postBody a multipart response body as string
     * @throws FileUploadException if there are problems reading/parsing the postBody.
     */
    private MultipartParser(final String postBody) throws FileUploadException, MultipartParseException {
        this.postBody = postBody;

        if (postBody.length() <= 2 || postBody.indexOf('\n') <= 2)  {
            throw new MultipartParseException("String could not be parsed, could not find a boundary!");
        }

        this.boundary = postBody.substring(2, postBody.indexOf('\n')).trim();

        final var upload = new FileUpload(new DiskFileItemFactory());
        final var fileItems = upload.parseRequest(this);

        for (final var fileItem : fileItems) {
            if (fileItem.isFormField()) {
                //put the parameters into the map as "name, content"
                parameters.put(fileItem.getFieldName(), fileItem.getString());
            }
        }
    }

    /**
     * Convert a String from a multipart response to a Map with Partname/MessagePart.
     *
     * @param postBody a multipart response body as string
     * @return a Map from partname on content
     * @throws MultipartParseException if there are problems reading/parsing the postBody.
     */
    public static Map<String, String> stringToMultipart(final String postBody) throws MultipartParseException {
        try {
            final var resultMap = new MultipartParser(postBody).getParameters();

            if (resultMap.keySet().isEmpty()) {
                throw new MultipartParseException("Could not parse Multipart! No parts found!");
            }

            return resultMap;
        } catch (FileUploadException e) {
            throw new MultipartParseException("Could not parse given String:\n" + postBody, e);
        }
    }

    //these methods must be implemented because of the UploadContext interface
    @Override
    public long contentLength() {
        return postBody.length();
    }

    @Override
    public String getCharacterEncoding() {
        return "Cp1252";
    }

    @Override
    public String getContentType() {
        return "multipart/form-data, boundary=" + this.boundary;
    }

    @Override
    public int getContentLength() {
        return -1;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(postBody.getBytes());
    }
}
