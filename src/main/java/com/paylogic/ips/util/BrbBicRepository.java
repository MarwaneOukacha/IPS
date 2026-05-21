package com.paylogic.ips.util;

import java.util.Arrays;
import java.util.List;

public class BrbBicRepository {

    private static final List<String> BICS = Arrays.asList(
        "BRBUBIBI",
        "BCRBBIBI",
        "BCBUBIBI",
        "BUCIBIBI",
        "IBBUBIBI",
        "BKGFBIBI",
        "FIKNBIBI",
        "ECOCBIBI",
        "DTKEBIBI",
        "KCBLBIBI",
        "CORUBIBU",
        "BCABBIBI",
        "BIJEBIBI",
        "BHBUBIBI",
        "BIDFBIBI",
        "RNPBBIBU"
    );

    public static List<String> getAllBics() {
        return BICS;
    }

    public static boolean isValidBic(String bic) {
        return BICS.contains(bic);
    }
}