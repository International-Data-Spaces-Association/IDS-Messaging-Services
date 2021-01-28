package de.fraunhofer.ids.framework.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

/**
 * Utility Class for parsing Multipart Maps from String responses
 */
public class MultipartParser implements UploadContext {
    private String              postBody;
    private String              boundary;
    private Map<String, String> parameters = new HashMap<>();

    /**
     * Constructor for the MultipartStringParser used internally to parse a multipart response to a Map<Partname, MessagePart>
     *
     * @param postBody a multipart response body as string
     *
     * @throws FileUploadException if there are problems reading/parsing the postBody.
     */
    private MultipartParser( String postBody ) throws FileUploadException {
        this.postBody = postBody;
        this.boundary = postBody.substring(2, postBody.indexOf('\n')).trim();

        final FileItemFactory factory = new DiskFileItemFactory();
        FileUpload upload = new FileUpload(factory);
        List<FileItem> fileItems = upload.parseRequest(this);

        for( FileItem fileItem : fileItems ) {
            if( fileItem.isFormField() ) {
                //put the parameters into the map as "name, content"
                parameters.put(fileItem.getFieldName(), fileItem.getString());
            }
        }
    }

    /**
     * Convert a String from a multipart response to a Map with Partname/MessagePart
     *
     * @param postBody a multipart response body as string
     *
     * @return a Map from partname on content
     *
     * @throws FileUploadException if there are problems reading/parsing the postBody.
     */
    public static Map<String, String> stringToMultipart( String postBody ) throws FileUploadException {
        return new MultipartParser(postBody).getParameters();
    }

    /**
     * Getter for the parsed Map
     *
     * @return the parsed multipart Map
     */
    private Map<String, String> getParameters() {
        return parameters;
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
    public int getContentLength() { return -1; }

    @Override
    public InputStream getInputStream() { return new ByteArrayInputStream(postBody.getBytes()); }

}
