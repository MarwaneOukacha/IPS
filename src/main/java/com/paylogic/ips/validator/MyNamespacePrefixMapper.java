package com.paylogic.ips.validator;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
public class MyNamespacePrefixMapper extends NamespacePrefixMapper {

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {

        switch (namespaceUri) {

            case "urn:cma:stp:xsd:stp.1.0":
                return ""; // DEFAULT NAMESPACE → NO PREFIX

            case "urn:iso:std:iso:20022:tech:xsd:head.001.001.02":
                return "h"; // header prefix

            case "urn:iso:std:iso:20022:tech:xsd:pacs.008.001.10":
                return "p"; // document prefix

            default:
                return suggestion;
        }
    }

    @Override
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] {
            "urn:cma:stp:xsd:stp.1.0"
        };
    }
}
