package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.messaging.protocol.RequestBuilder;
import de.fraunhofer.ids.framework.util.MultipartDatapart;
import okhttp3.MultipartBody;
import okhttp3.Request;

public class MultipartRequestBuilder implements RequestBuilder {

    private static final Serializer SERIALIZER = new Serializer();



    /**
     * {@inheritDoc}
     */
    @Override
    public Request build(final Message message, final URI target) throws IOException {
        final var body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(MultipartDatapart.HEADER.toString(), SERIALIZER.serialize(message))
                .build();

        return new Request.Builder()
                .url(target.toURL())
                .post(body)
                .build();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Request build(final Message message, final URI target, final String payload) throws IOException {
        final var body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(MultipartDatapart.HEADER.toString(), SERIALIZER.serialize(message))
                .addFormDataPart(MultipartDatapart.PAYLOAD.toString(), payload)
                .build();

        return new Request.Builder()
                .url(target.toURL())
                .post(body)
                .build();
    }

}
