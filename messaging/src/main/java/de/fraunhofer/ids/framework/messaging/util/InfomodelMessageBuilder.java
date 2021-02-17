package de.fraunhofer.ids.framework.messaging.util;

import java.io.File;
import java.io.IOException;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.util.MultipartDatapart;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


/**
 * This Builder is a utility class for building OkHTTP
 * Multipart RequestBodies with RequestMessage header and String or File payload Part
 */
public class InfomodelMessageBuilder {
    private static final Serializer serializer = new Serializer();

    private final MultipartBody.Builder builder;

    /**
     * Build a MultipartMessage with {@link Message} header and {@link File} payload.
     *
     * @param header   the header Part of the MultipartMessage (an implementation of {@link Message})
     * @param payload  the File that is added to the MultipartMessages payload
     * @param fileType the MediaType of the file
     *
     * @return the built Message as OkHttp MultipartBody
     *
     * @throws IOException if the given header cannot be serialized by the given serializer
     */
    public static MultipartBody messageWithFile( Message header, File payload, MediaType fileType ) throws IOException {
        var imb = new InfomodelMessageBuilder(header);
        imb.addPayload(payload, fileType);
        return imb.getRequestBody();
    }

    /**
     * Build a MultipartMessage with {@link Message} header and String payload.
     *
     * @param header  the header Part of the MultipartMessage (an implementation of {@link Message})
     * @param payload the (String) payload that is added to the MultipartMessages Payload
     *
     * @return the built Message as OkHttp MultipartBody
     *
     * @throws IOException if the given header cannot be serialized by the given serializer
     */
    public static MultipartBody messageWithString( Message header, String payload ) throws IOException {
        var imb = new InfomodelMessageBuilder(header);
        imb.addPayload(payload);
        return imb.getRequestBody();
    }

    /**
     * Internal builder used by the static methods
     *
     * @param header the header Part of the MultipartMessage (an implementation of {@link Message})
     *
     * @throws IOException if the given header cannot be serialized by the given serializer
     */
    private InfomodelMessageBuilder( Message header ) throws IOException {
        this.builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        builder.addFormDataPart(MultipartDatapart.HEADER.name(), serializer.serialize(header));
    }

    /**
     * Add a String payload to the builder.
     *
     * @param payload the (String) payload that is added to the MultipartMessages Payload
     */
    private void addPayload( String payload ) {
        builder.addFormDataPart(MultipartDatapart.PAYLOAD.name(), payload);
    }

    /**
     * Add a File payload to the builder.
     *
     * @param file     the File that is added to the MultipartMessages payload
     * @param fileType the MediaType of the file
     */
    private void addPayload( File file, MediaType fileType ) {
        builder.addFormDataPart(MultipartDatapart.PAYLOAD.name(), file.getName(), RequestBody.create(file, fileType));
    }

    /**
     * Getter for the built multipart message as OkHttp {@link MultipartBody}
     *
     * @return the built Message as OkHttp {@link MultipartBody}
     */
    private MultipartBody getRequestBody() {
        return builder.build();
    }
}
