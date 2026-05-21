package com.paylogic.ips.services;


import org.w3c.dom.Node;

import javax.xml.crypto.*;
import javax.xml.crypto.dom.DOMURIReference;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Resolves the empty-URI reference in Ref 3 to the <Document> DOM node.
 * Standard XMLDSig cannot locate a node without a URI or Id attribute,
 * so we provide it manually.
 */
public class DocumentNodeDereferencer implements URIDereferencer {

    private final Node documentNode;

    public DocumentNodeDereferencer(Node documentNode) {
        this.documentNode = documentNode;
    }

    @Override
    public Data dereference(URIReference uriReference, XMLCryptoContext context)
            throws URIReferenceException {

        // Only intercept the empty-URI reference (Ref 3)
        String uri = uriReference.getURI();
        if (uri != null && !uri.isEmpty()) {
            // Let the default dereferencer handle all other references
            try {
                URIDereferencer defaultDereferencer =
                        XMLCryptoContext.class.cast(context)
                                .getURIDereferencer();
                // fallback to built-in
                return ((javax.xml.crypto.dsig.XMLSignatureFactory)
                        javax.xml.crypto.dsig.XMLSignatureFactory
                                .getInstance("DOM"))
                        .getURIDereferencer()
                        .dereference(uriReference, context);
            } catch (Exception e) {
                throw new URIReferenceException(e);
            }
        }

        // Serialize the <Document> node to bytes, then return as OctetStreamData
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(documentNode), new StreamResult(baos));
            return new OctetStreamData(new ByteArrayInputStream(baos.toByteArray()));
        } catch (Exception e) {
            throw new URIReferenceException("Failed to serialize Document node", e);
        }
    }
}