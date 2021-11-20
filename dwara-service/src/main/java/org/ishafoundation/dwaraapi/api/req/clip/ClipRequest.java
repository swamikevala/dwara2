package org.ishafoundation.dwaraapi.api.req.clip;

import java.util.List;

public class ClipRequest {
    String type;
    List<String> keyWords;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(List<String> keyWords) {
        this.keyWords = keyWords;
    }
}
