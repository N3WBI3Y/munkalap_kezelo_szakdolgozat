package com.giganet.giganet_worksheet.Network.SSO;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class CertsDto {
    @SerializedName("keys")
    private final ArrayList<Key> keys;

    public CertsDto(ArrayList<Key> keys) {
        this.keys = keys;
    }

    public ArrayList<Key> getKeys() {
        return keys;
    }

    public static class Key {
        @SerializedName("kid")
        private final String kId;
        @SerializedName("kty")
        private final String kTy;
        @SerializedName("alg")
        private final String alg;
        @SerializedName("use")
        private final String use;
        @SerializedName("n")
        private final String n;
        @SerializedName("e")
        private final String e;
        @SerializedName("x5c")
        private final ArrayList<String> x5c;
        @SerializedName("x5t")
        private final String x5t;
        @SerializedName("x5t#S256")
        private final String x5thashS256;

        public Key(String kId, String kTy, String alg, String use,
                   String n, String e, ArrayList<String> x5c,
                   String x5t, String x5thashS256) {
            this.kId = kId;
            this.kTy = kTy;
            this.alg = alg;
            this.use = use;
            this.n = n;
            this.e = e;
            this.x5c = x5c;
            this.x5t = x5t;
            this.x5thashS256 = x5thashS256;
        }

        public String getkId() {
            return kId;
        }

        public String getkTy() {
            return kTy;
        }

        public String getAlg() {
            return alg;
        }

        public String getUse() {
            return use;
        }

        public String getN() {
            return n;
        }

        public String getE() {
            return e;
        }

        public ArrayList<String> getX5c() {
            return x5c;
        }

        public String getX5t() {
            return x5t;
        }

        public String getX5thashS256() {
            return x5thashS256;
        }
    }
}
