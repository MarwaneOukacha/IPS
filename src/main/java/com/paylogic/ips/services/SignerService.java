package com.paylogic.ips.services;


import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.keyinfo.X509IssuerSerial;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class SignerService {

    private static final Logger LOG = Logger.getLogger(SignerService.class);

    private static final String RSA_SHA256 =
            "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    private static final String EXC_C14N =
            "http://www.w3.org/2001/10/xml-exc-c14n#";
    private static final String XADES_NS =
            "http://uri.etsi.org/01903/v1.3.2#";
    private static final String SIGNED_PROPS_TYPE =
            "http://uri.etsi.org/01903/v1.3.2#SignedProperties";
    private static final String SIGNED_PROPS_SUFFIX = "-signedprops";

    /**
     * Signs a pacs.008 DataPDU document.
     * Adds <Sgntr><ds:Signature> inside <AppHdr>.
     *
     * @param doc               DOM Document of the full DataPDU
     * @param privateKey        your bank's private key
     * @param signerCertificate your bank's X.509 certificate
     * @return the same Document with the signature injected
     */
    public Document sign(Document doc, PrivateKey privateKey, X509Certificate signerCertificate)
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException,
            MarshalException, XMLSignatureException {

        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        // ── 1. Algorithms ────────────────────────────────────────────────────
        DigestMethod digestMethod = fac.newDigestMethod(DigestMethod.SHA256, null);

        CanonicalizationMethod c14n = fac.newCanonicalizationMethod(
                EXC_C14N, (XMLStructure) null);
        SignatureMethod signatureMethod = fac.newSignatureMethod(RSA_SHA256, null);

        // ── 2. KeyInfo — issuer + serial only (no raw cert bytes) ────────────
        KeyInfoFactory kif = fac.getKeyInfoFactory();
        X509IssuerSerial x509is = kif.newX509IssuerSerial(
                signerCertificate.getIssuerX500Principal().toString(),
                signerCertificate.getSerialNumber()
        );
        X509Data x509data = kif.newX509Data(Collections.singletonList(x509is));
        String keyInfoId = "_" + UUID.randomUUID();
        KeyInfo ki = kif.newKeyInfo(Collections.singletonList(x509data), keyInfoId);

        // ── 3. References ────────────────────────────────────────────────────
        List<Reference> refs = new ArrayList<>();

        // Ref 1 — digest of KeyInfo element
        Reference ref1 = fac.newReference(
                "#" + keyInfoId,
                digestMethod,
                Collections.singletonList(
                        fac.newTransform(EXC_C14N, (XMLStructure) null)),
                null,
                null
        );
        refs.add(ref1);

        // Ref 2 — digest of SignedProperties (XAdES signing time)
        String signedPropsId = "_" + UUID.randomUUID() + SIGNED_PROPS_SUFFIX;
        Reference ref2 = fac.newReference(
                "#" + signedPropsId,
                digestMethod,
                Collections.singletonList(
                        fac.newTransform(EXC_C14N, (XMLStructure) null)),
                SIGNED_PROPS_TYPE,
                null
        );
        refs.add(ref2);

        // Ref 3 — digest of the <Document> payload (no URI → custom dereferencer)
        Reference ref3 = fac.newReference(
                "",   // empty string, not null, safer with some JDK versions
                fac.newDigestMethod(DigestMethod.SHA256, null),
                Collections.singletonList(
                        fac.newTransform(EXC_C14N, (XMLStructure) null)),
                null,
                null
        );
        refs.add(ref3);

        // ── 4. SignedInfo ────────────────────────────────────────────────────
        SignedInfo si = fac.newSignedInfo(c14n, signatureMethod, refs);

        // ── 5. <Sgntr> node — find or create inside <AppHdr> ─────────────────
        Node sgntr = findOrCreateSgntrNode(doc);

        // ── 6. DOMSignContext ────────────────────────────────────────────────
        DOMSignContext dsc = new DOMSignContext(privateKey, sgntr);
        dsc.putNamespacePrefix(XMLSignature.XMLNS, "ds");

        // Tell the signer how to resolve the empty-URI reference → <Document>
        Node documentNode = findDocumentNode(doc);
        dsc.setURIDereferencer(new DocumentNodeDereferencer(documentNode));

        // ── 7. XAdES Object — SigningTime ────────────────────────────────────
        String signatureId = "_" + UUID.randomUUID();

        Element qpElement = doc.createElementNS(XADES_NS, "xades:QualifyingProperties");
        qpElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xades", XADES_NS);
        qpElement.setAttribute("Target", "#" + signatureId);

        Element spElement = doc.createElementNS(XADES_NS, "xades:SignedProperties");
        spElement.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xades", XADES_NS);
        spElement.setAttributeNS(null, "Id", signedPropsId);
        // Register Id so the XML-DSIG engine can resolve "#signedPropsId"
        dsc.setIdAttributeNS(spElement, null, "Id");
        spElement.setIdAttributeNS(null, "Id", true);
        qpElement.appendChild(spElement);

        Element sspElement = doc.createElementNS(XADES_NS, "xades:SignedSignatureProperties");
        spElement.appendChild(sspElement);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Element stElement = doc.createElementNS(XADES_NS, "xades:SigningTime");
        stElement.appendChild(doc.createTextNode(df.format(new Date())));
        sspElement.appendChild(stElement);

        XMLObject xmlObject = fac.newXMLObject(
                Collections.singletonList(new DOMStructure(qpElement)),
                null, null, null
        );

        // ── 8. Sign ──────────────────────────────────────────────────────────
        XMLSignature signature = fac.newXMLSignature(
                si, ki,
                Collections.singletonList(xmlObject),
                signatureId,
                null
        );
        signature.sign(dsc);

        LOG.info("Document signed successfully, signatureId=" + signatureId);
        return doc;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Finds <Sgntr> inside <AppHdr>, or creates it if absent.
     * Inserts before <Rltd> if present, otherwise appends.
     */
    private Node findOrCreateSgntrNode(Document doc) {
        NodeList appHdrList = doc.getElementsByTagName("AppHdr");
        if (appHdrList.getLength() == 0) {
            throw new RuntimeException("AppHdr element not found in document");
        }
        Node appHdr = appHdrList.item(0);

        // Check if <Sgntr> already exists
        NodeList children = appHdr.getChildNodes();
        Node sgntr = null;
        Node rltdNode = null;
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if ("Sgntr".equals(child.getLocalName())) {
                    sgntr = child;
                }
                if ("Rltd".equals(child.getLocalName())) {
                    rltdNode = child;
                }
            }
        }

        if (sgntr == null) {
            // Create <Sgntr> in the same namespace as <AppHdr>
            sgntr = doc.createElementNS(appHdr.getNamespaceURI(), "Sgntr");
            if (rltdNode != null) {
                appHdr.insertBefore(sgntr, rltdNode);
            } else {
                appHdr.appendChild(sgntr);
            }
        }
        return sgntr;
    }

    /**
     * Finds the <Document> element (the pacs.008 payload).
     * This is what Ref 3 digests.
     */
    private Node findDocumentNode(Document doc) {
        NodeList list = doc.getElementsByTagName("Document");
        if (list.getLength() == 0) {
            throw new RuntimeException("Document element not found in DataPDU");
        }
        return list.item(0);
    }
}