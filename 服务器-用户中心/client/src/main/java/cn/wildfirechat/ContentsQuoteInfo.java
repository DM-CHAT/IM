package cn.wildfirechat;

public class ContentsQuoteInfo {

    /**
     * data : 哦哦哦
     * mentionedType : 0
     * type : text
     * quoteInfo : {"d":"哦哦哦","i":"OSNU6ngJSuBe4M6AgvkrcA511xf2giRjjUb2qtoxBmARnGYpyQf","u":82,"n":"lxy"}
     */

    private String data;
    private int mentionedType;
    private String type;
    private QuoteInfo quoteInfo;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getMentionedType() {
        return mentionedType;
    }

    public void setMentionedType(int mentionedType) {
        this.mentionedType = mentionedType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public QuoteInfo getQuoteInfo() {
        return quoteInfo;
    }

    public void setQuoteInfo(QuoteInfo quoteInfo) {
        this.quoteInfo = quoteInfo;
    }

    public static class QuoteInfo {
        /**
         * d : 哦哦哦
         * i : OSNU6ngJSuBe4M6AgvkrcA511xf2giRjjUb2qtoxBmARnGYpyQf
         * u : 82
         * n : lxy
         */

        private String d;
        private String i;
        private int u;
        private String n;

        public String getD() {
            return d;
        }

        public void setD(String d) {
            this.d = d;
        }

        public String getI() {
            return i;
        }

        public void setI(String i) {
            this.i = i;
        }

        public int getU() {
            return u;
        }

        public void setU(int u) {
            this.u = u;
        }

        public String getN() {
            return n;
        }

        public void setN(String n) {
            this.n = n;
        }
    }
}
